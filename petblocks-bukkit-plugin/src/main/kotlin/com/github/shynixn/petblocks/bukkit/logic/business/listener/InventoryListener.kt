@file:Suppress("unused")

package com.github.shynixn.petblocks.bukkit.logic.business.listener

import com.github.shynixn.petblocks.api.business.service.GUIPetStorageService
import com.github.shynixn.petblocks.api.business.service.GUIService
import com.google.inject.Inject
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Handles clicking into the PetBlocks GUI.
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
class InventoryListener @Inject constructor(
    private val guiService: GUIService,
    private val guiPetStorageService: GUIPetStorageService
) : Listener {
    /**
     * Gets called and handles action to the gui inventory.
     */
    @EventHandler
    fun onPlayerClickInInventoryEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as Player

        if (!guiService.isGUIInventory(event.inventory)) {
            return
        }

        if (event.currentItem == null || event.currentItem!!.type == Material.AIR) {
            return
        }

        event.isCancelled = true
        player.updateInventory()

        guiService.clickInventoryItem(player, event.slot, event.currentItem)
    }

    /**
     * Saves the storage inventory on close.
     */
    @EventHandler
    fun onPlayerCloseInventoryEvent(event: InventoryCloseEvent) {
        if (!guiPetStorageService.isStorage(event.inventory)) {
            return
        }

        guiPetStorageService.saveStorage(event.player)
    }

    /**
     * Gets called and handles cleaning up remaining resources.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        guiService.cleanResources(event.player)
    }
}