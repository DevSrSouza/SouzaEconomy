package br.com.devsrsouza.souzaeconomy

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class SouzaEconomyConfig(val file: File) : YamlConfiguration() {

    init { load(file) }

    fun save() {
        save(file)
    }
}

object Config {

    var vault = "money"

    var currencies: MutableMap<String, CurrencyByConfig> = mutableMapOf(
            "Money" to CurrencyByConfig().apply { type = "CachedSQL" },
            "Cash" to CurrencyByConfig()
    )
}

class CurrencyByConfig {
    var type = "SQL" // or CachedSQL
    var enable_command = true
}