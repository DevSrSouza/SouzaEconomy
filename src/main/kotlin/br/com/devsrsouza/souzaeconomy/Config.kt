package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.kotlinbukkitapi.dsl.config.ChangeColor

object Config {

    var main_currency = "money"

    var vault = VaultConfig

    var currencies: MutableMap<String, CurrencyByConfig> = mutableMapOf(
            "money" to CurrencyByConfig().apply { type = "CachedSQL" },
            "cash" to CurrencyByConfig()
    )

    var messages = MessagesConfig
    var commands_descriptions = CommandsDescriptionsConfig
}

object VaultConfig {
    var enable = true
    var currency = "money"
    var singular = "money"
    var plural = "money"
}

object MessagesConfig {
    var commands = CommandMessageConfig
}

object CommandMessageConfig {
    @ChangeColor var no_permission = "&cYou don't have permission to use this."
    @ChangeColor var already_has_currency = "It already has a registered currency with this name."
    @ChangeColor var type_not_found = "&cType not found, use &7/souzaeconomy currency typelist &cto list all types."
    @ChangeColor var enable_command_need_be_boolean = "&cArgument enable command need be &4TRUE &cor &4FALSE&c."
    @ChangeColor var load_cant_find_currency = "&cCan't found this currency," +
            "try to create her with&7/souzaeconomy currency create&c."
    @ChangeColor var currency_already_loaded = "&cThis currency is already loaded, " +
            "try reloaded with &7/souzaeconomy currency reload&c."
    @ChangeColor var currency_loaded = "&eCurrency loaded, have fun :3"
    @ChangeColor var problem_on_load_currency = "&cIs not possible to load the currency :("
    @ChangeColor var reload_cant_find_currency_loaded = "&cCurrency not found."
    @ChangeColor var remove_from_config_and_reload = "&cCurrency unload and can't found her on configuration again :("
    @ChangeColor var currency_reloaded = "&eCurrency reloaded, have a good time :3"
    @ChangeColor var problem_on_reload_currency = "&cIs not possible to load the currency again :("
}

object CommandsDescriptionsConfig {
    @ChangeColor var currency = "Currency configuration subcommands (reload/create...)"
    @ChangeColor var currency_create = "Create a currency"
    @ChangeColor var currency_load = "Load a currency created"
    @ChangeColor var currency_reload = "Reload a currency configuration (disabling and enabling again)"
    @ChangeColor var currency_typelist = "List all types of currency registreds"
    @ChangeColor var currency_list = "List all currencies loadeds"
    @ChangeColor var report = "See a currency report (WIP)"
}

class CurrencyByConfig {
    var type = "SQL" // or CachedSQL
    var enable_command = true
}