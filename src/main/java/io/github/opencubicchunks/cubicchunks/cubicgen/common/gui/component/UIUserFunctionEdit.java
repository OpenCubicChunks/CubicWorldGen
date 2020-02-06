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

import io.github.opencubicchunks.cubicchunks.cubicgen.common.DynamicLerpAnimation;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.DrawUtils;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IClipable;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UIUserFunctionEdit extends UIComponent<UIUserFunctionEdit> implements IClipable {

    private static final double SNAP_DISTANCE = 3;

    private final List<CustomGeneratorSettings.UserFunction.Entry> entries;

    private double scaleX = 1, offsetX = 0, scaleCenterX = 0;
    private double scaleY = 1, offsetY = 0, scaleCenterY = 0;

    private boolean flipXY = false;
    private boolean lockYRange = false;
    private double minLockedY = -200;
    private double maxLockedY = 100;

    private boolean isInteractionEnabled = true;

    private final DynamicLerpAnimation scaleAnimationX = new DynamicLerpAnimation(300, 0, x -> {
        double newScale = Math.pow(2, x);
        double ratio = newScale / scaleX;

        offsetX -= scaleCenterX;
        offsetX *= ratio;
        offsetX += scaleCenterX;

        scaleX = Math.pow(2, x);

        return x;
    }, x -> x);

    private final DynamicLerpAnimation scaleAnimationY = new DynamicLerpAnimation(300, 0, y -> {
        double newScale = Math.pow(2, y);
        double ratio = newScale / scaleY;

        offsetY -= scaleCenterY;
        offsetY *= ratio;
        offsetY += scaleCenterY;

        scaleY = Math.pow(2, y);
        return y;
    }, x -> x);


    private CustomGeneratorSettings.UserFunction.Entry dragging;

    private Consumer<UIUserFunctionEdit> onClickHandler;
    private Runnable toRunLater;
    private Runnable autoYLockHandler;

    public UIUserFunctionEdit(MalisisGui gui, CustomGeneratorSettings.UserFunction start) {
        super(gui);
        this.entries = new ArrayList<>(Arrays.asList(start.values));
    }

    public void resetView() {
        scaleAnimationX.finishNow();
        scaleAnimationY.finishNow();
        if (!Double.isFinite(scaleX)) {
            scaleAnimationX.setTarget(0);
            scaleAnimationX.finishNow();
            scaleX = 1;
        }
        if (!Double.isFinite(scaleY)) {
            scaleAnimationY.setTarget(0);
            scaleAnimationY.finishNow();
            scaleY = 1;
        }
        if (!Double.isFinite(offsetX)) {
            offsetX = 0;
        }
        if (!Double.isFinite(offsetY)) {
            offsetY = 0;
        }
        double ratioX = 1 / scaleX;
        // (offsetX - X)*ratioX + X == 0
        // offsetX*ratioX + X*(-ratioX + 1) == 0
        // offsetX*ratioX/(ratioX - 1) == X

        if (Math.abs(ratioX - 1) < 0.01) {
            // handle case where scale is very close to 1 already, by setting it further from 1 and starting animation then
            scaleAnimationX.setTarget(scaleAnimationX.getTarget() + 0.1);
            scaleAnimationX.finishNow();
            ratioX = 1 / scaleX;
            assert Math.abs(ratioX - 1) > 0.01;
        }
        scaleCenterX = offsetX * ratioX / (ratioX - 1);
        scaleAnimationX.setTarget(0);


        double ratioY = 1 / scaleY;
        if (Math.abs(ratioY - 1) < 0.01) {
            scaleAnimationY.setTarget(scaleAnimationY.getTarget() + 0.1);
            scaleAnimationY.finishNow();
            ratioY = 1 / scaleY;
            assert Math.abs(ratioY - 1) > 0.01;
        }
        scaleCenterY = offsetY * ratioY / (ratioY - 1);
        scaleAnimationY.setTarget(0);

    }

    public void setUserFunction(CustomGeneratorSettings.UserFunction function) {
        this.entries.clear();
        this.entries.addAll(Arrays.asList(function.values));
    }

    public CustomGeneratorSettings.UserFunction toUserFunction() {
        return toUserFunction(entries);
    }

    public UIUserFunctionEdit autoValueLockForParams(float minArg, float maxArg) {
        int i = 0;
        Runnable old = toRunLater;
        this.toRunLater = () -> {
            if (old != null) {
                old.run();
            }
            this.scaleX = (maxArg - minArg) / getWidthFlip();
            this.offsetX = minArg + getWidthFlip() * xScale() / 2.0f;

            CustomGeneratorSettings.UserFunction func = toUserFunction(entries);

            float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
            min = Math.min(func.getValue(minArg), min);
            max = Math.max(func.getValue(minArg), max);

            min = Math.min(func.getValue(maxArg), min);
            max = Math.max(func.getValue(maxArg), max);

            for (CustomGeneratorSettings.UserFunction.Entry value : func.values) {
                if (value.y >= minArg && value.y <= maxArg) {
                    min = Math.min(value.v, min);
                    max = Math.max(value.v, max);
                }
            }

            if (min == max) {
                min -= 0.05;
                max += 0.05;
            }
            float delta = (max - min) * 0.1f;
            lockValueRange(min - delta, max + delta);
        };
        return this;
    }

    public UIUserFunctionEdit autoYLockWithMinMax(double minMin, double maxMin, double minMax, double maxMax, double margin) {
        this.autoYLockHandler = () -> {
            CustomGeneratorSettings.UserFunction func = toUserFunction(entries);
            float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;

            for (CustomGeneratorSettings.UserFunction.Entry value : func.values) {
                min = Math.min(value.v, min);
                max = Math.max(value.v, max);
            }

            if (min == max) {
                min -= 0.05;
                max += 0.05;
            }
            min = (float) MathHelper.clamp(min, minMin, maxMin);
            max = (float) MathHelper.clamp(max, minMax, maxMax);
            double delta = (max - min) * margin;
            min -= delta;
            max += delta;
            min = (float) MathHelper.clamp(min, minMin, maxMin);
            max = (float) MathHelper.clamp(max, minMax, maxMax);
            lockValueRange(min, max);
        };
        return this;
    }

    public UIUserFunctionEdit setLockedMin(float minValue) {
        Runnable old = toRunLater;
        toRunLater = () -> {
            if (old != null) {
                old.run();
            }
            this.lockYRange = true;
            this.minLockedY = minValue;
        };
        return this;
    }

    public UIUserFunctionEdit setLockedMax(float maxValue) {
        Runnable old = toRunLater;
        toRunLater = () -> {
            if (old != null) {
                old.run();
            }
            this.lockYRange = true;
            this.maxLockedY = maxValue;
        };
        return this;
    }

    public UIUserFunctionEdit lockValueRange(float minValue, float maxValue) {
        this.lockYRange = true;
        this.minLockedY = minValue;
        this.maxLockedY = maxValue;
        return this;
    }

    public UIUserFunctionEdit unlockValueRange() {
        this.lockYRange = false;
        this.scaleY = 1;
        this.offsetY = 0;
        return this;
    }

    public UIUserFunctionEdit switchXY(boolean value) {
        this.flipXY = value;
        return this;
    }

    /**
     * Adds a custom click handler and disabled normal user interactions
     */
    public void onClick(Consumer<UIUserFunctionEdit> consumer) {
        this.onClickHandler = consumer;
        this.isInteractionEnabled = false;
    }

    // internal utils

    private CustomGeneratorSettings.UserFunction toUserFunction(List<CustomGeneratorSettings.UserFunction.Entry> entries) {
        return new CustomGeneratorSettings.UserFunction(
                entries.stream().collect(Collectors.toMap(e -> e.y, e -> e.v, (a, b) -> a))
        );
    }

    private double yScale() {
        if (lockYRange) {
            return (maxLockedY - minLockedY) / getHeightFlip();
        } else {
            return scaleY;
        }
    }

    private double yOffset() {
        if (lockYRange) {
            return minLockedY + getHeightFlip() * yScale() / 2.0;
        } else {
            return offsetY;
        }
    }

    private double xScale() {
        return scaleX;
    }

    private double xOffset() {
        return offsetX;
    }

    private float getMouseXDirectNoflip() {
        return Mouse.getX() / (float) getRenderer().getScaleFactor() - screenX();
    }

    private float getMouseYDirectNoflip() {
        return (Display.getHeight() - Mouse.getY()) / (float) getRenderer().getScaleFactor() - screenY();
    }

    private float getMouseXDirect() {
        return flipXY ? getHeight() - getMouseYDirectNoflip() : getMouseXDirectNoflip();
    }

    private float getMouseYDirect() {
        return flipXY ? getWidth() - getMouseXDirectNoflip() : getMouseYDirectNoflip();
    }

    private int getWidthFlip() {
        return flipXY ? getHeight() : getWidth();
    }

    private int getHeightFlip() {
        return flipXY ? getWidth() : getHeight();
    }


    private CustomGeneratorSettings.UserFunction.Entry getClosest(List<CustomGeneratorSettings.UserFunction.Entry> entries,
            float screenX, float screenY, boolean ignoreScreenY) {
        return getClosest(entries, screenX, screenY, ignoreScreenY, e -> true);
    }

    private CustomGeneratorSettings.UserFunction.Entry getClosest(List<CustomGeneratorSettings.UserFunction.Entry> entries,
            float screenX, float screenY, boolean ignoreScreenY, Predicate<CustomGeneratorSettings.UserFunction.Entry> filter) {
        final double xScale = xScale();
        final double xOffset = xOffset();
        final double yScale = yScale();
        final double yOffset = yOffset();

        CustomGeneratorSettings.UserFunction.Entry closest = null;
        float lastDistSq = Float.POSITIVE_INFINITY;
        for (CustomGeneratorSettings.UserFunction.Entry entry : entries) {
            @SuppressWarnings("SuspiciousNameCombination")
            float entryX = (float) DrawUtils.xToPos(getWidthFlip(), entry.y, xOffset, xScale);

            float dx, dy = 0;

            dx = Math.abs(entryX - screenX);
            if (dx > SNAP_DISTANCE) {
                continue;
            }
            if (!ignoreScreenY) {
                float entryY = (float) DrawUtils.yToPos(getHeightFlip(), entry.v, yOffset, yScale);
                dy = Math.abs(entryY - screenY);
                if (dy > SNAP_DISTANCE) {
                    continue;
                }
            }
            float d = dx * dx + dy * dy;
            if (d * d < lastDistSq && filter.test(entry)) {
                lastDistSq = d * d;
                closest = entry;
            }
        }
        return closest;
    }

    // handle input

    @Override public boolean onClick(int x, int y) {
        // intentionally run even if disabled
        if (this.onClickHandler != null) {
            this.onClickHandler.accept(this);
            return true;
        }
        return false;
    }

    @Override public boolean onButtonPress(int x, int y, MouseButton button) {
        if (!isInteractionEnabled) {
            return false;
        }
        float localX = getMouseXDirect();
        float localY = getMouseYDirect();
        if (button == MouseButton.LEFT) {
            modifyForClick(entries);
            this.dragging = getClosest(entries, localX, localY, false);
        } else if (button == MouseButton.RIGHT) {
            CustomGeneratorSettings.UserFunction.Entry closest = getClosest(entries, localX, localY, true);
            if (closest != null) {
                entries.remove(closest);
            }
        }
        return true;
    }

    @Override public boolean onButtonRelease(int x, int y, MouseButton button) {
        if (!isInteractionEnabled) {
            return false;
        }
        if (button == MouseButton.LEFT) {
            this.dragging = null;
        }
        return true;
    }

    @Override public boolean onDrag(int prevMouseX, int prevMouseY, int x, int y, MouseButton button) {
        if (button == MouseButton.MIDDLE) {
            double dx = x - prevMouseX;
            double dy = y - prevMouseY;
            if (flipXY) {
                double t = dx;
                dx = dy;
                dy = t;
            }
            dx *= scaleX;
            dy *= scaleY;
            offsetX += dx;
            offsetY += dy;
            return true;
        }
        if (!isInteractionEnabled) {
            return false;
        }
        if (dragging == null) {
            return false;
        }
        float localX = getMouseXDirect();
        float localY = getMouseYDirect();

        double lastYVal = dragging.y;

        dragging.y = (float) DrawUtils.posToX(getWidthFlip(), localX, xOffset(), xScale());
        dragging.v = (float) DrawUtils.posToY(getHeightFlip(), localY, yOffset(), yScale());

        CustomGeneratorSettings.UserFunction.Entry otherClosest = getClosest(entries, localX, localY, true, e -> e != dragging);
        Set<Float> allYValues = entries.stream().filter(e -> e != dragging).map(e -> e.y).collect(Collectors.toSet());

        if (otherClosest != null) {
            Boolean goUp = null;
            if (lastYVal < otherClosest.y && dragging.y >= otherClosest.y) {
                dragging.y = Math.min(dragging.y, otherClosest.y);
                goUp = false;
            } else if (lastYVal > otherClosest.y && dragging.y <= otherClosest.y) {
                dragging.y = Math.max(dragging.y, otherClosest.y);
                goUp = true;
            }
            if (goUp != null) {
                while (allYValues.contains(dragging.y)) {
                    dragging.y = goUp ? Math.nextUp(dragging.y) : Math.nextDown(dragging.y);
                }
            }
        }
        entries.sort(Comparator.comparingDouble(e -> e.y));
        return true;
    }

    @Override public boolean onScrollWheel(int x, int y, int delta) {
        if (!isInteractionEnabled) {
            return super.onScrollWheel(x, y, delta);
        }
        this.scaleCenterX = flipXY ?
                DrawUtils.posToY(getHeight(), getMouseYDirectNoflip(), xOffset(), xScale()) :
                DrawUtils.posToX(getWidth(), getMouseXDirectNoflip(), xOffset(), xScale());
        this.scaleAnimationX.setTarget(this.scaleAnimationX.getTarget() - delta * 0.3);

        if (!this.lockYRange) {
            this.scaleCenterY = flipXY ?
                    DrawUtils.posToX(getWidth(), getMouseXDirectNoflip(), yOffset(), yScale()) :
                    DrawUtils.posToY(getHeight(), getMouseYDirectNoflip(), yOffset(), yScale());
            this.scaleAnimationY.setTarget(this.scaleAnimationY.getTarget() - delta * 0.3);
        }
        return true;
    }

    private void modifyForClick(List<CustomGeneratorSettings.UserFunction.Entry> entries) {
        float mouseX = getMouseXDirect();
        float mouseY = getMouseYDirect();

        float currY = (float) DrawUtils.posToX(getWidthFlip(), mouseX, xOffset(), xScale());
        float currV = (float) DrawUtils.posToY(getHeightFlip(), mouseY, yOffset(), yScale());

        CustomGeneratorSettings.UserFunction.Entry closest = getClosest(entries, mouseX, mouseY, true);

        if (closest != null) {
            closest.v = currV;
        } else {
            CustomGeneratorSettings.UserFunction.Entry newEntry = new CustomGeneratorSettings.UserFunction.Entry(currY, currV);

            entries.add(newEntry);
            entries.sort(Comparator.comparingDouble(e -> e.y));
        }
    }

    // render

    @Override
    public ClipArea getClipArea() {
        return new ClipArea(this);
    }

    @Override public void setClipContent(boolean b) {
    }

    @Override public boolean shouldClipContent() {
        return true;
    }

    @Override public void drawBackground(GuiRenderer guiRenderer, int mouseX, int mouseY, float partialTick) {
        if (toRunLater != null) {
            toRunLater.run();
            toRunLater = null;
        }
        if (autoYLockHandler != null) {
            autoYLockHandler.run();
        }
        scaleAnimationX.tick();
        scaleAnimationY.tick();

        getRenderer().disableTextures();
        this.rp.setColor(0xFFFFFF);
        this.rp.setAlpha(50);
        guiRenderer.drawShape(this.shape, this.rp);
        getRenderer().next();
        getRenderer().enableTextures();
    }


    @Override public void drawForeground(GuiRenderer guiRenderer, int mouseX, int mouseY, float partialTick) {
        float xAxisY = (float) DrawUtils.yToPos(getHeight(), 0,
                flipXY ? -xOffset() : yOffset(),
                flipXY ? -xScale() : yScale());
        if (xAxisY >= 0 && xAxisY < getHeight()) {
            DrawUtils.drawLineF(guiRenderer, 0, xAxisY, getWidth(), xAxisY, 0xFF000000, 0.5f);
        }
        // vertical line, changes with offset
        float yAxisX = (float) DrawUtils.xToPos(getWidth(), 0,
                flipXY ? -yOffset() : xOffset(),
                flipXY ? -yScale() : xScale());
        if (yAxisX >= 0 && yAxisX <= getWidth()) {
            DrawUtils.drawLineF(guiRenderer, yAxisX, 0, yAxisX, getHeight(), 0xFF000000, 0.5f);
        }

        DrawUtils.drawXScale(guiRenderer, getWidth(), getHeight(),
                flipXY ? yOffset() : xOffset(),
                flipXY ? yScale() : xScale());
        DrawUtils.drawYScale(guiRenderer, getWidth(), getHeight(),
                flipXY ? xOffset() : yOffset(),
                flipXY ? xScale() : yScale());

        if (dragging == null && this.isInteractionEnabled) {
            List<CustomGeneratorSettings.UserFunction.Entry> predictedEntries = new ArrayList<>();
            // clone because entries are mutable
            for (CustomGeneratorSettings.UserFunction.Entry e : entries) {
                predictedEntries.add(new CustomGeneratorSettings.UserFunction.Entry(e.y, e.v));
            }
            modifyForClick(predictedEntries);
            drawGraph(predictedEntries, 100);
        }

        drawGraph(entries, 255);

        if (this.isInteractionEnabled && mouseX >= 0 && mouseY >= 0 && mouseX <= getWidth() && mouseY <= getHeight()) {
            // get mouse position directly for better accuracy on >1 gui scale
            float localX = getMouseXDirect();
            float localY = getMouseYDirect();

            final double xScale = xScale();
            final double xOffset = xOffset();
            final double yScale = yScale();
            final double yOffset = yOffset();

            float currY = (float) DrawUtils.posToX(getWidthFlip(), localX, xOffset, xScale);

            float r = 2;

            CustomGeneratorSettings.UserFunction.Entry closest = getClosest(entries, localX, localY, true);
            if (closest != null) {
                r = 3.5f;
                currY = closest.y;
            }

            CustomGeneratorSettings.UserFunction func = toUserFunction(entries);
            float currV = func.getValue(currY);

            @SuppressWarnings("SuspiciousNameCombination")
            float screenXRaw = (float) DrawUtils.xToPos(getWidthFlip(), currY, xOffset, xScale);
            float screenYRaw = (float) DrawUtils.yToPos(getHeightFlip(), currV, yOffset, yScale);

            float screenX = flipXY ? getWidth() - screenYRaw : screenXRaw;
            float screenY = flipXY ? getHeight() - screenXRaw : screenYRaw;
            DrawUtils.drawRectF(guiRenderer, screenX - r, screenY - r, screenX + r, screenY + r, 0xAAFFFFFF);
        }
    }

    private void drawGraph(List<CustomGeneratorSettings.UserFunction.Entry> entries, int alpha) {
        double xScale = xScale();
        double xOffset = xOffset();
        double yScale = yScale();
        double yOffset = yOffset();

        getRenderer().next();
        GlStateManager.disableTexture2D();

        CustomGeneratorSettings.UserFunction func = toUserFunction(entries);

        final float maxGraphY = (float) DrawUtils.posToX(getWidthFlip(), getWidthFlip(), xOffset, xScale);

        float lineWidth = 0.9f / getRenderer().getScaleFactor();

        float currY = (float) DrawUtils.posToX(getWidthFlip(), 0, xOffset, xScale);
        float currV = func.getValue(currY);
        for (CustomGeneratorSettings.UserFunction.Entry entry : entries) {
            if (entry.y > maxGraphY) {
                break;
            }
            if (entry.y > currY) {
                @SuppressWarnings("SuspiciousNameCombination")
                float startXRaw = (float) DrawUtils.xToPos(getWidthFlip(), currY, xOffset, xScale);
                float startYRaw = (float) DrawUtils.yToPos(getHeightFlip(), currV, yOffset, yScale);
                @SuppressWarnings("SuspiciousNameCombination")
                float pointScreenXRaw = (float) DrawUtils.xToPos(getWidthFlip(), entry.y, xOffset, xScale);
                float pointScreenYRaw = (float) DrawUtils.yToPos(getHeightFlip(), entry.v, yOffset, yScale);


                float startX = flipXY ? getWidth() - startYRaw : startXRaw;
                float startY = flipXY ? getHeight() - startXRaw : startYRaw;
                float pointScreenX = flipXY ? getWidth() - pointScreenYRaw : pointScreenXRaw;
                float pointScreenY = flipXY ? getHeight() - pointScreenXRaw : pointScreenYRaw;

                DrawUtils.drawLineF(getRenderer(),
                        startX,
                        startY,
                        pointScreenX,
                        pointScreenY,
                        0xFFFFFF | (alpha << 24), lineWidth);
                currY = entry.y;
                currV = entry.v;

                DrawUtils.drawRectF(getRenderer(),
                        pointScreenX - 2, pointScreenY - 2,
                        pointScreenX + 2, pointScreenY + 2,
                        0xFFFFFF | (alpha << 24));

            }

        }

        float lastY = (float) DrawUtils.posToX(getWidthFlip(), getWidthFlip(), xOffset, xScale);
        float lastV = func.getValue(lastY);

        @SuppressWarnings("SuspiciousNameCombination")
        float startX = (float) DrawUtils.xToPos(getWidthFlip(), currY, xOffset, xScale);
        float startY = (float) DrawUtils.yToPos(getHeightFlip(), currV, yOffset, yScale);
        @SuppressWarnings("SuspiciousNameCombination")
        float pointScreenX = (float) DrawUtils.xToPos(getWidthFlip(), lastY, xOffset, xScale);
        float pointScreenY = (float) DrawUtils.yToPos(getHeightFlip(), lastV, yOffset, yScale);


        DrawUtils.drawLineF(getRenderer(),
                flipXY ? getWidth() - startY : startX,
                flipXY ? getHeight() - startX : startY,
                flipXY ? getWidth() - pointScreenY : pointScreenX,
                flipXY ? getHeight() - pointScreenX : pointScreenY,
                0xFFFFFF | (alpha << 24), lineWidth);

        getRenderer().next();
    }
}
