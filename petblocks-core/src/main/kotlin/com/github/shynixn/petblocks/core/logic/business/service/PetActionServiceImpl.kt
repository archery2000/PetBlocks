package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.core.logic.business.extension.relativeFront
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.SoundEntity
import com.google.inject.Inject

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
class PetActionServiceImpl @Inject constructor(
    private val petService: PetService,
    private val persistencePetMetaService: PersistencePetMetaService,
    private val configurationService: ConfigurationService,
    private val soundService: SoundService,
    private val proxyService: ProxyService
) : PetActionService {

    private val maxSkinLength = 200
    private val explosionSound = SoundEntity("EXPLODE", 1.0, 2.0)

    /**
     * Launches the pet of the player.
     */
    override fun <P> launchPet(player: P) {
        val direction = proxyService.getDirectionVector(player)
        direction.y = 0.5
        direction.multiply(3.0)

        if (petService.hasPet(player)) {
            val pet = petService.getOrSpawnPetFromPlayer(player).get()
            pet.setVelocity(direction)
            soundService.playSound(pet.getLocation<Any>(), explosionSound, player)
        }
    }

    /**
     * Sets the pet of the given [player] to the given skin.
     */
    override fun <P> changePetSkin(player: P, skin: String) {
        val prefix = configurationService.findValue<String>("messages.prefix")

        if (skin.length > maxSkinLength) {
            val namingSuccessMessage = configurationService.findValue<String>("messages.customhead-error")
            proxyService.sendMessage(player, prefix + namingSuccessMessage)

            return
        }

        val petMeta = persistencePetMetaService.getPetMetaFromPlayer(player)
        petMeta.skin.typeName = "397"
        petMeta.skin.dataValue = 3
        petMeta.skin.owner = skin

        persistencePetMetaService.save(petMeta)

        val namingSuccessMessage = configurationService.findValue<String>("messages.customhead-success")
        proxyService.sendMessage(player, prefix + namingSuccessMessage)
    }

    /**
     * Calls the pet to the given player. If the pet is not enabled, it will be enabled after calling.
     */
    override fun <P> callPet(player: P) {
        val pet = petService.getOrSpawnPetFromPlayer(player)

        val message = if (pet.isPresent) {
            pet.get().teleport<Any>(proxyService.toPosition(proxyService.getPlayerLocation<Any, P>(player)).relativeFront(3.0))

            configurationService.findValue<String>("messages.prefix") + configurationService.findValue<String>("messages.called-success")
        } else {
            configurationService.findValue<String>("messages.prefix") + configurationService.findValue<String>("messages.called-error")
        }

        proxyService.sendMessage(player, message)
    }

    /**
     * Disables the pet of the given [player].
     */
    override fun <P> disablePet(player: P) {
        if (!petService.hasPet(player)) {
            return
        }

        val pet = petService.getOrSpawnPetFromPlayer(player).get()
        pet.remove()

        val message = configurationService.findValue<String>("messages.prefix") + configurationService.findValue<String>("messages.despawn")
        proxyService.sendMessage(player, message)
    }

    /**
     * Toggles the pet of the given [player]. If the pet is disabled it will be enabled and when enabled it will be
     * disabled.
     */
    override fun <P> togglePet(player: P) {
        if (petService.hasPet(player)) {
            this.disablePet(player)
        } else {
            this.callPet(player)
        }
    }

    /**
     * Renames the pet of the given [player] to the given [name].
     */
    override fun <P> renamePet(player: P, name: String) {
        val prefix = configurationService.findValue<String>("messages.prefix")
        val maxPetNameLength = configurationService.findValue<Int>("global-configuration.max-petname-length")

        if (name.length > maxPetNameLength) {
            val namingErrorMessage = configurationService.findValue<String>("messages.rename-error")
            proxyService.sendMessage(player, prefix + namingErrorMessage)
            return
        }

        val petNameBlackList = configurationService.findValue<List<String>>("global-configuration.petname-blacklist").map { s -> s.toUpperCase() }
        val upperCaseName = name.toUpperCase()

        for (blackName in petNameBlackList) {
            if (upperCaseName.contains(blackName)) {
                val namingErrorMessage = configurationService.findValue<String>("messages.rename-error")
                proxyService.sendMessage(player, prefix + namingErrorMessage)
                return
            }
        }

        val petMeta = persistencePetMetaService.getPetMetaFromPlayer(player)
        val namingSuccessMessage = configurationService.findValue<String>("messages.rename-success")

        petMeta.displayName = name.translateChatColors()
        persistencePetMetaService.save(petMeta)

        proxyService.sendMessage(player, prefix + namingSuccessMessage)
    }
}