package com.smanzana.nostrummagica.world.gen;

import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.utils.DimensionUtils;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumLoadedDungeon;
import com.smanzana.nostrummagica.world.dungeon.room.DragonStartRoom;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;
import com.smanzana.nostrummagica.world.dungeon.room.LoadedRoom;
import com.smanzana.nostrummagica.world.dungeon.room.LoadedStartRoom;
import com.smanzana.nostrummagica.world.dungeon.room.RoomArena;
import com.smanzana.nostrummagica.world.dungeon.room.RoomChallenge2;
import com.smanzana.nostrummagica.world.dungeon.room.RoomEnd1;
import com.smanzana.nostrummagica.world.dungeon.room.RoomGrandHallway;
import com.smanzana.nostrummagica.world.dungeon.room.RoomGrandStaircase;
import com.smanzana.nostrummagica.world.dungeon.room.RoomJail1;
import com.smanzana.nostrummagica.world.dungeon.room.RoomLectern;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;

public abstract class NostrumDungeonStructure extends Structure<NoFeatureConfig> {
	
//	public static final class NostrumDungeonConfig implements IFeatureConfig {
//		
////		public static final Codec<EmptyChunkGen> CODEC = RecordCodecBuilder.create(instance -> instance.group( 
////				RegistryLookupCodec.getLookUpCodec(Registry.BIOME_KEY).forGetter(EmptyChunkGen::getBiomeRegistry),
////				ResourceLocation.CODEC.xmap(s -> RegistryKey.getOrCreateKey(Registry.BIOME_KEY, s), k -> k.getLocation()).fieldOf("biome").forGetter(EmptyChunkGen::getBiome)
////			).apply(instance, EmptyChunkGen::new));
//		
//		public static Codec<NostrumDungeonConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
//				Codec.INT.optionalFieldOf("dummy", 1).forGetter(NostrumDungeonConfig::getDummy)
//			).apply(instance, NostrumDungeonConfig::new));
//
//		public NostrumDungeonConfig(int dummy) {
//			;
//		}
//		
//		public int getDummy() {
//			return 1;
//		}
//
//		public boolean allowedInDimension(RegistryKey<World> dimension) {
//			return DimensionUtils.IsOverworld(dimension);
//		}
//		
//		public boolean allowedAtPos(IWorld world, BlockPos pos) {
//			return true;
//		}
//		
//	}
	
	public NostrumDungeonStructure() {
		super(NoFeatureConfig.field_236558_a_);
	}
	
	@Override
	protected boolean /*hasStartAt*/ func_230363_a_(ChunkGenerator generator, BiomeProvider biomeProvider, long seed, SharedSeedRandom rand, int x, int z, Biome biome, ChunkPos pos, NoFeatureConfig config) {
		return super.func_230363_a_(generator, biomeProvider, seed, rand, x, z, biome, pos, config);
	}
	
//	@Override
//	public int getSize() {
//		return 8;
//	}

	/**
	 * Generation stage for when to generate the structure. there are 10 stages you can pick from!
	 * This surface structure stage places the structure before plants and ores are generated.
	 */
	@Override
	public GenerationStage.Decoration getDecorationStage() {
		return GenerationStage.Decoration.SURFACE_STRUCTURES;
	}
	
	// What is basically an 'instance' of the struct in MC gen. Doesn't have to do much besides generate logical dungeon and populate children list.
	public static class Start extends StructureStart<NoFeatureConfig> {
		
		private final NostrumDungeon dungeon;
		
		public Start(NostrumDungeon dungeon, Structure<NoFeatureConfig> parent, int i1, int i2, MutableBoundingBox bounds, int i3, long l1) {
			super(parent, i1, i2, bounds, i3, l1);
			this.dungeon = dungeon;
		}

		@Override
		public void /*init*/ func_230364_a_(DynamicRegistries registries, ChunkGenerator generator, TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoFeatureConfig config) {
			// Pick random Y between 30 and 60 to start
			final int y = this.rand.nextInt(30) + 30;
			// Center in chunk to try and avoid some 'CanSpawnHere' chunk spillage
			final int x = (chunkX * 16) + 8;
			final int z = (chunkZ * 16) + 8;
			
			final DungeonExitPoint start = new DungeonExitPoint(new BlockPos(x, y, z), Direction.Plane.HORIZONTAL.random(this.rand));
			List<DungeonRoomInstance> instances = this.dungeon.generate(start);
			
			for (DungeonRoomInstance instance : instances) {
				DungeonPiece piece = new DungeonPiece(instance);
				components.add(piece);
			}
			
			this.recalculateStructureSize();
		}
	}
	
