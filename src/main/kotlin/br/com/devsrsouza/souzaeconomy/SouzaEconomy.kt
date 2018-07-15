package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.command
import br.com.devsrsouza.kotlinbukkitapi.dsl.config.loadAndSetDefault
import br.com.devsrsouza.kotlinbukkitapi.extensions.text.*
import br.com.devsrsouza.souzaeconomy.currency.Currency
import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.sql.SQLCurrency
import br.com.devsrsouza.souzaeconomy.currency.sql.SQLCurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.sql.cached.CachedSQLCurrency
import br.com.devsrsouza.souzaeconomy.currency.sql.cached.CachedSQLCurrencyConfig
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.lang.Exception
import kotlin.reflect.KClass

class SouzaEconomy : JavaPlugin() {

    companion object {
        @JvmStatic
        internal lateinit var INSTANCE: SouzaEconomy
            private set
        @JvmStatic
        lateinit var API: SouzaEconomyAPI
            private set
    }

    lateinit var config: SouzaEconomyConfig

    override fun onLoad() {
        INSTANCE = this
        API = SouzaEconomyAPI()
    }

    override fun onEnable() {
        config = File(dataFolder.apply { mkdirs() }, "config.yml")
                .apply { if (!exists()) createNewFile() }
                .let { SouzaEconomyConfig(it) }
                .apply { if (loadAndSetDefault(Config::class) > 0) save() }

        API.registerCurrencyType<SQLCurrency<SQLCurrencyConfig>, SQLCurrencyConfig>("SQL", "Currency based on SQL database") {
            name, config->
            SQLCurrency(name, config)
        }

        API.registerCurrencyType<CachedSQLCurrency<CachedSQLCurrencyConfig>, CachedSQLCurrencyConfig>("CachedSQL", "Currency based on SQL database with local cache") {
            name, config->
            CachedSQLCurrency(name, config)
        }

        loadCurrencies()

        command("souzaeconomy") {
            aliases = listOf("se")
            permission = "souzaeconomy.cmd"
            description = "SouzaEconomy configuration command"

            command("currency") {
                aliases = listOf("c")
                permission += ".$name"

                command("create") {
                    // [name] [type] [optional: enable command]
                    // create a base currency and made owner load after
                }

                command("load") {

                }

                command("typelist") {

                }

                command("list") {
                    permission += ".$name"
                    description = ""

                    executor {
                        sender.sendMessage(+"&8&m------------------------------")
                        sender.sendMessage(+"&bSouzaEconomy-> &6Currencies")
                        for (currency in API.currencies) {
                            sender.sendMessage("&b${currency.name} &7${currency.type}")
                        }
                        sender.sendMessage(+"&8&m------------------------------")
                    }
                }
            }

            command("report") {
                // TODO /se report [currency name]
                aliases = listOf("r")
                permission = ".$name"
                description = ""
            }

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
    }

    private fun loadCurrencies() {
        for((name, config) in  Config.currencies.also { println(it) }) {
            loadCurrency(name, config)
        }
    }

    private fun loadCurrency(name: String, config: CurrencyByConfig) {
        val type: CurrencyType<Currency<CurrencyConfig>, CurrencyConfig>? = API.currenciesTypes
                .find { it.typeName.equals(config.type, true) }
        if (type != null) {
            val file = File(dataFolder, "currencies/$name.yml")
                    .apply {
                        parentFile.mkdirs()
                        if (!exists()) createNewFile()
                    }
                    .let { SouzaEconomyConfig(it) }
            val configurationCurrency = type.currencyConfigClass.constructors.first().call()
            file.apply {
                if (loadAndSetDefault(type.currencyConfigClass, configurationCurrency) > 0)
                    save()
            }
            try {
                val currency: Currency<CurrencyConfig> = type.factory(name, configurationCurrency)
                API.registerCurrency(currency, config.enable_command)
                logger.info("Currency ${currency.name} loaded.")
            } catch (e: Throwable) {
                logger.severe("Can't load currency $name.")
                e.printStackTrace()
                return
            }
        }
    }


    override fun onDisable() {}
}

