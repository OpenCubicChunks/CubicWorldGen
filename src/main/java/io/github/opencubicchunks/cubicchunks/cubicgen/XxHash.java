/*
 * Copyright 2015 Higher Frequency Trading http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opencubicchunks.cubicchunks.cubicgen;

// simplified for this use case code from https://github.com/OpenHFT/Zero-Allocation-Hashing/blob/efe54e3890bf021bb33a36f80c006c52d8f866fe/src/main/java/net/openhft/hashing/XxHash.java
public class XxHash {
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
