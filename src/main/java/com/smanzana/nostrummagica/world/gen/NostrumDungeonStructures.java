package com.smanzana.nostrummagica.world.gen;

import com.smanzana.autodungeons.world.gen.DungeonStructure;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeons;

public abstract class NostrumDungeonStructures {
	
	public static class DragonStructure extends DungeonStructure {

		public DragonStructure() {
			super(NostrumDungeons.DRAGON_DUNGEON);
		}
		
//		@Override
//		public boolean canGenerate(RegistryAccess registry, ChunkGenerator generator, BiomeSource biomeProvider, StructureManager structures, long seed, ChunkPos pos, NoneFeatureConfiguration config, LevelHeightAccessor p_197179_, Predicate<Holder<Biome>> height) {
//			Random rand = new Random((long)(pos.x ^ pos.z << 4) ^ seed);
//			if (rand.nextInt(2) < 1) {
//				return super.canGenerate(registry, generator, biomeProvider, structures, seed, pos, config, p_197179_, height);
//			}
//			return false;
//		}
	}
	
	public static class PortalStructure extends DungeonStructure {

		public PortalStructure() {
			super(NostrumDungeons.PORTAL_DUNGEON);
		}
		
	}
	
	public static class PlantBossStructure extends DungeonStructure {

		public PlantBossStructure() {
			super(NostrumDungeons.PLANTBOSS_DUNGEON);
		}

//		@Override
//		public boolean canGenerate(RegistryAccess registry, ChunkGenerator generator, BiomeSource biomeProvider, StructureManager structures, long seed, ChunkPos pos, NoneFeatureConfiguration config, LevelHeightAccessor p_197179_, Predicate<Holder<Biome>> height) {
//			Random rand = new Random((long)(pos.x ^ pos.z << 4) ^ seed);
//			if (rand.nextInt(2) < 1) {
//				return super.canGenerate(registry, generator, biomeProvider, structures, seed, pos, config, p_197179_, height);
//			}
//			return false;
//		}
	}
	
	public static class ManiCastleStructure extends DungeonStructure {

		public ManiCastleStructure() {
			super(NostrumDungeons.MANI_CASTLE_DUNGEON);
		}
		
	}
	
	public static class SorceryIslandStructure extends DungeonStructure {

		public SorceryIslandStructure() {
			super(NostrumDungeons.SORCERY_ISLAND_DUNGEON);
		}
		
	}
	
	public static class KaniDungeonStructure extends DungeonStructure {

		public KaniDungeonStructure() {
			super(NostrumDungeons.KANI_JAIL_DUNGEON);
		}
		
	}
}
