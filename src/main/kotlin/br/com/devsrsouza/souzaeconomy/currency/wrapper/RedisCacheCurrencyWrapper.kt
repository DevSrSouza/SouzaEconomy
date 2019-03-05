package br.com.devsrsouza.souzaeconomy.currency.wrapper

import br.com.devsrsouza.souzaeconomy.currency.ICurrency
import br.com.devsrsouza.souzaeconomy.currency.ICurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.nosql.redis.CurrencyRedisConfig
import br.com.devsrsouza.souzaeconomy.currency.nosql.redis.RedisCurrency
import br.com.devsrsouza.souzaeconomy.currency.nosql.redis.RedisCurrencyConfig
import org.bukkit.OfflinePlayer

class RedisCacheCurrencyWrapper(
        val cache: RedisCurrency<RedisCurrencyConfig>,
        val database: ICurrency<ICurrencyConfig>
) : ICurrency<ICurrencyConfig> by database {
    override val config: ICurrencyConfig = RedisCacheCurrencyConfig(database.config, cache.config.redis)

    override fun getMoney(player: OfflinePlayer): Long {
        val cached = cache.getMoneyIfHasAccount(player)
        if(cached == null) {
            return database.getMoney(player).also {
                cache.setMoney(player, it)
            }
        } else return cached
    }

    override fun setMoney(player: OfflinePlayer, amount: Long): Long {
        return cache.setMoney(player, amount)
    }

    override fun removeMoney(player: OfflinePlayer, amount: Long): Boolean {
        val money = getMoney(player)
        return cache.removeMoney(player, money)
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return cache.hasAccount(player) || database.hasAccount(player)
    }

    override fun onDisable() {
        cache.onDisable()
        database.onDisable()
    }
}

class RedisCacheCurrencyConfig<T : ICurrencyConfig>(
        val base: T,
        redisConfig: CurrencyRedisConfig
) : ICurrencyConfig by base {
    var redis = redisConfig
}