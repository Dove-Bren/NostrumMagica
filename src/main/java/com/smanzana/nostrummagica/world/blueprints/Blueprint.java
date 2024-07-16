package com.smanzana.nostrummagica.world.blueprints;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.IOrientedTileEntity;
import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Contains all the data needed to spawn a blueprint in the world
 * @author Skyler
 *
 */
public class Blueprint implements IBlueprint {
	
	public static final class LoadContext {
		public final String source;
		public String name;
		
		public LoadContext(String source) {
			this.source = source;
		}
		
		public LoadContext(String source, String name) {
			this(source);
			this.name = name;
		}
	}
	
	protected static class CaptureContext {
		public final Blueprint blueprint;
		public final IWorld world;
		public final BlockPos pos1;
		public final BlockPos pos2;
		public final @Nullable BlueprintLocation origin;
		
		public CaptureContext(Blueprint blueprint, IWorld world, BlockPos pos1, BlockPos pos2, BlueprintLocation origin) {
			this.blueprint = blueprint;
			this.world = world;
			this.pos1 = pos1;
			this.pos2 = pos2;
			this.origin = origin;
		}
	}
	
	private static final String NBT_DIMS = "dimensions";
	private static final String NBT_WHOLE_DIMS = "master_dimensions";
	private static final String NBT_BLOCK_LIST = "blocks";
	private static final String NBT_ENTRY = "entry";
	private static final String NBT_VERSION = "version";
	private static final String NBT_PIECE_OFFSET = "part_offset";
	//private static final String NBT_PIECE_COMPOSITE = "composite_marker";
	private static final int MAX_BLUEPRINT_BLOCKS = 32 * 32 * 32;
	
	private BlockPos dimensions;
	private BlueprintBlock[] blocks;
	protected BlueprintLocation entry;
	private int partOffset; // Only used for fragments
	
	// Cached sublist of blocks for previews (5x2x5)
	private BlueprintBlock[][][] previewBlocks = new BlueprintBlock[5][2][5];
	
	
	public Blueprint(BlockPos dimensions, BlueprintBlock[] blocks, BlueprintLocation entry) {
		if (dimensions != null && blocks != null
				&& (dimensions.getX() * dimensions.getY() * dimensions.getZ() != blocks.length)) {
			throw new RuntimeException("Dimensions do not match block array provided to blueprint constructor!");
		}
		this.dimensions = dimensions;
		this.blocks = blocks;
		this.entry = entry;

		refreshPreview();
	}
	
	public static Blueprint Capture(IWorld world, BlockPos pos1, BlockPos pos2, @Nullable BlueprintLocation origin) {
		Blueprint blueprint = new Blueprint(null, null, null);
		blueprint.capture(world, pos1, pos2, origin);
		return blueprint;
	}
	
	protected CaptureContext makeCaptureContext(IWorld world, BlockPos pos1, BlockPos pos2, @Nullable BlueprintLocation origin) {
		return new CaptureContext(this, world, pos1, pos2, origin);
	}
	
	protected BlueprintBlock captureBlock(CaptureContext context, IWorld world, BlockPos pos) {
		return new BlueprintBlock(world, pos);
	}
	
	protected void finishCapture(CaptureContext context) {
		;
	}
	
	/**
	 * Scans the world between two block positions and captures all blocks, blockstates, and tile entities
	 * to save as a blueprint.
	 * @param world
	 * @param pos1
	 * @param pos2
	 */
	protected void capture(IWorld world, BlockPos pos1, BlockPos pos2, @Nullable BlueprintLocation origin) {
		// fix positions
		BlockPos low = new BlockPos(pos1.getX() < pos2.getX() ? pos1.getX() : pos2.getX(),
				pos1.getY() < pos2.getY() ? pos1.getY() : pos2.getY(),
				pos1.getZ() < pos2.getZ() ? pos1.getZ() : pos2.getZ());
		if (!low.equals(pos1)) {
			BlockPos high = new BlockPos(pos1.getX() > pos2.getX() ? pos1.getX() : pos2.getX(),
					pos1.getY() > pos2.getY() ? pos1.getY() : pos2.getY(),
					pos1.getZ() > pos2.getZ() ? pos1.getZ() : pos2.getZ());
			pos1 = low;
			pos2 = high;
		}

		final CaptureContext context = makeCaptureContext(world, pos1, pos2, origin);
		
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		this.dimensions = new BlockPos(
				1 + (pos2.getX() - pos1.getX()),
				1 + (pos2.getY() - pos1.getY()),
				1 + (pos2.getZ() - pos1.getZ()));
		final int width = dimensions.getX();
		final int height = dimensions.getY();
		final int length = dimensions.getZ();
		this.blocks = new BlueprintBlock[width * height * length];
		
		int slot = 0;
		for (int i = 0; i < width; i++)
		for (int j = 0; j < height; j++)
		for (int k = 0; k < length; k++) {
			cursor.setPos(pos1.getX() + i, pos1.getY() + j, pos1.getZ() + k);
			
			BlueprintBlock block = captureBlock(context, world, cursor);
			blocks[slot++] = block;
		}
		
		if (origin != null) {
			this.entry = origin;
		} else {
			this.entry = new BlueprintLocation(new BlockPos(width / 2, 0, length / 2), Direction.NORTH);
		}
		
		finishCapture(context);
		
		refreshPreview();
	}
	
