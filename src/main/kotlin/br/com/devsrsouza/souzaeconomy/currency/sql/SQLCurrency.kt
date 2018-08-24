package br.com.devsrsouza.souzaeconomy.currency.sql

import br.com.devsrsouza.souzaeconomy.utils.Databases
import br.com.devsrsouza.souzaeconomy.SouzaEconomy
import br.com.devsrsouza.souzaeconomy.currency.Currency
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.OfflinePlayer
import java.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.SQLException

open class SQLCurrency<C : SQLCurrencyConfig>(name: String, configuration: C = SQLCurrencyConfig() as C)
    : Currency<C>(name, configuration) {

    val database: Database
    val dataSource: HikariDataSource

    open val table = SQLCurrencyTable("se_$name")

    init {

        dataSource = getHikariDataSource()
        database = Database.connect(dataSource)

        transaction(database) {
            create(table)
        }
    }

    override fun getMoney(player: OfflinePlayer): Long {
        return getMoneyIfHasAccount(player) ?: 0
    }

    override fun setMoney(player: OfflinePlayer, amount: Long): Long {
        transaction(database) {
            if (hasAccount(player)) {
                table.update({ table.id.eq(player.uniqueId) }) {
                    it[money] = amount
                    it[playerName] = player.name
                }
            } else {
                table.insert {
                    it[id] = player.uniqueId
                    it[money] = amount
                    it[playerName] = player.name
                }
            }
        }
        return amount
    }

    override fun removeMoney(player: OfflinePlayer, amount: Long): Boolean {
        val moneyGetted = getMoneyIfHasAccount(player)
        if (moneyGetted != null && moneyGetted >= amount) {
            table.update({ table.id.eq(player.uniqueId) }) {
                it[money] = moneyGetted - amount
                it[playerName] = player.name
            }
            return true
        } else return false
    }

    override fun addMoney(player: OfflinePlayer, amount: Long): Long {
        val moneyGetted = getMoneyIfHasAccount(player)
        var newMoney: Long = 0
        if (moneyGetted != null) {
            table.update({ table.id.eq(player.uniqueId) }) {
                newMoney = moneyGetted + amount
                it[money] = newMoney
                it[playerName] = player.name
            }
        } else {
            table.insert {
                newMoney = amount
                it[id] = player.uniqueId
                it[money] = newMoney
                it[playerName] = player.name
            }
        }
        return newMoney
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return getMoneyIfHasAccount(player) != null
    }

    override fun getTop(range: IntRange): Map<UUID, Long> {
        return transaction(database) {
            table.selectAll().orderBy(table.money).limit(range.start, range.endInclusive)
                    .map { it[table.id] to it[table.money] }.toMap()
        }
    }

    open fun getMoneyIfHasAccount(player: OfflinePlayer): Long? {
        return transaction(database) {
            table.select { table.id.eq(player.uniqueId) }.firstOrNull()?.get(table.money)
        }
    }

    open fun getHikariDataSource(): HikariDataSource {
        val sql = config.sql
        val type = Databases.values().find { it.name.equals(sql.type, true) }
        if (type != null) {
            if (type.file) {
                return type.getDataSource(File(SouzaEconomy.INSTANCE.dataFolder, "database/$name")
                        .apply {
                            if (type == Databases.SQLite && !exists()) {
                                parentFile.mkdirs()
                            }
                        }.absolutePath)
            } else {
                return type.getDataSource(sql.hostname, sql.port, sql.database, sql.user, sql.password).also {
                    if (type == Databases.MySQL || type == Databases.MariaDB) {
                        // TODO add mysql otimization parameters
                    }
                }
            }
        } else throw SQLException("SQL type not finded or supported.")
    }

    override fun onDisable() {
        dataSource.close()
    }
}

open class SQLCurrencyTable(name: String) : Table(name) {
    val id = uuid("uuid").primaryKey() // Column<UUID>
    val playerName = varchar("nickname", length = 26) // Column<String>
    val money = long("money") // Column<Long>
}