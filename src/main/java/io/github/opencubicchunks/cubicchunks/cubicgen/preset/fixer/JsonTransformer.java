package io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * A general json data transformer that preserves comments, doesn't discard data
 * and the result is guaranteed to contain the requested data after successful
 * transformation.
 */
public class JsonTransformer<CTX> {

    private final String transformName;
    private final Map<EntryTransform<CTX>, String> transformToNew;
    private final Map<String, List<EntryTransform<CTX>>> oldToTransforms;
    @Nullable private final UnhandledEntryTransform<CTX> unhandled;

    private JsonTransformer(String transformName, Map<EntryTransform<CTX>, String> transformToNew,
            Map<String, List<EntryTransform<CTX>>> oldToTransforms, @Nullable UnhandledEntryTransform<CTX> unhandled) {
        this.transformName = transformName;
        this.transformToNew = transformToNew;
        this.oldToTransforms = oldToTransforms;
        this.unhandled = unhandled;
    }

    public static <CTX> Builder<CTX> builder(String transformName) {
        return new Builder<CTX>(transformName);
    }

    public static <CTX> JsonTransformer<CTX> passthroughAll(String transformName) {
        return JsonTransformer.<CTX>builder(transformName)
                .defaultTransform((name, oldElement, comment, newRoot, context) -> newRoot.put(name, oldElement, comment))
                .build();
    }


    public JsonObject transform(JsonObject json, CTX context) {
        JsonObject newRoot = new JsonObject();
        Set<String> newHandled = new HashSet<>();

        List<String> commentLines = new ArrayList<>();
        for (String oldKey : getObjectKeys(json)) {
            List<EntryTransform<CTX>> transforms = oldToTransforms.get(oldKey);
            if (transforms == null || transforms.isEmpty()) {
                if (unhandled != null) {
                    unhandled.apply(oldKey, json.get(oldKey), json.getComment(oldKey), newRoot, context);
                    if (newRoot.getComment(oldKey) == null) {
                        newRoot.setComment(oldKey, json.getComment(oldKey));
                    }
                    continue;
                }
                if (commentLines.isEmpty()) {
                    commentLines.add(transformName + " unknown entries:");
                }
                String oldComment = json.getComment(oldKey);
                if (oldComment != null) {
                    commentLines.add(oldComment);
                }
                commentLines.add("\"" + oldKey + "\": " + json.get(oldKey).toJson());
            } else {
                for (int i = 0; i < transforms.size(); i++) {
                    EntryTransform<CTX> transform = transforms.get(i);
                    String newName = transformToNew.get(transform);
                    if (newHandled.contains(newName)) {
                        continue;
                    }
                    newHandled.add(newName);
                    transform.apply(json, newRoot, context);

                    // handle putting comments from missing entries
                    if (i == 0 && !commentLines.isEmpty()) {
                        assert newName != null;
                        String existingComment = newRoot.getComment(newName);
                        if (existingComment != null) {
                            commentLines.add(0, existingComment);
                        }
                        String newComment = String.join("\n", commentLines);
                        commentLines.clear();
                        newRoot.setComment(newName, newComment);
                    }
                }

            }
        }

        for (Map.Entry<EntryTransform<CTX>, String> entry : transformToNew.entrySet()) {
            if (newHandled.contains(entry.getValue())) {
                continue;
            }
            entry.getKey().apply(json, newRoot, context);
        }
        return newRoot;
    }

    public static class Builder<CTX> {

        private final String transformName;
        private Map<EntryTransform<CTX>, String> transformToNew = new HashMap<>();
        private Map<String, List<EntryTransform<CTX>>> oldToTransforms = new HashMap<>();
        private UnhandledEntryTransform<CTX> unhandledTransform;

        public Builder(String transformName) {
            this.transformName = transformName;
        }

        public Builder<CTX> defaultTransform(UnhandledEntryTransform<CTX> transform) {
            this.unhandledTransform = transform;
            return this;
        }

        public Builder<CTX> passthroughFor(String... names) {
            return passthroughFor(Arrays.asList(names));
        }

        public Builder<CTX> drop(String... names) {
            return drop(Arrays.asList(names));
        }

        public Builder<CTX> drop(List<String> names) {
            for (String name : names) {
                transform(name, (oldRoot, newRoot, context) -> {
                    // no-op
                });
            }
            return this;
        }

        public Builder<CTX> passthroughWithDefault(String key, Object defaultPrimitive) {
            return valueTransform(key, (old, ctx) -> old == null ? new JsonPrimitive(defaultPrimitive) : old);
        }

