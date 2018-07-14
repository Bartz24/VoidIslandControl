package com.bartz24.voidislandcontrol.api;

import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.google.common.base.Strings;
import net.minecraft.command.CommandGive;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IslandManager {
    public static ArrayList<IslandGen> IslandGenerations = new ArrayList<IslandGen>();

    public static ArrayList<IslandPos> CurrentIslandsList = new ArrayList<IslandPos>();

    public static ArrayList<String> spawnedPlayers = new ArrayList<String>();

    public static boolean worldOneChunk = false;
    public static boolean worldLoaded = false;
    public static int initialIslandDistance = ConfigOptions.islandSettings.islandDistance;

    public static void registerIsland(IslandGen gen) {
        IslandGenerations.add(gen);
    }

    public static List<String> getIslandGenTypes() {
        List<String> types = new ArrayList<String>();
        for (IslandGen g : IslandGenerations)
            types.add(g.Identifier);

        return types;
    }

    public static int getIndexOfIslandType(String type) {
        for (int i = 0; i < IslandGenerations.size(); i++)
            if (IslandGenerations.get(i).Identifier.equals(type))
                return i;
        return -1;
    }

    public static IslandPos getNextIsland() {
        int size = (int) Math.floor(Math.sqrt(CurrentIslandsList.size()));
        if (size % 2 == 0 && size > 0)
            size--;

        size = (size + 1) / 2;
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                if (!hasPosition(x, z)) {
                    return new IslandPos(x, z);
                }
            }
        }
        return null;
    }

    public static IslandPos getPlayerIsland(UUID playerUUID) {
        for (IslandPos pos : CurrentIslandsList) {
            if (pos.getPlayerUUIDs().contains(playerUUID.toString()))
                return pos;
        }
        return null;
    }

    public static IslandPos getIslandAtPos(int x, int y) {
        for (IslandPos pos : CurrentIslandsList) {
            if (pos.getX() == x && pos.getY() == y)
                return pos;
        }
        return null;
    }

    public static List<String> getPlayerNames(World world) {
        List<String> names = new ArrayList();
        for (IslandPos pos : CurrentIslandsList) {
            for (String s : pos.getPlayerUUIDs())

                names.add(world.getPlayerEntityByUUID(UUID.fromString(s)).getName());
        }
        return names;
    }

    public static boolean hasPosition(int x, int y) {
        for (IslandPos pos : CurrentIslandsList) {
            if (pos.getX() == x && pos.getY() == y)
                return true;
        }

        return false;
    }

    public static boolean playerHasIsland(UUID playerUUID) {
        for (IslandPos pos : CurrentIslandsList) {
            if (pos.getPlayerUUIDs().contains(playerUUID.toString()))
                return true;
        }

        return false;
    }

    public static void addPlayer(UUID playerUUID, IslandPos posAdd) {
        for (IslandPos pos : CurrentIslandsList) {
            if (pos.getX() == posAdd.getX() && pos.getY() == posAdd.getY()) {
                pos.addNewPlayer(playerUUID);
                return;
            }
        }
    }

    public static void removePlayer(UUID playerUUID) {
        IslandPos pos = getPlayerIsland(playerUUID);
        pos.removePlayer(playerUUID);
    }

    public static boolean hasPlayerSpawned(UUID playerUUID) {
        return spawnedPlayers.contains(playerUUID.toString());
    }

    public static void setStartingInv(EntityPlayerMP player) {
        if (ConfigOptions.islandSettings.resetInventory) {
            player.inventory.clear();

            try {
                for (int i = 0; i < Math.max(ConfigOptions.islandSettings.startingItems.length, 36); i++) {
                    String s = ConfigOptions.islandSettings.startingItems[i];
                    if (!Strings.isNullOrEmpty(s) && s.contains(":") && s.contains("*")) {
                        String trimmed = s.replaceAll(" ", "");
                        String itemName = trimmed.split(":")[0] + ":" + trimmed.split(":")[1];
                        int meta = Integer.parseInt(trimmed.split(":")[2].split("\\*")[0]);
                        int amt = Integer.parseInt(trimmed.split(":")[2].split("\\*")[1]);

                        Item item = CommandGive.getItemByText(player, itemName);

                        ItemStack stack = new ItemStack(item, amt, meta);

                        player.inventory.setInventorySlotContents(i, stack);
                    }
                }
            } catch (Exception e) {
                player.inventory.clear();
                player.sendMessage(new TextComponentString(
                        TextFormatting.RED + "Error getting starting inventory.\n" + e.toString()));
            }
        }
    }

    public static void tpPlayerToPos(EntityPlayer player, BlockPos pos) {
        if (!ConfigOptions.islandSettings.forceSpawnAtOffset) {
            if (!player.getEntityWorld().isAirBlock(pos) && !player.getEntityWorld().isAirBlock(pos.up())) {
                pos = player.getEntityWorld().getTopSolidOrLiquidBlock(pos.up(2));

                player.sendMessage(new TextComponentString("Failed to spawn. Sent to top block of platform spawn."));
            }
        } else
            pos = new BlockPos(ConfigOptions.islandSettings.forceSpawnOffset.x,
                    ConfigOptions.islandSettings.forceSpawnOffset.y, ConfigOptions.islandSettings.forceSpawnOffset.z)
                    .add(pos);

        player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 120, 20, false, false));


        if (player.dimension != ConfigOptions.worldGenSettings.baseDimension && player instanceof EntityPlayerMP)
            player.getServer().getPlayerList().transferPlayerToDimension((EntityPlayerMP) player, ConfigOptions.worldGenSettings.baseDimension,
                    new VICTeleporter(player.getServer().getWorld(ConfigOptions.worldGenSettings.baseDimension),
                            pos.getX() + 0.5f, pos.getY() + 2.6f, pos.getZ() + 0.5f));
        else
            player.setPositionAndUpdate(pos.getX() + 0.5, pos.getY() + 2.6, pos.getZ() + 0.5);
    }

    public static void tpPlayerToPosSpawn(EntityPlayer player, BlockPos pos) {
        tpPlayerToPos(player, pos);
        player.setSpawnPoint(pos, true);
    }

    public static void setVisitLoc(EntityPlayer player, int x, int y) {
        NBTTagCompound persist = setPlayerData(player);

        persist.setInteger("VICVisitX", x);
        persist.setInteger("VICVisitY", y);
    }

    public static void removeVisitLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);

        persist.removeTag("VICVisitX");
        persist.removeTag("VICVisitY");
    }

    public static boolean hasVisitLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);

        return persist.hasKey("VICVisitX") && persist.hasKey("VICVisitY");
    }

    public static IslandPos getVisitLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);

        return hasVisitLoc(player) ? new IslandPos(persist.getInteger("VICVisitX"), persist.getInteger("VICVisitY"))
                : null;
    }

    public static void setJoinLoc(EntityPlayer player, int x, int y) {
        NBTTagCompound persist = setPlayerData(player);

        persist.setInteger("VICJoinX", x);
        persist.setInteger("VICJoinY", y);
        persist.setInteger("VICJoinTime", 400);
    }

    public static void removeJoinLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);

        persist.removeTag("VICJoinX");
        persist.removeTag("VICJoinY");
        persist.removeTag("VICJoinTime");
    }

    public static boolean hasJoinLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);

        return persist.hasKey("VICJoinX") && persist.hasKey("VICJoinY");
    }

    public static IslandPos getJoinLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);

        return hasJoinLoc(player) ? new IslandPos(persist.getInteger("VICJoinX"), persist.getInteger("VICJoinY"))
                : null;
    }

    public static int getJoinTime(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);

        return hasJoinLoc(player) ? persist.getInteger("VICJoinTime") : -1;
    }

    public static void setJoinTime(EntityPlayer player, int val) {
        NBTTagCompound persist = setPlayerData(player);

        persist.setInteger("VICJoinTime", val);
    }

    public static NBTTagCompound setPlayerData(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
            data.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        return data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }
}
