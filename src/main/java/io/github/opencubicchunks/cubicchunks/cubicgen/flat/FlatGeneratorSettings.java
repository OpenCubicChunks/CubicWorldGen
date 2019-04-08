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
package io.github.opencubicchunks.cubicchunks.cubicgen.flat;

import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.BlockStateSerializer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class FlatGeneratorSettings {

    public TreeMap<Integer, Layer> layers = new TreeMap<Integer, Layer>();
    public int version = 1;

    public FlatGeneratorSettings() {
        addLayer(Integer.MIN_VALUE + 1, Blocks.BEDROCK.getDefaultState());
        addLayer(-8, Blocks.STONE.getDefaultState());
        addLayer(-1, Blocks.DIRT.getDefaultState());
        addLayer(0, Blocks.GRASS.getDefaultState());
    }

    public void addLayer(int toY, IBlockState block) {
        int fromY = Integer.MIN_VALUE + 1;
        if (layers.floorEntry(toY) != null) {
            fromY = layers.floorEntry(toY).getValue().toY;
        }
        layers.put(fromY, new Layer(fromY, toY, block));
    }

    public String toJson() {
        Gson gson = gson();
        return gson.toJson(this);
    }

    public static FlatGeneratorSettings fromJson(String json) {
        if (json.isEmpty()) {
            return defaults();
        }
        boolean isOutdated = !FlatGeneratorSettingsFixer.isUpToDate(json);
        if (isOutdated) {
            json = FlatGeneratorSettingsFixer.fixGeneratorOptions(json);
        }
        Gson gson = gson();
        return gson.fromJson(json, FlatGeneratorSettings.class);
    }

    public static Gson gson() {
        return new GsonBuilder().registerTypeHierarchyAdapter(IBlockState.class, BlockStateSerializer.INSTANCE).create();
    }

    public static FlatGeneratorSettings defaults() {
        return new FlatGeneratorSettings();
    }
}
