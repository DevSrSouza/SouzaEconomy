package br.com.devsrsouza.souzaeconomy

import java.util.*

fun main(args: Array<String>) {
    val name = "srsouza"
    UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray(Charsets.UTF_8)).also { println(it) }
}