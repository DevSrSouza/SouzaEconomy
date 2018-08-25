package br.com.devsrsouza.souzaeconomy.currency.sql

import br.com.devsrsouza.souzaeconomy.currency.Currency
import org.bukkit.OfflinePlayer
import java.util.*

open class SQLCurrency<C : SQLCurrencyConfig> : Currency<C> {

    private val controller: SQLController
    val registry: SQLControllerRegistry

    constructor(name: String, configuration: C = SQLCurrencyConfig() as C)
            : this(name, configuration, SQLController("se_$name", configuration.sql)) {
        controller.init()
    }

    constructor(name: String, configuration: C = SQLCurrencyConfig() as C, controller: SQLController)
            : super(name, configuration) {
        this.controller = controller
        registry = controller.registerCurrency(this as SQLCurrency<SQLCurrencyConfig>)
    }

    override fun getMoney(player: OfflinePlayer): Long {
        return getMoneyIfHasAccount(player) ?: 0
    }

    override fun setMoney(player: OfflinePlayer, amount: Long): Long {
        return registry.setBalance(player, amount)
    }

    override fun removeMoney(player: OfflinePlayer, amount: Long): Boolean {
        return registry.removeBalance(player, amount)
    }

    override fun addMoney(player: OfflinePlayer, amount: Long): Long {
        return registry.addBalance(player, amount)
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return getMoneyIfHasAccount(player) != null
    }

    override fun getTop(range: IntRange): Map<UUID, Long> {
        return registry.getTop(range)
    }

    open fun getMoneyIfHasAccount(player: OfflinePlayer): Long? {
        return registry.getMoneyIfHasAccount(player)
    }

    override fun onDisable() {
        controller.dataSource.close()
    }
}
