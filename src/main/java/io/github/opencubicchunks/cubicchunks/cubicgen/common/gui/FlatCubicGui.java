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
package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui;

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.label;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.wrap;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.vanillaText;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiLabel.Type.CENTERED;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiButton;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiLabel;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.FlatGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIBorderLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIColoredPanel;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UITabbedContainer;
import mcp.MethodsReturnNonnullByDefault;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.minecraft.client.gui.GuiCreateWorld;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlatCubicGui extends ExtraGui {

    public final GuiCreateWorld parent;
    public static final int WIDTH_1_COL = 6;
    public static final int WIDTH_2_COL = 3;
    public static final int WIDTH_3_COL = 2;

    static final int VERTICAL_PADDING = 30;
    static final int HORIZONTAL_PADDING = 25;
    static final int VERTICAL_INSETS = 2;
    static final int HORIZONTAL_INSETS = 4;
    static final int BTN_WIDTH = 60;

    private FlatLayersTab layersTab;
    private UITabbedContainer tabs;
    private FlatGeneratorSettings conf;

    public FlatCubicGui(GuiCreateWorld parent) {
        super();
        this.parent = parent;
        this.conf = FlatGeneratorSettings.fromJson(parent.chunkProviderSettingsJson);
    }

    /**
     * Called before display() if this {@link MalisisGui} is not constructed
     * yet.<br> Called when Ctrl+R is pressed to rebuild the GUI.
     */
    @Override
    public void construct() {
        this.layersTab = new FlatLayersTab(this, conf);
        tabs = makeTabContainer();
        UIContainer<?> layersPanel = inPanel(layersTab.getContainer());
        layersTab.setInPanel(layersPanel);
        tabs.addTab(layersPanel, vanillaText("layers_title"));
        addToScreen(tabs);
    }

    private UIContainer<?> inPanel(UIComponent<?> comp) {
        UIColoredPanel panel = new UIColoredPanel(this);
        panel.setSize(UIComponent.INHERITED, UIComponent.INHERITED - VERTICAL_PADDING * 2);
        panel.setPosition(0, VERTICAL_PADDING);
        panel.add(comp);
        return panel;
    }

    private UITabbedContainer makeTabContainer() {
        final int xSize = UIComponent.INHERITED - HORIZONTAL_PADDING * 2 - HORIZONTAL_INSETS * 2;
        final int ySize = VERTICAL_PADDING;
        final int xPos = HORIZONTAL_PADDING + HORIZONTAL_INSETS;
        CwgGuiButton prev = CwgGuiFactory.button( "previous_page");
        CwgGuiButton next = CwgGuiFactory.button( "next_page");
        prev.setWidth(BTN_WIDTH);
        next.setWidth(BTN_WIDTH);

        CwgGuiButton done = CwgGuiFactory.button( "done", btn -> FlatCubicGui.this.done());
        done.setWidth(BTN_WIDTH);

        CwgGuiLabel label = label(CENTERED);

        UIBorderLayout upperLayout = new UIBorderLayout(this)
                .setSize(xSize, ySize)
                .setPosition(xPos, 0)
                .add(wrap(this, prev), UIBorderLayout.Border.LEFT)
                .add(wrap(this, next), UIBorderLayout.Border.RIGHT)
                .add(wrap(this, label), UIBorderLayout.Border.CENTER);

        UIBorderLayout lowerLayout = new UIBorderLayout(this)
                .setSize(xSize, ySize)
                .setAnchor(Anchor.BOTTOM).setPosition(xPos, 0)
                .add(wrap(this, done), UIBorderLayout.Border.CENTER);

        UITabbedContainer tabGroup = new UITabbedContainer(this, prev, next, label::setTranslationKey);
        tabGroup.add(upperLayout, lowerLayout);

        return tabGroup;
    }

    private void done() {
        saveConfig();
        this.mc.displayGuiScreen(parent);
    }

    public void saveConfig() {
        this.layersTab.writeToConf(conf);
        parent.chunkProviderSettingsJson = conf.toJson();
    }
}
