package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.config.ConfigOptions;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.ChunkProviderOverworld;

public class WorldTypeVoid extends WorldType {
	public WorldTypeVoid() {
		super("voidworld");
	}

	@Override
	public boolean showWorldInfoNotice() {
		return true;
	}

	@Override
	public int getMinimumSpawnHeight(World world) {
		return ConfigOptions.islandYSpawn;
	}

	public int getSpawnFuzz() {
		return 2;
	}

	@Override
	public float getCloudHeight() {
		return ConfigOptions.cloudHeight;
	}

	@Override
	public double getHorizon(World world) {
		return ConfigOptions.horizonHeight;
	}

	public BiomeProvider getBiomeProvider(World world) {
		if (ConfigOptions.singleBiomeID > -1) {
			return new BiomeProviderSingle(Biome.getBiome(ConfigOptions.singleBiomeID));
		} else {
			return new BiomeProvider(world.getWorldInfo());
		}
	}

	@Override
	public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
		if (!ConfigOptions.overworldGen) {
			ChunkProviderFlat provider = new ChunkProviderFlat(world, world.getSeed(), false,
					"3;1*" + ConfigOptions.bottomBlock + "," + (ConfigOptions.islandYSpawn - 3) + "*"
							+ ConfigOptions.fillBlock + ";");
			world.setSeaLevel(63);
			return provider;
		} else
			return new ChunkProviderOverworld(world, world.getSeed(), true, generatorOptions);
	}
}
