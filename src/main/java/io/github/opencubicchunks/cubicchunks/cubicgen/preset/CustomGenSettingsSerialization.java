package io.github.opencubicchunks.cubicchunks.cubicgen.preset;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.DeserializationException;
import blue.endless.jankson.api.Marshaller;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.BiomeBlockReplacerConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.CubeAreas;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.IntAABB;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.PeriodicGaussianOreConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.PeriodicOreList;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.StandardOreConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.StandardOreList;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.UserFunction;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BiomeDesc;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockDesc;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.minecraft.util.ResourceLocation;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;

public class CustomGenSettingsSerialization {

    public static final JsonGrammar OUT_GRAMMAR = JsonGrammar.builder()
            .withComments(true)
            .bareSpecialNumerics(true)
            .printWhitespace(true)
            .printUnquotedKeys(false) // TODO set to true once it's fixed in jankson https://github.com/falkreon/Jankson/issues/26
            .printCommas(true)
            .printTrailingCommas(true)
            .bareRootObject(false)
            .build();

    public static final Marshaller MARSHALLER = jankson().getMarshaller();

    private static BiomeBlockReplacerConfig deserializeReplacerConfig(JsonObject obj, Marshaller marshaller) {
        JsonObject defaults = (JsonObject) obj.get("defaults");
        JsonObject overrides = (JsonObject) obj.get("overrides");

        BiomeBlockReplacerConfig conf = BiomeBlockReplacerConfig.defaults();
        if (defaults != null) {
            for (Map.Entry<String, JsonElement> e : defaults.entrySet()) {
                ResourceLocation key = new ResourceLocation(e.getKey());
                Object value = getObject(e);
                conf.setDefault(key, value);
            }
        }
        if (overrides != null) {
            for (Map.Entry<String, JsonElement> e : overrides.entrySet()) {
                ResourceLocation key = new ResourceLocation(e.getKey());
                Object value = getObject(e);
                conf.set(key, value);
            }
        }
        return conf;
    }

    private static JsonObject serializeReplacerConfig(BiomeBlockReplacerConfig src, Marshaller marshaller) {
        JsonObject root = new JsonObject();
        root.setMarshaller(marshaller);

        JsonObject defaults = new JsonObject();
        defaults.setMarshaller(marshaller);
        JsonObject overrides = new JsonObject();
        defaults.setMarshaller(marshaller);

        for (Map.Entry<ResourceLocation, Object> e : src.getDefaults().entrySet()) {
            defaults.put(e.getKey().toString(), getJsonElement(marshaller, e));
        }
        for (Map.Entry<ResourceLocation, Object> e : src.getOverrides().entrySet()) {
            overrides.put(e.getKey().toString(), getJsonElement(marshaller, e));
        }
        root.put("defaults", defaults);
        root.put("overrides", overrides);
        return root;
    }

    private static Object getObject(Map.Entry<String, JsonElement> e) {
        Object value;
        if (e.getValue() instanceof JsonPrimitive) {
            value = ((JsonPrimitive) e.getValue()).asDouble(0.0);
        } else {
            // currently the only object suppoorted is blockstate
            value = ((JsonObject) e.getValue()).getMarshaller().marshall(BlockStateDesc.class, e.getValue());
        }
        return value;
    }

    private static JsonElement getJsonElement(Marshaller marshaller, Map.Entry<ResourceLocation, Object> e) {
        JsonElement v;
        if (e.getValue() == null) {
            return JsonNull.INSTANCE;
        }
        if (e.getValue() instanceof Number) {
            v = new JsonPrimitive(e.getValue());
        } else if (e.getValue() instanceof BlockStateDesc) {
            v = marshaller.serialize(e.getValue());
        } else {
            throw new UnsupportedOperationException(e.getValue() + " of type " + e.getValue().getClass() + " is not supported");
        }
        return v;
    }

    private static BiomeDesc deserializeBiome(String obj, Marshaller marshaller) {
        return new BiomeDesc(obj);
    }

