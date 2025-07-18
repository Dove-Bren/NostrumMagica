package com.smanzana.nostrummagica.world.gen;

import com.smanzana.autodungeons.world.gen.DungeonStructure;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeons;

import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;

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
	
	public static class VaniSolarStructure extends DungeonStructure {

		public VaniSolarStructure() {
			super(NostrumDungeons.VANI_SOLAR_DUNGEON);
		}
		
	}
	
	public static class LegacySorceryStructure extends DungeonStructure {

		public LegacySorceryStructure() {
			super(NostrumDungeons.LEGACY_SORCERY_DUNGEON);
		}
		
	}
	
	public static class ElementalTrialStructure extends DungeonStructure {

		public ElementalTrialStructure() {
			super(NostrumDungeons.ELEMENTAL_TRIAL_DUNGEON);
		}

		@Override
		public PostPlacementProcessor getPostPlacementProcessor() {
			System.out.println();
			System.out.println();
			System.out.println("Post process for elemental trials");
			System.out.println();
			System.out.println();
			return super.getPostPlacementProcessor();
		}
		
		
		
	}
}