	protected void refreshPreview() {
		refreshPreview(5, 2, 5);
	}
	
	protected void refreshPreview(int width, int height, int depth) {
		// Get preview based on 'entry' origin point and blocks around it
		if (dimensions != null) {
			BlockPos offset = (entry == null ? BlockPos.ZERO : entry.getPos());
			for (int xOff = -(width/2); xOff <= (width-1)/2; xOff++)
			for (int yOff = -1; yOff < height-1; yOff++)
			for (int zOff = -(depth/2); zOff <= (depth-1)/2; zOff++) {
				final int x = xOff + offset.getX();
				final int y = yOff + offset.getY();
				final int z = zOff + offset.getZ();
				
				if (x < 0 || y < 0 || z < 0
					|| x >= dimensions.getX()
					|| y >= dimensions.getY()
					|| z >= dimensions.getZ()) {
					previewBlocks[xOff + (width/2)][yOff+1][zOff + (depth/2)] = null;
					continue;
				}
				
				final int bIndex = (x * dimensions.getY() * dimensions.getZ())
						+ (y * dimensions.getZ())
						+ z;
				if (bIndex < 0 || bIndex >= blocks.length) {
					previewBlocks[xOff + (width/2)][yOff+1][zOff + (depth/2)] = null;
				} else {
					previewBlocks[xOff + (width/2)][yOff+1][zOff + (depth/2)] =
							blocks[bIndex];
				}
			}
		}
	}
	
	private static final boolean isSecondPassBlock(BlueprintBlock block) {
		if (block.getState() != null) {
			//if (!(block.getState().isFullBlock() || block.getState().isOpaqueCube() || block.getState().isNormalCube())) {
			if (!block.getState().isSolid()) {
				return true;
			}
		}
		
		return false;
	}
	
	protected void fixupTileEntity(TileEntity te, Direction direction, BlueprintSpawnContext context) {
		if (te instanceof IOrientedTileEntity) {
			// Let tile ent respond to rotation
			((IOrientedTileEntity) te).setSpawnedFromRotation(direction, context.isWorldGen);
		}
	}
	
	protected void placeBlock(BlueprintSpawnContext context, BlockPos at, Direction direction, BlueprintBlock block) {
		BlockState placeState;
		TileEntity te = null;
		
		if (context.placer == null || !context.placer.spawnBlock(context, at, direction, block)) {
			placeState = block.getSpawnState(direction);
			if (placeState != null) {
				// TODO: add fluid state support
				context.world.setBlockState(at, placeState, 2);
				if (WorldUtil.blockNeedsGenFixup(block.getState())) {
					if (context.isWorldGen) {
						context.world.getChunk(at).markBlockForPostprocessing(at);
					} else {
						BlockState blockstate = context.world.getBlockState(at);
						BlockState blockstate1 = Block.getValidBlockForPosition(blockstate, context.world, at);
						context.world.setBlockState(at, blockstate1, 20);
					}
				}
				
				CompoundNBT tileEntityData = block.getTileEntityData();
				if (tileEntityData != null) {
					te = TileEntity.readTileEntity(placeState, tileEntityData.copy());
					
					if (te != null) {
						if (context.isWorldGen || !(context.world instanceof IServerWorld)) {
							context.world.getChunk(at).addTileEntity(at, te);
						} else {
							((IServerWorld) context.world).getWorld().setTileEntity(at, te);
						}
						this.fixupTileEntity(te, direction, context);
					} else {
						NostrumMagica.logger.error("Could not deserialize TileEntity with id \"" + tileEntityData.getString("id") + "\"");
					}
				}
			} else {
				//world.removeBlock(at, false);
				placeState = Blocks.AIR.getDefaultState();
				context.world.setBlockState(at, placeState, 2);
			}
		} else {
			// placer handled placement. Do slower lookup of what happened
			placeState = context.world.getBlockState(at);
			te = context.world.getTileEntity(at);
		}
		
		if (context.placer != null) {
			context.placer.finalizeBlock(context, at, placeState, te, direction, block);
		}
	}
	
