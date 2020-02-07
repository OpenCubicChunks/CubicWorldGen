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
package io.github.opencubicchunks.cubicchunks.cubicgen;

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome.oceanWaterReplacer;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome.terrainShapeReplacer;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.MesaSurfaceReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.MutatedSavannaSurfaceReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.SwampWaterWithLilypadReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.TaigaSurfaceReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomCubicWorldType;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator.DefaultDecorator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator.DesertDecorator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator.ForestDecorator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator.JungleDecorator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator.PlainsDecorator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator.SavannaDecorator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator.SnowBiomeDecorator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator.SwampDecorator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator.TaigaDecorator;
import io.github.opencubicchunks.cubicchunks.cubicgen.flat.FlatCubicWorldType;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeBeach;
import net.minecraft.world.biome.BiomeDesert;
import net.minecraft.world.biome.BiomeForest;
import net.minecraft.world.biome.BiomeForestMutated;
import net.minecraft.world.biome.BiomeHills;
import net.minecraft.world.biome.BiomeJungle;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.biome.BiomeMushroomIsland;
import net.minecraft.world.biome.BiomeOcean;
import net.minecraft.world.biome.BiomePlains;
import net.minecraft.world.biome.BiomeRiver;
import net.minecraft.world.biome.BiomeSavanna;
import net.minecraft.world.biome.BiomeSavannaMutated;
import net.minecraft.world.biome.BiomeSnow;
import net.minecraft.world.biome.BiomeStoneBeach;
import net.minecraft.world.biome.BiomeSwamp;
import net.minecraft.world.biome.BiomeTaiga;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mod(modid = CustomCubicMod.MODID,
        dependencies = "required-after:cubicchunks@[1.11.2-0.0.1047.0-SNAPSHOT,);after:forge@[13.20.1.2454,)",
        acceptableRemoteVersions = "*",
        version = CustomCubicMod.VERSION
)
@Mod.EventBusSubscriber
public class CustomCubicMod {

    public static final String MODID = "cubicgen";
    public static final String VERSION = "0.0.9999.0";
    public static final String MALISIS_VERSION = "@@MALISIS_VERSION@@";

    public static final boolean DEBUG_ENABLED = false;
    public static Logger LOGGER = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        LOGGER = e.getModLog();
        ConversionUtils.initFlowNoiseHack();

        FlatCubicWorldType.create();
        CustomCubicWorldType.create();
        DebugWorldType.create();
    }

    @Mod.EventHandler
    public void preInit(FMLPostInitializationEvent e) {
        CubicBiome.postInit();
    }

    @SubscribeEvent
    public static void registerRegistries(RegistryEvent.NewRegistry evt) {
        CubicBiome.init();
    }

    @SubscribeEvent
    public static void registerCubicBiomes(RegistryEvent.Register<CubicBiome> event) {
        // Vanilla biomes are initialized during bootstrap which happens before registration events
        // so it should be safe to use them here
        autoRegister(event, Biome.class, b -> b
                .addDefaultBlockReplacers()
                .defaultDecorators());
        autoRegister(event, BiomeBeach.class, b -> b
                .addDefaultBlockReplacers()
                .defaultDecorators());
        autoRegister(event, BiomeDesert.class, b -> b
                .addDefaultBlockReplacers()
                .defaultDecorators().decorator(new DesertDecorator()));
        autoRegister(event, BiomeForest.class, b -> b
                .addDefaultBlockReplacers()
                .decorator(new ForestDecorator()).defaultDecorators());
        autoRegister(event, BiomeForestMutated.class, b -> b
                .addDefaultBlockReplacers()
                .decorator(new ForestDecorator()).defaultDecorators());
        autoRegister(event, BiomeHills.class, b -> b
                .addDefaultBlockReplacers()
                .defaultDecorators());
        autoRegister(event, BiomeJungle.class, b -> b
                .addDefaultBlockReplacers()
                .defaultDecorators().decorator(new JungleDecorator()));
        autoRegister(event, BiomeMesa.class, b -> b
                .addBlockReplacer(terrainShapeReplacer()).addBlockReplacer(MesaSurfaceReplacer.provider()).addBlockReplacer(oceanWaterReplacer())
                .decoratorProvider(DefaultDecorator.Ores::new).decoratorProvider(DefaultDecorator::new));
        autoRegister(event, BiomeMushroomIsland.class, b -> b
                .addDefaultBlockReplacers()
                .defaultDecorators());
        autoRegister(event, BiomeOcean.class, b -> b
                .addDefaultBlockReplacers()
                .defaultDecorators());
        autoRegister(event, BiomePlains.class, b -> b
                .addDefaultBlockReplacers()
                .decorator(new PlainsDecorator()).defaultDecorators());
        autoRegister(event, BiomeRiver.class, b -> b
                .addDefaultBlockReplacers()
                .defaultDecorators());
        autoRegister(event, BiomeSavanna.class, b -> b
                .addDefaultBlockReplacers()
                .decorator(new SavannaDecorator()).defaultDecorators());
        autoRegister(event, BiomeSavannaMutated.class, b -> b
                .addBlockReplacer(terrainShapeReplacer()).addBlockReplacer(MutatedSavannaSurfaceReplacer.provider()).addBlockReplacer(oceanWaterReplacer())
                .defaultDecorators());
        autoRegister(event, BiomeSnow.class, b -> b
                .addDefaultBlockReplacers()
                .decorator(new SnowBiomeDecorator()).defaultDecorators());
        autoRegister(event, BiomeStoneBeach.class, b -> b
                .addDefaultBlockReplacers()
                .defaultDecorators());
        autoRegister(event, BiomeSwamp.class, b -> b
                .addDefaultBlockReplacers().addBlockReplacer(SwampWaterWithLilypadReplacer.provider())
                .defaultDecorators().decorator(new SwampDecorator()));
        autoRegister(event, BiomeTaiga.class, b -> b
                .addBlockReplacer(terrainShapeReplacer()).addBlockReplacer(TaigaSurfaceReplacer.provider()).addBlockReplacer(oceanWaterReplacer())
                .decorator(new TaigaDecorator()).defaultDecorators());

    }

    private static void autoRegister(RegistryEvent.Register<CubicBiome> event, Class<? extends Biome> cl, Consumer<CubicBiome.Builder> cons) {
        ForgeRegistries.BIOMES.getValues().stream()
                .filter(x -> x.getRegistryName().getResourceDomain().equals("minecraft"))
                .filter(x -> x.getClass() == cl).forEach(b -> {
            CubicBiome.Builder builder = CubicBiome.createForBiome(b);
            cons.accept(builder);
            CubicBiome biome = builder.defaultPostDecorators().setRegistryName(MODID, b.getRegistryName().getResourcePath()).create();
            event.getRegistry().register(biome);
        });
    }

    public static ResourceLocation location(String name) {
        return new ResourceLocation(MODID, name);
    }
}
