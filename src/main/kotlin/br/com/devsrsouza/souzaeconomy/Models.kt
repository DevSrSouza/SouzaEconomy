package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.ICurrency
import org.bukkit.OfflinePlayer
import kotlin.reflect.KClass

class CurrencyType<T : ICurrency<C>, C : CurrencyConfig>(
        val typeName: String,
        val description: String,
        val currencyClass: KClass<T>,
        val currencyConfigClass: KClass<C>,
        val factory: (currencyName: String, config: C) -> T
)

class Transaction(val amount: Long, val currency: ICurrency<*>) {

    fun pay(payer: OfflinePlayer, receiver: OfflinePlayer): Boolean {
        if (remove(payer)) {
            add(receiver)
            return true
        } else return false
    }

    fun remove(player: OfflinePlayer): Boolean {
        return currency.removeMoney(player, amount)
    }

    fun add(player: OfflinePlayer): Long {
        return currency.addMoney(player, amount)
    }

    fun has(player: OfflinePlayer) : Boolean {
        return currency.getMoney(player) >= amount
    }

    operator fun times(x: Int) = Transaction(amount * x, currency)

    operator fun plus(x: Int) = Transaction(amount + x, currency)

    fun revenue(percent: Int) = Transaction(amount * ((percent.toDouble() * 0.01) + 1).toLong(), currency)

    override fun toString() = "${currency.name};$amount"
}