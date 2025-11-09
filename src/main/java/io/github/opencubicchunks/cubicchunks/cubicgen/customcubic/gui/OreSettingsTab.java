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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import com.google.common.eventbus.Subscribe;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.ExtraGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiBlockStateButton;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiCheckBox;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiSlider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UILayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIList;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIRangeSlider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISplitLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISplitLayout.Type;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIVerticalTableLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.WrappedVanillaComponent;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.JsonObjectView;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.JsonTransformer;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BiomeDesc;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.makeButton;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.makeCheckBox;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.makeCheckBoxUnlocalized;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.makeIntSlider;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.makeSlider;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.wrap;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.*;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.HORIZONTAL_PADDING;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.VERTICAL_INSETS;

class OreSettingsTab {

    private static final JsonTransformer<Void> STANDARD_TO_PERIODIC_TRANSFORM =
            JsonTransformer.<Void>builder("Standard to periodic ore")
            .passthroughFor(
                    "blockstate",
                    "biomes",
                    "generateWhen",
                    "placeBlockWhen",
                    "spawnSize",
                    "spawnTries",
                    "spawnProbability",
                    "minHeight",
                    "maxHeight"
            )
            .passthroughWithDefault("heightMean", 0.0)
            .passthroughWithDefault("heightStdDeviation", 1.0)
            .passthroughWithDefault("heightSpacing", 2.0)
            .build();

    private static final JsonTransformer<Void> PERIODIC_TO_STANDARD_TRANSFORM =
            JsonTransformer.<Void>builder("Periodic to standard ore")
                    .passthroughFor(
                            "blockstate",
                            "biomes",
                            "generateWhen",
                            "placeBlockWhen",
                            "spawnSize",
                            "spawnTries",
                            "spawnProbability",
                            "minHeight",
                            "maxHeight"
                    )
                    .drop(
                            "heightMean",
                            "heightStdDeviation",
                            "heightSpacing"
                    )
                    .build();

    private static final JsonTransformer<UIOreOptionEntry> WRITE_TO_JSON_TRANSFORM =
            JsonTransformer.<UIOreOptionEntry>builder("Write GUI state to json")
                    .passthroughWithDefault("generateWhen", JsonNull.INSTANCE)
                    .passthroughWithDefault("placeBlockWhen", JsonNull.INSTANCE)
                    .valueTransform("blockstate", (json, ore) -> CustomGenSettingsSerialization.MARSHALLER.serialize(ore.block.getBlockState()))
                    .valueTransform("biomes", (json, ore) -> {
                        Set<BiomeDesc> biomes = ore.selectBiomes.isChecked() ?
                                ore.biomesArea.getData().stream()
                                        .filter(b -> ore.biomesArea.component(b).get().isChecked())
                                        .map(BiomeDesc::new)
                                        .collect(Collectors.toSet())
                                : null;
                        return CustomGenSettingsSerialization.MARSHALLER.serialize(biomes);
                    })
                    .setPrimitive("spawnSize", ore -> (int) Math.round(ore.size.getSliderValue()))
                    .setPrimitive("spawnTries", ore -> (int) Math.round(ore.attempts.getSliderValue()))
                    .setPrimitive("spawnProbability", ore -> ore.probability.getSliderValue())
                    .setPrimitive("minHeight", ore -> ore.heightRange.getMinValue())
                    .setPrimitive("maxHeight", ore -> ore.heightRange.getMaxValue())
                    .setPrimitiveIf(ore -> ore.genType == OreGenType.PERIODIC_GAUSSIAN, "heightMean", ore -> ore.mean.getSliderValue())
                    .setPrimitiveIf(ore -> ore.genType == OreGenType.PERIODIC_GAUSSIAN, "heightStdDeviation", ore -> ore.stdDev.getSliderValue())
                    .setPrimitiveIf(ore -> ore.genType == OreGenType.PERIODIC_GAUSSIAN, "heightSpacing", ore -> ore.spacing.getSliderValue())
                    .build();

    private static final JsonObject DEFAULT_STANDARD_ORE = JsonObjectView.empty()
            .put("blockstate", JsonObjectView.empty().put("Name", "minecraft:tnt"))
            .putNull("biomes")
            .putNull("generateWhen")
            .putNull("placeBlockWhen")
            .put("spawnSize", 8)
            .put("spawnTries", 4)
            .put("spawnProbability", 1.0)
            .put("minHeight", Double.NEGATIVE_INFINITY)
            .put("maxHeight", Double.POSITIVE_INFINITY)
            .object();

    private final ArrayList<UIComponent<?>> componentList;

    private final UIContainer<?> container;
    private final DoubleSupplier baseHeight;
    private final DoubleSupplier heightVariation;

