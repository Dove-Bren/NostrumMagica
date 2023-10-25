package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

// Tiny tight spiral staircase used when creating the entrance to the dungeon
public class RoomEntryShrine extends StaticRoom {
	
	private SpellComponentWrapper component;
	
	public RoomEntryShrine(SpellComponentWrapper component, boolean dark) {
		
		super("RoomEntryShrine", -5, 0, -5, 5, 21, 5, // Would have trouble and need to be unique per component and registered?
				// Floor
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBB  BBBBB",
				"BBBB  UBBBB",
				"BBBB   BBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B   l l   B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B   lSl   B",
				"B         B",
				"B         B",
				"B         B",
				"BE       WB",
				"B         B",
				"B         B",
				"B         B",
				"B    N    B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B   l l   B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B   l l   B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B   l l   B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"BBBBlBlBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				//
				"BB  BBB  BB",
				"B   l l   B",
				"           ",
				"           ",
				"B         B",
				"B         B",
				"B         B",
				"           ",
				"           ",
				"B         B",
				"BB  BBB  BB",
				//
				"BB  BBB  BB",
				"B         B",
				"           ",
				"           ",
				"B         B",
				"B         B",
				"B         B",
				"           ",
				"           ",
				"B         B",
				"BB  BBB  BB",
				//
				"BB  BBB  BB",
				"B         B",
				"           ",
				"           ",
				"B         B",
				"B         B",
				"          B",
				"           ",
				"           ",
				"B         B",
				"BB  BBB  BB",
				//
				"BB  BBB  BB",
				"B         B",
				"           ",
				"           ",
				"B         B",
				"B         B",
				"          B",
				"           ",
				"           ",
				"B         B",
				"BB  BBB  BB",
				//
				"BB  BB   BB",
				"B          ",
				"           ",
				"           ",
				"          B",
				"B         B",
				"           ",
				"           ",
				"           ",
				"B         B",
				"BB  BBB  B ",
				//
				"BB  BB   B ",
				"B          ",
				"           ",
				"           ",
				"           ",
				"B         B",
				"          B",
				"           ",
				"           ",
				"B         B",
				"BB  BB   B ",
				//
				"BB  BB     ",
				"B          ",
				"           ",
				"           ",
				"           ",
				"          B",
				"           ",
				"           ",
				"           ",
				"          B",
				"BB  BB     ",
				//
				"BB  BB     ",
				"B          ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"          B",
				"BB   B     ",
				//
				"BB   B     ",
				"B          ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"BB         ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				'B', NostrumBlocks.lightDungeonBlock,
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				'S', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.SOUTH)),
				'l', new StaticBlockState(Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH)),
				' ', null,
				'U', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)));
		
		this.component = component;
	}

	@Override
	public int getNumExits() {
		return 0;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		return new LinkedList<>();
	}

	@Override
	public int getDifficulty() {
		return 0;
	}

	@Override
	public boolean hasEnemies() {
		return true;
	}

	@Override
	public boolean hasTraps() {
		return false;
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
		return new LinkedList<>();
	}
	
	@Override
	public void spawn(IWorld world, DungeonExitPoint start, @Nullable MutableBoundingBox bounds) {
		super.spawn(world, start, bounds);
		
		final BlockPos pos = start.getPos().add(0, 21, 0);
		if (this.component != null && (bounds == null || bounds.isVecInside(pos))) {
			NostrumBlocks.symbolBlock.setInWorld(world, pos, component);
		}
	}
}
