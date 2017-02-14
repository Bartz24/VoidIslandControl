package com.bartz24.voidislandcontrol.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.botania.common.world.SkyblockWorldEvents;

public class GoGSupport {
	public static void spawnGoGIsland(World world, BlockPos pos)
	{
		SkyblockWorldEvents.createSkyblock(world, pos.down(2));
	}
}
