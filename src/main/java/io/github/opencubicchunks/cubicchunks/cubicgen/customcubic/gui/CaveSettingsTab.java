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

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.*;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.HORIZONTAL_PADDING;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.google.common.eventbus.Subscribe;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.ExtraGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIBlockStateButton;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIList;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISplitLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIVerticalTableLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.JsonObjectView;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.JsonTransformer;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISlider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class CaveSettingsTab {

    private static final JsonTransformer<CaveSettingsTab.UICaveOptionEntry> WRITE_TO_JSON_TRANSFORM =
            JsonTransformer.<CaveSettingsTab.UICaveOptionEntry>builder("Write GUI state to json")
                    .valueTransform("caveBlock", (json, cave) -> CustomGenSettingsSerialization.MARSHALLER.serialize(cave.caveBlock.getState()))
                    .setPrimitive("caveRarity", cave -> cave.caveRarity.getValue())
                    .setPrimitive("maxInitNodes", cave -> cave.maxInitNodes.getValue())
                    .setPrimitive("largeNodeRarity", cave -> cave.largeNodeRarity.getValue())
                    .setPrimitive("largeNodeMaxBranches", cave -> cave.largeNodeMaxBranches.getValue())
                    .setPrimitive("bigCaveRarity", cave -> cave.bigCaveRarity.getValue())
                    .setPrimitive("caveSizeAdd", cave -> cave.caveSizeAdd.getValue())
                    .setPrimitive("steepStepRarity", cave -> cave.steepStepRarity.getValue())
                    .setPrimitive("flattenFactor", cave -> cave.flattenFactor.getValue())
                    .setPrimitive("steeperFlattenFactor", cave -> cave.steeperFlattenFactor.getValue())
                    .setPrimitive("directionChangeFactor", cave -> cave.directionChangeFactor.getValue())
                    .setPrimitive("prevHorizDirectionChangeWeight", cave -> cave.prevHorizDirectionChangeWeight.getValue())
                    .setPrimitive("prevVertDirectionChangeWeight", cave -> cave.prevVertDirectionChangeWeight.getValue())
                    .setPrimitive("maxAddDirectionChangeHoriz", cave -> cave.maxAddDirectionChangeHoriz.getValue())
                    .setPrimitive("maxAddDirectionChangeVert", cave -> cave.maxAddDirectionChangeVert.getValue())
                    .setPrimitive("carveStepRarity", cave -> cave.carveStepRarity.getValue())
                    .setPrimitive("caveFloorDepth", cave -> cave.caveFloorDepth.getValue())
                    .valueTransform("isBlockReplaceable", (json, cave) ->
                            cave.replacedBlocks.stream().map(button-> button.getState()).collect(Collector.of(
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
            .put("blockstate", JsonObjectView.empty().put("Name", "minecraft:air"))
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

    CaveSettingsTab(ExtraGui gui, JsonObjectView conf) {
        this.componentList = new ArrayList<>();
        UIList<UIComponent<?>, UIComponent<?>> layout = new UIList<>(gui, this.componentList, x -> x);
        layout.setPadding(HORIZONTAL_PADDING, 0);
        layout.setSize(UIComponent.INHERITED, UIComponent.INHERITED);

        layout.add(new UIButton(gui, malisisText("add_cave")).setAutoSize(false).setSize(UIComponent.INHERITED, 30).register(
                new Object() {
                    @Subscribe
                    public void onClick(UIButton.ClickEvent evt) {
                        componentList.add(1,
                                new CaveSettingsTab.UICaveOptionEntry(gui,
                                    JsonObjectView.of(DEFAULT_STANDARD_CAVE.clone()),
                                    toDelete -> removeEntry(toDelete)));
                    }
                }
        ));

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

    private class UICaveOptionEntry extends UIVerticalTableLayout<CaveSettingsTab.UICaveOptionEntry> {
        private UIBlockStateButton<?> caveBlock;
        private UILabel caveBlockLabel;
        private UISlider<Integer> caveRarity;
        private UISlider<Integer> maxInitNodes;
        private UISlider<Integer> largeNodeRarity;
        private UISlider<Integer> largeNodeMaxBranches;
        private UISlider<Integer> bigCaveRarity;
        private UISlider<Float> caveSizeAdd;
        private UISlider<Integer> steepStepRarity;
        private UISlider<Float> flattenFactor;
        private UISlider<Float> steeperFlattenFactor;
        private UISlider<Float> directionChangeFactor;
        private UISlider<Float> prevHorizDirectionChangeWeight;
        private UISlider<Float> prevVertDirectionChangeWeight;
        private UISlider<Float> maxAddDirectionChangeHoriz;
        private UISlider<Float> maxAddDirectionChangeVert;
        private UISlider<Integer> carveStepRarity;
        private UISlider<Float> caveFloorDepth;

        private final Set<UIBlockStateButton> replacedBlocks = new HashSet<>();
        UIVerticalTableLayout<?> replacedArea;
        private UIButton addReplaceableBtn;

        private final Consumer<CaveSettingsTab.UICaveOptionEntry> deleteFunc;
        private final UIButton deleteBtn;

        private JsonObjectView conf;

        UICaveOptionEntry(ExtraGui gui, JsonObjectView conf, Consumer<CaveSettingsTab.UICaveOptionEntry> deleteFunc) {
            super(gui, 1);
            this.conf = conf;

            this.deleteFunc = deleteFunc;
            this.add(deleteBtn = new UIButton(gui, malisisText("delete")).setSize(10, 20).setAutoSize(false),
                    new GridLocation(0, 0, 0));

            deleteBtn.register(new Object() {
                @Subscribe
                public void onDelete(UIButton.ClickEvent evt) {
                    deleteFunc.accept(UICaveOptionEntry.this);
                }
            });

            this.caveBlock = new UIBlockStateButton<>(gui,conf.getBlockState("caveBlock"));
            this.caveBlock.onClick(evnt->{
                UIBlockStateSelect.makeOverlay(gui, state -> {
                    caveBlock.setBlockState(new BlockStateDesc(state));
                    updateCaveLabel();
                }).display();
            });
            this.caveBlockLabel = new UILabel(gui, "");
            updateCaveLabel();

            this.caveRarity = makeIntSlider(gui, malisisText("cave_rarity", " %d"), 1, 128, conf.getInt("caveRarity"));
            this.maxInitNodes = makeIntSlider(gui, malisisText("max_init_nodes", " %d"), 1, 128, conf.getInt("maxInitNodes"));
            this.largeNodeRarity = makeIntSlider(gui, malisisText("large_node_rarity", " %d"), 1, 128, conf.getInt("largeNodeRarity"));
            this.largeNodeMaxBranches = makeIntSlider(gui, malisisText("large_node_max_branches", " %d"), 1, 256, conf.getInt("largeNodeMaxBranches"));
            this.bigCaveRarity = makeIntSlider(gui, malisisText("big_cave_rarity", " %d"), 1, 128, conf.getInt("bigCaveRarity"));
            this.caveSizeAdd = makeFloatSlider(gui, malisisText("cave_size_add", " %f"), 0, 16, conf.getFloat("caveSizeAdd"));
            this.steepStepRarity = makeIntSlider(gui, malisisText("steep_step_rarity", " %d"), 1, 128, conf.getInt("steepStepRarity"));
            this.flattenFactor = makeFloatSlider(gui, malisisText("flatten_factor", " %f"), 0, 1, conf.getFloat("flattenFactor"));
            this.steeperFlattenFactor = makeFloatSlider(gui, malisisText("steeper_flatten_factor", " %f"), 0, 1, conf.getFloat(
                    "steeperFlattenFactor"));
            this.directionChangeFactor = makeFloatSlider(gui, malisisText("direction_change_factor",
                    " %f"),0, 1, conf.getFloat("directionChangeFactor"));
            this.prevHorizDirectionChangeWeight = makeFloatSlider(gui, malisisText(
                    "prev_horiz_direction_change_weight", " %f"), 0, 1, conf.getFloat("prevHorizDirectionChangeWeight"));
            this.prevVertDirectionChangeWeight = makeFloatSlider(gui, malisisText(
                    "prev_vert_direction_change_weight", " %f"), 0, 1, conf.getFloat("prevVertDirectionChangeWeight"));
            this.maxAddDirectionChangeHoriz = makeFloatSlider(gui, malisisText(
                    "max_add_direction_change_horiz", " %f"), 0, 128, conf.getFloat("maxAddDirectionChangeHoriz"));
            this.maxAddDirectionChangeVert = makeFloatSlider(gui, malisisText(
                    "max_add_direction_change_vert", " %f"), 0, 128, conf.getFloat("maxAddDirectionChangeVert"));
            this.carveStepRarity = makeIntSlider(gui, malisisText("carve_step_rarity", " %d"), 1, 128, conf.getInt("carveStepRarity"));
            this.caveFloorDepth = makeFloatSlider(gui, malisisText("cave_floor_depth", " %f"), -1, 1, conf.getFloat("caveFloorDepth"));

            this.addReplaceableBtn = new UIButton(gui, malisisText("cave_replacer")).setSize(10, 20).setAutoSize(true).register(
                    new Object() {
                        @Subscribe
                        public void onClick(UIButton.ClickEvent evt) {
                            UIBlockStateSelect.makeOverlay(gui, state -> {
                                addReplaceableBlock(gui,new BlockStateDesc(state));
                            }).display();
                        }
                    }
            );


            UIVerticalTableLayout<?> mainArea = new UIVerticalTableLayout<>(gui, 6).autoFitToContent(true);
            replacedArea = new UIVerticalTableLayout<>(gui, 1).setLeftPadding(5);

            setupMainArea(mainArea);
            setupReplacedArea(gui, conf);
            setupSharedArea(gui,mainArea,replacedArea);
        }

        private void setupMainArea(UIVerticalTableLayout<?> mainArea) {
            int y = -1;
            mainArea.add(this.caveBlock, new GridLocation(0,++y,1));
            mainArea.add(this.caveBlockLabel, new GridLocation(1,y,4));
            mainArea.add(this.caveRarity, new GridLocation(0, ++y, 3));
            mainArea.add(this.maxInitNodes, new GridLocation(3, y, 3));
            mainArea.add(this.largeNodeRarity, new GridLocation(0, ++y, 3));
            mainArea.add(this.largeNodeMaxBranches, new GridLocation(3, y, 3));
            mainArea.add(this.bigCaveRarity, new GridLocation(0, ++y, 3));
            mainArea.add(this.caveSizeAdd, new GridLocation(3, y, 3));
            mainArea.add(this.steepStepRarity, new GridLocation(0, ++y, 3));
            mainArea.add(this.flattenFactor, new GridLocation(3, y, 3));
            mainArea.add(this.steeperFlattenFactor, new GridLocation(0, ++y, 3));
            mainArea.add(this.carveStepRarity, new GridLocation(3, y, 3));
            mainArea.add(this.directionChangeFactor, new GridLocation(0, ++y, 6));
            mainArea.add(this.prevHorizDirectionChangeWeight, new GridLocation(0, ++y, 6));
            mainArea.add(this.prevVertDirectionChangeWeight, new GridLocation(0, ++y, 6));
            mainArea.add(this.maxAddDirectionChangeHoriz, new GridLocation(0, ++y, 6));
            mainArea.add(this.maxAddDirectionChangeVert, new GridLocation(0, ++y, 6));
            mainArea.add(this.caveFloorDepth, new GridLocation(0, ++y, 3));
            mainArea.add(this.addReplaceableBtn, new GridLocation(5, y, 1));
            mainArea.add(this.deleteBtn, new GridLocation(3,y,2));
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
            UISplitLayout<?> split =
                    new UISplitLayout<>(gui, UISplitLayout.Type.SIDE_BY_SIDE, mainArea, replacedArea).sizeWeights(8, 1).autoFitToContent(true).userResizable(false).setBottomPadding(10);
            this.autoFitToContent(true);
            this.add(split);
            replacedArea.setHeightFunc(() -> ((UIContainer<?>) Objects.requireNonNull(split.getFirst())).getContentHeight());
        }

        private void addReplaceableBlock(ExtraGui gui, BlockStateDesc blockState) {
            for (UIBlockStateButton replacedBlock : replacedBlocks) {
                if(replacedBlock.getState().getBlockState()==(blockState.getBlockState())) {return;}
            }
            UIBlockStateButton<?> newButton = new UIBlockStateButton(gui, blockState) {

            };
            newButton.onClick((evt)-> {
                removeReplaceableBlock(newButton);
                UIBlockStateSelect.makeOverlay(gui, state -> {
                    addReplaceableBlock(gui,new BlockStateDesc(state));
                }).display();
            });
            newButton.onRightClick((evt)-> {
                removeReplaceableBlock(newButton);
            });
            replacedBlocks.add(newButton);
            replacedArea.add(newButton);
        }

        private void removeReplaceableBlock(UIBlockStateButton<?> button) {
            replacedArea.remove(button);
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
            String name = caveBlock.getState().getBlockId();
            String props = caveBlock.getState().getProperties().entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .reduce((a, b) -> a + ", " + b).orElse("");

            caveBlockLabel.setText(String.format("%s   [%s]", name, props));
        }
    }
}
