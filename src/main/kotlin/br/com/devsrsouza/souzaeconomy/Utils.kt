package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.Executor
import br.com.devsrsouza.kotlinbukkitapi.dsl.command.KCommand
import br.com.devsrsouza.kotlinbukkitapi.extensions.text.*
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.Command
import org.bukkit.entity.Player

fun String.toBooleanOrNull(): Boolean? {
    return when {
        equals("true", true) -> true
        equals("false", false) -> false
        else -> null
    }
}

fun <K, V> Map<K,V>.findEntry(block: (Map.Entry<K, V>) -> Boolean): Map.Entry<K, V>? {
    for(entry in this) {
        if(block(entry)) return entry
    }
    return null
}


fun <K, V> Map<K,V>.findValue(block: (Map.Entry<K, V>) -> Boolean): V? {
    return findEntry(block)?.value
}

fun <K, V> Map<K,V>.findKey(block: (Map.Entry<K, V>) -> Boolean): K? {
    return findEntry(block)?.key
}