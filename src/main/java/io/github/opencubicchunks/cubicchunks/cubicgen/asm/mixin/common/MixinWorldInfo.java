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
package io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.world.storage.IWorldInfoAccess;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomCubicWorldType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;

/**
 * Allow us to safely store long strings in {@code generatorOptions} without
 * throwing {@code UTFDataFormatException} in attempt to save such strings in NBT.
 */
@Mixin(WorldInfo.class)
@Implements(@Interface(iface = IWorldInfoAccess.class, prefix = "worldinfo$"))
public class MixinWorldInfo implements IWorldInfoAccess {
    
    @Shadow
    private WorldType terrainType;
    
    @Shadow
    private String generatorOptions;

    @Redirect(method = "updateTagCompound", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;setString(Ljava/lang/String;Ljava/lang/String;)V"), require = 1)
    private void redirectNBTSetString(NBTTagCompound nbt, String key, String value) {
        if (terrainType instanceof CustomCubicWorldType && key.equals("generatorOptions"))
            return;
        nbt.setString(key, value);
    }

    @Override
    public void setGeneratorOptions(String generatorOptionsIn) {
        generatorOptions = generatorOptionsIn;
    }
}
