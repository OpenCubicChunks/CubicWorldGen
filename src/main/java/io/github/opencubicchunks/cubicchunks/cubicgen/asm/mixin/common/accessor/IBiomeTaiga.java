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
package io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common.accessor;

import net.minecraft.world.biome.BiomeTaiga;
import net.minecraft.world.gen.feature.WorldGenBlockBlob;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeTaiga.class)
public interface IBiomeTaiga {
    @Accessor BiomeTaiga.Type getType();
    @Accessor("PINE_GENERATOR") static WorldGenTaiga1 getPineGenerator() {
        throw new Error("IBiomeTaiga failed to apply");
    }
    @Accessor("SPRUCE_GENERATOR") static WorldGenTaiga2 getSpruceGenerator() {
        throw new Error("IBiomeTaiga failed to apply");
    }
    @Accessor("MEGA_PINE_GENERATOR") static WorldGenMegaPineTree getMegaPineGenerator() {
        throw new Error("IBiomeTaiga failed to apply");
    }
    @Accessor("MEGA_SPRUCE_GENERATOR") static WorldGenMegaPineTree getMegaSpruceGenerator() {
        throw new Error("IBiomeTaiga failed to apply");
    }
    @Accessor("FOREST_ROCK_GENERATOR") static WorldGenBlockBlob getForestRockGenerator() {
        throw new Error("IBiomeTaiga failed to apply");
    }
}
