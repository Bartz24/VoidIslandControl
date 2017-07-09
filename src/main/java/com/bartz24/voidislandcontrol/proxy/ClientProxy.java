package com.bartz24.voidislandcontrol.proxy;

import com.bartz24.voidislandcontrol.ClientEventHandler;
import com.bartz24.voidislandcontrol.References;

import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent e)
	{
		super.preInit(e);

		OBJLoader.INSTANCE.addDomain(References.ModID);
	}

	@Override
	public void init(FMLInitializationEvent e)
	{
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
		super.init(e);
	}
}
