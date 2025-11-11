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

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.checkBox;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.doubleTextField;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.label;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.positiveExponentialSlider;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.slider;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.symmetricExponentialSlider;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory.wrap;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.makeUISelect;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.malisisText;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiLabel.Type.CENTERED;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiLabel.Type.LEFT_ALIGN;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.HORIZONTAL_INSETS;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.HORIZONTAL_PADDING;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.VERTICAL_INSETS;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.WIDTH_1_COL;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.WIDTH_2_COL;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.WIDTH_3_COL;

import com.google.common.eventbus.Subscribe;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.CwgGuiFactory;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.ExtraGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiCheckBox;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiSlider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISplitLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIVerticalTableLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.JsonObjectView;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

class AdvancedTerrainShapeTab {

    private final UIContainer<?> container;

    private final CwgGuiCheckBox lockExpectedHeights;
    private final GuiTextField expectedBaseHeight;
    private final GuiTextField expectedHeightVariation;
    private final GuiTextField actualHeight;

    private final CwgGuiSlider heightVariationFactor;
    private final CwgGuiSlider heightVariationSpecialFactor;
    private final CwgGuiSlider heightVariationOffset;
    private final CwgGuiSlider heightFactor;
    private final CwgGuiSlider heightOffset;

    private final CwgGuiSlider depthNoisePeriodX;
    private final CwgGuiSlider depthNoisePeriodZ;
    private final CwgGuiSlider depthNoiseOctaves;
    private final CwgGuiSlider depthNoiseFactor;
    private final CwgGuiSlider depthNoiseOffset;

    private final CwgGuiSlider selectorNoisePeriodX;
    private final CwgGuiSlider selectorNoisePeriodY;
    private final CwgGuiSlider selectorNoisePeriodZ;
    private final CwgGuiSlider selectorNoiseOctaves;
    private final CwgGuiSlider selectorNoiseFactor;
    private final CwgGuiSlider selectorNoiseOffset;

    private final CwgGuiSlider lowNoisePeriodX;
    private final CwgGuiSlider lowNoisePeriodY;
    private final CwgGuiSlider lowNoisePeriodZ;
    private final CwgGuiSlider lowNoiseOctaves;
    private final CwgGuiSlider lowNoiseFactor;
    private final CwgGuiSlider lowNoiseOffset;

    private final CwgGuiSlider highNoisePeriodX;
    private final CwgGuiSlider highNoisePeriodY;
    private final CwgGuiSlider highNoisePeriodZ;
    private final CwgGuiSlider highNoiseOctaves;
    private final CwgGuiSlider highNoiseFactor;
    private final CwgGuiSlider highNoiseOffset;

    // preview
    private final CwgGuiCheckBox keepPreviewVisible;
    private final CwgGuiSlider biomeScaleSlider, biomeOffsetSlider;
    private final UISelect<EnumFacing.Axis> horizontalAxis;
    private final CwgGuiCheckBox lockXZ;
    private final CwgGuiCheckBox showPreview;
    private final DoubleSupplier getWaterLevel;

