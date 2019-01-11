
package br.com.devsrsouza.souzaeconomy.currency.wrapper

import br.com.devsrsouza.souzaeconomy.VaultConfig
import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.ICurrency
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class VaultCurrencyWrapper(private val base: ICurrency<out CurrencyConfig>) : ICurrency<CurrencyConfig> by base as ICurrency<CurrencyConfig> {

    val vault = VaultEconomy()

    inner class VaultEconomy : Economy {

        override fun getName() = base.name

        override fun currencyNameSingular() = VaultConfig.singular

        override fun currencyNamePlural() = VaultConfig.plural

        override fun isEnabled() = true

        override fun fractionalDigits() = 0

        override fun format(amount: Double) = amount.toString()

        override fun getBalance(player: String) = getBalance(Bukkit.getOfflinePlayer(player))

        override fun getBalance(player: OfflinePlayer) = base.getMoney(player).toDouble()

        override fun getBalance(player: String, world: String?) = getBalance(Bukkit.getOfflinePlayer(player), world)

        override fun getBalance(player: OfflinePlayer, world: String?) = getBalance(player)

        override fun has(player: String, amount: Double) = has(Bukkit.getOfflinePlayer(player), amount)

        override fun has(player: OfflinePlayer, amount: Double) = base.getMoney(player) >= amount

        override fun has(player: String, world: String?, amount: Double) = has(Bukkit.getOfflinePlayer(player), world, amount)

        override fun has(player: OfflinePlayer, world: String?, amount: Double) = has(player, amount)

        override fun depositPlayer(player: String, amount: Double) = depositPlayer(Bukkit.getOfflinePlayer(player), amount)

        override fun depositPlayer(player: OfflinePlayer, amount: Double) = base.addMoney(player, amount.toLong()).let {
            EconomyResponse(amount, it.toDouble(), EconomyResponse.ResponseType.SUCCESS, "")
        }

        override fun depositPlayer(player: String, world: String?, amount: Double) = depositPlayer(Bukkit.getOfflinePlayer(player), world, amount)

        override fun depositPlayer(player: OfflinePlayer, world: String?, amount: Double) = depositPlayer(player, amount)

        override fun hasAccount(player: String) = hasAccount(Bukkit.getOfflinePlayer(player))

        override fun hasAccount(player: OfflinePlayer) = base.hasAccount(player)

        override fun hasAccount(player: String, world: String?) = hasAccount(Bukkit.getOfflinePlayer(player), world)

        override fun hasAccount(player: OfflinePlayer, world: String?) = hasAccount(player)

        override fun withdrawPlayer(player: String, amount: Double) = withdrawPlayer(Bukkit.getOfflinePlayer(player), amount)

        override fun withdrawPlayer(player: OfflinePlayer, amount: Double) = if (base.removeMoney(player, amount.toLong())) {
            EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "")
        } else {
            EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.FAILURE, "")
        }

        override fun withdrawPlayer(player: String, world: String?, amount: Double) = withdrawPlayer(Bukkit.getOfflinePlayer(player), world, amount)

        override fun withdrawPlayer(player: OfflinePlayer, world: String?, amount: Double) = withdrawPlayer(player, amount)

        override fun createPlayerAccount(p0: String?) = true

        override fun createPlayerAccount(p0: OfflinePlayer?) = true

        override fun createPlayerAccount(p0: String?, p1: String?) = true

        override fun createPlayerAccount(p0: OfflinePlayer?, p1: String?) = true

        override fun getBanks() = emptyList<String>()

        override fun bankHas(account: String, amount: Double) = notImplemented()

        override fun bankBalance(account: String) = notImplemented()

        override fun isBankMember(p0: String?, p1: String?) = notImplemented()

        override fun isBankMember(p0: String?, p1: OfflinePlayer?) = notImplemented()

        override fun isBankOwner(p0: String?, p1: String?) = notImplemented()

        override fun isBankOwner(p0: String?, p1: OfflinePlayer?) = notImplemented()

        override fun hasBankSupport() = false

        override fun bankDeposit(account: String, amount: Double) = notImplemented()

        override fun bankWithdraw(account: String, amount: Double) = notImplemented()

        override fun deleteBank(account: String) = notImplemented()

        override fun createBank(p0: String?, p1: String?) = notImplemented()

        override fun createBank(p0: String?, p1: OfflinePlayer?) = notImplemented()

        private inline fun notImplemented()
                = EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "not implemented")

    }
}
