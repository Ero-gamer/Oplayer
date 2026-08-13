package one.only.player.feature.player.subtitle

import android.util.SparseArray
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.ParserException
import androidx.media3.common.util.ParsableByteArray
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.ExtractorInput
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.TrackOutput
import androidx.media3.extractor.mkv.MatroskaExtractor
import androidx.media3.extractor.text.SubtitleParser
import io.github.peerless2012.ass.media.AssHandler
import io.github.peerless2012.ass.media.type.AssRenderType
import java.lang.reflect.Field
import java.util.regex.Pattern
import one.only.player.feature.player.extensions.normalizeAssDialogueFields
import one.only.player.feature.player.extensions.normalizeAssSubtitleText

@UnstableApi
internal class NormalizingAssMatroskaExtractor(
    subtitleParserFactory: SubtitleParser.Factory,
    private val assHandler: AssHandler,
) : MatroskaExtractor(subtitleParserFactory) {

    private var currentAttachmentName: String? = null
    private var currentAttachmentMime: String? = null
    private var isCurrentTrackWebmVtt = false
    private val webmVttTrackNumbers = mutableSetOf<Int>()
    private val subtitleSample = subtitleSampleField.get(this) as ParsableByteArray

    override fun getElementType(id: Int): Int = when (id) {
        ID_ATTACHMENTS,
        ID_ATTACHED_FILE,
        -> ELEMENT_TYPE_MASTER
        ID_FILE_NAME,
        ID_FILE_MIME_TYPE,
        -> ELEMENT_TYPE_STRING
        ID_FILE_DATA -> ELEMENT_TYPE_BINARY
        else -> super.getElementType(id)
    }

    override fun isLevel1Element(id: Int): Boolean = super.isLevel1Element(id) || id == ID_ATTACHMENTS

    @Throws(ParserException::class)
    override fun startMasterElement(
        id: Int,
        contentPosition: Long,
        contentSize: Long,
    ) {
        when (id) {
            ID_EBML -> {
                wrapExtractorOutput()
                super.startMasterElement(id, contentPosition, contentSize)
            }
            ID_TRACK_ENTRY -> {
                isCurrentTrackWebmVtt = false
                super.startMasterElement(id, contentPosition, contentSize)
            }
            ID_ATTACHED_FILE -> clearAttachment()
            else -> super.startMasterElement(id, contentPosition, contentSize)
        }
    }

    @Throws(ParserException::class)
    override fun endMasterElement(id: Int) {
        when (id) {
            ID_VIDEO -> {
                val track = getCurrentTrack(id)
                assHandler.setVideoSize(track.width, track.height)
                super.endMasterElement(id)
            }
            ID_EDITION_ENTRY -> {
                super.endMasterElement(id)
                publishUpdatedTrackFormats()
            }
            ID_TRACK_ENTRY -> {
                if (isCurrentTrackWebmVtt) {
                    webmVttTrackNumbers += getCurrentTrack(id).number
                    isCurrentTrackWebmVtt = false
                }
                super.endMasterElement(id)
            }
            ID_ATTACHED_FILE -> clearAttachment()
            else -> super.endMasterElement(id)
        }
    }

    @Throws(ParserException::class)
    override fun stringElement(id: Int, value: String) {
        when (id) {
            ID_FILE_NAME -> currentAttachmentName = value
            ID_FILE_MIME_TYPE -> currentAttachmentMime = value
            ID_CODEC_ID -> {
                isCurrentTrackWebmVtt = value in WEBM_VTT_CODEC_IDS
                super.stringElement(id, if (isCurrentTrackWebmVtt) MATROSKA_VTT_CODEC_ID else value)
            }
            else -> super.stringElement(id, value)
        }
    }

    override fun binaryElement(
        id: Int,
        contentSize: Int,
        input: ExtractorInput,
    ) {
        if (id == ID_BLOCK) {
            super.binaryElement(id, contentSize, input)
            maybeRewriteWebmVttSample()
            return
        }
        if (id != ID_FILE_DATA) {
            super.binaryElement(id, contentSize, input)
            return
        }

        val name = requireNotNull(currentAttachmentName)
        val mime = requireNotNull(currentAttachmentMime)
        if (mime in FONT_MIME_TYPES) {
            val data = ByteArray(contentSize)
            input.readFully(data, 0, contentSize)
            assHandler.addFont(name, data)
        } else {
            input.skipFully(contentSize)
        }
    }

    private fun wrapExtractorOutput() {
        if (assHandler.renderType == AssRenderType.CUES) return

        val output = extractorOutputField.get(this) as ExtractorOutput
        if (output is NormalizingAssSubtitleExtractorOutput) return

        extractorOutputField.set(
            this,
            NormalizingAssSubtitleExtractorOutput(
                delegate = output,
                assHandler = assHandler,
                extractor = this,
            ),
        )
    }

    private fun clearAttachment() {
        currentAttachmentName = null
        currentAttachmentMime = null
    }

    /**
     * WebM 规范（ffmpeg / Chrome 产出）内嵌 VTT 的 block 载荷为 “cue id\ncue settings\ncue text”，
     * 与 Matroska 规范的 S_TEXT/WEBVTT 不同。MatroskaExtractor 已把载荷写入 subtitleSample 并加上
     * “WEBVTT\n\n00:00:00.000 --> 00:00:00.000\n” 前缀，这里去掉 id 行并把 settings 合并到时间码行，
     * 使 WebvttParser 能正确解析。结束时间码由 MatroskaExtractor 在固定偏移处回写，不能改动前缀区。
     */
    private fun maybeRewriteWebmVttSample() {
        if (webmVttTrackNumbers.isEmpty()) return
        if (blockTrackNumberField.getInt(this) !in webmVttTrackNumbers) return

        val limit = subtitleSample.limit()
        if (limit <= VTT_SAMPLE_PREFIX_LENGTH) return

        val data = subtitleSample.data
        var idEnd = -1
        var settingsEnd = -1
        for (index in VTT_SAMPLE_PREFIX_LENGTH until limit) {
            if (data[index] != NEWLINE) continue
            if (idEnd < 0) {
                idEnd = index
            } else {
                settingsEnd = index
                break
            }
        }
        if (idEnd < 0 || settingsEnd < 0) return

        val headerLength = VTT_SAMPLE_PREFIX_LENGTH - 1
        val settingsLength = settingsEnd - idEnd - 1
        val settingsExtra = if (settingsLength > 0) settingsLength + 1 else 0
        val textLength = limit - settingsEnd - 1
        val rewritten = ByteArray(headerLength + settingsExtra + 1 + textLength)
        data.copyInto(rewritten, destinationOffset = 0, startIndex = 0, endIndex = headerLength)
        var offset = headerLength
        if (settingsLength > 0) {
            rewritten[offset] = SPACE
            data.copyInto(rewritten, destinationOffset = offset + 1, startIndex = idEnd + 1, endIndex = settingsEnd)
            offset += settingsLength + 1
        }
        rewritten[offset] = NEWLINE
        offset++
        data.copyInto(rewritten, destinationOffset = offset, startIndex = settingsEnd + 1, endIndex = limit)
        subtitleSample.reset(rewritten, rewritten.size)
    }

    private fun publishUpdatedTrackFormats() {
        val tracks = tracksField.get(this) as SparseArray<*>
        for (index in 0 until tracks.size()) {
            val track = tracks.valueAt(index) as Track
            track.output.format(track.format)
        }
    }

    internal fun getSubtitleSample(): ParsableByteArray = subtitleSample

    companion object {
        private const val ID_EBML = 0x1A45DFA3
        private const val ID_VIDEO = 0xE0
        private const val ID_EDITION_ENTRY = 0x45B9
        private const val ID_ATTACHMENTS = 0x1941A469
        private const val ID_ATTACHED_FILE = 0x61A7
        private const val ID_FILE_NAME = 0x466E
        private const val ID_FILE_MIME_TYPE = 0x4660
        private const val ID_FILE_DATA = 0x465C
        private const val ID_TRACK_ENTRY = 0xAE
        private const val ID_CODEC_ID = 0x86
        private const val ID_BLOCK = 0xA1

        private const val ELEMENT_TYPE_MASTER = 1
        private const val ELEMENT_TYPE_STRING = 3
        private const val ELEMENT_TYPE_BINARY = 4

        private const val MATROSKA_VTT_CODEC_ID = "S_TEXT/WEBVTT"

        // “WEBVTT\n\n00:00:00.000 --> 00:00:00.000\n” 的字节长度，与 MatroskaExtractor.VTT_PREFIX 一致
        private const val VTT_SAMPLE_PREFIX_LENGTH = 38

        private const val NEWLINE = '\n'.code.toByte()
        private const val SPACE = ' '.code.toByte()

        private val WEBM_VTT_CODEC_IDS = setOf(
            "D_WEBVTT/SUBTITLES",
            "D_WEBVTT/CAPTIONS",
            "D_WEBVTT/DESCRIPTIONS",
        )

        private val FONT_MIME_TYPES = setOf(
            "font/ttf",
            "font/otf",
            "font/sfnt",
            "font/woff",
            "font/woff2",
            "application/font-sfnt",
            "application/font-woff",
            "application/x-truetype-font",
            "application/vnd.ms-opentype",
            "application/x-font-ttf",
        )

        private val extractorOutputField: Field = MatroskaExtractor::class.java.getDeclaredField("extractorOutput").apply {
            isAccessible = true
        }

        private val subtitleSampleField: Field = MatroskaExtractor::class.java.getDeclaredField("subtitleSample").apply {
            isAccessible = true
        }

        private val tracksField: Field = MatroskaExtractor::class.java.getDeclaredField("tracks").apply {
            isAccessible = true
        }

        private val blockTrackNumberField: Field = MatroskaExtractor::class.java.getDeclaredField("blockTrackNumber").apply {
            isAccessible = true
        }
    }
}

