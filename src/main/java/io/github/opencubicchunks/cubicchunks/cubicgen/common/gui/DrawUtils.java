package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui;

import static java.lang.Math.max;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import java.math.BigInteger;
import java.text.DecimalFormat;

public class DrawUtils {

    public static void drawLineF(GuiRenderer render, float x1, float y1, float x2, float y2, int argb, float width) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lenInv = width / Math.sqrt(dx * dx + dy * dy);
        dx *= lenInv;
        dy *= lenInv;

        GuiShape shape = directShape(
                new Vertex(x2 - dy, y2 + dx, 0),
                new Vertex(x2 + dy, y2 - dx, 0),
                new Vertex(x1 + dy, y1 - dx, 0),
                new Vertex(x1 - dy, y1 + dx, 0)
        );
        RenderParameters rp = new RenderParameters();
        rp.setColor(argb & 0xFFFFFF);
        rp.setAlpha(argb >>> 24);
        render.drawShape(shape, rp);
    }

    public static void drawRectF(GuiRenderer render, float x1, float y1, float x2, float y2, int argb) {
        GuiShape shape = directShape(
                new Vertex(x1, y1, 0),
                new Vertex(x1, y2, 0),
                new Vertex(x2, y2, 0),
                new Vertex(x2, y1, 0)
        );
        RenderParameters rp = new RenderParameters();
        rp.setColor(argb & 0xFFFFFF);
        rp.setAlpha(argb >>> 24);
        render.drawShape(shape, rp);
    }

    public static GuiShape directShape(Vertex... vertices) {
        return new GuiShape(new Face(vertices)) {

            @Override public void setSize(int i, int i1) {
                throw new UnsupportedOperationException();
            }

            @Override public void scale(float v, float v1) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static String formatFloatX(double f) {
        return new DecimalFormat("#.##").format(Math.round(f * 100) / 100.0);
    }

    private static String formatFloatY(double f) {
        return new DecimalFormat("#.#####").format(f);
    }

    public static void drawXScale(GuiRenderer render, int width, int height, double offsetX, double scaleX) {
        double blockLeft = posToX(width, 0, offsetX, scaleX);
        double blockRight = posToX(width, width, offsetX, scaleX);

        FontOptions fo = new FontOptions.FontOptionsBuilder().color(0xFFFFFF).shadow(true).build();


        String maxFormatted = formatFloatX(max(blockLeft, blockRight));
        String minFormatted = formatFloatX(Math.min(blockLeft, blockRight));
        String withFractionFormatted = formatFloatX(Math.min(blockLeft, blockRight) < 0 ? -0.11111111 : 0.11111111);
        float entryWidth = max(
                max(
                        MalisisFont.minecraftFont.getStringWidth(maxFormatted, fo),
                        MalisisFont.minecraftFont.getStringWidth(minFormatted, fo)
                ),
                MalisisFont.minecraftFont.getStringWidth(withFractionFormatted, fo)
        );

        int count = max(1, (int) (width / entryWidth));
        double increment = getIncrement(blockLeft, blockRight, count);
        double start = Math.round(blockLeft / increment) * increment;

        for (int i = 0; i < count; i++) {
            double x = start + i * increment;
            int pos = (int) xToPos(width, x, offsetX, scaleX);
            String formatted = formatFloatX(x);
            int strWidth = (int) MalisisFont.minecraftFont.getStringWidth(formatted, fo) / 2;
            int strPos = pos - strWidth + 1;
            if (strPos < 30) {
                continue;// avoid intersecting with y axis
            }
            render.drawText(MalisisFont.minecraftFont, formatted, strPos, height - 10, 0, fo);
        }

        render.next();
        GlStateManager.disableTexture2D();
        SimpleGuiShape shape = new SimpleGuiShape();
        shape.setSize(1, 2);
        RenderParameters rp = new RenderParameters();
        for (int i = 0; i < count; i++) {
            double x = start + i * increment;
            int pos = (int) xToPos(width, x, offsetX, scaleX);
            shape.storeState();
            shape.setPosition(pos, height - 1);
            render.drawShape(shape, rp);

            shape.resetState();
        }
        render.next();
        GlStateManager.enableTexture2D();
    }


    public static void drawYScale(GuiRenderer render, int width, int height, double offsetY, double scaleY) {
        double blockBottom = posToY(height, height, offsetY, scaleY);// bottom -> getHeight()
        double blockTop = posToY(height, 0, offsetY, scaleY);

        int count = height / 11;
        double increment = getIncrement(blockBottom, blockTop, count);

        double start = Math.round(blockBottom / increment) * increment;

        FontOptions fo = new FontOptions.FontOptionsBuilder().color(0xFFFFFF).shadow(true).build();

        int maxSrtY = MathHelper.ceil(height - MalisisFont.minecraftFont.getStringHeight(fo));

        float[] yMarkYCoords = new float[count];
        for (int i = 0; i < count; i++) {
            double y = start + i * increment;
            int pos = (int) yToPos(height, y, offsetY, scaleY);
            if (pos < -1 || pos > height) {
                continue;
            }
            int strHeight = (int) (MalisisFont.minecraftFont.getStringHeight() / 2);

            int yDraw = pos - strHeight;
            int yDrawStr = MathHelper.clamp(yDraw, 0, maxSrtY);
            yMarkYCoords[i] = pos;
            render.drawText(MalisisFont.minecraftFont, formatFloatY(y), 10, yDrawStr, 0, fo);
        }

        render.next();
        GlStateManager.disableTexture2D();
        for (float pos : yMarkYCoords) {
            DrawUtils.drawLineF(render, 0, pos, 4, pos, 0xFFFFFFFF, 1f);
        }
        render.next();
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