        public Builder<CTX> passthroughFor(List<String> names) {
            for (String name : names) {
                transform(name, (oldRoot, newRoot, context) ->
                        newRoot.put(name, Objects.requireNonNull(oldRoot.get(name), "Entry \"" + name + "\" is required"), oldRoot.getComment(name)));
            }
            return this;
        }

        public Builder<CTX> valueTransform(String name, BiFunction<JsonElement, CTX, JsonElement> transform) {
            return transform(name, (oldRoot, newRoot, context) ->
                    newRoot.put(name, transform.apply(oldRoot.get(name), context), oldRoot.getComment(name)));
        }

        public Builder<CTX> setPrimitive(String name, Function<CTX, Object> makeValue) {
            return valueTransform(name, (jsonElement, ctx) -> {
                Object p = makeValue.apply(ctx);
                if (p != null && !(p instanceof Number) && !(p instanceof String)) {
                    throw new IllegalArgumentException("Unexpected type " + p.getClass() + ", expected null, Number or String");
                }
                return p == null ? JsonNull.INSTANCE : new JsonPrimitive(p);
            });
        }

        public Builder<CTX> setPrimitiveIf(Predicate<CTX> condition, String name, Function<CTX, Object> makeValue) {
            return transform(name, (oldRoot, newRoot, context) -> {
                if (!condition.test(context)) {
                    return;
                }
                Object p = makeValue.apply(context);
                if (p != null && !(p instanceof Number) && !(p instanceof String)) {
                    throw new IllegalArgumentException("Unexpected type " + p.getClass() + ", expected null, Number or String");
                }
                JsonElement newVal = p == null ? JsonNull.INSTANCE : new JsonPrimitive(p);
                newRoot.put(name, newVal, oldRoot.getComment(name));
            });
        }

        /**
         * Adds a transformation for a json element, using an EntryTransform instance.
         * Assumes entry name didn't change.
         *
         * @param name name of the json object
         * @param transform the entry transform
         * @return this
         */
        public Builder<CTX> transform(String name, EntryTransform<CTX> transform) {
            return transform(name, transform, name);
        }

        /**
         * Adds a transformation for a json element, using an EntryTransform instance.
         *
         * @param newName name of the new json object
         * @param transform the entry transform
         * @param oldNames array of old names that the new entry corresponds to. Used only for keeping entry and comment order.
         * @return this
         */
        public Builder<CTX> transform(String newName, EntryTransform<CTX> transform, String... oldNames) {
            transformToNew.put(transform, newName);
            for (String oldPath : oldNames) {
                oldToTransforms.computeIfAbsent(oldPath, p -> new ArrayList<>()).add(transform);
            }
            return this;
        }

        /**
         * Adds a transformation for an object, using a new JsonTransformer instance,
         * with passthrough pre-transform, with default value
         *
         * @param name name of the json object
         * @param transformer transformer to use, with combined previous context and the old json as context
         * @param defaultValue the default value to use if an entry doesn't exist
         * @return this
         */
        public Builder<CTX> objectTransform(String name,
                JsonTransformer<CombinedContext<CTX>> transformer, JsonElement defaultValue) {
            return objectTransform(name,
                    (oldRoot, newRoot, context) -> newRoot.put(name, oldRoot.getOrDefault(name, defaultValue), oldRoot.getComment(name)),
                    transformer, name);
        }

        /**
         * Adds a transformation for an object, using a new JsonTransformer instance.
         *
         * @param name name of the json object
         * @param preTransform pre-transformer for the array. Puts untransformed object into new json object.
         * @param transformer transformer to use, with combined previous context and the old json as context
         * @return this
         */
        public Builder<CTX> objectTransform(String name,
                EntryTransform<CTX> preTransform,
                JsonTransformer<CombinedContext<CTX>> transformer) {
            return objectTransform(name, preTransform, transformer, name);
        }

        /**
         * Adds a transformation for an object, using a new JsonTransformer instance.
         *
         * @param newName name of the new json object. This assumes that the name doesn't change.
         * @param preTransform pre-transformer for the array. Puts untransformed object into new json object.
         * @param transformer transformer to use, with combined previous context and the old json as context
         * @param oldNames array of old names that the new entry corresponds to. Used only for keeping entry and comment order.
         * @return this
         */
        public Builder<CTX> objectTransform(String newName,
                EntryTransform<CTX> preTransform,
                JsonTransformer<CombinedContext<CTX>> transformer, String... oldNames) {

            EntryTransform<CTX> impl = (oldRoot, newRoot, context) -> {
                CombinedContext<CTX> ctx = new CombinedContext<>(context, oldRoot);
                preTransform.apply(oldRoot, newRoot, context);
                JsonObject entry = newRoot.get(JsonObject.class, newName);
                if (entry == null) {
                    entry = new JsonObject();
                }
                JsonObject transformed = transformer.transform(entry, ctx);
                newRoot.put(newName, transformed);
            };
            return this.transform(newName, impl, oldNames);
        }

