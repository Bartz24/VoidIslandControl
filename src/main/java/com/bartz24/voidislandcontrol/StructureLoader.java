package com.bartz24.voidislandcontrol;

import java.io.File;

import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class StructureLoader {

	public static TemplateManager tempManager;

	public static void preInit(FMLPreInitializationEvent event) {

		File structFolder = new File(event.getModConfigurationDirectory().getAbsolutePath() + File.separator + References.ModID
				+ "structures");
		if(!structFolder.exists())
			structFolder.mkdirs();
		if (tempManager == null)
			tempManager = new TemplateManager(structFolder.getAbsolutePath(), new DataFixer(0));

	}

}
