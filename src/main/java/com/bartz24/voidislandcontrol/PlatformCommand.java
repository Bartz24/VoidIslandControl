package com.bartz24.voidislandcontrol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.api.event.IslandCreateEvent;
import com.bartz24.voidislandcontrol.api.event.IslandHomeEvent;
import com.bartz24.voidislandcontrol.api.event.IslandInviteEvent;
import com.bartz24.voidislandcontrol.api.event.IslandLeaveEvent;
import com.bartz24.voidislandcontrol.api.event.IslandResetEvent;
import com.bartz24.voidislandcontrol.api.event.IslandSpawnEvent;
import com.bartz24.voidislandcontrol.api.event.IslandVisitEvent;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.world.WorldTypeVoid;

import mcjty.lib.tools.ChatTools;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.common.MinecraftForge;

public class PlatformCommand extends CommandBase implements ICommand {
	private List<String> aliases;

	public PlatformCommand() {
		aliases = new ArrayList<String>();
		if (ConfigOptions.commandName.equals("island")) {
			aliases.add("island");
		} else
			aliases.add(ConfigOptions.commandName);

	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			@Nullable BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, "create", "invite", "join", "leave", "home", "spawn", "reset",
					"visit", "onechunk");
		} else {
			String subCommand = args[0];
			subCommand = subCommand.trim();

			if (subCommand.equals("create")) {
				return args.length == 2 ? getListOfStringsMatchingLastWord(args, IslandManager.getIslandGenTypes())
						: Collections.<String> emptyList();
			} else if (subCommand.equals("invite")) {
				return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames())
						: Collections.<String> emptyList();
			} else if (subCommand.equals("reset")) {
				return args.length == 2 ? getListOfStringsMatchingLastWord(args, IslandManager.getIslandGenTypes())
						: Collections.<String> emptyList();
			} else if (subCommand.equals("visit")) {
				return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames())
						: Collections.<String> emptyList();
			}
		}
		return Collections.<String> emptyList();

	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		World world = sender.getEntityWorld();
		EntityPlayerMP player = (EntityPlayerMP) world.getPlayerEntityByName(sender.getCommandSenderEntity().getName());

		if (!(world.getWorldInfo().getTerrainType() instanceof WorldTypeVoid)) {
			ChatTools.addChatMessage(player, new TextComponentString("You are not in a void world type."));
			return;
		}

		if (args.length == 0)
			showHelp(player);
		else {
			String subCommand = args[0];
			subCommand = subCommand.trim();

			if (subCommand.equals("create")) {
				if (args.length > 1 && args[1].equals("bypass"))
					args = new String[] { args[0] };
				newPlatform(player, args);
				MinecraftForge.EVENT_BUS.post(
						new IslandCreateEvent(player, IslandManager.getPlayerIsland(player.getGameProfile().getId())));
			} else if (subCommand.equals("invite")) {
				inviteOther(player, args, world);
				MinecraftForge.EVENT_BUS.post(
						new IslandInviteEvent(player, IslandManager.getPlayerIsland(player.getGameProfile().getId())));
			} else if (subCommand.equals("join")) {
				joinPlatform(player, args, world);
			} else if (subCommand.equals("leave")) {
				IslandPos pos = IslandManager.getPlayerIsland(player.getGameProfile().getId());
				leavePlatform(player, args);
				MinecraftForge.EVENT_BUS.post(new IslandLeaveEvent(player, pos));
			} else if (subCommand.equals("home")) {
				tpHome(player, args);
				MinecraftForge.EVENT_BUS.post(
						new IslandHomeEvent(player, IslandManager.getPlayerIsland(player.getGameProfile().getId())));
			} else if (subCommand.equals("spawn")) {
				tpSpawn(player, args);
				MinecraftForge.EVENT_BUS.post(new IslandSpawnEvent(player));
			} else if (subCommand.equals("reset")) {
				reset(player, args, world);
				MinecraftForge.EVENT_BUS.post(
						new IslandResetEvent(player, IslandManager.getPlayerIsland(player.getGameProfile().getId())));
			} else if (subCommand.equals("visit")) {
				visit(player, args);
				MinecraftForge.EVENT_BUS.post(
						new IslandVisitEvent(player, IslandManager.getPlayerIsland(player.getGameProfile().getId())));
			} else if (subCommand.equals("onechunk")) {

				if (!ConfigOptions.oneChunkCommandAllowed) {
					ChatTools.addChatMessage(player, new TextComponentString("This command is not allowed!"));
					return;

				}

				if (IslandManager.worldOneChunk) {
					ChatTools.addChatMessage(player, new TextComponentString("Already in one chunk mode!"));
					return;
				}
				IslandManager.CurrentIslandsList.clear();

				IslandManager.CurrentIslandsList.add(new IslandPos(0, 0));
				WorldBorder border = world.getMinecraftServer().worlds[0].getWorldBorder();

				border.setCenter(0, 0);
				border.setTransition(16);
				border.setWarningDistance(1);

				IslandManager.worldOneChunk = true;
				reset(player, args, world);
			}
		}

	}

	public static void visit(EntityPlayerMP player, String[] args) throws CommandException {
		if (args.length != 2) {
			ChatTools.addChatMessage(player, new TextComponentString("Must have 1 argument."));
			return;
		}
		if (IslandManager.worldOneChunk) {
			ChatTools.addChatMessage(player, new TextComponentString("Can't use this command in this mode."));
			return;
		}
		if (IslandManager.initialIslandDistance != ConfigOptions.islandDistance) {
			ChatTools.addChatMessage(player,
					new TextComponentString("This isn't going to work. The island distance has changed!"));
			return;
		}

		EntityPlayerMP player2 = (EntityPlayerMP) player.getEntityWorld().getPlayerEntityByName(args[1]);

		IslandPos isPos = IslandManager.getPlayerIsland(player2.getGameProfile().getId());

		if (args[1].equals(player.getName())) {
			ChatTools.addChatMessage(player, new TextComponentString("Can't visit your own island."));
			return;
		}

		if (isPos == null) {
			ChatTools.addChatMessage(player,
					new TextComponentString("Player doesn't exist or player doesn't have an island."));
			return;
		}

		BlockPos visitPos = new BlockPos(isPos.getX() * ConfigOptions.islandDistance, ConfigOptions.islandYSpawn,
				isPos.getY() * ConfigOptions.islandDistance);

		IslandManager.setVisitLoc(player, isPos.getX(), isPos.getY());
		player.setGameType(GameType.SPECTATOR);

		if (player.connection.playerEntity.dimension != 0)
			player.connection.playerEntity.changeDimension(0);
		player.connection.setPlayerLocation(visitPos.getX() + 0.5, visitPos.getY(), visitPos.getZ() + 0.5,
				player.rotationYaw, player.rotationPitch);

	}

	public static void reset(EntityPlayerMP player, String[] args, World world) throws CommandException {
		if (!ConfigOptions.allowIslandCreation) {
			ChatTools.addChatMessage(player,
					new TextComponentString(TextFormatting.RED + "Not allowed to create islands!"));
			return;
		}
		if (!IslandManager.worldOneChunk) {
			leavePlatform(player, new String[] { "" });
			newPlatform(player, args);
		} else {

			PlayerList players = world.getMinecraftServer().getPlayerList();
			for (EntityPlayerMP p : players.getPlayers()) {
				ChatTools.addChatMessage(player, new TextComponentString("Lag incoming for reset!"));
			}
			for (int x = -8; x < 9; x++) {
				for (int z = -8; z < 9; z++) {
					for (int y = 0; y < 256; y++) {
						world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 2);
					}
				}
			}
			if (args.length > 1) {
				Integer i = -1;

				try {
					i = Integer.parseInt(args[1]);
				} catch (Exception e) {
					i = IslandManager.getIndexOfIslandType(args[1]);
				}

				if (i > -1 && i < IslandManager.IslandGenerations.size()) {

					if (player.connection.playerEntity.dimension != 0)
						player.connection.playerEntity.changeDimension(0);
					EventHandler.spawnPlayer(player, new BlockPos(0, ConfigOptions.islandYSpawn, 0), i);
				}
			} else {
				EventHandler.createSpawn(player, world, new BlockPos(0, ConfigOptions.islandYSpawn, 0));
			}
			for (EntityPlayerMP p : players.getPlayers()) {
				p.inventory.clear();

				if (player.connection.playerEntity.dimension != 0)
					player.connection.playerEntity.changeDimension(0);
				EventHandler.spawnPlayer(p, new BlockPos(0, ConfigOptions.islandYSpawn, 0), false);
				ChatTools.addChatMessage(p, new TextComponentString("Chunk Reset!"));
			}
		}
	}

	void showHelp(EntityPlayerMP player) {

		ChatTools.addChatMessage(player,
				new TextComponentString(TextFormatting.RED + "create (optional int/string)<type>" + TextFormatting.WHITE
						+ " : Spawn a new platform. Must not already be on an island."));

		ChatTools.addChatMessage(player,
				new TextComponentString(TextFormatting.RED + "invite <player>" + TextFormatting.WHITE
						+ " : Ask another player join your island. Player must do join to go to your island."));

		ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "leave" + TextFormatting.WHITE
				+ " : Leave your island, clear inventory, and go to spawn.\n      (If you are the last person, no one can claim that island again.)"));

		ChatTools.addChatMessage(player,
				new TextComponentString(TextFormatting.RED + "home" + TextFormatting.WHITE
						+ " : Teleport back to your home island. Must be at least " + ConfigOptions.islandDistance / 2
						+ " blocks away."));

		ChatTools.addChatMessage(player, new TextComponentString(
				TextFormatting.RED + "spawn" + TextFormatting.WHITE + " : Teleport back to spawn (0, 0)."));

		ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED
				+ "reset (optional int/string)<type>" + TextFormatting.WHITE
				+ " : Creates a new platform in a new spot and clears the players' inventory.\n      (If it doesn't clear everything, be nice and toss the rest? Maybe?\nNot recommended unless all players for that island are online)"));

		ChatTools.addChatMessage(player,
				new TextComponentString(TextFormatting.RED + "onechunk" + TextFormatting.WHITE
						+ " : Play in one chunk, on one island. Also resets the spawn chunk."
						+ (ConfigOptions.oneChunkCommandAllowed ? ""
								: TextFormatting.RED
										+ "\n THE COMMAND IS NOT ALLOWED TO BE USED. SET THE CONFIG OPTION TO TRUE.")));

		ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "visit <player>"
				+ TextFormatting.WHITE + " : Visit another player's island in spectator mode."));
	}

	public static void newPlatform(EntityPlayerMP player, String[] args) throws CommandException {
		if ((args.length > 1 && !args[1].equals("bypass")) && !ConfigOptions.allowIslandCreation) {
			ChatTools.addChatMessage(player,
					new TextComponentString(TextFormatting.RED + "Not allowed to create islands!"));
			return;
		}
		if (args.length > 2) {
			ChatTools.addChatMessage(player, new TextComponentString("Must have 0 or 1 argument"));	
			return;
		}
		if (IslandManager.worldOneChunk) {
			ChatTools.addChatMessage(player, new TextComponentString("Can't use this command in this mode."));
			return;
		}
		if (IslandManager.initialIslandDistance != ConfigOptions.islandDistance) {
			ChatTools.addChatMessage(player,
					new TextComponentString("This isn't going to work. The island distance has changed!"));
			return;
		}

		if (IslandManager.playerHasIsland(player.getGameProfile().getId())) {
			ChatTools.addChatMessage(player, new TextComponentString("You already have an island!"));
			return;
		}

		IslandPos position = IslandManager.getNextIsland();
		if (args.length > 1 && args[1].equals("bypass"))
			args = new String[] { args[0] };

		if (args.length > 1 && ConfigOptions.worldSpawnType.equals("random")) {

			Integer i = -1;

			try {
				i = Integer.parseInt(args[1]);
			} catch (Exception e) {
				i = IslandManager.getIndexOfIslandType(args[1]);
			}

			if (i > -1 && i < IslandManager.IslandGenerations.size()) {

				if (player.connection.playerEntity.dimension != 0)
					player.connection.playerEntity.changeDimension(0);

				EventHandler.spawnPlayer(player, new BlockPos(position.getX() * ConfigOptions.islandDistance,
						ConfigOptions.islandYSpawn, position.getY() * ConfigOptions.islandDistance), i);
			}
		} else {
			if (args.length > 1) {
				ChatTools.addChatMessage(player,
						new TextComponentString("You can't pick your island as the config overrides it!"));
			}

			if (player.connection.playerEntity.dimension != 0)
				player.connection.playerEntity.changeDimension(0);
			EventHandler.spawnPlayer(player, new BlockPos(position.getX() * ConfigOptions.islandDistance,
					ConfigOptions.islandYSpawn, position.getY() * ConfigOptions.islandDistance), true);
		}
		if (IslandManager.hasVisitLoc(player)) {
			player.setGameType(GameType.SURVIVAL);
			IslandManager.removeVisitLoc(player);
		}
	}

	public static void inviteOther(EntityPlayerMP player, String[] args, World world) throws CommandException {
		if (args.length != 2) {
			ChatTools.addChatMessage(player, new TextComponentString("Must have 1 argument"));
			return;
		}
		if (IslandManager.worldOneChunk) {
			ChatTools.addChatMessage(player, new TextComponentString("Can't use this command in this mode."));
			return;
		}
		if (IslandManager.initialIslandDistance != ConfigOptions.islandDistance) {
			ChatTools.addChatMessage(player,
					new TextComponentString("This isn't going to work. The island distance has changed!"));
			return;
		}
		EntityPlayerMP player2 = (EntityPlayerMP) player.getEntityWorld().getPlayerEntityByName(args[1]);

		if (player2 == null) {
			ChatTools.addChatMessage(player, new TextComponentString(args[1] + " doesn't exist."));
			return;
		}

		if (player2.getName().equals(player.getName())) {
			ChatTools.addChatMessage(player, new TextComponentString(player2.getName() + " is yourself."));
			return;
		}

		if (!IslandManager.playerHasIsland(player.getGameProfile().getId())) {
			ChatTools.addChatMessage(player, new TextComponentString("You don't have an island."));
			return;
		}

		if (IslandManager.hasJoinLoc(player2)) {
			ChatTools.addChatMessage(player, new TextComponentString(player2.getName() + " has an invite already!"));
			return;
		}

		IslandPos position = IslandManager.getPlayerIsland(player.getGameProfile().getId());
		IslandManager.setJoinLoc(player2, position.getX(), position.getY());
		ChatTools.addChatMessage(player, new TextComponentString("Invited " + player2.getName() + " to your island!"));
		ChatTools.addChatMessage(player2,
				new TextComponentString("You have been invited to " + player.getName() + "'s island!"));
	}

	public static void joinPlatform(EntityPlayerMP player, String[] args, World world) throws CommandException {
		IslandPos position = IslandManager.getJoinLoc(player);
		if (position == null) {
			ChatTools.addChatMessage(player, new TextComponentString("You haven't been asked to join recently."));
			return;
		}

		IslandManager.addPlayer(player.getGameProfile().getId(), position);

		position = IslandManager.getPlayerIsland(player.getGameProfile().getId());

		for (String name : position.getPlayerUUIDs()) {
			EntityPlayerMP p = (EntityPlayerMP) world.getPlayerEntityByName(name);
			if (p != null)
				ChatTools.addChatMessage(p, new TextComponentString(player.getName() + " joined your island!"));
		}

		if (IslandManager.hasVisitLoc(player)) {
			player.setGameType(GameType.SURVIVAL);
			IslandManager.removeVisitLoc(player);
		}

		if (player.connection.playerEntity.dimension != 0)
			player.connection.playerEntity.changeDimension(0);
		IslandManager.tpPlayerToPosSpawn(player, new BlockPos(position.getX() * ConfigOptions.islandDistance,
				ConfigOptions.islandYSpawn, position.getY() * ConfigOptions.islandDistance));

	}

	public static void leavePlatform(EntityPlayerMP player, String[] args) throws CommandException {
		if (!ConfigOptions.allowIslandCreation) {
			ChatTools.addChatMessage(player,
					new TextComponentString(TextFormatting.RED + "Not allowed to create islands!"));
			return;
		}
		if (args.length > 1) {
			ChatTools.addChatMessage(player, new TextComponentString("Must have no arguments"));
			return;
		}
		if (IslandManager.worldOneChunk) {
			ChatTools.addChatMessage(player, new TextComponentString("Can't use this command in this mode."));
			return;
		}
		if (IslandManager.initialIslandDistance != ConfigOptions.islandDistance) {
			ChatTools.addChatMessage(player,
					new TextComponentString("This isn't going to work. The island distance has changed!"));
			return;
		}

		if (!IslandManager.playerHasIsland(player.getGameProfile().getId())) {
			ChatTools.addChatMessage(player, new TextComponentString("You don't have an island!"));
			return;
		}

		IslandManager.removePlayer(player.getGameProfile().getId());
		ChatTools.addChatMessage(player, new TextComponentString("You are now free to join another island!"));

		player.inventory.clear();

		if (IslandManager.hasVisitLoc(player)) {
			player.setGameType(GameType.SURVIVAL);
			IslandManager.removeVisitLoc(player);
		}

		if (player.connection.playerEntity.dimension != 0)
			player.connection.playerEntity.changeDimension(0);
		IslandManager.tpPlayerToPosSpawn(player, new BlockPos(0, ConfigOptions.islandYSpawn, 0));
	}

	public static void tpHome(EntityPlayerMP player, String[] args) throws CommandException {
		if (args.length > 1) {
			ChatTools.addChatMessage(player, new TextComponentString("Must have no arguments"));
			return;
		}
		if (IslandManager.worldOneChunk) {
			ChatTools.addChatMessage(player, new TextComponentString("Can't use this command in this mode."));
			return;
		}
		if (IslandManager.initialIslandDistance != ConfigOptions.islandDistance) {
			ChatTools.addChatMessage(player,
					new TextComponentString("This isn't going to work. The island distance has changed!"));
			return;
		}

		IslandPos isPos = IslandManager.getPlayerIsland(player.getGameProfile().getId());

		if (isPos == null) {
			ChatTools.addChatMessage(player, new TextComponentString("You don't have an island yet."));
			return;
		}

		BlockPos home = new BlockPos(isPos.getX() * ConfigOptions.islandDistance, ConfigOptions.islandYSpawn,
				isPos.getY() * ConfigOptions.islandDistance);

		if (Math.hypot(player.posX - home.getX() - 0.5, player.posZ - home.getZ() - 0.5) < ConfigOptions.islandDistance
				/ 2) {
			ChatTools.addChatMessage(player, new TextComponentString("You are too close to home!\nYou must be at least "
					+ (ConfigOptions.islandDistance / 2) + " blocks away!"));
			return;
		}

		if (IslandManager.hasVisitLoc(player)) {
			player.setGameType(GameType.SURVIVAL);
			IslandManager.removeVisitLoc(player);
		}

		if (player.connection.playerEntity.dimension != 0)
			player.connection.playerEntity.changeDimension(0);
		IslandManager.tpPlayerToPos(player, home);

	}

	public static void tpSpawn(EntityPlayerMP player, String[] args) throws CommandException {
		if (IslandManager.worldOneChunk) {
			ChatTools.addChatMessage(player, new TextComponentString("Can't use this command in this mode."));
			return;
		}

		if (IslandManager.hasVisitLoc(player)) {
			player.setGameType(GameType.SURVIVAL);
			IslandManager.removeVisitLoc(player);
		}

		if (player.connection.playerEntity.dimension != 0)
			player.connection.playerEntity.changeDimension(0);
		IslandManager.tpPlayerToPos(player, new BlockPos(0, ConfigOptions.islandYSpawn, 0));
	}

	@Override
	public String getName() {
		return aliases.get(0);
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return ConfigOptions.commandName;
	}
}
