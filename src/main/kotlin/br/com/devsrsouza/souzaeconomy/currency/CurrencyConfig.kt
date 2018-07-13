package br.com.devsrsouza.souzaeconomy.currency

import br.com.devsrsouza.kotlinbukkitapi.extensions.text.unaryPlus

open class CurrencyConfig {
    open var commands = CurrencyCommandsConfig()
    open var messages = CurrencyMessageConfig()
}
open class CurrencyMessageConfig {
    open var no_permission = +"&cYou don't have permission to use this command."
    open var just_players_can_run_this_command = +"&cThis command can just be runned in game."
}
open class CurrencyCommandsConfig {
    open var cooldownForExecute = 3
    open var add = true
    open var set = true
    open var remove = true
    open var reset = true
    open var pay = true
    open var top = true
}