package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.IWorldHeightReader;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.Heightmap;

/**
 * Entrance staircase. Extends up to nearly surface level and then spawns a shrine.
 * @author Skyler
 *
 */
public class RoomExtendedEntranceStaircase extends StaticRoom implements IStaircaseRoom {
	
	public static final ResourceLocation ID_LIGHT = NostrumMagica.Loc("room_extended_entrance_staircase");
	public static final ResourceLocation ID_DARK = NostrumMagica.Loc("room_extended_entrance_staircase_dark");

	public RoomExtendedEntranceStaircase(boolean dark) {
		super(dark ? ID_DARK : ID_LIGHT, -2, 0, -2, 2, 3, 2,
				// Floor
				"BBBBB",
				"B  BB",
				"B  UB",
				"B N B",
				"BBBBB",
				//
				"BBBBB",
				"BBL B",
				"B   B",
				"B   B",
				"BBBBB",
				//
				"BBBBB",
				"B   B",
				"BD  B",
				"BB  B",
				"BBBBB",
				//
				"BBBBB",
				"B   B",
				"B   B",
				"B RBB",
				"BBBBB",
				'B', (dark ? NostrumBlocks.dungeonBlock : NostrumBlocks.lightDungeonBlock),
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				' ', null,
				'U', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'R', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'D', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'L', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)));
	}
	
	@Override
	public boolean canSpawnAt(IWorld world, BlueprintLocation start) {
		int minX = start.getPos().getX() - 5;
		int minY = start.getPos().getY();
		int minZ = start.getPos().getZ() - 5;
		int maxX = start.getPos().getX() + 5;
		int maxY = start.getPos().getY();
		int maxZ = start.getPos().getZ() + 5;
		for (int i = minX; i <= maxX; i++)
		for (int j = minY; j <= maxY; j++)
		for (int k = minZ; k <= maxZ; k++) {
			BlockPos pos = new BlockPos(i, j, k);
			BlockState cur = world.getBlockState(pos);
		
			// Check if unbreakable...
			if (cur != null && cur.getBlockHardness(world, pos) == -1)
				return false;
		}
		
		return true;
	}
	
	@Override
	public void spawn(IWorld world, BlueprintLocation start, MutableBoundingBox bounds, UUID dungeonID) {
		getEntryStart((type, x, z) -> world.getHeight(type, x, z), start, true, world, bounds, dungeonID);
	}
	
	@Override
	public BlueprintLocation getEntryStart(IWorldHeightReader world, BlueprintLocation start) {
		return getEntryStart(world, start, false, null, null, null);
	}
	
	private BlueprintLocation getEntryStart(IWorldHeightReader heightReader, BlueprintLocation start, boolean spawn, IWorld world, MutableBoundingBox bounds, UUID dungeonID) {
		int stairHeight = 4;
		BlockPos pos = start.getPos();
		
		BlockPos blockpos = new BlockPos(pos.getX(), heightReader.getHeight(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()), pos.getZ());
		
		int maxY = blockpos.getY();
		BlockPos cur = start.getPos();
		int loops = 0;
		while (cur.getY() < maxY - 17
				|| loops < 2 // Make sure we always do at least two
			) {
			if (spawn) {
				super.spawn(world, new BlueprintLocation(cur, start.getFacing()), bounds, dungeonID);
			}
			cur = cur.add(0, stairHeight, 0);
			loops++;
		}
		
		return new BlueprintLocation(cur, start.getFacing());
	}
	
	@Override
	public int getNumExits() {
		return 0;
	}

	@Override
	public List<BlueprintLocation> getExits(BlueprintLocation start) {
		return new LinkedList<>();
	}

	@Override
	public int getDifficulty() {
		return 0;
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
	public BlueprintLocation getKeyLocation(BlueprintLocation start) {
		return null;
	}
	
	@Override
	public boolean supportsTreasure() {
		return false;
	}

	@Override
	public List<BlueprintLocation> getTreasureLocations(BlueprintLocation start) {
		return null;
	}

	@Override
	public boolean hasEnemies() {
		return false;
	}

	@Override
	public boolean hasTraps() {
		return false;
	}

	@Override
	public MutableBoundingBox getBounds(BlueprintLocation start) {
		// This should repeat what spawn does and find the actual bounds, but that requires querying the world which
		// this method would like to not do.
		// So instead, guess based on start to an approximate height of 128.
		
		final BlockPos topPos = new BlockPos(start.getPos().getX(), 128, start.getPos().getZ());
		MutableBoundingBox bounds = null;
		
		// Add staircase down to actual start
		final int stairHeight = 4;
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		cursor.setPos(start.getPos());
		for (int i = start.getPos().getY(); i < topPos.getY(); i+= stairHeight) {
			cursor.setY(i);
			if (bounds == null) {
				bounds = super.getBounds(new BlueprintLocation(cursor, start.getFacing()));
			} else {
				bounds.expandTo(super.getBounds(new BlueprintLocation(cursor, start.getFacing())));
			}
		}
		
		return bounds;
	}

	@Override
	public List<String> getRoomTags() {
		return new ArrayList<>(); // don't autoplace anywhere
	}

	@Override
	public String getRoomName() {
		return "Descent";
	}
}
