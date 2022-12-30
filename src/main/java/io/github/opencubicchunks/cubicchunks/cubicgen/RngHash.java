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
package io.github.opencubicchunks.cubicchunks.cubicgen;

public class RngHash {
    private static final int c1 = 0xcc9e2d51;
    private static final int c2 = 0x1b873593;
    private static final int r1 = 15;
    private static final int r2 = 13;
    private static final int m = 5;
    private static final int n = 0xe6546b64;

    public static int combineSeedsForNoise(long worldSeed, int featureSeed) {
        return murmurHashFinalize(murmurHashCoords(0, (int) worldSeed, (int) (worldSeed >>> 32), featureSeed));
    }

    public static int murmurHashInt(int hash, int v) {
        hash ^= Integer.rotateLeft(v * c1, r1) * c2;
        hash = Integer.rotateLeft(hash, r2) * m + n;

        return hash;
    }

    public static int murmurHashCoords(int hash, int x, int y, int z) {
        hash ^= Integer.rotateLeft(x * c1, r1) * c2;
        hash = Integer.rotateLeft(hash, r2) * m + n;

        hash ^= Integer.rotateLeft(y * c1, r1) * c2;
        hash = Integer.rotateLeft(hash, r2) * m + n;

        hash ^= Integer.rotateLeft(z * c1, r1) * c2;
        hash = Integer.rotateLeft(hash, r2) * m + n;

        return hash;
    }

    public static int murmurHashFinalize(int hash) {
        hash ^= hash >>> 16;
        hash *= 0x85ebca6b;
        hash ^= hash >>> 13;
        hash *= 0xc2b2ae35;
        hash ^= hash >>> 16;
        return hash;
    }

    // simplified for this use case code from https://github.com/OpenHFT/Zero-Allocation-Hashing/blob/efe54e3890bf021bb33a36f80c006c52d8f866fe/src/main/java/net/openhft/hashing/XxHash.java

    // Primes if treated as unsigned
    private static final long P1 = -7046029288634856825L;
    private static final long P2 = -4417276706812531889L;
    private static final long P3 = 1609587929392839161L;
    private static final long P4 = -8796714831421723037L;
    private static final long P5 = 2870177450012600261L;

    public static long xxHash64(long seed, int a, int b, int c, int d) {
        // changed: unrolled loops, inlined length constant, eliminated dead code for case of 16 byte length
        long hash;
        hash = seed + P5;
        hash += 16;

        long k1 = (a & 0xFFFFFFFFL) | (((long) b) << 32);
        k1 *= P2;
        k1 = Long.rotateLeft(k1, 31);
        k1 *= P1;
        hash ^= k1;
        hash = Long.rotateLeft(hash, 27) * P1 + P4;

        k1 = (c & 0xFFFFFFFFL) | (((long) d) << 32);
        k1 *= P2;
        k1 = Long.rotateLeft(k1, 31);
        k1 *= P1;
        hash ^= k1;
        hash = Long.rotateLeft(hash, 27) * P1 + P4;

        return finalize(hash);
    }

    private static long finalize(long hash) {
        hash ^= hash >>> 33;
        hash *= P2;
        hash ^= hash >>> 29;
        hash *= P3;
        hash ^= hash >>> 32;
        return hash;
    }
}
