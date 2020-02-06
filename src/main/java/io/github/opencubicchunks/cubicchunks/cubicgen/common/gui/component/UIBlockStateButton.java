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

import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.DummyWorld;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UIBlockStateButton<T extends UIBlockStateButton<T>> extends UIComponent<T> {

    public static final int SIZE = 24;
    private BlockStateDesc iBlockState;
    private final List<Consumer<? super UIBlockStateButton<?>>> onClick;

    public UIBlockStateButton(MalisisGui gui, BlockStateDesc iBlockState1) {
        super(gui);
        iBlockState = iBlockState1;
        onClick = new ArrayList<>();
        setTooltip(generateTooltip(iBlockState));
        setSize(SIZE, SIZE);
    }

    public T onClick(Consumer<? super UIBlockStateButton<?>> cons) {
        this.onClick.add(cons);
        return self();
    }

    public static String generateTooltip(BlockStateDesc blockState) {
        StringBuffer sb = new StringBuffer(128);
        sb.append(blockState.getBlockId());
        for (Entry<String, String> entry : blockState.getProperties().entrySet()) {
            sb.append(" \n ");
            sb.append(entry.getKey());
            sb.append(" = ");
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

    public BlockStateDesc getState() {
        return iBlockState;
    }

    public void setBlockState(BlockStateDesc iBlockState1) {
        iBlockState = iBlockState1;
        setTooltip(generateTooltip(iBlockState));
    }

    @Override
    public boolean onClick(int x, int y) {
        MalisisGui.playSound(SoundEvents.UI_BUTTON_CLICK);
        onClick.forEach(cons -> cons.accept(self()));
        return true;
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (iBlockState != null && iBlockState.getBlockState() != null) {
            IBlockState blockstate = iBlockState.getBlockState();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();

            VertexBuffer vertexbuffer = Tessellator.getInstance().getBuffer();
            Tessellator.getInstance().draw();
            ITextureObject blockTexture = Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, blockTexture.getGlTextureId());
            VertexFormat format = DefaultVertexFormats.BLOCK;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) this.screenX(), (float) this.screenY() + 16f, 100.0F);
            GlStateManager.scale(12.0F, 12.0F, -12.0F);
            GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
            vertexbuffer.begin(GL11.GL_QUADS, format);
            Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(blockstate, BlockPos.ORIGIN,
                    DummyWorld.getInstanceWithBlockState(blockstate), vertexbuffer);
            Tessellator.getInstance().draw();
            if (blockstate.getBlock().hasTileEntity(blockstate)) {
                TileEntity te = blockstate.getBlock().createTileEntity(null, blockstate);
                if (te != null) {
                    TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer =
                            TileEntityRendererDispatcher.instance.<TileEntity>getSpecialRenderer(te);
                    if (tileentityspecialrenderer != null) {
                        TileEntityItemStackRenderer.instance.renderByItem(new ItemStack(blockstate.getBlock()));
                    }
                }
            }
            GlStateManager.popMatrix();
            renderer.next();

            GlStateManager.disableRescaleNormal();
        }
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
    }

    public String getBlockName() {
        return this.iBlockState.getBlockId();
    }

    public String getBlockProperties() {
        return iBlockState.getProperties().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(",", "[", "]"));
    }
}
