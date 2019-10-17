package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui;

import net.malisis.core.client.gui.component.UIComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.function.Function;

public class GuiOverlay extends ExtraGui {

    private final GuiScreen parent;
    private final Function<GuiOverlay, UIComponent<?>> createComponent;
    private int guiScreenAlpha;

    public GuiOverlay(GuiScreen parent, Function<GuiOverlay, UIComponent<?>> createComponent) {
        this.parent = parent;
        this.createComponent = createComponent;
    }

    public GuiOverlay guiScreenAlpha(int alpha) {
        this.guiScreenAlpha = alpha;
        return this;
    }

    @Override public void construct() {
        this.clearScreen();
        this.addToScreen(createComponent.apply(this));
        this.guiscreenBackground = guiScreenAlpha != 0;
    }

    @Override
    public void drawBackground(int tint) {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float texScale = 32.0F;
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(0.0D, this.height, 0.0D)
                .tex(0.0D, this.height / texScale + tint)
                .color(64, 64, 64, guiScreenAlpha).endVertex();
        buffer.pos(this.width, this.height, 0.0D)
                .tex(this.width / texScale, this.height / texScale + tint)
                .color(64, 64, 64, guiScreenAlpha).endVertex();
        buffer.pos(this.width, 0.0D, 0.0D)
                .tex(this.width / texScale, tint)
                .color(64, 64, 64, guiScreenAlpha).endVertex();
        buffer.pos(0.0D, 0.0D, 0.0D)
                .tex(0.0D, tint)
                .color(64, 64, 64, guiScreenAlpha).endVertex();
        tessellator.draw();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (parent != null) {
            parent.drawScreen(mouseX, mouseY, partialTicks);
        }
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public void close() {
        // do proper cleanup, because there are static variables (and weak hash maps) that keep track of everything
        draggedComponent = null;
        tooltipComponent = null;
        setFocusedComponent(null, true);
        setHoveredComponent(null, true);
        this.clearScreen();
        
        Keyboard.enableRepeatEvents(false);

        this.mc.displayGuiScreen(parent);
    }
}
