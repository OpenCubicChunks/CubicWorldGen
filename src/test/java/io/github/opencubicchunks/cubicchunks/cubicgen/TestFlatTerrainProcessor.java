/*
 *  This file is part of Cubic Chunks Mod, licensed under the MIT License (MIT).
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
package io.github.opencubicchunks.cubicchunks.cubicgen;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.annotation.ParametersAreNonnullByDefault;

import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import net.minecraft.world.World;
import org.junit.Before;
import org.junit.Test;

import io.github.opencubicchunks.cubicchunks.testutil.MinecraftEnvironment;
import io.github.opencubicchunks.cubicchunks.cubicgen.flat.FlatGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.flat.FlatTerrainProcessor;
import io.github.opencubicchunks.cubicchunks.cubicgen.flat.Layer;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.storage.WorldInfo;
import org.mockito.Mockito;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TestFlatTerrainProcessor {

    static IBlockState nw;
    @Before
    public void setUp() throws IOException {
        MinecraftEnvironment.init();
        nw = Blocks.NETHER_WART_BLOCK.getDefaultState();
    }

    @Test
    public void testFlatTerrainProcessor() {
        int checkFromY = -1;
        int checkToY = 1;
        FlatGeneratorSettings fgs = new FlatGeneratorSettings();
        World world = Mockito.mock(World.class, Mockito.withSettings().extraInterfaces(ICubicWorld.class));
        WorldInfo worldInfo = Mockito.mock(WorldInfo.class);
        when(world.getWorldInfo()).thenReturn(worldInfo);
        
        // Default settings
        when(worldInfo.getGeneratorOptions()).thenReturn(fgs.toJson());
        FlatTerrainProcessor ftp = new FlatTerrainProcessor(world);
        for (int i = checkFromY; i <= checkToY; i++)
            ftp.generateCube(0, i, 0);
        
        // No layers at all
        fgs.layers.clear();
        when(worldInfo.getGeneratorOptions()).thenReturn(fgs.toJson());
        ftp = new FlatTerrainProcessor(world);
        CubePrimer primer = null;
        for (int i = checkFromY; i <= checkToY; i++) {
            primer = ftp.generateCube(0, i, 0);
            assertEquals(CubePrimer.DEFAULT_STATE, primer.getBlockState(8, 8, 8));
        }
        
        // Single layer in a middle of every cube
        for (int i = checkFromY; i <= checkToY; i++)
            fgs.layers.put(8 + Coords.cubeToMinBlock(i), new Layer(8 + Coords.cubeToMinBlock(i), 9 + Coords.cubeToMinBlock(i), nw));
        when(worldInfo.getGeneratorOptions()).thenReturn(fgs.toJson());
        ftp = new FlatTerrainProcessor(world);
        for (int i = checkFromY; i <= checkToY; i++) {
            primer = ftp.generateCube(0, i, 0);
            assertEquals(nw,primer.getBlockState(8, 8, 8));
        }
        
        // Two layers with a gap in-between
        for (int i = checkFromY; i <= checkToY; i++)
            fgs.layers.put(12 + Coords.cubeToMinBlock(i), new Layer(12 + Coords.cubeToMinBlock(i), 13 + Coords.cubeToMinBlock(i), nw));
        when(worldInfo.getGeneratorOptions()).thenReturn(fgs.toJson());
        ftp = new FlatTerrainProcessor(world);
        for (int i = checkFromY; i <= checkToY; i++) {
            primer = ftp.generateCube(0, i, 0);
            assertEquals(CubePrimer.DEFAULT_STATE, primer.getBlockState(8, 11, 8));
        }
    }
}
