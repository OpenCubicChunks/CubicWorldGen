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
package io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;

import javax.annotation.Nullable;

public class BiomeDesc {

    private final String biomeId;
    private final Biome biome;

    public BiomeDesc(String biomeId) {
        this.biomeId = biomeId;
        ResourceLocation id = new ResourceLocation(biomeId);
        if (!ForgeRegistries.BIOMES.containsKey(id)) {
            this.biome = null;
            return;
        }
        this.biome = ForgeRegistries.BIOMES.getValue(id);
    }

    public BiomeDesc(Biome biome) {
        this.biome = biome;
        this.biomeId = ForgeRegistries.BIOMES.getKey(biome).toString();
    }

    public String getBiomeId() {
        return biomeId;
    }

    @Nullable public Biome getBiome() {
        return biome;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BiomeDesc biomeDesc = (BiomeDesc) o;
        return biomeId.equals(biomeDesc.biomeId);
    }

    @Override public int hashCode() {
        return Objects.hash(biomeId);
    }

    @Override public String toString() {
        return "BiomeDesc{" +
                "biomeId='" + biomeId + '\'' +
                '}';
    }
}