	protected BlueprintSpawnContext makeContext(IWorld world, BlockPos at, Direction direction, boolean isWorldGen, MutableBoundingBox bounds, IBlueprintBlockPlacer placer) {
		return new BlueprintSpawnContext(world, at, direction, isWorldGen, bounds, placer);
	}
	
	@Override
	public void spawn(IWorld world, BlockPos at, Direction direction, @Nullable MutableBoundingBox bounds, @Nullable IBlueprintBlockPlacer placer) {
		final boolean worldGen = WorldUtil.IsWorldGen(world);
		BlueprintSpawnContext context = makeContext(world, at, direction, worldGen, bounds, placer);
		spawnWithContext(context);
	}
	
	protected void spawnWithContext(BlueprintSpawnContext context) {
		
		final int width = dimensions.getX();
		final int height = dimensions.getY();
		final int length = dimensions.getZ();
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		Direction modDir = Direction.NORTH; // 0
		
		// Apply rotation changes
		if (this.entry != null) {
			// Get facing mod
			modDir = IBlueprint.GetModDir(entry.getFacing(), context.direction);
		}
		
		BlockPos adjustedDims;
		{
			BlockPos wrapper = new BlockPos(width, 0, length);
			adjustedDims = IBlueprint.ApplyRotation(wrapper, modDir);
			
			// Note: Some vals may be negative. We leave them for now to get proper offset adjustement, and then
			// straighten them out for later calcs
		}
		
		// Outside loop for each chunk
		// Inner loop loops i, j, k to 0 to < 16 in each
		// Loops basically always go from 0 to 16 except in edge ones I suppose
		BlockPos offset = entry == null ? new BlockPos(0,0,0) : entry.getPos();
		BlockPos unit = IBlueprint.ApplyRotation(new BlockPos(1, 0, 1), modDir);
		
		// To get actual least-x leastz coordinates, we need to add offset rotated to proper orientation
		BlockPos origin;
		{
			BlockPos.Mutable rotOffset = new BlockPos.Mutable().setPos(IBlueprint.ApplyRotation(offset, modDir));
			//BlockPos rotOffset = applyRotation(offset, modDir.getHorizontalIndex() % 2 == 1 ? modDir.getOpposite() : modDir);
			
			// To get proper offset, need to move so that our old origin (0,0) is at the real one, which is encoded in our adjusted dimensions
			// Negative values there mean we need to shift our offset
			if (unit.getX() < 0 || unit.getZ() < 0) {
				int px = Math.abs(adjustedDims.getX());
				int pz = Math.abs(adjustedDims.getZ());
				
				if (adjustedDims.getX() < 0) {
					rotOffset.setPos(rotOffset.getX() + (px - 1), rotOffset.getY(), rotOffset.getZ());
				}
				
				if (adjustedDims.getZ() < 0) {
					rotOffset.setPos(rotOffset.getX(), rotOffset.getY(), rotOffset.getZ() + (pz - 1));
				}
				
				adjustedDims = new BlockPos(px, adjustedDims.getY(), pz);
			}
			
			origin = context.at.toImmutable().subtract(rotOffset);
		}
		final int chunkStartX = origin.getX() >> 4;
		final int chunkStartZ = origin.getZ() >> 4;
		final int chunkRemX = ((origin.getX() % 16) + 16) % 16;
		final int chunkRemZ = ((origin.getZ() % 16) + 16) % 16;
		
		// To get number of chunks in either direction we're going to go, we have to apply rotation to width and height
		// Note: add 'how far in the chunk we start' to width and height to find actual number of chunks affected
		final int numChunkX = (adjustedDims.getX() + chunkRemX) / 16;
		final int numChunkZ = (adjustedDims.getZ() + chunkRemZ) / 16;
		
		// Have a list for second-pass block placement
		List<BlueprintBlock> secondPassBlocks = new ArrayList<>(16 * 4);
		List<BlockPos> secondPassPos = new ArrayList<>(16 * 4);
		
		// Data has inverse rotation
		final Direction dataDir = modDir.getHorizontalIndex() % 2 == 1 ? modDir.getOpposite() : modDir;
		
		// Loop over all chunks from <x to >x (and <z to >z)
		for (int cx = 0; cx <= numChunkX; cx++)
		for (int cz = 0; cz <= numChunkZ; cz++) {
			final int chunkOffsetX = (cx + chunkStartX) * 16;
			final int chunkOffsetZ = (cz + chunkStartZ) * 16;
			final int startX = (cx == 0 ? chunkRemX : 0);
			final int startZ = (cz == 0 ? chunkRemZ : 0);
			final int endX = (cx == numChunkX ? (((origin.getX() + adjustedDims.getX()) % 16) + 16) % 16 : 16);
			final int endZ = (cz == numChunkZ ? (((origin.getZ() + adjustedDims.getZ()) % 16) + 16) % 16 : 16);
			
			
			
			// For each chunk, loop over x, y, z and spawn templated blocks
			// On low boundary chunks, start i and k and chunkRemX/Z
			// For high boundary chunks, cap i and k and (width or length / 16)
			secondPassBlocks.clear();
			secondPassPos.clear();
			for (int i = startX; i < endX; i++)
			for (int j = 0; j < height; j++)
			for (int k = startZ; k < endZ; k++) {
				// IWorld pos is simply chunk position + inner loop offset
				// (j is data y, so offset to world y)
				cursor.setPos(chunkOffsetX + i, j + origin.getY(), chunkOffsetZ + k);
				
				if (context.bounds != null && !context.bounds.isVecInside(cursor)) {
					continue;
				}
				
				// Find data position by applying rotation to transform x and z coords into u and v data coords
				unit = IBlueprint.ApplyRotation(new BlockPos(1, 0, 1), dataDir);
				BlockPos dataPos = IBlueprint.ApplyRotation(cursor.toImmutable().subtract(origin), dataDir);
				
				// Negative values here, though, imply reflection. So make "-x" be "max - x"
				if (unit.getX() < 0 || unit.getZ() < 0) {
					// If negative, shift by dimension size (-1 cause 0 offset)
					int px = dataPos.getX();
					int pz = dataPos.getZ();
					
					// if x < 0, make into width-x
					if (unit.getX() < 0) {
						px = width + (px - 1);
					}
					
					// ...
					if (unit.getZ() < 0) {
						pz = length + (pz - 1);
					}
					
					dataPos = new BlockPos(px, dataPos.getY(), pz);
				}
				
				BlueprintBlock block = blocks[
   				       (dataPos.getX() * length * height)
   				       + (j * length)
   				       + dataPos.getZ()
   				       ];
				
				// Either set the block now, or set it up to be placed later (if it might break based on order)
				if (isSecondPassBlock(block)) {
					secondPassBlocks.add(block);
					secondPassPos.add(new BlockPos(cursor));
				} else {
					placeBlock(context, cursor, modDir, block);
				}
			}
			
			// Spawn all second-pass blocks
			for (int i = 0; i < secondPassBlocks.size(); i++) {
				BlockPos secondPos = secondPassPos.get(i);
				BlueprintBlock block = secondPassBlocks.get(i);
				placeBlock(context, secondPos, modDir, block);
			}
			
			context.world.getChunk(cursor).setModified(true);
		}
	}
	
