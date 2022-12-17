package com.smanzana.nostrummagica.world;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
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

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class NostrumDungeonGenerator implements IWorldGenerator {

	private static class WorldGenNostrumShrine extends WorldGenerator {
		
		private NostrumDungeon dungeon;
		
		public WorldGenNostrumShrine(NostrumDungeon dungeon) {
			this.dungeon = dungeon;
		}
		
		@Override
		public boolean generate(World worldIn, Random rand, BlockPos position)
	    {
	        dungeon.spawn(worldIn,
	        		new NostrumDungeon.DungeonExitPoint(position, 
	        				EnumFacing.HORIZONTALS[rand.nextInt(4)]
	        				));
	        
	        return true;
	    }
	}
	
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
			new LoadedRoom(DungeonRoomRegistry.instance().getRoom(PORTAL_ROOM_NAME))
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
			new LoadedRoom(DungeonRoomRegistry.instance().getRoom(PLANTBOSS_ROOM_NAME))
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
	
//	public static NostrumDungeon PORTAL_DUNGEON = new NostrumDungeon(
//			new DragonStartRoom(),
//			new RoomPortal()
//			).add(new RoomGrandStaircase())
//			 .add(new RoomEnd1(false, true))
//			 .add(new RoomGrandHallway())
//			 .add(new RoomGrandHallway())
//			 .add(new RoomGrandHallway())
//			 .add(new RoomJail1())
//			 .add(new RoomJail1())
//			 .add(new RoomChallenge2())
//			 .add(new RoomChallenge2())
//			 .add(new RoomLectern())
//			 .add(new RoomEnd1(true, false))
//			 .add(new RoomEnd1(false, false));
	
	public static enum DungeonGen {
		PORTAL((i) -> { return new WorldGenNostrumShrine(PORTAL_DUNGEON);}, 20, 70),
		DRAGON((i) -> { return new WorldGenNostrumShrine(DRAGON_DUNGEON);}, 30, 60),
		PLANTBOSS((i) -> { return new WorldGenNostrumShrine(PLANTBOSS_DUNGEON);}, 30, 90),
		;
//		TEST(new WorldGenNostrumShrine(new NostrumLoadedDungeon("test", new StartRoom(), new RoomArena())), 30, 60);
		
		private WorldGenerator gen = null;
		private final Function<Integer, WorldGenerator> constructor;
		private final int minY;
		private final int maxY;
		
		private DungeonGen(Function<Integer, WorldGenerator> constructor, int minY, int maxY) {
			this.constructor = constructor;
			this.minY = minY;
			this.maxY = maxY;
		}
		
		public void initGen() {
			if (this.gen != null) {
				throw new RuntimeException("Init called twice on smae DungeonGen enum value");
			}
			gen = constructor.apply(0);
		}
		
		public WorldGenerator getGenerator() {
			return gen;
		}

		public int getMinY() {
			return minY;
		}

		public int getMaxY() {
			return maxY;
		}
		
		public boolean chanceSpawn(Random random, World world, int chunkX, int chunkZ, int bonusOdds) {
			int count = 0;
			int chance = 1;
			
			// Easy way to think about it:
			// [count] many in every [chance] chunks.
			// Note: CHUNK. So multiply by 16 to get blocks.
			
			if (this == PORTAL) {
				count = 4;
				chance = 200*200;
			}
			else if (this == DRAGON) {
				count = 1;
				chance = 100*100;
			} else if (this == PLANTBOSS) {
				count = 1;
				chance = 100 * 100;
			}
			
			// Add bonus chance from bonus items
			count *= Math.max(1, Math.min(MAX_BOOSTS, bonusOdds + 1));
			
			return random.nextInt(chance) < count;
		}
	}
	
	public static void initGens() {
		for (DungeonGen gen : DungeonGen.values()) {
			gen.initGen();
		}
	}
	
	private List<DungeonGen> list;
	private static boolean generating = false; // PREVENT triggering spawns of other dungs recursively
	
	// Some items increase odds of next dungeon spawning. Keep track of that here.
	private static Map<DungeonGen, Integer> increasedOdds;
	
	public static final int MAX_BOOSTS = 10;
	
	public NostrumDungeonGenerator() {
		increasedOdds = new EnumMap<>(DungeonGen.class);
	}
	
	/**
	 * Attempts to add to the chance that a dungeon type will be spawned.
	 * Returns whether bonus was applied. Failure indicates rate is maxed already.
	 * @param type
	 * @return
	 */
	public static boolean boostOdds(DungeonGen type) {
		Integer boost = increasedOdds.get(type);
		
		if (boost != null && boost >= MAX_BOOSTS)
			return false;
		
		int count = 1;
		if (boost != null)
			count += boost;
		
		increasedOdds.put(type, count);
		return true;
	}
	
	public static int getBonusOdds(DungeonGen type) {
		Integer boost = increasedOdds.get(type);
		if (boost == null)
			return 0;
		
		return (int) boost;
	}
	
	protected static void clearOdds(DungeonGen type) {
		increasedOdds.remove(type);
	}
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		
		// Do not allow recursion!
		if (NostrumDungeonGenerator.generating) {
			return;
		}
		
		int dim = world.provider.getDimension();
		int[] dims = ModConfig.config.getDimensionList();
		boolean found = false;
		for (int d : dims) {
			if (d == dim) {
				found = true;
				break;
			}
		}
		
		if (!found)
			return;
		
		NostrumDungeonGenerator.generating = true;
		
		long seed = world.getSeed();
		// Spawn a portal shrine somewhere in the 32x32 chunks around 0
		if (chunkX == (int) ((seed & (0x1F << 14)) >> 14) - 16
				&& chunkZ == (int) ((seed & (0x1F << 43)) >> 43) - 16) {
			DungeonGen gen = DungeonGen.PORTAL;
			runGenerator(gen.getGenerator(), world, random, chunkX, chunkZ,
					gen.getMinY(), gen.getMaxY());
			NostrumDungeonGenerator.generating = false;
			return;
		}
		
		if (list == null) {
			list = new LinkedList<DungeonGen>();
			for (DungeonGen gen : DungeonGen.values())
				list.add(gen);
		}
		Collections.shuffle(list);
		
		for (DungeonGen gen : list) {
			if (gen.chanceSpawn(random, world, chunkX, chunkZ, getBonusOdds(gen))) {
				NostrumMagica.logger.info("Generating NostrumMagica dungeon with type " + gen.name());
				runGenerator(gen.getGenerator(), world, random, chunkX, chunkZ,
					gen.getMinY(), gen.getMaxY());
				clearOdds(gen);
				break;
			}
		}
		NostrumDungeonGenerator.generating = false;
		
	}

	/**
	 * Taken from bedrockminer's worldgen tutorial
	 * http://bedrockminer.jimdo.com/modding-tutorials/basic-modding-1-8/world-generation/
	 * @param generator
	 * @param world
	 * @param rand
	 * @param chunk_X
	 * @param chunk_Z
	 * @param minHeight
	 * @param maxHeight
	 */
	private void runGenerator(WorldGenerator generator, World world, Random rand,
			int chunk_X, int chunk_Z, int minHeight, int maxHeight) {
	    if (minHeight < 0 || maxHeight > 256 || minHeight > maxHeight)
	        throw new IllegalArgumentException("Illegal Height Arguments for WorldGenerator");

	    int heightDiff = maxHeight - minHeight + 1;
        int x = chunk_X * 16 + rand.nextInt(16);
        int z = chunk_Z * 16 + rand.nextInt(16);
        int y = minHeight + rand.nextInt(heightDiff);
        
        BlockPos pos =  new BlockPos(x, y, z);
        		
       	generator.generate(world, rand, pos);
	}
	
}
