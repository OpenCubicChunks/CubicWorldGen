package io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import joptsimple.internal.Strings;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Converts old v3 mappings json file, into a jankson JsonObject.
 * It can't be loaded directly because:
 *  * GSON is not strictly a subset of what jankson supports
 *  * jankson doesn't (yet) support dealing with duplicate keys,
 *  which were used as a workaround for lack of comment support in gson
 *
 * This pre-processor, will automatically try to convert known commonly used comment
 * formats to proper comments, and put all redundant key duplicates into comments.
 *
 * Because neither JsonObject implementation supports duplicate keys, and the
 * transformation can't be done in place while reading, the preprocessor has a custom
 * minimalistic version of JsonObject (ParsedJson) and a json loader.
 *
 * V3 preprocessor will attempt to ONLY process json input that is v3 or earlier (unspecified).
 * This is done to avoid the potential need to support (potentially mutually exclusive) both gson
 * and jankson quirks when loading the same file.
 *
 * Loading V3 layers in V4+ preset is currently going to be explicitly unsupported, because if the
 * special inheritance mechanisms in V3, that are intentionally removed from V4+ as the reason for it
 * no longer applies (64k character limit for the whole preset, coming from DataInput/OutputStream limit)
 */
public class V3Preprocessor {

    private static class ParsedJson {

        static final ParsedJson NULL = new ParsedJson((Void) null);
        private final Object value;

        private List<String> comments = new ArrayList<>();

        private ParsedJson(Void _null) {
            this.value = _null;
        }

        ParsedJson(String value) {
            this.value = value;
        }

        ParsedJson(Number value) {
            this.value = value;
        }

        ParsedJson(boolean value) {
            this.value = value;
        }

        ParsedJson(ParsedJson[] array) {
            this.value = array;
        }

        ParsedJson(List<Map.Entry<String, ParsedJson>> object) {
            this.value = object;
        }

        ParsedJson(ParsedJson in) {
            if (in.isArray()) {
                ParsedJson[] src = in.getArray();
                ParsedJson[] dst = new ParsedJson[src.length];
                for (int i = 0; i < src.length; i++) {
                    dst[i] = new ParsedJson(src[i]);
                }
                this.value = dst;
            } else if (in.isObject()) {
                List<Map.Entry<String, ParsedJson>> src = in.getObject();
                List<Map.Entry<String, ParsedJson>> dst = new ArrayList<>(src.size());
                for (Map.Entry<String, ParsedJson> e : src) {
                    dst.add(new AbstractMap.SimpleEntry<>(
                            e.getKey(),
                            new ParsedJson(e.getValue())
                    ));
                }
                this.value = dst;
            } else {
                this.value = in.value; // immutable
            }
            this.comments = new ArrayList<>(in.comments);
        }

        void addComments(Collection<String> comments) {
            this.comments.addAll(comments);
        }

        private boolean isNull() {
            return value == null;
        }

        private boolean isString() {
            return value instanceof String;
        }

        private String getString() {
            return (String) value;
        }

        private boolean isNumber() {
            return value instanceof Number;
        }

        private Number getNumber() {
            return (Number) value;
        }

        private boolean isBoolean() {
            return value instanceof Boolean;
        }

        private boolean getBoolean() {
            return (Boolean) value;
        }

        private boolean isArray() {
            return value instanceof ParsedJson[];
        }

        private ParsedJson[] getArray() {
            return (ParsedJson[]) value;
        }

        private boolean isObject() {
            return value instanceof List;
        }

        @SuppressWarnings("unchecked") private List<Map.Entry<String, ParsedJson>> getObject() {
            return (List<Map.Entry<String, ParsedJson>>) value;
        }

