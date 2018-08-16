package com.bartz24.voidislandcontrol.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class IslandGen {
	public String Identifier;
	public BlockPos spawnOffset;

	public IslandGen(String id, BlockPos spawnOffset) {
		Identifier = id;
		this.spawnOffset = spawnOffset;
	}

	public void generate(World world, BlockPos pos) {

	}
}