@UnstableApi
private class NormalizingAssSubtitleExtractorOutput(
    private val delegate: ExtractorOutput,
    private val assHandler: AssHandler,
    private val extractor: NormalizingAssMatroskaExtractor,
) : ExtractorOutput {

    override fun track(id: Int, type: Int): TrackOutput {
        val output = delegate.track(id, type)
        return if (type == C.TRACK_TYPE_TEXT) {
            NormalizingAssTrackOutput(
                delegate = output,
                assHandler = assHandler,
                extractor = extractor,
            )
        } else {
            output
        }
    }

    override fun endTracks() = delegate.endTracks()

    override fun seekMap(seekMap: androidx.media3.extractor.SeekMap) = delegate.seekMap(seekMap)
}

@UnstableApi
private class NormalizingAssTrackOutput(
    private val delegate: TrackOutput,
    private val assHandler: AssHandler,
    private val extractor: NormalizingAssMatroskaExtractor,
) : TrackOutput {

    private var isAss = false
    private var trackId: String? = null

    override fun format(format: Format) {
        isAss = format.sampleMimeType == MimeTypes.TEXT_SSA || format.codecs == MimeTypes.TEXT_SSA
        trackId = format.id
        val normalizedFormat = if (isAss) format.normalizeAssHeader() else format
        delegate.format(normalizedFormat)
    }

    override fun sampleData(
        input: androidx.media3.common.DataReader,
        length: Int,
        allowEndOfInput: Boolean,
        sampleDataPart: Int,
    ): Int = delegate.sampleData(input, length, allowEndOfInput, sampleDataPart)

    override fun sampleData(
        data: ParsableByteArray,
        length: Int,
        sampleDataPart: Int,
    ) = delegate.sampleData(data, length, sampleDataPart)

    override fun sampleMetadata(
        timeUs: Long,
        flags: Int,
        size: Int,
        offset: Int,
        cryptoData: TrackOutput.CryptoData?,
    ) {
        if (isAss && timeUs != C.TIME_UNSET) {
            readAssDialogue(timeUs)
        }
        delegate.sampleMetadata(timeUs, flags, size, offset, cryptoData)
    }

    private fun readAssDialogue(timeUs: Long) {
        val trackId = trackId ?: return
        val sample = extractor.getSubtitleSample()
        val data = sample.data
        val firstComma = findTokenIndex(data, 1)
        val secondComma = findTokenIndex(data, 2)
        if (firstComma <= 0 || secondComma <= firstComma) return

        val endTimeUs = parseTimecodeUs(data.decodeToString(firstComma, secondComma - 1))
        if (endTimeUs == C.TIME_UNSET) return

        val normalizedData = normalizeSampleData(data, secondComma, sample.limit())
        assHandler.readTrackDialogue(
            trackId,
            timeUs / 1000,
            endTimeUs / 1000,
            normalizedData,
            secondComma,
            normalizedData.size - secondComma,
        )
    }

    private fun normalizeSampleData(
        data: ByteArray,
        textOffset: Int,
        limit: Int,
    ): ByteArray {
        val fields = data.decodeToString(textOffset, limit)
        val normalizedFields = fields.normalizeAssDialogueFields(ASS_MATROSKA_TEXT_INDEX)
        if (normalizedFields == fields) return data.copyOf(limit)

        val normalizedFieldBytes = normalizedFields.encodeToByteArray()
        return ByteArray(textOffset + normalizedFieldBytes.size).also { normalizedData ->
            data.copyInto(normalizedData, endIndex = textOffset)
            normalizedFieldBytes.copyInto(normalizedData, destinationOffset = textOffset)
        }
    }

    private fun Format.normalizeAssHeader(): Format {
        if (initializationData.isEmpty()) return this

        var hasChanges = false
        val normalizedInitializationData = initializationData.map { data ->
            val sourceText = data.decodeToString()
            val normalizedText = sourceText.normalizeAssSubtitleText()
            if (normalizedText != sourceText) {
                hasChanges = true
                normalizedText.encodeToByteArray()
            } else {
                data
            }
        }
        if (!hasChanges) return this

        return buildUpon()
            .setInitializationData(normalizedInitializationData)
            .build()
    }

    private fun findTokenIndex(data: ByteArray, token: Int): Int {
        if (token == 0) return 0

        var count = 0
        for (index in data.indices) {
            if (data[index] == COMMA) {
                count++
                if (count == token) return index + 1
            }
        }
        return 0
    }

    private fun parseTimecodeUs(timecode: String): Long {
        val matcher = SSA_TIMECODE_PATTERN.matcher(timecode.trim())
        if (!matcher.matches()) return C.TIME_UNSET

        val hours = matcher.group(1)?.toLongOrNull() ?: 0L
        val minutes = matcher.group(2)?.toLongOrNull() ?: return C.TIME_UNSET
        val seconds = matcher.group(3)?.toLongOrNull() ?: return C.TIME_UNSET
        val hundredths = matcher.group(4)?.toLongOrNull() ?: return C.TIME_UNSET
        return ((hours * 60 + minutes) * 60 + seconds) * 1_000_000 + hundredths * 10_000
    }

    companion object {
        private const val COMMA = ','.code.toByte()
        private const val ASS_MATROSKA_TEXT_INDEX = 8
        private val SSA_TIMECODE_PATTERN: Pattern = Pattern.compile("(?:(\\d+):)?(\\d+):(\\d+)[:.](\\d+)")
    }
}
