package br.com.devsrsouza.souzaeconomy.currency.sql

import br.com.devsrsouza.kotlinbukkitapi.utils.ExpirationList
import br.com.devsrsouza.souzaeconomy.Databases
import br.com.devsrsouza.souzaeconomy.SouzaEconomy
import br.com.devsrsouza.souzaeconomy.currency.Currency
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.SQLException

@Suppress("IMPLICIT_CAST_TO_ANY")
open class SQLCurrency(name: String, configuration: SQLCurrencyConfig = SQLCurrencyConfig()) : Currency {

    val database: Database

    open val table = SQLCurrencyTable()

    init {

        database = Database.connect(getHikariDataSource())

        transaction(database) {
            create(table)
        }
    }

    override val name: String = name
    override val cooldown by lazy { ExpirationList<Player>(SouzaEconomy.INSTANCE) }
    override val config: SQLCurrencyConfig = configuration
    override val type: String = "SQL(${config.sql.type})"

    override fun getMoney(player: OfflinePlayer): Long {
        return getMoneyIfHasAccount(player) ?: 0
    }

    override fun setMoney(player: OfflinePlayer, amount: Long) : Long{
        transaction(database) {
            if(hasAccount(player)) {
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
        if(moneyGetted != null && moneyGetted >= amount) {
            table.update({ table.id.eq(player.uniqueId) }) {
                it[money] = moneyGetted - amount
                it[playerName] = player.name
            }
            return true
        } else return false
    }

    override fun addMoney(player: OfflinePlayer, amount: Long) : Long {
        val moneyGetted = getMoneyIfHasAccount(player)
        var newMoney: Long = 0
        if(moneyGetted != null) {
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

    open fun getHikariDataSource() : HikariDataSource {
        val sql = config.sql
        val type = Databases.values().find { it.name.equals(sql.type, true) }
        if (type != null) {
            if(type.file) {
                return type.getDataSource(File(SouzaEconomy.INSTANCE.dataFolder, "database/$name")
                        .apply { if(type == Databases.SQLite && !exists()) { mkdirs();createNewFile() } }.absolutePath)
            } else {
                return type.getDataSource(sql.hostname, sql.port, sql.database, sql.user, sql.password).also {
                    if(type == Databases.MySQL || type == Databases.MariaDB) {
                        // TODO add mysql otimization parameters
                    }
                }
            }
        } else throw SQLException("SQL type not finded or supported.")
    }
}

open class SQLCurrencyTable : Table() {
    val id = uuid("uuid").primaryKey() // Column<String>
    val playerName = varchar("nickname", length = 26) // Column<String>
    val money = long("money") // Column<Long>
}