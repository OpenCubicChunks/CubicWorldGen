package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.ICubicStructureGenerator;
import net.minecraft.world.World;

import java.util.Random;

public abstract class CaveCommonBaseStructureGenerator implements ICubicStructureGenerator {

    public static final int RANGE = 8;

    @Override
    public void generate(World world, CubePrimer cube, CubePos cubePos) {
        this.generate(world, cube, cubePos, this::generate, RANGE, RANGE, 1, 1);
    }

    protected abstract void generate(World world, Random rand, CubePrimer cube, int structureX, int structureY, int structureZ,
                            CubePos generatedCubePos);

}
