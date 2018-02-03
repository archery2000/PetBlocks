package com.github.shynixn.petblocks.bukkit.addon.buffeffects;

import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.addon.buffeffects.logic.PetBlocksAddonManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

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
public class PetBlocksPotionAddonPlugin extends JavaPlugin {

    private PetBlocksAddonManager manager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        if (Bukkit.getPluginManager().getPlugin("PetBlocks") == null) {
            this.getLogger().log(Level.WARNING, "PetBlocks plugin is not installed. Addon gets now disabled!");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.GREEN + "Loading Addon BuffEffects ...");
            manager = new PetBlocksAddonManager(this);
            Bukkit.getServer().getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.GREEN + "Enabled Addon BuffEffects " + this.getDescription().getVersion() + " by Shynixn");
        }
    }
}
