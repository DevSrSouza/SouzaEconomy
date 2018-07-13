package br.com.devsrsouza.souzaeconomy.currency.sql

import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig

open class SQLCurrencyConfig : CurrencyConfig() {
    open var sql = CurrencySQLConfig()
}

open class CurrencySQLConfig {
    open var type = "H2"
    open var hostname = "localhost"
    open var port: Short = 3306
    open var database = "myserver"
    open var user = "souzaeconomy"
    open var password = "12345"
}