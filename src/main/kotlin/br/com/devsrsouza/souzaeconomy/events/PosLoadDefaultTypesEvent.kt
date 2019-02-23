package br.com.devsrsouza.souzaeconomy.events

import br.com.devsrsouza.souzaeconomy.SouzaEconomyAPI
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PosLoadDefaultTypesEvent(val api: SouzaEconomyAPI) : Event() {

    companion object { @JvmStatic val handlerList = HandlerList() }

    override fun getHandlers(): HandlerList = handlerList
}