package br.com.devsrsouza.souzaeconomy.events

import br.com.devsrsouza.souzaeconomy.currency.ICurrency
import br.com.devsrsouza.souzaeconomy.currency.ICurrencyConfig
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PreLoadCurrencyEvent(var currency: ICurrency<out ICurrencyConfig>, var registerCommand: Boolean) : Event() {

    companion object { @JvmStatic val handlerList = HandlerList() }

    override fun getHandlers(): HandlerList = handlerList
}