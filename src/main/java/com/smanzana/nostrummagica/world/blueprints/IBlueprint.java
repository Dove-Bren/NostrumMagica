package com.smanzana.nostrummagica.world.blueprints;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

public interface IBlueprint {

	public default void spawn(IWorld world, BlockPos at, UUID globalID) {
		this.spawn(world, at, Direction.NORTH, globalID);
	}
	
	public default void spawn(IWorld world, BlockPos at, Direction direction, UUID globalID) {
		this.spawn(world, at, direction, (MutableBoundingBox) null, globalID, null);
	}
	
	public void spawn(IWorld world, BlockPos at, Direction direction, @Nullable MutableBoundingBox bounds, UUID globalID, @Nullable IBlueprintBlockPlacer spawner);
	
	public Collection<DungeonExitPoint> getExits();
	
	public DungeonExitPoint getEntry();
	
	/**
	 * Returns a preview of the blueprint centered around the blueprint entry point.
	 * Note that the preview is un-rotated. You must rotate yourself if you want that.
	 * Note that air blocks and blocks outside the template are null in the arrays.
	 * @return
	 */
	public BlueprintBlock[][][] getPreview();
	
	public void scanBlocks(IBlueprintScanner scanner);

	public BlockPos getDimensions();
	
	/**
	 * Returns total space dimensions of the blueprint, if it were rotated to the desired facing.
	 * @param facing The desired facing for the entry way, if there is one.
	 * @return
	 */
	public default BlockPos getAdjustedDimensions(Direction facing) {
		final BlockPos dimensions = this.getDimensions();
		final DungeonExitPoint entry = this.getEntry();
		
		Direction mod = GetModDir(entry == null ? Direction.NORTH : entry.getFacing(), facing);
		int width = dimensions.getX();
		int height = dimensions.getY();
		int length = dimensions.getZ();
		
		switch (mod) {
		case DOWN:
		case UP:
		case NORTH:
			break;
		case EAST:
			// Single rotation: (-z, x)
			width = -dimensions.getZ();
			length = dimensions.getX();
			break;
		case SOUTH:
			// Double rotation: (-x, -z)
			width = -dimensions.getX();
			length = -dimensions.getZ();
			break;
		case WEST:
			// Triple: (z, -x)
			width = dimensions.getZ();
			length = -dimensions.getX();
			break;
		}
		
		return new BlockPos(width, height, length);
	}
	
	/**
	 * Returns entry point offsets when blueprint is rotated to the desired facing.
	 * @param facing
	 * @return
	 */
	public default BlockPos getAdjustedOffset(Direction facing) {
		final DungeonExitPoint entry = this.getEntry();
		BlockPos offset = entry == null ? new BlockPos(0,0,0) : entry.getPos().toImmutable();
		Direction mod = IBlueprint.GetModDir(entry == null ? Direction.NORTH : entry.getFacing(), facing);
		
		int x = offset.getX();
		int y = offset.getY();
		int z = offset.getZ();
		
		switch (mod) {
		case DOWN:
		case UP:
		case NORTH:
			break;
		case EAST:
			// Single rotation: (-z, x)
			x = -offset.getZ();
			z = offset.getX();
			break;
		case SOUTH:
			// Double rotation: (-x, -z)
			x = -offset.getX();
			z = -offset.getZ();
			break;
		case WEST:
			// Triple: (z, -x)
			x = offset.getZ();
			z = -offset.getX();
			break;
		}
		
		return new BlockPos(x, y, z);
	}
	
	static BlockPos ApplyRotation(BlockPos input, Direction modDir) {
		int x = 0;
		int z = 0;
		final int dx = input.getX();
		final int dz = input.getZ();
		switch (modDir) {
		case DOWN:
		case UP:
		case NORTH:
			 x = dx;
			 z = dz;
			break;
		case EAST:
			// Single rotation: (-z, x)
			x = -dz;
			z = dx;
			break;
		case SOUTH:
			// Double rotation: (-x, -z)
			x = -dx;
			z = -dz;
			break;
		case WEST:
			// Triple: (z, -x)
			x = dz;
			z = -dx;
			break;
		}
		return new BlockPos(x, input.getY(), z);
	}

	public static Direction GetModDir(Direction original, Direction newFacing) {
		int unused; // Isn't there another version of this in utils?
		Direction out = Direction.NORTH;
		int rotCount = (4 + newFacing.getHorizontalIndex() - original.getHorizontalIndex()) % 4;
		while (rotCount-- > 0) {
			out = out.rotateY();
		}
		return out;
	}
	
}