        String asString(int indent) {
            StringBuilder sb = new StringBuilder(indent);
            for (int i = 0; i < indent; i++) {
                sb.append(' ');
            }
            if (isNull()) {
                return sb.append("null").toString();
            }
            if (isString()) {
                return sb.append(getString()).toString();
            }
            if (isNumber()) {
                return sb.append(getNumber()).toString();
            }
            if (isBoolean()) {
                return sb.append(getBoolean()).toString();
            }
            if (isArray()) {
                sb.append("[\n");
                ParsedJson[] arr = getArray();
                for (ParsedJson val : arr) {
                    sb.append(val.asString(indent + 2)).append("\n");
                }
                return sb.append(']').toString();
            }
            assert isObject();
            sb.append("{\n");
            List<Map.Entry<String, ParsedJson>> obj = getObject();
            for (Map.Entry<String, ParsedJson> val : obj) {
                sb.append(val.getKey()).append(": ").append(val.getValue().asString(indent + 2)).append('\n');
            }
            sb.append("}");
            return sb.toString();
        }

        JsonElement toJankson() {
            JsonElement val;
            if (isNull()) {
                val = JsonNull.INSTANCE;
            } else if (isObject()) {
                JsonObject json = new JsonObject();
                List<Map.Entry<String, ParsedJson>> obj = getObject();
                for (Map.Entry<String, ParsedJson> entry : obj) {
                    json.put(entry.getKey(), entry.getValue().toJankson());
                    if (!entry.getValue().comments.isEmpty()) {
                        json.setComment(entry.getKey(), Strings.join(entry.getValue().comments, "\n"));
                    }
                }
                val = json;
            } else if (isArray()) {
                JsonArray json = new JsonArray();
                ParsedJson[] array = getArray();
                for (int i = 0; i < array.length; i++) {
                    ParsedJson e = array[i];
                    json.add(e.toJankson());
                    if (!e.comments.isEmpty()) {
                        json.setComment(i, Strings.join(e.comments, "\n"));
                    }
                }
                val = json;
            } else if (isString()) {
                val = new JsonPrimitive(getString());
            } else if (isNumber()) {
                val = new JsonPrimitive(getNumber());
            } else {
                assert isBoolean();
                val = new JsonPrimitive(getBoolean());
            }
            return val;
        }
    }

    private ParsedJson loadForPreprocess(JsonReader in) throws IOException {
        switch (in.peek()) {
            case STRING: {
                try {
                    return new ParsedJson(in.nextDouble());
                } catch (MalformedJsonException | IllegalStateException | NumberFormatException ignored) {
                    return new ParsedJson(in.nextString());
                }
            }
            case NUMBER:
                String number = in.nextString();
                return new ParsedJson(new LazilyParsedNumber(number));
            case BOOLEAN:
                return new ParsedJson(in.nextBoolean());
            case NULL:
                in.nextNull();
                return ParsedJson.NULL;
            case BEGIN_ARRAY:
                List<ParsedJson> arr = new ArrayList<>();
                in.beginArray();
                while (in.hasNext()) {
                    arr.add(loadForPreprocess(in));
                }
                in.endArray();
                return new ParsedJson(arr.toArray(new ParsedJson[0]));
            case BEGIN_OBJECT:
                List<Map.Entry<String, ParsedJson>> obj = new ArrayList<>();
                in.beginObject();
                while (in.hasNext()) {
                    obj.add(new AbstractMap.SimpleEntry<>(
                            in.nextName(),
                            loadForPreprocess(in)
                    ));
                }
                in.endObject();
                return new ParsedJson(obj);
            case END_DOCUMENT:
            case NAME:
            case END_OBJECT:
            case END_ARRAY:
            default:
                throw new IllegalArgumentException();
        }
    }

