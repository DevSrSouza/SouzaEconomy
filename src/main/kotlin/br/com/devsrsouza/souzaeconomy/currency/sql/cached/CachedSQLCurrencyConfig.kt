package br.com.devsrsouza.souzaeconomy.currency.sql.cached

import br.com.devsrsouza.kotlinbukkitapi.utils.time.second
import br.com.devsrsouza.souzaeconomy.currency.sql.SQLCurrencyConfig

open class CachedSQLCurrencyConfig : SQLCurrencyConfig() {
    open var cache = CurrencyCacheConfig()
}

open class CurrencyCacheConfig {
    var update_delay = 10.second.toTick()
}
