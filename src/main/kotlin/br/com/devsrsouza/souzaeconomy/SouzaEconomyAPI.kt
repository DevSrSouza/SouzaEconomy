package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.command
import br.com.devsrsouza.kotlinbukkitapi.extensions.text.*
import br.com.devsrsouza.souzaeconomy.currency.Currency
import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.Command
import org.bukkit.entity.Player
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

                executor {
                    val commandsMessage = arrayListOf<Pair<Command, BaseComponent>>().apply {
                        for (subCmd in subCommands) {
                            add(subCmd to "&b/${label} &e${subCmd.name}"
                                    .showText(" ${subCmd.description}".color(ChatColor.YELLOW))
                                    .suggestCommand("/${label} ${subCmd.name}"))
                        }
                    }
                    sender.sendMessage(+"&8&m-----------------------------")
                    sender.sendMessage(+"&bSouzaEconomy-> &6Commands")
                    if (sender is Player) {
                        for (message in commandsMessage) {
                            (sender as Player).sendMessage(message.second.replaceAll("&", "§"))
                        }
                    } else {
                        for (message in commandsMessage) {
                            sender.sendMessage(+(message.second.toLegacyText() + "&b - ${message.first.description}"))
                        }
                    }
                    sender.sendMessage(+"&8&m------------------------------")
                }
            }

        return true
    }

    fun getCurrency(name: String) : Currency<out CurrencyConfig>? {
        return currencies.find { it.name.equals(name, true) }
    }

    inline fun <reified T : Currency<C>, reified C : CurrencyConfig> registerCurrencyType(
            typeName: String,
            description: String,
            noinline factory: (currencyName: String, config: C) -> T): Boolean {
        return registerCurrencyType(typeName, description, T::class, C::class, factory)
    }

    fun <T : Currency<C>, C : CurrencyConfig> registerCurrencyType(
            typeName: String,
            description: String,
            type: KClass<T>,
            configType: KClass<C>,
            factory: (currencyName: String, config: C) -> T): Boolean {
        if (currenciesTypes.find { it.typeName.equals(typeName, true) } == null) {
            currenciesTypes.add(CurrencyType(typeName, description, type, configType, factory) as CurrencyType<Currency<CurrencyConfig>, CurrencyConfig>)
            return true
        } else return false
    }
}