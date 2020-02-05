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
package io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.PresetLoadError;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.world.storage.IWorldInfoAccess;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomCubicWorldType;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.CustomGeneratorSettingsFixer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mixin(SaveHandler.class)
public class MixinSaveHandler {

    @Shadow @Final private File worldDirectory;

    @Inject(method = "loadWorldInfo", at = @At("RETURN"))
    private void onLoadWorldInfo(CallbackInfoReturnable<WorldInfo> cir) {
        if (cir.getReturnValue() == null || !(cir.getReturnValue().getTerrainType() instanceof CustomCubicWorldType))
            return;
        String generatorOptions = CustomGeneratorSettings.loadJsonStringFromSaveFolder((ISaveHandler) this);
        if (generatorOptions == null)
            return;
        try {
            String lastCwgVersion = getCwgVersionFromLevel(this.worldDirectory.toPath().resolve("level.dat"));
            generatorOptions = CustomGeneratorSettingsFixer.INSTANCE.fixJsonString(generatorOptions, lastCwgVersion);
            CustomGeneratorSettings.saveToFile((ISaveHandler) this, generatorOptions);
        } catch (PresetLoadError presetLoadError) {
            throw new RuntimeException(presetLoadError);
        }
        IWorldInfoAccess worldInfo = (IWorldInfoAccess) cir.getReturnValue();
        worldInfo.setGeneratorOptions(generatorOptions);
    }

    private String getCwgVersionFromLevel(Path levelDat) {
        try {
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(Files.newInputStream(levelDat));
            NBTTagCompound fml = nbt.getCompoundTag("FML");
            NBTTagList modList = fml.getTagList("ModList", Constants.NBT.TAG_COMPOUND);
            for (NBTBase entry : modList) {
                NBTTagCompound mod = (NBTTagCompound) entry;
                String id = mod.getString("ModId");
                if (id.equals(CustomCubicMod.MODID)) {
                    String v = mod.getString("ModVersion");
                    if (v.equals("${version}")) {
                        return CustomCubicMod.MODID;
                    }
                    return v;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // cubicgen wasn't there, so the world had to be loaded with pre-api version
        return "0.0.0.0";
    }


    @Inject(method = "saveWorldInfoWithPlayer", at = @At("RETURN"))
    private void onSavingWorldInfoWithPlayer(WorldInfo worldInformation, @Nullable NBTTagCompound tagCompound,
            CallbackInfo ci) {
        if (!(worldInformation.getTerrainType() instanceof CustomCubicWorldType))
            return;
        if (!CustomGeneratorSettings.getPresetFile((ISaveHandler) this).exists()) {
            CustomGeneratorSettings.saveToFile((ISaveHandler) this, worldInformation.getGeneratorOptions());
        }
    }
}