    AdvancedTerrainShapeTab(CustomCubicGui gui, JsonObjectView conf, DoubleSupplier getWaterLevel) {
        this.getWaterLevel = getWaterLevel;
        final float MAX_NOISE_FREQ_POWER = -4;

        int gridY = -1;

        UIVerticalTableLayout<?> table = new UIVerticalTableLayout(gui, 6);
        table.setPadding(HORIZONTAL_PADDING, 0);

        table.setInsets(VERTICAL_INSETS, VERTICAL_INSETS, HORIZONTAL_INSETS, HORIZONTAL_INSETS)
                .setRightPadding(6)

                //expected heights
                .add(wrap(gui, label(CENTERED, "expected_heights_group")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_1_COL * 0, gridY += 2, WIDTH_1_COL))
                .add(wrap(gui, this.lockExpectedHeights = checkBox("lock_expected_heights", true)),
                        new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 0, ++gridY, WIDTH_2_COL))
                .add(doubleInput(gui, "actual_height", this.actualHeight = doubleTextField(conf.getDouble("actualHeight"))),
                        new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 1, gridY, WIDTH_2_COL))
                .add(doubleInput(gui, "expected_base_height",
                        this.expectedBaseHeight = doubleTextField(conf.getDouble("expectedBaseHeight"))),
                        new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 0, ++gridY, WIDTH_2_COL))
                .add(doubleInput(gui, "expected_height_variation",
                        this.expectedHeightVariation = doubleTextField(conf.getDouble("expectedHeightVariation"))),
                        new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 1, gridY, WIDTH_2_COL))
                // height variation
                .add(wrap(gui, label(CENTERED, "height_variation_group")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_1_COL * 0, ++gridY, WIDTH_1_COL))
                .add(wrap(gui, this.heightVariationFactor = positiveExponentialSlider(
                                0, 20, conf.getDouble("heightVariationFactor"), "height_variation_factor_slider")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 0, ++gridY, WIDTH_3_COL))
                .add(wrap(gui, this.heightVariationSpecialFactor = positiveExponentialSlider(
                                -6, 6,
                                conf.getDouble("specialHeightVariationFactorBelowAverageY"), "height_variation_special_factor_slider")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 1, gridY, WIDTH_3_COL))
                .add(wrap(gui, this.heightVariationOffset = symmetricExponentialSlider(
                        0, 20, conf.getDouble("heightVariationOffset"), "height_variation_offset_slider")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 2, gridY, WIDTH_3_COL))

                // height
                .add(wrap(gui, label(CENTERED, "height_group")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_1_COL * 0, ++gridY, WIDTH_1_COL))
                .add(wrap(gui, this.heightFactor = symmetricExponentialSlider(
                        1, 20, conf.getDouble("heightFactor"), "height_factor")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 0, ++gridY, WIDTH_2_COL))
                .add(wrap(gui, this.heightOffset = symmetricExponentialSlider(
                        1, 20, conf.getDouble("heightOffset"), "height_offset")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 1, gridY, WIDTH_2_COL))

                // depth noise
                .add(wrap(gui, label(CENTERED, "depth_noise_group")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_1_COL * 0, ++gridY, WIDTH_1_COL))
                .add(wrap(gui, this.depthNoisePeriodX = CwgGuiFactory.invertedPositiveExponentialSlider(
                        -8, MAX_NOISE_FREQ_POWER, 1.0 / conf.getDouble("depthNoiseFrequencyX"), "depth_noise_period_x")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 0, ++gridY, WIDTH_2_COL))
                .add(wrap(gui, this.depthNoisePeriodZ = CwgGuiFactory.invertedPositiveExponentialSlider(
                        -8, MAX_NOISE_FREQ_POWER, 1.0 / conf.getDouble("depthNoiseFrequencyZ"), "depth_noise_period_z")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_2_COL * 1, gridY, WIDTH_2_COL))

                .add(wrap(gui, this.depthNoiseOctaves = CwgGuiFactory.intSlider(
                        1, 16, conf.getInt("depthNoiseOctaves"), "depth_noise_octaves")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 0, ++gridY, WIDTH_3_COL))
                .add(wrap(gui, this.depthNoiseFactor = positiveExponentialSlider(
                         1, 12, conf.getDouble("depthNoiseFactor"), "depth_noise_factor")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 1, gridY, WIDTH_3_COL))
                .add(wrap(gui, this.depthNoiseOffset = symmetricExponentialSlider(
                        1, 12, conf.getDouble("depthNoiseOffset"), "depth_noise_offset")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 2, gridY, WIDTH_3_COL))

                // selector noise
                .add(wrap(gui, label(CENTERED, "selector_noise_group")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_1_COL * 0, ++gridY, WIDTH_1_COL))
                .add(wrap(gui, this.selectorNoisePeriodX = CwgGuiFactory.invertedPositiveExponentialSlider(
                        -8, MAX_NOISE_FREQ_POWER, 1.0 / conf.getDouble("selectorNoiseFrequencyX"), "selector_noise_period_x")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 0, ++gridY, WIDTH_3_COL))
                .add(wrap(gui, this.selectorNoisePeriodY = CwgGuiFactory.invertedPositiveExponentialSlider(
                        -8, MAX_NOISE_FREQ_POWER, 1.0 / conf.getDouble("selectorNoiseFrequencyY"), "selector_noise_period_y")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 1, gridY, WIDTH_3_COL))
                .add(wrap(gui, this.selectorNoisePeriodZ = CwgGuiFactory.invertedPositiveExponentialSlider(
                        -8, MAX_NOISE_FREQ_POWER, 1.0 / conf.getDouble("selectorNoiseFrequencyZ"), "selector_noise_period_z")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 2, gridY, WIDTH_3_COL))

                .add(wrap(gui, this.selectorNoiseOctaves = CwgGuiFactory.intSlider(
                        1, 16, conf.getInt("selectorNoiseOctaves"), "selector_noise_octaves")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 0, ++gridY, WIDTH_3_COL))
                .add(wrap(gui, this.selectorNoiseFactor = positiveExponentialSlider(
                        0, 10, conf.getDouble("selectorNoiseFactor"), "selector_noise_factor")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 1, gridY, WIDTH_3_COL))
                .add(wrap(gui, this.selectorNoiseOffset = symmetricExponentialSlider(
                        -5, 5, conf.getDouble("selectorNoiseOffset"), "selector_noise_offset")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 2, gridY, WIDTH_3_COL))


                // low noise
                .add(wrap(gui, label(CENTERED, "low_noise_group")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_1_COL * 0, ++gridY, WIDTH_1_COL))
                .add(wrap(gui, this.lowNoisePeriodX = CwgGuiFactory.invertedPositiveExponentialSlider(
                        -8, MAX_NOISE_FREQ_POWER, 1.0 / conf.getDouble("lowNoiseFrequencyX"), "low_noise_period_x")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 0, ++gridY, WIDTH_3_COL))
                .add(wrap(gui, this.lowNoisePeriodY = CwgGuiFactory.invertedPositiveExponentialSlider(
                        -8, MAX_NOISE_FREQ_POWER, 1.0 / conf.getDouble("lowNoiseFrequencyY"), "low_noise_period_y")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 1, gridY, WIDTH_3_COL))
                .add(wrap(gui, this.lowNoisePeriodZ = CwgGuiFactory.invertedPositiveExponentialSlider(
                        -8, MAX_NOISE_FREQ_POWER, 1.0 / conf.getDouble("lowNoiseFrequencyZ"), "low_noise_period_z")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 2, gridY, WIDTH_3_COL))

                .add(wrap(gui, this.lowNoiseOctaves = CwgGuiFactory.intSlider(
                        1, 16, conf.getInt("lowNoiseOctaves"), "low_noise_octaves")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 0, ++gridY, WIDTH_3_COL))
                .add(wrap(gui, this.lowNoiseFactor = positiveExponentialSlider(
                        -10, 10, conf.getDouble("lowNoiseFactor"), "low_noise_factor")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 1, gridY, WIDTH_3_COL))
                .add(wrap(gui, this.lowNoiseOffset = symmetricExponentialSlider(
                        -5, 5, conf.getDouble("lowNoiseOffset"), "low_noise_offset")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 2, gridY, WIDTH_3_COL))

                // high noise
                .add(wrap(gui, label(CENTERED, "high_noise_group")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_1_COL * 0, ++gridY, WIDTH_1_COL))
                .add(wrap(gui, this.highNoisePeriodX = CwgGuiFactory.invertedPositiveExponentialSlider(
                        -8, MAX_NOISE_FREQ_POWER, 1.0 / conf.getDouble("highNoiseFrequencyX"), "high_noise_period_x")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 0, ++gridY, WIDTH_3_COL))
                .add(wrap(gui, this.highNoisePeriodY = CwgGuiFactory.invertedPositiveExponentialSlider(
                        -8, MAX_NOISE_FREQ_POWER, 1.0 / conf.getDouble("highNoiseFrequencyY"), "high_noise_period_y")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 1, gridY, WIDTH_3_COL))
                .add(wrap(gui, this.highNoisePeriodZ = CwgGuiFactory.invertedPositiveExponentialSlider(
                        -8, MAX_NOISE_FREQ_POWER, 1.0 / conf.getDouble("highNoiseFrequencyZ"), "high_noise_period_z")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 2, gridY, WIDTH_3_COL))

                .add(wrap(gui, this.highNoiseOctaves = CwgGuiFactory.intSlider(
                        1, 16, conf.getInt("highNoiseOctaves"), "high_noise_octaves")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 0, ++gridY, WIDTH_3_COL))
                .add(wrap(gui, this.highNoiseFactor = positiveExponentialSlider(
                        -10, 10, conf.getDouble("highNoiseFactor"), "high_noise_factor")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 1, gridY, WIDTH_3_COL))
                .add(wrap(gui, this.highNoiseOffset = symmetricExponentialSlider(
                        -5, 5, conf.getDouble("highNoiseOffset"), "high_noise_offset")),
                        new UIVerticalTableLayout.GridLocation(WIDTH_3_COL * 2, gridY, WIDTH_3_COL));

        final int previewHeight = 100;
        final int settingsSize = 150;
        UITerrainPreview preview;

        gridY = -1;
        final float biomeCount = ForgeRegistries.BIOMES.getValues().size();

        UIContainer<?> settingsContrainer = new UIVerticalTableLayout<>(gui, 4)
                .setInsets(1, 1, 0, 0)
                .add(wrap(gui, keepPreviewVisible = checkBox("keep_preview_visible", true)),
                        new UIVerticalTableLayout.GridLocation(0, ++gridY, 4))
                .add(wrap(gui, showPreview = checkBox("show_preview", true)),
                        new UIVerticalTableLayout.GridLocation(0, ++gridY, 4))
                .add(wrap(gui, biomeScaleSlider = CwgGuiFactory.invertedPositiveExponentialSlider(-10, -6, 64, "biome_scale")),
                        new UIVerticalTableLayout.GridLocation(0, ++gridY, 4))
                .add(wrap(gui, biomeOffsetSlider = slider(0, biomeCount, 0, "biome_offset")),
                        new UIVerticalTableLayout.GridLocation(0, ++gridY, 4))
                .add(wrap(gui, lockXZ = checkBox("lock_xz_together", true)),
                        new UIVerticalTableLayout.GridLocation(0, ++gridY, 4))
                .add(horizontalAxis = makeUISelect(gui, Arrays.asList(EnumFacing.Axis.X, EnumFacing.Axis.Z))
                                .setLabelPattern(malisisText("preview_horizontal_axis", ": %s")),
                        new UIVerticalTableLayout.GridLocation(0, ++gridY, 4));
        settingsContrainer.setSize(settingsSize, previewHeight);


        UISplitLayout<?> previewSplitView = new UISplitLayout<>(gui, UISplitLayout.Type.SIDE_BY_SIDE, null, null)
                .setSeparatorSize(4)
                .setMinimumUserComponentSize(UISplitLayout.Pos.FIRST, 50)
                .setMinimumUserComponentSize(UISplitLayout.Pos.SECOND, 150)
                .userResizable(true)
                .setSizeOf(UISplitLayout.Pos.SECOND, 150);

        UITerrainPreview.TerrainPreviewDataAccess dataAccess = makeDataAccess();
        previewSplitView.add(preview = new UITerrainPreview(gui, dataAccess).setSize(UIComponent.INHERITED - settingsSize, UIComponent.INHERITED),
                UISplitLayout.Pos.FIRST);
        previewSplitView.add(settingsContrainer, UISplitLayout.Pos.SECOND);

        UISplitLayout<?> rootSplit = new UISplitLayout<>(gui, UISplitLayout.Type.STACKED, previewSplitView, table);
        rootSplit.setSize(UIComponent.INHERITED, UIComponent.INHERITED).setMinimumUserComponentSize(UISplitLayout.Pos.SECOND, 64);

        heightFactor.onUpdate(slider -> updateExpectedHeightsIfNeeded());
        heightOffset.onUpdate(slider -> updateExpectedHeightsIfNeeded());
        lockExpectedHeights.onClick(check -> updateExpectedHeightsIfNeeded());
        biomeScaleSlider.onUpdate(slider -> preview.setBiomeScale((float) slider.getSliderValue()));
        biomeOffsetSlider.onUpdate(slider -> preview.setBiomeOffset((float) slider.getSliderValue()));
        horizontalAxis.register(new Object() {
            @Subscribe
            public void onUpdate(ComponentEvent.ValueChange<UISelect<EnumFacing.Axis>, EnumFacing.Axis> evt) {
                preview.setShownAxis(evt.getNewValue());
            }
        });
        lockXZ.onClick(check -> setLockedXZ(check.isChecked()));
        showPreview.onClick(check -> preview.setEnabled(check.isChecked()));
        depthNoisePeriodX.onUpdate(slider -> {
                if (lockXZ.isChecked()) {
                    depthNoisePeriodZ.setSliderValue(depthNoisePeriodX.getSliderValue());
                }
            });
        lowNoisePeriodX.onUpdate(slider -> {
                if (lockXZ.isChecked()) {
                    lowNoisePeriodZ.setSliderValue(lowNoisePeriodX.getSliderValue());
                }
            });
        highNoisePeriodX.onUpdate(slider -> {
                if (lockXZ.isChecked()) {
                    highNoisePeriodZ.setSliderValue(highNoisePeriodX.getSliderValue());
                }
            });
        selectorNoisePeriodX.onUpdate(slider -> {
                if (lockXZ.isChecked()) {
                    selectorNoisePeriodZ.setSliderValue(selectorNoisePeriodX.getSliderValue());
                }
            });
        setLockedXZ(lockXZ.isChecked());
        preview.setBiomeScale((float) biomeScaleSlider.getSliderValue());
        preview.setBiomeOffset((float) biomeOffsetSlider.getSliderValue());
        horizontalAxis.setSelectedOption(EnumFacing.Axis.X);
        preview.setShownAxis(horizontalAxis.getSelectedValue());
        Consumer<CwgGuiCheckBox> keepPreviewVisibleUpdate;
        keepPreviewVisible.onClick(keepPreviewVisibleUpdate = check -> {
            if (check.isChecked()) {
                table.setSize(UIComponent.INHERITED, UIComponent.INHERITED - previewSplitView.getHeight());
                // check if the container is added, because the first time the event is fired it's not added
                if (previewSplitView.getParent() == table) {
                    table.remove(previewSplitView);
                }
                rootSplit.setMinimumUserComponentSize(UISplitLayout.Pos.FIRST, 64)
                        .setSeparatorSize(4)
                        .setSizeOf(UISplitLayout.Pos.FIRST, 64)
                        .userResizable(true);
                previewSplitView.setPadding(HORIZONTAL_PADDING + HORIZONTAL_INSETS, 2);
                rootSplit.add(previewSplitView, UISplitLayout.Pos.FIRST);
            } else {
                if (previewSplitView.getParent() == rootSplit) {
                    rootSplit.remove(previewSplitView);
                }
                previewSplitView.setPadding(0, 0);
                rootSplit.setMinimumUserComponentSize(UISplitLayout.Pos.FIRST, 0)
                        .setSeparatorSize(0)
                        .setSizeOf(UISplitLayout.Pos.FIRST, 0)
                        .userResizable(false);
                table.setSize(UIComponent.INHERITED, UIComponent.INHERITED);
                table.add(previewSplitView, new UIVerticalTableLayout.GridLocation(WIDTH_1_COL * 0, 0, WIDTH_1_COL));
            }
        });
        // call the handler to correctly set the layout
        keepPreviewVisibleUpdate.accept(keepPreviewVisible);
        this.container = rootSplit;
    }

    private static UIComponent<?> doubleInput(ExtraGui gui, String labelText, GuiTextField field) {
        UISplitLayout<?> split = new UISplitLayout<>(gui, UISplitLayout.Type.SIDE_BY_SIDE, wrap(gui, label(LEFT_ALIGN, labelText)), wrap(gui, field));
        split.setSizeOf(UISplitLayout.Pos.SECOND, 40);
        split.autoFitToContent(true);
        return split;
    }

    private UITerrainPreview.TerrainPreviewDataAccess makeDataAccess() {
        return new UITerrainPreview.TerrainPreviewDataAccess()
                .setWaterLevel(this.getWaterLevel)
                .setHeightVariationFactor(this.heightVariationFactor::getSliderValue)
                .setSpecialHeightVariationFactorBelowAverageY(this.heightVariationSpecialFactor::getSliderValue)
                .setHeightVariationOffset(this.heightVariationOffset::getSliderValue)
                .setHeightFactor(this.heightFactor::getSliderValue)
                .setHeightOffset(this.heightOffset::getSliderValue)
                .setDepthNoiseFactor(this.depthNoiseFactor::getSliderValue)
                .setDepthNoiseOffset(this.depthNoiseOffset::getSliderValue)
                .setDepthNoiseFrequencyX(() -> 1.0 / this.depthNoisePeriodX.getSliderValue())
                .setDepthNoiseFrequencyZ(() -> 1.0 / this.depthNoisePeriodZ.getSliderValue())
                .setDepthNoiseOctaves(this.depthNoiseOctaves::getSliderValue)
                .setSelectorNoiseFactor(this.selectorNoiseFactor::getSliderValue)
                .setSelectorNoiseOffset(this.selectorNoiseOffset::getSliderValue)
                .setSelectorNoiseFrequencyX(() -> 1.0 / this.selectorNoisePeriodX.getSliderValue())
                .setSelectorNoiseFrequencyZ(() -> 1.0 / this.selectorNoisePeriodZ.getSliderValue())
                .setSelectorNoiseFrequencyY(() -> 1.0 / this.selectorNoisePeriodY.getSliderValue())
                .setSelectorNoiseOctaves(this.selectorNoiseOctaves::getSliderValue)
                .setLowNoiseFactor(this.lowNoiseFactor::getSliderValue)
                .setLowNoiseOffset(this.lowNoiseOffset::getSliderValue)
                .setLowNoiseFrequencyX(() -> 1.0 / this.lowNoisePeriodX.getSliderValue())
                .setLowNoiseFrequencyZ(() -> 1.0 / this.lowNoisePeriodZ.getSliderValue())
                .setLowNoiseFrequencyY(() -> 1.0 / this.lowNoisePeriodY.getSliderValue())
                .setLowNoiseOctaves(this.lowNoiseOctaves::getSliderValue)
                .setHighNoiseFactor(this.highNoiseFactor::getSliderValue)
                .setHighNoiseOffset(this.highNoiseOffset::getSliderValue)
                .setHighNoiseFrequencyX(() -> 1.0 / this.highNoisePeriodX.getSliderValue())
                .setHighNoiseFrequencyZ(() -> 1.0 / this.highNoisePeriodZ.getSliderValue())
                .setHighNoiseFrequencyY(() -> 1.0 / this.highNoisePeriodY.getSliderValue())
                .setHighNoiseOctaves(this.highNoiseOctaves::getSliderValue);
    }

    private void updateExpectedHeightsIfNeeded() {
        if (this.lockExpectedHeights.isChecked()) {
            this.expectedBaseHeight.setText(String.format("%.1f", this.heightOffset.getSliderValue()));
            this.expectedHeightVariation.setText(String.format("%.1f", this.heightFactor.getSliderValue()));
            double actualHeight = (this.heightOffset.getSliderValue() + this.heightVariationOffset.getSliderValue() +
                    Math.max(this.heightFactor.getSliderValue() * 2 + this.heightVariationFactor.getSliderValue(),
                            this.heightFactor.getSliderValue() + this.heightVariationFactor.getSliderValue() * 2));
            this.actualHeight.setText(String.format("%.1f", actualHeight));
        }
    }

    private void setLockedXZ(boolean lock) {
        this.depthNoisePeriodZ.enabled = !lock;
        this.lowNoisePeriodZ.enabled = !lock;
        this.highNoisePeriodZ.enabled = !lock;
        this.selectorNoisePeriodZ.enabled = !lock;

        if (lock) {
            this.depthNoisePeriodZ.setSliderValue(this.depthNoisePeriodX.getSliderValue());
            this.lowNoisePeriodZ.setSliderValue(this.lowNoisePeriodZ.getSliderValue());
            this.highNoisePeriodZ.setSliderValue(this.highNoisePeriodZ.getSliderValue());
            this.selectorNoisePeriodZ.setSliderValue(this.selectorNoisePeriodZ.getSliderValue());
        }
    }

    UIContainer<?> getContainer() {
        return container;
    }

    DoubleSupplier getExpectedBaseHeight() {
        return () -> tryParseFloatOrDefault(expectedBaseHeight.getText(), Float.NaN);
    }

    DoubleSupplier getExpectedHeightVariation() {
        return () -> tryParseFloatOrDefault(expectedHeightVariation.getText(), Float.NaN);
    }

    private static float tryParseFloatOrDefault(String val, float def) {
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException ignored) {
        }
        try {
            return (float) Double.parseDouble(val);
        } catch (NumberFormatException ignored) {
        }
        return def;
    }

    void writeConfig(JsonObjectView conf) {
        /* 
         * Possible NumberFormatException errors in TextFields. 
         * - If they happen, turn the Cursor to red, as soon as a value is valid, turn back to default grey.
         * - If the customization is left (by pressing Done) when such an error exists, the value resets to default.
         */
        double expectedBaseHeight, expectedHeightVariation, actualHeight;
        try {
            expectedBaseHeight = Double.parseDouble(this.expectedBaseHeight.getText());
            this.expectedBaseHeight.setTextColor(0xFFFFFFFF);
        } catch (NumberFormatException e) {
            this.expectedBaseHeight.setTextColor(0xFFD00000);
            return;
        }
        
        try {
            expectedHeightVariation = Double.parseDouble(this.expectedHeightVariation.getText());
            this.expectedHeightVariation.setTextColor(0xFFFFFFFF);
        } catch (NumberFormatException e) {
            this.expectedHeightVariation.setTextColor(0xFFD00000);
            return;
        }
        
        try {
            actualHeight = Double.parseDouble(this.actualHeight.getText());
            this.actualHeight.setTextColor(0xFFFFFFFF);
        } catch (NumberFormatException e) {
            this.actualHeight.setTextColor(0xFFD00000);
            return;
        }
        conf.put("expectedBaseHeight", expectedBaseHeight);
        conf.put("expectedHeightVariation", expectedHeightVariation);
        conf.put("actualHeight", actualHeight);

        conf.put("heightVariationFactor", this.heightVariationFactor.getSliderValue());
        conf.put("specialHeightVariationFactorBelowAverageY", this.heightVariationSpecialFactor.getSliderValue());
        conf.put("heightVariationOffset", this.heightVariationOffset.getSliderValue());
        conf.put("heightFactor", this.heightFactor.getSliderValue());
        conf.put("heightOffset", this.heightOffset.getSliderValue());

        conf.put("depthNoiseFrequencyX", 1.0 / this.depthNoisePeriodX.getSliderValue());
        conf.put("depthNoiseFrequencyZ", 1.0 / this.depthNoisePeriodZ.getSliderValue());
        conf.put("depthNoiseOctaves", this.depthNoiseOctaves.getSliderValue());
        conf.put("depthNoiseFactor", this.depthNoiseFactor.getSliderValue());
        conf.put("depthNoiseOffset", this.depthNoiseOffset.getSliderValue());

        conf.put("selectorNoiseFrequencyX", 1.0 / this.selectorNoisePeriodX.getSliderValue());
        conf.put("selectorNoiseFrequencyY", 1.0 / this.selectorNoisePeriodY.getSliderValue());
        conf.put("selectorNoiseFrequencyZ", 1.0 / this.selectorNoisePeriodZ.getSliderValue());
        conf.put("selectorNoiseOctaves", this.selectorNoiseOctaves.getSliderValue());
        conf.put("selectorNoiseFactor", this.selectorNoiseFactor.getSliderValue());
        conf.put("selectorNoiseOffset", this.selectorNoiseOffset.getSliderValue());

        conf.put("lowNoiseFrequencyX", 1.0 / this.lowNoisePeriodX.getSliderValue());
        conf.put("lowNoiseFrequencyY", 1.0 / this.lowNoisePeriodY.getSliderValue());
        conf.put("lowNoiseFrequencyZ", 1.0 / this.lowNoisePeriodZ.getSliderValue());
        conf.put("lowNoiseOctaves", this.lowNoiseOctaves.getSliderValue());
        conf.put("lowNoiseFactor", this.lowNoiseFactor.getSliderValue());
        conf.put("lowNoiseOffset", this.lowNoiseOffset.getSliderValue());

        conf.put("highNoiseFrequencyX", 1.0 / this.highNoisePeriodX.getSliderValue());
        conf.put("highNoiseFrequencyY", 1.0 / this.highNoisePeriodY.getSliderValue());
        conf.put("highNoiseFrequencyZ", 1.0 / this.highNoisePeriodZ.getSliderValue());
        conf.put("highNoiseOctaves", this.highNoiseOctaves.getSliderValue());
        conf.put("highNoiseFactor", this.highNoiseFactor.getSliderValue());
        conf.put("highNoiseOffset", this.highNoiseOffset.getSliderValue());
    }
}
