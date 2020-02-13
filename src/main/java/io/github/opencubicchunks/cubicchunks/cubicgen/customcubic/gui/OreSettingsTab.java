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
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIBlockStateButton;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UICheckboxNoAutoSize;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UILayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIList;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIRangeSlider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISplitLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISplitLayout.Type;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIVerticalTableLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.JsonObjectView;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.JsonTransformer;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BiomeDesc;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UISlider;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.*;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.HORIZONTAL_PADDING;

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
                    .valueTransform("blockstate", (json, ore) -> CustomGenSettingsSerialization.MARSHALLER.serialize(ore.block.getState()))
                    .valueTransform("biomes", (json, ore) -> {
                        Set<BiomeDesc> biomes = ore.selectBiomes.isChecked() ?
                                ore.biomesArea.getData().stream()
                                        .filter(b -> ore.biomesArea.component(b).isChecked())
                                        .map(BiomeDesc::new)
                                        .collect(Collectors.toSet())
                                : null;
                        return CustomGenSettingsSerialization.MARSHALLER.serialize(biomes);
                    })
                    .setPrimitive("spawnSize", ore -> ore.size.getValue())
                    .setPrimitive("spawnTries", ore -> ore.attempts.getValue())
                    .setPrimitive("spawnProbability", ore -> ore.probability.getValue())
                    .setPrimitive("minHeight", ore -> ore.heightRange.getMinValue())
                    .setPrimitive("maxHeight", ore -> ore.heightRange.getMaxValue())
                    .setPrimitiveIf(ore -> ore.genType == OreGenType.PERIODIC_GAUSSIAN, "heightMean", ore -> ore.mean.getValue())
                    .setPrimitiveIf(ore -> ore.genType == OreGenType.PERIODIC_GAUSSIAN, "heightStdDeviation", ore -> ore.stdDev.getValue())
                    .setPrimitiveIf(ore -> ore.genType == OreGenType.PERIODIC_GAUSSIAN, "heightSpacing", ore -> ore.spacing.getValue())
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
        layout.setPadding(HORIZONTAL_PADDING, 0);
        layout.setSize(UIComponent.INHERITED, UIComponent.INHERITED);

        layout.add(new UIButton(gui, malisisText("add_ore")).setAutoSize(false).setSize(UIComponent.INHERITED, 30).register(
                new Object() {
                    @Subscribe
                    public void onClick(UIButton.ClickEvent evt) {
                        componentList.add(1, new UIOreOptionEntry(gui, JsonObjectView.of(DEFAULT_STANDARD_ORE.clone()), OreGenType.UNIFORM));
                    }
                }
        ));

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
        private UIBlockStateButton<?> block;
        private UIComponent<?> name;

        private UISlider<Integer> size;
        private UISlider<Integer> attempts;

        private UISlider<Float> mean;
        private UISlider<Float> spacing;

        private UISlider<Float> stdDev;

        private UISlider<Float> probability;
        private UICheckBox selectBiomes;

        private UIRangeSlider<Float> heightRange;

        private UIList<String, UICheckBox> biomesArea;

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
            this.block = new UIBlockStateButton<>(gui, conf.getBlockState("blockstate"));
            this.name = makeLabel(gui);
            UIButton delete = new UIButton(gui, malisisText("delete")).setSize(10, 20).setAutoSize(false);
            UISelect<OreGenType> type = makeUISelect(gui, Arrays.asList(OreGenType.values()));
            this.size = makeIntSlider(gui, malisisText("spawn_size", " %d"), 1, 50, conf.getInt("spawnSize"));
            this.attempts = makeIntSlider(gui, malisisText("spawn_tries", " %d"), 1, 40, conf.getInt("spawnTries"));
            if (genType == OreGenType.PERIODIC_GAUSSIAN) {
                this.mean = makeFloatSlider(gui, -4.0f, 4.0f, conf.getFloat("heightMean"), getTranslation("mean_height"));
                this.spacing = makePositiveExponentialSlider(gui, -1f, 6.0f, conf.getFloat("heightSpacing"), getTranslation("spacing_height"));
                this.stdDev = makeFloatSlider(gui, 0f, 1f, conf.getFloat("heightStdDeviation"), getTranslation("height_std_dev"));
            } else {
                this.mean = null;
                this.spacing = null;
                this.stdDev = null;
            }
            this.probability = makeFloatSlider(gui, malisisText("spawn_probability", " %.3f"), conf.getFloat("spawnProbability"));
            this.selectBiomes = makeCheckbox(gui, malisisText("select_biomes"), !conf.get("biomes").equals(JsonNull.INSTANCE));
            this.heightRange = makeOreHeightSlider(gui, vanillaText("spawn_range"), -2.0f, 2.0f,
                    conf.getFloat("minHeight"), conf.getFloat("maxHeight"), baseHeight, heightVariation);

            UISplitLayout<?> deleteTypeArea =
                    new UISplitLayout<>(gui, Type.STACKED, delete, type).sizeWeights(1, 1).setSizeOf(UISplitLayout.Pos.SECOND, 10)
                            .setSize(0, 30);
            UIVerticalTableLayout<?> mainArea = new UIVerticalTableLayout<>(gui, 6).autoFitToContent(true);

            // use new ArrayList so it can be sorted
            biomesArea = new UIList<>(gui,
                    ForgeRegistries.BIOMES.getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()),
                    this::makeBiomeCheckbox);

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
            this.selectBiomes.register(new Object() {
                @Subscribe
                public void onClick(UICheckBox.CheckEvent evt) {
                    allowSelectBiomes(biomesArea, evt.isChecked());
                }
            });
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

        Function<Double, String> getTranslation(String mean_height) {
            return val -> I18n.format(vanillaText(mean_height),
                    String.format("%.3f", val), String.format("%.1f", val * heightVariation.getAsDouble()));
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
            mainArea.add(this.size, new GridLocation(0, ++y, 3));
            mainArea.add(this.attempts, new GridLocation(3, y, 3));
            mainArea.add(this.probability, new GridLocation(0, ++y, 3));
            mainArea.add(this.selectBiomes, new GridLocation(3, y, 3));
            if (this.genType == OreGenType.PERIODIC_GAUSSIAN) {
                mainArea.add(this.mean, new GridLocation(0, ++y, 3));
                mainArea.add(this.spacing, new GridLocation(3, y, 3));
                mainArea.add(this.stdDev, new GridLocation(0, ++y, 6));
            }
            mainArea.add(this.heightRange, new GridLocation(0, ++y, 6));
        }

        private void allowSelectBiomes(UIList<String, UICheckBox> biomes, boolean checked) {
            biomes.setVisible(checked);
            if (!biomes.isVisible()) {
                biomes.getData().forEach(e -> biomes.component(e).setChecked(true));
            }
        }

        private void setupBiomeArea(JsonObjectView conf, UIList<String, UICheckBox> biomesArea) {
            biomesArea.setRightPadding(6);

            if (!conf.get("biomes").equals(JsonNull.INSTANCE)) {
                conf.forEachString("biomes", b -> biomesArea.component(b).setChecked(true));
            }

            ((List<String>) biomesArea.getData()).sort((b1, b2) ->
                    biomesArea.component(b1).isChecked() && !biomesArea.component(b2).isChecked() ? 1 : 0
            );
        }

        private void setupThis(ExtraGui gui, UIComponent<?> deleteTypeArea, UIComponent<?> mainArea, UILayout<?> biomesArea) {
            UISplitLayout<?> split =
                    new UISplitLayout<>(gui, Type.SIDE_BY_SIDE, mainArea, biomesArea).sizeWeights(2, 1).autoFitToContent(true).userResizable(false);

            this.autoFitToContent(true);
            this.add(this.name, new GridLocation(1, 0, 4));
            this.add(this.block, new GridLocation(0, 0, 1));
            this.add(deleteTypeArea, new GridLocation(5, 0, 1));
            this.add(split, new GridLocation(0, 1, 6));
            biomesArea.setHeightFunc(() -> ((UIContainer<?>) Objects.requireNonNull(split.getFirst())).getContentHeight());
        }

        private UICheckBox makeBiomeCheckbox(String name) {
            Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(name));
            String text = biome == null ? name : String.format("%s (%s)", biome.getBiomeName(), biome.getRegistryName());
            return new UICheckboxNoAutoSize(getGui(), text);
        }

        private UIContainer<?> makeLabel(ExtraGui gui) {
            UIVerticalTableLayout<?> label = new UIVerticalTableLayout<>(gui, 1).setInsets(0, 0, 0, 0);
            updateLabel(gui, label);
            return label;
        }

        private void updateLabel(ExtraGui gui, UIComponent<?> label) {

            ((UIContainer<?>) label).removeAll();

            String name = block.getState().getBlockId();
            String props = block.getState().getProperties().entrySet().stream()
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