    private ParsedJson preprocess(ParsedJson in) {
        if (!in.isObject() && !in.isArray()) {
            return new ParsedJson(in);
        }
        if (in.isArray()) {
            ParsedJson[] out = new ParsedJson[in.getArray().length];
            for (int i = 0; i < in.getArray().length; i++) {
                out[i] = preprocess(in.getArray()[i]);
            }
            return new ParsedJson(out);
        }
        // step 1: turn everything that contains a comment into an actual comment
        List<Map.Entry<String, ParsedJson>> inObj = in.getObject();
        List<Map.Entry<String, ParsedJson>> outObj = new ArrayList<>();

        List<String> commentsToAdd = new ArrayList<>();
        for (Map.Entry<String, ParsedJson> e : inObj) {
            if (e.getKey().toLowerCase(Locale.ROOT).contains("comment")) {
                commentsToAdd.add(e.getValue().asString(0));
            } else {
                ParsedJson newVal = preprocess(e.getValue());
                newVal.addComments(commentsToAdd);
                commentsToAdd.clear();
                outObj.add(new AbstractMap.SimpleEntry<>(e.getKey(), newVal));
            }
        }
        if (!commentsToAdd.isEmpty()) {
            outObj.get(outObj.size() - 1).getValue().addComments(commentsToAdd);
        }

        inObj = outObj;
        outObj = new ArrayList<>();

        Set<String> seenNames = new HashSet<>();
        List<String> commentedOut = new ArrayList<>();
        ParsedJson prev = null;
        for (int i = inObj.size() - 1; i >= 0; i--) {
            Map.Entry<String, ParsedJson> e = inObj.get(i);
            if (!seenNames.contains(e.getKey())) {
                if (!commentedOut.isEmpty()) {
                    prev.addComments(commentedOut);
                }
                seenNames.add(e.getKey());
                ParsedJson cloned = new ParsedJson(e.getValue());
                prev = cloned;
                outObj.add(0, new AbstractMap.SimpleEntry<>(e.getKey(), cloned));
            } else {
                commentedOut.add(e.getKey() + ": " + e.getValue().asString(0));
            }
        }
        if (!commentedOut.isEmpty()) {
            prev.addComments(commentedOut);
        }
        return new ParsedJson(outObj);
    }

    public JsonObject load(String json) throws PresetLoadError {
        Exception gsonException = null;
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().serializeSpecialFloatingPointValues().create();
            JsonReader reader = gson.newJsonReader(new StringReader(json));
            ParsedJson outJson = loadForPreprocess(reader);
            Map.Entry<String, ParsedJson> version =
                    Streams.findLast(outJson.getObject().stream().filter(e -> e.getKey().equals("version")))
                            .orElse(new AbstractMap.SimpleEntry<>("version", new ParsedJson(3)));
            if (!version.getValue().isNumber()) {
                throw new RuntimeException("Invalid preset, version is not a number!");
            }
            int v = version.getValue().getNumber().intValue();
            if (v <= 3) {
                outJson = preprocess(outJson);
                return (JsonObject) outJson.toJankson();
            }
        } catch (JsonParseException | MalformedJsonException ex) {
            CustomCubicMod.LOGGER.catching(ex);
            gsonException = ex;
            // this is probably v4+ that uses jankson-supported quirks. If parsing with jankson fails, consider it invalid
        } catch (IOException e) {
            throw new PresetLoadError(e);
        }
        Jankson jankson = blue.endless.jankson.Jankson
                .builder()
                .build();
        try {
            JsonObject configObject = jankson.load(json);
            if (((JsonPrimitive) configObject.getOrDefault("version", new JsonPrimitive(3))).asInt(3) <= 3) {
                throw new RuntimeException("Preset version 3 or older did not get parsed using GSON!");
            }
            return configObject;
        } catch (SyntaxError err) {
            if (gsonException != null) {
                StringBuilder msg = new StringBuilder();
                msg.append("Unable to load preset json due to syntax errors.\nV3 load attempt error: ")
                        .append(gsonException.getMessage()).append("\n")
                        .append("V4+ load attempt error:\n")
                        .append(err.getMessage()).append("\n")
                        .append(err.getLineMessage());
                throw new PresetLoadError(msg.toString(), err);
            } else {
                StringBuilder msg = new StringBuilder();
                msg.append("Unable to load preset json due to syntax errors:\n")
                        .append(err.getMessage()).append("\n")
                        .append(err.getLineMessage());
                throw new PresetLoadError(msg.toString(), err);
            }
        }
    }

    public JsonObject load(Path jsonFile) throws PresetLoadError {
        String json;
        try {
            json = new String(Files.readAllBytes(jsonFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PresetLoadError(e);
        }
        return load(json);
    }
}
