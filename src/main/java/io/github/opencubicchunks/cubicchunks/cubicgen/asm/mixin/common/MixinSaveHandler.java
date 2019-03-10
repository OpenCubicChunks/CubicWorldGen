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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.world.storage.IWorldInfoAccess;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettingsFixer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

@Mixin(SaveHandler.class)
public class MixinSaveHandler {

    @Inject(method = "loadWorldInfo", at = @At("RETURN"))
    public void onLoadWorldInfo(CallbackInfoReturnable<WorldInfo> cir) {
        if (cir.getReturnValue() == null)
            return;
        String generatorOptions = CustomGeneratorSettings.loadJsonStringFromSaveFolder((ISaveHandler) (Object) this);
        if (generatorOptions == null)
            return;
        boolean isOutdated = !CustomGeneratorSettingsFixer.isUpToDate(generatorOptions);
        if (isOutdated) {
            generatorOptions = CustomGeneratorSettingsFixer.fixGeneratorOptions(generatorOptions, null);
            CustomGeneratorSettings.saveToFile((ISaveHandler) (Object) this, generatorOptions);
        }
        IWorldInfoAccess worldInfo = (IWorldInfoAccess) cir.getReturnValue();
        worldInfo.setGeneratorOptions(generatorOptions);
    }

    @Inject(method = "saveWorldInfoWithPlayer", at = @At("RETURN"))
    public void onSavingWorldInfoWithPlayer(WorldInfo worldInformation, @Nullable NBTTagCompound tagCompound,
            CallbackInfo ci) {
        if (!CustomGeneratorSettings.getPresetFile((ISaveHandler) (Object) this).exists())
            CustomGeneratorSettings.saveToFile((ISaveHandler) (Object) this, worldInformation.getGeneratorOptions());
    }
}