	public static class DungeonPiece extends StructurePiece {
		
		protected DungeonRoomInstance instance;
		
		public DungeonPiece(DungeonRoomInstance instance) {
			super(DungeonPieceSerializer.instance, 0);
			this.instance = instance;
			
			this.boundingBox = instance.getBounds();
		}
		
		@Override
		protected void readAdditional(CompoundNBT tagCompound) { // Note: Actually "WRITE" !!!
			DungeonPieceSerializer.write(this, tagCompound);
		}

		@Override
		public boolean /*addComponentParts*/ func_230383_a_(ISeedReader worldIn, StructureManager manager, ChunkGenerator chunkGen,
				Random randomIn, MutableBoundingBox structureBoundingBoxIn,
				ChunkPos chunkPosIn, BlockPos something) {
			
			// Stop gap: is this the overworld?
			if (!DimensionUtils.IsOverworld(worldIn.getWorld())) {
				return false;
			}
			
			instance.spawn(worldIn, structureBoundingBoxIn);
			return true;
		}
		
	}
	
	public static class DungeonPieceSerializer implements IStructurePieceType {
		
		public static final String PIECE_ID = "nostrummagica:dungeonpiecedynamic";
		public static final DungeonPieceSerializer instance = new DungeonPieceSerializer();
		private static final String NBT_DATA = "nostrumdungeondata";

		@Override
		public DungeonPiece load(TemplateManager templateManager, CompoundNBT tag) {
			final CompoundNBT subTag = tag.getCompound(NBT_DATA);
			DungeonRoomInstance instance = DungeonRoomInstance.fromNBT(subTag);
			return new DungeonPiece(instance);
		}
		
		public static void write(DungeonPiece piece, CompoundNBT tagCompound) {
			tagCompound.put(NBT_DATA, piece.instance.toNBT(null));
		}
		
	}
	
	public static class DragonStructure extends NostrumDungeonStructure {

		public DragonStructure() {
			super();
		}
		
		@Override
		protected boolean /*hasStartAt*/ func_230363_a_(ChunkGenerator generator, BiomeProvider biomeProvider, long seed, SharedSeedRandom rand, int x, int z, Biome biome, ChunkPos pos, NoFeatureConfig config) {
			int unused; // I think this is way too big now
			return rand.nextInt(100*100) < 1;
		}

		@Override
		public IStartFactory<NoFeatureConfig> getStartFactory() {
			return (Structure<NoFeatureConfig> parent, int i1, int i2, MutableBoundingBox bounds, int i3, long l1)
					-> {
						return new Start(DRAGON_DUNGEON, parent, i1, i2, bounds, i3, l1);
					};
		}
		
//		@Override
//		public String getStructureName() {
//			return NostrumMagica.MODID + ":OverworldDragonDungeon";
//		}
		
	}
	
	public static class PortalStructure extends NostrumDungeonStructure {

		public PortalStructure() {
			super();
		}
		
		@Override
		protected boolean /*hasStartAt*/ func_230363_a_(ChunkGenerator generator, BiomeProvider biomeProvider, long seed, SharedSeedRandom rand, int x, int z, Biome biome, ChunkPos pos, NoFeatureConfig config) {
			// Spawn a portal shrine somewhere in the 32x32 chunks around 0
			if (x == (int) ((seed & (0x1F << 14)) >> 14) - 16
					&& z == (int) ((seed & (0x1F << 43)) >> 43) - 16) {
				return true;
			}
			
			return rand.nextInt(200*200) < 4;
		}

		@Override
		public IStartFactory<NoFeatureConfig> getStartFactory() {
			return (Structure<NoFeatureConfig> parent, int i1, int i2, MutableBoundingBox bounds, int i3, long l1)
					-> {
						return new Start(PORTAL_DUNGEON, parent, i1, i2, bounds, i3, l1);
					};
		}
		
//		@Override
//		public String getStructureName() {
//			return NostrumMagica.MODID + ":OverworldPortalDungeon";
//		}
		
	}
	
