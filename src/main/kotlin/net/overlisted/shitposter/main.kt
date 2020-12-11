package net.overlisted.shitposter

import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import java.lang.Exception

class Shitposter {

}

suspend fun main() {
  bot("") {
    val sp = Shitposter()

    commands {
      command("sp interval") {

        reply("OK")
      }
    }
  }
}