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

import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.PresetLoadError;
import org.spongepowered.asm.mixin.Mixin;
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

@Mixin(SaveHandler.class)
public class MixinSaveHandler {

    @Inject(method = "loadWorldInfo", at = @At("RETURN"))
    private void onLoadWorldInfo(CallbackInfoReturnable<WorldInfo> cir) {
        if (cir.getReturnValue() == null || !(cir.getReturnValue().getTerrainType() instanceof CustomCubicWorldType))
            return;
        String generatorOptions = CustomGeneratorSettings.loadJsonStringFromSaveFolder((ISaveHandler) this);
        if (generatorOptions == null)
            return;
        try {
            generatorOptions = CustomGeneratorSettingsFixer.INSTANCE.fixJsonString(generatorOptions);
            CustomGeneratorSettings.saveToFile((ISaveHandler) this, generatorOptions);
        } catch (PresetLoadError presetLoadError) {
            throw new RuntimeException(presetLoadError);
        }
        IWorldInfoAccess worldInfo = (IWorldInfoAccess) cir.getReturnValue();
        worldInfo.setGeneratorOptions(generatorOptions);
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
