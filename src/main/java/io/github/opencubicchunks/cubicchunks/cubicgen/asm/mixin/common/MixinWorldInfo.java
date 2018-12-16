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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomCubicWorldType;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraft.world.storage.WorldInfo;

@Mixin(WorldInfo.class)
public class MixinWorldInfo {

    @Shadow
    private String levelName;
    @Shadow
    private WorldType terrainType;

    @Inject(method = "getGeneratorOptions", at = @At("HEAD"), cancellable = true)
    private void getGeneratorOptionsFromExternalFile(CallbackInfoReturnable<String> cbir) {
        if (!(terrainType instanceof CustomCubicWorldType))
            return;
        if(levelName.equals("MpServer"))
            return;
        if (CustomCubicMod.SERVER == null)
            throw new NullPointerException("Server is null");
        File externalGeneratorPresetFile = new File(((SaveFormatOld) CustomCubicMod.SERVER.getActiveAnvilConverter()).savesDirectory,
                levelName + "/data/" + CustomCubicMod.MODID + "/custom_generator_settings.json");
        if (!externalGeneratorPresetFile.exists()) {
            CustomCubicMod.LOGGER.info("No settings provided at " + externalGeneratorPresetFile.getAbsolutePath());
            CustomCubicMod.LOGGER.info("Loading settings from 'level.dat'");
            return;
        }
        try {
            FileReader reader = new FileReader(externalGeneratorPresetFile);
            CharBuffer sb = CharBuffer.allocate(Short.MAX_VALUE << 3);
            reader.read(sb);
            sb.flip();
            cbir.setReturnValue(sb.toString());
            cbir.cancel();
            reader.close();
            CustomCubicMod.LOGGER.info("Loading settings provided at " + externalGeneratorPresetFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
