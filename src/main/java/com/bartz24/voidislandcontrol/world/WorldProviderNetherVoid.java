package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.config.ConfigOptions;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderNetherVoid extends WorldProviderHell {

	@Override
	public IChunkGenerator createChunkGenerator() {
		if (getDimension() == -1 && ConfigOptions.worldGenSettings.netherVoid)
			return new ChunkGeneratorNetherVoid(world, world.getSeed());
		return super.createChunkGenerator();
	}

}
