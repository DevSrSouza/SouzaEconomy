package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.command
import br.com.devsrsouza.souzaeconomy.currency.Currency
import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig
import kotlin.reflect.KClass

class SouzaEconomyAPI {

    internal val currencies: MutableList<Currency<out CurrencyConfig>> = mutableListOf()
    internal val currenciesTypes: MutableList<CurrencyType<Currency<CurrencyConfig>, CurrencyConfig>> = mutableListOf()

    fun registerCurrency(currency: Currency<CurrencyConfig>, registerCommand: Boolean) : Boolean {
        if(currencies.find { it.name.equals(currency.name, true) } != null)
            return false

        currencies.add(currency)

        if(registerCommand)
            command(currency.name) {

                permission = "souzaeconomy.currency.$name.cmd"

                subCommands.addAll(currency.commands())
            }

        return true
    }

    fun getCurrency(name: String) : Currency<out CurrencyConfig>? {
        return currencies.find { it.name.equals(name, true) }
    }

    inline fun <reified T : Currency<C>, reified C : CurrencyConfig> registerCurrencyType(
            typeName: String,
            noinline factory: (currencyName: String, config: C) -> T): Boolean {
        return registerCurrencyType(typeName, T::class, C::class, factory)
    }

    fun <T : Currency<C>, C : CurrencyConfig> registerCurrencyType(
            typeName: String,
            type: KClass<T>,
            configType: KClass<C>,
            factory: (currencyName: String, config: C) -> T): Boolean {
        if (currenciesTypes.find { it.typeName.equals(typeName, true) } == null) {
            currenciesTypes.add(CurrencyType(typeName, type, configType, factory) as CurrencyType<Currency<CurrencyConfig>, CurrencyConfig>)
            return true
        } else return false
    }
}