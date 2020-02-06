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
package io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.control.IControlComponent;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(UIComponent.class)
public abstract class MixinUIComponent<T extends UIComponent<T>> {

    @Shadow protected UIComponent<?> parent;

    @Shadow public abstract boolean fireEvent(ComponentEvent<?> event);

    @Shadow public abstract T self();

    @Shadow @Final private Set<IControlComponent> controlComponents;

    /**
     * @author Barteks2x
     * @reason fix memory leak
     */
    @Overwrite(remap = false)
    public void setParent(UIComponent<?> parent) {
        this.parent = parent;
        //noinspection ConstantConditions
        if ((Object) this instanceof UIContainer) {
            UIContainer<?> self = (UIContainer<?>) (Object) this;
            for (UIComponent<?> component : ((IUIContainer) self).getComponents()) {
                if (parent == null) {
                    component.setParent(null);
                } else {
                    component.setParent(self);
                }
            }
        }
        UIComponent<?> self = (UIComponent<?>) (Object) this;
        for (IControlComponent controlComponent : this.controlComponents) {
            if (parent == null) {
                controlComponent.setParent(null);
            } else {
                controlComponent.setParent(self);
            }
        }
        this.fireEvent(new ContentUpdateEvent<>(this.self()));
    }
}
