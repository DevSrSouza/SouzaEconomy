package br.com.devsrsouza.souzaeconomy.currency

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.KCommand
import br.com.devsrsouza.souzaeconomy.Transaction
import org.bukkit.OfflinePlayer
import java.util.*

interface ICurrency<C : CurrencyConfig> {

    val name: String
    val config: C

    fun getMoney(player: OfflinePlayer): Long
    fun setMoney(player: OfflinePlayer, amount: Long): Long
    fun removeMoney(player: OfflinePlayer, amount: Long): Boolean
    fun addMoney(player: OfflinePlayer, amount: Long): Long
    fun hasAccount(player: OfflinePlayer): Boolean

    fun getTop(range: IntRange = 0..10): Map<UUID, Long>

    fun onDisable()

    fun transaction(amount: Long) = Transaction(amount, this)

    fun commands(): List<KCommand>
}