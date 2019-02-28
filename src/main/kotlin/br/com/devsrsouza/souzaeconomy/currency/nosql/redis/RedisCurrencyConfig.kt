package br.com.devsrsouza.souzaeconomy.currency.nosql.redis

import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig

open class RedisCurrencyConfig : CurrencyConfig() {
    open var redis = CurrencyRedisConfig()
}

open class CurrencyRedisConfig {
    open var hostname = "localhost"
    open var port: Int = 6379
    open var database = "myserver:economy"
    open var user = "souzaeconomy"
    open var password = "12345"
}