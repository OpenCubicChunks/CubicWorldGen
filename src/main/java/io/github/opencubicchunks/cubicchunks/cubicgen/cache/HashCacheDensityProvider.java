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
package io.github.opencubicchunks.cubicchunks.cubicgen.cache;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.Vec3i;

import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HashCacheDensityProvider {

    private final double[] cache;
    private final int[] keys;
    private final HashFunction hashFunction;
    private final DensityProvider source;

    @SuppressWarnings("unchecked")
    private HashCacheDensityProvider(int size, HashFunction hashCode, DensityProvider source) {
        this.cache = new double[size];
        this.keys = new int[size * 4];
        this.hashFunction = hashCode;
        this.source = source;
    }

    public double get(int x, int y, int z, double biomeVol, double biomeHeight, double height2d) {
        int index = index(hashFunction.hash(x, y, z));
        int index2 = index << 2;
        double value;
        if (!equals(x, y, z, index2)) {
            keys[index2] = x;
            keys[index2|1] = y;
            keys[index2|2] = z;
            cache[index] = value = source.get(x, y, z, biomeVol, biomeHeight, height2d);
        } else {
            value = cache[index];
        }
        return value;
    }

    private boolean equals(int x, int y, int z, int index) {
        if (keys[index] != x) {
            return false;
        }
        if (keys[index|1] != y) {
            return false;
        }
        if (keys[index|2] != z) {
            return false;
        }
        return false;
    }

    private int index(int hash) {
        return Math.floorMod(hash, cache.length);
    }

    public static HashCacheDensityProvider create(int size, HashFunction hashCode, DensityProvider source) {
        return new HashCacheDensityProvider(size, hashCode, source);
    }

    public interface HashFunction {
        int hash(int x, int y, int z);
    }

    @FunctionalInterface
    public interface DensityProvider {
        double get(int x, int y, int z, double biomeVol, double biomeHeight, double height2d);
    }
}