    private static JsonElement serializeBiome(BiomeDesc biome, Marshaller marshaller) {
        return new JsonPrimitive(biome.getBiomeId());
    }

    public static UserFunction deserializeUserFunction(JsonArray arr, @Nullable Marshaller marshaller) {
        UserFunction.Entry[] entries = new UserFunction.Entry[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            JsonObject e = (JsonObject) arr.get(i);
            entries[i] = new UserFunction.Entry(e.get(float.class, "y"), e.get(float.class, "v"));
        }
        return new UserFunction(entries);
    }

    public static JsonElement serializeUserFunction(UserFunction func, @Nullable Marshaller marshaller) {
        JsonArray arr = new JsonArray();
        for (UserFunction.Entry value : func.values) {
            JsonObject e = new JsonObject();
            e.put("y", new JsonPrimitive(value.y));
            e.put("v", new JsonPrimitive(value.v));
            arr.add(e);
        }
        return arr;
    }

    private static BlockDesc deserializeBlock(String obj, Marshaller marshaller) {
        return new BlockDesc(obj);
    }

    private static JsonElement serializeBlock(BlockDesc block, Marshaller m) {
        return new JsonPrimitive(block.getBlockId());
    }

    public static BlockStateDesc deserializeBlockstate(JsonObject obj, @Nullable Marshaller marshaller) {
        String name = obj.get(String.class, "Name");
        Map<String, String> properties = new HashMap<>();
        if (obj.containsKey("Properties")) {
            JsonObject propertiesObj = obj.get(JsonObject.class, "Properties");
            for (Map.Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                properties.put(entry.getKey(), ((JsonPrimitive) entry.getValue()).asString());
            }
        }
        return new BlockStateDesc(name, properties);
    }

    public static JsonElement serializeBlockstate(BlockStateDesc state, Marshaller m) {
        JsonObject json = new JsonObject();
        json.put("Name", new JsonPrimitive(state.getBlockId()));

        if (!state.getProperties().isEmpty()) {
            JsonObject properties = new JsonObject();
            state.getProperties().forEach((name, value) -> properties.put(name, new JsonPrimitive(value)));
            json.put("Properties", properties);
        }

        return json;
    }

