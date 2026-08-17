package one.only.player.core.model

fun AudioStreamInfo.displayCodecName(): String {
    val haystack = listOfNotNull(codecName, title, channelLayout).joinToString(" ")
    val isAtmos = haystack.containsAtmosHint()
    return when {
        haystack.isEac3() && isAtmos -> "Dolby Atmos"
        haystack.isEac3() -> "Dolby Digital Plus"
        haystack.isAc3() -> "Dolby Digital"
        haystack.isTrueHd() && isAtmos -> "Dolby TrueHD Atmos"
        haystack.isTrueHd() -> "Dolby TrueHD"
        haystack.isAc4() && isAtmos -> "Dolby Atmos"
        haystack.isAc4() -> "Dolby AC-4"
        else -> codecName
    }
}

fun String?.containsAtmosHint(): Boolean {
    val normalized = this?.lowercase() ?: return false
    return "atmos" in normalized ||
        "joc" in normalized ||
        "ec+3" in normalized
}

private fun String.isEac3(): Boolean {
    val normalized = lowercase()
    return "e-ac-3" in normalized ||
        "eac3" in normalized ||
        "a/52b" in normalized ||
        "ec+3" in normalized
}

private fun String.isAc3(): Boolean {
    if (isEac3()) return false
    val normalized = lowercase()
    return "a/52a" in normalized ||
        "ac-3" in normalized ||
        "ac3" in normalized
}

private fun String.isTrueHd(): Boolean {
    val normalized = lowercase()
    return "truehd" in normalized ||
        "true-hd" in normalized ||
        "true hd" in normalized ||
        "mlp" in normalized ||
        "meridian lossless" in normalized
}

private fun String.isAc4(): Boolean {
    val normalized = lowercase()
    return "ac-4" in normalized ||
        "ac4" in normalized
}
