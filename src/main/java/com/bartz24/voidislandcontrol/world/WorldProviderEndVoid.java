package com.bartz24.voidislandcontrol.world;

import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderEndVoid extends WorldProviderEnd
{

	@Override
	public IChunkGenerator createChunkGenerator() {
		if (getDimension() == 1)
			return new ChunkGeneratorEndVoid(world, world.getSeed());
		return super.createChunkGenerator();
	}

}
