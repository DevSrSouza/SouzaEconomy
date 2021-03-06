package br.com.devsrsouza.souzaeconomy

import br.com.devsrsouza.kotlinbukkitapi.dsl.config.YamlConfig
import br.com.devsrsouza.kotlinbukkitapi.dsl.config.loadAndSetDefault
import br.com.devsrsouza.kotlinbukkitapi.dsl.config.loadTransformerChangeColor
import br.com.devsrsouza.kotlinbukkitapi.dsl.config.saveTransformerChangeColor
import br.com.devsrsouza.kotlinbukkitapi.dsl.event.event
import br.com.devsrsouza.kotlinbukkitapi.dsl.event.events
import br.com.devsrsouza.kotlinbukkitapi.dsl.event.registerEvents
import br.com.devsrsouza.kotlinbukkitapi.extensions.plugin.info
import br.com.devsrsouza.kotlinbukkitapi.plugins.placeholderapi.hasPlaceholderAPI
import br.com.devsrsouza.kotlinbukkitapi.plugins.vault.hasVault
import br.com.devsrsouza.souzaeconomy.command.commands
import br.com.devsrsouza.souzaeconomy.currency.ICurrency
import br.com.devsrsouza.souzaeconomy.currency.ICurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.nosql.redis.RedisCurrency
import br.com.devsrsouza.souzaeconomy.currency.nosql.redis.RedisCurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.sql.SQLCurrency
import br.com.devsrsouza.souzaeconomy.currency.sql.SQLCurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.sql.cached.CachedSQLCurrency
import br.com.devsrsouza.souzaeconomy.currency.sql.cached.CachedSQLCurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.wrapper.VaultCurrencyWrapper
import br.com.devsrsouza.souzaeconomy.events.PosLoadDefaultTypesEvent
import br.com.devsrsouza.souzaeconomy.events.PreLoadCurrencyEvent
import br.com.devsrsouza.souzaeconomy.hooks.PlaceholderAPI
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SouzaEconomy : JavaPlugin() {

    companion object {
        @JvmStatic
        internal lateinit var INSTANCE: SouzaEconomy
            private set
        @JvmStatic
        lateinit var API: SouzaEconomyAPI
            private set
    }

    lateinit var config: YamlConfig

    override fun onLoad() {
        INSTANCE = this
        API = SouzaEconomyAPI()
    }

    override fun onEnable() {
        config = File(dataFolder.apply { mkdirs() }, "config.yml")
                .apply { if (!exists()) createNewFile() }
                .let { YamlConfig(it) }
                .apply { if (loadAndSetDefault(Config::class,
                                saveTransformer = { saveTransformerChangeColor(it) },
                                loadTransformer = { loadTransformerChangeColor(it) }) > 0) save() }

        /**
         * loading default types
         */
        API.registerCurrencyType<SQLCurrency<SQLCurrencyConfig>, SQLCurrencyConfig>("SQL",
                "Currency based on SQL database") { name, config ->
            SQLCurrency(name, config)
        }

        API.registerCurrencyType<CachedSQLCurrency<CachedSQLCurrencyConfig>, CachedSQLCurrencyConfig>("CachedSQL",
                "Currency based on SQL database with local cache") { name, config ->
            CachedSQLCurrency(name, config)
        }

        API.registerCurrencyType<RedisCurrency<RedisCurrencyConfig>, RedisCurrencyConfig>("Redis",
                "Currency based on Redis") { name, config ->
            RedisCurrency(name, config)
        }

        server.pluginManager.callEvent(PosLoadDefaultTypesEvent(API))

        if(VaultConfig.enable && hasVault) {
            events {
                event<PreLoadCurrencyEvent> {
                    if(currency.name.equals(VaultConfig.currency, true)) {
                        currency = VaultCurrencyWrapper(currency).also {
                            Bukkit.getServer().servicesManager.register(Economy::class.java, it.vault, this@SouzaEconomy, ServicePriority.Highest)
                        }
                    }
                }
            }.registerEvents(this)
        }

        loadCurrencies()

        val main = API.getCurrency(Config.main_currency) ?: API.currencies.firstOrNull()
        if(main == null) {
            logger.severe("Can't load the plugin without a currency.")
            pluginLoader.disablePlugin(this)
            return
        } else {
            API.mainCurrency = main
            logger.info("Main currency load: ${main.name}")
        }

        commands()

        // PlaceholderAPI hook
        if (hasPlaceholderAPI) PlaceholderAPI(this).hook()
    }

    private fun loadCurrencies() {
        for ((name, config) in Config.currencies) {
            info("Loading the currency $name")
            loadCurrency(name, config)
        }
    }

    internal fun loadCurrency(name: String, config: CurrencyByConfig): Boolean {
        val type: CurrencyType<ICurrency<ICurrencyConfig>, ICurrencyConfig>? = API.currenciesTypes
                .find { it.typeName.equals(config.type, true) }
        if (type != null) {
            val file = File(dataFolder, "currencies/$name.yml")
                    .apply {
                        parentFile.mkdirs()
                        if (!exists()) createNewFile()
                    }
                    .let { YamlConfig(it) }
            val configurationCurrency = type.currencyConfigClass.constructors.first().call()
            configurationCurrency.displayname = "${ChatColor.GOLD}${name.capitalize()}"
            file.apply {
                if (loadAndSetDefault(type.currencyConfigClass, configurationCurrency) > 0)
                    save()
            }
            try {
                val currency: ICurrency<ICurrencyConfig> = type.factory(name, configurationCurrency)
                API.registerCurrency(currency, config.enable_command)
                logger.info("Currency ${currency.name} loaded.")
                return true
            } catch (e: Throwable) {
                logger.severe("Can't load currency $name.")
                e.printStackTrace()
                return false
            }
        } else return false
    }


    override fun onDisable() {
        for (currency in API.currencies) {
            currency.onDisable()
        }
    }
}

