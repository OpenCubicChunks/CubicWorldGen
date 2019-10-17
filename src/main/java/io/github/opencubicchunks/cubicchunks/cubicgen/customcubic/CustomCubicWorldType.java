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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic;

import blue.endless.jankson.JsonGrammar;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldType;
import io.github.opencubicchunks.cubicchunks.api.util.IntRange;
import io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common.accessor.IBiomeProvider;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.CustomGeneratorSettingsFixer;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.init.Biomes;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomCubicWorldType extends WorldType implements ICubicWorldType {

    private CustomCubicWorldType() {
        super("CustomCubic");
    }

    public static CustomCubicWorldType create() {
        return new CustomCubicWorldType();
    }

    private IBiomeProvider self() {
        return (IBiomeProvider) this;
    }

    @Override public IntRange calculateGenerationHeightRange(WorldServer world) {
        CustomGeneratorSettings opts = CustomGeneratorSettings.getFromWorld(world);
        // TODO: better handling of min height
        return new IntRange(0, (int) opts.actualHeight);
    }

    @Override public boolean hasCubicGeneratorForWorld(World w) {
        return w.provider.getClass() == WorldProviderSurface.class; // a more general way to check if it's overworld
    }

    public BiomeProvider getBiomeProvider(World world) {
        if ("true".equalsIgnoreCase(System.getProperty("cubicchunks.debug.biomes"))) {
            return new BiomeProvider() {{
                self().setGenBiomes(new GenLayerDebug(4));
                self().setBiomeIndexLayer(new GenLayerDebug(4 + 2));
            }};
        } else {
            if (world.isRemote)
                return new BiomeProviderSingle(Biomes.PLAINS);
            CustomGeneratorSettings conf = CustomGeneratorSettings.getFromWorld(world);
            return makeBiomeProvider(world, conf);
        }
    }

    public static BiomeProvider makeBiomeProvider(World world, CustomGeneratorSettings conf) {
        WorldSettings fakeSettings = new WorldSettings(world.getWorldInfo());
        ChunkGeneratorSettings.Factory fakeGenOpts = new ChunkGeneratorSettings.Factory();
        fakeGenOpts.biomeSize = conf.biomeSize;
        fakeGenOpts.riverSize = conf.riverSize;
        fakeSettings.setGeneratorOptions(fakeGenOpts.toString());
        WorldInfo fakeInfo = new WorldInfo(fakeSettings, world.getWorldInfo().getWorldName());
        fakeInfo.setTerrainType(WorldType.CUSTOMIZED);
        Biome biome = Biome.getBiomeForId(conf.biome);
        return conf.biome < 0 ? new BiomeProvider(fakeInfo) : new BiomeProviderSingle(biome == null ? Biomes.OCEAN : biome);
    }

    @Override
    public ICubeGenerator createCubeGenerator(World world) {
        return new CustomTerrainGenerator(world, world.getSeed());
    }

    public boolean isCustomizable() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(Minecraft mc, GuiCreateWorld guiCreateWorld) {
        if (Loader.isModLoaded("malisiscore")) {
            new CustomCubicGui(guiCreateWorld).display();
        } else {
            mc.displayGuiScreen(new MinimalCustomizeWorldGui(guiCreateWorld,
                    CustomGeneratorSettingsFixer.INSTANCE.fixJson(guiCreateWorld.chunkProviderSettingsJson).toJson(JsonGrammar.COMPACT),
                    preset -> {
                        try {
                            CustomGeneratorSettings.fromJson(preset);
                            return true;
                        } catch (RuntimeException ex) {
                            return false;
                        }
                    }));
        }
    }

    private static class GenLayerDebug extends GenLayer {

        private final ArrayList<Biome> biomes;
        private int scaleBits;

        GenLayerDebug(int scaleBits) {
            super(0);
            this.scaleBits = scaleBits;

            this.biomes = new ArrayList<>();
            // use reflection to get all biomes
            for (Field fld : Biomes.class.getDeclaredFields()) {
                if (Biome.class.isAssignableFrom(fld.getType())) {
                    try {
                        Biome b = (Biome) fld.get(null);
                        if (b != null) {
                            this.biomes.add(b);
                        }

                    } catch (IllegalAccessException e) {
                        throw new Error(e);
                    }
                }
            }
        }

        @Override
        public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
            int[] biomes = IntCache.getIntCache(areaWidth * areaHeight);
            for (int offY = 0; offY < areaHeight; ++offY) {
                for (int offX = 0; offX < areaWidth; ++offX) {
                    int index = (offX + areaX) >> scaleBits;
                    index = Math.floorMod(index, this.biomes.size());
                    Biome biome = this.biomes.get(index);
                    biomes[offX + offY * areaWidth] = Biome.getIdForBiome(biome);
                }
            }
            return biomes;
        }
    }

}
