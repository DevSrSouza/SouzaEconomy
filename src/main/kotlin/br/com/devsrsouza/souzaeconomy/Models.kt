package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.souzaeconomy.currency.Currency
import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig
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