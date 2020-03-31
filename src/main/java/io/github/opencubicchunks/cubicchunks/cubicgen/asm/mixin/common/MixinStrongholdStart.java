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

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.feature.CubicStrongholdGenerator;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mixin(MapGenStronghold.Start.class)
public abstract class MixinStrongholdStart extends StructureStart implements CubicStrongholdGenerator.CubicStart {

    private Random constructorRandom;

    // added by class transformer (MapGenStrongholdCubicConstructorTransform)

    @Dynamic @Shadow(remap = false) private void reinitCubicStronghold(World world, Random random, int chunkX, int chunkZ) {
        throw new AssertionError();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Ljava/util/Random;II)V", at = @At("RETURN"))
    private void cubicChunksConstruct(World worldIn, Random random, int chunkX, int chunkZ, CallbackInfo ci) {
        this.constructorRandom = random;
    }

    @Override
    public void initCubicStronghold(World world, int cubeY, int maxTopY) {
        this.initCubic(world, cubeY);
        this.reinitCubicStronghold(world, constructorRandom, getChunkPosX(), getChunkPosZ());

        int currCenterY = this.boundingBox.getYSize() / 2;
        int targetCenterY = Coords.localToBlock(cubeY, constructorRandom.nextInt(ICube.SIZE));
        int dy = targetCenterY - currCenterY;

        {
            int movedTopY = this.boundingBox.getYSize() + 1 + dy;
            if (movedTopY > maxTopY) {
                dy -= movedTopY - maxTopY;
            }
        }
        this.boundingBox.offset(0, dy, 0);

        for (StructureComponent structurecomponent : this.components) {
            structurecomponent.offset(0, dy, 0);
        }
    }
}
