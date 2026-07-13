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

import static java.lang.Math.max;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

import java.math.BigInteger;
import java.text.DecimalFormat;

public class DrawUtils {

    public static void drawLineF(float x1, float y1, float x2, float y2, int argb, float width) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lenInv = width / Math.sqrt(dx * dx + dy * dy);
        dx *= lenInv;
        dy *= lenInv;

        float alpha = (float) (argb >> 24 & 255) / 255.0F;
        float r = (float) (argb >> 16 & 255) / 255.0F;
        float g = (float) (argb >> 8 & 255) / 255.0F;
        float b = (float) (argb & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, alpha);
        buf.begin(7, DefaultVertexFormats.POSITION);
        buf.pos(x2 - dy, y2 + dx, 0.0D).endVertex();
        buf.pos(x2 + dy, y2 - dx, 0.0D).endVertex();
        buf.pos(x1 + dy, y1 - dx, 0.0D).endVertex();
        buf.pos(x1 - dy, y1 + dx, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRectF(float x1, float y1, float x2, float y2, int argb) {
        float alpha = (float) (argb >> 24 & 255) / 255.0F;
        float r = (float) (argb >> 16 & 255) / 255.0F;
        float g = (float) (argb >> 8 & 255) / 255.0F;
        float b = (float) (argb & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, alpha);
        buf.begin(7, DefaultVertexFormats.POSITION);
        buf.pos(x1, y1, 0.0D).endVertex();
        buf.pos(x1, y2, 0.0D).endVertex();
        buf.pos(x2, y2, 0.0D).endVertex();
        buf.pos(x2, y1, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static String formatFloatX(double f) {
        return new DecimalFormat("#.##").format(Math.round(f * 100) / 100.0);
    }

    private static String formatFloatY(double f) {
        return new DecimalFormat("#.#####").format(f);
    }

    public static void drawXScale(int posX, int posY, int width, int height, double offsetX, double scaleX) {
        double blockLeft = posToX(width, 0, offsetX, scaleX);
        double blockRight = posToX(width, width, offsetX, scaleX);

        FontRenderer font = Minecraft.getMinecraft().fontRenderer;

        String maxFormatted = formatFloatX(max(blockLeft, blockRight));
        String minFormatted = formatFloatX(Math.min(blockLeft, blockRight));
        String withFractionFormatted = formatFloatX(Math.min(blockLeft, blockRight) < 0 ? -0.11111111 : 0.11111111);
        float entryWidth = max(
                max(font.getStringWidth(maxFormatted), font.getStringWidth(minFormatted)),
                font.getStringWidth(withFractionFormatted)
        );

        int count = max(1, (int) (width / entryWidth));
        double increment = getIncrement(blockLeft, blockRight, count);
        double start = Math.round(blockLeft / increment) * increment;
        GlStateManager.enableBlend();

        for (int i = 0; i < count; i++) {
            double x = start + i * increment;
            int pos = (int) xToPos(width, x, offsetX, scaleX);
            String formatted = formatFloatX(x);
            int strWidth = font.getStringWidth(formatted) / 2;
            int strPos = pos - strWidth + 1;
            if (strPos < 30) {
                continue;// avoid intersecting with y axis
            }
            font.drawString(formatted, posX + strPos, posY + height - 10, 0xFFFFFFFF);
        }

        GlStateManager.disableTexture2D();
        for (int i = 0; i < count; i++) {
            double x = start + i * increment;
            int pos = (int) xToPos(width, x, offsetX, scaleX);
            drawRectF(posX + pos, posY + height - 1, posX + pos + 1, posY + height + 1, 0xFFFFFFFF);
        }
        GlStateManager.enableTexture2D();
    }


    public static void drawYScale(int posX, int posY, int width, int height, double offsetY, double scaleY) {
        double blockBottom = posToY(height, height, offsetY, scaleY);// bottom -> getHeight()
        double blockTop = posToY(height, 0, offsetY, scaleY);

        int count = height / 11;
        double increment = getIncrement(blockBottom, blockTop, count);

        double start = Math.round(blockBottom / increment) * increment;

        FontRenderer font = Minecraft.getMinecraft().fontRenderer;

        int maxSrtY = MathHelper.ceil(height - font.FONT_HEIGHT);
        GlStateManager.enableBlend();

        float[] yMarkYCoords = new float[count];
        for (int i = 0; i < count; i++) {
            double y = start + i * increment;
            int pos = (int) yToPos(height, y, offsetY, scaleY);
            if (pos < -1 || pos > height) {
                continue;
            }
            int strHeight = font.FONT_HEIGHT / 2;

            int yDraw = pos - strHeight;
            int yDrawStr = MathHelper.clamp(yDraw, 0, maxSrtY);
            yMarkYCoords[i] = pos;
            font.drawString(formatFloatY(y), posX + 10, posY + yDrawStr, 0xFFFFFFFF);
        }

        GlStateManager.disableTexture2D();
        for (float pos : yMarkYCoords) {
            DrawUtils.drawLineF(posX, posY + pos, posX + 4, posY + pos, 0xFFFFFFFF, 1f);
        }
        GlStateManager.enableTexture2D();
    }


    private static boolean isPowerOf10(BigInteger input) {
        BigInteger x = BigInteger.ONE;
        BigInteger ten = BigInteger.valueOf(10);
        int comparison;
        while ((comparison = x.compareTo(input)) < 0) {
            x = x.multiply(ten);
        }
        return comparison == 0;
    }

    private static double getIncrement(double start, double end, int maxAmount) {
        double totalSize = Math.abs(end - start);

        int minValConst = 10000;
        // TODO: does it need to be faster?
        BigInteger FIVE = BigInteger.valueOf(5);

        BigInteger curr = new BigInteger("1");
        while (curr.doubleValue() < minValConst * totalSize / maxAmount) {
            if (isPowerOf10(curr)) {
                curr = curr.shiftLeft(1);
            } else if (isPowerOf10(curr.shiftRight(1))) {
                curr = curr.shiftRight(1).multiply(FIVE);
            } else {
                assert isPowerOf10(curr.divide(FIVE)); // 5*powerOf10
                curr = curr.shiftLeft(1);
            }
        }

        return curr.doubleValue() / minValConst;
    }


    public static double posToY(int height, double pos, double offsetY, double scaleY) {
        return offsetY + scaleY * (-pos + height / 2.0);
    }

    public static double yToPos(int height, double y, double offsetY, double scaleY) {
        return -(y - offsetY) / scaleY + height / 2.0;
    }

    public static double posToX(int width, double pos, double offsetX, double scaleX) {
        return offsetX + scaleX * (pos - width / 2.0);
    }

    public static double xToPos(int width, double x, double offsetX, double scaleX) {
        return (x - offsetX) / scaleX + width / 2.0;
    }


}
