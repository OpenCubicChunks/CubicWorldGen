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

import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.feature.CubicStrongholdGenerator;
import mcp.MethodsReturnNonnullByDefault;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mixin(MapGenStronghold.Start.class)
public abstract class MixinStrongholdStart extends StructureStart implements CubicStrongholdGenerator.CubicStart {

    private Random constructorRandom;

    // added by class transformer (MapGenStrongholdCubicConstructorTransform)

    @SuppressWarnings("ShadowTarget")
    @Dynamic
    @Shadow(remap = false)
    private int cubicchunks$baseY;

    @SuppressWarnings("ShadowTarget")
    @Dynamic
    @Shadow(remap = false)
    private void reinitCubicStronghold(World world, Random random, int chunkX, int chunkZ) {
        throw new AssertionError();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Ljava/util/Random;II)V", at = @At("RETURN"))
    private void cubicChunksConstruct(World worldIn, Random random, int chunkX, int chunkZ, CallbackInfo ci) {
        this.constructorRandom = random;
    }

    @Group(name = "CC_StrongholdStart_reinit_markAvailableHeight", min = 1)
    @Dynamic
    @Redirect(method = "reinitCubicStronghold", remap = false, require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/structure/MapGenStronghold$Start;markAvailableHeight(Lnet/minecraft/world/World;Ljava/util/Random;I)V"))
    private void offsetTo(MapGenStronghold.Start self, World worldIn, Random rand, int targetY) {
        int offset = targetY - (this.boundingBox.minY + this.boundingBox.maxY) / 2;
        this.boundingBox.offset(0, offset, 0);

        for (StructureComponent structurecomponent : this.components) {
            structurecomponent.offset(0, offset, 0);
        }
    }

    // Mixin AP doesn't generate a refmap entry here because the target method doesn't exist at compile time
    // as a workaround, here is a second version of the redirect with SRG target
    @Group(name = "CC_StrongholdStart_reinit_markAvailableHeight", min = 1)
    @Dynamic
    @Redirect(method = "reinitCubicStronghold", remap = false, require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/structure/MapGenStronghold$Start;func_75067_a(Lnet/minecraft/world/World;Ljava/util/Random;I)V"))
    private void offsetTo_Obf(MapGenStronghold.Start self, World worldIn, Random rand, int targetY) {
        offsetTo(self, worldIn, rand, targetY);
    }

    @Override
    public void initCubicStronghold(World world, int cubeY, int baseY) {
        cubicchunks$baseY = baseY;
        this.initCubic(world, cubeY);
        this.reinitCubicStronghold(world, constructorRandom, getChunkPosX(), getChunkPosZ());
    }
}