	@Override
	public BlockPos getDimensions() {
		return this.dimensions;
	}
	
	@Override
	public BlueprintLocation getEntry() {
		return this.entry;
	}
	
	/**
	 * Returns a preview of the blueprint centered around the blueprint entry point.
	 * Note that the preview is un-rotated. You must rotate yourself if you want that.
	 * Also note that this is a 5x2x5 preview.
	 * And again, Note that air blocks and blocks outside the template are null in the arrays.
	 * @return
	 */
	@Override
	public BlueprintBlock[][][] getPreview() {
		return this.previewBlocks;
	}

	/**
	 * Adds one blueprint to the other. Returns the original blueprint modified to include blocks from the other.
	 * This SHOULD be compatible with completely different blueprints.
	 * For now, all I'm implementing is piecing blueprints broken apart with {@link #toNBTWithBreakdown()}, where
	 * all blueprints passed in should be within the dimensions of the original.
	 * @param blueprint
	 * @return
	 */
	public Blueprint join(Blueprint blueprint) {
		// TODO expand this to accept different ones!
		// That would include taking the non-block stuff like doors, keys, and doors!
		
		final long start = System.currentTimeMillis();
		
		if (blueprint.dimensions.getZ() != dimensions.getZ()
				|| blueprint.dimensions.getY() != dimensions.getY()) {
			throw new RuntimeException("Can't combine blueprints that don't have the same base size!");
		}
		
		if (!blueprint.entry.equals(entry)) {
			throw new RuntimeException("Arbitrary blueprint joining not implemented!");
		}
		
		if (blueprint.partOffset == 0) {
			throw new RuntimeException("Can only join sub-blueprints");
		}
		
		final int base = dimensions.getY() * dimensions.getZ();
		final int count = blueprint.dimensions.getX() * base;
		final int offset = blueprint.partOffset * base;
		System.arraycopy(blueprint.blocks, 0, blocks, offset, Math.min(count, blocks.length - offset));
		
		final long now = System.currentTimeMillis();
		if (now - start > 100) {
			NostrumMagica.logger.info("Joining to took " + (now - start) + "ms!");
		}
		
		return this;
	}
	
