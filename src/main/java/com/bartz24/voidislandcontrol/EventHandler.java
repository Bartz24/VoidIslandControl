package com.bartz24.voidislandcontrol;

import java.util.Random;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.world.WorldTypeVoid;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

public class EventHandler {
	@SubscribeEvent
	public void playerUpdate(LivingUpdateEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer && !event.getEntity().world.isRemote) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			NBTTagCompound data = player.getEntityData();
			if (!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
				data.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());

			NBTTagCompound persist = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

			if (player.world.getWorldInfo().getTerrainType() instanceof WorldTypeVoid && player.dimension == 0) {
				if (!IslandManager.hasPlayerSpawned(player.getGameProfile().getId())) {
					World world = player.world;
					if (world.getSpawnPoint().getX() != 0 && world.getSpawnPoint().getY() != 0)
						world.setSpawnPoint(new BlockPos(0, ConfigOptions.islandYSpawn, 0));
					BlockPos spawn = world.getSpawnPoint();

					if (!IslandManager.hasPosition(0, 0)) {
						IslandManager.CurrentIslandsList.add(new IslandPos(0, 0));

						if (ConfigOptions.oneChunk) {
							WorldBorder border = event.getEntityLiving().getEntityWorld().getMinecraftServer().worlds[0]
									.getWorldBorder();

							border.setCenter(0, 0);
							border.setTransition(16);
							border.setWarningDistance(1);

							IslandManager.worldOneChunk = true;
						}

						spawnPlayer(player, spawn, true);
					} else
						spawnPlayer(player, spawn, false);

					IslandManager.spawnedPlayers.add(player.getGameProfile().getId().toString());

				}
			}
		}
	}

	public static void spawnPlayer(EntityPlayer player, BlockPos pos, boolean spawnPlat) {
		if (spawnPlat)
			createSpawn(player.world, pos);

		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP pmp = (EntityPlayerMP) player;
			IslandManager.tpPlayerToPosSpawn(player, pos.up(4));
			pmp.setSpawnPoint(pos, true);

			IslandManager.setStartingInv(pmp);
		}
	}

	public static void spawnPlayer(EntityPlayer player, BlockPos pos, int forceType) {
		spawnPlayer(player, pos, false);

		spawnPlat(player.world, pos, forceType);
	}

	public static void createSpawn(World world, BlockPos spawn) {
		if (spawn.getX() == 0 && spawn.getZ() == 0 && !IslandManager.worldOneChunk) {
			mainSpawn(world, spawn);
			return;
		}

		Random random = world.rand;
		int type = ConfigOptions.worldSpawnType.equals("random")
				? random.nextInt(IslandManager.IslandGenerations.size())
				: IslandManager.getIndexOfIslandType(ConfigOptions.worldSpawnType);

		spawnPlat(world, spawn, type);
	}

	private static void spawnPlat(World world, BlockPos spawn, int type) {
		IslandManager.IslandGenerations.get(type).generate(world, spawn);
	}

	private static void mainSpawn(World world, BlockPos spawn) {
		for (int x = -(int) Math.floor((float) ConfigOptions.islandSize / 2F); x <= (int) Math
				.floor((float) ConfigOptions.islandSize / 2F); x++) {
			for (int z = -(int) Math.floor((float) ConfigOptions.islandSize / 2F); z <= (int) Math
					.floor((float) ConfigOptions.islandSize / 2F); z++) {
				BlockPos pos = new BlockPos(spawn.getX() + x, spawn.getY(), spawn.getZ() + z);
				world.setBlockState(pos.down(3), Blocks.BEDROCK.getDefaultState(), 2);
				world.setBlockState(pos.down(4), Blocks.BEDROCK.getDefaultState(), 2);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerJoinEvent(PlayerLoggedInEvent event) {
		EntityPlayer player = event.player;

		if (player.world.getWorldInfo().getTerrainType() instanceof WorldTypeVoid) {
			if (!IslandManager.playerHasIsland(player.getGameProfile().getId()) && !IslandManager.worldOneChunk)
				player.sendMessage(new TextComponentString(
						"Type " + TextFormatting.AQUA.toString() + "/" + ConfigOptions.commandName + " create"
								+ TextFormatting.WHITE.toString() + " to create your starting island"));
		}
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		EntityPlayer player = event.player;

		if (player.world.getWorldInfo().getTerrainType() instanceof WorldTypeVoid) {
			if (player.getBedLocation() == null
					|| player.getBedSpawnLocation(player.world, player.getBedLocation(), true) == null) {

				IslandPos iPos = IslandManager.getPlayerIsland(player.getGameProfile().getId());

				BlockPos pos = new BlockPos(0, ConfigOptions.islandYSpawn, 0);
				if (iPos != null)
					pos = new BlockPos(iPos.getX() * ConfigOptions.islandDistance, ConfigOptions.islandYSpawn,
							iPos.getY() * ConfigOptions.islandDistance);

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
	public void onTravelToDimensionEvent(EntityTravelToDimensionEvent event) {
		if (ConfigOptions.netherPortalLink) {
			Entity entity = event.getEntity();
			int dim = event.getDimension();

			if ((dim != 0 && dim != -1) || (entity.dimension != 0 && entity.dimension != -1)) {
				return;
			}
			PortalTeleporterNether tp = new PortalTeleporterNether();
			entity = tp.travelToDimension(entity, entity.dimension == 0 ? -1 : 0, entity.getPosition(), 16, false);
			if (entity.dimension != dim) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equalsIgnoreCase(References.ModID)) {
			ConfigOptions.reloadConfigs();
		}
	}
}
