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
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.makeIntSlider;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.makeSlider;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.wrap;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.*;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.HORIZONTAL_PADDING;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.VERTICAL_INSETS;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common.IUIContainer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.ExtraGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiBlockStateButton;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiButton;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiSlider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIList;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIRangeSlider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISplitLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIVerticalTableLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.WrappedVanillaComponent;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.JsonObjectView;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.JsonTransformer;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.stream.Collector;

// TODO: redesign this UI
public class CaveSettingsTab {

    private static final JsonTransformer<UICaveOptionEntry> WRITE_TO_JSON_TRANSFORM =
            JsonTransformer.<UICaveOptionEntry>builder("Write GUI state to json")
                    .valueTransform("caveBlock", (json, cave) -> CustomGenSettingsSerialization.MARSHALLER.serialize(cave.caveBlock.getBlockState()))
                    .setPrimitive("caveRarity", cave -> cave.caveRarity.getSliderValue())
                    .setPrimitive("caveMinHeight", cave -> cave.caveCubeHeight.getMinValue().intValue())
                    .setPrimitive("caveMaxHeight", cave -> cave.caveCubeHeight.getMaxValue().intValue())
                    .setPrimitive("maxInitNodes", cave -> cave.maxInitNodes.getSliderValue())
                    .setPrimitive("largeNodeRarity", cave -> cave.largeNodeRarity.getSliderValue())
                    .setPrimitive("largeNodeMaxBranches", cave -> cave.largeNodeMaxBranches.getSliderValue())
                    .setPrimitive("bigCaveRarity", cave -> cave.bigCaveRarity.getSliderValue())
                    .setPrimitive("caveSizeAdd", cave -> cave.caveSizeAdd.getSliderValue())
                    .setPrimitive("steepStepRarity", cave -> cave.steepStepRarity.getSliderValue())
                    .setPrimitive("flattenFactor", cave -> cave.flattenFactor.getSliderValue())
                    .setPrimitive("steeperFlattenFactor", cave -> cave.steeperFlattenFactor.getSliderValue())
                    .setPrimitive("directionChangeFactor", cave -> cave.directionChangeFactor.getSliderValue())
                    .setPrimitive("prevHorizDirectionChangeWeight", cave -> cave.prevHorizDirectionChangeWeight.getSliderValue())
                    .setPrimitive("prevVertDirectionChangeWeight", cave -> cave.prevVertDirectionChangeWeight.getSliderValue())
                    .setPrimitive("maxAddDirectionChangeHoriz", cave -> cave.maxAddDirectionChangeHoriz.getSliderValue())
                    .setPrimitive("maxAddDirectionChangeVert", cave -> cave.maxAddDirectionChangeVert.getSliderValue())
                    .setPrimitive("carveStepRarity", cave -> cave.carveStepRarity.getSliderValue())
                    .setPrimitive("caveFloorDepth", cave -> cave.caveFloorDepth.getSliderValue())
                    .valueTransform("isBlockReplaceable", (json, cave) ->
                            cave.replacedBlocks.stream().map(CwgGuiBlockStateButton::getBlockState).collect(Collector.of(
                                    JsonArray::new,
                                    (array, block) -> array.add(CustomGenSettingsSerialization.MARSHALLER.serialize(block)),
                                    (arr1, arr2) -> {
                                        JsonArray arr = new JsonArray();
                                        arr.addAll(arr1);
                                        arr.addAll(arr2);
                                        return arr;
                                    })))
                    .build();

    private static final JsonObject DEFAULT_STANDARD_CAVE = JsonObjectView.empty()
            .put("caveBlock", JsonObjectView.empty().put("Name", "minecraft:air"))
            .put("caveMinHeight", Integer.MIN_VALUE / 16)
            .put("caveMaxHeight", Integer.MAX_VALUE / 16)
            .put("caveRarity", 16 * 7 / (2 * 2 * 2))
            .put("maxInitNodes", 14)
            .put("largeNodeRarity", 4)
            .put("largeNodeMaxBranches", 4)
            .put("bigCaveRarity", 10)
            .put("caveSizeAdd", 1.5)
            .put("steepStepRarity", 6)
            .put("flattenFactor", 0.699999988079071)
            .put("steeperFlattenFactor", 0.9200000166893005)
            .put("directionChangeFactor", 0.10000000149011612)
            .put("prevHorizDirectionChangeWeight", 0.75)
            .put("prevVertDirectionChangeWeight", 0.8999999761581421)
            .put("maxAddDirectionChangeHoriz", 4.0)
            .put("maxAddDirectionChangeVert", 2.0)
            .put("carveStepRarity", 4)
            .put("caveFloorDepth", -0.7)
            .put("isBlockReplaceable", JsonObjectView.JsonArrayView.empty().add(new JsonPrimitive("grass"))
                    .add(new JsonPrimitive("dirt"))
                    .add(new JsonPrimitive("stone")
                    ))
            .object();

