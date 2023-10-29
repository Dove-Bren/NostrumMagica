package com.smanzana.nostrummagica.world.gen;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumLoadedDungeon;
import com.smanzana.nostrummagica.world.dungeon.room.DragonStartRoom;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;
import com.smanzana.nostrummagica.world.dungeon.room.LoadedRoom;
import com.smanzana.nostrummagica.world.dungeon.room.RoomArena;
import com.smanzana.nostrummagica.world.dungeon.room.RoomChallenge2;
import com.smanzana.nostrummagica.world.dungeon.room.RoomEnd1;
import com.smanzana.nostrummagica.world.dungeon.room.RoomGrandHallway;
import com.smanzana.nostrummagica.world.dungeon.room.RoomGrandStaircase;
import com.smanzana.nostrummagica.world.dungeon.room.RoomJail1;
import com.smanzana.nostrummagica.world.dungeon.room.RoomLectern;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure.NostrumDungeonConfig;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;

public abstract class NostrumDungeonStructure extends Structure<NostrumDungeonConfig> {
	
	public static final class NostrumDungeonConfig implements IFeatureConfig {

		public static NostrumDungeonConfig deserialize(Dynamic<?> dynamic) {
			return new NostrumDungeonConfig();
		}

		@Override
		public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
			return new Dynamic<>(ops);
		}
		
		public boolean allowedInDimension(DimensionType dimension) {
			return dimension == DimensionType.OVERWORLD;
		}
		
		public boolean allowedAtPos(IWorld world, BlockPos pos) {
			return true;
		}
		
	}
	
	public NostrumDungeonStructure(Function<Dynamic<?>, ? extends NostrumDungeonConfig> dynamic) {
		super(dynamic);
	}
	
//	@Override
//	public boolean hasStartAt(ChunkGenerator<?> chunkGen, Random rand, int chunkPosX, int chunkPosZ) {
//		// Should start to generate one at this chunk?
//		// Note: Other structures are deterministic based on the chunk they're being rolled for.
//		// Could copy village/woodland manor's version and use a deterministic seeded random.
//		// Keeping original determination for now; roll for every new chunk.
//		
//		if (rand.nextInt(200*200) < 4) {
//			// Portal
//			
//		} else if (rand.nextInt(100*100) < 1) {
//			// Dragon
//		} else if (rand.nextInt(100*100) < 1) {
//			// PlantBoss
//		}
//	}
//	
//	public Structure.IStartFactory getStartFactory() {
//		return Start::new;
//	}

	@Override
	public int getSize() {
		return 8;
	}
	
	// What is basically an 'instance' of the struct in MC gen. Doesn't have to do much besides generate logical dungeon and populate children list.
	public static class Start extends StructureStart {
		
		private final NostrumDungeon dungeon;
		
		public Start(NostrumDungeon dungeon, Structure<?> parent, int i1, int i2, Biome biome, MutableBoundingBox bounds, int i3, long l1) {
			super(parent, i1, i2, biome, bounds, i3, l1);
			this.dungeon = dungeon;
		}

		@Override
		public void init(ChunkGenerator<?> generator, TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn) {
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
		public boolean addComponentParts(IWorld worldIn, Random randomIn, MutableBoundingBox structureBoundingBoxIn,
				ChunkPos chunkPosIn) {
			// Stop gap: is this the overworld?
			if (worldIn.getDimension().getType() != DimensionType.OVERWORLD) {
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

		public DragonStructure(Function<Dynamic<?>, ? extends NostrumDungeonConfig> dynamic) {
			super(dynamic);
		}

		@Override
		public boolean hasStartAt(ChunkGenerator<?> chunkGen, Random rand, int chunkPosX, int chunkPosZ) {
			return rand.nextInt(100*100) < 1;
		}

		@Override
		public IStartFactory getStartFactory() {
			return (Structure<?> parent, int i1, int i2, Biome biome, MutableBoundingBox bounds, int i3, long l1)
					-> {
						return new Start(DRAGON_DUNGEON, parent, i1, i2, biome, bounds, i3, l1);
					};
		}
		
		@Override
		public String getStructureName() {
			return NostrumMagica.MODID + ":OverworldDragonDungeon";
		}
		
	}
	
	public static class PortalStructure extends NostrumDungeonStructure {

		public PortalStructure(Function<Dynamic<?>, ? extends NostrumDungeonConfig> dynamic) {
			super(dynamic);
		}

		@Override
		public boolean hasStartAt(ChunkGenerator<?> chunkGen, Random rand, int chunkPosX, int chunkPosZ) {
			// Spawn a portal shrine somewhere in the 32x32 chunks around 0
			long seed = chunkGen.getSeed();
			if (chunkPosX == (int) ((seed & (0x1F << 14)) >> 14) - 16
					&& chunkPosZ == (int) ((seed & (0x1F << 43)) >> 43) - 16) {
				return true;
			}
			
			return rand.nextInt(200*200) < 4;
		}

		@Override
		public IStartFactory getStartFactory() {
			return (Structure<?> parent, int i1, int i2, Biome biome, MutableBoundingBox bounds, int i3, long l1)
					-> {
						return new Start(PORTAL_DUNGEON, parent, i1, i2, biome, bounds, i3, l1);
					};
		}
		
		@Override
		public String getStructureName() {
			return NostrumMagica.MODID + ":OverworldPortalDungeon";
		}
		
	}
	
	public static class PlantBossStructure extends NostrumDungeonStructure {

		public PlantBossStructure(Function<Dynamic<?>, ? extends NostrumDungeonConfig> dynamic) {
			super(dynamic);
		}

		@Override
		public boolean hasStartAt(ChunkGenerator<?> chunkGen, Random rand, int chunkPosX, int chunkPosZ) {
			return rand.nextInt(100*100) < 1;
		}

		@Override
		public IStartFactory getStartFactory() {
			return (Structure<?> parent, int i1, int i2, Biome biome, MutableBoundingBox bounds, int i3, long l1)
					-> {
						return new Start(PLANTBOSS_DUNGEON, parent, i1, i2, biome, bounds, i3, l1);
					};
		}
		
		@Override
		public String getStructureName() {
			return NostrumMagica.MODID + ":OverworldPlantBossDungeon";
		}
		
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
			new DragonStartRoom(),
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
			new DragonStartRoom(),
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
