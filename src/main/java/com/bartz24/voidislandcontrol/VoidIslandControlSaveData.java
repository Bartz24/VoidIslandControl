package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.config.ConfigOptions;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class VoidIslandControlSaveData extends WorldSavedData
{
	private static VoidIslandControlSaveData INSTANCE;
	public static final String dataName = "VICData";

	public VoidIslandControlSaveData(String s)
	{
		super(s);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		IslandManager.CurrentIslandsList.clear();
		IslandManager.spawnedPlayers.clear();
		IslandManager.worldOneChunk = false;
		IslandManager.initialIslandDistance = ConfigOptions.islandSettings.islandDistance;
		NBTTagList list = nbt.getTagList("Positions",
				Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound stackTag = list.getCompoundTagAt(i);

			IslandPos pos = new IslandPos(0, 0);
			pos.readFromNBT(stackTag);
			IslandManager.CurrentIslandsList.add(pos);
		}

		list = nbt.getTagList("SpawnedPlayers", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound stackTag = list.getCompoundTagAt(i);

			String name = stackTag.getString("name");

			IslandManager.spawnedPlayers.add(name);
		}
		if (nbt.hasKey("oneChunkWorld"))
			IslandManager.worldOneChunk = nbt.getBoolean("oneChunkWorld");
		if (nbt.hasKey("initialDist"))
			IslandManager.initialIslandDistance = nbt.getInteger("initialDist");
		if (nbt.hasKey("worldLoaded"))
			IslandManager.worldLoaded = nbt.getBoolean("worldLoaded");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < IslandManager.CurrentIslandsList.size(); i++)
		{
			NBTTagCompound stackTag = new NBTTagCompound();

			IslandManager.CurrentIslandsList.get(i).writeToNBT(stackTag);

			list.appendTag(stackTag);
		}
		nbt.setTag("Positions", list);
		NBTTagList list2 = new NBTTagList();
		for (int i = 0; i < IslandManager.spawnedPlayers.size(); i++)
		{
			NBTTagCompound stackTag = new NBTTagCompound();

			stackTag.setString("name", IslandManager.spawnedPlayers.get(i));

			list2.appendTag(stackTag);
		}
		nbt.setTag("SpawnedPlayers", list2);

		if (IslandManager.worldOneChunk)
			nbt.setBoolean("oneChunkWorld", true);

		nbt.setInteger("initialDist", IslandManager.initialIslandDistance);
		nbt.setBoolean("worldLoaded", IslandManager.worldLoaded);

		return nbt;
	}

	public static void setDirty(int dimension)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER
				&& INSTANCE != null)
			INSTANCE.markDirty();
	}

	public static void setInstance(int dimension, VoidIslandControlSaveData in)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			INSTANCE = in;
	}
}
