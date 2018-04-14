package com.bartz24.voidislandcontrol.world;

import java.util.List;

import com.bartz24.voidislandcontrol.config.ConfigOptions;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorHell;

public class ChunkGeneratorNetherVoid extends ChunkGeneratorHell {
    World world;

    public ChunkGeneratorNetherVoid(World par1World, long par2) {
        super(par1World, ConfigOptions.worldGenSettings.netherVoidStructures, par2);
        world = par1World;
    }

    @Override
    public void prepareHeights(int p_185936_1_, int p_185936_2_, ChunkPrimer primer) {
        if (!ConfigOptions.worldGenSettings.netherVoid)
            super.prepareHeights(p_185936_1_, p_185936_2_, primer);
    }

    @Override
    public void buildSurfaces(int p_185937_1_, int p_185937_2_, ChunkPrimer primer) {
        if (!ConfigOptions.worldGenSettings.netherVoid)
            super.buildSurfaces(p_185937_1_, p_185937_2_, primer);
    }
}
