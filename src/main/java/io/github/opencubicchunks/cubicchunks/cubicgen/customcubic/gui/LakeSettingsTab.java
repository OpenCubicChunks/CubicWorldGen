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

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.makeButton;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.makeCheckBoxUnlocalized;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.wrap;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.*;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.HORIZONTAL_PADDING;
import static io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization.deserializeUserFunction;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.ExtraGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.GuiOverlay;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiButton;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiCheckBox;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIBlockStateButton;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIList;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIProbabilityDistributionEditor;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISplitLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIUserFunctionEdit;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIVerticalTableLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.JsonObjectView;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.JsonObjectView.JsonArrayView;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.JsonTransformer;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockDesc;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class LakeSettingsTab {

    private static final JsonTransformer<UILakeEntry> WRITE_TO_JSON_TRANSFORM =
            JsonTransformer.<UILakeEntry>builder("Write GUI state to json")
                    .setPrimitive("block", lake -> lake.blockstate.getBlockName())
                    .setPrimitive("biomeSelect", lake -> lake.biomeSelectMode.getSelectedValue().toString())
                    .valueTransform("biomes", (json, lake) -> lake.biomeCheckBoxes.keySet().stream()
                            .filter(CwgGuiCheckBox::isChecked)
                            .map(lake.biomeCheckBoxes::get)
                            .collect(Collector.of(
                                    JsonArray::new,
                                    (array, str) -> array.add(new JsonPrimitive(str)),
                                    (arr1, arr2) -> {
                                        JsonArray arr = new JsonArray();
                                        arr.addAll(arr1);
                                        arr.addAll(arr2);
                                        return arr;
                                    }
                            )))
                    .valueTransform("surfaceProbability",
                            (json, lake) -> CustomGenSettingsSerialization.serializeUserFunction(lake.graphMiniViewSurface.toUserFunction(), null))
                    .valueTransform("mainProbability",
                            (json, lake) -> CustomGenSettingsSerialization.serializeUserFunction(lake.graphMiniViewMain.toUserFunction(), null))
                    .build();

    private static final JsonObject DEFAULT_LAKE = JsonObjectView.empty()
            .put("block", "minecraft:tnt")
            .put("biomes", new JsonArray())
            .put("biomeSelect", "EXCLUDE")
            .put("surfaceProbability", JsonArrayView.empty()
                    .addObject(JsonObjectView.empty().put("y", 0.0).put("v", 0.3)))
            .put("mainProbability", JsonArrayView.empty()
                    .addObject(JsonObjectView.empty().put("y", 0.0).put("v", 0.2)))
            .object();

    private final UIList<UIComponent<?>, UIComponent<?>> container;

    private final List<UIComponent<?>> componentList;

    <T> LakeSettingsTab(ExtraGui gui, JsonObjectView conf) {
        this.componentList = new ArrayList<>();
        UIList<UIComponent<?>, UIComponent<?>> layout = new UIList<>(gui, this.componentList, x -> x);
        layout.setPadding(HORIZONTAL_PADDING, 0);
        layout.setSize(UIComponent.INHERITED, UIComponent.INHERITED);

        layout.add(wrap(gui, makeButton("add_lake", btn ->
                componentList.add(1, new UILakeEntry(gui, JsonObjectView.of(DEFAULT_LAKE.clone()), this::removeEntry))
        )));

        for (JsonObjectView lake : conf.objectArray("lakes")) {
            layout.add(new UILakeEntry(gui, lake, this::removeEntry));
        }
        layout.setRightPadding(HORIZONTAL_PADDING + 6);
        this.container = layout;

    }

    private void removeEntry(UILakeEntry entry) {
        this.componentList.remove(entry);
    }

    UIContainer<?> getContainer() {
        return container;
    }

    void writeConfig(JsonObjectView conf) {
        List<Integer> exist = new ArrayList<>();
        for (UIComponent<?> c : componentList) {
            if (!(c instanceof UILakeEntry)) {
                continue;
            }
            UILakeEntry entry = (UILakeEntry) c;
            int idx = entry.writeJson(conf);
            exist.add(idx);
        }
        exist.sort(Integer::compareTo);

        JsonObjectView.JsonArrayView<JsonObjectView> old = conf.objectArray("lakes");

        JsonArray newArray = new JsonArray();

        for (int idx : exist) {
            newArray.add(old.value(idx).object(), old.comment(idx));
        }
        conf.put("lakes", newArray);
    }

    private static class UILakeEntry extends UIVerticalTableLayout<UILakeEntry> {

        /*
        The layout:

        +------+------+------+------+------+------+
        |BLOCK : <=========NAME==========> :DELETE|
        |STATE : <=BLOCKSTATE PROPERTIES=> :      |
        +------+------+------+------+------+------+
        | <====== MAIN PROBABILITY LABEL =======> |
        | ----- mini graph view (clickable) ----- |
        +------+------+------+------+------+------+
        | <===== SURFACE PROBABILITY LABEL =====> |
        | ----- mini graph view (clickable) ----- |
        +------+------+------+------+------+------+
        | <===========BIOME SELECTION===========> |
        +------+------+------+------+------+------+
        |[SEL TYPE [V]| [V] A   [V] B   [V] C  /\ |
        | SELECT ALL  | [V] D   [V] E   [V] F  || |
        |   INVERT    | [V] G   [V] H   [V] I  \/ |
        +------+------+------+------+------+------+
        */

        private final UIBlockStateButton<?> blockstate;

        private final UIContainer<?> nameLabel;

        private final CwgGuiButton deleteBtn;


        private final UIComponent<?> mainProbabilityLabel;
        private final UIUserFunctionEdit graphMiniViewMain;
        private final UIComponent<?> surfaceProbabilityLabel;
        private final UIUserFunctionEdit graphMiniViewSurface;

        private final UIComponent<?> biomeSelectionLabel;

        private final UISplitLayout<?> biomeSelectionSplit;
        private final UISelect<CustomGeneratorSettings.FilterType> biomeSelectMode;
        private final CwgGuiButton selectAllBiomesBtn;
        private final CwgGuiButton invertBiomeSelection;
        private final UIVerticalTableLayout<?> biomeTable;
        private final Consumer<UILakeEntry> deleteFunc;
        private final ArrayList<String> biomes;
        private final Map<CwgGuiCheckBox, String> biomeCheckBoxes;

        private JsonObjectView conf;

        /**
         * Default constructor, creates the components list.
         *
         * @param gui the gui
         */
        public UILakeEntry(ExtraGui gui, JsonObjectView conf, Consumer<UILakeEntry> deleteFunc) {
            super(gui, 6);
            this.conf = conf;
            this.deleteFunc = deleteFunc;

            int gridY = 0;

            this.add(blockstate = new UIBlockStateButton<>(gui, new BlockDesc(conf.getString("block")).defaultState()), new GridLocation(0, gridY, 1));
            this.add(nameLabel = makeLabel(gui), new GridLocation(1, gridY, 4));
            this.add(wrap(gui, deleteBtn = makeButton("delete")), new GridLocation(5, gridY, 1));

            gridY++;
            this.add(mainProbabilityLabel = label(gui, malisisText("lakes.main_probability")), new GridLocation(0, gridY, 6));
            gridY++;
            this.add(graphMiniViewMain = new UIUserFunctionEdit(gui,
                            deserializeUserFunction(conf.objectArray("mainProbability").array(), null))
                            .setSize(INHERITED, 30).autoYLockWithMinMax(0, 0, 0.001, 1, 0.1),
                    new GridLocation(0, gridY, 6));

            gridY++;
            this.add(surfaceProbabilityLabel = label(gui, malisisText("lakes.surface_probability")), new GridLocation(0, gridY, 6));
            gridY++;
            this.add(graphMiniViewSurface = new UIUserFunctionEdit(gui,
                            deserializeUserFunction(conf.objectArray("surfaceProbability").array(), null))
                            .setSize(INHERITED, 30).autoYLockWithMinMax(0, 0, 0.001, 1, 0.1),
                    new GridLocation(0, gridY, 6));

            gridY++;
            this.add(biomeSelectionLabel = label(gui, malisisText("select_biomes_label")), new GridLocation(0, gridY, 6));

            UIContainer<?> biomeSelectionLeft = new UIVerticalTableLayout<>(gui, 1)
                    .setInsets(1, 1, 0, 0);
            biomeSelectionLeft.add(
                    biomeSelectMode = makeUISelect(gui, Arrays.asList(CustomGeneratorSettings.FilterType.values())),
                    wrap(gui, selectAllBiomesBtn = makeButton("select_all")),
                    wrap(gui, invertBiomeSelection = makeButton("invert_selection"))
            );
            biomeSelectMode.select(CustomGeneratorSettings.FilterType.valueOf(conf.getString("biomeSelect")));

            biomeTable = new UIVerticalTableLayout<>(gui, 3).setScrollbarOffset(-1);

            List<String> selectedBiomes = ((JsonArray) conf.get("biomes")).stream()
                    .map(e -> ((JsonPrimitive) e).asString())
                    .distinct().collect(Collectors.toCollection(ArrayList::new));
            biomes = new ArrayList<>(selectedBiomes);
            for (ResourceLocation biome : ForgeRegistries.BIOMES.getKeys()) {
                if (!biomes.contains(biome.toString())) {
                    biomes.add(biome.toString());
                }
            }
            biomeCheckBoxes = new HashMap<>();
            for (String biome : biomes) {
                CwgGuiCheckBox checkbox = makeBiomeCheckbox(biome, selectedBiomes.contains(biome));
                biomeCheckBoxes.put(checkbox, biome);
                biomeTable.add(wrap(gui, checkbox));
            }
            biomeSelectionSplit = new UISplitLayout<>(gui, UISplitLayout.Type.SIDE_BY_SIDE, biomeSelectionLeft, biomeTable);
            biomeSelectionSplit.setSize(10, 62);

            gridY++;
            this.add(biomeSelectionSplit, new GridLocation(0, gridY, 6));

            selectAllBiomesBtn.onClick(btn -> {
                for (CwgGuiCheckBox biomeCheckBox : biomeCheckBoxes.keySet()) {
                    biomeCheckBox.setIsChecked(true);
                }
            });

            invertBiomeSelection.onClick(btn -> {
                for (CwgGuiCheckBox biomeCheckBox : biomeCheckBoxes.keySet()) {
                    biomeCheckBox.setIsChecked(!biomeCheckBox.isChecked());
                }
            });

            blockstate.onClick(btn ->
                    UIBlockStateSelect.makeDefaultStatesOverlay(gui, state -> {
                        blockstate.setBlockState(new BlockStateDesc(state));
                        updateLabel(gui, nameLabel);
                    }).display()
            );

            deleteBtn.onClick(btn -> deleteFunc.accept(UILakeEntry.this));
            graphMiniViewMain.onClick(c -> {
                new GuiOverlay(gui,
                        overlay ->
                                new UIProbabilityDistributionEditor(overlay,
                                        graphMiniViewMain.toUserFunction(),
                                        graphMiniViewMain::setUserFunction)
                ).guiScreenAlpha(255).display();
            });

            graphMiniViewSurface.onClick(c -> {
                new GuiOverlay(gui,
                        overlay ->
                                new UIProbabilityDistributionEditor(overlay,
                                        graphMiniViewSurface.toUserFunction(),
                                        graphMiniViewSurface::setUserFunction)
                ).guiScreenAlpha(255).display();
            });
            this.autoFitToContent(true);
        }

        private CwgGuiCheckBox makeBiomeCheckbox(String biome, boolean checked) {
            String biomeName = Objects.requireNonNull(ForgeRegistries.BIOMES.getValue(new ResourceLocation(biome))).getBiomeName();
            return makeCheckBoxUnlocalized(biomeName, checked);
        }

        int writeJson(JsonObjectView rootJson) {
            JsonArray oreList = rootJson.objectArray("lakes").array();
            int idx = -1;
            for (int i = 0; i < oreList.size(); i++) {
                if (oreList.get(i) == conf.object()) {
                    idx = i;
                    break;
                }
            }
            this.conf = JsonObjectView.of(WRITE_TO_JSON_TRANSFORM.transform(conf.object(), this));
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

        private UIContainer<?> makeLabel(ExtraGui gui) {
            UIVerticalTableLayout<?> label = new UIVerticalTableLayout<>(gui, 1).setInsets(0, 0, 0, 0);
            updateLabel(gui, label);
            return label;
        }

        private void updateLabel(ExtraGui gui, UIContainer<?> label) {
            label.removeAll();
            UIComponent<?> l1 = label(gui, blockstate.getBlockName());
            UIComponent<?> l2 = label(gui, blockstate.getBlockProperties());
            label.add(l1, l2);
            label.setSize(label.getWidth(), l1.getHeight() + l2.getHeight());
        }
    }
}