        /**
         * Adds a transformation for an object array, applying a JsonTransformer instance to each array entry.
         *
         * @param name name of the json object. This assumes that the name doesn't change.
         * @param preTransform pre-transformer for the array
         * @param transformer transformer to use for each array element, with combined previous context and the old json as context
         * @return this
         */
        public Builder<CTX> objectArrayTransform(String name,
                EntryTransform<CTX> preTransform,
                JsonTransformer<CombinedContext<CTX>> transformer) {
            return objectArrayTransform(name, preTransform, transformer);
        }

        /**
         * Adds a transformation for an object array, applying a JsonTransformer instance to each array entry.
         *
         * @param newName name of the new json object
         * @param preTransform pre-transformer for the array
         * @param transformer transformer to use for each array element, with combined previous context and the old json as context
         * @param oldNames array of old names that the new entry corresponds to. Used only for keeping entry and comment order.
         * @return this
         */
        public Builder<CTX> objectArrayTransform(String newName,
                EntryTransform<CTX> preTransform,
                JsonTransformer<CombinedContext<CTX>> transformer,
                String... oldNames) {

            EntryTransform<CTX> impl = (oldRoot, newRoot, context) -> {
                CombinedContext<CTX> ctx = new CombinedContext<>(context, oldRoot);

                preTransform.apply(oldRoot, newRoot, context);
                JsonArray oldArray = newRoot.get(JsonArray.class, newName);
                if (oldArray == null) {
                    oldArray = new JsonArray();
                }
                String oldComment = newRoot.getComment(newName);
                JsonArray transformedArray = new JsonArray();
                for (int i = 0; i < oldArray.size(); i++) {
                    JsonObject transformedObject = transformer.transform((JsonObject) oldArray.get(i), ctx);
                    transformedArray.add(transformedObject, oldArray.getComment(i));
                }
                newRoot.put(newName, transformedArray, oldComment);
            };
            return this.transform(newName, impl, oldNames);
        }

        public JsonTransformer<CTX> build() {
            return new JsonTransformer<>(transformName, transformToNew, oldToTransforms, unhandledTransform);
        }
    }

    public static final class CombinedContext<CTX> {

        private final CTX parentCtx;
        private final JsonObject ctx;

        public CombinedContext(CTX parentCtx, JsonObject ctx) {
            this.parentCtx = parentCtx;
            this.ctx = ctx;
        }

        public CTX parent() {
            return parentCtx;
        }

        public JsonObject context() {
            return ctx;
        }

        @Override public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CombinedContext<?> that = (CombinedContext<?>) o;
            return parentCtx.equals(that.parentCtx) &&
                    ctx.equals(that.ctx);
        }

        @Override public int hashCode() {
            return Objects.hash(parentCtx, ctx);
        }
    }

    /**
     * Handles outputting a single json entry to new transformed object.
     */
    @FunctionalInterface
    public interface EntryTransform<CTX> {

        /**
         * Using source json root object and an optional provided context, transforms
         * data from source object and writes it into new object.
         * This method is also responsible for copying comments.
         *
         * @param oldRoot source "old" json object. This object should not be modified.
         * @param newRoot new json object
         * @param context optional context
         */
        void apply(JsonObject oldRoot, JsonObject newRoot, CTX context);
    }

    public interface UnhandledEntryTransform<CTX> {

        void apply(String name, JsonElement oldElement, String comment, JsonObject newRoot, CTX context);
    }

    private static Collection<String> getObjectKeys(JsonObject obj) {
        // this is an ugly hack because Jankson doesn't support it yet
        try {
            Class<?> entryClass = Class.forName("blue.endless.jankson.JsonObject$Entry");
            Field keyField = entryClass.getDeclaredField("key");
            keyField.setAccessible(true);

            Field entries = JsonObject.class.getDeclaredField("entries");
            entries.setAccessible(true);
            List<?> list = (List<?>) entries.get(obj);
            List<String> keys = new ArrayList<>();

            for (Object entry : list) {
                String key = (String) keyField.get(entry);
                keys.add(key);
            }
            return keys;
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
            return obj.keySet();
        }
    }
}
