package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.api.IslandGen;
import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.world.GoGSupport;

import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;

public class IslandRegistry {
	public static void initIslands() {

		if (ConfigOptions.enableGrassIsland) {
			IslandManager.registerIsland(new IslandGen("grass") {
				public void generate(World world, BlockPos spawn) {

					for (int x = -(int) Math.floor((float) ConfigOptions.islandSize / 2F); x <= (int) Math
							.floor((float) ConfigOptions.islandSize / 2F); x++) {
						for (int z = -(int) Math.floor((float) ConfigOptions.islandSize / 2F); z <= (int) Math
								.floor((float) ConfigOptions.islandSize / 2F); z++) {
							BlockPos pos = new BlockPos(spawn.getX() + x, spawn.getY(), spawn.getZ() + z);
							world.setBlockState(pos.down(3), ConfigOptions.replaceDirt ? Blocks.DIRT.getDefaultState()
									: Blocks.GRASS.getDefaultState(), 2);
							if (ConfigOptions.spawnBedrock)
								world.setBlockState(pos.down(4), Blocks.BEDROCK.getDefaultState(), 2);
							else if (ConfigOptions.replaceBedrock)
								world.setBlockState(pos.down(4), Blocks.DIRT.getDefaultState(), 2);
						}
					}

					if (ConfigOptions.spawnTree) {
						for (int y = 0; y < 5; y++) {
							for (int x = -2; x < 3; x++) {
								for (int z = -2; z < 3; z++) {
									BlockPos pos = new BlockPos(spawn.getX() + x, spawn.getY() - 2 + y,
											spawn.getZ() + z);
									if (x == 0 && z == 0) {
										if (y < 3)
											world.setBlockState(pos, Blocks.LOG.getDefaultState(), 2);
										else
											world.setBlockState(pos, Blocks.LEAVES.getDefaultState(), 2);
									} else if (y == 2 || y == 3) {
										world.setBlockState(pos, Blocks.LEAVES.getDefaultState(), 2);
									} else if (y == 4 && x >= -1 && x <= 1 && z >= -1 && z <= 1) {
										world.setBlockState(pos, Blocks.LEAVES.getDefaultState(), 2);
									}
								}
							}
						}
					}
					if (ConfigOptions.spawnChest) {
						BlockPos pos = new BlockPos(spawn.getX(), spawn.getY() - 2, spawn.getZ() - 1);
						world.setBlockState(pos, Blocks.CHEST.getDefaultState());
					}
					changeBiome(spawn.getX(), spawn.getZ(), world);
				}
			});
		}
		if (ConfigOptions.enableSandIsland) {
			IslandManager.registerIsland(new IslandGen("sand") {
				public void generate(World world, BlockPos spawn) {

					for (int x = -(int) Math.floor((float) ConfigOptions.islandSize / 2F); x <= (int) Math
							.floor((float) ConfigOptions.islandSize / 2F); x++) {
						for (int z = -(int) Math.floor((float) ConfigOptions.islandSize / 2F); z <= (int) Math
								.floor((float) ConfigOptions.islandSize / 2F); z++) {
							BlockPos pos = new BlockPos(spawn.getX() + x, spawn.getY(), spawn.getZ() + z);
							world.setBlockState(pos.down(3),
									Blocks.SAND.getStateFromMeta(ConfigOptions.redSand ? 1 : 0), 2);
							if (ConfigOptions.spawnBedrock)
								world.setBlockState(pos.down(4), Blocks.BEDROCK.getDefaultState(), 2);
							else if (ConfigOptions.replaceBedrock)
								world.setBlockState(pos.down(4), Blocks.SANDSTONE.getDefaultState(), 2);
						}
					}
					BlockPos pos = new BlockPos(spawn.getX() - 1, spawn.getY(), spawn.getZ() + 1);
					if (ConfigOptions.spawnCactus) {
						world.setBlockState(pos.down(2), Blocks.CACTUS.getDefaultState(), 2);
						world.setBlockState(pos.down(1), Blocks.CACTUS.getDefaultState(), 2);
						world.setBlockState(pos, Blocks.CACTUS.getDefaultState(), 2);
					}
					if (ConfigOptions.spawnChest) {
						pos = new BlockPos(spawn.getX(), spawn.getY() - 2, spawn.getZ() - 1);
						world.setBlockState(pos, Blocks.CHEST.getDefaultState());
					}
					changeBiome(spawn.getX(), spawn.getZ(), world);
				}
			});
		}

		if (ConfigOptions.enableSnowIsland) {
			IslandManager.registerIsland(new IslandGen("snow") {
				public void generate(World world, BlockPos spawn) {
					for (int x = -(int) Math.floor((float) ConfigOptions.islandSize / 2F)
							- 1; x <= (int) Math.floor((float) ConfigOptions.islandSize / 2F) + 1; x++) {
						for (int z = -(int) Math.floor((float) ConfigOptions.islandSize / 2F)
								- 1; z <= (int) Math.floor((float) ConfigOptions.islandSize / 2F) + 1; z++) {
							BlockPos pos = new BlockPos(spawn.getX() + x, spawn.getY(), spawn.getZ() + z);
							if (world.getBiome(pos).getFloatTemperature(new BlockPos(pos)) > 1.0F)
								world.getChunkFromBlockCoords(pos).getBiomeArray()[(pos.getZ() & 15) << 4
										| (pos.getX() & 15)] = (byte) Biome.getIdForBiome(Biomes.PLAINS);

							if (x == -(int) Math.floor((float) ConfigOptions.islandSize / 2F) - 1
									|| x == (int) Math.floor((float) ConfigOptions.islandSize / 2F) + 1
									|| z == -(int) Math.floor((float) ConfigOptions.islandSize / 2F) - 1
									|| z == (int) Math.floor((float) ConfigOptions.islandSize / 2F) + 1) {
								if (ConfigOptions.spawnIgloo) {
									world.setBlockState(pos.down(3), Blocks.PACKED_ICE.getDefaultState(), 2);

									world.setBlockState(pos.down(2), Blocks.PACKED_ICE.getDefaultState(), 2);

									world.setBlockState(pos.down(1), Blocks.PACKED_ICE.getDefaultState(), 2);
								}
							} else {
								if (!(x == 0 && z == 0) && ConfigOptions.spawnIgloo)
									world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState(), 2);
								world.setBlockState(pos.down(3), Blocks.SNOW.getDefaultState(), 2);
								if (ConfigOptions.spawnBedrock)
									world.setBlockState(pos.down(4), Blocks.BEDROCK.getDefaultState(), 2);
								else if (ConfigOptions.replaceBedrock)
									world.setBlockState(pos.down(4), Blocks.PACKED_ICE.getDefaultState(), 2);
								if (((x == -1 && z == 1) || (x == 1 && z == 1)) && ConfigOptions.spawnPumpkins)
									world.setBlockState(pos.down(2), Blocks.PUMPKIN.getDefaultState(), 2);
								else
									world.setBlockState(pos.down(2), Blocks.SNOW_LAYER.getDefaultState(), 2);
							}
						}
					}
					if (ConfigOptions.spawnChest) {
						BlockPos pos = new BlockPos(spawn.getX(), spawn.getY() - 2, spawn.getZ() - 1);
						world.setBlockState(pos, Blocks.CHEST.getDefaultState());
					}
					changeBiome(spawn.getX(), spawn.getZ(), world);
				}
			});
		}