    private final UIContainer<?> container;

    private final ArrayList<UIComponent<?>> componentList;

    private final DoubleSupplier baseHeight;
    private final DoubleSupplier heightVariation;

    CaveSettingsTab(ExtraGui gui, JsonObjectView conf, DoubleSupplier baseHeight, DoubleSupplier heightVariation) {
        this.baseHeight = baseHeight;
        this.heightVariation = heightVariation;
        this.componentList = new ArrayList<>();
        UIList<UIComponent<?>, UIComponent<?>> layout = new UIList<>(gui, this.componentList, x -> x);
        layout.setPadding(HORIZONTAL_PADDING, VERTICAL_INSETS);
        layout.setSize(UIComponent.INHERITED, UIComponent.INHERITED);

        layout.add(wrap(gui, makeButton("add_cave", btn ->
                componentList.add(1, new UICaveOptionEntry(gui, JsonObjectView.of(DEFAULT_STANDARD_CAVE.clone()), this::removeEntry))
        )));

        for (JsonObjectView cave : conf.objectArray("caves")) {
            layout.add(new UICaveOptionEntry(gui, cave, this::removeEntry));
        }

        layout.setRightPadding(HORIZONTAL_PADDING + 6);
        this.container = layout;
    }

    private void removeEntry(UICaveOptionEntry entry) {
        this.componentList.remove(entry);
    }

    UIContainer<?> getContainer() {
        return container;
    }

    void writeConfig(JsonObjectView conf) {
        List<Integer> exist = new ArrayList<>();

        for (UIComponent<?> c : componentList) {
            if (!(c instanceof UICaveOptionEntry)) {
                continue;
            }
            UICaveOptionEntry entry = (UICaveOptionEntry) c;
            int idx = entry.writeJson(conf);
            exist.add(idx);
        }

        JsonObjectView.JsonArrayView<JsonObjectView> old = conf.objectArray("caves");

        JsonArray newArray = new JsonArray();

        for (int idx : exist) {
            newArray.add(old.value(idx).object(), old.comment(idx));
        }
        conf.put("caves", newArray);

    }

    private class UICaveOptionEntry extends UIVerticalTableLayout<UICaveOptionEntry> {

        private final CwgGuiBlockStateButton caveBlock;
        private final UILabel caveBlockLabel;
        private final UIRangeSlider<Float> caveCubeHeight;
        private final CwgGuiSlider caveRarity;
        private final CwgGuiSlider maxInitNodes;
        private final CwgGuiSlider largeNodeRarity;
        private final CwgGuiSlider largeNodeMaxBranches;
        private final CwgGuiSlider bigCaveRarity;
        private final CwgGuiSlider caveSizeAdd;
        private final CwgGuiSlider steepStepRarity;
        private final CwgGuiSlider flattenFactor;
        private final CwgGuiSlider steeperFlattenFactor;
        private final CwgGuiSlider directionChangeFactor;
        private final CwgGuiSlider prevHorizDirectionChangeWeight;
        private final CwgGuiSlider prevVertDirectionChangeWeight;
        private final CwgGuiSlider maxAddDirectionChangeHoriz;
        private final CwgGuiSlider maxAddDirectionChangeVert;
        private final CwgGuiSlider carveStepRarity;
        private final CwgGuiSlider caveFloorDepth;

        private final Set<CwgGuiBlockStateButton> replacedBlocks = new HashSet<>();
        UIVerticalTableLayout<?> replacedArea;
        private final CwgGuiButton addReplaceableBtn;

        private final CwgGuiButton deleteBtn;

        private JsonObjectView conf;

