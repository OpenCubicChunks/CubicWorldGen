package io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;

public interface IJsonFix {

    JsonObject fix(CustomGeneratorSettingsFixer fixer, JsonObject jsonObject);


    default void copyDirect(String name, JsonObject oldRoot, JsonObject newRoot) {
        copyDirect(name, name, oldRoot, newRoot);
    }

    default void copyDirect(String srcName, String dstName, JsonObject oldRoot, JsonObject newRoot) {
        JsonElement ret = oldRoot.get(srcName);
        newRoot.put(dstName, ret);
        newRoot.setComment(dstName, oldRoot.getComment(srcName));
    }
}
