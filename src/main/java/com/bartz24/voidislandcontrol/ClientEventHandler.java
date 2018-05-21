package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.world.WorldTypeVoid;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientEventHandler {
    @SubscribeEvent
    public void playerUpdate(LivingUpdateEvent event) {

        if (event.getEntityLiving() instanceof EntityPlayer && event.getEntityLiving().world.isRemote) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (player.getEntityWorld().getWorldInfo().getTerrainType() instanceof WorldTypeVoid
                    && player.dimension == ConfigOptions.worldGenSettings.baseDimension && Minecraft.getMinecraft().player != null && !IslandManager.worldOneChunk && !ConfigOptions.otherSettings.hideToasts) {
                boolean atSpawn = Math.abs(player.posX) < 100 && Math.abs(player.posZ) < 100;
                if (atSpawn && Minecraft.getMinecraft().player.getGameProfile().getId()
                        .equals(player.getGameProfile().getId())) {
                    if (Minecraft.getMinecraft().getToastGui().getToast(IslandToast.class,
                            IslandToast.Type.Island) == null)
                        Minecraft.getMinecraft().getToastGui().add(new IslandToast(
                                new TextComponentString("Create an island!"),
                                new TextComponentString("/" + ConfigOptions.commandSettings.commandName + " for help")));
                } else if (!atSpawn && Minecraft.getMinecraft().player.getGameProfile().getId()
                        .equals(player.getGameProfile().getId())) {
                    if (Minecraft.getMinecraft().getToastGui().getToast(IslandToast.class,
                            IslandToast.Type.Island) != null)
                        Minecraft.getMinecraft().getToastGui().getToast(IslandToast.class, IslandToast.Type.Island)
                                .hide();
                }
            } else {
                if (Minecraft.getMinecraft().getToastGui().getToast(IslandToast.class,
                        IslandToast.Type.Island) != null)
                    Minecraft.getMinecraft().getToastGui().getToast(IslandToast.class, IslandToast.Type.Island)
                            .hide();
            }

        }
    }
}