        UICaveOptionEntry(ExtraGui gui, JsonObjectView conf, Consumer<UICaveOptionEntry> deleteFunc) {
            super(gui, 1);
            this.conf = conf;

            deleteBtn = makeButton("delete", btn -> deleteFunc.accept(UICaveOptionEntry.this));
            deleteBtn.setWidth(10);

            this.caveBlock = new CwgGuiBlockStateButton(conf.getBlockState("caveBlock"));
            this.caveBlock.onClick(evnt -> {
                UIBlockStateSelect.makeOverlay(gui, state -> {
                    caveBlock.setBlockState(new BlockStateDesc(state));
                    updateCaveLabel();
                }).display();
            });
            this.caveBlockLabel = new UILabel(gui, "");
            updateCaveLabel();

            this.caveRarity = makeIntSlider(1, 128, conf.getInt("caveRarity"), "cave_rarity");
            this.caveCubeHeight = makeRangeSlider(gui, vanillaText("cave_cube_height"),
                    (float) (-2f * (heightVariation.getAsDouble() / 4) + (baseHeight.getAsDouble() / 8)) / 100,
                    (float) (2f * (heightVariation.getAsDouble() / 8) + (baseHeight.getAsDouble() / 16)) / 100,
                    conf.getFloat("caveMinHeight"), conf.getFloat("caveMaxHeight"));
            this.maxInitNodes = makeIntSlider(1, 128, conf.getInt("maxInitNodes"), "max_init_nodes");
            this.largeNodeRarity = makeIntSlider(1, 128, conf.getInt("largeNodeRarity"), "large_node_rarity");
            this.largeNodeMaxBranches = makeIntSlider(1, 256, conf.getInt("largeNodeMaxBranches"), "large_node_max_branches");
            this.bigCaveRarity = makeIntSlider(1, 128, conf.getInt("bigCaveRarity"), "big_cave_rarity");
            this.caveSizeAdd = makeSlider(0, 16, conf.getFloat("caveSizeAdd"), "cave_size_add");
            this.steepStepRarity = makeIntSlider(1, 128, conf.getInt("steepStepRarity"), "steep_step_rarity");
            this.flattenFactor = makeSlider(0, 1, conf.getFloat("flattenFactor"), "flatten_factor");
            this.steeperFlattenFactor = makeSlider(0, 1, conf.getFloat("steeperFlattenFactor"), "steeper_flatten_factor");
            this.directionChangeFactor = makeSlider(0, 1, conf.getFloat("directionChangeFactor"), "direction_change_factor");
            this.prevHorizDirectionChangeWeight =
                    makeSlider(0, 1, conf.getFloat("prevHorizDirectionChangeWeight"), "prev_horiz_direction_change_weight");
            this.prevVertDirectionChangeWeight =
                    makeSlider(0, 1, conf.getFloat("prevVertDirectionChangeWeight"), "prev_vert_direction_change_weight");
            this.maxAddDirectionChangeHoriz = makeSlider(0, 128, conf.getFloat("maxAddDirectionChangeHoriz"), "max_add_direction_change_horiz");
            this.maxAddDirectionChangeVert = makeSlider(0, 128, conf.getFloat("maxAddDirectionChangeVert"), "max_add_direction_change_vert");
            this.carveStepRarity = makeIntSlider(1, 128, conf.getInt("carveStepRarity"), "carve_step_rarity");
            this.caveFloorDepth = makeSlider(-1, 1, conf.getFloat("caveFloorDepth"), "cave_floor_depth");

            this.addReplaceableBtn = makeButton("cave_replacer", btn ->
                    UIBlockStateSelect.makeOverlay(gui, state -> {
                        addReplaceableBlock(gui, new BlockStateDesc(state));
                    }).display()
            );


            UIVerticalTableLayout<?> mainArea = new UIVerticalTableLayout<>(gui, 6).autoFitToContent(true);
            replacedArea = new UIVerticalTableLayout<>(gui, 1).setLeftPadding(5);

            setupMainArea(mainArea);
            setupReplacedArea(gui, conf);
            setupSharedArea(gui, mainArea, replacedArea);
        }

