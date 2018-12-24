package br.com.devsrsouza.souzaeconomy.utils

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.Executor
import br.com.devsrsouza.kotlinbukkitapi.dsl.command.arguments.string
import br.com.devsrsouza.kotlinbukkitapi.dsl.command.exception
import br.com.devsrsouza.souzaeconomy.CurrencyType
import br.com.devsrsouza.souzaeconomy.SouzaEconomy
import br.com.devsrsouza.souzaeconomy.currency.ICurrency
import net.md_5.bungee.api.chat.BaseComponent

fun Executor<*>.currencyOrNull(
        index: Int,
        argMissing: BaseComponent
): ICurrency<*>? = SouzaEconomy.API.currencies.find {
    it.name.equals(string(index, argMissing), true)
}

fun Executor<*>.currency(
        index: Int,
        argMissing: BaseComponent,
        notFound: BaseComponent
): ICurrency<*> = currencyOrNull(index, argMissing) ?: exception(notFound)

fun Executor<*>.currencyTypeOrNull(
        index: Int,
        argMissing: BaseComponent
): CurrencyType<*, *>? = SouzaEconomy.API.currenciesTypes.find {
    it.typeName.equals(string(index, argMissing), true)
}

fun Executor<*>.currencyType(
        index: Int,
        argMissing: BaseComponent,
        notFound: BaseComponent
): CurrencyType<*, *> = currencyTypeOrNull(index, argMissing) ?: exception(notFound)