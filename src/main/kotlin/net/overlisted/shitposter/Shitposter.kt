package net.overlisted.shitposter

import com.jessecorbett.diskord.api.channel.ChannelClient
import com.jessecorbett.diskord.util.sendMessage
import java.io.File

data class GuildConfig(var cooldown: Int, var allowedChannels: MutableSet<String>)

class Shitposter {
    private val adjectives: List<String>
    private val nouns: List<String>
    private var config: MutableMap<String, GuildConfig>

    init {
        val adjectivesTxt = readResource("english-adjectives.txt")
        val nounsTxt = readResource("english-nouns.txt")

        config = parseConfig(readOrMakeFile("config.txt"))

        if (adjectivesTxt != null && nounsTxt != null) {
            adjectives = adjectivesTxt.split("\n")
            nouns = nounsTxt.split("\n")
        } else throw Exception("Can't load the dictionaries!")
    }

    private fun parseConfig(text: String): MutableMap<String, GuildConfig> =
        text.lines().filter { it.isNotEmpty() }.associate {
            val parts = it.split("/")

            if (parts.size != 3) throw java.lang.Exception("Bad syntax of config.txt")

            Pair(parts[0], GuildConfig(parts[1].toInt(), if (parts[2].isNotEmpty()) parts[2].split(",").toMutableSet() else mutableSetOf()))
        }.toMutableMap()

    private fun serializeConfig(config: Map<String, GuildConfig>): String =
        config.map {
            "${it.key}/${it.value.cooldown}/${it.value.allowedChannels.joinToString(",")}"
        }.joinToString("\n")

    fun saveConfig() = File("config.txt").writeText(serializeConfig(config))

    fun addGuild(guild: String, defaultConfig: GuildConfig) {
        config[guild] = defaultConfig
    }

    fun removeGuild(guild: String) {
        config.remove(guild)
    }

    fun allowChannel(guild: String, channel: String) {
        config[guild]?.allowedChannels?.add(channel)
    }

    fun disallowChannel(guild: String, channel: String) {
        config[guild]?.allowedChannels?.remove(channel)
    }

    fun getConfig(guild: String) = config[guild]

    fun setCooldown(guild: String, cooldown: Int) {
        config[guild]?.cooldown = cooldown
    }

    suspend fun doShit(channel: ChannelClient) {
        val adj = adjectives[(Math.random() * adjectives.size).toInt()]
        val noun = nouns[(Math.random() * nouns.size).toInt()]

        channel.sendMessage("$adj $noun.")
    }
}