        private void setupMainArea(UIVerticalTableLayout<?> mainArea) {
            int y = -1;
            mainArea.add(wrap(getGui(), this.caveBlock), new GridLocation(0, ++y, 1));
            mainArea.add(this.caveBlockLabel, new GridLocation(1, y, 4));
            mainArea.add(this.caveCubeHeight, new GridLocation(0, ++y, 6));
            mainArea.add(wrap(getGui(), this.caveRarity), new GridLocation(0, ++y, 3));
            mainArea.add(wrap(getGui(), this.maxInitNodes), new GridLocation(3, y, 3));
            mainArea.add(wrap(getGui(), this.largeNodeRarity), new GridLocation(0, ++y, 3));
            mainArea.add(wrap(getGui(), this.largeNodeMaxBranches), new GridLocation(3, y, 3));
            mainArea.add(wrap(getGui(), this.bigCaveRarity), new GridLocation(0, ++y, 3));
            mainArea.add(wrap(getGui(), this.caveSizeAdd), new GridLocation(3, y, 3));
            mainArea.add(wrap(getGui(), this.steepStepRarity), new GridLocation(0, ++y, 3));
            mainArea.add(wrap(getGui(), this.flattenFactor), new GridLocation(3, y, 3));
            mainArea.add(wrap(getGui(), this.steeperFlattenFactor), new GridLocation(0, ++y, 3));
            mainArea.add(wrap(getGui(), this.carveStepRarity), new GridLocation(3, y, 3));
            mainArea.add(wrap(getGui(), this.directionChangeFactor), new GridLocation(0, ++y, 6));
            mainArea.add(wrap(getGui(), this.prevHorizDirectionChangeWeight), new GridLocation(0, ++y, 6));
            mainArea.add(wrap(getGui(), this.prevVertDirectionChangeWeight), new GridLocation(0, ++y, 6));
            mainArea.add(wrap(getGui(), this.maxAddDirectionChangeHoriz), new GridLocation(0, ++y, 6));
            mainArea.add(wrap(getGui(), this.maxAddDirectionChangeVert), new GridLocation(0, ++y, 6));
            mainArea.add(wrap(getGui(), this.caveFloorDepth), new GridLocation(0, ++y, 3));
            mainArea.add(wrap(getGui(), this.addReplaceableBtn), new GridLocation(3, y, 2));
            mainArea.add(wrap(getGui(), this.deleteBtn), new GridLocation(5, y, 1));
        }

        private void setupReplacedArea(ExtraGui gui, JsonObjectView conf) {
            if (!conf.get("isBlockReplaceable").equals(JsonNull.INSTANCE)) {
                conf.objectArray("isBlockReplaceable").array().forEach(obj -> {
                    BlockStateDesc block = CustomGenSettingsSerialization.deserializeBlockstate(obj, null);
                    addReplaceableBlock(gui, block);
                });
            }
        }

        private void setupSharedArea(ExtraGui gui, UIVerticalTableLayout<?> mainArea, UIVerticalTableLayout<?> replacedArea) {
            UISplitLayout<?> split = new UISplitLayout<>(gui, UISplitLayout.Type.SIDE_BY_SIDE, mainArea, replacedArea)
                    .autoFitToContent(true)
                    .setSizeOf(UISplitLayout.Pos.SECOND, CwgGuiBlockStateButton.PADDED_SIZE)
                    .userResizable(false)
                    .setRightPadding(4)
                    .setBottomPadding(10);
            this.autoFitToContent(true);
            this.add(split);
            replacedArea.setHeightFunc(() -> ((UIContainer<?>) Objects.requireNonNull(split.getFirst())).getContentHeight());
        }

        private void addReplaceableBlock(ExtraGui gui, BlockStateDesc blockState) {
            for (CwgGuiBlockStateButton replacedBlock : replacedBlocks) {
                if (replacedBlock.getBlockState().getBlockState() == (blockState.getBlockState())) {
                    return;
                }
            }
            CwgGuiBlockStateButton newButton = new CwgGuiBlockStateButton(blockState);
            newButton.onClick((evt) -> {
                removeReplaceableBlock(newButton);
                UIBlockStateSelect.makeOverlay(gui, state -> {
                    addReplaceableBlock(gui, new BlockStateDesc(state));
                }).display();
            });
            replacedBlocks.add(newButton);
            replacedArea.add(wrap(gui, newButton));
        }

        private void removeReplaceableBlock(CwgGuiBlockStateButton button) {
            // TODO: this is slow
            UIComponent<?> component = ((IUIContainer) replacedArea).getComponents().stream()
                    .filter(x -> x instanceof WrappedVanillaComponent && ((WrappedVanillaComponent<?>) x).get() == button)
                    .findAny().get();
            replacedArea.remove(component);
            replacedBlocks.remove(button);
        }

        int writeJson(JsonObjectView rootJson) {
            JsonArray caveList = rootJson.objectArray("caves").array();

            int idx = -1;
            for (int i = 0; i < caveList.size(); i++) {
                if (caveList.get(i) == conf.object()) {
                    idx = i;
                    break;
                }
            }
            this.conf = JsonObjectView.of(WRITE_TO_JSON_TRANSFORM.transform(conf.object(), this));
            if (idx >= 0) {
                String comment = caveList.getComment(idx);
                caveList.set(idx, this.conf.object());
                caveList.setComment(idx, comment);
                return idx;
            } else {
                caveList.add(conf.object());
                return caveList.size() - 1;
            }
        }

        private void updateCaveLabel() {
            String name = caveBlock.getBlockState().getBlockId();
            String props = caveBlock.getBlockState().getProperties().entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .reduce((a, b) -> a + ", " + b).orElse("");

            caveBlockLabel.setText(String.format("%s   [%s]", name, props));
        }
    }
}
