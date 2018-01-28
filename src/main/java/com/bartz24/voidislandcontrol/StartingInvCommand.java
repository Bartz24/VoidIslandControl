package com.bartz24.voidislandcontrol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.bartz24.voidislandcontrol.config.ConfigOptions;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

public class StartingInvCommand extends CommandBase implements ICommand {
	private List<String> aliases;

	public StartingInvCommand() {
		aliases = new ArrayList<String>();
		aliases.add("startingInv");
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
		return Collections.<String> emptyList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		World world = sender.getEntityWorld();
		EntityPlayerMP player = (EntityPlayerMP) world.getPlayerEntityByName(sender.getCommandSenderEntity().getName());

		ConfigOptions.islandSettings.startingItems = ConfigOptions.emptyFilledArray(36);
		for (int i = 0; i < 36; i++) {
			String item = "";
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				int amt = stack.getCount();
				int meta = stack.getMetadata();
				String name = Item.REGISTRY.getNameForObject(stack.getItem()).toString();

				ConfigOptions.islandSettings.startingItems[i] = name + ":" + meta + "*" + amt;
			} else {
				ConfigOptions.islandSettings.startingItems[i] = "";
			}
			ConfigManager.sync(References.ModID, Config.Type.INSTANCE);
		}
		player.sendMessage(new TextComponentString("Starting Inventory config set!"));
	}

	@Override
	public String getName() {
		return aliases.get(0);
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "";
	}
}
