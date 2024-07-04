package com.smanzana.nostrummagica.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.server.ServerWorld;

public class WorldUtil {

	public static interface IWorldScanner {
		/**
		 * Scan over the provided block. Return whether or not to continue scanning. (False aborts scan)
		 * @param access
		 * @param pos
		 * @return
		 */
		public boolean scan(IBlockReader access, BlockPos pos);
	}
	
	/**
	 * Scans a set of blocks, calling the scanning function on each.
	 * Does work to scan whole chunks at a time before moving to other chunks to aid in performance.
	 * Automatically skips chunks that aren't loaded even if they're in the scan area
	 * @param min
	 * @param max
	 * @param scanner
	 * @return number of positions scanned
	 */
	public static final int ScanBlocks(World world, BlockPos pos1, BlockPos pos2, IWorldScanner scanner) {
		int count = 0;
		
		final BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()),
				Math.min(pos1.getY(), pos2.getY()),
				Math.min(pos1.getZ(), pos2.getZ()));
		final BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()),
				Math.max(pos1.getY(), pos2.getY()),
				Math.max(pos1.getZ(), pos2.getZ()));

		final int beginChunkPosX = min.getX() & (0xFFFFFFF0); // real world block count
		final int beginChunkX = beginChunkPosX >> 4; // chunk index, not real world position
		final int beginChunkPosZ = min.getZ() & (0xFFFFFFF0);
		final int beginChunkZ = beginChunkPosZ >> 4;
		final int endChunkPosX = max.getX() & (0xFFFFFFF0);
		final int endChunkX = endChunkPosX >> 4;
		final int endChunkPosZ = max.getZ() & (0xFFFFFFF0);
		final int endChunkZ = endChunkPosZ >> 4;
		final int beginChunkOffsetX = min.getX() - beginChunkPosX; // how many over the real first chunk. May be 0.
		final int beginChunkOffsetZ = min.getZ() - beginChunkPosZ;
		final int endChunkOffsetX = max.getX() - endChunkPosX; // How many over the real last chunk offset. May be 0.
		final int endChunkOffsetZ = max.getZ() - endChunkPosZ;
		
		// we have some blocks in beginChunkX (and Z).
		// We may have some blocks in endChunkX+1 (and Z)
		// beginChunkX may start with a portion of blocks known as beginChunkOffsetX
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		for (int chunkX = beginChunkX; chunkX <= endChunkX; chunkX++)
		for (int chunkZ = beginChunkZ; chunkZ <= endChunkZ; chunkZ++) {
			final int baseX = (chunkX << 4);
			final int baseZ = (chunkZ << 4);

			// Test that chunk is loaded
			cursor.setPos(baseX, 1, baseZ);
			if (!NostrumMagica.isBlockLoaded(world, cursor)) {
				continue;
			}
			
			// Iterate over blocks in chunk
			// Note chunkX == beginChunkX and chunkX == endChunkX have special bounds (same with Z)
			final int startX = (chunkX == beginChunkX ? beginChunkOffsetX : 0);
			final int startZ = (chunkZ == beginChunkZ ? beginChunkOffsetZ : 0);
			final int endX = (chunkX == endChunkX ? endChunkOffsetX : 16);
			final int endZ = (chunkZ == endChunkZ ? endChunkOffsetZ : 16);
			for (int iterX = startX; iterX < endX; iterX++)
			for (int iterZ = startZ; iterZ < endZ; iterZ++)
			for (int iterY = min.getY(); iterY <= max.getY(); iterY++) {
				final int x = (baseX + iterX);
				final int z = (baseZ + iterZ);
				final int y = iterY;
				
				if (y < 0 || y > world.getHeight()) {
					continue;
				}
				
				count++;
				cursor.setPos(x, y, z);
				if (!scanner.scan(world, cursor)) {
					return count;
				}
			}
		}
		
		return count;
	}
	
	public static interface IBlockWalker {
		/**
		 * Check whether the provided block/pos COULD be visitted. It may or may not be walked to depending on iteration order.
		 * A typical check here is that the block state is related to the one that started the walk.
		 * @param world
		 * @param startPos
		 * @param startState
		 * @param pos
		 * @param state
		 * @param distance
		 * @return
		 */
		public boolean canVisit(IBlockReader world, BlockPos startPos, BlockState startState, BlockPos pos, BlockState state, int distance);
		
		/**
		 * Actually visit a block/pos.
		 * Return whether to STOP walking. False continues walking.
		 * @param world
		 * @param startPos
		 * @param startState
		 * @param pos
		 * @param state
		 * @param distance
		 * @param walkCount
		 * @return
		 */
		public boolean walk(IBlockReader world, BlockPos startPos, BlockState startState, BlockPos pos, BlockState state, int distance, int walkCount);
	}
	
	/**
	 * Iterate over all blocks connected to the provided one, as per the passed in walker.
	 * @param world
	 * @param start
	 * @param walker
	 * @param maxIterations
	 * @return The final pos iterated over.
	 */
	public static BlockPos WalkConnectedBlocks(IBlockReader world, BlockPos start, IBlockWalker walker, int maxIterations) {
		final BlockState startState = world.getBlockState(start);
		final Set<BlockPos> visited = new HashSet<>();
		final List<BlockPos> next = new LinkedList<>(); // Doing breadthfirst/queue, so linked
		int walkCount = 0;
		
		final Consumer<BlockPos> checkAndAdd = (pos) -> {
			if (!visited.contains(pos) && walker.canVisit(world, start, startState, pos, world.getBlockState(pos), getBlockDistance(start, pos))) {
				next.add(pos);
			}
			visited.add(pos);
		};
		
		next.add(start);
		visited.add(start);
		
		BlockPos last = null;
		while (!next.isEmpty()) {
			last = next.remove(0);
			
			BlockState state = world.getBlockState(last);
			
			final int dist = getBlockDistance(start, last);
			if (walker.walk(world, start, startState, last, state, dist, walkCount++)) {
				break;
			}
			if (walkCount >= maxIterations) {
				break;
			}
			
			checkAndAdd.accept(last.up());
			checkAndAdd.accept(last.down());
			checkAndAdd.accept(last.north());
			checkAndAdd.accept(last.east());
			checkAndAdd.accept(last.south());
			checkAndAdd.accept(last.west());
		}
		
		return last;
	}
	
	/**
	 * Good old-fashioned manhattan distance
	 * @return
	 */
	public static final int getBlockDistance(Vector3i pos1, Vector3i pos2) {
		return Math.abs(pos1.getX() - pos2.getX())
				+ Math.abs(pos1.getY() - pos2.getY())
				+ Math.abs(pos1.getZ() - pos2.getZ());
	}
	
	private static final int NUM_X_BITS = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
	private static final int NUM_Z_BITS = NUM_X_BITS;
	private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
	private static final int Y_SHIFT = 0 + NUM_Z_BITS;
	private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
	public static final BlockPos blockPosFromLong1_12_2(long serialized) {
		int i = (int)(serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
		int j = (int)(serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
		int k = (int)(serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
		return new BlockPos(i, j, k);
	}
	
	/**
	 * Check if this block is one we know has state that should be re-evaluated after it's placed as part
	 * of worldgen. For example, iron bars and fences so they can connect to what generates around them.
	 * @param block
	 * @return
	 */
	public static final boolean blockNeedsGenFixup(@Nullable Block block) {
		// Like StructurePiece's "BLOCKS_NEEDING_POSTPROCESSING" array; some blocks need their
		// states fixed up after placing. Things like fences and walls that extend and what not.
		if (block != null) {
			if (VANILLA_BLOCKS_NEEDING_POSTPROCESSING.contains(block)) {
				return true;
			}
			
			// Some main cases I know of
			if (block instanceof PaneBlock) {
				return true;
			}
			
			if (block instanceof StairsBlock) {
				return true;
			}
		}
		
		return false;
	}
	
	public static final boolean blockNeedsGenFixup(@Nullable BlockState state) {
		if (state != null) {
			return blockNeedsGenFixup(state.getBlock());
		} 
		return false;
	}
	
	// Copied from StructurePiece
	private static final Set<Block> VANILLA_BLOCKS_NEEDING_POSTPROCESSING = ImmutableSet.<Block>builder().add(Blocks.NETHER_BRICK_FENCE).add(Blocks.TORCH).add(Blocks.WALL_TORCH).add(Blocks.OAK_FENCE).add(Blocks.SPRUCE_FENCE).add(Blocks.DARK_OAK_FENCE).add(Blocks.ACACIA_FENCE).add(Blocks.BIRCH_FENCE).add(Blocks.JUNGLE_FENCE).add(Blocks.LADDER).add(Blocks.IRON_BARS).build();
	
	/**
	 * Check whether the given position is within a structure of the provided type.
	 * @param world
	 * @param pos
	 * @param structure
	 * @param insideCheck if false, only check the outer bounds of a structure rather than the individual structure piece bounds.
	 * 					 AKA check one giant bounding box for the whole structure instead of checking if the position is actually in a structure piece.
	 * @return
	 */
	public static final boolean IsInStructure(ServerWorld world, BlockPos pos, Structure<?> structure, boolean insideCheck) {
		return GetContainingStructure(world, pos, structure, insideCheck).isValid();
	}
	
	/**
	 * Get the first matching structure start (instance) for the specified type and position.
	 * @param world
	 * @param pos
	 * @param structure
	 * @param insideCheck if false, only check the outer bounds of a structure rather than the individual structure piece bounds.
	 * 					 AKA check one giant bounding box for the whole structure instead of checking if the position is actually in a structure piece.
	 * @return
	 */
	public static final @Nonnull StructureStart<?> GetContainingStructure(ServerWorld world, BlockPos pos, Structure<?> structure, boolean insideCheck) {
		return world.func_241112_a_().getStructureStart(pos, insideCheck, structure);
	}
	
	public static final @Nullable StructurePiece GetContainingStructurePiece(ServerWorld world, BlockPos pos, Structure<?> structure, boolean insideCheck) {
		return world.func_241112_a_().func_235011_a_(SectionPos.from(pos), structure).filter((start) -> {
	         return start.getBoundingBox().isVecInside(pos);
	      }).flatMap(start -> start.getComponents().stream()).filter(piece -> piece.getBoundingBox().isVecInside(pos))
				.findFirst().orElse(null);
	}
}
