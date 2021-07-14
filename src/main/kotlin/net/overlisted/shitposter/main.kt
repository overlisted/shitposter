package net.overlisted.shitposter

import com.jessecorbett.diskord.bot.*
import kotlin.concurrent.timer

const val BOT_ID = "786739224643895326"
val DEFAULT_CONFIG = GuildConfig(10000, mutableSetOf())
const val CREATOR_ID = "239386885766512650"
const val AUTOSAVE_INTERVAL = 60000
const val MIN_COOLDOWN = 500

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
                if (it.user?.id == BOT_ID) {
                    sp.addGuild(it.guildId, DEFAULT_CONFIG)
                }
            }

            onGuildMemberRemove {
                if (it.user.id == BOT_ID) {
                    sp.removeGuild(it.guildId)
                }
            }

            onMessageCreate {
                val id = it.guildId

                if (id != null) {
                    if (it.author.id == BOT_ID) {
                        return@onMessageCreate
                    }

                    var config = sp.getConfig(id)

                    if (config == null) {
                        sp.addGuild(id, DEFAULT_CONFIG)

                        config = sp.getConfig(id)!!
                    }

                    if (!config.allowedChannels.contains(it.channelId)) {
                        return@onMessageCreate
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

                if (id != null && cooldown != null) {
                    if (cooldown < MIN_COOLDOWN) {
                        it.respond("Cooldown can't be less than $MIN_COOLDOWN milliseconds! (or else you will be able to get me rate limited)")
                    } else {
                        sp.setCooldown(id, cooldown)

                        it.respond("Set the cooldown to $cooldown milliseconds (${cooldown / 1000} seconds)!")
                    }
                }
            }
            command("stop") {
                if (it.author.id == CREATOR_ID) {
                    it.respond("Shutting down!")

                    sp.saveConfig()
                    bot.shutdown()
                } else {
                    it.respond("Only the creator of the bot can do this")
                }
            }
            command("allow") {
                val channel = it.content.split(" ")[2]
                val id = it.guildId

                if (id != null) {
                    sp.allowChannel(id, channel)
                }
            }
            command("disallow") {
                val channel = it.content.split(" ")[2]
                val id = it.guildId

                if (id != null) {
                    sp.disallowChannel(id, channel)
                }
            }

            command("allowhere") {
                val id = it.guildId

                if (id != null) {
                    sp.allowChannel(id, it.channelId)
                }
            }
            command("disallowhere") {
                val id = it.guildId

                if (id != null) {
                    sp.disallowChannel(id, it.channelId)
                }
            }
        }
    }
}
