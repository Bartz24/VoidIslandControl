package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.common.world.SkyblockWorldEvents;

public class GoGSupport {
	public static void spawnGoGIsland(World world, BlockPos pos) {
		SkyblockWorldEvents.createSkyblock(world, pos.down(2));
	}

	@SubscribeEvent
	public void onPlayerInteract(RightClickBlock event) {

		if (event.getWorld().getWorldType() instanceof WorldTypeVoid) {
			EntityPlayer player = event.getEntityPlayer();
			IslandPos pos = IslandManager.getPlayerIsland(player.getGameProfile().getId());
			if (pos != null) {
				int posX = pos.getX() * ConfigOptions.islandSettings.islandDistance;
				int posY = pos.getY() * ConfigOptions.islandSettings.islandDistance;
				if (pos.getType().equals("gog") && Math.abs(player.posX - posX) < ConfigOptions.islandSettings.protectionBuildRange
						&& Math.abs(player.posZ - posY) < ConfigOptions.islandSettings.protectionBuildRange) {
					ItemStack equipped = event.getItemStack();
					if (equipped.isEmpty() && event.getEntityPlayer().isSneaking()) {
						Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
						if (ImmutableSet
								.of(Blocks.GRASS, Blocks.GRASS_PATH, Blocks.FARMLAND, Blocks.DIRT, ModBlocks.altGrass)
								.contains(block)) {
							if (event.getWorld().isRemote)
								event.getEntityPlayer().swingArm(event.getHand());
							else {
								event.getWorld().playSound(null, event.getPos(), block.getSoundType().getBreakSound(),
										SoundCategory.BLOCKS, block.getSoundType().getVolume() * 0.4F,
										block.getSoundType().getPitch() + (float) (Math.random() * 0.2 - 0.1));

								if (Math.random() < 0.8)
									event.getEntityPlayer().dropItem(new ItemStack(ModItems.manaResource, 1, 21),
											false);
							}

							event.setCanceled(true);
							event.setCancellationResult(EnumActionResult.SUCCESS);
						}
					} else if (!equipped.isEmpty() && equipped.getItem() == Items.BOWL) {
						RayTraceResult rtr = ToolCommons.raytraceFromEntity(event.getWorld(), event.getEntityPlayer(),
								true, 4.5F);
						if (rtr != null) {
							if (rtr.typeOfHit == net.minecraft.util.math.RayTraceResult.Type.BLOCK) {
								if (event.getWorld().getBlockState(rtr.getBlockPos()).getMaterial() == Material.WATER) {
									if (!event.getWorld().isRemote) {
										equipped.shrink(1);

										if (equipped.isEmpty())
											event.getEntityPlayer().setHeldItem(event.getHand(),
													new ItemStack(ModItems.waterBowl));
										else
											ItemHandlerHelper.giveItemToPlayer(event.getEntityPlayer(),
													new ItemStack(ModItems.waterBowl));
									}

									event.setCanceled(true);
									event.setCancellationResult(EnumActionResult.SUCCESS);
								}
							}
						}
					}
				}
			}
		}

	}
}
