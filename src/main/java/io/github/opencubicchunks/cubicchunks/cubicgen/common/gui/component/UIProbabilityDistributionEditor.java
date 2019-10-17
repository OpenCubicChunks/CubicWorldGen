package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component;

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.malisisText;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.HORIZONTAL_INSETS;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.HORIZONTAL_PADDING;
import static io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui.CustomCubicGui.VERTICAL_PADDING;

import com.google.common.eventbus.Subscribe;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.ExtraGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.UserFunction;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.UserFunction.Entry;
import mcp.MethodsReturnNonnullByDefault;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.interaction.UIButton;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class UIProbabilityDistributionEditor extends UIContainer<UIProbabilityDistributionEditor> {

    private final UserFunction start;
    private final Consumer<UserFunction> saveHandler;
    private final UIUserFunctionEdit functionEdit;

    /*
      +------------+------------+
      |                         |
      |                         |
      |                         |
      |          GRAPH          |
      |                         |
      |                         |
      +------------+------------+
      |      MANUAL_EDITOR      |
      |   RESET    | RESET VIEW |
      +------------+------------+
      |     CANCEL    APPLY     |
      +------------+------------+
    */
    public UIProbabilityDistributionEditor(ExtraGui gui, UserFunction start,
            Consumer<UserFunction> saveHandler) {
        super(gui);
        this.start = start;
        this.saveHandler = saveHandler;
        this.add(makeButtonsContainer());
        UISplitLayout<?> split = new UISplitLayout<>(gui, UISplitLayout.Type.STACKED,
                this.functionEdit = new UIUserFunctionEdit(gui, start).autoYLockWithMinMax(0, 0, 0.001f, 1, 0.1).switchXY(true),
                makeOptionsButtons(gui, functionEdit));
        split.setPadding(HORIZONTAL_PADDING, 5);
        split.setSizeOf(UISplitLayout.Pos.SECOND, 20);
        this.add(inPanel(gui, split));
    }


    private static final int BTN_WIDTH = 60;

    @Override public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        rp.useTexture.set(false);
        int i = 0;
    }


    private UIContainer<?> makeOptionsButtons(ExtraGui gui, UIUserFunctionEdit editor) {
        UIVerticalTableLayout<?> layout = new UIVerticalTableLayout<>(gui, 3);

        UIButton reset = new UIButton(gui, malisisText("reset")).setSize(20, 20);
        UIButton resetView = new UIButton(gui, malisisText("reset_view")).setSize(20, 20);
        UIButton flatEdges = new UIButton(gui, malisisText("make_flat_edges")).setSize(20, 20);

        reset.register(new Object() {
            @Subscribe
            public void onClick(UIButton.ClickEvent evt) {
                editor.setUserFunction(start);
            }
        });

        resetView.register(new Object() {
            @Subscribe
            public void onClick(UIButton.ClickEvent evt) {
                editor.resetView();
            }
        });

        flatEdges.register(new Object() {
            @Subscribe
            public void onClick(UIButton.ClickEvent evt) {
                UserFunction func = editor.toUserFunction();
                UserFunction modified = toFlatEdges(func);
                editor.setUserFunction(modified);
            }

            private UserFunction toFlatEdges(UserFunction func) {
                if (func.values.length == 0 || func.values.length == 1) {
                    return func;
                }
                int len = func.values.length;
                boolean addBefore = func.values[0].v != func.values[1].v;
                boolean addAfter = func.values[len - 1].v != func.values[len - 2].v;
                Entry[] newValues = new Entry[func.values.length + (addBefore ? 1 : 0) + (addAfter ? 1 : 0)];
                System.arraycopy(func.values, 0, newValues, addBefore ? 1 : 0, func.values.length);
                if (addBefore) {
                    newValues[0] = new Entry(func.values[0].y - 1, func.values[0].v);
                }
                if (addAfter) {
                    newValues[newValues.length - 1] =
                            new Entry(func.values[func.values.length - 1].y + 1, func.values[func.values.length - 1].v);
                }
                return new UserFunction(newValues);
            }
        });

        layout.add(reset, resetView, flatEdges);
        return layout;
    }

    private UIBorderLayout makeButtonsContainer() {
        ExtraGui gui = (ExtraGui) this.getGui();
        final int xSize = UIComponent.INHERITED - HORIZONTAL_PADDING * 2 - HORIZONTAL_INSETS * 2;
        final int xPos = HORIZONTAL_PADDING + HORIZONTAL_INSETS;


        UIButton done = new UIButton(gui, malisisText("done")).setAutoSize(false).setSize(BTN_WIDTH, 20);
        done.register(new Object() {
            @Subscribe
            public void onClick(UIButton.ClickEvent evt) {
                UIProbabilityDistributionEditor.this.saveHandler.accept(functionEdit.toUserFunction());
                getGui().close();
            }
        });
        done.setPosition(0, 0);

        UIButton cancel = new UIButton(gui, malisisText("cancel")).setAutoSize(false).setSize(BTN_WIDTH, 20);
        cancel.register(new Object() {
            @Subscribe
            public void onClick(UIButton.ClickEvent evt) {
                getGui().close();
            }
        });
        cancel.setPosition(BTN_WIDTH + 10, 0);

        UIContainer<?> container = new UIContainer<>(gui);
        container.add(done, cancel);
        container.setSize(BTN_WIDTH * 2 + 10, 20);

        return new UIBorderLayout(gui)
                .setSize(xSize, VERTICAL_PADDING)
                .setAnchor(Anchor.BOTTOM).setPosition(xPos, 0)
                .add(container, UIBorderLayout.Border.CENTER);

    }

    private static UIContainer<?> inPanel(ExtraGui gui, UIComponent<?> comp) {
        UIColoredPanel panel = new UIColoredPanel(gui);
        panel.setSize(UIComponent.INHERITED, UIComponent.INHERITED - VERTICAL_PADDING * 2);
        panel.setPosition(0, VERTICAL_PADDING);
        panel.add(comp);
        return panel;
    }
}
