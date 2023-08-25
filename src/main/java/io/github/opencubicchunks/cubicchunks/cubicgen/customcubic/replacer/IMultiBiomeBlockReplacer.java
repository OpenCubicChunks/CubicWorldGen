package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.replacer;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class IMultiBiomeBlockReplacer {
    /**
     * Replaces the given block with another block based on given location, density gradient and density value. Biome
     * block replacers can be chained (output if one replacer used as input to another replacer)
     * <p>
     * The common interpretation of density value: If it's greater than 0, there is block at that position. Density is
     * scaled in such way that it approximately represents how many blocks below the surface this position is.
     * <p>
     * Gradient values approximate how the value will change after going 1 block in x/y/z direction.
     *
     * @param previousBlock the block that was there before using this replacer
     * @param biome         the biome at current location
     * @param x             the block X coordinate
     * @param y             the block Y coordinate
     * @param z             the block Z coordinate
     * @param dx            the X component of density gradient
     * @param dy            the Y component of density gradient
     * @param dz            the Z component of density gradient
     * @param density       the density value
     * @param replacerFlags the flags indicating which block replacers should be used
     * @see IBiomeBlockReplacer#getReplacedBlock(IBlockState, Biome, int, int, int, double, double, double, double)
     */
    public abstract IBlockState getReplacedBlock(IBlockState previousBlock, Biome biome,
            int x, int y, int z, double dx, double dy, double dz, double density,
            long[] replacerFlags);
}
