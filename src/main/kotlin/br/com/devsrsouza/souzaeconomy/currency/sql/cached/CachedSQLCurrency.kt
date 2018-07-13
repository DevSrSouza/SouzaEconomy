package br.com.devsrsouza.souzaeconomy.currency.sql.cached

import br.com.devsrsouza.kotlinbukkitapi.dsl.scheduler.task
import br.com.devsrsouza.kotlinbukkitapi.dsl.scheduler.taskAsync
import br.com.devsrsouza.souzaeconomy.SouzaEconomy
import br.com.devsrsouza.souzaeconomy.currency.sql.SQLCurrency
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

open class CachedSQLCurrency(name: String, configuration: CachedSQLCurrencyConfig = CachedSQLCurrencyConfig()) : SQLCurrency(name) {

    inner class PlayerCache(val player: Player,
                            internal var backupMoney: Long,
                            internal var changedMoney: Long)

    open val cache = mutableListOf<PlayerCache>()
    override val config: CachedSQLCurrencyConfig = configuration
    override val type: String = "CachedSQL(${config.sql.type})"

    init {

        // TODO FAZER ASYNC
        task(config.cache.update_delay, plugin = SouzaEconomy.INSTANCE) {
            cache.removeAll {
                if(it.backupMoney != it.changedMoney) {
                    taskAsync { super.setMoney(it.player, it.changedMoney) }
                    it.backupMoney = it.changedMoney
                }
                !it.player.isOnline
            }
        }
    }

    override fun getMoney(player: OfflinePlayer): Long {
        val online = player.player
        if(online != null) {
            return cache.find { it.player.name.equals(online.name) }?.changedMoney ?: kotlin.run {
                super.getMoney(player).also {
                    cache.add(PlayerCache(online, it, it))
                }
            }
        }
        return super.getMoney(player)
    }

    override fun setMoney(player: OfflinePlayer, amount: Long) : Long {
        val online = player.player
        if(online != null) {
            getMoney(player)
            return (cache.find { it.player.name.equals(online.name) } ?: kotlin.run {
                super.getMoney(player).let {
                    PlayerCache(online, it, it).also { cache.add(it) }
                }
            }).apply { changedMoney = amount }.changedMoney
        }
        return super.setMoney(player, amount)
    }

    override fun removeMoney(player: OfflinePlayer, amount: Long): Boolean {
        val online = player.player
        if(online != null) {
            getMoney(player)
            val cache = cache.find { it.player.name.equals(online.name) } ?: kotlin.run {
                super.getMoney(player).let {
                    PlayerCache(online, it, it).also { cache.add(it) }
                }
            }
            if(cache.changedMoney - amount >= 0) {
                cache.changedMoney -= amount
                return true
            } else return false
        }
        return super.removeMoney(player, amount)
    }

    override fun addMoney(player: OfflinePlayer, amount: Long) : Long {
        val online = player.player
        if(online != null) {
            getMoney(player)
            return (cache.find { it.player.name.equals(online.name) } ?: kotlin.run {
                super.getMoney(player).let {
                    PlayerCache(online, it, it).also { cache.add(it) }
                }
            }).apply { changedMoney += amount }.changedMoney
        }
        return super.addMoney(player, amount)
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return super.hasAccount(player)
    }

    override fun getMoneyIfHasAccount(player: OfflinePlayer): Long? {
        return super.getMoneyIfHasAccount(player)
    }
}

