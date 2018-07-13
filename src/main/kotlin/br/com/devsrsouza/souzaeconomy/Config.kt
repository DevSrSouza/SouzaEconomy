package br.com.devsrsouza.souzaeconomy

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