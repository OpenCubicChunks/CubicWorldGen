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

import java.io.StringReader;
import java.util.Map.Entry;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

public class FlatGeneratorSettingsFixer {

    private static boolean isVersionUpToDate(JsonObject root) {
        return root.has("version") && root.get("version").getAsInt() >= 1;
    }

    public static boolean isUpToDate(String json) {
        return isVersionUpToDate(stringToJson(json));
    }

    public static JsonObject stringToJson(String jsonString) {
        if (jsonString.isEmpty()) {
            // avoid JsonNull
            jsonString = "{}";
        }
        JsonReader reader = new JsonReader(new StringReader(jsonString));
        return new JsonParser().parse(reader).getAsJsonObject();
    }

    public static String fixGeneratorOptions(String json) {
        Gson gson = FlatGeneratorSettings.gson();
        JsonObject oldRoot = stringToJson(json);
        JsonObject newRoot = stringToJson("{}");
        newRoot.add("version", new JsonPrimitive(1));
        newRoot.add("layers", getLayers(oldRoot));
        return gson.toJson(newRoot);
    }
    
    @SuppressWarnings("unchecked")
    private static JsonElement getLayers(JsonObject json) {
        JsonArray layers = new JsonArray();
        if (json.has("layers")) {
            JsonObject oldLayersMap = json.get("layers").getAsJsonObject();
            JsonObject newLayersMap = new JsonObject();
            for (Entry<String, JsonElement> entry : oldLayersMap.entrySet()) {
                String key = entry.getKey();
                JsonObject oldLayer = entry.getValue().getAsJsonObject();
                JsonObject newLayer = new JsonObject();
                newLayer.add("fromY", oldLayer.get("fromY"));
                newLayer.add("toY", oldLayer.get("toY"));
                if (oldLayer.has("biome")) {
                    newLayer.add("biome", oldLayer.get("biome"));
                } else {
                    newLayer.add("biome", new JsonPrimitive(-1));
                }
                if (oldLayer.has("blockState")) {
                    newLayer.add("blockState", oldLayer.get("blockState"));
                } else {
                    Block block = Block.getBlockFromName(oldLayer.get("blockRegistryName").getAsString());
                    IBlockState blockState = block.getBlockState().getBaseState();
                    for (@SuppressWarnings("rawtypes")
                    IProperty property : block.getBlockState().getProperties()) {
                        if (oldLayer.has(property.getName())) {
                            blockState = blockState.withProperty(property,
                                    findPropertyValueByName(property, oldLayer.get(property.getName()).getAsString()));
                        }
                    }
                    NBTTagCompound tag = new NBTTagCompound();
                    NBTUtil.writeBlockState(tag, blockState);
                    String tagString = tag.toString();
                    newLayer.add("blockState", new JsonParser().parse(tagString));
                }
                newLayersMap.add(key, newLayer);
            }
        }
        return layers;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Comparable findPropertyValueByName(IProperty property, String valueIn) {
        Optional<Comparable> value = property.parseValue(valueIn);
        if (value.isPresent()) {
            return value.get();
        } else {
            for (Object v : property.getAllowedValues()) {
                if (isValueEqualTo(property, (Comparable) v, valueIn)) {
                    return (Comparable) v;
                }
            }
        }
        return null;
    }
    
    @SuppressWarnings("rawtypes")
    private static boolean isValueEqualTo(IProperty property, Comparable value, String valueIn) {
        return getValueName(property, value).equals(valueIn);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String getValueName(IProperty property, Comparable v) {
        return property.getName(v);
    }
}
