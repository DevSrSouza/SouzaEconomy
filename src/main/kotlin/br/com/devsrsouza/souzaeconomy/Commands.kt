package br.com.devsrsouza.souzaeconomy.command

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.*
import br.com.devsrsouza.kotlinbukkitapi.dsl.command.arguments.*
import br.com.devsrsouza.kotlinbukkitapi.dsl.config.YamlConfig
import br.com.devsrsouza.kotlinbukkitapi.dsl.config.loadAndSetDefault
import br.com.devsrsouza.kotlinbukkitapi.dsl.config.saveFrom
import br.com.devsrsouza.kotlinbukkitapi.extensions.text.*
import br.com.devsrsouza.souzaeconomy.*
import br.com.devsrsouza.souzaeconomy.utils.*
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.entity.Player
import java.io.File

internal fun SouzaEconomy.commands() {
    command("souzaeconomy", "se", plugin = this) {
        permission = "souzaeconomy.cmd"
        permissionMessage = CommandMessageConfig.no_permission
        description = "SouzaEconomy configuration command"

        command("currency", "c") {
            permission += ".$name"
            description = CommandsDescriptionsConfig.currency

            command("create", "c") {
                permission += ".$name"
                description = CommandsDescriptionsConfig.currency_create

                executor {
                    val usageFormat = "/$label [name] [type] [optional: enable command]".color(ChatColor.RED)

                    val name = currencyOrNull(0, usageFormat)
                            ?.run { exception(CommandMessageConfig.already_has_currency) }
                            ?: string(0, usageFormat)

                    val type = currencyType(1, usageFormat, CommandMessageConfig.type_not_found.asText())

                    val enableCommand = optional { boolean(2, usageFormat, CommandMessageConfig.enable_command_need_be_boolean.asText()) }

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
                            .let { YamlConfig(it) }
                    file.apply {
                        if (loadAndSetDefault(type.currencyConfigClass) > 0)
                            save()
                    }

                    sender.sendMessage(+"&eConfigs generated on &7plugins/SouzaEconomy/currencies/$name.yml&e.")
                    sender.sendMessage(+"&eAfter the configuration on file, load the currency with &7/souzaeconomy currency load&e.")
                }
            }

            command("load", "l") {
                // [name]
                permission += ".$name"
                description = CommandsDescriptionsConfig.currency_load

                executor {
                    val usageFormat = "/$label [name]".color(ChatColor.RED)
                    val name = string(0, usageFormat)

                    val config = Config.currencies.findEntry { it.key.equals(name, true) }
                            ?: exception(CommandMessageConfig.load_cant_find_currency)

                    currencyOrNull(0, usageFormat)?.run {
                        exception(CommandMessageConfig.currency_already_loaded)
                    }

                    if (loadCurrency(config.key, config.value)) {
                        sender.sendMessage(CommandMessageConfig.currency_loaded)
                    } else {
                        sender.sendMessage(CommandMessageConfig.problem_on_load_currency)
                    }
                }
            }

            command("reload", "r") {
                permission += ".$name"
                description = CommandsDescriptionsConfig.currency_reload

                executor {
                    val usageFormat = "/$label [name]".color(ChatColor.RED)

                    val currency = currency(0, usageFormat, CommandMessageConfig.reload_cant_find_currency_loaded.asText())

                    currency.onDisable()
                    SouzaEconomy.API.currencies.remove(currency)

                    if (config.reload().loadAndSetDefault(Config::class) > 0) {
                        config.save()
                    }

                    val configuration = Config.currencies.findEntry { it.key.equals(currency.name, true) }
                            ?: exception(CommandMessageConfig.remove_from_config_and_reload)

                    if (loadCurrency(configuration.key, configuration.value)) {
                        sender.sendMessage(CommandMessageConfig.currency_reloaded)
                    } else {
                        sender.sendMessage(CommandMessageConfig.problem_on_reload_currency)
                    }
                }
            }

            command("typelist", "tls") {
                permission += ".$name"
                description = CommandsDescriptionsConfig.currency_typelist

                executor {
                    sender.sendMessage(+"&8&m------------------------------")
                    sender.sendMessage(+"&bSouzaEconomy-> &6Types")
                    for (type in SouzaEconomy.API.currenciesTypes) {
                        sender.sendMessage(+"&b${type.typeName}: &e${type.description}")
                    }
                    sender.sendMessage(+"&8&m------------------------------")
                }
            }

            command("list", "ls") {
                permission += ".$name"
                description = CommandsDescriptionsConfig.currency_list

                executor {
                    sender.sendMessage(+"&8&m------------------------------")
                    sender.sendMessage(+"&bSouzaEconomy-> &6Currencies")
                    for (currency in SouzaEconomy.API.currencies) {
                        val type = SouzaEconomy.API.currenciesTypes.find { it.currencyClass == currency::class }
                        sender.sendMessage(+"&b${currency.name} &7${type?.typeName ?: ""}")
                    }
                    sender.sendMessage(+"&8&m------------------------------")
                }
            }

            executor {
                listSubCommands(subCommands, "Currency commands")
            }
        }

        command("report", "r") {
            permission = ".$name"
            description = CommandsDescriptionsConfig.report

            executor {
                sender.sendMessage("SZ WIP SZ")
            }
        }

        executor {
            listSubCommands(subCommands, "Commands")
        }
    }
}

private fun Executor<*>.listSubCommands(subCommands: List<KCommand>, description: String) {
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