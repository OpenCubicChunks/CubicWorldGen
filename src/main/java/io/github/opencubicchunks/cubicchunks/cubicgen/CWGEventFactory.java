package io.github.opencubicchunks.cubicchunks.cubicgen;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.event.CubicOreGenEvent;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.event.DecorateCubeBiomeEvent;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.event.PopulateCubeEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Random;

public class CWGEventFactory {

    public static boolean populate(World world, Random rand,
        int cubeX, int cubeY, int cubeZ, boolean hasVillageGenerated, PopulateChunkEvent.Populate.EventType type) {
        PopulateCubeEvent.Populate event = new PopulateCubeEvent.Populate(world, rand,
            cubeX, cubeY, cubeZ, hasVillageGenerated, type);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.getResult() != Event.Result.DENY;
    }

    public static boolean populate(World world, Random rand,
        CubePos pos, boolean hasVillageGenerated, PopulateChunkEvent.Populate.EventType type) {
        return populate(world, rand, pos.getX(), pos.getY(), pos.getZ(), hasVillageGenerated, type);
    }

    public static boolean generateOre(World world, Random rand, WorldGenerator generator, CubePos pos, IBlockState type) {
        CubicOreGenEvent.GenerateMinable event = new CubicOreGenEvent.GenerateMinable(world, rand, generator, pos, type);
        MinecraftForge.ORE_GEN_BUS.post(event);
        return event.getResult() != Event.Result.DENY;
    }

    public static boolean decorate(World world, Random rand, CubePos chunkPos, DecorateBiomeEvent.Decorate.EventType type) {
        DecorateCubeBiomeEvent.Decorate event = new DecorateCubeBiomeEvent.Decorate(world, rand, chunkPos, null, type);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.getResult() != Event.Result.DENY;
    }

}
