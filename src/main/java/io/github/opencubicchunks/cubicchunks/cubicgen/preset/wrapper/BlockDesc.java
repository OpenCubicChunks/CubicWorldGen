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
