package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui;

public class DynamicZoomTransform extends net.malisis.core.renderer.animation.transformation.Scale {

    public void setStart(float start) {
        fromX = fromY = fromZ = start;
    }

    public void setTarget(float start) {
        toX = toY = toZ = start;
    }

    public void scaleTarget(float factor) {
        this.toX *= factor;
        this.toY *= factor;
        this.toZ *= factor;
    }
}