		if (ConfigOptions.enableWoodIsland) {
			IslandManager.registerIsland(new IslandGen("wood") {
				public void generate(World world, BlockPos spawn) {
					for (int x = -(int) Math.floor((float) ConfigOptions.islandSize / 2F); x <= (int) Math
							.floor((float) ConfigOptions.islandSize / 2F); x++) {
						for (int z = -(int) Math.floor((float) ConfigOptions.islandSize / 2F); z <= (int) Math
								.floor((float) ConfigOptions.islandSize / 2F); z++) {
							BlockPos pos = new BlockPos(spawn.getX() + x, spawn.getY(), spawn.getZ() + z);
							if (world.getBiome(pos).getFloatTemperature(new BlockPos(pos)) < 0.5F)
								world.getChunkFromBlockCoords(pos).getBiomeArray()[(pos.getZ() & 15) << 4
										| (pos.getX() & 15)] = (byte) Biome.getIdForBiome(Biomes.PLAINS);
							if (x == 0 && z == 0 && ConfigOptions.spawnWater)
								world.setBlockState(pos.down(3), Blocks.WATER.getDefaultState(), 2);
							else
								world.setBlockState(pos.down(3), Blocks.PLANKS.getStateFromMeta(ConfigOptions.woodMeta),
										2);
							if (ConfigOptions.spawnBedrock)
								world.setBlockState(pos.down(4), Blocks.BEDROCK.getDefaultState(), 2);
							else if (ConfigOptions.replaceBedrock)
								world.setBlockState(pos.down(4), Blocks.PLANKS.getStateFromMeta(ConfigOptions.woodMeta),
										2);
						}
					}
					BlockPos pos = new BlockPos(spawn.getX() - 1, spawn.getY(), spawn.getZ() + 1);
					if (ConfigOptions.spawnString) {
						world.setBlockState(pos.down(2), Blocks.TRIPWIRE.getDefaultState(), 2);
					}
					if (ConfigOptions.spawnChest) {
						pos = new BlockPos(spawn.getX(), spawn.getY() - 2, spawn.getZ() - 1);
						world.setBlockState(pos, Blocks.CHEST.getDefaultState());
					}
					changeBiome(spawn.getX(), spawn.getZ(), world);
				}
			});
		}

		if (isValidGoG()) {
			IslandManager.registerIsland(new IslandGen("gog") {
				public void generate(World world, BlockPos spawn) {
					GoGSupport.spawnGoGIsland(world, spawn);
					if (ConfigOptions.spawnChest) {
						BlockPos pos = new BlockPos(spawn.getX(), spawn.getY() - 2, spawn.getZ() - 1);
						world.setBlockState(pos, Blocks.CHEST.getDefaultState());
					}
					changeBiome(spawn.getX(), spawn.getZ(), world);
				}
			});
		}
	}

	public static boolean isValidGoG() {
		return ConfigOptions.enableGOGIsland && Loader.isModLoaded("botania") && Loader.isModLoaded("gardenofglass");
	}

	private static void changeBiome(int xIs, int zIs, World world) {
		if (ConfigOptions.islandBiomeID >= 0) {
			for (int x = xIs - (int) Math.floor((float) ConfigOptions.islandBiomeRange / 2F); x <= xIs
					+ (int) Math.floor((float) ConfigOptions.islandBiomeRange / 2F); x++) {
				for (int z = zIs - (int) Math.floor((float) ConfigOptions.islandBiomeRange / 2F); z <= zIs
						+ (int) Math.floor((float) ConfigOptions.islandBiomeRange / 2F); z++) {
					world.getChunkFromBlockCoords(new BlockPos(x, 64, z))
							.getBiomeArray()[(new BlockPos(x, 64, z).getZ() & 15) << 4
									| (new BlockPos(x, 64, z).getX() & 15)] = (byte) ConfigOptions.islandBiomeID;
				}
			}
		}
	}
}