    OreSettingsTab(ExtraGui gui, JsonObjectView conf, DoubleSupplier baseHeight, DoubleSupplier heightVariation) {
        this.baseHeight = baseHeight;
        this.heightVariation = heightVariation;
        this.componentList = new ArrayList<>();
        UIList<UIComponent<?>, UIComponent<?>> layout = new UIList<>(gui, this.componentList, x -> x);
        layout.setPadding(HORIZONTAL_PADDING, VERTICAL_INSETS);
        layout.setSize(UIComponent.INHERITED, UIComponent.INHERITED);

        layout.add(wrap(gui, makeButton("add_ore", btn -> {
            JsonObjectView newJson = JsonObjectView.of(DEFAULT_STANDARD_ORE.clone());
            UIOreOptionEntry newEntry = new UIOreOptionEntry(gui, newJson, OreGenType.UNIFORM);
            componentList.add(1, newEntry);
        })));

        for (JsonObjectView c : conf.objectArray("standardOres")) {
            layout.add(new UIOreOptionEntry(gui, c, OreGenType.UNIFORM));
        }

        for (JsonObjectView c : conf.objectArray("periodicGaussianOres")) {
            layout.add(new UIOreOptionEntry(gui, c, OreGenType.PERIODIC_GAUSSIAN));
        }
        layout.setRightPadding(HORIZONTAL_PADDING + 6);
        this.container = layout;
    }

    UIContainer<?> getContainer() {
        return container;
    }

    void writeConfig(JsonObjectView json) {
        List<Integer> existStandard = new ArrayList<>();
        List<Integer> existPeriodic = new ArrayList<>();
        for (UIComponent<?> c : componentList) {
            if (!(c instanceof UIOreOptionEntry)) {
                continue;
            }
            UIOreOptionEntry entry = (UIOreOptionEntry) c;
            int idx = entry.writeJson(json);
            if (entry.genType == OreGenType.UNIFORM) {
                existStandard.add(idx);
            } else {
                existPeriodic.add(idx);
            }
        }
        existStandard.sort(Integer::compareTo);
        existPeriodic.sort(Integer::compareTo);

        JsonObjectView.JsonArrayView<JsonObjectView> oldStandard = json.objectArray("standardOres");
        JsonObjectView.JsonArrayView<JsonObjectView> oldPeriodic = json.objectArray("periodicGaussianOres");

        JsonArray newStandard = new JsonArray();
        JsonArray newPeriodic = new JsonArray();

        for (int i : existStandard) {
            newStandard.add(oldStandard.value(i).object(), oldStandard.comment(i));
        }
        for (int i : existPeriodic) {
            newPeriodic.add(oldPeriodic.value(i).object(), oldPeriodic.comment(i));
        }
        json.put("standardOres", newStandard);
        json.put("periodicGaussianOres", newPeriodic);
    }

    private class UIOreOptionEntry extends UIVerticalTableLayout<UIOreOptionEntry> {

        /*
        The layout:

        Biome selection Off
        +------+------+------+------+------+------+
        |BLOCK : <=========NAME==========> :DELETE|
        |STATE : <=BLOCKSTATE PROPERTIES=> : TYPE |
        +------+------+------+------+------+------+
        | <===SPAWN SIZE===> : <===VEIN COUNT===> |
        | <===SPAWN PROB===> : <==BIOME ON/OFF==> |
        | <======MEAN======> : <=====SPACING====> | // periodic only
        | <============STD DEVIATION============> | // periodic only
        | <============SPAWN HEIGHTS============> |
        +------+------+------+------+------+------+

        Biome selection On
        +------+------+------+------+------+------+ ----\
        |BLOCK : <=========NAME==========> :DELETE|\TABLE\
        |STATE : <=BLOCKSTATE PROPERTIES=> : TYPE |/LAYOUT\
        +--------------+--------------+-----------+        \
        |  SPAWN SIZE  :  VEIN COUNT  |[V]Biome 1||         \  VERTICAL TABLE
        |  SPAWN PROB  : BIOME ON/OFF |[V]Biome 2 |         /  LAYOUT (this)
        | <===MEAN===> : <=SPACING==> |[V]Biome 3 |        /
        | <======STD DEVIATION======> |[ ]Biome 4 |       /
        | <======SPAWN HEIGHTS======> |[ ]Biome 5 |      /
        +--------------+--------------^-----------+ ----/
        |                             |           |
        |<---VERTICAL TABLE LAYOUT--->|<-UI LIST->|
                  (MAIN AREA)         \->Split layout
        */
        private CwgGuiBlockStateButton block;
        private UIComponent<?> name;

        private CwgGuiSlider size;
        private CwgGuiSlider attempts;

