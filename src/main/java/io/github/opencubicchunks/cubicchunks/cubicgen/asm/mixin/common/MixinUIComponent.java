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
