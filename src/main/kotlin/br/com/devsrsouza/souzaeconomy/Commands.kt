package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.Executor
import br.com.devsrsouza.kotlinbukkitapi.dsl.command.KCommand
import br.com.devsrsouza.kotlinbukkitapi.dsl.command.command
import br.com.devsrsouza.kotlinbukkitapi.dsl.config.loadAndSetDefault
import br.com.devsrsouza.kotlinbukkitapi.dsl.config.saveFrom
import br.com.devsrsouza.kotlinbukkitapi.extensions.text.*
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.Command
import org.bukkit.entity.Player
import java.io.File

internal fun SouzaEconomy.commands() {
    command("souzaeconomy") {
        aliases = listOf("se")
        permission = "souzaeconomy.cmd"
        description = "SouzaEconomy configuration command"

        command("currency") {
            aliases = listOf("c")
            permission += ".$name"
            description = ""

            command("create") {
                aliases = listOf("c")
                permission += ".$name"
                description = ""
                // create a base currency and made owner load after
                executor {
                    if (args.size > 1) {
                        val name = args[0].takeIf { name ->
                            SouzaEconomy.API.currencies.find { it.name.equals(name, true) } == null
                        }
                        val type = args[1].let { type ->
                            SouzaEconomy.API.currenciesTypes.find { it.typeName.equals(type, true) }
                        }

                        if (name == null) {
                            sender.sendMessage("Have a currency registred with this name")
                            return@executor
                        }
                        if (type == null) {
                            sender.sendMessage("Type not found, use /souzaeconomy currency typelist to list of types")
                            return@executor
                        }

                        val arg2 = args.getOrNull(2)
                        val enableCommand = arg2?.toBooleanOrNull()

                        if (arg2 != null && enableCommand == null) {
                            sender.sendMessage("&cEnable command argument need be &4TRUE &cor &4FALSE")
                            return@executor
                        }

                        val configuration = CurrencyByConfig().also {
                            it.type = type.typeName
                            if (enableCommand != null)
                                it.enable_command = enableCommand
                        }

                        Config.currencies.put(name, configuration)

                        config.saveFrom(Config::class)
                        config.save()

                        val file = File(dataFolder, "currencies/$name.yml")
                                .apply {
                                    parentFile.mkdirs()
                                    if (!exists()) createNewFile()
                                }
                                .let { SouzaEconomyConfig(it) }
                        file.apply {
                            if (loadAndSetDefault(type.currencyConfigClass) > 0)
                                save()
                        }

                        sender.sendMessage("Configs generated on plugins/SouzaEconomy/currencies/$name.yml")
                        sender.sendMessage("After the configuration, load the currency with /souzaeconomy currency load")
                    } else {
                        sender.sendMessage("/$label [name] [type] [optional: enable command]")
                    }
                }
            }

            command("load") {
                // [name]
                aliases = listOf("l")
                permission += ".$name"
                description = ""

                executor {
                    val name = args.getOrNull(0)

                    if (name != null) {

                        val config = Config.currencies.findEntry { it.key.equals(name, true) }

                        if (config == null) {
                            sender.sendMessage("Can't found this currency, " +
                                    "try to create her with /souzaeconomy currency create")
                            return@executor
                        }

                        if (SouzaEconomy.API.currencies.find { it.name.equals(name, true) } != null) {
                            sender.sendMessage("This currency is already loaded, " +
                                    "try reloaded with /souzaeconomy currency reload")
                            return@executor
                        }

                        if (loadCurrency(config.key, config.value)) {
                            sender.sendMessage("Currency loaded, have fun :3")
                        } else {
                            sender.sendMessage("Is not possible to load the currency :(")
                        }

                    } else {
                        sender.sendMessage("/$label [name]")
                    }
                }
            }

            command("reload") {
                // [name]
                aliases = listOf("r")
                permission += ".$name"
                description = ""

                executor {
                    val name = args.getOrNull(0)

                    if (name != null) {
                        val currency = SouzaEconomy.API.currencies.find { it.name.equals(name, true) }
                        val configuration = Config.currencies.findEntry { it.key.equals(name, true) }

                        if (currency == null) {
                            sender.sendMessage("Currency not found")
                            return@executor
                        }

                        currency.onDisable()
                        SouzaEconomy.API.currencies.remove(currency)

                        if (config.reload().loadAndSetDefault(Config::class) > 0) {
                            config.save()
                        }

                        if (configuration == null) {
                            sender.sendMessage("Currency unload but is not possible to load again :C")
                            return@executor
                        }

                        if (loadCurrency(configuration.key, configuration.value)) {
                            sender.sendMessage("Currency reloaded, have a good time :3")
                        } else {
                            sender.sendMessage("Is not possible to reload the currency :(")
                        }

                    } else {
                        sender.sendMessage("/$label [name]")
                    }
                }
            }

            command("typelist") {
                aliases = listOf("tls")
                permission += ".$name"
                description = ""

                executor {
                    sender.sendMessage(+"&8&m------------------------------")
                    sender.sendMessage(+"&bSouzaEconomy-> &6Types")
                    for (type in SouzaEconomy.API.currenciesTypes) {
                        sender.sendMessage("&b${type.typeName}: &e${type.description}")
                    }
                    sender.sendMessage(+"&8&m------------------------------")
                }
            }

            command("list") {
                aliases = listOf("ls")
                permission += ".$name"
                description = ""

                executor {
                    sender.sendMessage(+"&8&m------------------------------")
                    sender.sendMessage(+"&bSouzaEconomy-> &6Currencies")
                    for (currency in SouzaEconomy.API.currencies) {
                        val type = SouzaEconomy.API.currenciesTypes.find { it.currencyClass == currency::class }
                        sender.sendMessage("&b${currency.name} &7${type?.typeName ?: ""}")
                    }
                    sender.sendMessage(+"&8&m------------------------------")
                }
            }

            executor {
                listSubCommands(subCommands, "Currency commands")
            }
        }

        command("report") {
            // TODO /se report [currency name]
            aliases = listOf("r")
            permission = ".$name"
            description = ""

            executor {
                sender.sendMessage("SZ FUTURE FEATURE SZ")
            }
        }

        executor {
            listSubCommands(subCommands, "Commands")
        }
    }
}

fun Executor<*>.listSubCommands(subCommands: List<KCommand>, description: String) {
    val commandsMessage = arrayListOf<Pair<Command, BaseComponent>>().apply {
        for (subCmd in subCommands) {
            add(subCmd to "&b/${label} &e${subCmd.name}"
                    .showText(" ${subCmd.description}".color(ChatColor.YELLOW))
                    .suggestCommand("/${label} ${subCmd.name}"))
        }
    }
    sender.sendMessage(+"&8&m-----------------------------")
    sender.sendMessage(+"&bSouzaEconomy-> &6$description")
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