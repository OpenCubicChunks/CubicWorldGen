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
package io.github.opencubicchunks.cubicchunks.cubicgen.asm.coremod;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
// the mcVersion value is inlined at compile time, so this MC version check may still fail
@IFMLLoadingPlugin.MCVersion(value = ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(value = 5000)
public class CubicGenCoreMod implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        // normally mixin would be initialized in constructor
        // but we don't actually have mixin in our jar, and depend on CubicChunks containing mixin
        // which means that according to FML, we are not a tweaker
        // so our coremod is loaded normally by FML instead of mixin
        // if CWG initialized Mixin in constructor, it would initialize mixin too early
        // which breaks ViveCraft.
        // we also can't just declare mixin tweaker and specify mixin config in manifest
        // because of a mixin bug where it will inject the transformers before FML deobf transformer.
        Mixins.addConfiguration("cubicgen.mixins.json");
        return new String[]{"io.github.opencubicchunks.cubicchunks.cubicgen.asm.coremod.MapGenStrongholdCubicConstructorTransform"};
    }

    @Nullable @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Nullable @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
