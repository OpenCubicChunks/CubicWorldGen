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
package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.converter;

import com.google.common.base.Converter;
import com.google.common.base.Preconditions;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.DoublePredicate;

public class Converters {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected Converter<Double, Double> currentConverter = Converter.identity();

        public ExponentialBuilder exponential() {
            return new ExponentialBuilder(self());
        }

        public InfinityBuilder withInfinity() {
            return new InfinityBuilder(self());
        }

        public InverseBuilder inverse() {
            return new InverseBuilder(self());
        }

        public Builder reverse() {
            return composeWithSelf(new ReverseConverter());
        }

        public Builder scale(double scale) {
            return composeWithSelf(new ScaleConverter(scale));
        }

        public Builder offset(double offset) {
            return composeWithSelf(new OffsetConverter(offset));
        }

        public Builder linearScale(double min, double max) {
            return scale(max - min).offset(min);
        }

        public Builder pow(double power) {
            return composeWithSelf(new PowerConverter(power));
        }

        public RoundingBuilder rounding() {
            return new RoundingBuilder(self());
        }

        public Converter<Double, Double> build() {
            return self().currentConverter;
        }

        public Converter<Float, Float> buildFloat() {
            Converter<Double, Float> dbl2Flt = new Converter<Double, Float>() {
                @Override protected Float doForward(Double x) {
                    return x.floatValue();
                }

                @Override protected Double doBackward(Float x) {
                    return x.doubleValue();
                }
            };
            Converter<Float, Double> flt2Dbl = new Converter<Float, Double>() {
                @Override protected Double doForward(Float x) {
                    return x.doubleValue();
                }

                @Override protected Float doBackward(Double x) {
                    return x.floatValue();
                }
            };

            return compose(dbl2Flt, compose(self().currentConverter, flt2Dbl));
        }

        /**
         * This special method allows to do something like this:
         * <p>
         * builder().someConverter().setSomethingInSomeConverter(a).otherConverter().yetAnotherConverter().build();
         * <p>
         * someConverter() returns SomeConverterBuilder, which extends the normal Builder. When otherConverter is
         * called, which won't be implemented in SomeConverterBuilder and the implementation in Builder calls self()
         * instead of accessing this(). self() in SomeConverterBuilder can then finish any work in progress building and
         * return the Builder it wrapped.
         */
        protected Builder self() {
            return this;
        }

        private final Builder composeWithSelf(Converter<Double, Double> conv) {
            Builder self = self();
            self.currentConverter = compose(conv, self.currentConverter);
            return self;
        }
    }

    public static class ExponentialBuilder extends Builder {

        private Builder baseSelf;

        boolean hasZero = false;
        double minExpPos = Double.NaN;
        double maxExpPos = Double.NaN;

        double minExpNeg = Double.NaN;
        double maxExpNeg = Double.NaN;
        double baseVal;

        public ExponentialBuilder(Builder baseSelf) {
            this.baseSelf = baseSelf;
        }

        public ExponentialBuilder withZero() {
            ExponentialBuilder self = exponentialSelf();
            self.hasZero = true;
            return self;
        }

        public ExponentialBuilder withPositiveExponentRange(double min, double max) {
            ExponentialBuilder self = exponentialSelf();
            self.minExpPos = min;
            self.maxExpPos = max;
            return self;
        }

        public ExponentialBuilder withNegativeExponentRange(double min, double max) {
            ExponentialBuilder self = exponentialSelf();
            self.minExpNeg = min;
            self.maxExpNeg = max;
            return self;
        }

        public ExponentialBuilder withBaseValue(double baseVal) {
            ExponentialBuilder self = exponentialSelf();
            self.baseVal = baseVal;
            return self;
        }

        protected ExponentialBuilder exponentialSelf() {
            return this;
        }

        @Override
        protected Builder self() {
            baseSelf.currentConverter = compose(new ExponentialConverter(this), baseSelf.currentConverter);
            return baseSelf;
        }
    }

    public static class InfinityBuilder extends Builder {

        private Builder baseSelf;

        private double negInf = 0, posInf = 1;

        public InfinityBuilder(Builder baseSelf) {
            this.baseSelf = baseSelf;
        }

        public InfinityBuilder negativeAt(double negInf) {
            InfinityBuilder self = infinitySelf();
            self.negInf = negInf;
            return self;
        }

        public InfinityBuilder positiveAt(double posInf) {
            InfinityBuilder self = infinitySelf();
            self.posInf = posInf;
            return self;
        }

        protected InfinityBuilder infinitySelf() {
            return this;
        }

        @Override
        protected Builder self() {
            baseSelf.currentConverter = compose(new ConverterWithInfinity(negInf, posInf), baseSelf.currentConverter);
            return baseSelf;
        }
    }

    public static class InverseBuilder extends Builder {

        private Builder baseSelf;

        private double mult = 1;

        public InverseBuilder(Builder baseSelf) {
            this.baseSelf = baseSelf;
        }

        public InverseBuilder withMultiplier(double mult) {
            InverseBuilder self = inverseSelf();
            self.mult = mult;
            return self;
        }

        protected InverseBuilder inverseSelf() {
            return this;
        }

        @Override
        protected Builder self() {
            baseSelf.currentConverter = compose(new InverseConverter(this.mult), baseSelf.currentConverter);
            return baseSelf;
        }
    }

    public static class RoundingBuilder extends Builder {

        private Builder baseSelf;

        double maxExp = Double.NaN;
        Set<RoundingConverter.RoundingEntry> roundingData = new HashSet<>();
        BiPredicate<Double, Double> isValueInRadius;

        public RoundingBuilder(Builder baseSelf) {
            this.baseSelf = baseSelf;
        }

        public RoundingBuilder withMaxExp(double max) {
            RoundingBuilder self = roundingSelf();
            self.maxExp = max;
            return self;
        }

        public RoundingBuilder withRoundingRadiusPredicate(BiPredicate<Double, Double> op) {
            Preconditions.checkNotNull(op);
            RoundingBuilder self = roundingSelf();
            self.isValueInRadius = op;
            return self;
        }

        public RoundingBuilder withBase(double baseVal, double multiplier) {
            RoundingBuilder self = roundingSelf();
            self.roundingData.add(new RoundingConverter.RoundingEntry(baseVal, multiplier));
            return self;
        }

        public RoundingBuilder withBases(double base, double[] multiplier) {
            RoundingBuilder self = roundingSelf();
            if (multiplier == null) {
                return self;
            }
            for (double val : multiplier) {
                self.roundingData.add(new RoundingConverter.RoundingEntry(base, val));
            }
            return self;
        }

        protected RoundingBuilder roundingSelf() {
            return this;
        }

        @Override
        protected Builder self() {
            RoundingConverter rb = new RoundingConverter(this);
            rb.setReverse(baseSelf.currentConverter.reverse());
            baseSelf.currentConverter = compose(rb, baseSelf.currentConverter);
            return baseSelf;
        }
    }

    public static <IN, X, OUT> Converter<IN, OUT> compose(Converter<X, OUT> f, Converter<IN, X> g) {
        return Converter.from(
                x -> f.convert(g.convert(x)),
                x -> g.reverse().convert(f.reverse().convert(x))
        );
    }
}
