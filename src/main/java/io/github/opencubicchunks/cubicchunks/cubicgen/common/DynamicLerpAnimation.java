package io.github.opencubicchunks.cubicchunks.cubicgen.common;

import io.github.opencubicchunks.cubicchunks.api.util.MathUtil;
import net.minecraft.util.math.MathHelper;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;

public class DynamicLerpAnimation {

    private final int animTimeMillis;
    private final DoubleUnaryOperator onUpdate;
    private final double startVal;
    private DoubleUnaryOperator limiter;

    private double startX;
    private double targetX;
    private double currentX;
    private boolean done = true;
    private long animationEnd = System.nanoTime();
    private long lastFrameTime = System.nanoTime();

    public DynamicLerpAnimation(int animTimeMillis, double startVal, DoubleUnaryOperator onUpdate, DoubleUnaryOperator limiter) {
        this.animTimeMillis = animTimeMillis;
        this.startX = startVal;
        this.targetX = startVal;
        this.currentX = startVal;
        this.onUpdate = onUpdate;
        this.limiter = limiter;

        this.startVal = startVal;
    }

    public void tick() {
        if (!Double.isFinite(targetX) || !Double.isFinite(startX) || !Double.isFinite(currentX)) {
            if (!Double.isFinite(targetX)) {
                setTarget(startVal);
            }
            finishNow();
            return;
        }
        if (!this.done) {
            double clampedTarget = this.limiter.applyAsDouble(this.targetX);
            if (Math.abs(clampedTarget - this.currentX) < 1e-8) {
                this.targetX = this.onUpdate.applyAsDouble(this.targetX);
                this.startX = this.targetX;
                this.targetX = clampedTarget;
                this.done = true;
            } else {

                final double durationNanos = TimeUnit.MILLISECONDS.toNanos(animTimeMillis);
                double dtNs = (System.nanoTime() - lastFrameTime);

                double currentScrollProgress = MathUtil.unlerp(currentX, startX, targetX);
                // handle infinity case (startX == targetX)
                currentScrollProgress = MathHelper.clamp(currentScrollProgress, 0, 1);

                // we are at specified progress, we know end time, duration, and current progress
                // find what time we "should" be at
                double currentExpectedTimeNanos = MathUtil.lerp(currentScrollProgress, animationEnd - durationNanos, animationEnd);

                double expectedNextTimeNanos = dtNs;
                double newProgress = MathUtil.unlerp(expectedNextTimeNanos, animationEnd - durationNanos - currentExpectedTimeNanos, animationEnd - currentExpectedTimeNanos);
                double newPosition = MathUtil.lerp(MathHelper.clamp(newProgress, 0, 1), startX, targetX);

                newPosition = onUpdate.applyAsDouble(newPosition);
                this.currentX = newPosition;

                if (newProgress >= 1.0) {
                    this.startX = this.targetX;
                    this.targetX = clampedTarget;
                    this.done = true;
                }
            }
        }
        this.lastFrameTime = System.nanoTime();
    }

    public double getTarget() {
        return this.targetX;
    }

    public void setTarget(double value) {
        this.targetX = value;
        if (this.done) {
            this.lastFrameTime = System.nanoTime();
        }
        this.done = false;
        this.animationEnd = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(this.animTimeMillis);
    }

    public void finishNow() {
        this.targetX = this.onUpdate.applyAsDouble(this.targetX);
        this.startX = this.targetX;
        this.currentX = this.onUpdate.applyAsDouble(this.targetX);
        this.done = true;
    }
}
