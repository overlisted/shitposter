package net.overlisted.shitposter

import com.jessecorbett.diskord.api.channel.ChannelClient
import com.jessecorbett.diskord.bot.*
import com.jessecorbett.diskord.util.sendMessage

fun readResource(filename: String) = Shitposter::class.java.classLoader.getResource(filename)?.readText()

class Shitposter {
  private val adjectives: List<String>
  private val nouns: List<String>

  init {
    val adjectivesTxt = readResource("english-adjectives.txt")
    val nounsTxt = readResource("english-nouns.txt")

    if(adjectivesTxt != null && nounsTxt != null) {
      adjectives = adjectivesTxt.split("\n")
      nouns = nounsTxt.split("\n")
    } else throw Exception("Can't load the dictionaries!")
  }

  suspend fun doShit(channel: ChannelClient) {
    val adj = adjectives[(Math.random() * adjectives.size).toInt()]
    val noun = nouns[(Math.random() * nouns.size).toInt()]

    channel.sendMessage("$adj $noun.")
  }
}

suspend fun main() {
  val token = readResource("token.txt") ?: return

  bot(token) {
    val sp = Shitposter()

    var lastTime = System.currentTimeMillis()
    events {
      onMessageCreate {
        if (System.currentTimeMillis() - 1000 > lastTime) {
          sp.doShit(it.channel)

          lastTime = System.currentTimeMillis()
        }
      }
    }

    classicCommands(commandPrefix = "/sp ") {
      command("interval") {
        it.respond("no-op")
      }
    }
  }
}
