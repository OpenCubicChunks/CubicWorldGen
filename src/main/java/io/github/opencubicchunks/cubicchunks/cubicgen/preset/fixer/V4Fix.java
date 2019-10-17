package io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.JsonTransformer.CombinedContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class V4Fix implements IJsonFix {

    private final JsonTransformer<CustomGeneratorSettingsFixer> transformer = JsonTransformer.<CustomGeneratorSettingsFixer>builder("V3 -> V4")
            .drop("v3fix")
            .valueTransform("version", (e, ctx) -> new JsonPrimitive(4))
            .passthroughFor(
                    "waterLevel",
                    "caves",
                    "strongholds", "alternateStrongholdsPositions",
                    "villages", "mineshafts", "temples", "oceanMonuments", "woodlandMansions", "ravines",
                    "dungeons", "dungeonCount",
                    "lavaOceans",
                    "biome", "biomeSize", "riverSize",
                    "heightVariationFactor", "specialHeightVariationFactorBelowAverageY", "heightVariationOffset", "heightFactor", "heightOffset",
                    "depthNoiseFactor", "depthNoiseOffset", "depthNoiseFrequencyX", "depthNoiseFrequencyZ", "depthNoiseOctaves",
                    "selectorNoiseFactor", "selectorNoiseOffset",
                    "selectorNoiseFrequencyX", "selectorNoiseFrequencyY", "selectorNoiseFrequencyZ", "selectorNoiseOctaves",
                    "lowNoiseFactor", "lowNoiseOffset", "lowNoiseFrequencyX", "lowNoiseFrequencyY", "lowNoiseFrequencyZ", "lowNoiseOctaves",
                    "highNoiseFactor", "highNoiseOffset", "highNoiseFrequencyX", "highNoiseFrequencyY", "highNoiseFrequencyZ", "highNoiseOctaves",
                    "standardOres", "periodicGaussianOres",
                    "expectedBaseHeight", "expectedHeightVariation", "actualHeight"
            )
            .objectArrayTransform("lakes",
                    (oldRoot, newRoot, ctx) -> {
                        convertWaterLakes(oldRoot, newRoot);
                        convertLavaLakes(oldRoot, newRoot);
                    }, JsonTransformer.passthroughAll("V3 -> V4 (lakes)"),
                    "waterLakeRarity", "lavaLakeRarity", "aboveSeaLavaLakeRarity", "waterLakes", "lavaLakes")
            .objectTransform("replacerConfig",
                    JsonTransformer.<CombinedContext<CustomGeneratorSettingsFixer>>builder("V3 -> V4 (replacer config)")
                            .passthroughFor("overrides")
                            .objectTransform("defaults",
                                    JsonTransformer.<CombinedContext<CombinedContext<CustomGeneratorSettingsFixer>>>builder(
                                            "V3 -> V4 (replacer config defaults)")
                                            .passthroughFor(
                                                    "cubicgen:biome_fill_noise_octaves",
                                                    "cubicgen:ocean_block",
                                                    "cubicgen:height_scale",
                                                    "cubicgen:biome_fill_noise_freq",
                                                    "cubicgen:water_level",
                                                    "cubicgen:biome_fill_depth_factor",
                                                    "cubicgen:terrain_fill_block",
                                                    "cubicgen:mesa_depth",
                                                    "cubicgen:horizontal_gradient_depth_decrease_weight",
                                                    "cubicgen:biome_fill_depth_offset",
                                                    "cubicgen:height_offset"
                                            )
                                            .passthroughWithDefault("cubicgen:surface_depth_limit", 9.0)
                                            .build(),
                                    new JsonObject())
                            .build(),
                    new JsonObject())
            .valueTransform("cubeAreas", this::convertCubeAreas)
            .build();

    @Override public JsonObject fix(CustomGeneratorSettingsFixer fixer, JsonObject oldRoot) {
        return transformer.transform(oldRoot, fixer);
    }

    private JsonElement convertCubeAreas(JsonElement oldCubeAreasElement, CustomGeneratorSettingsFixer fixer) {
        JsonArray newCubeAreas = new JsonArray();
        JsonArray oldCubeAreas = (JsonArray) oldCubeAreasElement;
        for (int i = 0; i < oldCubeAreas.size(); i++) {
            JsonArray oldLayer = (JsonArray) oldCubeAreas.get(i);
            JsonArray newLayer = new JsonArray();

            newLayer.add(oldLayer.get(0), oldLayer.getComment(0));

            JsonObject fixedLayer = fixer.fixJsonNew((JsonObject) oldLayer.get(1));
            newLayer.add(fixedLayer, oldLayer.getComment(1));

            newCubeAreas.add(newLayer, oldCubeAreas.getComment(i));
        }
        return newCubeAreas;
    }

    private void convertWaterLakes(JsonObject oldRoot, JsonObject newRoot) {
        if (!newRoot.containsKey("lakes")) {
            newRoot.put("lakes", new JsonArray());
        }
        if (!oldRoot.get("waterLakes").equals(JsonPrimitive.TRUE)) {
            return;
        }
        double waterLakeRarity = ((JsonPrimitive) oldRoot.get("waterLakeRarity")).asFloat(4);

        JsonArray userFunction = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.put("y", new JsonPrimitive(0));
        entry.put("v", new JsonPrimitive(1 / waterLakeRarity));
        userFunction.add(entry);

        JsonArray biomes = new JsonArray();
        biomes.add(new JsonPrimitive("minecraft:desert"));
        biomes.add(new JsonPrimitive("minecraft:desert_hills"));

        JsonObject lakeConfig = new JsonObject();
        lakeConfig.put("block", new JsonPrimitive("minecraft:water"));
        lakeConfig.put("biomes", biomes);
        lakeConfig.put("biomeSelect", new JsonPrimitive("EXCLUDE"));
        lakeConfig.put("surfaceProbability", userFunction.clone());
        lakeConfig.put("mainProbability", userFunction.clone());

        JsonArray lakes = (JsonArray) newRoot.get("lakes");

        List<String> comments = new ArrayList<>();
        String waterLakesComment = oldRoot.getComment("waterLakes");
        String waterLakeRarityComment = oldRoot.getComment("waterLakeRarity");
        if (waterLakesComment != null) {
            comments.add(waterLakesComment);
        }
        if (waterLakeRarityComment != null) {
            comments.add(waterLakeRarityComment);
        }
        String comment = null;
        if (!comments.isEmpty()) {
            comment = String.join("\n", comments);
        }
        lakes.add(lakeConfig, comment);
    }

    private void convertLavaLakes(JsonObject oldRoot, JsonObject newRoot) {
        if (!newRoot.containsKey("lakes")) {
            newRoot.put("lakes", new JsonArray());
        }
        if (!oldRoot.get("lavaLakes").equals(JsonPrimitive.TRUE)) {
            return;
        }
        double expectedBaseHeight = ((JsonPrimitive) oldRoot.get("expectedBaseHeight")).asInt(64);
        double expectedHeightVariation = ((JsonPrimitive) oldRoot.get("expectedHeightVariation")).asInt(64);

        double lavaLakeRarity = ((JsonPrimitive) oldRoot.get("lavaLakeRarity")).asFloat(8);
        double aboveSeaLavaLakeRarity = ((JsonPrimitive) oldRoot.get("aboveSeaLavaLakeRarity")).asInt(13);

        Map<Float, Float> builder = new HashMap<>();

        double increment = expectedHeightVariation * 0.1;

        for (int i = -11; i <= 11; i++) {
            int clamped = Math.max(Math.min(i, 10), -10);
            double y = Math.signum(i) * (Math.pow(2, Math.abs(i)) - 1) * increment + expectedBaseHeight;
            double getY = Math.signum(clamped) * (Math.pow(2, Math.abs(clamped)) - 1) * increment + expectedBaseHeight;
            double v = lavaLakeProbability(expectedBaseHeight, expectedHeightVariation, lavaLakeRarity, aboveSeaLavaLakeRarity, getY);
            builder.put((float) y, (float) v);
        }
        double v0 = lavaLakeProbability(expectedBaseHeight, expectedHeightVariation, lavaLakeRarity, aboveSeaLavaLakeRarity, expectedBaseHeight - 1);
        builder.put((float) expectedBaseHeight - 1, (float) v0);
        double v1 = lavaLakeProbability(expectedBaseHeight, expectedHeightVariation, lavaLakeRarity, aboveSeaLavaLakeRarity, expectedBaseHeight + 1);
        builder.put((float) expectedBaseHeight + 1, (float) v1);

        List<Float> yCoords = new ArrayList<>(builder.keySet());
        yCoords.sort(Float::compareTo);

        JsonArray userFunction = new JsonArray();
        for (Float yCoord : yCoords) {
            JsonObject entry = new JsonObject();
            entry.put("y", new JsonPrimitive(yCoord));
            entry.put("v", new JsonPrimitive(builder.get(yCoord)));
            userFunction.add(entry);
        }

        JsonArray biomes = new JsonArray();
        biomes.add(new JsonPrimitive("minecraft:desert"));
        biomes.add(new JsonPrimitive("minecraft:desert_hills"));


        JsonObject lakeConfig = new JsonObject();
        lakeConfig.put("block", new JsonPrimitive("minecraft:lava"));
        lakeConfig.put("biomes", biomes);
        lakeConfig.put("biomeSelect", new JsonPrimitive("EXCLUDE"));
        lakeConfig.put("surfaceProbability", userFunction);
        lakeConfig.put("mainProbability", userFunction);

        JsonArray lakes = (JsonArray) newRoot.get("lakes");
        List<String> comments = new ArrayList<>();
        String lavaLakesComment = oldRoot.getComment("lavaLakes");
        String lavaLakeRarityComment = oldRoot.getComment("lavaLakeRarity");
        String aboveSeaLavaLakeRarityComment = oldRoot.getComment("aboveSeaLavaLakeRarity");
        if (lavaLakesComment != null) {
            comments.add(lavaLakesComment);
        }
        if (lavaLakeRarityComment != null) {
            comments.add(lavaLakeRarityComment);
        }
        if (aboveSeaLavaLakeRarityComment != null) {
            comments.add(aboveSeaLavaLakeRarityComment);
        }
        String comment = null;
        if (!comments.isEmpty()) {
            comment = String.join("\n", comments);
        }
        lakes.add(lakeConfig, comment);
    }

    private void convertReplacerConfigs(JsonObject oldRoot, JsonObject newRoot) {

    }

    private double lavaLakeProbability(double expectedBaseHeight, double expectedHeightVariation, double rarity, double aboveSeaRarity, double y) {
        // same as DefaultDecorator.waterSourceProbabilityForY
        final double yScale = -0.0242676003062542;
        final double yOffset = 0.723583275161355;
        final double valueScale = 0.00599930877922822;

        double normalizedY = (y - expectedBaseHeight) / expectedHeightVariation;
        double vanillaY = normalizedY * 64 + 64;
        double v = (Math.atan(vanillaY * yScale + yOffset) + Math.PI / 2) * valueScale / rarity;
        if (y > expectedBaseHeight) {
            v /= aboveSeaRarity;
        }
        return v;
    }
}
