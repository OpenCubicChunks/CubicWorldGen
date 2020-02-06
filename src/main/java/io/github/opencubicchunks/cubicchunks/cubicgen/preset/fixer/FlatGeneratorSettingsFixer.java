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
package io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer;

import java.util.Map.Entry;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.base.Optional;

import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.FlatGeneratorSettings;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

public class FlatGeneratorSettingsFixer {

    private static boolean isVersionUpToDate(JsonObject root) {
        return root.containsKey("version") && root.get(int.class, "version") >= 1;
    }

    public static boolean isUpToDate(String json) {
        return isVersionUpToDate(stringToJson(json));
    }

    public static JsonObject stringToJson(String jsonString) {
        if (jsonString.isEmpty()) {
            // avoid JsonNull
            jsonString = "{}";
        }
        try {
            return CustomGenSettingsSerialization.jankson().load(jsonString);
        } catch (SyntaxError err) {
            String message = err.getMessage() + "\n" + err.getLineMessage();
            throw new RuntimeException(message, err);
        }
    }

    public static String fixGeneratorOptions(String json) {
        Jankson gson = FlatGeneratorSettings.jankson();
        JsonObject oldRoot = stringToJson(json);
        JsonObject newRoot = stringToJson("{}");
        newRoot.put("version", new JsonPrimitive(1));
        try {
            newRoot.put("layers", getLayers(oldRoot));
        } catch (SyntaxError err) {
            String message = err.getMessage() + "\n" + err.getLineMessage();
            throw new RuntimeException(message, err);
        }
        return newRoot.toJson();
    }
    
    @SuppressWarnings("unchecked")
    private static JsonElement getLayers(JsonObject json) throws SyntaxError {
        Jankson jankson = CustomGenSettingsSerialization.jankson();
        JsonArray layers = new JsonArray();
        if (json.containsKey("layers")) {
            JsonObject oldLayersMap = (JsonObject) json.get("layers");
            JsonObject newLayersMap = new JsonObject();
            for (Entry<String, JsonElement> entry : oldLayersMap.entrySet()) {
                String key = entry.getKey();
                JsonObject oldLayer = (JsonObject) entry.getValue();
                JsonObject newLayer = new JsonObject();
                newLayer.put("fromY", oldLayer.get("fromY"));
                newLayer.put("toY", oldLayer.get("toY"));
                if (oldLayer.containsKey("biome")) {
                    newLayer.put("biome", oldLayer.get("biome"));
                } else {
                    newLayer.put("biome", new JsonPrimitive(-1));
                }
                if (oldLayer.containsKey("blockState")) {
                    newLayer.put("blockState", oldLayer.get("blockState"));
                } else {
                    Block block = Block.getBlockFromName(oldLayer.get(String.class, "blockRegistryName"));
                    IBlockState blockState = block.getBlockState().getBaseState();
                    for (@SuppressWarnings("rawtypes")
                    IProperty property : block.getBlockState().getProperties()) {
                        if (oldLayer.containsKey(property.getName())) {
                            blockState = blockState.withProperty(property,
                                    findPropertyValueByName(property, oldLayer.get(String.class, property.getName())));
                        }
                    }
                    NBTTagCompound tag = new NBTTagCompound();
                    NBTUtil.writeBlockState(tag, blockState);
                    String tagString = tag.toString();
                    newLayer.put("blockState", jankson.load(tagString));
                }
                newLayersMap.put(key, newLayer);
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
