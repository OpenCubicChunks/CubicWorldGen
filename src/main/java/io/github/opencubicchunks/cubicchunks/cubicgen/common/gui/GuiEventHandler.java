/*
 *  This file is part of Cubic World Generation, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui;

import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomCubicWorldType;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListWorldSelectionEntry;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * This GUI handler serve a sole purpose to copy CustomCubic generator settings
 * in case if player making a copy of such world
 */
public class GuiEventHandler {
    
    @SubscribeEvent
    public void onButtonPressed(GuiScreenEvent.ActionPerformedEvent.Pre action) {
        if (action.getGui() instanceof GuiWorldSelection) {
            if (!action.getButton().enabled)
                return;
            // "Recreate world" has button id = 5
            if (action.getButton().id != 5)
                return;
            GuiWorldSelection gui = (GuiWorldSelection) action.getGui();
            if (gui.selectionList.getSelectedWorld() == null)
                return;
            GuiListWorldSelectionEntry entry = gui.selectionList.getSelectedWorld();
            ISaveHandler isavehandler = Minecraft.getMinecraft().getSaveLoader()
                    .getSaveLoader(entry.worldSummary.getFileName(), false);
            WorldInfo worldinfo = isavehandler.loadWorldInfo();

            if (worldinfo == null || !(worldinfo.getTerrainType() instanceof CustomCubicWorldType))
                return;
            String json = CustomGeneratorSettings.loadJsonStringFromSaveFolder(isavehandler);
            isavehandler.flush();
            if (json != null) {
                CustomCubicGui.settingsJsonString = json;
            }
        }
    }
}
