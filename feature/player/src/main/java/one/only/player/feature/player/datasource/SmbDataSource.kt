package one.only.player.feature.player.datasource

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import java.io.IOException
import java.io.InputStream
import java.util.EnumSet
import java.util.concurrent.TimeUnit

// 通过 smbj 读取 SMB 共享文件的 Media3 DataSource 实现
@OptIn(UnstableApi::class)
class SmbDataSource private constructor(
    private val username: String,
    private val password: String,
) : BaseDataSource(true) {

    private var client: SMBClient? = null
    private var connection: Connection? = null
    private var session: Session? = null
    private var share: DiskShare? = null
    private var smbFile: com.hierynomus.smbj.share.File? = null
    private var inputStream: InputStream? = null
    private var uri: Uri? = null
    private var bytesRemaining: Long = 0
    private var hasStartedTransfer: Boolean = false

    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri
        transferInitializing(dataSpec)

        val host = dataSpec.uri.host ?: throw IOException("SMB URI missing host")
        val port = dataSpec.uri.port.takeIf { it > 0 } ?: DEFAULT_PORT
        val pathSegments = dataSpec.uri.pathSegments
        if (pathSegments.size < 2) throw IOException("SMB URI path too short: ${dataSpec.uri}")

        val shareName = pathSegments.first()
        val filePath = pathSegments.drop(1).joinToString("\\")

        val config = SmbConfig.builder()
            .withTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .withSoTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        val smbClient = SMBClient(config)
        client = smbClient

        try {
            connection = smbClient.connect(host, port)
            val authContext = toAuthenticationContext(username, password)
            session = connection!!.authenticate(authContext)
            val diskShare = session!!.connectShare(shareName) as DiskShare
            share = diskShare

            smbFile = diskShare.openFile(
                filePath,
                EnumSet.of(AccessMask.GENERIC_READ),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                EnumSet.noneOf(com.hierynomus.mssmb2.SMB2CreateOptions::class.java),
            )

            val fileSize = smbFile!!.fileInformation.standardInformation.endOfFile
            if (dataSpec.position < 0L || dataSpec.position > fileSize) {
                throw IOException("SMB position ${dataSpec.position} is outside file size $fileSize")
            }

            // 走输入流而非逐次随机读：它整块读取并异步预取下一块，随机读会让每次 read 都多一个网络往返
            val stream = smbFile!!.inputStream
            // skip 只调整读取偏移，不产生额外请求
            stream.skip(dataSpec.position)
            inputStream = stream

            val availableBytes = fileSize - dataSpec.position
            bytesRemaining = if (dataSpec.length == C.LENGTH_UNSET.toLong()) {
                availableBytes
            } else {
                dataSpec.length.coerceAtMost(availableBytes)
            }

            transferStarted(dataSpec)
            hasStartedTransfer = true
            return bytesRemaining
        } catch (exception: Exception) {
            closeResources()
            throw if (exception is IOException) exception else IOException("Failed to open SMB file", exception)
        }
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT

        val bytesToRead = minOf(length.toLong(), bytesRemaining).toInt()
        val stream = inputStream ?: throw IOException("SMB file is not open")
        val bytesRead = stream.read(buffer, offset, bytesToRead)
        if (bytesRead == -1) return C.RESULT_END_OF_INPUT

        bytesRemaining -= bytesRead
        bytesTransferred(bytesRead)
        return bytesRead
    }

    override fun getUri(): Uri? = uri

    override fun close() {
        closeResources()
        if (hasStartedTransfer) {
            hasStartedTransfer = false
            transferEnded()
        }
    }

    private fun closeResources() {
        try {
            inputStream?.close()
        } catch (_: Exception) {
        }
        try {
            smbFile?.close()
        } catch (_: Exception) {
        }
        try {
            share?.close()
        } catch (_: Exception) {
        }
        try {
            session?.close()
        } catch (_: Exception) {
        }
        try {
            connection?.close()
        } catch (_: Exception) {
        }
        try {
            client?.close()
        } catch (_: Exception) {
        }
        smbFile = null
        inputStream = null
        share = null
        session = null
        connection = null
        client = null
        uri = null
        bytesRemaining = 0
    }

    class Factory(
        private val username: String,
        private val password: String,
    ) : DataSource.Factory {
        override fun createDataSource(): SmbDataSource = SmbDataSource(username, password)
    }

    companion object {
        private const val DEFAULT_PORT = 445
        private const val TIMEOUT_SECONDS = 30L

        private fun toAuthenticationContext(username: String, password: String): AuthenticationContext {
            if (username.isBlank()) return AuthenticationContext.anonymous()

            val domain = username.substringBefore('\\', missingDelimiterValue = "")
                .substringBefore('/', missingDelimiterValue = "")
            val account = username.substringAfterLast('\\').substringAfterLast('/')

            return AuthenticationContext(account, password.toCharArray(), domain)
        }
    }
}
