package br.com.devsrsouza.souzaeconomy.events

import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.ICurrency
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PreLoadCurrencyEvent(var currency: ICurrency<out CurrencyConfig>, var registerCommand: Boolean) : Event() {

    companion object { @JvmStatic val handlerList = HandlerList() }

    override fun getHandlers(): HandlerList = handlerList
}