    private static CubeAreas deserializeCubeAreas(JsonArray arr, Marshaller marshaller) {
        List<Map.Entry<IntAABB, CustomGeneratorSettings>> map = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JsonArray entry = (JsonArray) arr.get(i);
            JsonObject key = (JsonObject) entry.get(0);
            JsonObject value = (JsonObject) entry.get(1);

            IntAABB aabb = arr.getMarshaller().marshall(IntAABB.class, key);
            CustomGeneratorSettings conf = arr.getMarshaller().marshall(CustomGeneratorSettings.class, value);
            map.add(new AbstractMap.SimpleEntry<>(aabb, conf));
        }
        return new CubeAreas(map);
    }

    private static JsonElement serializeCubeAreas(CubeAreas areas, Marshaller marshaller) {
        JsonArray json = new JsonArray();
        json.setMarshaller(marshaller);
        for (Map.Entry<IntAABB, CustomGeneratorSettings> entry : areas.map) {
            JsonObject aabb = (JsonObject) marshaller.serialize(entry.getKey());
            aabb.setMarshaller(marshaller);
            JsonObject conf = (JsonObject) marshaller.serialize(entry.getValue());
            conf.setMarshaller(marshaller);

            JsonArray layer = new JsonArray();
            layer.setMarshaller(marshaller);
            layer.add(aabb);
            layer.add(conf);

            json.add(layer);
        }
        return json;
    }

    private static TreeMap<Integer, FlatLayer> deserializeFlatCubicLayers(JsonObject obj, Marshaller marshaller) {
        TreeMap<Integer, FlatLayer> map = new TreeMap<>();

        for (String yStr : obj.keySet()) {
            JsonObject value = (JsonObject) obj.get(yStr);
            int y = Integer.parseInt(yStr);
            FlatLayer layer = obj.getMarshaller().marshall(FlatLayer.class, value);
            map.put(y, layer);
        }
        return map;
    }

    private static JsonElement serializeCubeFlatCubicLayers(TreeMap<Integer, FlatLayer> map, Marshaller marshaller) {
        JsonObject json = new JsonObject();
        for (Map.Entry<Integer, FlatLayer> entry : map.entrySet()) {
            json.put(entry.getKey().toString(), marshaller.serialize(entry.getValue()));
        }
        return json;
    }

    private static StandardOreList deserializeStandardOreList(JsonArray obj, Marshaller marshaller) throws DeserializationException {
        StandardOreList ores = new StandardOreList();
        for (JsonElement element : obj) {
            ores.list.add(marshaller.marshallCarefully(StandardOreConfig.class, element));
        }
        return ores;
    }

    private static JsonArray serializeStandardOreList(StandardOreList ores, Marshaller marshaller) {
        JsonArray json = new JsonArray();
        for (StandardOreConfig ore : ores.list) {
            json.add(marshaller.serialize(ore));
        }
        return json;
    }

    private static PeriodicOreList deserializePeriodicOreList(JsonArray obj, Marshaller marshaller) throws DeserializationException {
        PeriodicOreList ores = new PeriodicOreList();
        for (JsonElement element : obj) {
            ores.list.add(marshaller.marshallCarefully(PeriodicGaussianOreConfig.class, element));
        }
        return ores;
    }

    private static JsonArray serializePeriodicOreList(PeriodicOreList ores, Marshaller marshaller) {
        JsonArray json = new JsonArray();
        for (PeriodicGaussianOreConfig ore : ores.list) {
            json.add(marshaller.serialize(ore));
        }
        return json;
    }

    public static Jankson jankson() {

        Jankson.Builder builder = Jankson.builder();
        builder.registerDeserializer(JsonObject.class, BiomeBlockReplacerConfig.class, CustomGenSettingsSerialization::deserializeReplacerConfig);
        builder.registerSerializer(BiomeBlockReplacerConfig.class, CustomGenSettingsSerialization::serializeReplacerConfig);

        builder.registerDeserializer(String.class, BiomeDesc.class, CustomGenSettingsSerialization::deserializeBiome);
        builder.registerSerializer(BiomeDesc.class, CustomGenSettingsSerialization::serializeBiome);

        builder.registerDeserializer(JsonArray.class, UserFunction.class, CustomGenSettingsSerialization::deserializeUserFunction);
        builder.registerSerializer(UserFunction.class, CustomGenSettingsSerialization::serializeUserFunction);

        builder.registerDeserializer(String.class, BlockDesc.class, CustomGenSettingsSerialization::deserializeBlock);
        builder.registerSerializer(BlockDesc.class, CustomGenSettingsSerialization::serializeBlock);

        builder.registerDeserializer(JsonObject.class, BlockStateDesc.class, CustomGenSettingsSerialization::deserializeBlockstate);
        builder.registerSerializer(BlockStateDesc.class, CustomGenSettingsSerialization::serializeBlockstate);

        builder.registerDeserializer(JsonArray.class, CubeAreas.class, CustomGenSettingsSerialization::deserializeCubeAreas);
        builder.registerSerializer(CubeAreas.class, CustomGenSettingsSerialization::serializeCubeAreas);

        builder.registerDeserializer(JsonObject.class, TreeMap.class, CustomGenSettingsSerialization::deserializeFlatCubicLayers);
        builder.registerSerializer(TreeMap.class, CustomGenSettingsSerialization::serializeCubeFlatCubicLayers);

        builder.registerDeserializer(JsonArray.class, StandardOreList.class, CustomGenSettingsSerialization::deserializeStandardOreList);
        builder.registerSerializer(StandardOreList.class, CustomGenSettingsSerialization::serializeStandardOreList);

        builder.registerDeserializer(JsonArray.class, PeriodicOreList.class, CustomGenSettingsSerialization::deserializePeriodicOreList);
        builder.registerSerializer(PeriodicOreList.class, CustomGenSettingsSerialization::serializePeriodicOreList);

        return builder.build();
    }
}
