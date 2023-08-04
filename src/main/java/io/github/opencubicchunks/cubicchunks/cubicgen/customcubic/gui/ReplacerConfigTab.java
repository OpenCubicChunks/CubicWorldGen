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

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.floatInput;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.malisisText;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.WIDTH_2_COL;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.ExtraGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UITextFieldFixed;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIVerticalTableLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.JsonObjectView;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.util.math.MathHelper;

public class ReplacerConfigTab {

    private static class Entry extends UIVerticalTableLayout<Entry> {

        /**
         * Default constructor, creates the components list.
         *
         * @param gui     the gui
         */
        public Entry(ExtraGui gui, String name) {
            super(gui, 3);

        }
    }

    private static class UIReplacerEntry<T extends UIReplacerEntry<T>> extends UIVerticalTableLayout<T> {

        final JsonObjectView config;
        UITextField minY;
        UITextField maxY;

        UIReplacerEntry(ExtraGui gui, JsonObjectView conf) {
            super(gui, 2);
            this.config = conf;
            init(gui);
        }

        private void init(ExtraGui gui) {
            this.removeAll();
            int gridY = -1;
            add(floatInput(gui, malisisText("minY"), this.minY = new UITextField(gui, ""), (int) config.getDouble("minY")),
                    new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 0, ++gridY, WIDTH_2_COL));
            add(floatInput(gui, malisisText("maxY"), this.maxY = new UITextField(gui, ""), (int) config.getDouble("maxY")),
                    new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 1, gridY, WIDTH_2_COL));
        }
    }
    private static class UIDensityRangeReplacerEntry extends UIReplacerEntry<UIDensityRangeReplacerEntry> {

        UIDensityRangeReplacerEntry(ExtraGui gui, JsonObjectView conf) {
            super(gui, conf);
            init(gui);
        }

        private void init(ExtraGui gui) {
            int gridY = 0;
            //        public BlockStateDesc blockInRange;
            //        public BlockStateDesc blockOutOfRange;
            //        public List<BlockStateDesc> filterBlocks = new ArrayList<>();
            //        public FilterType blockFilterType;
            //        public double minDensity;
            //        public double maxDensity;
            add(floatInput(gui, malisisText("minY"), this.minY = new UITextField(gui, ""), (int) config.getDouble("minY")),
                    new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 0, ++gridY, WIDTH_2_COL));
            add(floatInput(gui, malisisText("maxY"), this.maxY = new UITextField(gui, ""), (int) config.getDouble("maxY")),
                    new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 1, gridY, WIDTH_2_COL));
        }
    }
}
