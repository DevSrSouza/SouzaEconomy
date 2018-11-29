package br.com.devsrsouza.souzaeconomy.currency.sql

import br.com.devsrsouza.souzaeconomy.SouzaEconomy
import br.com.devsrsouza.souzaeconomy.utils.Databases
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.SQLException
import java.util.*

class SQLController(val tableName: String, val config: CurrencySQLConfig) {

    private val currencies = mutableListOf<SQLCurrency<SQLCurrencyConfig>>()
    val table = SQLCurrencyTable()
    lateinit var database: Database
        private set
    val dataSource: HikariDataSource by lazy {
        val type = Databases.values().find { it.name.equals(config.type, true) }
        if (type != null) {
            if (type.file) {
                type.getDataSource(File(SouzaEconomy.INSTANCE.dataFolder, "database/$tableName")
                        .apply {
                            if (type == Databases.SQLite && !exists()) {
                                parentFile.mkdirs()
                            }
                        }.absolutePath)
            } else {
                type.getDataSource(config.hostname, config.port, config.database, config.user, config.password).also {
                    if (type == Databases.MySQL || type == Databases.MariaDB) {
                        // TODO add mysql otimization parameters
                    }
                }
            }
        } else throw SQLException("SQL type not finded or supported.")
    }

    fun registerCurrency(currency: SQLCurrency<SQLCurrencyConfig>): SQLControllerRegistry {
        if (currencies.find { it.name.equals(currency.name, true) } == null) {
            currencies.add(currency)
            return SQLControllerRegistry(this, table.run { long(currency.name).default(0) })
        }
        throw IllegalArgumentException("has a currency with the same name in the sql controller")
    }

    fun init() {

        database = Database.connect(dataSource)

        transaction {
            createMissingTablesAndColumns(table)
        }
    }

    inner class SQLCurrencyTable : Table(tableName) {
        val id = uuid("uuid").primaryKey() // Column<UUID>
        val playerName = varchar("nickname", length = 26) // Column<String>
    }
}

class SQLControllerRegistry(private val controller: SQLController, val column: Column<Long>) {
    private val table = controller.table

    fun setBalance(player: OfflinePlayer, amount: Long): Long {
        transaction(controller.database) {
            if (getMoneyIfHasAccount(player) != null) {
                table.update({ table.id.eq(player.uniqueId) }) {
                    it[column] = amount
                    if(player.name != null)
                        it[playerName] = player.name
                }
            } else {
                table.insert {
                    it[id] = player.uniqueId
                    it[column] = amount
                    it[playerName] = player.name ?: "unknown name"
                }
            }
        }
        return amount
    }

    fun removeBalance(player: OfflinePlayer, amount: Long): Boolean {
        return transaction(controller.database) {
            val moneyGetted = getMoneyIfHasAccount(player)
            if (moneyGetted != null && moneyGetted >= amount) {
                table.update({ table.id.eq(player.uniqueId) }) {
                    it[column] = moneyGetted - amount
                    if(player.name != null)
                        it[playerName] = player.name
                }
                return@transaction true
            } else return@transaction false
        }
    }

    fun addBalance(player: OfflinePlayer, amount: Long): Long {
        val moneyGetted = getMoneyIfHasAccount(player)
        return transaction(controller.database) {
            var newMoney: Long = 0
            if (moneyGetted != null) {
                table.update({ table.id.eq(player.uniqueId) }) {
                    newMoney = moneyGetted + amount
                    it[column] = newMoney
                    if(player.name != null)
                        it[playerName] = player.name
                }
            } else {
                table.insert {
                    newMoney = amount
                    it[id] = player.uniqueId
                    it[column] = newMoney
                    it[playerName] = player.name ?: "unknown name"
                }
            }
            return@transaction newMoney
        }
    }

    fun getMoneyIfHasAccount(player: OfflinePlayer): Long? {
        return transaction(controller.database) {
            table.select { table.id.eq(player.uniqueId) }.firstOrNull()?.get(column)
        }
    }

    fun getTop(range: IntRange): Map<UUID, Long> {
        return transaction(controller.database) {
            table.selectAll().orderBy(column).limit(range.start, range.endInclusive)
                    .map { it[table.id] to it[column] }.toMap()
        }
    }
}
