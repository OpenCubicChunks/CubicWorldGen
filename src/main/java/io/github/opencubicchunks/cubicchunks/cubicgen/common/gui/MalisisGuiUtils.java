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

import static java.lang.Math.round;

import com.google.common.base.Converter;
import com.google.common.base.Predicate;
import com.google.common.eventbus.Subscribe;
import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UICheckboxNoAutoSize;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIRangeSlider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISliderImproved;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISplitLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.converter.Converters;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UISlider;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

import javax.annotation.Nonnull;

public class MalisisGuiUtils {

    // MalisisCore 6.5.1 removed validator and replaced it with filter
    private static final MethodHandle setValidator, setFilter;

    static {
        MethodHandle handle;
        try {
            handle = MethodHandles.lookup().findVirtual(
                UITextField.class,
                "setValidator",
                MethodType.methodType(
                    UITextField.class,
                    Predicate/*<String>*/.class
                )
            );
        } catch (NoSuchMethodException e) {
            handle = null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        setValidator = handle;

        try {
            handle = MethodHandles.lookup().findVirtual(
                UITextField.class,
                "setFilter",
                MethodType.methodType(
                    void.class,
                    Function/*<String, String>*/.class
                )
            );
        } catch (NoSuchMethodException e) {
            handle = null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        setFilter = handle;
        if (setFilter == null && setValidator == null) {
            throw new NoSuchMethodError("Expected to find either setFilter or setValidator in UITextField");
        }
    }


    public static UISlider<Float> makeFloatSlider(MalisisGui gui, float min, float max, float defVal, Function<Double, String> text) {

        UISlider<Float>[] wrappedSlider = new UISlider[1];
        BiPredicate<Double, Double> isInRoundRadius = getIsInRoundRadiusPredicate(wrappedSlider);

        float defMult = defVal == 0 ? 1 : defVal;

        Converter<Float, Float> conv = Converters.builder()
                .linearScale(min, max).rounding().withBase(2, 1).withBase(10, 1).withBase(2, defMult).withBase(10, defMult).withMaxExp(128)
                .withRoundingRadiusPredicate(isInRoundRadius)
                .build();

        UISlider<Float> slider = new UISliderImproved<>(gui, 100, conv, value -> text.apply((double) (float) value)).setValue(defVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static UISlider<Float> makeFloatSlider(MalisisGui gui, String name, float min, float max, float defaultVal) {
        return makeFloatSlider(gui, min, max, defaultVal, value -> String.format(name, value));
    }

    public static UISlider<Float> makePositiveExponentialSlider(MalisisGui gui, float minPos, float maxPos, float defaultVal, Function<Double, String> text) {

        UISlider<Float>[] wrappedSlider = new UISlider[1];
        BiPredicate<Double, Double> isInRoundRadius = getIsInRoundRadiusPredicate(wrappedSlider);

        float defMult = defaultVal == 0 ? 1 : defaultVal;

        Converter<Float, Float> conv = Converters.builder()
                .exponential().withBaseValue(2).withPositiveExponentRange(minPos, maxPos)
                .rounding().withBase(2, 1).withBase(10, 1).withBase(2, defMult).withBase(10, defMult).withMaxExp(128)
                .withRoundingRadiusPredicate(isInRoundRadius)
                .withInfinity().positiveAt((float)Math.pow(2, maxPos)).negativeAt(Float.NaN)
                .build();

        UISlider<Float> slider = new UISliderImproved<>(gui, 100, conv, value -> text.apply((double) (float) value)).setValue(defaultVal);
        wrappedSlider[0] = slider;
        return slider;
    }


    public static UISlider<Float> makePositiveExponentialSlider(MalisisGui gui, String name, float minPos, float maxPos, float defaultVal) {

        UISlider<Float>[] wrappedSlider = new UISlider[1];
        BiPredicate<Double, Double> isInRoundRadius = getIsInRoundRadiusPredicate(wrappedSlider);

        float defMult = defaultVal == 0 ? 1 : defaultVal;

        Converter<Float, Float> conv = Converters.builder()
                .exponential().withBaseValue(2).withPositiveExponentRange(minPos, maxPos)
                .rounding().withBase(2, 1).withBase(10, 1).withBase(2, defMult).withBase(10, defMult).withMaxExp(128)
                .withRoundingRadiusPredicate(isInRoundRadius)
                .withInfinity().positiveAt((float)Math.pow(2, maxPos)).negativeAt(Float.NaN)
                .build();

        UISlider<Float> slider = new UISliderImproved<>(gui, 100, conv, name).setValue(defaultVal);
        wrappedSlider[0] = slider;
        return makePositiveExponentialSlider(gui, minPos, maxPos, defaultVal, value -> String.format(name, value));
    }

    public static UISlider<Float> makeExponentialSlider(MalisisGui gui, String name, float minNeg, float maxNeg, float minPos, float maxPos,
            float defaultVal) {

        UISlider<Float>[] wrappedSlider = new UISlider[1];
        BiPredicate<Double, Double> isInRoundRadius = getIsInRoundRadiusPredicate(wrappedSlider);

        float defMult = defaultVal == 0 ? 1 : defaultVal;

        Converter<Float, Float> conv = Converters.builder()
                .exponential().withZero().withBaseValue(2).withNegativeExponentRange(minNeg, maxNeg).withPositiveExponentRange(minPos, maxPos)
                .rounding().withBase(2, 1).withBase(10, 1).withBase(2, defMult).withBase(10, defMult).withMaxExp(128)
                .withRoundingRadiusPredicate(isInRoundRadius)
                .build();

        UISlider<Float> slider = new UISliderImproved<>(gui, 100, conv, name).setValue(defaultVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    @Nonnull private static BiPredicate<Double, Double> getIsInRoundRadiusPredicate(UISlider<Float>[] floatUISlider) {
        return getIsInRoundRadiusPredicate(() -> floatUISlider[0] == null ? 1000 : floatUISlider[0].getWidth());
    }

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

    public static UISlider<Float> makeInvertedExponentialSlider(MalisisGui gui, String name, float minNeg, float maxNeg, float minPos, float maxPos,
            float defaultVal) {

        UISlider<Float>[] wrappedSlider = new UISlider[1];
        BiPredicate<Double, Double> isInRoundRadius = getIsInRoundRadiusPredicate(wrappedSlider);

        float defMult = defaultVal == 0 ? 1 : defaultVal;

        Converter<Float, Float> conv = Converters.builder()
                .reverse()
                .pow(2)
                .exponential().withZero().withBaseValue(2).withNegativeExponentRange(minNeg, maxNeg).withPositiveExponentRange(minPos, maxPos)
                .inverse()
                .rounding().withBase(2, 1).withBase(10, 1).withBase(2, defMult).withBase(10, defMult).withMaxExp(128)
                .withRoundingRadiusPredicate(isInRoundRadius)
                .build();

        UISlider<Float> slider = new UISliderImproved<>(gui, 100, conv, name).setValue(defaultVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static UISlider<Integer> makeIntSlider(MalisisGui gui, String name, int min, int max, int defaultValue) {
        // the explicit <Integer> needs to be there because otherwise it won't compile on some systems
        UISlider<Integer> slider = new UISliderImproved<Integer>(
                gui,
                100,
                Converter.from(
                        x -> round(x * (max - min) + min),
                        x -> (x - min) / ((float) max - min))
                , name)
                .setValue(defaultValue)
                .setSize(0, 20);
        return slider;
    }

    public static UISlider<Float> makeFloatSlider(MalisisGui gui, String name, float defaultValue) {
        // the explicit <Float> needs to be there because otherwise it won't compile on some systems
        UISlider<Float> slider = new UISliderImproved<Float>(
                gui,
                100,
                Converter.identity(),
                name)
                .setValue(defaultValue)
                .setSize(0, 20);
        return slider;
    }

    public static UICheckBox makeCheckbox(MalisisGui gui, String name, boolean defaultValue) {
        UICheckBox cb = new UICheckboxNoAutoSize(gui, name)
                .setChecked(defaultValue)
                .setFontOptions(FontOptions.builder().color(0xFFFFFF).shadow().build());
        return cb;
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
                .build();

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

    public static UIComponent<?> label(MalisisGui gui, String text) {
        return wrappedCentered(
                gui, new UILabel(gui, text)
                        .setFontOptions(FontOptions.builder().color(0xFFFFFF).shadow().build())
        ).setSize(0, 15);
    }

    // textInput as argument so that it's easy to access it later
    // otherwise, to access the value of the text field, it would be necessary to get it out of implementation-specific contaier
    public static UIComponent<?> floatInput(ExtraGui gui, String text, UITextField textField, float defaultValue) {
        if (setValidator != null) {
            try {
                setValidator.invoke(textField, (Predicate<String>) str -> {
                    try {
                        Float.parseFloat(str);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            try {
                Function<String, String> filter = newStr -> {
                    try {
                        Float.parseFloat(newStr);
                        return newStr;
                    } catch (NumberFormatException e1) {
                        // bug in 6.5.1 where the old text is actually the new text
                        String str = textField.getText();
                        try {
                            Float.parseFloat(newStr);
                            return str; // return old text
                        } catch (NumberFormatException e2) {
                            // this is ugly...

                            // first, is it empty or a single character that doesn't parse?
                            if (str.length() <= 1) {
                                return "";
                            }
                            // this should cover all the "user is typing" cases
                            int length = str.length();
                            // iterate end-to-beginning and check if after removing that character, it becomes a valid number
                            for (int i = length - 1; i >= 0; i--) {
                                String sub = str.substring(0, i) + str.substring(i + 1);
                                try {
                                    Float.parseFloat(sub);
                                    return sub;
                                } catch (NumberFormatException e3) {
                                }
                            }
                            // uh... we still didn't return?
                            // I don't know what could trigger this, but this is the way we will try to handle it:
                            // iterate over the characters and remove everything we don't want.
                            //  * up until the dot, remove everything non-digit
                            //    * except 'e' if it's second or later character, then assume there was no dot, and after that, that we are past 'e'
                            //  * if there was no dot, or we are past the dot, remove everything non-digit except 'e'
                            //  * after an 'e', remove everything non-digit
                            // If it *still* doesn't parse, give up and return empty string
                            StringBuilder newsb = new StringBuilder(str.length());
                            boolean seenFirstDigit = false;
                            boolean seenDot = false;
                            boolean seenE = false;
                            for (char ch : str.toCharArray()) {
                                if (ch >= '0' && ch <= '9') {
                                    newsb.append(ch);
                                    seenFirstDigit = true;
                                } else if (ch == 'e' || ch == 'E') {
                                    if (!seenE && seenFirstDigit) {
                                        newsb.append(ch);
                                        seenE = true;
                                        seenDot = true;
                                    }
                                } else if (ch == '.') {
                                    if (!seenDot) {
                                        newsb.append(ch);
                                        seenDot = true;
                                    }
                                }
                            }
                            str = newsb.toString();
                            try {
                                Float.parseFloat(str);
                                return str;
                            } catch (NumberFormatException e3) {
                                return "";
                            }
                        }
                    }
                };
                setFilter.invoke(textField, filter);
                // another (imperfect) hack because filter isn't applied when remoing characters
                textField.register(new Object() {
                    @Subscribe public void onValueChange(ComponentEvent.ValueChange<UITextField, String> change) {
                        String newText = filter.apply(change.getNewValue());
                        if (!newText.equals(change.getNewValue())) {
                            textField.setText(newText);
                        }
                    }
                });
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        textField.setEditable(true);
        textField.setText(String.format("%.1f", defaultValue));
        textField.setFontOptions(FontOptions.builder().color(0xFFFFFF).build());
        UIComponent<?> label = wrappedMiddle(gui, new UILabel(gui, text).setFontOptions(FontOptions.builder().color(0xFFFFFF).build()));
        UISplitLayout<?> split = new UISplitLayout<>(gui, UISplitLayout.Type.SIDE_BY_SIDE, label, textField);
        split.setSizeOf(UISplitLayout.Pos.SECOND, 40);
        split.autoFitToContent(true);
        return split;
    }
    public static UIContainer<?> wrappedCentered(MalisisGui gui, UIComponent<?> comp) {
        comp.setAnchor(Anchor.MIDDLE | Anchor.CENTER);
        UIContainer<?> cont = new UIContainer<>(gui);
        cont.add(comp);
        return cont;
    }

    public static UIContainer<?> wrappedMiddle(MalisisGui gui, UIComponent<?> comp) {
        comp.setAnchor(Anchor.MIDDLE);
        UIContainer<?> cont = new UIContainer<>(gui);
        cont.add(comp);
        return cont;
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
