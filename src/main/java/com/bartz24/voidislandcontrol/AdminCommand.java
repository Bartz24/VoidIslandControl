package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.world.WorldTypeVoid;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminCommand extends CommandBase implements ICommand {
    private static List<String> aliases;

    public AdminCommand() {
        aliases = new ArrayList<String>();
        aliases.add("islandAdmin");

    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "assign", "kick", "getIslandHere", "assignOwner");
        } else {
            String subCommand = args[0];
            subCommand = subCommand.trim();

            if (subCommand.equals("kick") || subCommand.equals("assign") || subCommand.equals("assignOwner")) {
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

            if (subCommand.equals("kick"))
                kick(player, args);
            else if (subCommand.equals("assign"))
                kick(player, args);
            else if (subCommand.equals("assignOwner"))
                kick(player, args);
            else if (subCommand.equals("getIslandHere"))
                kick(player, args);

        }

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

    public static void assignOwner(EntityPlayerMP player, String[] args) throws CommandException {
        if (args.length != 2) {
            player.sendMessage(new TextComponentString("Must have 1 argument."));
            return;
        }

        EntityPlayerMP player2 = (EntityPlayerMP) player.getEntityWorld().getPlayerEntityByName(args[1]);

        IslandPos isPos = IslandManager.getPlayerIsland(player2.getGameProfile().getId());

        isPos.getPlayerUUIDs().set(0, isPos.getPlayerUUIDs().remove(isPos.getPlayerUUIDs().indexOf(player2.getGameProfile().getId())));

        for (String name : isPos.getPlayerUUIDs()) {
            EntityPlayerMP p = (EntityPlayerMP) player.world.getPlayerEntityByName(name);
            if (p != null)
                p.sendMessage(new TextComponentString(player2.getName() + " is now the owner of the island!"));
        }
        player.sendMessage(new TextComponentString(player2.getName() + " is now the owner of the island!"));
    }

    void showHelp(EntityPlayerMP player) {


        player.sendMessage(new TextComponentString(TextFormatting.RED + "kick <player>" + TextFormatting.WHITE
                + " : Kick a player from their island"));

        player.sendMessage(new TextComponentString(TextFormatting.RED + "assign <player> islandX islandY" + TextFormatting.WHITE
                + " : Assign a player to an island. Use island coordinates (found using getClosestIslandPos), not real coordinates"));

        player.sendMessage(new TextComponentString(TextFormatting.RED + "assignOwner <player>" + TextFormatting.WHITE
                + " : Sets the player as the owner of their island"));

        player.sendMessage(new TextComponentString(TextFormatting.RED + "getIslandHere" + TextFormatting.WHITE
                + " : Shows the island coordinates of the island the player is at"));
    }

    public static void assign(EntityPlayerMP player, String[] args, World world) throws CommandException {

        EntityPlayerMP player2 = (EntityPlayerMP) player.getEntityWorld().getPlayerEntityByName(args[1]);

        if (args.length != 4) {
            player.sendMessage(new TextComponentString("Must have 3 arguments."));
            return;
        }

        IslandPos position = IslandManager.getIslandAtPos(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        if (position == null) {
            player.sendMessage(new TextComponentString("This island does not exist"));
            return;
        }

        IslandManager.addPlayer(player2.getGameProfile().getId(), position);

        for (String name : position.getPlayerUUIDs()) {
            EntityPlayerMP p = (EntityPlayerMP) world.getPlayerEntityByName(name);
            if (p != null)
                p.sendMessage(new TextComponentString(player2.getName() + " joined your island!"));
        }

        if (IslandManager.hasVisitLoc(player2)) {
            player2.setGameType(GameType.SURVIVAL);
            IslandManager.removeVisitLoc(player2);
        }

        IslandManager.tpPlayerToPosSpawn(player2,
                new BlockPos(position.getX() * ConfigOptions.islandSettings.islandDistance,
                        ConfigOptions.islandSettings.islandYLevel,
                        position.getY() * ConfigOptions.islandSettings.islandDistance));

    }


    public static void getIsland(EntityPlayerMP player, String[] args, World world) throws CommandException {

        if (args.length != 1) {
            player.sendMessage(new TextComponentString("Must have no arguments."));
            return;
        }

        IslandPos position = IslandManager.getIslandAtPos(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        if (position == null) {
            player.sendMessage(new TextComponentString("No island here"));
            return;
        }

        player.sendMessage(new TextComponentString("X: " + position.getX() + ", Y: " + position.getY()));
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
