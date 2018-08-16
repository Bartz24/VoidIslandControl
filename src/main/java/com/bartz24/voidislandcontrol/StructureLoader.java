package com.bartz24.voidislandcontrol;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

public class StructureLoader {

    public static TemplateManager tempManager;

    public static void preInit(FMLPreInitializationEvent event) {

        File structFolder = new File(event.getModConfigurationDirectory().getAbsolutePath() + File.separator + References.ModID
                + "structures");
        if (!structFolder.exists())
            structFolder.mkdirs();
        if (tempManager == null)
            tempManager = new TemplateManager(structFolder.getAbsolutePath(), new DataFixer(0)) {
                @Nullable
                public Template get(@Nullable MinecraftServer server, ResourceLocation templatePath) {
                    String s = templatePath.getResourcePath();
                    final Map<String, Template> templates = ObfuscationReflectionHelper.getPrivateValue(TemplateManager.class, this, "templates");
                    if (templates.containsKey(s)) {
                        return templates.get(s);
                    } else {
                        this.readTemplate(templatePath);

                        return templates.containsKey(s) ? (Template) templates.get(s) : null;
                    }
                }
            };

    }

}
