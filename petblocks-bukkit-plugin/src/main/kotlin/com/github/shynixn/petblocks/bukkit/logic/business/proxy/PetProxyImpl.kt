package com.github.shynixn.petblocks.bukkit.logic.business.proxy

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.bukkit.event.PetRemoveEvent
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIMovement
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.api.persistence.entity.Skin
import com.github.shynixn.petblocks.bukkit.logic.business.extension.*
import com.github.shynixn.petblocks.core.logic.business.extension.hasChanged
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.ItemEntity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import java.util.*

@Suppress("UNCHECKED_CAST")
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
class PetProxyImpl(override val meta: PetMeta, private val design: ArmorStand, private val owner: Player) :
    PetProxy {

    override var teleportTarget: Any? = null
    override var aiGoals: List<Any>? = null
    var hitBox: LivingEntity? = null

    private val particleService = PetBlocksApi.resolve(ParticleService::class.java)
    private val soundService = PetBlocksApi.resolve(SoundService::class.java)
    private val logger: LoggingService = PetBlocksApi.resolve(LoggingService::class.java)
    private val itemService = PetBlocksApi.resolve(ItemTypeService::class.java)

    /**
     * Init.
     */
    init {
        design.bodyPose = EulerAngle(0.0, 0.0, 2878.0)
        design.leftArmPose = EulerAngle(2878.0, 0.0, 0.0)
        design.setMetadata("keep", FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PetBlocks")!!, true))
        design.isCustomNameVisible = true
        design.removeWhenFarAway = false

        meta.enabled = true

        meta.propertyTracker.onPropertyChanged(PetMeta::displayName)
        meta.propertyTracker.onPropertyChanged(PetMeta::aiGoals)
        meta.propertyTracker.onPropertyChanged(Skin::typeName)

        design.equipment!!.boots = generateMarkerItemStack()
    }

    /**
     * Gets if the pet is dead or was removed.
     */
    override val isDead: Boolean
        get() = this.design.isDead || (hitBox != null && hitBox!!.isDead)

    /**
     * Gets the pet owner.
     */
    override fun <P> getPlayer(): P {
        return owner as P
    }

    /**
     * Gets the head armorstand.
     */
    override fun <A> getHeadArmorstand(): A {
        return design as A
    }

    /**
     * Gets the head of the head armorstand.
     */
    override fun <I> getHeadArmorstandItemStack(): I {
        return design.helmet.clone() as I
    }

    /**
     * Gets a living hitbox entity.
     */
    override fun <L> getHitBoxLivingEntity(): Optional<L> {
        return Optional.ofNullable(hitBox as L)
    }

    /**
     * Gets called from any Movement AI to play movement effects.
     */
    override fun playMovementEffects() {
        try {
            for (aiBase in meta.aiGoals) {
                if (aiBase is AIMovement) {
                    val location = getLocation<Location>()

                    if (meta.particleEnabled) {
                        particleService.playParticle(location, aiBase.movementParticle, owner)
                    }

                    if (meta.soundEnabled) {
                        soundService.playSound(location, aiBase.movementSound, owner)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to play moving sound and particle.", e)
        }
    }

    /**
     * Gets called when the hitbox changes.
     */
    override fun changeHitBox(hitBox: Any?) {
        if (hitBox !is LivingEntity?) {
            return
        }

        this.hitBox = hitBox

        if (hitBox == null) {
            return
        }

        hitBox.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1))
        hitBox.setMetadata("keep", FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PetBlocks")!!, true))
        hitBox.isCustomNameVisible = false
        hitBox.equipment!!.boots = generateMarkerItemStack()
    }

    /**
     * Teleports the pet to the given [location].
     */
    override fun <L> teleport(location: L) {
        var target: Any = location as Any

        if (target is Position) {
            target = target.toLocation()
        }

        if (target !is Location) {
            throw IllegalArgumentException("Location has to be a BukkitLocation!")
        }

        teleportTarget = target
    }

    /**
     * When an object implementing interface `Runnable` is used
     * to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing
     * thread.
     *
     *
     * The general contract of the method `run` is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread.run
     */
    override fun run() {
        if (!meta.enabled && !isDead) {
            val event = PetRemoveEvent(getPlayer(), this)
            Bukkit.getPluginManager().callEvent(event)

            if (event.isCancelled) {
                meta.enabled = true
                return
            }

            (this.design as EntityPetProxy).deleteFromWorld()

            if (this.hitBox != null) {
                (this.hitBox as EntityPetProxy).deleteFromWorld()
            }

            return
        }

        val displayNameChanged = PetMeta::displayName.hasChanged(meta)

        if (displayNameChanged) {
            design.customName = meta.displayName.translateChatColors()
        }

        if (displayNameChanged || Skin::typeName.hasChanged(meta.skin)) {
            val item = ItemEntity(
                meta.skin.typeName,
                meta.skin.dataValue,
                meta.skin.unbreakable,
                meta.displayName,
                null,
                meta.skin.owner
            )

            val itemStack = itemService.toItemStack<ItemStack>(item)
            design.setHelmet(itemStack)
        }
    }

    /**
     * Gets the location of the pet.
     */
    override fun <L> getLocation(): L {
        if (hitBox == null) {
            return this.design.location as L
        }

        return hitBox!!.location as L
    }

    /**
     * Removes the pet.
     */
    override fun remove() {
        meta.enabled = false
    }

    /**
     * Sets the velocity of the pet.
     */
    override fun <V> setVelocity(vector: V) {
        if (vector is Position) {
            if (hitBox != null) {
                hitBox!!.velocity = vector.toVector()
            } else {
                design.velocity = vector.toVector()
            }

            return
        }

        if (vector !is Vector) {
            throw IllegalArgumentException("Vector has to be a BukkitVector!")
        }

        if (hitBox != null) {
            hitBox!!.velocity = vector
        } else {
            design.velocity = vector
        }
    }

    /**
     * Gets the velocity of the pet.
     */
    override fun <V> getVelocity(): V {
        if (hitBox == null) {
            return this.design.velocity as V
        }

        return hitBox!!.velocity as V
    }

    /**
     * Gets a new marker itemstack.
     */
    private fun generateMarkerItemStack(): ItemStack {
        val item = ItemEntity("APPLE", 0, false, null, arrayListOf("PetBlocks"))
        return itemService.toItemStack(item)
    }
}