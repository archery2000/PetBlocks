package com.github.shynixn.petblocks.api.business.service

import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.persistence.entity.Position

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
interface ProxyService {
    /**
     * Gets the name of a player.
     */
    fun <P> getPlayerName(player: P): String

    /**
     * Gets the player from the given UUID.
     */
    fun <P> getPlayerFromUUID(uuid: String): P

    /**
     * Drops the given item at the given position.
     */
    fun <L, I> dropInventoryItem(location : L, item: I)

    /**
     * Gets the inventory item at the given index.
     */
    fun <I, IT> getInventoryItem(inventory: I, index: Int): IT?

    /**
     * Gets the location of the player.
     */
    fun <L, P> getPlayerLocation(player: P): L

    /**
     * Converts the given [location] to a [Position].
     */
    fun <L> toPosition(location: L): Position

    /**
     * Gets the looking direction of the player.
     */
    fun <P> getDirectionVector(player: P): Position

    /**
     * Gets the item in the player hand.
     */
    fun <P, I> getPlayerItemInHand(player: P, offhand: Boolean = false): I?

    /**
     * Sets the item in the player hand.
     */
    fun <P, I> setPlayerItemInHand(player: P, itemStack: I, offhand: Boolean = false)

    /**
     * Gets if the given player has got the given permission.
     */
    fun <P> hasPermission(player: P, permission: Permission): Boolean

    /**
     * Gets if the given player has got the given permission.
     */
    fun <P> hasPermission(player: P, permission: String): Boolean

    /**
     * Closes the inventory of the given player.
     */
    fun <P> closeInventory(player: P)

    /**
     * Gets if the given inventory belongs to a player. Returns null if not.
     */
    fun <P, I> getPlayerFromInventory(inventory: I): P?

    /**
     * Gets the lower inventory of an inventory.
     */
    fun <I> getLowerInventory(inventory: I): I

    /**
     * Clears the given inventory.
     */
    fun <I> clearInventory(inventory: I)

    /**
     * Opens a new inventory for the given player.
     */
    fun <P, I> openInventory(player: P, title: String, size: Int): I

    /**
     * Gets the player uuid.
     */
    fun <P> getPlayerUUID(player: P): String

    /**
     * Gets if the given instance can be converted to a player.
     */
    fun <P> isPlayer(instance: P): Boolean

    /**
     * Updates the inventory.
     */
    fun <I, IT> setInventoryItem(inventory: I, index: Int, item: IT)

    /**
     * Updates the given player inventory.
     */
    fun <P> updateInventory(player: P)

    /**
     * Sends a message to the [sender].
     */
    fun <S> sendMessage(sender: S, message: String)
}