	public boolean shouldSplit() {
		return dimensions.getX() * dimensions.getY() * dimensions.getZ() > MAX_BLUEPRINT_BLOCKS; 
	}
	
	@Override
	public void scanBlocks(IBlueprintScanner scanner) {
		int slot = 0;
		final int width = this.dimensions.getX();
		final int height = this.dimensions.getY();
		final int length = this.dimensions.getZ();
		final BlockPos origin = this.entry.getPos();
		for (int i = 0; i < width; i++)
		for (int j = 0; j < height; j++)
		for (int k = 0; k < length; k++) {
			BlockPos offsetOrigOrient = new BlockPos(i, j, k).subtract(origin);
//			BlockPos offsetNorthOrient = IBlueprint.ApplyRotation(offsetOrigOrient, 
//						IBlueprint.GetModDir(Direction.NORTH, this.entry.getFacing())
//					); // rotate to north
			final int blockIdx = slot++;
			blocks[blockIdx] = scanner.scan(offsetOrigOrient, blocks[blockIdx]);
		}
	}
	
	private void deserializeNBTStyleInternal(CompoundNBT nbt, byte version) {
		BlockPos dims = NBTUtil.readBlockPos(nbt.getCompound(NBT_DIMS));
		// When breaking blueprints into pieces, the first one has an actual copy of the real size of the whole thing.
		// If we have one of those, allocate the FULL array size instead of the small one
		BlockPos masterDims = NBTUtil.readBlockPos(nbt.getCompound(NBT_WHOLE_DIMS));
		BlueprintBlock[] blocks = null;
		BlueprintLocation entry = null;
		
		if (dims.distanceSq(0, 0, 0, false) == 0) {
			return;
		} else {
			ListNBT list = nbt.getList(NBT_BLOCK_LIST, NBT.TAG_COMPOUND);
			
			final int count = dims.getX() * dims.getY() * dims.getZ();
			if (count != list.size()) {
				return;
			}
			
//			ProgressBar bar = null;
//			if (!NostrumMagica.initFinished) {
//				bar = ProgressManager.push("Loading Room", 2);
//				bar.step("Blocks");
//			}
			
			if (masterDims.distanceSq(0, 0, 0, false) == 0) {
				blocks = new BlueprintBlock[count];
			} else {
				blocks = new BlueprintBlock[masterDims.getX() * masterDims.getY() * masterDims.getZ()];
			}
			
			for (int i = 0; i < count; i++) {
				CompoundNBT tag = (CompoundNBT) list.get(i);
				blocks[i] = BlueprintBlock.fromNBT((byte)version, tag);
			}
			
//			if (!NostrumMagica.initFinished && bar != null) {
//				bar.step("Doors and Exits");
//			}
			
			if (nbt.contains(NBT_ENTRY)) {
				CompoundNBT tag = nbt.getCompound(NBT_ENTRY);
				entry = BlueprintLocation.fromNBT(tag);
			}
			
			if (!NostrumMagica.initFinished) {
//				ProgressManager.pop(bar);
			}
		}
		
		this.dimensions = masterDims.distanceSq(0, 0, 0, false) == 0 ? dims : masterDims;
		this.blocks = blocks;
		this.entry = entry;
	}
	
	private void deserializeVersion4(CompoundNBT nbt) {
		deserializeNBTStyleInternal(nbt, (byte) 3);
		
		if (nbt.contains(NBT_PIECE_OFFSET)) {
			partOffset = nbt.getInt(NBT_PIECE_OFFSET);
		}
	}
	
