package br.com.devsrsouza.souzaeconomy.utils

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.Executor
import br.com.devsrsouza.kotlinbukkitapi.dsl.command.arguments.string
import br.com.devsrsouza.kotlinbukkitapi.dsl.command.exception
import br.com.devsrsouza.kotlinbukkitapi.dsl.config.serializable
import br.com.devsrsouza.souzaeconomy.CurrencyType
import br.com.devsrsouza.souzaeconomy.SouzaEconomy
import br.com.devsrsouza.souzaeconomy.Transaction
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

private fun String.transactionFromString(): Transaction? {
    val slices = split(";")
    return slices.getOrNull(0)?.toLongOrNull()?.let { amount ->
        slices.getOrNull(1)?.let { SouzaEconomy.API.getCurrency(it) }?.transaction(amount)
    }
}

fun transactionSerializer(transaction: Transaction) = serializable(transaction) {
    load {
        (it as? String)?.run {
            transactionFromString()
        } ?: default
    }
    save { toString() }
}

fun transactionListSerializer(transactions: List<Transaction>) = serializable(transactions.toMutableList()) {
    load {
        (it as? List<String>)?.run {
            it.mapNotNull {
                it.transactionFromString()
            }.toMutableList()
        } ?: default
    }
    save { map { it.toString() } }
}