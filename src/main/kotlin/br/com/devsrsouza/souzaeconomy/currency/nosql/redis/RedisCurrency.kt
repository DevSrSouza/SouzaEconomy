package br.com.devsrsouza.souzaeconomy.currency.nosql.redis

import br.com.devsrsouza.kotlinbukkitapi.extensions.bukkit.Log
import br.com.devsrsouza.souzaeconomy.currency.Currency
import org.bukkit.OfflinePlayer
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisDataException
import java.util.*

class RedisCurrency<C : RedisCurrencyConfig>(name: String, config: C) : Currency<C>(name, config) {

    val jedis: Jedis = Jedis(config.redis.hostname, config.redis.port, 5000)

    init {
        try {
            jedis.connect()
            jedis.auth(config.redis.password)
        } catch (e: JedisDataException) {
            Log.severe("Can't connect to Redis in currency $name")
            e.printStackTrace()
        }
    }

    private val OfflinePlayer.redis get() = "${config.redis.database}:$name:${player.uniqueId.toString().toLowerCase()}"

    override fun getMoney(player: OfflinePlayer): Long {
        val money = jedis.get(player.redis)?.toLongOrNull()
        return money ?: 0
    }

    override fun setMoney(player: OfflinePlayer, amount: Long): Long {
        jedis.set(player.redis, amount.toString())
        return amount
    }

    override fun removeMoney(player: OfflinePlayer, amount: Long): Boolean {
        val money = getMoney(player)
        if(money >= amount) {
            setMoney(player, money - amount)
            return true
        } else return false
    }

    override fun addMoney(player: OfflinePlayer, amount: Long): Long {
        return setMoney(player, getMoney(player) + amount)
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return jedis.get(player.redis) != null
    }

    override fun getTop(range: IntRange): Map<UUID, Long> {
        TODO("not implemented")
    }

    override fun onDisable() {
        if(jedis.isConnected) jedis.disconnect()
    }

}