	public static class PlantBossStructure extends NostrumDungeonStructure {

		public PlantBossStructure() {
			super();
		}

		@Override
		protected boolean /*hasStartAt*/ func_230363_a_(ChunkGenerator generator, BiomeProvider biomeProvider, long seed, SharedSeedRandom rand, int x, int z, Biome biome, ChunkPos pos, NoFeatureConfig config) {
			return rand.nextInt(100*100) < 1;
		}

		@Override
		public IStartFactory<NoFeatureConfig> getStartFactory() {
			return (Structure<NoFeatureConfig> parent, int i1, int i2, MutableBoundingBox bounds, int i3, long l1)
					-> {
						return new Start(PLANTBOSS_DUNGEON, parent, i1, i2, bounds, i3, l1);
					};
		}
		
//		@Override
//		public String getStructureName() {
//			return NostrumMagica.MODID + ":OverworldPlantBossDungeon";
//		}
		
	}
	
	
	
	

//	public static class WorldGenNostrumShrine {
//		
//		private NostrumDungeon dungeon;
//		
//		public WorldGenNostrumShrine(NostrumDungeon dungeon) {
//			this.dungeon = dungeon;
//		}
//		
//		public boolean generate(IWorld worldIn, Random rand, BlockPos position) {
//	        dungeon.spawn(worldIn,
//	        		new NostrumDungeon.DungeonExitPoint(position, 
//	        				Direction.Plane.HORIZONTAL.random(rand)
//	        				));
//	        
//	        return true;
//	    }
//	}
	
	public static NostrumDungeon DRAGON_DUNGEON = new NostrumLoadedDungeon(
			"dragon",
			new DragonStartRoom(),
			new RoomArena(),
			4, 1
			).add(new RoomGrandStaircase())
			 .add(new RoomEnd1(false, true))
			 .add(new RoomGrandHallway())
			 .add(new RoomGrandHallway())
			 .add(new RoomGrandHallway())
			 .add(new RoomJail1())
			 .add(new RoomJail1())
			 .add(new RoomChallenge2())
			 .add(new RoomChallenge2())
			 .add(new RoomLectern())
			 .add(new RoomEnd1(true, false))
			 .add(new RoomEnd1(false, false));
	
	public static final String PORTAL_ROOM_NAME = "portal_room";
	
	public static NostrumDungeon PORTAL_DUNGEON = new NostrumLoadedDungeon(
			"portal",
			new LoadedStartRoom(DungeonRoomRegistry.instance().getRoomRecord("portal_lobby"),
					DungeonRoomRegistry.instance().getRoomRecord("portal_entrance")),
			new LoadedRoom(DungeonRoomRegistry.instance().getRoomRecord(PORTAL_ROOM_NAME))
			).add(new RoomGrandStaircase())
			 .add(new RoomEnd1(false, true))
			 .add(new RoomGrandHallway())
			 .add(new RoomGrandHallway())
			 .add(new RoomGrandHallway())
			 .add(new RoomJail1())
			 .add(new RoomJail1())
			 .add(new RoomChallenge2())
			 .add(new RoomChallenge2())
			 .add(new RoomLectern())
			 .add(new RoomEnd1(true, false))
			 .add(new RoomEnd1(false, false));
	
	public static final String PLANTBOSS_ROOM_NAME = "plant_boss_room";
	
	public static NostrumDungeon PLANTBOSS_DUNGEON = new NostrumLoadedDungeon(
			"plant_boss",
			new LoadedStartRoom(DungeonRoomRegistry.instance().getRoomRecord("plantboss_lobby"),
					DungeonRoomRegistry.instance().getRoomRecord("plantboss_dungeon_entrance")),
			new LoadedRoom(DungeonRoomRegistry.instance().getRoomRecord(PLANTBOSS_ROOM_NAME))
			).add(new RoomGrandStaircase())
			 .add(new RoomEnd1(false, true))
			 .add(new RoomGrandHallway())
			 .add(new RoomGrandHallway())
			 .add(new RoomGrandHallway())
			 .add(new RoomJail1())
			 .add(new RoomJail1())
			 .add(new RoomChallenge2())
			 .add(new RoomChallenge2())
			 .add(new RoomLectern())
			 .add(new RoomEnd1(true, false))
			 .add(new RoomEnd1(false, false));
	
}
