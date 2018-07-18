package br.com.devsrsouza.souzaeconomy.currency

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.KCommand
import br.com.devsrsouza.kotlinbukkitapi.utils.ExpirationList
import br.com.devsrsouza.souzaeconomy.SouzaEconomy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

fun Map<UUID, Long>.toOfflinePlayer() = mapKeys { Bukkit.getOfflinePlayer(it.value as UUID) }

abstract class Currency<C : CurrencyConfig>(open val name: String, open val config: C) {

    open val cooldown: ExpirationList<Player> by lazy { ExpirationList<Player>(SouzaEconomy.INSTANCE) }

    abstract fun getMoney(player: OfflinePlayer): Long
    abstract fun setMoney(player: OfflinePlayer, amount: Long) : Long
    abstract fun removeMoney(player: OfflinePlayer, amount: Long): Boolean
    abstract fun addMoney(player: OfflinePlayer, amount: Long) : Long
    abstract fun hasAccount(player: OfflinePlayer): Boolean

    abstract fun getTop(range: IntRange = 0..10): Map<UUID, Long>

    abstract fun onDisable()

    open fun commands() : List<KCommand> {
        val checkCooldown = fun(sender: CommandSender): Boolean {
            val cdTime = config.commands.cooldownForExecute
            if(cdTime > 0 && sender is Player) {
                val time = cooldown.missingTime(sender)
                if(time != null) {
                    sender.sendMessage("") // TODO cooldown message
                    return true
                } else {
                    cooldown.add(sender, cdTime)
                }
            }
            return false
        }
        return mutableListOf<KCommand>().apply {
            if (config.commands.add)
                add(KCommand("add").apply {
                    permission += ".$name"

                    // money add player amount
                    executor {
                        val target = args.getOrNull(0)?.let { Bukkit.getOfflinePlayer(it) }
                        val amount = args.getOrNull(1)?.toLongOrNull()

                        if(target == null)return@executor; // TODO
                        if(amount == null)return@executor; // TODO

                        addMoney(target, amount)
                        sender.sendMessage("Sucess message HERE") // TODO
                    }
                })
            if (config.commands.set)
                add(KCommand("set").apply {
                    permission += ".$name"

                    executor {
                        val target = args.getOrNull(0)?.let { Bukkit.getOfflinePlayer(it) }
                        val amount = args.getOrNull(1)?.toLongOrNull()

                        if(target == null)return@executor; // TODO
                        if(amount == null)return@executor; // TODO

                        setMoney(target, amount)
                        sender.sendMessage("Sucess message HERE") // TODO
                    }
                })
            if (config.commands.remove)
                add(KCommand("remove").apply {
                    permission += ".$name"

                    executor {
                        val target = args.getOrNull(0)?.let { Bukkit.getOfflinePlayer(it) }
                        val amount = args.getOrNull(1)?.toLongOrNull()

                        if(target == null)return@executor; // TODO
                        if(amount == null)return@executor; // TODO

                        if(removeMoney(target, amount)) {
                            sender.sendMessage("Sucess message HERE") // TODO
                        } else sender.sendMessage("Fail message HERE") // TODO

                    }
                })
            if (config.commands.reset)
                add(KCommand("reset").apply {
                    permission += ".$name"

                    executor {
                        val target = args.getOrNull(0)?.let { Bukkit.getOfflinePlayer(it) }

                        if(target == null)return@executor; // TODO

                        setMoney(target, 0)
                        sender.sendMessage("Sucess message HERE") // TODO
                    }
                })
            if (config.commands.pay)
                add(KCommand("pay").apply {
                    permission += ".$name"

                    executorPlayer {
                        val target = args.getOrNull(0)?.let { Bukkit.getOfflinePlayer(it) }
                        val amount = args.getOrNull(1)?.toLongOrNull()

                        if(target == null)return@executorPlayer; // TODO
                        if(amount == null)return@executorPlayer; // TODO

                        if(checkCooldown(sender))return@executorPlayer

                        if(removeMoney(sender, amount)) {
                            addMoney(target, amount)
                            sender.sendMessage("Sucess message HERE") // TODO
                            if(target.isOnline)
                                target.player?.sendMessage("Receibe message HERE") // TODO
                        } else sender.sendMessage("Fail message HERE") // TODO
                    }
                })
            if (config.commands.top)
                add(KCommand("top").apply {
                    permission += ".$name"

                    executor {
                        val range = args.getOrNull(0)?.toIntOrNull()?.let {
                            if(it < 1) return@executor // TODO FAIL MESSAGE
                            return@let it*5-5..it*5
                        } ?: 0..5

                        if(checkCooldown(sender))return@executor

                        val toppers = getTop(range).toOfflinePlayer()

                        // TODO send topper map
                    }
                })
        }
    }
}