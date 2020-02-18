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
package io.github.opencubicchunks.cubicchunks.cubicgen.asm;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

public class CubicGenMixinConfig implements IMixinConfigPlugin {

    @Nonnull
    public static Logger LOGGER = LogManager.getLogger("CubicGenMixinConfig");
    private boolean aloowFarLands;

    @Override public void onLoad(String s) {
        File folder = new File(".", "config");
        folder.mkdirs();
        File configFile = new File(folder, "cubicworldgen_mixin_config.json");
        LOGGER.info("Loading configuration file " + configFile.getAbsolutePath());
        try {
            if (!configFile.exists()) {
                this.writeConfigToJson(configFile);
            }
            this.readConfigFromJson(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readConfigFromJson(File configFile) throws IOException {
        JsonReader reader = new JsonReader(new FileReader(configFile));

        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("allow_farlands_worldgen")) {
                    aloowFarLands = reader.nextBoolean();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }
        reader.endArray();
        reader.close();
    }

    private void writeConfigToJson(File configFile) throws IOException {
        JsonWriter writer = new JsonWriter(new FileWriter(configFile));
        writer.setIndent(" ");
        writer.beginArray();

        writer.beginObject();
        writer.name("allow_farlands_worldgen");
        writer.value(false);
        writer.name("description");
        writer.value("Removes a part of code that prevents far lands known from Minecraft beta 1.7 and earlier from generating in CustomCubic");
        writer.endObject();

        writer.endArray();
        writer.close();
    }

    @Override public String getRefMapperConfig() {
        return null;
    }

    @Override public boolean shouldApplyMixin(String target, String mixin) {
        if (target.equals("com.flowpowered.noise.Utils")) {
            return aloowFarLands;
        }
        return true;
    }

    @Override public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override public List<String> getMixins() {
        return null;
    }

    @Override public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
