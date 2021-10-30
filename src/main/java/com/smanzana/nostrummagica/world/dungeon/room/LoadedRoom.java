package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.dungeon.LootUtil;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Room where all blocks are loaded from a file at startup.
 * @author Skyler
 *
 */
public class LoadedRoom implements IDungeonRoom {
	
	private RoomBlueprint blueprint;
	private List<DungeonExitPoint> chestsRelative;
	
	public LoadedRoom(RoomBlueprint blueprint) {
		this.blueprint = blueprint;
		
		if (blueprint == null) {
			throw new RuntimeException("Blueprint null when creating LoadedRoom. Wrong room name looked up, or too early?");
		}
		
		// Find and save chest locations
		chestsRelative = new ArrayList<>();
		blueprint.scanBlocks((offset, block) -> {
			IBlockState state = block.getSpawnState(EnumFacing.NORTH); 
			if (state != null && state.getBlock() == Blocks.CHEST) {
				chestsRelative.add(new DungeonExitPoint(offset, state.getValue(BlockChest.FACING)));
			}
		});
	}
	
	// Need to have some sort of 'exit point' placeholder block so that I can encode doorways into the blueprint
	
	@Override
	public boolean canSpawnAt(World world, DungeonExitPoint start) {
		BlockPos dims = blueprint.getAdjustedDimensions(start.getFacing());
		BlockPos offset = blueprint.getAdjustedOffset(start.getFacing());
		
		int minX = start.getPos().getX() - offset.getX();
		int minY = start.getPos().getY() - offset.getY();
		int minZ = start.getPos().getZ() - offset.getZ();
		int maxX = minX + dims.getX();
		int maxY = minY + dims.getY();
		int maxZ = minZ + dims.getZ();
		for (int i = minX; i <= maxX; i++)
		for (int j = minY; j <= maxY; j++)
		for (int k = minZ; k <= maxZ; k++) {
			BlockPos pos = new BlockPos(i, j, k);
			IBlockState cur = world.getBlockState(pos);
		
			// Check if unbreakable...
			if (cur != null && cur.getBlockHardness(world, pos) == -1)
				return false;
		}
		
		return true;
	}
	
	@Override
	public void spawn(NostrumDungeon dungeon, World world, DungeonExitPoint start) {
		// See note about dungeon vs blueprint facing in @getExits
		blueprint.spawn(world, start.getPos(), start.getFacing());
		
		List<DungeonExitPoint> loots = this.getTreasureLocations(start);
		if (loots != null && !loots.isEmpty())
		for (NostrumDungeon.DungeonExitPoint lootSpot : loots) {
			LootUtil.generateLoot(world, lootSpot.getPos(), lootSpot.getFacing());
		}
	}

	@Override
	public int getNumExits() {
		Collection<DungeonExitPoint> exits = blueprint.getExits();
		return exits == null ? 0 : exits.size();
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		Collection<DungeonExitPoint> exits = blueprint.getExits();
		
		// Dungeon notion of direction is backwards to blueprints:
		// Dungeon wants facing to be you looking back through the door
		// Blueprint wants your facing as you go in the door. That's there the 'opposite' comes from.
		
		// Blueprint exits are rotated to the entry entry direction (and have their own rotation too).
		final EnumFacing modDir = RoomBlueprint.getModDir(blueprint.getEntry().getFacing(), start.getFacing());
		// Door offset and final rotation is what's in exits rotated modDir times
		
		List<DungeonExitPoint> ret;
		if (exits != null) {
			System.out.println("For start: " + start);
			ret = new ArrayList<>(exits.size());
			for (DungeonExitPoint door : exits) {
				EnumFacing doorDir = door.getFacing();
				int times = (modDir.getHorizontalIndex() + 2) % 4;
				while (times-- > 0) {
					doorDir = doorDir.rotateY();
				}
				final DungeonExitPoint fromEntry = new DungeonExitPoint(
						RoomBlueprint.applyRotation(door.getPos(), modDir),
						doorDir
						);
				final DungeonExitPoint relative = new DungeonExitPoint(start.getPos().add(fromEntry.getPos()), fromEntry.getFacing()); 
				ret.add(relative);
				System.out.println("Door at " + door + " -> " + relative);
			}
		} else {
			ret = new LinkedList<>();
		}
		return ret;
	}

	@Override
	public int getDifficulty() {
		return 1;
	}

	@Override
	public boolean supportsDoor() {
		return false;
	}

	@Override
	public boolean supportsKey() {
		return false;
	}

	@Override
	public DungeonExitPoint getKeyLocation(DungeonExitPoint start) {
		return null;
	}

	@Override
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start) {
		List<DungeonExitPoint> ret = new ArrayList<>();
		System.out.println("For start: " + start);
		for (DungeonExitPoint orig : chestsRelative) {
			final DungeonExitPoint relative = NostrumDungeon.asRotated(start, orig.getPos(), orig.getFacing().getOpposite()); 
			System.out.println("Chest at " + orig + " -> " + relative);
			ret.add(relative);
		}
		return ret;
	}

	@Override
	public boolean hasEnemies() {
		return true;
	}

	@Override
	public boolean hasTraps() {
		return true;
	}
}
