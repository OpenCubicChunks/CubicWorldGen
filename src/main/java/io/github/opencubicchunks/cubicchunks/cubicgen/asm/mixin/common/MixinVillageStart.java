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

import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.feature.CubicVillageGenerator;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mixin(MapGenVillage.Start.class)
public abstract class MixinVillageStart extends StructureStart implements CubicVillageGenerator.CubicStart {

    @Unique private Random cubicWorldGen$constructorRandom;

    // added by class transformer (MapGenVillageCubicConstructorTransform)

    @SuppressWarnings("ShadowTarget")
    @Dynamic
    @Shadow(remap = false)
    private void reinitCubicVillage(World world, Random random, int chunkX, int chunkZ, int size) {
        throw new AssertionError();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Ljava/util/Random;III)V", at = @At("RETURN"))
    private void cubicChunksConstruct(World worldIn, Random random, int chunkX, int chunkZ, int villageSize, CallbackInfo ci) {
        this.cubicWorldGen$constructorRandom = random;
    }

    @Override
    public void initCubicVillage(World world, int cubeY, int size) {
        this.initCubic(world, cubeY);
        this.reinitCubicVillage(world, cubicWorldGen$constructorRandom, getChunkPosX(), getChunkPosZ(), size);
    }
}
