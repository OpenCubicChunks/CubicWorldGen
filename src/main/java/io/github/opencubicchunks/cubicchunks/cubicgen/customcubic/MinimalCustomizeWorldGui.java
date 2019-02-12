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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic;

import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MinimalCustomizeWorldGui extends GuiScreen {
    private final GuiCreateWorld parent;
    private String preset;
    private Predicate<String> stringTester;
    private String title = "";
    private String needMalisisCoreText1 = "MalisisCore not found!";
    private String needMalisisCoreText2 = "You need to install MalisisCore to use full world customization";
    private String needMalisisCoreText3 = "(Version at least " + CustomCubicMod.MALISIS_VERSION + " needed)";
    private GuiTextField presetEdit;
    private boolean error;
    private GuiButton done;

    public MinimalCustomizeWorldGui(GuiScreen parentIn, String preset, Predicate<String> stringTester) {
        this.parent = (GuiCreateWorld) parentIn;
        this.preset = preset;
        this.stringTester = stringTester;
    }

    public void initGui() {
        this.title = I18n.format("options.customizeTitle");
        this.buttonList.clear();
        this.done = this.addButton(new GuiButton(300, this.width / 2 - 45, this.height - 27, 90, 20, I18n.format("gui.done")));
        this.presetEdit = new GuiTextField(301, fontRenderer, 10, 55, width - 20, 15);
        this.presetEdit.setMaxStringLength(Integer.MAX_VALUE);
        this.presetEdit.setText(preset);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        if (button.id == 300) {
            if (this.stringTester.test(this.preset)) {
                this.parent.chunkProviderSettingsJson = this.preset;
                this.mc.displayGuiScreen(this.parent);
            } else {
                this.error = true;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.presetEdit.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.presetEdit.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (this.presetEdit.isFocused()) {
            this.error = false;
            this.presetEdit.textboxKeyTyped(typedChar, keyCode);
            this.preset = this.presetEdit.getText();
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.error) {
            this.done.packedFGColour = 0x00FF0000;
        } else {
            this.done.packedFGColour = 0x00FFFFFF;
        }
        this.drawDefaultBackground();
        this.presetEdit.drawTextBox();
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 2, 0x00FFFFFF);
        this.drawCenteredString(this.fontRenderer, this.needMalisisCoreText1, this.width / 2, 12, 0x00FF5555);
        this.drawCenteredString(this.fontRenderer, this.needMalisisCoreText2, this.width / 2, 22, 0x00FF5555);
        this.drawCenteredString(this.fontRenderer, this.needMalisisCoreText3, this.width / 2, 32, 0x00FF5555);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
