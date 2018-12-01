package br.com.devsrsouza.souzaeconomy.events

import br.com.devsrsouza.souzaeconomy.currency.CurrencyConfig
import br.com.devsrsouza.souzaeconomy.currency.ICurrency
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PreLoadCurrencyEvent(var currency: ICurrency<CurrencyConfig>, var registerCommand: Boolean) : Event() {

    companion object {
        @JvmField val handlers = HandlerList()
    }

    override fun getHandlers(): HandlerList = PosLoadDefaultTypesEvent.handlers
}