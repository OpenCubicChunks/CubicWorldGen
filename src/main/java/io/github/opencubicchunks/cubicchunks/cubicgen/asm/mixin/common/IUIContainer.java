package io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(UIContainer.class)
public interface IUIContainer {
    @Accessor Set<UIComponent<?>> getComponents();
}