	protected Blueprint(LoadContext context, CompoundNBT nbt) {
		this(null, null, null);
		
		byte version = nbt.getByte(NBT_VERSION);
		switch (version) {
		//case 0: // Was tag per block with block ID as int and TE data on that tag
		//case 1: // Was one giant int array for block(states) and a seperate tag array for TE
		//case 2: // Was like version 0 except blockstate was an int.
		case 3:
			deserializeVersion4(nbt);
			break;
		default:
			NostrumMagica.logger.fatal(context.name + ": Blueprint has version we don't understand: " + context.source);
			throw new RuntimeException(context.name + ": Could not parse blueprint version " + version + ": " + context.source);
		}
	}
	
	public static Blueprint FromNBT(LoadContext context, CompoundNBT nbt) {
		return new Blueprint(context, nbt);
	}
	
	public CompoundNBT toNBT() {
		if (shouldSplit()) {
			// Too big! Use toNBTWithBreakdown() instead!
			throw new RuntimeException("Blueprint too large to be written as single blob!");
		}
		
		return toNBTInternal(0, MAX_BLUEPRINT_BLOCKS);
	}
	
	public INBTGenerator toNBTWithBreakdown() {
		if (!shouldSplit()) {
			return new INBTGenerator() {
				boolean used = false;
				
				@Override
				public CompoundNBT next() {
					used = true;
					return toNBT();
				}

				@Override
				public int getTotal() {
					return 1;
				}

				@Override
				public boolean hasNext() {
					return !used;
				}
			};
			//return new CompoundNBT[]{toNBT()};
		}
		
		// Need to be writing out whole blocks. Get number of blocks based on whole dimensions and whatever fits within the block limit.
		// Attempt to grab some number of entire Z by Y slices. If we can't even grab a full one, bump it up to a full one anyways.
		int size = (dimensions.getZ() * dimensions.getY()) / MAX_BLUEPRINT_BLOCKS; //int division
		if (size == 0) {
			// Not even one slice could fit. Fudge it anyways.
			size = dimensions.getZ() * dimensions.getY();
		}
		final int xSlices = (int) Math.ceil((double) blocks.length / (double) size);
		final int fSize = size;
		
		return new INBTGenerator() {

			int i = 0;
			
			@Override
			public CompoundNBT next() {
				if (i >= xSlices) {
					return null;
				}
				return toNBTInternal(i++ * fSize, fSize);
			}

			@Override
			public int getTotal() {
				return xSlices;
			}

			@Override
			public boolean hasNext() {
				return i < xSlices;
			}
			
		};
	}
	
	// Version 4
	protected CompoundNBT toNBTInternal(int startIdx, int count) {
		
		CompoundNBT nbt = new CompoundNBT();
		ListNBT list = new ListNBT();
		final int endIdx = Math.min(blocks.length, startIdx + count);
		
		if (blocks != null) {
			for (int i = startIdx; i < endIdx; i++) {
				list.add(blocks[i].toNBT());
			}
		}
		
		nbt.put(NBT_BLOCK_LIST, list);
		{
			// If whole struct, just dump dims
			if (startIdx == 0 && endIdx == blocks.length) {
				nbt.put(NBT_DIMS, NBTUtil.writeBlockPos(dimensions));
			} else {
				// Else figure out how big it was
				final int numBlocks = endIdx - startIdx;
				final int base = dimensions.getY() * dimensions.getZ();
				nbt.put(NBT_DIMS, NBTUtil.writeBlockPos(new BlockPos(numBlocks / base, dimensions.getY(), dimensions.getZ())));
			}
		}
		if (this.entry != null) {
			nbt.put(NBT_ENTRY, entry.toNBT());
		}
		
		// First blueprint (when splitting) has all the extra pieces
		if (startIdx == 0) {
			// Master ALSO has the REAL size, so we can allocate all the space up front instead of resizing
			nbt.put(NBT_WHOLE_DIMS, NBTUtil.writeBlockPos(dimensions));
		} else {
			// Others have their row offset recorded
			nbt.putInt(NBT_PIECE_OFFSET, startIdx / (dimensions.getY() * dimensions.getZ()));
		}
		
		nbt.putByte(NBT_VERSION, (byte)3);
		
		return nbt;
	}
	
	public static interface INBTGenerator {
		
		public CompoundNBT next();
		
		public int getTotal();
		
		public boolean hasNext();
		
	}
}
