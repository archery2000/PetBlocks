package com.github.shynixn.petblocks.bukkit.logic.business.listener

import com.github.shynixn.petblocks.api.business.service.DependencyHeadDatabaseService
import com.google.inject.Inject
import me.arcaniax.hdb.api.PlayerClickHeadEvent
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class DependencyHeadDatabaseListener @Inject constructor(private val headDatabaseService: DependencyHeadDatabaseService) : Listener {
    /**
     * Gets called from HeadDatabase and handles action to the inventory.
     */
    @EventHandler
    fun playerClickOnHeadEvent(event: PlayerClickHeadEvent) {
        val player = event.player

        if (event.head == null || event.head!!.type == Material.AIR) {
            return
        }

        val cancelEvent = headDatabaseService.clickInventoryItem(player, event.head)

        if (!event.isCancelled && cancelEvent) {
            // Other plugins can modify this so check before manipulating.
            event.isCancelled = cancelEvent
        }
    }

    /**
     * Gets called when the player quits.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        headDatabaseService.clearResources(event.player)
    }
}