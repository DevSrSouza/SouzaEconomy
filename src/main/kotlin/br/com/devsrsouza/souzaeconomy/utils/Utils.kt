package br.com.devsrsouza.souzaeconomy.utils

fun <K, V> Map<K, V>.findEntry(block: (Map.Entry<K, V>) -> Boolean): Map.Entry<K, V>? {
    for (entry in this) {
        if (block(entry)) return entry
    }
    return null
}

fun <K, V> Map<K, V>.findValue(block: (Map.Entry<K, V>) -> Boolean): V? {
    return findEntry(block)?.value
}

fun <K, V> Map<K, V>.findKey(block: (Map.Entry<K, V>) -> Boolean): K? {
    return findEntry(block)?.key
}