        private CwgGuiSlider mean;
        private CwgGuiSlider spacing;

        private CwgGuiSlider stdDev;

        private CwgGuiSlider probability;
        private CwgGuiCheckBox selectBiomes;

        private UIRangeSlider<Float> heightRange;

        private UIList<String, WrappedVanillaComponent<CwgGuiCheckBox>> biomesArea;

        private JsonObjectView conf;
        private OreGenType genType;

        UIOreOptionEntry(ExtraGui gui, JsonObjectView conf, OreGenType type) {
            super(gui, 6);
            this.genType = type;
            this.conf = conf;
            this.init(gui);
        }

        private void init(ExtraGui gui) {
            this.removeAll();
            this.block = new CwgGuiBlockStateButton(conf.getBlockState("blockstate"));
            this.name = makeLabel(gui);
            UIButton delete = new UIButton(gui, malisisText("delete")).setSize(10, 20).setAutoSize(false);
            UISelect<OreGenType> type = makeUISelect(gui, Arrays.asList(OreGenType.values()));

            this.size = makeIntSlider(1, 50, conf.getInt("spawnSize"), "spawn_size");
            this.attempts = makeIntSlider(1, 40, conf.getInt("spawnTries"), "spawn_tries");
            if (genType == OreGenType.PERIODIC_GAUSSIAN) {
                this.mean = makeSlider(-4.0, 4.0, conf.getDouble("heightMean"),
                        "mean_height",
                        value -> String.format("%.3f (%.1f)", value, value * heightVariation.getAsDouble()));

                this.spacing = CwgGuiFactory.makePositiveExponentialSlider(-1, 6.0, conf.getDouble("heightSpacing"),
                        "spacing_height",
                        value -> String.format("%.3f (%.1f)", value, value * heightVariation.getAsDouble()));

                this.stdDev =  makeSlider(0, 1, conf.getDouble("heightStdDeviation"),
                        "height_std_dev",
                        value -> String.format("%.3f (%.1f)", value, value * heightVariation.getAsDouble()));
            } else {
                this.mean = null;
                this.spacing = null;
                this.stdDev = null;
            }
            this.probability = makeSlider(0, 1, conf.getDouble("spawnProbability"),
                    "spawn_probability", value -> String.format("%.3f", value));
            this.selectBiomes = makeCheckBox("select_biomes", !conf.get("biomes").equals(JsonNull.INSTANCE));
            this.heightRange = makeOreHeightSlider(gui, vanillaText("spawn_range"), -2.0f, 2.0f,
                    conf.getFloat("minHeight"), conf.getFloat("maxHeight"), baseHeight, heightVariation);

            UISplitLayout<?> deleteTypeArea =
                    new UISplitLayout<>(gui, Type.STACKED, delete, type).sizeWeights(1, 1).setSizeOf(UISplitLayout.Pos.SECOND, 10)
                            .setSize(0, 30);
            UIVerticalTableLayout<?> mainArea = new UIVerticalTableLayout<>(gui, 6).autoFitToContent(true);

            // use new ArrayList so it can be sorted
            biomesArea = new UIList<>(gui,
                    ForgeRegistries.BIOMES.getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()),
                    name1 -> wrap(gui, makeBiomeCheckbox(name1)));

            this.block.onClick(btn ->
                    UIBlockStateSelect.makeOverlay(gui, state -> {
                        block.setBlockState(new BlockStateDesc(state));
                        updateLabel(gui, name);
                    }).display()
            );
            delete.register(new Object() {
                @Subscribe
                public void onClick(UIButton.ClickEvent evt) {
                    container.remove(UIOreOptionEntry.this);
                }
            });
            this.selectBiomes.onClick(check -> allowSelectBiomes(biomesArea, check.isChecked()));
            type.register(new Object() {
                @Subscribe
                public void onClick(UISelect.SelectEvent<OreGenType> evt) {
                    if (evt.getNewValue() == genType) {
                        return;
                    }
                    if (evt.getNewValue() == OreGenType.UNIFORM) {
                        conf = JsonObjectView.of(PERIODIC_TO_STANDARD_TRANSFORM.transform(toJson(), null));
                        genType = OreGenType.UNIFORM;
                    } else {
                        conf = JsonObjectView.of(STANDARD_TO_PERIODIC_TRANSFORM.transform(toJson(), null));
                        genType = OreGenType.PERIODIC_GAUSSIAN;
                    }
                    init(gui);
                }
            });
            type.select(genType);

            setupMainArea(mainArea);
            allowSelectBiomes(biomesArea, this.selectBiomes.isChecked());
            setupBiomeArea(conf, biomesArea);
            setupThis(gui, deleteTypeArea, mainArea, biomesArea);
        }

