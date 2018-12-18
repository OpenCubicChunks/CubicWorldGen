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
package io.github.opencubicchunks.cubicchunks.cubicgen;

import static org.mockito.Mockito.when;

import javax.annotation.ParametersAreNonnullByDefault;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomTerrainGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.testutil.MinecraftEnvironment;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TestCustomTerrainGenerator {

    @Before
    public void setUp() {
        MinecraftEnvironment.init();
    }

    @Test
    public void testCustomTerrainGenerator() {
        int checkFromY = -1;
        int checkToY = 1;
        CustomGeneratorSettings cgs = new CustomGeneratorSettings();
        World world = Mockito.mock(World.class, Mockito.withSettings().extraInterfaces(ICubicWorld.class));
        WorldInfo worldInfo = Mockito.mock(WorldInfo.class);
        when(world.getWorldInfo()).thenReturn(worldInfo);
        
        when(worldInfo.getGeneratorOptions()).thenReturn(cgs.toJson(true));
        CustomTerrainGenerator ctp = new CustomTerrainGenerator(world, 0);
        for (int i = checkFromY; i <= checkToY; i++)
            ctp.generateCube(0, i, 0);
    }
}
