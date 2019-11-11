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
package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.init.Blocks;

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.malisisText;

import com.google.common.eventbus.Subscribe;

import io.github.opencubicchunks.cubicchunks.cubicgen.flat.Layer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.FlatCubicGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.FlatLayersTab;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.UIBlockStateSelect;

public class UIFlatTerrainLayer extends UIContainer<UIFlatTerrainLayer> {

    private static final int BTN_WIDTH = 90;
    private final FlatLayersTab flatLayersTab;
    private final UIButton addLayer;
    private final UIButton removeLayer;
    private final UIBlockStateButton block;
    private final UILabel blockName;
    private final UILabel blockProperties;
    private final UILabel from;
    private final UILabel to;
    private final UISeparator separator;
    private final UIIntegerInputField fromField;
    private final UIIntegerInputField toField;
    private final FontOptions whiteFontWithShadow = FontOptions.builder().color(0xFFFFFF).shadow().build();

    private final FlatCubicGui gui;

    public UIFlatTerrainLayer(FlatCubicGui guiFor, FlatLayersTab flatLayersTabFor, Layer layer) {
        super(guiFor);
        this.setSize(UIComponent.INHERITED, 60);
        this.flatLayersTab = flatLayersTabFor;
        this.gui = guiFor;

        this.block = new UIBlockStateButton(gui, layer.blockState);
        this.blockName = new UILabel(gui).setPosition(30, 0).setFontOptions(whiteFontWithShadow);
        this.blockProperties = new UILabel(gui).setPosition(30, 10).setFontOptions(whiteFontWithShadow);
        this.block.onClick(btn -> new UIBlockStateSelect<>(gui).display(state -> {
            block.setBlockState(state);
            updateLabels();
        }));
        add(block);
        updateLabels();
        add(blockName);
        add(blockProperties);

        addLayer = new UIButton(gui, malisisText("add_layer")).setSize(BTN_WIDTH, 20).setPosition(0, 0)
                .setAnchor(Anchor.RIGHT).register(new Object() {

                    @Subscribe
                    public void onClick(UIButton.ClickEvent evt) {
                        UIFlatTerrainLayer.this.addLayer();
                    }
                });
        add(addLayer);

        removeLayer = new UIButton(gui, malisisText("remove_layer")).setSize(BTN_WIDTH, 20).setPosition(0, 20)
                .setAnchor(Anchor.RIGHT).register(new Object() {

                    @Subscribe
                    public void onClick(UIButton.ClickEvent evt) {
                        UIFlatTerrainLayer.this.removeLayer();
                    }
                });
        add(removeLayer);

        toField = (UIIntegerInputField) new UIIntegerInputField(gui, layer.toY).setPosition(0, 45, Anchor.RIGHT)
                .setSize(80, 5);
        add(toField);

        to = new UILabel(gui, malisisText("to_exclusively"), false)
                .setPosition(-10 - toField.getWidth(), 47, Anchor.RIGHT).setFontOptions(whiteFontWithShadow);
        add(to);

        from = new UILabel(gui, malisisText("from"), false).setPosition(0, 47).setFontOptions(whiteFontWithShadow);
        add(from);

        fromField = (UIIntegerInputField) new UIIntegerInputField(gui, layer.fromY)
                .setPosition(from.getWidth() + 10, 45).setSize(80, 5);
        add(fromField);

        separator = new UISeparator(gui, false).setColor(0x767676).setPosition(0, to.getY() + to.getHeight() + 3)
                .setSize(UIComponent.INHERITED, 1);
        super.add(separator);
    }

    private void updateLabels() {
        blockName.setText(block.getBlockName());
        blockProperties.setText(block.getBlockProperties());
    }

    protected void saveConfig() {
        this.gui.saveConfig();
    }

    protected void removeLayer() {
        this.flatLayersTab.remove(this);
    }

    protected void addLayer() {
        int to = this.toField.getValue();
        Layer newLayer = new Layer(to, to + 1, Blocks.SANDSTONE.getDefaultState());
        this.flatLayersTab.add(this, newLayer);
    }

    public Layer toLayer() {
        return new Layer(fromField.getValue(), toField.getValue(), block.getState());
    }
}
