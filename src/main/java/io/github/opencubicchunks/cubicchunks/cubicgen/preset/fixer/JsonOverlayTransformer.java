package io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonOverlayTransformer {

    private final JsonTransformer<JsonObject> transformer;

    public JsonOverlayTransformer() {
        this.transformer = JsonTransformer.<JsonObject>builder("JsonOverlayTransform")
                .defaultTransform(this::mergeElement)
                .build();
    }

    private void mergeElement(String name, JsonElement overlayElement, String comment, JsonObject newRoot, JsonObject previous) {
        mergeElement(name, overlayElement, comment, newRoot::put, previous.get(name), previous.getComment(name));
    }

    private <T> void mergeElement(T name, JsonElement overlayElement, String comment,
            TriConsumer<T, JsonElement, String> addNew,
            JsonElement previousElement, String previousComment) {

        String newComment = comment;
        if (newComment == null) {
            newComment = previousComment;
        }
        if (previousElement == null) {
            addNew.accept(name, overlayElement, newComment);
            return;
        }
        if (overlayElement instanceof JsonPrimitive && previousElement instanceof JsonPrimitive) {
            addNew.accept(name, overlayElement, newComment);
            return;
        }
        if (overlayElement instanceof JsonObject && previousElement instanceof JsonObject) {
            JsonObject merged = merge((JsonObject) previousElement, (JsonObject) overlayElement);
            addNew.accept(name, merged, newComment);
            return;
        }
        if (overlayElement instanceof JsonArray && previousElement instanceof JsonArray) {
            JsonArray newArr = new JsonArray();
            Set<JsonElement> matched = Sets.newIdentityHashSet();
            int len = Math.min(((JsonArray) overlayElement).size(), ((JsonArray) previousElement).size());
            for (int i = 0; i < len; i++) {
                if (areEquivalent(((JsonArray) overlayElement).get(i), ((JsonArray) previousElement).get(i))) {
                    matched.add(((JsonArray) overlayElement).get(i));
                    mergeElement(i,
                            ((JsonArray) overlayElement).get(i),
                            ((JsonArray) overlayElement).getComment(i),
                            (idx, elem, comm) -> newArr.add(elem, comm),
                            ((JsonArray) previousElement).get(i),
                            ((JsonArray) previousElement).getComment(i));
                }
            }
            // this is a bit of a hack to quickly find what a new element might be equivalent to
            // it relies on hashmap checking the key with equals and hashcode, but returning the
            // actual stored instance for it, so it's possible to do
            // map.get(new EquivalenceWrapper(objectToFindEquivalenceFor))
            Map<EquivalenceWrapper, EquivalenceWrapper> equivalenceMap =
                    ((JsonArray) previousElement).stream()
                            .map(EquivalenceWrapper::new)
                            .collect(Collectors.toMap(x -> x, x -> x, (x, y) -> x));


            Map<JsonElement, String> previousCommentMap = Maps.newIdentityHashMap();
            JsonArray jsonElements = (JsonArray) previousElement;
            for (int i = 0; i < jsonElements.size(); i++) {
                previousCommentMap.put(jsonElements.get(i), jsonElements.getComment(i));
            }
            JsonArray overlayArray = (JsonArray) overlayElement;
            for (int i = 0; i < overlayArray.size(); i++) {
                JsonElement e = overlayArray.get(i);
                if (matched.contains(e)) {
                    continue;
                }
                EquivalenceWrapper previousEqWrapper = equivalenceMap.get(new EquivalenceWrapper(e));
                if (previousEqWrapper == null) {
                    newArr.add(e, ((JsonArray) overlayElement).getComment(i));
                    continue;
                }
                JsonElement previousEq = previousEqWrapper.element;

                mergeElement(i,
                        e, overlayArray.getComment(i),
                        (idx, elem, comm) -> newArr.add(elem, comm),
                        previousEq,
                        previousCommentMap.get(previousEq));
            }
            addNew.accept(name, newArr, newComment);
        }

    }

    public JsonObject merge(JsonObject previous, JsonObject overlay) {
        return transformer.transform(overlay, previous);
    }

    // heuristics for determining json equivalence to merge comments

    private static boolean areEquivalent(JsonElement obj1, JsonElement obj2) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 instanceof JsonPrimitive && obj2 instanceof JsonPrimitive) {
            return ((JsonPrimitive) obj1).asString().equals(((JsonPrimitive) obj2).asString());
        }
        if (obj1 instanceof JsonNull && obj2 instanceof JsonNull) {
            return true;
        }
        if (obj1 instanceof JsonObject && obj2 instanceof JsonObject) {
            Set<String> prevKeys = ((JsonObject) obj1).keySet();
            Set<String> newKeys = ((JsonObject) obj2).keySet();

            Set<String> common = new HashSet<>(prevKeys);
            common.retainAll(newKeys);

            if (common.isEmpty() && (!prevKeys.isEmpty() || !newKeys.isEmpty())) {
                return false;
            }
            for (String key : common) {
                if (!areEquivalent(((JsonObject) obj1).get(key), ((JsonObject) obj2).get(key))) {
                    return false;
                }
            }
            // assume they are true, this is the best we can do
            return true;
        }
        if (obj1 instanceof JsonArray && obj2 instanceof JsonArray) {
            if (((JsonArray) obj1).isEmpty() && ((JsonArray) obj2).isEmpty()) {
                return true;
            }
            // if one of them is empty, assume equivalent
            if (((JsonArray) obj1).isEmpty() != ((JsonArray) obj2).isEmpty()) {
                return true;
            }
            Set<EquivalenceWrapper> set = ((JsonArray) obj1).stream().map(EquivalenceWrapper::new).collect(Collectors.toSet());
            Set<EquivalenceWrapper> arr2Set = ((JsonArray) obj2).stream().map(EquivalenceWrapper::new).collect(Collectors.toSet());

            // if there are any common elements, assume arrays are equivalent
            set.retainAll(arr2Set);
            return !set.isEmpty();
        }
        // they are not equivalent of they have different types
        return false;
    }

    private static int equivalenceHash(JsonElement obj) {
        if (obj instanceof JsonPrimitive) {
            return obj.hashCode();
        }
        if (obj instanceof JsonNull) {
            return 123456789;
        }
        // give up for objects and arrays
        if (obj instanceof JsonObject) {
            return 123451234;
        }
        if (obj instanceof JsonArray) {
            return 123123123;
        }
        return 0;
    }

    private static final class EquivalenceWrapper {

        private final JsonElement element;

        private EquivalenceWrapper(JsonElement element) {
            this.element = element;
        }

        @Override public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            EquivalenceWrapper that = (EquivalenceWrapper) o;
            return areEquivalent(element, that.element);
        }

        @Override public int hashCode() {
            return equivalenceHash(element);
        }
    }
}
