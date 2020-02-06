/*
 *  This file is part of Cubic World Generation, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015-2020 contributors
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
package io.github.opencubicchunks.cubicchunks.cubicgen.flat;

import io.github.opencubicchunks.cubicchunks.api.util.IntRange;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldType;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.MinimalCustomizeWorldGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.FlatGeneratorSettings;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlatCubicWorldType extends WorldType implements ICubicWorldType {

    public FlatCubicWorldType() {//todo: make it private, used in test
        super("FlatCubic");
    }

    public static FlatCubicWorldType create() {
        return new FlatCubicWorldType();
    }

    @Override
    public ICubeGenerator createCubeGenerator(World world) {
        return new FlatTerrainProcessor(world);
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        return new BiomeProviderSingle(Biomes.PLAINS);
    }

    @Override public IntRange calculateGenerationHeightRange(WorldServer world) {
        return new IntRange(0, 256); // TODO: Flat generation height range
    }

    @Override public boolean hasCubicGeneratorForWorld(World w) {
        // See CustomCubicWorldType.hasCubicGeneratorForWorld for explanation
        return w.provider.getClass() == DimensionManager.getProvider(0).getClass();
    }

    public boolean isCustomizable() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(Minecraft mc, GuiCreateWorld guiCreateWorld) {
        mc.displayGuiScreen(new MinimalCustomizeWorldGui(guiCreateWorld,
                FlatGeneratorSettings.fromJson(guiCreateWorld.chunkProviderSettingsJson).toJson(),
                preset -> {
            try {
                FlatGeneratorSettings.fromJson(preset);
                return true;
            } catch (RuntimeException ex) {
                return false;
            }
        }));
    }
}
