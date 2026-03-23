package com.example.onyxapp

data class Channel(
    val name: String,
    val url: String,
    val logo: String? = null,
    val group: String? = null,
    val isActive: Boolean = true,
    val id: String? = null
)

object ChannelsConfig {
    const val PC_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    fun parseM3U(source: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = source.lines()
        var currentName = ""
        var currentLogo = ""
        var currentGroup = "OTROS"

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            
            if (trimmed.startsWith("#EXTINF", ignoreCase = true)) {
                currentName = trimmed.substringAfterLast(",", "").trim()
                val logoMatch = Regex("""tvg-logo\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(trimmed)
                currentLogo = logoMatch?.groupValues?.get(1) ?: ""
                val groupMatch = Regex("""(?:group-title|group|group-id)\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(trimmed)
                currentGroup = groupMatch?.groupValues?.get(1) ?: "OTROS"
                
                if (currentName.isEmpty()) currentName = "Canal"
            } else if (!trimmed.startsWith("#") && (trimmed.startsWith("http") || trimmed.contains("://"))) {
                if (currentName.isNotEmpty()) {
                    channels.add(Channel(currentName, trimmed, currentLogo, currentGroup.uppercase().trim(), true))
                }
                currentName = ""; currentLogo = ""
            }
        }
        return channels
    }
}
