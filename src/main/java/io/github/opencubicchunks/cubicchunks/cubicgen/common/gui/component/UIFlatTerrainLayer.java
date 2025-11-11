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
package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component;

import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.init.Blocks;

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.button;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.intTextField;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.label;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.separator;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.wrap;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.FlatLayersTab.HORIZONTAL_INSETS;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.FlatLayersTab.VERTICAL_INSETS;

import io.github.opencubicchunks.cubicchunks.cubicgen.preset.FlatLayer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.FlatCubicGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.FlatLayersTab;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.UIBlockStateSelect;

// TODO: avoid actually extending layouts
public final class UIFlatTerrainLayer extends UIVerticalTableLayout<UIFlatTerrainLayer> {

    private static final int BTN_WIDTH = 90;
    private final FlatLayersTab flatLayersTab;
    private final CwgGuiButton addLayer;
    private final CwgGuiButton removeLayer;
    private final CwgGuiBlockStateButton block;
    private final CwgGuiLabel blockInfo;
    private final CwgGuiLabel from;
    private final CwgGuiLabel to;
    private final CwgGuiSeparator separator;
    private final GuiTextField fromField;
    private final GuiTextField toField;

    private final FlatCubicGui gui;

    public UIFlatTerrainLayer(FlatCubicGui guiFor, FlatLayersTab flatLayersTabFor, FlatLayer layer) {
        super(guiFor, 2);
        setInsets(VERTICAL_INSETS, VERTICAL_INSETS, HORIZONTAL_INSETS, HORIZONTAL_INSETS);
        this.flatLayersTab = flatLayersTabFor;
        this.gui = guiFor;

        this.block = new CwgGuiBlockStateButton(layer.blockState);
        this.block.onClick(btn -> UIBlockStateSelect.makeOverlay(gui, state -> {
            block.setBlockState(new BlockStateDesc(state));
            updateLabels();
        }).display());

        this.blockInfo = label();
        updateLabels();

        addLayer = button("add_layer");
        addLayer.onClick(btn -> addLayer());

        removeLayer = button("remove_layer");
        removeLayer.y = 20;
        removeLayer.onClick(btn -> removeLayer());

        from = label("from");
        to = label("to_exclusively");

        fromField = intTextField(layer.fromY);
        toField = intTextField(layer.toY);

        separator = separator();

        /*
        The layout:

                  left/right split, size to fit second
                                   v
        +-----------------------------------------+
        | BLOCKSTATE               |   ADD LAYER^ |
        |                          | REMOVE LAYER |
        +--------------------+--------------------+ < vertical table layout, 2 columns, top one takes 2 columns
        | From [     ]       | To [     ]         |
        +--------------------+--------------------+
        */

        UIContainer<?> blockstateContainer = new UIContainer<>(gui);
        blockstateContainer.add(wrap(gui, block));
        blockstateContainer.add(wrap(gui, blockInfo).setPosition(CwgGuiBlockStateButton.PADDED_SIZE, 0));
        blockstateContainer.setSize(0, CwgGuiBlockStateButton.PADDED_SIZE); // width set by layout

        UIVerticalTableLayout<?> buttonsContainer = new UIVerticalTableLayout<>(gui, 1).autoFitToContent(true);
        buttonsContainer.add(wrap(gui, addLayer), new GridLocation(0, 0, 1));
        buttonsContainer.add(wrap(gui, removeLayer), new GridLocation(0,  1, 1));

        UILayout<?> blockstateButtonsSplit = new UISplitLayout<>(gui, UISplitLayout.Type.SIDE_BY_SIDE, blockstateContainer, buttonsContainer)
                .userResizable(false).setSizeOf(UISplitLayout.Pos.SECOND, BTN_WIDTH).autoFitToContent(true);

        UISplitLayout<?> fromLayout = new UISplitLayout<>(gui, UISplitLayout.Type.SIDE_BY_SIDE,
                wrap(gui, from), wrap(gui, fromField)).setSizeOf(UISplitLayout.Pos.FIRST, 50).autoFitToContent(true);
        UISplitLayout<?> toLayout = new UISplitLayout<>(gui, UISplitLayout.Type.SIDE_BY_SIDE,
                wrap(gui, to), wrap(gui, toField)).setSizeOf(UISplitLayout.Pos.FIRST, 90).autoFitToContent(true);

        this.add(blockstateButtonsSplit, new GridLocation(0, 0, 2));
        this.add(fromLayout, new GridLocation(0, 1, 1));
        this.add(toLayout, new GridLocation(1, 1, 1));
        this.add(wrap(gui, separator), new GridLocation(0, 2, 2));
        this.autoFitToContent(true);

    }

    private void updateLabels() {
        blockInfo.setLines(block.getBlockName(), block.getBlockProperties());
    }

    protected void removeLayer() {
        this.flatLayersTab.remove(this);
    }

    protected void addLayer() {
        int to = Integer.parseInt(this.toField.getText());
        FlatLayer newLayer = new FlatLayer(to, to + 1, new BlockStateDesc(Blocks.SANDSTONE.getDefaultState()));
        this.flatLayersTab.add(this, newLayer);
    }

    public FlatLayer toLayer() {
        return new FlatLayer(Integer.parseInt(fromField.getText()), Integer.parseInt(toField.getText()), block.getBlockState());
    }
}
