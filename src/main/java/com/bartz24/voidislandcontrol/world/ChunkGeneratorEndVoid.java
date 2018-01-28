package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.config.ConfigOptions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorEnd;

public class ChunkGeneratorEndVoid extends ChunkGeneratorEnd {
	World world;

	public ChunkGeneratorEndVoid(World par1World, long par2) {
		super(par1World, ConfigOptions.worldGenSettings.endVoidStructures, par2,
				new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0));
		world = par1World;
	}

	@Override
	public void setBlocksInChunk(int x, int z, ChunkPrimer primer) {
	}

	@Override
	public void buildSurfaces(ChunkPrimer primer) {
	}
}
