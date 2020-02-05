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

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;
import java.util.Objects;

public class BlockDesc {

    private final String blockId;
    private final Block block;

    public BlockDesc(String blockId) {
        this.blockId = blockId;
        ResourceLocation id = new ResourceLocation(blockId);
        if (!ForgeRegistries.BLOCKS.containsKey(id)) {
            this.block = null;
            return;
        }
        this.block = ForgeRegistries.BLOCKS.getValue(id);
    }

    public BlockDesc(Block block) {
        this.block = block;
        this.blockId = ForgeRegistries.BLOCKS.getKey(block).toString();
    }

    public String getBlockId() {
        return blockId;
    }

    public Block getBlock() {
        return block;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockDesc blockDesc = (BlockDesc) o;
        return blockId.equals(blockDesc.blockId);
    }

    @Override public int hashCode() {
        return Objects.hash(blockId);
    }

    @Override public String toString() {
        return "BlockDesc{" +
                "blockId='" + blockId + '\'' +
                '}';
    }

    public BlockStateDesc defaultState() {
        return new BlockStateDesc(blockId, new HashMap<>());
    }
}
