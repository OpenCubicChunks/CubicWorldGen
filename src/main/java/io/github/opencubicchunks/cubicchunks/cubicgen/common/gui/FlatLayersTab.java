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
package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import io.github.opencubicchunks.cubicchunks.cubicgen.flat.FlatGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.flat.Layer;
import java.util.Comparator;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIFlatTerrainLayer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIVerticalTableLayout;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;

public class FlatLayersTab implements Comparator<UIFlatTerrainLayer> {

    public static final int VERTICAL_PADDING = 30;
    public static final int HORIZONTAL_PADDING = 25;
    public static final int VERTICAL_INSETS = 2;
    public static final int HORIZONTAL_INSETS = 4;

    private final FlatGeneratorSettings settings;
    private final List<UIFlatTerrainLayer> uiLayersList;
    private final FlatCubicGui gui;
    private UIContainer<?> layersPanel;

    FlatLayersTab(FlatCubicGui guiFor, FlatGeneratorSettings settings1) {
        this.settings = settings1;
        this.gui = guiFor;
        int i = settings.layers.entrySet().size();
        uiLayersList = new ArrayList<UIFlatTerrainLayer>(i);
        for (Entry<Integer, Layer> entry : settings.layers.entrySet()) {
            UIFlatTerrainLayer uiLayer = new UIFlatTerrainLayer(gui, this, entry.getValue());
            uiLayersList.add(uiLayer);
        }
        uiLayersList.sort(this);
    }

    public UIVerticalTableLayout generateLayout() {
        UIVerticalTableLayout<?> layout = new UIVerticalTableLayout<>(gui, 1);
        layout.setPadding(HORIZONTAL_PADDING, 0);
        layout.setSize(UIComponent.INHERITED, UIComponent.INHERITED)
                .setInsets(VERTICAL_INSETS, VERTICAL_INSETS, HORIZONTAL_INSETS, HORIZONTAL_INSETS);
        for (int i = 0; i < uiLayersList.size(); i++) {
            layout.add(uiLayersList.get(i),
                    new UIVerticalTableLayout.GridLocation(0, i, 1));
        }
        return layout;
    }

    public void regenerateLayout() {
        this.layersPanel.removeAll();
        this.layersPanel.add(generateLayout());
    }

    UIVerticalTableLayout getContainer() {
        return generateLayout();
    }

    public void remove(UIFlatTerrainLayer uiFlatTerrainLayer) {
        uiLayersList.remove(uiFlatTerrainLayer);
        regenerateLayout();
    }

    public void add(UIFlatTerrainLayer oldUIFlatTerrainLayer, Layer newLayer) {
        int oldIndex = uiLayersList.indexOf(oldUIFlatTerrainLayer);
        uiLayersList.add(oldIndex, new UIFlatTerrainLayer(gui, this, newLayer));
        regenerateLayout();
    }

    public void writeToConf(FlatGeneratorSettings conf) {
        conf.layers.clear();
        for (UIFlatTerrainLayer uiLayer : uiLayersList) {
            Layer layer = uiLayer.toLayer();
            conf.layers.put(layer.fromY, layer);
        }
    }

    @Override
    public int compare(UIFlatTerrainLayer o1, UIFlatTerrainLayer o2) {
        Layer l1 = o1.toLayer();
        Layer l2 = o2.toLayer();
        return l2.fromY - l1.fromY;
    }

    public void setInPanel(UIContainer<?> layersPanelIn) {
        layersPanel = layersPanelIn;
    }
}
