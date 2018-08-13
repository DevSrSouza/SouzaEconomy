package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.souzaeconomy.currency.Currency
import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.reflect.KClass

class SouzaEconomyConfig(val file: File) : YamlConfiguration() {

    init { load(file) }

    fun save(): SouzaEconomyConfig {
        save(file)
        return this
    }

    fun reload(): SouzaEconomyConfig {
        load(file)
        return this
    }
}

class CurrencyType<T : Currency<C>, C : CurrencyConfig>(
        val typeName: String,
        val description: String,
        val currencyClass: KClass<T>,
        val currencyConfigClass: KClass<C>,
        val factory: (currencyName: String, config: C) -> T
)

class Transaction(val amount: Long, val currency: Currency<*>) {

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
}