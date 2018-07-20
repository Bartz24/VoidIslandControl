package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.config.ConfigOptions.CommandSettings.CommandBlockType;
import com.bartz24.voidislandcontrol.world.WorldTypeVoid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.command.CommandException;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventHandler {
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onOpenGui(GuiOpenEvent e) {
        if (e.getGui() instanceof GuiCreateWorld
                && Minecraft.getMinecraft().currentScreen instanceof GuiWorldSelection) {
            // Thanks YUNoMakeGoodMap :D
            GuiCreateWorld cw = (GuiCreateWorld) e.getGui();
            ReflectionHelper.setPrivateValue(GuiCreateWorld.class, cw, getType(), "field_146331_K", "selectedIndex");
        }
    }

    private int getType() {
        for (int i = 0; i < WorldType.WORLD_TYPES.length; i++) {
            if (WorldType.WORLD_TYPES[i] instanceof WorldTypeVoid)
                return i;
        }
        return 0;
    }

    @SubscribeEvent
    public void playerUpdate(LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && !event.getEntity().getEntityWorld().isRemote) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();


            if (player.getEntityWorld().getWorldInfo().getTerrainType() instanceof WorldTypeVoid) {
                if (IslandManager.spawnedPlayers.size() == 0
                        || !IslandManager.hasPlayerSpawned(player.getGameProfile().getId())) {
                    IslandManager.tpPlayerToPos(player, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0));
                    World world = player.getEntityWorld();
                    if (world.getSpawnPoint().getX() != 0 || world.getSpawnPoint().getZ() != 0)
                        world.setSpawnPoint(new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0));
                    BlockPos spawn = new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0);

                    if (!IslandManager.hasPosition(0, 0)) {
                        IslandManager.CurrentIslandsList.add(new IslandPos(0, 0));
                        createSpawn(player, player.getEntityWorld(), spawn);
                    }

                    if (ConfigOptions.islandSettings.autoCreate && !IslandManager.worldOneChunk) {

                        if (player instanceof EntityPlayerMP) {
                            try {
                                PlatformCommand.newPlatform((EntityPlayerMP) player,
                                        new String[]{"create", "bypass"});
                            } catch (CommandException e) {
                                player.sendMessage(new TextComponentString(e.getMessage()));
                            }
                        }
                    } else {

                        if (ConfigOptions.islandSettings.oneChunk) {
                            WorldBorder border = event.getEntityLiving().getEntityWorld().getMinecraftServer().worlds[0]
                                    .getWorldBorder();

                            border.setCenter(0, 0);
                            border.setTransition(16);
                            border.setWarningDistance(1);

                            IslandManager.worldOneChunk = true;
                        }

                        spawnPlayer(player, spawn, false);
                        player.extinguish();
                    }
                    IslandManager.spawnedPlayers.add(player.getGameProfile().getId().toString());
                }
            }

            if (player.getEntityWorld().getWorldInfo().getTerrainType() instanceof WorldTypeVoid
                    && IslandManager.hasVisitLoc(player) && player.dimension == ConfigOptions.worldGenSettings.baseDimension && !player.isCreative()) {
                if (player instanceof EntityPlayerMP
                        && ((EntityPlayerMP) player).interactionManager.getGameType() != GameType.SPECTATOR)
                    player.setGameType(GameType.SPECTATOR);
                int posX = IslandManager.getVisitLoc(player).getX() * ConfigOptions.islandSettings.islandDistance;
                int posY = IslandManager.getVisitLoc(player).getY() * ConfigOptions.islandSettings.islandDistance;
                if (ConfigOptions.islandSettings.islandProtection && (
                        Math.abs(player.posX - posX) > ConfigOptions.islandSettings.protectionBuildRange
                                || Math.abs(player.posZ - posY) > ConfigOptions.islandSettings.protectionBuildRange)) {
                    player.sendMessage(
                            new TextComponentString(TextFormatting.RED + "You can't be visiting that far away!"));
                    player.setGameType(GameType.SURVIVAL);
                    IslandManager.removeVisitLoc(player);
                    IslandManager.tpPlayerToPos(player,
                            new BlockPos(posX, ConfigOptions.islandSettings.islandYLevel, posY));
                }
            }

            if (player.getEntityWorld().getWorldInfo().getTerrainType() instanceof WorldTypeVoid
                    && player.dimension == ConfigOptions.worldGenSettings.baseDimension && !player.isCreative() && !IslandManager.hasVisitLoc(player)) {
                if (ConfigOptions.islandSettings.islandProtection && (Math.abs(player.posX) > ConfigOptions.islandSettings.protectionBuildRange
                        || Math.abs(player.posZ) > ConfigOptions.islandSettings.protectionBuildRange)) {
                    IslandPos pos = IslandManager.getPlayerIsland(player.getGameProfile().getId());
                    int posX = pos == null ? 0 : (pos.getX() * ConfigOptions.islandSettings.islandDistance);
                    int posY = pos == null ? 0 : (pos.getY() * ConfigOptions.islandSettings.islandDistance);
                    if (ConfigOptions.islandSettings.islandProtection && (Math.abs(player.posX - posX) > ConfigOptions.islandSettings.protectionBuildRange
                            || Math.abs(player.posZ - posY) > ConfigOptions.islandSettings.protectionBuildRange)) {
                        player.sendMessage(
                                new TextComponentString(TextFormatting.RED + "You can't be away from your island or spawn that far away!"));
                        player.setGameType(GameType.SURVIVAL);
                        IslandManager.removeVisitLoc(player);
                        IslandManager.tpPlayerToPos(player,
                                new BlockPos(posX, ConfigOptions.islandSettings.islandYLevel, posY));
                    }
                }
            }

            List<IslandPos> removeAt = new ArrayList<>();
            if (IslandManager.hasJoinLoc(player)) {
                int time = IslandManager.getJoinTime(player);
                if (time > 0)
                    IslandManager.setJoinTime(player, time - 1);
                else
                    IslandManager.removeJoinLoc(player);
            }

            loadWorld(player);
        }
    }

    private static void loadWorld(EntityPlayer player) {
        if (!IslandManager.worldLoaded) {
            for (String s : ConfigOptions.commandSettings.worldLoadCommands) {
                if (!StringUtils.isBlank(s))
                    player.getEntityWorld().getMinecraftServer().getCommandManager()
                            .executeCommand(new EntityCow(player.getEntityWorld()) {
                                public boolean canUseCommand(int permLevel, String commandName) {
                                    return true;
                                }
                            }, s);
            }
        }
        IslandManager.worldLoaded = true;
    }

    public static void spawnPlayer(EntityPlayer player, BlockPos pos, boolean spawnPlat) {
        if (spawnPlat)
            createSpawn(player, player.getEntityWorld(), pos);

        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP pmp = (EntityPlayerMP) player;
            IslandManager.tpPlayerToPosSpawn(player, pos.up(4));

            IslandManager.setStartingInv(pmp);
        }
    }

    public static void spawnPlayer(EntityPlayer player, BlockPos pos, int forceType) {
        spawnPlayer(player, pos, false);

        spawnPlat(player, player.getEntityWorld(), pos, forceType);
    }

    public static void createSpawn(EntityPlayer player, World world, BlockPos spawn) {
        if (spawn.getX() == 0 && spawn.getZ() == 0 && !IslandManager.worldOneChunk) {
            if (ConfigOptions.islandSettings.islandMainSpawnType.equals("bedrock"))
                mainSpawn(world, spawn);
            else {
                Random random = world.rand;
                int type = ConfigOptions.islandSettings.islandMainSpawnType.equals("random")
                        ? random.nextInt(IslandManager.IslandGenerations.size())
                        : IslandManager.getIndexOfIslandType(ConfigOptions.islandSettings.islandMainSpawnType);

                spawnPlat(null, world, spawn, type);
            }

            return;
        }

        Random random = world.rand;
        int type = ConfigOptions.islandSettings.islandSpawnType.equals("random")
                ? random.nextInt(IslandManager.IslandGenerations.size())
                : IslandManager.getIndexOfIslandType(ConfigOptions.islandSettings.islandSpawnType);

        spawnPlat(player, world, spawn, type);
    }

    private static void spawnPlat(@Nullable EntityPlayer player, World world, BlockPos spawn, int type) {
        IslandManager.IslandGenerations.get(type).generate(world, spawn);

        if (ConfigOptions.commandSettings.commandBlockType != CommandBlockType.NONE) {
            Block cmdBlock = null;
            if (ConfigOptions.commandSettings.commandBlockType == CommandBlockType.IMPULSE)
                cmdBlock = Blocks.COMMAND_BLOCK;
            else if (ConfigOptions.commandSettings.commandBlockType == CommandBlockType.CHAIN)
                cmdBlock = Blocks.CHAIN_COMMAND_BLOCK;
            else if (ConfigOptions.commandSettings.commandBlockType == CommandBlockType.REPEATING)
                cmdBlock = Blocks.REPEATING_COMMAND_BLOCK;

            if (cmdBlock != null) {
                world.setBlockState(
                        spawn.down(3).add(ConfigOptions.commandSettings.commandBlockPos.x,
                                ConfigOptions.commandSettings.commandBlockPos.y,
                                ConfigOptions.commandSettings.commandBlockPos.z),
                        cmdBlock.getDefaultState().withProperty(BlockCommandBlock.FACING,
                                ConfigOptions.commandSettings.commandBlockDirection),
                        3);
                TileEntityCommandBlock te = (TileEntityCommandBlock) world
                        .getTileEntity(spawn.down(3).add(ConfigOptions.commandSettings.commandBlockPos.x,
                                ConfigOptions.commandSettings.commandBlockPos.y,
                                ConfigOptions.commandSettings.commandBlockPos.z));
                te.getCommandBlockLogic().setCommand(ConfigOptions.commandSettings.commandBlockCommand);
                te.setAuto(ConfigOptions.commandSettings.commandBlockAuto);
            }
        }

        if (player != null) {
            IslandPos position = IslandManager.getNextIsland();
            IslandManager.CurrentIslandsList.add(new IslandPos(IslandManager.IslandGenerations.get(type).Identifier,
                    position.getX(), position.getY(), player.getGameProfile().getId()));
        }
    }

    private static void mainSpawn(World world, BlockPos spawn) {
        for (int x = -(int) Math.floor((float) ConfigOptions.islandSettings.islandSize / 2F); x <= (int) Math
                .floor((float) ConfigOptions.islandSettings.islandSize / 2F); x++) {
            for (int z = -(int) Math.floor((float) ConfigOptions.islandSettings.islandSize / 2F); z <= (int) Math
                    .floor((float) ConfigOptions.islandSettings.islandSize / 2F); z++) {
                BlockPos pos = new BlockPos(spawn.getX() + x, spawn.getY(), spawn.getZ() + z);
                world.setBlockState(pos.down(3), Blocks.BEDROCK.getDefaultState(), 2);
                world.setBlockState(pos.down(4), Blocks.BEDROCK.getDefaultState(), 2);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        EntityPlayer player = event.player;

        if (player.getEntityWorld().getWorldInfo().getTerrainType() instanceof WorldTypeVoid) {
            if (player.getBedLocation() == null
                    || player.getBedSpawnLocation(player.getEntityWorld(), player.getBedLocation(), true) == null) {

                IslandPos iPos = IslandManager.getPlayerIsland(player.getGameProfile().getId());

                BlockPos pos = new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0);
                if (iPos != null)
                    pos = new BlockPos(iPos.getX() * ConfigOptions.islandSettings.islandDistance,
                            ConfigOptions.islandSettings.islandYLevel,
                            iPos.getY() * ConfigOptions.islandSettings.islandDistance);

                IslandManager.tpPlayerToPos(player, pos);
            }
        }
    }

    @SubscribeEvent
    public void onSave(Save event) {
        VoidIslandControlSaveData.setDirty(0);
    }

    @SubscribeEvent
    public void onUnload(Unload event) {
        VoidIslandControlSaveData.setDirty(0);
    }

    @SubscribeEvent
    public static PlayerInteractEvent spawnProtection(PlayerInteractEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        World world = player.getEntityWorld();

        if (!ConfigOptions.islandSettings.spawnProtection || Math.abs(player.posX) > ConfigOptions.islandSettings.protectionBuildRange
                || Math.abs(player.posZ) > ConfigOptions.islandSettings.protectionBuildRange) {
            return event;
        } else {
            if (!player.isCreative() && event.isCancelable()) {
                event.setCanceled(true);
            }
            return null;
        }
    }
}
