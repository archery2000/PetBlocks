@file:Suppress("unused")

package com.github.shynixn.petblocks.bukkit.logic.business.listener

import com.github.shynixn.petblocks.api.bukkit.event.PetBlocksLoginEvent
import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIFlyRiding
import com.github.shynixn.petblocks.api.persistence.entity.AIGroundRiding
import com.github.shynixn.petblocks.api.persistence.entity.AIWearing
import com.github.shynixn.petblocks.bukkit.logic.business.extension.findClazz
import com.github.shynixn.petblocks.bukkit.logic.business.extension.teleportUnsafe
import com.github.shynixn.petblocks.core.logic.business.extension.cast
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.github.shynixn.petblocks.core.logic.business.extension.thenAcceptSafely
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.PlayerLeashEntityEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.spigotmc.event.entity.EntityDismountEvent
import java.util.*
import kotlin.collections.HashSet

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
class PetListener @Inject constructor(
    private val petService: PetService,
    private val persistencePetMetaService: PersistencePetMetaService,
    private val concurrencyService: ConcurrencyService,
    private val entityService: EntityService,
    private val loadService: GUIItemLoadService,
    private val debugService: PetDebugService,
    private val itemTypeService: ItemTypeService,
    private val configurationService: ConfigurationService,
    private val version: Version
) : Listener {
    private val joinCooldown = 20 * 6L
    private val alreadyLoading = HashSet<UUID>()

    /**
     * Gets called when a player joins the server. Join the pet if it was already enabled last time.
     */
    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if ((event.joinMessage.cast<String?>()) != null && event.joinMessage == "PetBlocksRunTime") {
            this.loadPetBlocks(event.player)
        } else {
            val uuid = event.player.uniqueId

            if (alreadyLoading.contains(uuid)) {
                return
            }

            alreadyLoading.add(uuid)

            sync(concurrencyService, joinCooldown) {
                this.loadPetBlocks(event.player)
            }
        }
    }

    /**
     * Gets called when the pet meta data would be prepared for a player.
     */
    @EventHandler
    fun onPetBlocksLoginEvent(event: PetBlocksLoginEvent) {
        val overwrite = configurationService.findValue<Boolean>("global-configuration.overwrite-previous-pet")

        if (overwrite) {
            val newPetMeta = loadService.generateDefaultPetMeta(event.player.uniqueId.toString(), event.player.name)
            persistencePetMetaService.save(newPetMeta)
            persistencePetMetaService.refreshPetMetaFromRepository(event.player).thenAcceptSafely {
                performFirstSpawn(event.player)
            }
        }

        if (event.petMeta.new) {
            performFirstSpawn(event.player)
        }
    }

    /**
     * Gets called when a player quits the server. Clear pet resources and fix enable state in persistence.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        if (petService.hasPet(event.player)) {
            val pet = petService.getOrSpawnPetFromPlayer(event.player).get()

            (pet.getHeadArmorstand() as EntityPetProxy).deleteFromWorld()
            pet.getHitBoxLivingEntity<EntityPetProxy>().ifPresent { p ->
                p.deleteFromWorld()
            }
        }

        persistencePetMetaService.clearResources(event.player)
        debugService.unRegister(event.player)
    }

    /**
     * Avoids saving the pet into the chunk data.
     */
    @EventHandler
    fun onChunkUnloadEvent(event: ChunkUnloadEvent) {
        for (entity in event.chunk.entities) {
            if (entity is EntityPetProxy) {
                entity.deleteFromWorld()
            }
        }
    }

    /**
     * Avoids loading an invalid pet into a chunk.
     */
    @EventHandler
    fun onChunkLoadEvent(event: ChunkLoadEvent) {
        entityService.cleanUpInvalidEntities(event.chunk.entities.toList())
    }

    /**
     * Cancels the entity interact event for pets.
     *
     * @param event event
     */
    @EventHandler
    fun onEntityInteractEvent(event: EntityInteractEvent) {
        val optPet = petService.findPetByEntity(event.entity)

        try {
            if (optPet != null && itemTypeService.findItemType<Any>(event.block.type) == itemTypeService.findItemType(
                    MaterialType.SOIL
                )
            ) {
                event.isCancelled = true
            }
        } catch (e: Exception) {
            // Ignored. Sometimes ItemStacks cannot be parsed but we do not care.
        }
    }

    /**
     * Gets called when a player presses the sneak button and removes the pet of the players head if present.
     *
     * @param event event
     */
    @EventHandler
    fun onEntityToggleSneakEvent(event: PlayerToggleSneakEvent) {
        unMountPet(event.player)
    }

    /**
     * Gets called when a player passenger presses the sneak button and removes the riding player.
     *
     * @param event event
     */
    @EventHandler
    fun onEntityDismountEvent(event: EntityDismountEvent) {
        if (event.entity !is Player) {
            return
        }

        unMountPet(event.entity as Player)
    }

    /**
     * Gets called when an animal gets leashed and cancels it for all pet entities.
     *
     * @param event event
     */
    @EventHandler
    fun onEntityLeashEvent(event: PlayerLeashEntityEvent) {
        val optPet = petService.findPetByEntity(event.entity)

        if (optPet != null) {
            event.isCancelled = true
        }
    }

    /**
     * Handles pet despawning and respawning on player respawn.
     */
    @EventHandler
    fun onPlayerRespawnEvent(event: PlayerRespawnEvent) {
        if (!petService.hasPet(event.player)) {
            return
        }

        val pet = petService.getOrSpawnPetFromPlayer(event.player).get()
        pet.remove()

        val warpDelay = configurationService.findValue<Int>("global-configuration.respawn-delay") * 20L

        sync(concurrencyService, warpDelay) {
            petService.getOrSpawnPetFromPlayer(event.player)
        }
    }

    /**
     * Handles allowing the entity spawn. Denies spawn prevention from other plugins.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntitySpawnEvent(event: EntitySpawnEvent) {
        if (event.entity is EntityPetProxy) {
            event.isCancelled = false
        }
    }

    /**
     * Handles pet despawning and respawning on player teleport.
     */
    @EventHandler
    fun onPlayerTeleportEvent(event: PlayerTeleportEvent) {
        if (!petService.hasPet(event.player)) {
            return
        }

        val pet = petService.getOrSpawnPetFromPlayer(event.player).get()


        if (event.to!!.world!!.name != event.from.world!!.name) {
            pet.remove()

            val warpDelay = configurationService.findValue<Int>("global-configuration.teleport-delay") * 20L + 120

            sync(concurrencyService, warpDelay) {
                petService.getOrSpawnPetFromPlayer(event.player)
            }

            return
        }

        Entity::class.java.getDeclaredMethod("getPassenger").invoke(event.player) as Entity? ?: return

        val fallOffHead = configurationService.findValue<Boolean>("global-configuration.teleport-fall")

        if (fallOffHead) {
            pet.meta.aiGoals.removeIf { a -> a is AIGroundRiding || a is AIFlyRiding || a is AIWearing }
            return
        }

        event.isCancelled = true
        event.player.teleportUnsafe(event.to!!)
    }

    /**
     * Performs the first spawn of the pet if enabled.
     */
    private fun performFirstSpawn(player: Player) {
        val applyPetOnFirstSpawn =
            configurationService.findValue<Boolean>("global-configuration.apply-pet-on-first-spawn")

        if (applyPetOnFirstSpawn) {
            petService.getOrSpawnPetFromPlayer(player)
        }
    }

    /**
     * UnMounts the pet of the given [player].
     */
    private fun unMountPet(player: Player) {
        if (!petService.hasPet(player)) {
            return
        }

        val pet = petService.getOrSpawnPetFromPlayer(player).get()

        for (name in configurationService.findValue<List<String>>("global-configuration.disable-on-sneak")) {
            var changed = false

            for (ai in pet.meta.aiGoals.toTypedArray()) {
                if (ai.type == name) {
                    pet.meta.aiGoals.remove(ai)
                    changed = true
                }
            }

            if (changed && version.isVersionSameOrGreaterThan(Version.VERSION_1_14_R1)) {
                // Execute a tick on the Armorstand manually since 1.14 passengers do not tick.
                // This is required by wearing pets.
                val handle =
                    findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftLivingEntity").getDeclaredMethod("getHandle")
                        .invoke(pet.getHeadArmorstand())

                val method =
                    findClazz("com.github.shynixn.petblocks.bukkit.logic.business.nms.VERSION.NMSPetArmorstand")
                        .getDeclaredMethod("doTick")
                method.isAccessible = true

                method.invoke(handle)
            }
        }
    }

    /**
     * Loads the PetBlocks data.
     */
    private fun loadPetBlocks(player: Player) {
        if (!player.isOnline || (player.world as World?) == null) {
            return
        }

        persistencePetMetaService.refreshPetMetaFromRepository(player).thenAcceptSafely { petMeta ->
            if (player.isOnline && (player.world as World?) != null) {
                val optPet: PetProxy? = if (petMeta.enabled) {
                    val pet = petService.getOrSpawnPetFromPlayer(player)

                    if (pet.isPresent) {
                        pet.get()
                    } else {
                        null
                    }
                } else {
                    null
                }

                val joinEvent = PetBlocksLoginEvent(player, petMeta, optPet)
                Bukkit.getPluginManager().callEvent(joinEvent)
            }

            if (alreadyLoading.contains(player.uniqueId)) {
                alreadyLoading.remove(player.uniqueId)
            }
        }
    }
}