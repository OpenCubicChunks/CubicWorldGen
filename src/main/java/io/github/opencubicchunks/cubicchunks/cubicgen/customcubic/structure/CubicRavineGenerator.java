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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure;

import static io.github.opencubicchunks.cubicchunks.api.util.Coords.cubeToMinBlock;
import static io.github.opencubicchunks.cubicchunks.api.util.Coords.localToBlock;
import static net.minecraft.util.math.MathHelper.cos;
import static net.minecraft.util.math.MathHelper.floor;
import static net.minecraft.util.math.MathHelper.sin;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.cubicgen.StructureGenUtil;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CubicRavineGenerator extends CaveCommonBaseStructureGenerator {

    /**
     * Vanilla value: 50
     * <p>
     * Multiply by 16 and divide by 8: 16 cubes in vanilla chunks, only one in 8 cubes has structures generated
     */
    private static final int RAVINE_RARITY = 50 * 16 / (2 * 2 * 2);

    /**
     * Add this value to lava height (Y below which lava exists)
     * <p>
     * Positive value to increase amount of lava, negative to decrease.
     */
    private static final double LAVA_HEIGHT_OFFSET = -10;

    /**
     * Add Y value multiplied by this to lava height
     * <p>
     * Negative value will generate more lava in ravines that are deeper
     */
    private static final double LAVA_HEIGHT_Y_FACTOR = -0.1;

    private static final double VERT_SIZE_FACTOR = 3.0;

    /**
     * Value added to the size of the cave (radius)
     */
    private static final double RAVINE_SIZE_ADD = 1.5D;

    private static final double MIN_RAND_SIZE_FACTOR = 0.75;
    private static final double MAX_RAND_SIZE_FACTOR = 1.00;

    /**
     * After each step the Y direction component will be multiplied by this value
     */
    private static final float FLATTEN_FACTOR = 0.7f;

    /**
     * Each step ravine direction angles will be changed by this fraction of values that specify how direction changes
     */
    private static final float DIRECTION_CHANGE_FACTOR = 0.05f;

    /**
     * This fraction of the previous value that controls horizontal direction changes will be used in next step
     */
    private static final float PREV_HORIZ_DIRECTION_CHANGE_WEIGHT = 0.5f;

    /**
     * This fraction of the previous value that controls vertical direction changes will be used in next step
     */
    private static final float PREV_VERT_DIRECTION_CHANGE_WEIGHT = 0.8f;

    /**
     * Maximum value by which horizontal cave direction randomly changes each step, lower values are much more likely.
     */
    private static final float MAX_ADD_DIRECTION_CHANGE_HORIZ = 4.0f;

    /**
     * Maximum value by which vertical cave direction randomly changes each step, lower values are much more likely.
     */
    private static final float MAX_ADD_DIRECTION_CHANGE_VERT = 2.0f;

    /**
     * 1 in this amount of steps will actually carve any blocks,
     */
    private static final int CARVE_STEP_RARITY = 4;

    /**
     * Higher values will make width difference between top/bottom and center smaller
     * lower values will make top and bottom of the ravine smaller. Values less than one will shrink size of the ravine
     */
    private static final double STRETCH_Y_FACTOR = 6.0;

    /**
     * Controls which blocks can be replaced by cave
     */
    @Nonnull private static final Predicate<IBlockState> isBlockReplaceable = (state ->
            state.getBlock() == Blocks.STONE || state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.GRASS);

    private final int maxCubeY;

    /**
     * Contains values of ravine widths at each height.
     * <p>
     * For cubic chunks the height value used wraps around.
     */
    @Nonnull private float[] widthDecreaseFactors = new float[1024];

    public CubicRavineGenerator(CustomGeneratorSettings cfg) {
        this.maxCubeY = Coords.blockToCube(cfg.expectedBaseHeight);
    }

    protected void generate(World world, Random rand, CubePrimer cube, int structureX, int structureY, int structureZ,
            CubePos generatedCubePos) {
        if (rand.nextInt(RAVINE_RARITY) != 0 || structureY > maxCubeY) {
            return;
        }
        double startX = localToBlock(structureX, rand.nextInt(ICube.SIZE));
        double startY = localToBlock(structureY, rand.nextInt(ICube.SIZE));
        double startZ = localToBlock(structureZ, rand.nextInt(ICube.SIZE));

        float vertDirectionAngle = rand.nextFloat() * (float) Math.PI * 2.0F;
        float horizDirectionAngle = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
        float baseRavineSize = (rand.nextFloat() * 2.0F + rand.nextFloat()) * 2.0F;

        int startWalkedDistance = 0;
        int maxWalkedDistance = 0;//choose value automatically

        int lavaHeight = (int) (startY -
                (baseRavineSize + RAVINE_SIZE_ADD) * VERT_SIZE_FACTOR +
                LAVA_HEIGHT_OFFSET + startY * LAVA_HEIGHT_Y_FACTOR);

        this.generateNode(cube, rand.nextLong(), generatedCubePos, startX, startY, startZ,
                baseRavineSize, vertDirectionAngle, horizDirectionAngle,
                startWalkedDistance, maxWalkedDistance, VERT_SIZE_FACTOR, lavaHeight);
    }

    private void generateNode(CubePrimer cube, long seed, CubePos generatedCubePos,
            double ravineX, double ravineY, double ravineZ,
            float baseRavineSize, float horizDirAngle, float vertDirAngle,
            int startWalkedDistance, int maxWalkedDistance, double vertRavineSizeMod,
            int lavaHeight) {
        Random rand = new Random(seed);

        //store by how much the horizontal and vertical(?) direction angles will change each step
        float horizDirChange = 0.0F;
        float vertDirChange = 0.0F;

        if (maxWalkedDistance <= 0) {
            int maxBlockRadius = cubeToMinBlock(RANGE - 1);
            maxWalkedDistance = maxBlockRadius - rand.nextInt(maxBlockRadius / 4);
        }

        //always false for ravine generator
        boolean finalStep = false;

        int walkedDistance;
        if (startWalkedDistance == -1) {
            //UNUSED: generate a ravine equivalent of cave room
            //start at half distance towards the end = max size
            walkedDistance = maxWalkedDistance / 2;
            finalStep = true;
        } else {
            walkedDistance = startWalkedDistance;
        }

        this.widthDecreaseFactors = generateRavineWidthFactors(rand);

        for (; walkedDistance < maxWalkedDistance; ++walkedDistance) {
            float fractionWalked = walkedDistance / (float) maxWalkedDistance;
            //horizontal and vertical size of the ravine
            //size starts small and increases, then decreases as ravine goes further
            double ravineSizeHoriz = RAVINE_SIZE_ADD + sin(fractionWalked * (float) Math.PI) * baseRavineSize;
            double ravineSizeVert = ravineSizeHoriz * vertRavineSizeMod;
            ravineSizeHoriz *= rand.nextFloat() * (MAX_RAND_SIZE_FACTOR - MIN_RAND_SIZE_FACTOR) + MIN_RAND_SIZE_FACTOR;
            ravineSizeVert *= rand.nextFloat() * (MAX_RAND_SIZE_FACTOR - MIN_RAND_SIZE_FACTOR) + MIN_RAND_SIZE_FACTOR;

            //Walk forward a single step:

            //from sin(alpha)=y/r and cos(alpha)=x/r ==> x = r*cos(alpha) and y = r*sin(alpha)
            //always moves by one block in some direction

            //here x is xzDirectionSize, y is yDirection
            float xzDirectionFactor = cos(vertDirAngle);
            float yDirectionFactor = sin(vertDirAngle);

            ravineX += cos(horizDirAngle) * xzDirectionFactor;
            ravineY += yDirectionFactor;
            ravineZ += sin(horizDirAngle) * xzDirectionFactor;

            vertDirAngle *= FLATTEN_FACTOR;

            //change the direction
            vertDirAngle += vertDirChange * DIRECTION_CHANGE_FACTOR;
            horizDirAngle += horizDirChange * DIRECTION_CHANGE_FACTOR;
            //update direction change angles
            vertDirChange *= PREV_VERT_DIRECTION_CHANGE_WEIGHT;
            horizDirChange *= PREV_HORIZ_DIRECTION_CHANGE_WEIGHT;
            vertDirChange += (rand.nextFloat() - rand.nextFloat()) * rand.nextFloat() * MAX_ADD_DIRECTION_CHANGE_VERT;
            horizDirChange += (rand.nextFloat() - rand.nextFloat()) * rand.nextFloat() * MAX_ADD_DIRECTION_CHANGE_HORIZ;

            if (rand.nextInt(CARVE_STEP_RARITY) == 0 && !finalStep) {
                continue;
            }

            double xDist = ravineX - generatedCubePos.getXCenter();
            double zDist = ravineZ - generatedCubePos.getZCenter();
            double maxStepsDist = maxWalkedDistance - walkedDistance;

            double maxDistToCube = baseRavineSize + RAVINE_SIZE_ADD + ICube.SIZE;
            //can this cube be reached at all?
            //if even after going max distance allowed by remaining steps, it's still too far - stop
            //NOTE: don't check yDist, this is optimization and with Y scale stretched as much as with ravines
            //the check would be useless
            //TODO: does it make any performance difference?
            if (xDist * xDist + zDist * zDist - maxStepsDist * maxStepsDist > maxDistToCube * maxDistToCube) {
                return;
            }

            tryCarveBlocks(cube, generatedCubePos,
                    ravineX, ravineY, ravineZ,
                    ravineSizeHoriz, ravineSizeVert, lavaHeight);

            if (finalStep) {
                return;
            }
        }
    }

    private void tryCarveBlocks(CubePrimer cube, CubePos generatedCubePos,
            double ravineX, double ravineY, double ravineZ,
            double ravineSizeHoriz, double ravineSizeVert, int lavaHeight) {
        double genCubeCenterX = generatedCubePos.getXCenter();
        double genCubeCenterY = generatedCubePos.getYCenter();
        double genCubeCenterZ = generatedCubePos.getZCenter();
        if (ravineX < genCubeCenterX - ICube.SIZE - ravineSizeHoriz * 2.0D ||
                ravineY < genCubeCenterY - ICube.SIZE - ravineSizeVert * 2.0D ||
                ravineZ < genCubeCenterZ - ICube.SIZE - ravineSizeHoriz * 2.0D ||
                ravineX > genCubeCenterX + ICube.SIZE + ravineSizeHoriz * 2.0D ||
                ravineY > genCubeCenterY + ICube.SIZE + ravineSizeVert * 2.0D ||
                ravineZ > genCubeCenterZ + ICube.SIZE + ravineSizeHoriz * 2.0D) {
            return;
        }
        int minLocalX = floor(ravineX - ravineSizeHoriz) - generatedCubePos.getMinBlockX() - 1;
        int maxLocalX = floor(ravineX + ravineSizeHoriz) - generatedCubePos.getMinBlockX() + 1;
        int minLocalY = floor(ravineY - ravineSizeVert) - generatedCubePos.getMinBlockY() - 1;
        int maxLocalY = floor(ravineY + ravineSizeVert) - generatedCubePos.getMinBlockY() + 1;
        int minLocalZ = floor(ravineZ - ravineSizeHoriz) - generatedCubePos.getMinBlockZ() - 1;
        int maxLocalZ = floor(ravineZ + ravineSizeHoriz) - generatedCubePos.getMinBlockZ() + 1;

        //skip is if everything is outside of that cube
        if (maxLocalX <= 0 || minLocalX >= ICube.SIZE ||
                maxLocalY <= 0 || minLocalY >= ICube.SIZE ||
                maxLocalZ <= 0 || minLocalZ >= ICube.SIZE) {
            return;
        }
        StructureBoundingBox boundingBox = new StructureBoundingBox(minLocalX, minLocalY, minLocalZ, maxLocalX, maxLocalY, maxLocalZ);

        StructureGenUtil.clampBoundingBoxToLocalCube(boundingBox);

        boolean hitLiquid = StructureGenUtil.scanWallsForBlock(cube, boundingBox,
                (b) -> b.getBlock() == Blocks.WATER || b.getBlock() == Blocks.FLOWING_WATER);

        if (!hitLiquid) {
            carveBlocks(cube, generatedCubePos, ravineX, ravineY, ravineZ,
                    ravineSizeHoriz, ravineSizeVert, boundingBox, lavaHeight);
        }
    }

    private void carveBlocks(CubePrimer cube, CubePos generatedCubePos,
            double ravineX, double ravineY, double ravineZ,
            double ravineSizeHoriz, double ravineSizeVert, StructureBoundingBox boundingBox,
            int lavaHeight) {
        int generatedCubeX = generatedCubePos.getX();
        int generatedCubeY = generatedCubePos.getY();
        int generatedCubeZ = generatedCubePos.getZ();

        int minX = boundingBox.minX;
        int maxX = boundingBox.maxX;
        int minY = boundingBox.minY;
        int maxY = boundingBox.maxY;
        int minZ = boundingBox.minZ;
        int maxZ = boundingBox.maxZ;

        for (int localX = minX; localX < maxX; ++localX) {
            double distX = StructureGenUtil.normalizedDistance(generatedCubeX, localX, ravineX, ravineSizeHoriz);

            for (int localZ = minZ; localZ < maxZ; ++localZ) {
                double distZ = StructureGenUtil.normalizedDistance(generatedCubeZ, localZ, ravineZ, ravineSizeHoriz);

                if (distX * distX + distZ * distZ >= 1.0D) {
                    continue;
                }
                for (int localY = minY; localY < maxY; ++localY) {
                    double distY = StructureGenUtil.normalizedDistance(generatedCubeY, localY, ravineY, ravineSizeVert);

                    //distY*distY/STRETCH_Y_FACTOR is a hack
                    //it should make the ravine way more stretched in the Y dimension, but because of previous checks
                    //most of these blocks beyond the not-stretched height range are never carved out
                    //the result is that instead the ravine isn't very small at the bottom,
                    //but ends with actual floor instead
                    double widthDecreaseFactor = this.widthDecreaseFactors[(localY + generatedCubeY * ICube.SIZE) & 0xFF];
                    if ((distX * distX + distZ * distZ) * widthDecreaseFactor + distY * distY / STRETCH_Y_FACTOR >= 1.0D) {
                        continue;
                    }

                    if (!isBlockReplaceable.test(cube.getBlockState(localX, localY, localZ))) {
                        continue;
                    }
                    if (localToBlock(generatedCubeY, localY) < lavaHeight) {
                        cube.setBlockState(localX, localY, localZ, Blocks.FLOWING_LAVA.getDefaultState());
                    } else {
                        cube.setBlockState(localX, localY, localZ, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    private float[] generateRavineWidthFactors(Random rand) {
        float[] values = new float[1024];
        float value = 1.0F;

        for (int i = 0; i < ICube.SIZE*ICube.SIZE; ++i) {
            //~33% probability that the value will change at that height
            if (i == 0 || rand.nextInt(3) == 0) {
                //value = 1.xxx, lower = higher probability -> Wider parts are more common.
                value = 1.0F + rand.nextFloat() * rand.nextFloat();
            }

            values[i] = value * value;
        }

        return values;
    }
}
