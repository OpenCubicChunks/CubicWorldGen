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
package io.github.opencubicchunks.cubicchunks.cubicgen.preset;

import java.util.Map.Entry;
import java.util.TreeMap;

import blue.endless.jankson.Jankson;

import blue.endless.jankson.api.DeserializationException;
import blue.endless.jankson.api.SyntaxError;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.FlatGeneratorSettingsFixer;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class FlatGeneratorSettings {

    public TreeMap<Integer, FlatLayer> layers = new TreeMap<Integer, FlatLayer>();
    public int version = 1;

    public void addLayer(int toY, IBlockState block) {
        int fromY = Integer.MIN_VALUE + 1;
        if (layers.floorEntry(toY) != null) {
            fromY = layers.floorEntry(toY).getValue().toY;
        }
        layers.put(fromY, new FlatLayer(fromY, toY, new BlockStateDesc(block)));
    }

    public String toJson() {
        Jankson gson = jankson();
        return gson.toJson(this).toJson();
    }

    public static FlatGeneratorSettings fromJson(String json) {
        if (json.isEmpty()) {
            return defaults();
        }
        boolean isOutdated = !FlatGeneratorSettingsFixer.isUpToDate(json);
        if (isOutdated) {
            json = FlatGeneratorSettingsFixer.fixGeneratorOptions(json);
        }
        Jankson gson = jankson();
        try {
            return gson.fromJsonCarefully(json, FlatGeneratorSettings.class);
        } catch (SyntaxError syntaxError) {
            String message = syntaxError.getMessage() + "\n" + syntaxError.getLineMessage();
            throw new RuntimeException(message, syntaxError);
        } catch (DeserializationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Jankson jankson() {
        return CustomGenSettingsSerialization.jankson();
    }

    public static FlatGeneratorSettings defaults() {
        FlatGeneratorSettings s = new FlatGeneratorSettings();
        s.addLayer(Integer.MIN_VALUE + 1, Blocks.BEDROCK.getDefaultState());
        s.addLayer(-8, Blocks.STONE.getDefaultState());
        s.addLayer(-1, Blocks.DIRT.getDefaultState());
        s.addLayer(0, Blocks.GRASS.getDefaultState());
        return s;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FlatGeneratorSettings[");
        sb.append("version:");
        sb.append(version);
        for(Entry<Integer, FlatLayer> layer:layers.entrySet()) {
            sb.append(",");
            sb.append(layer.toString());
        }
        sb.append("]");
        return sb.toString();
    }
}
