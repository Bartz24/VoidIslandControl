package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.VoidIslandControl;
import com.bartz24.voidislandcontrol.config.ConfigOptions;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class WorldOverride
{
	public static void registerWorldProviders()
	{
		if (ConfigOptions.worldGenSettings.netherVoid)
		{
			try
			{
				DimensionManager.unregisterDimension(-1);
				DimensionManager.registerDimension(-1, DimensionType.register("VoidNether",
						"_nether", -1, WorldProviderNetherVoid.class, true));
			} catch (Exception e)
			{
				VoidIslandControl.logger.error("Could not override the nether dimension to be void!");
			}
		}
		if (ConfigOptions.worldGenSettings.endVoid)
		{
			try
			{
				DimensionManager.unregisterDimension(1);
				DimensionManager.registerDimension(1, DimensionType.register("VoidEnd",
						"_end", 1, WorldProviderEndVoid.class, true));
			} catch (Exception e)
			{
				VoidIslandControl.logger.error("Could not override the end dimension to be void!");
			}
		}
	}
}
