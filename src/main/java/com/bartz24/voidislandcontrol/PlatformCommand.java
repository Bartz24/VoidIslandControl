package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.api.VICTeleporter;
import com.bartz24.voidislandcontrol.api.event.*;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.world.WorldTypeVoid;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlatformCommand extends CommandBase implements ICommand {
    private static List<String> aliases;

    public PlatformCommand() {
        aliases = new ArrayList<String>();
        if (ConfigOptions.commandSettings.commandName.equals("island")) {
            aliases.add("island");
        } else
            aliases.add(ConfigOptions.commandSettings.commandName);

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
            return getListOfStringsMatchingLastWord(args, "create", "invite", "join", "leave", "kick", "home", "spawn",
                    "reset", "visit", "onechunk");
        } else {
            String subCommand = args[0];
            subCommand = subCommand.trim();

            if (subCommand.equals("create")) {
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, IslandManager.getIslandGenTypes())
                        : Collections.<String>emptyList();
            } else if (subCommand.equals("invite")) {
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames())
                        : Collections.<String>emptyList();
            } else if (subCommand.equals("reset")) {
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, IslandManager.getIslandGenTypes())
                        : Collections.<String>emptyList();
            } else if (subCommand.equals("visit")) {
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames())
                        : Collections.<String>emptyList();
            } else if (subCommand.equals("kick")) {
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames())
                        : Collections.<String>emptyList();
            }
        }
        return Collections.<String>emptyList();

    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        World world = sender.getEntityWorld();
        EntityPlayerMP player = (EntityPlayerMP) world.getPlayerEntityByName(sender.getCommandSenderEntity().getName());

        if (!(world.getWorldInfo().getTerrainType() instanceof WorldTypeVoid)) {
            player.sendMessage(new TextComponentString("You are not in a void world type."));
            return;
        }

        if (args.length == 0)
            showHelp(player);
        else {
            String subCommand = args[0];
            subCommand = subCommand.trim();

            if (subCommand.equals("create")) {
                if (args.length > 1 && args[1].equals("bypass"))
                    args = new String[]{args[0]};
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
            } else if (subCommand.equals("kick")) {
                kick(player, args);
            } else if (subCommand.equals("onechunk")) {

                if (!ConfigOptions.commandSettings.oneChunkCommandAllowed) {
                    player.sendMessage(new TextComponentString("This command is not allowed!"));
                    return;

                }

                if (IslandManager.worldOneChunk) {
                    player.sendMessage(new TextComponentString("Already in one chunk mode!"));
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
            player.sendMessage(new TextComponentString("Must have 1 argument."));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentString("Can't use this command in this mode."));
            return;
        }
        if (IslandManager.initialIslandDistance != ConfigOptions.islandSettings.islandDistance) {
            player.sendMessage(new TextComponentString("This isn't going to work. The island distance has changed!"));
            return;
        }

        EntityPlayerMP player2 = (EntityPlayerMP) player.getEntityWorld().getPlayerEntityByName(args[1]);

        IslandPos isPos = IslandManager.getPlayerIsland(player2.getGameProfile().getId());

        if (args[1].equals(player.getName())) {
            player.sendMessage(new TextComponentString("Can't visit your own island."));
            return;
        }

        if (isPos == null) {
            player.sendMessage(new TextComponentString("Player doesn't exist or player doesn't have an island."));
            return;
        }

        BlockPos visitPos = new BlockPos(isPos.getX() * ConfigOptions.islandSettings.islandDistance,
                ConfigOptions.islandSettings.islandYLevel, isPos.getY() * ConfigOptions.islandSettings.islandDistance);

        IslandManager.setVisitLoc(player, isPos.getX(), isPos.getY());
        player.setGameType(GameType.SPECTATOR);

        player.connection.setPlayerLocation(visitPos.getX() + 0.5, visitPos.getY(), visitPos.getZ() + 0.5,
                player.rotationYaw, player.rotationPitch);

    }

    public static void kick(EntityPlayerMP player, String[] args) throws CommandException {
        if (args.length != 2) {
            player.sendMessage(new TextComponentString("Must have 1 argument."));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentString("Can't use this command in this mode."));
            return;
        }

        EntityPlayerMP player2 = (EntityPlayerMP) player.getEntityWorld().getPlayerEntityByName(args[1]);

        IslandPos isPos = IslandManager.getPlayerIsland(player2.getGameProfile().getId());

        if (args[1].equals(player.getName())) {
            player.sendMessage(new TextComponentString("Why are you kicking yourself."));
            return;
        }

        if (isPos == null) {
            player.sendMessage(new TextComponentString("Player doesn't exist or player doesn't have an island."));
            return;
        }

        if (!isPos.getPlayerUUIDs().contains(player2.getGameProfile().getId())) {
            player.sendMessage(new TextComponentString("Player isn't on your island."));
            return;
        }

        if (!isPos.getPlayerUUIDs().get(0).equals(player.getGameProfile().getId())) {
            player.sendMessage(new TextComponentString("You are not the owner of the island"));
            return;
        }

        for (int i = 0; i < player2.inventory.getSizeInventory(); i++) {
            ItemStack stack = player2.inventory.getStackInSlot(i).copy();
            EntityItem item = new EntityItem(player.world);
            item.setItem(stack);
            item.posX = player.posX;
            item.posY = player.posY;
            item.posZ = player.posZ;
            player.world.spawnEntity(item);
        }
        EventHandler.spawnPlayer(player2, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0), false);
        player2.sendMessage(new TextComponentString("You have been kicked..."));

    }

    public static void reset(EntityPlayerMP player, String[] args, World world) throws CommandException {
        if (!ConfigOptions.islandSettings.allowIslandCreation) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Not allowed to create islands!"));
            return;
        }
        if (!IslandManager.worldOneChunk) {
            leavePlatform(player, new String[]{""});
            newPlatform(player, args);
        } else {

            PlayerList players = world.getMinecraftServer().getPlayerList();
            for (EntityPlayerMP p : players.getPlayers()) {
                player.sendMessage(new TextComponentString("Lag incoming for reset!"));
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

                    EventHandler.spawnPlayer(player, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0), i);
                }
            } else {
                EventHandler.createSpawn(player, world, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0));
            }
            for (EntityPlayerMP p : players.getPlayers()) {
                p.inventory.clear();

                EventHandler.spawnPlayer(p, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0), false);
                player.sendMessage(new TextComponentString("Chunk Reset!"));
            }
        }
    }

    void showHelp(EntityPlayerMP player) {

        player.sendMessage(new TextComponentString(TextFormatting.RED + "create (optional int/string)<type>"
                + TextFormatting.WHITE + " : Spawn a new platform. Must not already be on an island."));

        player.sendMessage(new TextComponentString(TextFormatting.RED + "invite <player>" + TextFormatting.WHITE
                + " : Ask another player join your island. Player must do join to go to your island."));

        player.sendMessage(new TextComponentString(TextFormatting.RED + "leave" + TextFormatting.WHITE
                + " : Leave your island, clear inventory, and go to spawn.\n      (If you are the last person, no one can claim that island again.)"));

        player.sendMessage(new TextComponentString(TextFormatting.RED + "home" + TextFormatting.WHITE
                + " : Teleport back to your home island. Must be at least "
                + ConfigOptions.islandSettings.protectionBuildRange + " blocks away."));

        player.sendMessage(new TextComponentString(
                TextFormatting.RED + "spawn" + TextFormatting.WHITE + " : Teleport back to spawn (0, 0)."));

        player.sendMessage(new TextComponentString(TextFormatting.RED + "reset (optional int/string)<type>"
                + TextFormatting.WHITE
                + " : Creates a new platform in a new spot and clears the players' inventory.\n      (If it doesn't clear everything, be nice and toss the rest? Maybe?\nNot recommended unless all players for that island are online)"));

        player.sendMessage(new TextComponentString(TextFormatting.RED + "onechunk" + TextFormatting.WHITE
                + " : Play in one chunk, on one island. Also resets the spawn chunk."
                + (ConfigOptions.commandSettings.oneChunkCommandAllowed ? ""
                : TextFormatting.RED
                + "\n THE COMMAND IS NOT ALLOWED TO BE USED. SET THE CONFIG OPTION TO TRUE.")));

        player.sendMessage(new TextComponentString(TextFormatting.RED + "visit <player>" + TextFormatting.WHITE
                + " : Visit another player's island in spectator mode."));
    }

    public static void newPlatform(EntityPlayerMP player, String[] args) throws CommandException {
        if ((args.length == 1 || (args.length > 1 && !args[1].equals("bypass"))) && !ConfigOptions.islandSettings.allowIslandCreation) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Not allowed to create islands!"));
            return;
        }
        if (args.length > 2) {
            player.sendMessage(new TextComponentString("Must have 0 or 1 argument"));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentString("Can't use this command in this mode."));
            return;
        }
        if (IslandManager.initialIslandDistance != ConfigOptions.islandSettings.islandDistance) {
            player.sendMessage(new TextComponentString("This isn't going to work. The island distance has changed!"));
            return;
        }

        if (IslandManager.playerHasIsland(player.getGameProfile().getId())) {
            player.sendMessage(new TextComponentString("You already have an island!"));
            return;
        }

        IslandPos position = IslandManager.getNextIsland();
        if (args.length > 1 && args[1].equals("bypass"))
            args = new String[]{args[0]};

        if (args.length > 1 && ConfigOptions.islandSettings.islandSpawnType.equals("random")) {

            Integer i = -1;

            try {
                i = Integer.parseInt(args[1]);
            } catch (Exception e) {
                i = IslandManager.getIndexOfIslandType(args[1]);
            }

            if (i > -1 && i < IslandManager.IslandGenerations.size()) {

                EventHandler.spawnPlayer(player,
                        new BlockPos(position.getX() * ConfigOptions.islandSettings.islandDistance,
                                ConfigOptions.islandSettings.islandYLevel,
                                position.getY() * ConfigOptions.islandSettings.islandDistance),
                        i);
            }
        } else {
            if (args.length > 1) {
                player.sendMessage(new TextComponentString("You can't pick your island as the config overrides it!"));
            }
            EventHandler.spawnPlayer(player,
                    new BlockPos(position.getX() * ConfigOptions.islandSettings.islandDistance,
                            ConfigOptions.islandSettings.islandYLevel,
                            position.getY() * ConfigOptions.islandSettings.islandDistance),
                    true);
        }
        if (IslandManager.hasVisitLoc(player)) {
            player.setGameType(GameType.SURVIVAL);
            IslandManager.removeVisitLoc(player);
        }
    }

    public static void inviteOther(EntityPlayerMP player, String[] args, World world) throws CommandException {
        if (args.length != 2) {
            player.sendMessage(new TextComponentString("Must have 1 argument"));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentString("Can't use this command in this mode."));
            return;
        }
        if (IslandManager.initialIslandDistance != ConfigOptions.islandSettings.islandDistance) {
            player.sendMessage(new TextComponentString("This isn't going to work. The island distance has changed!"));
            return;
        }
        EntityPlayerMP player2 = (EntityPlayerMP) player.getEntityWorld().getPlayerEntityByName(args[1]);

        if (player2 == null) {
            player.sendMessage(new TextComponentString(args[1] + " doesn't exist."));
            return;
        }

        if (player2.getName().equals(player.getName())) {
            player.sendMessage(new TextComponentString(player2.getName() + " is yourself."));
            return;
        }

        if (!IslandManager.playerHasIsland(player.getGameProfile().getId())) {
            player.sendMessage(new TextComponentString("You don't have an island."));
            return;
        }

        if (IslandManager.hasJoinLoc(player2)) {
            player.sendMessage(new TextComponentString(player2.getName() + " has an invite already!"));
            return;
        }

        IslandPos position = IslandManager.getPlayerIsland(player.getGameProfile().getId());
        IslandManager.setJoinLoc(player2, position.getX(), position.getY());
        player.sendMessage(new TextComponentString("Invited " + player2.getName() + " to your island!"));
        player2.sendMessage(new TextComponentString("You have been invited to " + player.getName() + "'s island! Type /"
                + aliases.get(0) + " join to join!"));
    }

    public static void joinPlatform(EntityPlayerMP player, String[] args, World world) throws CommandException {
        IslandPos position = IslandManager.getJoinLoc(player);
        if (position == null) {
            player.sendMessage(new TextComponentString("You haven't been asked to join recently."));
            return;
        }

        IslandManager.addPlayer(player.getGameProfile().getId(), position);

        position = IslandManager.getPlayerIsland(player.getGameProfile().getId());

        for (String name : position.getPlayerUUIDs()) {
            EntityPlayerMP p = (EntityPlayerMP) world.getPlayerEntityByName(name);
            if (p != null)
                p.sendMessage(new TextComponentString(player.getName() + " joined your island!"));
        }

        if (IslandManager.hasVisitLoc(player)) {
            player.setGameType(GameType.SURVIVAL);
            IslandManager.removeVisitLoc(player);
        }

        IslandManager.tpPlayerToPosSpawn(player,
                new BlockPos(position.getX() * ConfigOptions.islandSettings.islandDistance,
                        ConfigOptions.islandSettings.islandYLevel,
                        position.getY() * ConfigOptions.islandSettings.islandDistance));

    }

    public static void leavePlatform(EntityPlayerMP player, String[] args) throws CommandException {
        if (!ConfigOptions.islandSettings.allowIslandCreation) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Not allowed to leave islands!"));
            return;
        }
        if (args.length > 1) {
            player.sendMessage(new TextComponentString("Must have no arguments"));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentString("Can't use this command in this mode."));
            return;
        }
        if (IslandManager.initialIslandDistance != ConfigOptions.islandSettings.islandDistance) {
            player.sendMessage(new TextComponentString("This isn't going to work. The island distance has changed!"));
            return;
        }

        if (!IslandManager.playerHasIsland(player.getGameProfile().getId())) {
            player.sendMessage(new TextComponentString("You don't have an island!"));
            return;
        }

        IslandManager.removePlayer(player.getGameProfile().getId());
        player.sendMessage(new TextComponentString("You are now free to join another island!"));

        player.inventory.clear();

        if (IslandManager.hasVisitLoc(player)) {
            player.setGameType(GameType.SURVIVAL);
            IslandManager.removeVisitLoc(player);
        }

        IslandManager.tpPlayerToPosSpawn(player, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0));
    }

    public static void tpHome(EntityPlayerMP player, String[] args) throws CommandException {
        if (args.length > 1) {
            player.sendMessage(new TextComponentString("Must have no arguments"));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentString("Can't use this command in this mode."));
            return;
        }
        if (IslandManager.initialIslandDistance != ConfigOptions.islandSettings.islandDistance) {
            player.sendMessage(new TextComponentString("This isn't going to work. The island distance has changed!"));
            return;
        }

        IslandPos isPos = IslandManager.getPlayerIsland(player.getGameProfile().getId());

        if (isPos == null) {
            player.sendMessage(new TextComponentString("You don't have an island yet."));
            return;
        }

        BlockPos home = new BlockPos(isPos.getX() * ConfigOptions.islandSettings.islandDistance,
                ConfigOptions.islandSettings.islandYLevel, isPos.getY() * ConfigOptions.islandSettings.islandDistance);

        if (Math.hypot(player.posX - home.getX() - 0.5,
                player.posZ - home.getZ() - 0.5) < ConfigOptions.islandSettings.protectionBuildRange) {
            player.sendMessage(new TextComponentString("You are too close to home!\nYou must be at least "
                    + (ConfigOptions.islandSettings.protectionBuildRange) + " blocks away!"));
            return;
        }

        if (IslandManager.hasVisitLoc(player)) {
            player.setGameType(GameType.SURVIVAL);
            IslandManager.removeVisitLoc(player);
        }

        IslandManager.tpPlayerToPos(player, home);

    }

    public static void tpSpawn(EntityPlayerMP player, String[] args) throws CommandException {
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentString("Can't use this command in this mode."));
            return;
        }

        if (IslandManager.hasVisitLoc(player)) {
            player.setGameType(GameType.SURVIVAL);
            IslandManager.removeVisitLoc(player);
        }

        IslandManager.tpPlayerToPos(player, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0));
    }

    @Override
    public String getName() {
        return aliases.get(0);
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return ConfigOptions.commandSettings.commandName;
    }
}
