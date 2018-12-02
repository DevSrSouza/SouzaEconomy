package br.com.devsrsouza.souzaeconomy.hooks

import br.com.devsrsouza.souzaeconomy.SouzaEconomy
import me.clip.placeholderapi.external.EZPlaceholderHook
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PlaceholderAPI(plugin: SouzaEconomy)
    : EZPlaceholderHook(plugin, "souzaeconomy") {

    override fun onPlaceholderRequest(player: Player?, identifier: String): String {
        val api = SouzaEconomy.API

        /*
        * [currency]_top_[pos]_player
        * [currency]_top_[pos]_money
        *
        * [currency]_money
        * [currency]_top
        **/

        val args = identifier.split("_")
        if(args.size > 1) {
            val currency = args.getOrNull(0)?.let { api.getCurrency(it) } ?: return ""
            val arg2 = args[1].toLowerCase()

            return when(arg2) {
                "top" -> {
                    val rank = args.getOrNull(2)?.toIntOrNull() ?: return ""
                    val res = args.getOrNull(3)?.toLowerCase() ?: return ""
                    val top = fun() = currency.getTop(rank..rank)
                    when(res) {
                        "player" -> top().keys.firstOrNull()?.let { Bukkit.getOfflinePlayer(it).name ?: "unknown" } ?: ""
                        "money" -> top().values.firstOrNull()?.toString() ?: ""
                        else -> ""
                    }
                }
                "money" -> player?.let { currency.getMoney(it).toString() } ?: ""
                else -> ""
            }

        } else return ""
    }
}