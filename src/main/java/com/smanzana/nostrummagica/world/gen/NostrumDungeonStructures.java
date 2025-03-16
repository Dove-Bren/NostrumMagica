package com.smanzana.nostrummagica.world.gen;

import com.smanzana.autodungeons.world.gen.DungeonStructure;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeons;

import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class NostrumDungeonStructures {
	
	public static class DragonStructure extends DungeonStructure {

		public DragonStructure() {
			super(NostrumDungeons.DRAGON_DUNGEON);
		}
		
		@Override
		protected boolean /*hasStartAt*/ isFeatureChunk(ChunkGenerator generator, BiomeSource biomeProvider, long seed, WorldgenRandom rand, int x, int z, Biome biome, ChunkPos pos, NoneFeatureConfiguration config) {
			rand.setSeed((long)(x ^ z << 4) ^ seed);
			return rand.nextInt(2) < 1;
		}
	}
	
	public static class PortalStructure extends DungeonStructure {

		public PortalStructure() {
			super(NostrumDungeons.PORTAL_DUNGEON);
		}
		
		@Override
		protected boolean /*hasStartAt*/ isFeatureChunk(ChunkGenerator generator, BiomeSource biomeProvider, long seed, WorldgenRandom rand, int x, int z, Biome biome, ChunkPos pos, NoneFeatureConfiguration config) {
			// Spawn a portal shrine somewhere in the 32x32 chunks around 0
//			if (x == (int) ((seed & (0x1F << 14)) >> 14) - 16
//					&& z == (int) ((seed & (0x1F << 43)) >> 43) - 16) {
//				return true;
//			}
//			
//			rand.setSeed((long)(x ^ z << 4) ^ seed);
//			return rand.nextInt(2) < 1;
			return true;
		}
	}
	
	public static class PlantBossStructure extends DungeonStructure {

		public PlantBossStructure() {
			super(NostrumDungeons.PLANTBOSS_DUNGEON);
		}

		@Override
		protected boolean /*hasStartAt*/ isFeatureChunk(ChunkGenerator generator, BiomeSource biomeProvider, long seed, WorldgenRandom rand, int x, int z, Biome biome, ChunkPos pos, NoneFeatureConfiguration config) {
			rand.setSeed((long)(x ^ z << 4) ^ seed);
			return rand.nextInt(2) < 1;
		}
	}
	
}
