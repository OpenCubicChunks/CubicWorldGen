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

import com.google.common.base.Converter;
import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIRangeSlider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.converter.Converters;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.DoubleSupplier;

public class MalisisGuiUtils {

    @Nonnull private static BiPredicate<Double, Double> getIsInRoundRadiusPredicate(UIRangeSlider<Float>[] floatUISlider) {
        return getIsInRoundRadiusPredicate(() -> floatUISlider[0] == null ? 1000 : floatUISlider[0].getWidth());
    }

    @Nonnull private static BiPredicate<Double, Double> getIsInRoundRadiusPredicate(DoubleSupplier width) {
        return (previousSlide, foundSlide) -> {
            double w = width.getAsDouble();
            double rangeCenter = Math.round(previousSlide * w) / w;
            double minRange = rangeCenter - 0.5 / w;
            double maxRange = rangeCenter + 0.5 / w;

            return foundSlide >= minRange && foundSlide <= maxRange;
        };
    }

    public static UIRangeSlider<Float> makeRangeSlider(ExtraGui gui, String name, float min, float max, float defaultMin, float defaultMax) {
        return rangeSlider(gui, name, min, max, defaultMin, defaultMax, (a, b) -> I18n.format(name, a * 100, b * 100));
    }

    public static UIRangeSlider<Float> makeOreHeightSlider(ExtraGui gui, String name, float min, float max, float defaultMin, float defaultMax,
            DoubleSupplier expectedBaseHeight, DoubleSupplier expectedHeightVariation) {
        BiFunction<Float, Float, String> i18nFormat = (a, b) -> I18n.format(name,
                String.format("%.2f", a * 100), String.format("%.2f", b * 100),
                String.format("%.1f", a * expectedHeightVariation.getAsDouble() + expectedBaseHeight.getAsDouble()),
                String.format("%.1f", b * expectedHeightVariation.getAsDouble() + expectedBaseHeight.getAsDouble()));
        return rangeSlider(gui, name, min, max, defaultMin, defaultMax, i18nFormat);
    }

    private static UIRangeSlider<Float> rangeSlider(ExtraGui gui, String name, float min, float max, float defMin, float defMax,
            BiFunction<Float, Float, String> i18nFormat) {

        UIRangeSlider<Float>[] wrappedSlider = new UIRangeSlider[1];
        BiPredicate<Double, Double> isInRoundRadius = getIsInRoundRadiusPredicate(wrappedSlider);
        float maxExp = MathHelper.ceil(Math.log(Math.max(1, max)) / Math.log(2));

        Converter<Float, Float> conv = Converters.builder()
                .linearScale(min, max)
                .rounding().withBase(2, 1).withBase(10, 1).withMaxExp(maxExp).withRoundingRadiusPredicate(isInRoundRadius)
                .withInfinity().negativeAt(min).positiveAt(max)
                .buildFloat();

        UIRangeSlider<Float> slider = new UIRangeSlider<>(gui, 100, conv, i18nFormat).setRange(defMin, defMax);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static <T> UISelect<T> makeUISelect(MalisisGui gui, Iterable<T> values) {
        UISelect<T> select = new UISelect<T>(gui, 10, values) {{
            gui.removeFromScreen(this.optionsContainer);
            ((ExtraGui) gui).delayedAdd(this.optionsContainer);
        }};
        return select;
    }

    public static UISelect<BiomeOption> makeBiomeList(MalisisGui gui, int selectedId) {
        List<BiomeOption> biomes = new ArrayList<>();
        Map<Integer, BiomeOption> byId = new HashMap<>();
        biomes.add(BiomeOption.ALL);
        for (Biome biome : ForgeRegistries.BIOMES) {
            BiomeOption bo = new BiomeOption(biome);
            biomes.add(bo);
            byId.put(Biome.REGISTRY.getIDForObject(biome), bo);
        }
        UISelect<BiomeOption> select = makeUISelect(gui, biomes);

        select.select(byId.getOrDefault(selectedId, BiomeOption.ALL));

        select.maxDisplayedOptions(8);
        return select;
    }

    public static String vanillaText(String name) {
        String unloc = CustomCubicMod.MODID + ".gui.cubicgen." + name;
        return unloc;
    }

    public static String malisisText(String name) {
        String unloc = "{" + CustomCubicMod.MODID + ".gui.cubicgen." + name + "}";
        return unloc;
    }

    public static String malisisText(String name, String fmt) {
        String unloc = "{" + CustomCubicMod.MODID + ".gui.cubicgen." + name + "}" + fmt;
        return unloc;
    }
}
