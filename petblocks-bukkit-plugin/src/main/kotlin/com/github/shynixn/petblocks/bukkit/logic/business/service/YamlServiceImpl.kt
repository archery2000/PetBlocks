package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.service.YamlService
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration

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
class YamlServiceImpl : YamlService {
    /**
     * Writes the given yaml content to a string.
     */
    override fun writeToString(content: Map<String, Any?>): String {
        val configuration = YamlConfiguration()

        for (key in content.keys) {
            configuration.set(key, content[key])
        }

        return configuration.saveToString()
    }

    /**
     * Reads the given yaml content from a string.c
     */
    override fun readFromString(content: String): Map<String, Any> {
        val configuration = YamlConfiguration()
        configuration.loadFromString(content)
        val section = configuration.getValues(false)
        deserialize(section)
        return section
    }

    /**
     * DeSerializes the given section.
     */
    private fun deserialize(section: MutableMap<String, Any?>) {
        for (key in section.keys) {
            if (section[key] is MemorySection) {
                val map = (section[key] as MemorySection).getValues(false)
                deserialize(map)
                section[key] = map
            }
        }
    }
}