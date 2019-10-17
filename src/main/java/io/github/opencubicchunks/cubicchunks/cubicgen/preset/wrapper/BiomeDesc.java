package io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;

import javax.annotation.Nullable;

public class BiomeDesc {

    private final String biomeId;
    private final Biome biome;

    public BiomeDesc(String biomeId) {
        this.biomeId = biomeId;
        ResourceLocation id = new ResourceLocation(biomeId);
        if (!ForgeRegistries.BIOMES.containsKey(id)) {
            this.biome = null;
            return;
        }
        this.biome = ForgeRegistries.BIOMES.getValue(id);
    }

    public BiomeDesc(Biome biome) {
        this.biome = biome;
        this.biomeId = ForgeRegistries.BIOMES.getKey(biome).toString();
    }

    public String getBiomeId() {
        return biomeId;
    }

    @Nullable public Biome getBiome() {
        return biome;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BiomeDesc biomeDesc = (BiomeDesc) o;
        return biomeId.equals(biomeDesc.biomeId);
    }

    @Override public int hashCode() {
        return Objects.hash(biomeId);
    }

    @Override public String toString() {
        return "BiomeDesc{" +
                "biomeId='" + biomeId + '\'' +
                '}';
    }
}
