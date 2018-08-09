package br.com.devsrsouza.souzaeconomy.events

import br.com.devsrsouza.souzaeconomy.SouzaEconomyAPI
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class AfterLoadDefaultTypesEvent(val api: SouzaEconomyAPI) : Event() {

    companion object {
        @JvmField val handlers = HandlerList()
    }

    override fun getHandlers(): HandlerList = AfterLoadDefaultTypesEvent.handlers
}