package com.smanzana.nostrummagica.utils;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WorldUtil {

	public static interface IWorldScanner {
		/**
		 * Scan over the provided block. Return whether or not to continue scanning. (False aborts scan)
		 * @param access
		 * @param pos
		 * @return
		 */
		public boolean scan(IBlockAccess access, BlockPos pos);
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
		MutableBlockPos cursor = new MutableBlockPos();
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
				
				
				if (y < 0 || y > world.provider.getHeight()) {
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
	
	/**
	 * Good old-fashioned manhattan distance
	 * @return
	 */
	public static final int getBlockDistance(Vec3i pos1, Vec3i pos2) {
		return Math.abs(pos1.getX() - pos2.getX())
				+ Math.abs(pos1.getY() - pos2.getY())
				+ Math.abs(pos1.getZ() - pos2.getZ());
	}
}