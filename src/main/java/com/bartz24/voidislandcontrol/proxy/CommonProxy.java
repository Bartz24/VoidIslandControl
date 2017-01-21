package com.bartz24.voidislandcontrol.proxy;

import com.bartz24.voidislandcontrol.EventHandler;
import com.bartz24.voidislandcontrol.IslandRegistry;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.world.WorldOverride;
import com.bartz24.voidislandcontrol.world.WorldTypeVoid;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
	EventHandler events = new EventHandler();

	public void preInit(FMLPreInitializationEvent e) {
		ConfigOptions.loadConfigThenSave(e);
		new WorldTypeVoid();
		IslandRegistry.initIslands();
	}

	public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(events);

		WorldOverride.registerWorldProviders();
	}

	public void postInit(FMLPostInitializationEvent e) {
	}
}
