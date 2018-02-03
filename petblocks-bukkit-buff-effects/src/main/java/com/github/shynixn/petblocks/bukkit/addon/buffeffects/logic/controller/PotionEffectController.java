package com.github.shynixn.petblocks.bukkit.addon.buffeffects.logic.controller;

import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.addon.buffeffects.api.controller.BuffEffectController;
import com.github.shynixn.petblocks.bukkit.addon.buffeffects.api.entity.BuffEffect;
import com.github.shynixn.petblocks.bukkit.addon.buffeffects.logic.entity.PotionEffectBuilder;
import org.bukkit.configuration.MemorySection;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;

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
public class PotionEffectController implements BuffEffectController {

    private final Map<Integer, PotionEffectBuilder[]> items = new HashMap<>();

    private final Plugin plugin;

    public PotionEffectController(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void reload() {
        this.items.clear();
        this.plugin.reloadConfig();
        final Map<String, Object> data = ((MemorySection) this.plugin.getConfig().get("effects")).getValues(false);
        for (final String key : data.keySet()) {
            try {
                final Map<String, Object> subData = ((MemorySection) data.get(key)).getValues(false);
                final PotionEffectBuilder[] result = new PotionEffectBuilder[subData.size()];
                int i = 0;
                for (final String subKey : subData.keySet()) {
                    final PotionEffectBuilder builder = new PotionEffectBuilder(((MemorySection) subData.get(subKey)).getValues(false));
                    result[i] = builder;
                    i++;
                }
                this.items.put(Integer.parseInt(key), result);
            } catch (final Exception e) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load guiItem " + key + '.', e);
            }
        }
    }

    @Override
    public void store(BuffEffect potionEffectBuilder) {
        throw new IllegalArgumentException("Cannot be added directory.");
    }

    @Override
    public void remove(BuffEffect potionEffectBuilder) {
        throw new IllegalArgumentException("Cannot be added directory.");

    }

    @Override
    public int size() {
        return this.items.size();

    }

    @Override
    public List<BuffEffect> getAll() {
        final List<BuffEffect> result = new ArrayList<>();
        for (final int key : this.items.keySet()) {
            result.addAll(Arrays.asList(this.items.get(key)));
        }
        return result;
    }

    @Override
    public void close() throws Exception {
        this.items.clear();
    }

    /**
     * Returns the given buff potionEffects from the given engine id.
     *
     * @param engineId engineid
     * @return potioneffects
     */
    @Override
    public Optional<BuffEffect[]> getPotionEffectsFromEngine(int engineId) {
        if (this.items.containsKey(engineId)) {
            return Optional.of(this.items.get(engineId));
        }
        return Optional.empty();
    }
}
