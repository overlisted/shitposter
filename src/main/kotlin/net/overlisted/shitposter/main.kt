package net.overlisted.shitposter

import com.jessecorbett.diskord.api.channel.ChannelClient
import com.jessecorbett.diskord.bot.*
import com.jessecorbett.diskord.util.sendMessage
import java.io.File
import kotlin.concurrent.timer

const val BOT_ID = "786739224643895326"
val DEFAULT_CONFIG = GuildConfig(10000)
const val CREATOR_ID = "239386885766512650"
const val AUTOSAVE_INTERVAL = 60000

fun readResource(filename: String) = Shitposter::class.java.classLoader.getResource(filename)?.readText()
fun readOrMakeFile(filename: String): String {
  val file = File(filename)

  if(!file.exists()) file.createNewFile()

  return file.readText(Charsets.UTF_8)
}

data class GuildConfig(var cooldown: Int)

class Shitposter {
  private val adjectives: List<String>
  private val nouns: List<String>
  private var config: MutableMap<String, GuildConfig>

  init {
    val adjectivesTxt = readResource("english-adjectives.txt")
    val nounsTxt = readResource("english-nouns.txt")

    config = parseConfig(readOrMakeFile("config.txt"))

    if(adjectivesTxt != null && nounsTxt != null) {
      adjectives = adjectivesTxt.split("\n")
      nouns = nounsTxt.split("\n")
    } else throw Exception("Can't load the dictionaries!")
  }

  private fun parseConfig(text: String): MutableMap<String, GuildConfig> =
    text.lines().filter { it.isNotEmpty() }.associate {
      val parts = it.split("/")

      if (parts.size != 2) throw java.lang.Exception("Bad syntax of config.txt")

      Pair(parts[0], GuildConfig(parts[1].toInt()))
    }.toMutableMap()

  private fun serializeConfig(config: Map<String, GuildConfig>): String =
    config.map {
      "${it.key}/${it.value.cooldown}"
    }.joinToString("\n")

  fun saveConfig() = File("config.txt").writeText(serializeConfig(config))

  fun addGuild(guild: String, defaultConfig: GuildConfig) {
    config[guild] = defaultConfig
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

suspend fun main() {
  val token = readResource("token.txt") ?: return
  val sp = Shitposter()

  timer("Autosave", true, 0.toLong(), AUTOSAVE_INTERVAL.toLong()) {
    sp.saveConfig()
  }

  bot(token) {
    val bot = this
    val lastTimes = mutableMapOf<String, Long>()

    events {
      onGuildMemberAdd {
        if(it.user?.id == BOT_ID) {
          sp.addGuild(it.guildId, DEFAULT_CONFIG)
        }
      }

      onMessageCreate {
        val id = it.guildId

        if(id != null) {
          if(it.author.id == BOT_ID) {
            return@onMessageCreate
          }

          var config = sp.getConfig(id)

          if(config == null) {
            sp.addGuild(id, DEFAULT_CONFIG)

            config = sp.getConfig(id)!!
          }

          if (!lastTimes.containsKey(id) || System.currentTimeMillis() - config.cooldown > lastTimes[id]!!) {
            sp.doShit(it.channel)

            lastTimes[id] = System.currentTimeMillis()
          }
        }
      }
    }

    classicCommands(commandPrefix = "/sp ") {
      command("cooldown") {
        val cooldown = it.content.split(" ")[2].toIntOrNull()
        val id = it.guildId

        if(id != null && cooldown != null) {
          sp.setCooldown(id, cooldown)
        }
      }
    }
  }
}
