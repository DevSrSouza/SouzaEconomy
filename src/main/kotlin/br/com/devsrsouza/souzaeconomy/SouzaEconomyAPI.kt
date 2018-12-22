package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.Executor
import br.com.devsrsouza.kotlinbukkitapi.dsl.command.command
import br.com.devsrsouza.kotlinbukkitapi.extensions.text.*
import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.ICurrency
import br.com.devsrsouza.souzaeconomy.events.PreLoadCurrencyEvent
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.reflect.KClass

class SouzaEconomyAPI {

    lateinit var mainCurrency: ICurrency<out CurrencyConfig>
        internal set

    internal val currencies: MutableList<ICurrency<out CurrencyConfig>> = mutableListOf()
    internal val currenciesTypes: MutableList<CurrencyType<ICurrency<CurrencyConfig>, CurrencyConfig>> = mutableListOf()

    fun registerCurrency(currency: ICurrency<CurrencyConfig>, registerCommand: Boolean): Boolean {

        val event = PreLoadCurrencyEvent(currency, registerCommand)
                .also { SouzaEconomy.INSTANCE.server.pluginManager.callEvent(it) }

        val currency = event.currency
        val registerCommand = event.registerCommand

        if (currencies.find { it.name.equals(currency.name, true) } != null)
            return false

        currencies.add(currency)

        if (registerCommand)
            command(currency.name) {

                permission = "souzaeconomy.currency.$name.cmd"

                subCommands.addAll(currency.commands())

                val view = fun(sender: CommandSender, player: OfflinePlayer?) {
                    sender.sendMessage(currency.config.messages.viewing_player_balance
                            .replace("{balance}", (player?.let { currency.getMoney(it) } ?: 0).toString(), true))
                }

                executorPlayer {
                    if (args.isNotEmpty())
                        view(sender, Bukkit.getOfflinePlayer(args[0]))
                    else
                        sender.sendMessage(currency.config.messages.show_player_balance
                                .replace("{balance}", currency.getMoney(sender).toString(), true))

                }

                val help = fun(executor: Executor<*>) {
                    val commandsMessage = arrayListOf<Pair<Command, BaseComponent>>().apply {
                        for (subCmd in subCommands) {
                            add(subCmd to "&b/${label} &e${subCmd.name}"
                                    .showText(" ${subCmd.description}".color(ChatColor.YELLOW))
                                    .suggestCommand("/${label} ${subCmd.name}"))
                        }
                    }
                    executor.sender.sendMessage(+"&8&m-----------------------------")
                    executor.sender.sendMessage(+"&b${currency.config.displayname}-> &6Commands")
                    if (executor.sender is Player) {
                        for (message in commandsMessage) {
                            (executor.sender as Player).sendMessage(message.second.replaceAll("&", "§"))
                        }
                    } else {
                        for (message in commandsMessage) {
                            executor.sender.sendMessage(+(message.second.toLegacyText() + "&b - ${message.first.description}"))
                        }
                    }
                    executor.sender.sendMessage(+"&8&m------------------------------")
                }

                executor {
                    if (args.isNotEmpty())
                        view(sender, Bukkit.getOfflinePlayer(args[0]))
                    else
                        executor { help(this) }
                }

                command("help") {
                    aliases = listOf("ajuda")

                    executor { help(this) }
                }
            }

        return true
    }

    fun getCurrency(name: String): ICurrency<out CurrencyConfig>? {
        return currencies.find { it.name.equals(name, true) }
    }

    inline fun <reified T : ICurrency<C>, reified C : CurrencyConfig> registerCurrencyType(
            typeName: String,
            description: String,
            noinline factory: (currencyName: String, config: C) -> T): Boolean {
        return registerCurrencyType(typeName, description, T::class, C::class, factory)
    }

    fun <T : ICurrency<C>, C : CurrencyConfig> registerCurrencyType(
            typeName: String,
            description: String,
            type: KClass<T>,
            configType: KClass<C>,
            factory: (currencyName: String, config: C) -> T): Boolean {
        if (currenciesTypes.find {
                    it.typeName.equals(typeName, true) || it.currencyClass == type
                } == null) {
            currenciesTypes.add(CurrencyType(typeName, description, type, configType, factory) as CurrencyType<ICurrency<CurrencyConfig>, CurrencyConfig>)
            return true
        } else return false
    }
}