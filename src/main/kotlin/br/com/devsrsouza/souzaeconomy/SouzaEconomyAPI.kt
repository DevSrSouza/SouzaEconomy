package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.command
import br.com.devsrsouza.souzaeconomy.currency.Currency
import kotlin.reflect.KClass

class SouzaEconomyAPI {

    internal val currencies: MutableList<Currency> = mutableListOf()

    fun registerCurrency(currency: Currency, registerCommand: Boolean) : Boolean {
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

    fun <T : Currency> registerCurrencyType(typeName: String,
                                            type: KClass<T>,
                                            factory: (currencyName: String) -> T) {

    }

    fun getCurrency(name: String) : Currency? {
        return currencies.find { it.name.equals(name, true) }
    }
}