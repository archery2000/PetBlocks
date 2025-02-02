package com.github.shynixn.petblocks.core.logic.business.commandexecutor

import com.github.shynixn.petblocks.api.business.command.SourceCommand
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.GUIItemLoadService
import com.github.shynixn.petblocks.api.business.service.MessageService
import com.google.inject.Inject

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
class ReloadCommandExecutorImpl @Inject constructor(
    private val configurationService: ConfigurationService,
    private val loadService: GUIItemLoadService,
    private val messageService: MessageService
) : SourceCommand {
    /**
     * Gets called when the given [source] executes the defined command with the given [args].
     */
    override fun <S> onExecuteCommand(source: S, args: Array<out String>): Boolean {
        loadService.reload()
        configurationService.reload()
        messageService.sendSourceMessage(source, "Reloaded PetBlocks.")

        return true
    }
}

