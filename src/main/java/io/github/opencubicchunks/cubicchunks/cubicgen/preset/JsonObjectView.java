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
package io.github.opencubicchunks.cubicchunks.cubicgen.preset;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A JsonObject wrapper with convenience access methods
 */
public class JsonObjectView {

    private final JsonObject obj;

    private JsonObjectView(JsonObject obj) {
        this.obj = obj;
    }

    public static JsonObjectView of(JsonObject obj) {
        return new JsonObjectView(obj);
    }

    public static JsonObjectView empty() {
        return of(new JsonObject());
    }

    public JsonObjectView put(String key, JsonElement value) {
        this.obj.put(key, value);
        return this;
    }

    public JsonObjectView put(String key, JsonArrayView<?> value) {
        this.obj.put(key, value.array());
        return this;
    }

    public JsonObjectView put(String key, JsonObjectView value) {
        this.obj.put(key, value.obj);
        return this;
    }

    public JsonObjectView put(String key, boolean value) {
        this.obj.put(key, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectView put(String key, int value) {
        this.obj.put(key, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectView put(String key, float value) {
        this.obj.put(key, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectView put(String key, double value) {
        this.obj.put(key, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectView put(String key, String value) {
        this.obj.put(key, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectView putNull(String key) {
        this.obj.put(key, JsonNull.INSTANCE);
        return this;
    }

    public JsonObjectView object(String key) {
        return Objects.requireNonNull(JsonObjectView.of(this.obj.get(JsonObject.class, key)), () -> "Missing object entry " + key);
    }

    public JsonObject object() {
        return this.obj;
    }

    public boolean getBool(String key) {
        return Objects.requireNonNull(this.obj.get(boolean.class, key), () -> "Missing boolean entry " + key);
    }

    public int getInt(String key) {
        return Objects.requireNonNull(this.obj.get(int.class, key), () -> "Missing int entry " + key);
    }

    public float getFloat(String key) {
        return (float) (double) Objects.requireNonNull(this.obj.get(double.class, key), () -> "Missing float entry " + key);
    }

    public String getString(String key) {
        return Objects.requireNonNull(this.obj.get(String.class, key), () -> "Missing string entry " + key);
    }

    public BlockStateDesc getBlockState(String key) {
        return CustomGenSettingsSerialization.deserializeBlockstate(object(key).obj, null);
    }

    public JsonArrayView<JsonObjectView> objectArray(String key) {
        return new JsonArrayView<>(Objects.requireNonNull(this.obj.get(JsonArray.class, key), () -> "Missing entry " + key + " of type array"),
                x -> of((JsonObject) x));
    }

    public JsonElement get(String key) {
        return Objects.requireNonNull(obj.get(key), () -> "Missing entry " + key);
    }

    public void forEachString(String key, Consumer<String> toRun) {
        JsonArray array = Objects.requireNonNull(obj.get(JsonArray.class, key), () -> "Missing entry " + key + " of type array");
        array.forEach(e -> toRun.accept(((JsonPrimitive) e).asString()));
    }

    public static class JsonArrayView<T> implements Iterable<T> {

        private final JsonArray array;
        private final Function<JsonElement, T> wrapFunc;

        private JsonArrayView(JsonArray array, Function<JsonElement, T> wrapFunc) {
            this.array = array;
            this.wrapFunc = wrapFunc;
        }

        public static JsonArrayView<JsonObjectView> empty() {
            return new JsonArrayView<>(new JsonArray(), e -> JsonObjectView.of((JsonObject) e));
        }

        public JsonArrayView<T> addObject(JsonObjectView obj) {
            this.array.add(obj.object());
            return this;
        }

        public JsonArray array() {
            return array;
        }

        public T value(int i) {
            return wrapFunc.apply(array.get(i));
        }

        public String comment(int i) {
            return array.getComment(i);
        }

        @Override public Iterator<T> iterator() {
            return new Iterator<T>() {
                private final Iterator<JsonElement> it = array.iterator();

                @Override public boolean hasNext() {
                    return it.hasNext();
                }

                @Override public T next() {
                    return wrapFunc.apply(it.next());
                }
            };
        }
    }
}
