package com.smanzana.nostrummagica.entity.boss.shadowdragon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class ShadowDragonAttackPattern {

	protected final ShadowDragonArena arena;
	
	protected int targetCount;
	
	protected ShadowDragonAttackPattern(ShadowDragonArena arena) {
		this.arena = arena;
	}
	
	/**
	 * Get the next location to attack. Returns null once the attack pattern is complete.
	 * The caller gets to determine frequency of calling.
	 * @return
	 */
	public abstract @Nullable Vec3 getNextTarget();
	
	/**
	 * Get the total number of targets this pattern will be able to generate
	 * @return
	 */
	public abstract int getTargetCount();
	
	/**
	 * Get the recommended number of targets to execute at once, for a given difficulty measure.
	 * I'm writing this to roughly target every 10% of health. So for example, a pattern that emits
	 * row positions might give a whole row per slice.
	 * @return
	 */
	public abstract int getRecommendedCountPerDifficulty();
	
	public abstract boolean supportsMultiBatching();
	
	/**
	 * Walks the arena in a grid, visitting all of one axis (x) before starting the next
	 */
	public static class FloorWave extends ShadowDragonAttackPattern {

		// How many targets per 'row' that the arena is split into.
		// There will be this squared number of targets.
		protected final int subdivisions;
		
		protected final double xPer;
		protected final double zPer;
		protected final Vec3 minPos;
		
		public FloorWave(ShadowDragonArena arena) {
			super(arena);
			
			final double cellsPer = 5.0; // once per 3 blocks
			AABB bounds = arena.getBounds(); 
			this.subdivisions = (int) (Math.max(bounds.getXsize(), bounds.getZsize()) / cellsPer) - 1; // -1 because we skip the "0" one
			this.xPer = bounds.getXsize() / (subdivisions + 1); // ideally goes back to cellsPer, but will be smaller if one is larger or based on int cast
			this.zPer = bounds.getZsize() / (subdivisions + 1);
			minPos = new Vec3(bounds.minX, bounds.minY, bounds.minZ);
		}

		@Override
		public Vec3 getNextTarget() {
			if (targetCount == this.getTargetCount()) {
				return null;
			}
			
			final int gridIdx = getGridIdx(targetCount++);
			
			final double x = ((gridIdx % subdivisions) + 1) * xPer;
			final double z = ((gridIdx / subdivisions) + 1) * zPer;
			final Vec3 rawPos = minPos.add(x, 0, z);
			
			return getSurfacePosition(rawPos);
		}
		
		protected int getGridIdx(int targetIdx) {
			return targetIdx;
		}

		@Override
		public int getTargetCount() {
			return subdivisions * subdivisions;
		}
		
		public int getRowCount() {
			return subdivisions;
		}
		
		protected Vec3 getSurfacePosition(Vec3 pos) {
			// reach in to arena to get world...
			Level level = arena.level;
			MutableBlockPos cursor = new MutableBlockPos();
			cursor.set(pos.x, pos.y, pos.z);
			
			while (!level.isEmptyBlock(cursor)) {
				cursor.move(Direction.UP);
			}
			
			return new Vec3(pos.x, cursor.getY(), pos.z);
		}

		@Override
		public int getRecommendedCountPerDifficulty() {
			return subdivisions;
		}

		@Override
		public boolean supportsMultiBatching() {
			return true;
		}
		
	}
	
	/**
	 * Generates in large square chunks that break up the floor into 9ths.
	 * Returns chunks in either a sequential order, or random order
	 */
	public static class FloorChunks extends FloorWave {
		
		protected final int[] chunkOrder; // Should contain 0-8
		
		final double idxPerChunk;
		final int chunkLen;

		protected FloorChunks(ShadowDragonArena arena, int[] chunkOrder) {
			super(arena);
			this.chunkOrder = chunkOrder;
			
			idxPerChunk = (double) subdivisions / 3.0;
			chunkLen = (int) idxPerChunk + 1;
		}
		
		public static final FloorChunks Sequential(ShadowDragonArena arena) {
			return new FloorChunks(arena, new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8});
		}
		
		public static final FloorChunks Random(ShadowDragonArena arena) {
			List<Integer> chunks = new ArrayList<>(9);
			for (int i = 0; i < 9; i++) {
				chunks.add(i);
			}
			Collections.shuffle(chunks);
			return new FloorChunks(arena, chunks.stream().mapToInt(i -> i).toArray());
		}

		@Override
		public int getRecommendedCountPerDifficulty() {
			return (chunkLen * chunkLen);
		}
		
		@Override
		protected int getGridIdx(int targetIdx) {
			// First, figure out which chunk we'll be in. Mod off the sub-chunk idx
			final int chunkRawIdx = targetIdx / (chunkLen * chunkLen);
			final int chunkIdx = chunkOrder[chunkRawIdx];
			
			final int subIdx = targetIdx % (chunkLen * chunkLen);
			
			return getBigGridIdxFromMini(subIdx % chunkLen, subIdx / chunkLen, chunkIdx);
		}
		
		protected int getBigGridIdxFromMini(int miniX, int miniZ, int chunkIdx) {
			// 9 chunks from lowx/lowz to highx/lowz all the way to highx/highz
			final int chunkX = chunkIdx % 3;
			final int chunkZ = chunkIdx / 3;
			
			final int bigX = miniX + (chunkX * chunkLen);
			final int bigZ = miniZ + (chunkZ * chunkLen);
			
			// Return the opposite of mod/div
			return (bigZ * subdivisions) + bigX;
		}
		
	}
	
	public static class FloorRings extends FloorWave {

		public FloorRings(ShadowDragonArena arena) {
			super(arena);
		}
		
		@Override
		protected int getGridIdx(int targetIdx) {
			final int[] storage = {targetIdx};
			final int ringIdx = getRingIdx(storage);
			final int ringSideLen = this.getRingSideLen(ringIdx);
			final int ringTotal = this.getCountInRing(ringIdx);
			
			int subIdx = storage[0];
			int x = 0;
			int z = 0;
			if (subIdx < ringSideLen) {
				x = subIdx;
				z = 0;
			} else if (subIdx > (ringTotal - ringSideLen)) {
				z = ringSideLen - 1;
				x = (subIdx - ringTotal) + ringSideLen;
			} else {
				subIdx -= ringSideLen;
				z = 1;
				while (subIdx > 0) { subIdx -= 2; z++; }
				x = (subIdx == 0 ? 0 : ringSideLen - 1);
			}
			
			return getGridIdx(x + ringIdx, z + ringIdx);
		}
		
		protected int getGridIdx(int x, int z) {
			// Return the opposite of mod/div
			return (z * subdivisions) + x;
		}
		
		protected int getRingIdx(int idx) {
			return getRingIdx(new int[] {idx});
		}
		
		protected int getRingIdx(int[] idx) {
			int len = this.subdivisions;
			int count = 0;
			while (len > 0) {
				final int ringTotal = getCountInRingLen(len);
				if (ringTotal > idx[0]) {
					break;
				}
				idx[0] -= ringTotal;
				len -= 2;
				count++;
			}
			
			return count;
		}
		
		protected int getCountInRing(int ringIdx) {
			return getCountInRingLen(getRingSideLen(ringIdx));
		}
		
		protected int getCountInRingLen(int len) {
			return (4 * len) - 4;
		}
		
		protected int getRingSideLen(int ringIdx) {
			return this.subdivisions - (ringIdx * 2);
		}
		
		@Override
		public int getRecommendedCountPerDifficulty() {
			final int ringCount = getRingIdx(this.targetCount);
			return this.getCountInRing(ringCount);
		}

		@Override
		public boolean supportsMultiBatching() {
			return false;
		}
		
	}
	
}
