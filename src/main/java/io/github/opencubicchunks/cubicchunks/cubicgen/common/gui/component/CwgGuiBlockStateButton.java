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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.DummyWorld;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class CwgGuiBlockStateButton extends GuiButton {

    public static final int SIZE = 24;
    public static final int PADDED_SIZE = SIZE + 6;
    private final List<String> tooltipLines = new ArrayList<>();
    private BlockStateDesc blockState;
    private Consumer<CwgGuiBlockStateButton> onClick;

    public CwgGuiBlockStateButton(BlockStateDesc blockState) {
        super(0, 0, 0, SIZE, SIZE, "");
        this.blockState = blockState;
        updateTooltip();
    }

    public void onClick(Consumer<CwgGuiBlockStateButton> action) {
        this.onClick = action;
    }

    private void updateTooltip() {
        this.tooltipLines.clear();
        this.tooltipLines.add(blockState.getBlockId());
        for (Entry<String, String> entry : blockState.getProperties().entrySet()) {
            this.tooltipLines.add(entry.getKey() + " = " + entry.getValue());
        }
    }

    public BlockStateDesc getBlockState() {
        return blockState;
    }

    public void setBlockState(BlockStateDesc state) {
        blockState = state;
        updateTooltip();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int x, int y) {
        onClick.accept(this);
        return true;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTick) {
        if (!visible || blockState == null || blockState.getBlockState() == null) {
            return;
        }
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        IBlockState state = blockState.getBlockState();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        ITextureObject blockTexture = Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, blockTexture.getGlTextureId());
        VertexFormat format = DefaultVertexFormats.BLOCK;
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.x, this.y + 16f, 100.0F);
        GlStateManager.scale(12.0F, 12.0F, -12.0F);
        GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        buffer.begin(GL11.GL_QUADS, format);
        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(state, BlockPos.ORIGIN,
                DummyWorld.getInstanceWithBlockState(state), buffer);
        Tessellator.getInstance().draw();
        if (state.getBlock().hasTileEntity(state)) {
            TileEntity te = state.getBlock().createTileEntity(null, state);
            if (te != null) {
                TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer =
                        TileEntityRendererDispatcher.instance.getRenderer(te);
                if (tileentityspecialrenderer != null) {
                    TileEntityItemStackRenderer.instance.renderByItem(new ItemStack(state.getBlock()));
                }
            }
        }
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
    }

    @Override public void drawButtonForegroundLayer(int mouseX, int mouseY) {
        if (hovered) {
            Minecraft.getMinecraft().currentScreen.drawHoveringText(tooltipLines, mouseX, mouseY);
        }
    }

    public String getBlockName() {
        return this.blockState.getBlockId();
    }
    
    public String getBlockProperties() {
        return blockState.getProperties().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(",", "[", "]"));
    }
}