        private JsonObject toJson() {
            return WRITE_TO_JSON_TRANSFORM.transform(conf.object(), this);
        }

        int writeJson(JsonObjectView rootJson) {
            JsonArray oreList = rootJson.objectArray(genType == OreGenType.UNIFORM ? "standardOres" : "periodicGaussianOres").array();
            int idx = -1;
            for (int i = 0; i < oreList.size(); i++) {
                if (oreList.get(i) == conf.object()) {
                    idx = i;
                    break;
                }
            }
            this.conf = JsonObjectView.of(toJson());
            if (idx >= 0) {
                String comment = oreList.getComment(idx);
                oreList.set(idx, this.conf.object());
                oreList.setComment(idx, comment);
                return idx;
            } else {
                oreList.add(conf.object());
                return oreList.size() - 1;
            }
        }

        private void setupMainArea(UIVerticalTableLayout<?> mainArea) {
            int y = -1;
            mainArea.add(wrap(getGui(), this.size), new GridLocation(0, ++y, 3));
            mainArea.add(wrap(getGui(), this.attempts), new GridLocation(3, y, 3));
            mainArea.add(wrap(getGui(), this.probability), new GridLocation(0, ++y, 3));
            mainArea.add(wrap(getGui(), this.selectBiomes), new GridLocation(3, y, 3));
            if (this.genType == OreGenType.PERIODIC_GAUSSIAN) {
                mainArea.add(wrap(getGui(), this.mean), new GridLocation(0, ++y, 3));
                mainArea.add(wrap(getGui(), this.spacing), new GridLocation(3, y, 3));
                mainArea.add(wrap(getGui(), this.stdDev), new GridLocation(0, ++y, 6));
            }
            mainArea.add(this.heightRange, new GridLocation(0, ++y, 6));
        }

        private void allowSelectBiomes(UIList<String, WrappedVanillaComponent<CwgGuiCheckBox>> biomes, boolean checked) {
            biomes.setVisible(checked);
            if (!biomes.isVisible()) {
                biomes.getData().forEach(e -> biomes.component(e).get().setIsChecked(true));
            }
        }

        private void setupBiomeArea(JsonObjectView conf, UIList<String, WrappedVanillaComponent<CwgGuiCheckBox>> biomesArea) {
            biomesArea.setRightPadding(6);

            if (!conf.get("biomes").equals(JsonNull.INSTANCE)) {
                conf.forEachString("biomes", b -> biomesArea.component(b).get().setIsChecked(true));
            }

            ((List<String>) biomesArea.getData()).sort((b1, b2) ->
                    biomesArea.component(b1).get().isChecked() && !biomesArea.component(b2).get().isChecked() ? 1 : 0
            );
        }

        private void setupThis(ExtraGui gui, UIComponent<?> deleteTypeArea, UIComponent<?> mainArea, UILayout<?> biomesArea) {
            UISplitLayout<?> split =
                    new UISplitLayout<>(gui, Type.SIDE_BY_SIDE, mainArea, biomesArea).sizeWeights(2, 1).autoFitToContent(true).userResizable(false);

            this.autoFitToContent(true);
            this.add(this.name, new GridLocation(1, 0, 4));
            this.add(wrap(gui, this.block), new GridLocation(0, 0, 1));
            this.add(deleteTypeArea, new GridLocation(5, 0, 1));
            this.add(split, new GridLocation(0, 1, 6));
            biomesArea.setHeightFunc(() -> ((UIContainer<?>) Objects.requireNonNull(split.getFirst())).getContentHeight());
        }

        private CwgGuiCheckBox makeBiomeCheckbox(String name) {
            Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(name));
            String text = biome == null ? name : String.format("%s (%s)", biome.getBiomeName(), biome.getRegistryName());
            return makeCheckBoxUnlocalized(text, false);
        }

        private UIContainer<?> makeLabel(ExtraGui gui) {
            UIVerticalTableLayout<?> label = new UIVerticalTableLayout<>(gui, 1).setInsets(0, 0, 0, 0);
            updateLabel(gui, label);
            return label;
        }

        private void updateLabel(ExtraGui gui, UIComponent<?> label) {

            ((UIContainer<?>) label).removeAll();

            String name = block.getBlockState().getBlockId();
            String props = block.getBlockState().getProperties().entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .reduce((a, b) -> a + ", " + b).orElse("");

            UIComponent<?> l1 = label(gui, name);
            UIComponent<?> l2 = label(gui, String.format("[%s]", props));
            ((UIContainer<?>) label).add(l1, l2);
            label.setSize(label.getWidth(), l1.getHeight() + l2.getHeight());
        }
    }

    public enum OreGenType {
        UNIFORM, PERIODIC_GAUSSIAN
    }
}
