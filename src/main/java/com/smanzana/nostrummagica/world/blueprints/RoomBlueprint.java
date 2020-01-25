package com.smanzana.nostrummagica.world.blueprints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

/**
 * Contains all the data needed to spawn a room in the world
 * @author Skyler
 *
 */
public class RoomBlueprint {
	
	public static class BlueprintBlock {
		
		private static Map<String, Block> BLOCK_CACHE = new HashMap<>();
		
		private static Block CHECK_CACHE(String name) {
			return BLOCK_CACHE.get(name.toLowerCase());
		}
		
		private static void SET_CACHE(String name, Block block) {
			BLOCK_CACHE.put(name.toLowerCase(), block);
		}
		
		private IBlockState state;
		private NBTTagCompound tileEntityData;
		
		public BlueprintBlock(IBlockState state, NBTTagCompound teData) {
			this.state = state;
			this.tileEntityData = teData;
			
			// Refuse to store air
			if (state != null && state.getBlock() == Blocks.AIR) {
				state = null;
				tileEntityData = null;
			}
		}
		
		public BlueprintBlock(World world, BlockPos pos) {
			if (world.isAirBlock(pos)) {
				; //leave null
			} else {
				this.state = world.getBlockState(pos);
				TileEntity te = world.getTileEntity(pos);
				if (te != null) {
					this.tileEntityData = new NBTTagCompound();
					te.writeToNBT(this.tileEntityData);
				}
			}
		}
		
		public static final String NBT_BLOCK = "block_id";
		public static final String NBT_BLOCK_TYPE = "block_type";
		public static final String NBT_BLOCK_STATE = "block_meta";
		public static final String NBT_TILE_ENTITY = "te_data";
		
		@SuppressWarnings("deprecation")
		public static BlueprintBlock fromNBT(byte version, NBTTagCompound nbt) {
			IBlockState state = null;
			NBTTagCompound teData = null;
			switch (version) {
			case 0:
				state = Block.getStateById(nbt.getInteger(NBT_BLOCK));
				
				// Block.getStateById defaults to air. Remove it!
				if (state != null && state.getBlock() == Blocks.AIR) {
					state = null;
				}
				
				if (state != null && nbt.hasKey(NBT_TILE_ENTITY)) {
					teData = nbt.getCompoundTag(NBT_TILE_ENTITY);
				}
				break;
			case 1:
				throw new RuntimeException("Blueprint block doesn't understand version " + version);
			case 2:
				String type = nbt.getString(NBT_BLOCK_TYPE);
				if (!type.isEmpty()) {
					Block block = CHECK_CACHE(type);
					if (block == null) {
						block = Block.getBlockFromName(type);
						SET_CACHE(type, block);
					}
					if (block != null) {
						state = block.getStateFromMeta(nbt.getInteger(NBT_BLOCK_STATE));
					}
				}
				
				// Prevent air from getting in
				if (state != null && state.getBlock() == Blocks.AIR) {
					state = null;
				}
				
				if (state != null && nbt.hasKey(NBT_TILE_ENTITY)) {
					teData = nbt.getCompoundTag(NBT_TILE_ENTITY);
				}
				break;
			default:
				throw new RuntimeException("Blueprint block doesn't understand version " + version);
			}
			
			return new BlueprintBlock(state, teData);
		}
		
		public NBTTagCompound toNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			
			// Version 0
//			if (state != null) {
//				tag.setInteger(NBT_BLOCK, Block.getStateId(state));
//				if (tileEntityData != null) {
//					tag.setTag(NBT_TILE_ENTITY, tileEntityData);
//				}
//			}
			
			// Version 2
			if (state != null) {
				tag.setString(NBT_BLOCK_TYPE, state.getBlock().getRegistryName().toString());
				tag.setInteger(NBT_BLOCK_STATE, state.getBlock().getMetaFromState(state));
				if (tileEntityData != null) {
					tag.setTag(NBT_TILE_ENTITY, tileEntityData);
				}
			}
			
			
			return tag;
		}
		
		private static EnumFacing rotate(EnumFacing in, EnumFacing mod) {
			if (in != EnumFacing.UP && in != EnumFacing.DOWN) {
				int count = mod.getOpposite().getHorizontalIndex();
				while (count > 0) {
					count--;
					in = in.rotateY();
				}
			}
			
			return in;
		}
		
		public void spawn(World world, BlockPos at, EnumFacing facing) {
			if (state != null) {
				IBlockState placeState = state;
				
				if (facing != null && facing.getOpposite().getHorizontalIndex() != 0) {
					
					Block block = placeState.getBlock();
					if (block instanceof BlockHorizontal) {
						EnumFacing cur = placeState.getValue(BlockHorizontal.FACING);
						cur = rotate(cur, facing);
						placeState = placeState.withProperty(BlockHorizontal.FACING, cur);
					} else if (block instanceof BlockTorch) {
						EnumFacing cur = placeState.getValue(BlockTorch.FACING);
						cur = rotate(cur, facing);
						placeState = placeState.withProperty(BlockTorch.FACING, cur);
					} else if (block instanceof BlockLadder) {
						EnumFacing cur = placeState.getValue(BlockLadder.FACING);
						cur = rotate(cur, facing);
						placeState = placeState.withProperty(BlockLadder.FACING, cur);
					} else if (block instanceof BlockStairs) {
						EnumFacing cur = placeState.getValue(BlockStairs.FACING);
						cur = rotate(cur, facing);
						placeState = placeState.withProperty(BlockStairs.FACING, cur);
					}
				}
				
				world.setBlockState(at, placeState, 2);
				if (tileEntityData != null) {
					TileEntity te = TileEntity.create(world, tileEntityData);
					world.setTileEntity(at, te);
				}
			} else {
				world.removeTileEntity(at);
				world.setBlockToAir(at);
			}
		}
		
		public boolean isDoorIndicator() {
			return state != null && state.getBlock() instanceof BlockRedstoneRepeater;
		}
		
		public boolean isEntry() {
			return state != null && state.getBlock() instanceof BlockRedstoneComparator;
		}
		
		public EnumFacing getFacing() {
			EnumFacing ret = null;
			Block block = state.getBlock();
			if (block instanceof BlockHorizontal) {
				ret = state.getValue(BlockHorizontal.FACING);
				
				// HACK: Reverse if special enterance block cause they're backwards LOL
				if (block instanceof BlockRedstoneRepeater || block instanceof BlockRedstoneComparator) {
					ret = ret.getOpposite();
				}
				
			} else if (block instanceof BlockTorch) {
				ret = state.getValue(BlockTorch.FACING);
			} else if (block instanceof BlockLadder) {
				ret = state.getValue(BlockLadder.FACING);
			} else if (block instanceof BlockStairs) {
				ret = state.getValue(BlockStairs.FACING);
			}
			return ret;
		}
	}
	
	public static final String NBT_DIMS = "dimensions";
	public static final String NBT_WHOLE_DIMS = "master_dimensions";
	public static final String NBT_BLOCK_LIST = "blocks";
	public static final String NBT_DOOR_LIST = "doors";
	public static final String NBT_ENTRY = "entry";
	public static final String NBT_VERSION = "version";
	public static final String NBT_ENTITIES = "entities";
	public static final String NBT_PIECE_OFFSET = "part_offset";
	public static final int MAX_BLUEPRINT_BLOCKS = 32 * 32 * 32;
	
	private BlockPos dimensions;
	private BlueprintBlock[] blocks;
	private DungeonExitPoint entry;
	private Set<DungeonExitPoint> doors;
	private int partOffset; // Only used for fragments
	
	public RoomBlueprint(BlockPos dimensions, BlueprintBlock[] blocks, Set<DungeonExitPoint> exits, DungeonExitPoint entry) {
		if (dimensions != null && blocks != null
				&& (dimensions.getX() * dimensions.getY() * dimensions.getZ() != blocks.length)) {
			throw new RuntimeException("Dimensions do not match block array provided to blueprint constructor!");
		}
		this.dimensions = dimensions;
		this.blocks = blocks;
		this.doors = exits == null ? new HashSet<>() : exits;
		this.entry = entry;
	}
	
	public RoomBlueprint(World world, BlockPos pos1, BlockPos pos2) {
		this(null, null, null, null);
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
		
		MutableBlockPos cursor = new MutableBlockPos();
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
			BlueprintBlock block = new BlueprintBlock(world, cursor);
			if (block.isDoorIndicator()) {
				this.doors.add(new DungeonExitPoint(cursor.toImmutable().subtract(pos1), block.getFacing().getOpposite()));
				block = new BlueprintBlock((IBlockState) null, null); // Make block an air one
			} else if (block.isEntry()) {
				if (this.entry != null) {
					NostrumMagica.logger.error("Found multiple entry points to room while creating blueprint!");
				}
				this.entry = new DungeonExitPoint(cursor.toImmutable().subtract(pos1), block.getFacing());
				block = new BlueprintBlock((IBlockState) null, null); // Make block an air one
			}
			
			blocks[slot++] = block;
		}
	}
	
	private static EnumFacing getModDir(EnumFacing original, EnumFacing newFacing) {
		EnumFacing out = EnumFacing.NORTH;
		int rotCount = (4 + newFacing.getHorizontalIndex() - original.getHorizontalIndex()) % 4;
		while (rotCount-- > 0) {
			out = out.rotateY();
		}
		return out;
	}
	
	private BlockPos applyRotation(BlockPos input, EnumFacing modDir) {
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
	
	private static final boolean isSecondPassBlock(BlueprintBlock block) {
		if (block.state != null) {
			if (!(block.state.isFullBlock() || block.state.isOpaqueCube() || block.state.isNormalCube())) {
				return true;
			}
		}
		
		return false;
	}
	
	public void spawn(World world, BlockPos at) {
		this.spawn(world, at, EnumFacing.NORTH);
	}
	
	public void spawn(World world, BlockPos at, EnumFacing direction) {
		
		final int width = dimensions.getX();
		final int height = dimensions.getY();
		final int length = dimensions.getZ();
		MutableBlockPos cursor = new MutableBlockPos();
		EnumFacing modDir = EnumFacing.NORTH; // 0
		
		// Apply rotation changes
		if (this.entry != null) {
			// Get facing mod
			modDir = getModDir(entry.getFacing(), direction);
		}
		
		BlockPos adjustedDims;
		{
			BlockPos wrapper = new BlockPos(width, 0, length);
			adjustedDims = applyRotation(wrapper, modDir);
			
			// Note: Some vals may be negative. We leave them for now to get proper offset adjustement, and then
			// straighten them out for later calcs
		}
		
		// Outside loop for each chunk
		// Inner loop loops i, j, k to 0 to < 16 in each
		// Loops basically always go from 0 to 16 except in edge ones I suppose
		BlockPos offset = entry == null ? new BlockPos(0,0,0) : entry.getPos();
		BlockPos unit = applyRotation(new BlockPos(1, 0, 1), modDir);
		
		// To get actual least-x leastz coordinates, we need to add offset rotated to proper orientation
		BlockPos origin;
		{
			MutableBlockPos rotOffset = new MutableBlockPos(applyRotation(offset, modDir));
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
			
			origin = at.toImmutable().subtract(rotOffset);
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
		modDir = modDir.getHorizontalIndex() % 2 == 1 ? modDir.getOpposite() : modDir;
		
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
				// World pos is simply chunk position + inner loop offset
				// (j is data y, so offset to world y)
				cursor.setPos(chunkOffsetX + i, j + origin.getY(), chunkOffsetZ + k);
				
				// Find data position by applying rotation to transform x and z coords into u and v data coords
				unit = applyRotation(new BlockPos(1, 0, 1), modDir);
				BlockPos dataPos = applyRotation(cursor.toImmutable().subtract(origin), modDir);
				
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
					block.spawn(world, cursor, modDir);
				}
			}
			
			// Spawn all second-pass blocks
			for (int i = 0; i < secondPassBlocks.size(); i++) {
				BlockPos secondPos = secondPassPos.get(i);
				BlueprintBlock block = secondPassBlocks.get(i);
				block.spawn(world, secondPos, modDir);
			}
			
			world.getChunkFromBlockCoords(cursor).setChunkModified();
		}
	}
	
	public BlockPos getDimensions() {
		return this.dimensions;
	}
	
	/**
	 * Returns total space dimensions of the blueprint, if it were rotated to the desired facing.
	 * @param facing The desired facing for the entry way, if there is one.
	 * @return
	 */
	public BlockPos getAdjustedDimensions(EnumFacing facing) {
		EnumFacing mod = getModDir(entry == null ? EnumFacing.NORTH : entry.getFacing(), facing);
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
	public BlockPos getAdjustedOffset(EnumFacing facing) {
		BlockPos offset = entry == null ? new BlockPos(0,0,0) : entry.getPos().toImmutable();
		EnumFacing mod = getModDir(entry == null ? EnumFacing.NORTH : entry.getFacing(), facing);
		
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
	
	public Collection<DungeonExitPoint> getExits() {
		return this.doors;
	}
	
	public DungeonExitPoint getEntry() {
		return this.entry;
	}
	
	/**
	 * Adds one blueprint to the other. Returns the original blueprint modified to include blocks from the other.
	 * This SHOULD be compatible with completely different blueprints.
	 * For now, all I'm implementing is piecing blueprints broken apart with {@link #toNBTWithBreakdown()}, where
	 * all blueprints passed in should be within the dimensions of the original.
	 * @param blueprint
	 * @return
	 */
	public RoomBlueprint join(RoomBlueprint blueprint) {
		// TODO expand this to accept different ones!
		
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
	
	private static class BlueprintSavedTE {
		
		public static final String NBT_DATA = "te_data";
		public static final String NBT_POS = "pos";
		
		public NBTTagCompound nbtTagData;
		public BlockPos pos;
		
		public BlueprintSavedTE(NBTTagCompound data, BlockPos pos) {
			this.nbtTagData = data;
			this.pos = pos;
		}
		
		@SuppressWarnings("unused")
		public NBTTagCompound toNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setTag(NBT_DATA, nbtTagData);
			tag.setLong(NBT_POS, pos.toLong());
			return tag;
		}
		
		public static BlueprintSavedTE fromNBT(NBTTagCompound nbt) {
			long pos = nbt.getLong(NBT_POS);
			return new BlueprintSavedTE(nbt.getCompoundTag(NBT_DATA), BlockPos.fromLong(pos));
		}
	}
	
	@SuppressWarnings("null")
	private static RoomBlueprint deserializeNBTStyleInternal(NBTTagCompound nbt, byte version) {
		BlockPos dims = NBTUtil.getPosFromTag(nbt.getCompoundTag(NBT_DIMS));
		// When breaking blueprints into pieces, the first one has an actual copy of the real size of the whole thing.
		// If we have one of those, allocate the FULL array size instead of the small one
		BlockPos masterDims = NBTUtil.getPosFromTag(nbt.getCompoundTag(NBT_WHOLE_DIMS));
		BlueprintBlock[] blocks = null;
		Set<DungeonExitPoint> doors = null;
		DungeonExitPoint entry = null;
		
		if (dims.distanceSq(0, 0, 0) == 0) {
			return null;
		} else {
			NBTTagList list = nbt.getTagList(NBT_BLOCK_LIST, NBT.TAG_COMPOUND);
			
			final int count = dims.getX() * dims.getY() * dims.getZ();
			if (count != list.tagCount()) {
				return null;
			}
			
			ProgressBar bar = null;
			if (!NostrumMagica.initFinished) {
				bar = ProgressManager.push("Loading Room", 2);
				bar.step("Blocks");
			}
			
			if (masterDims.distanceSq(0, 0, 0) == 0) {
				blocks = new BlueprintBlock[count];
			} else {
				blocks = new BlueprintBlock[masterDims.getX() * masterDims.getY() * masterDims.getZ()];
			}
			
			for (int i = 0; i < count; i++) {
				NBTTagCompound tag = (NBTTagCompound) list.get(i);
				blocks[i] = BlueprintBlock.fromNBT((byte)version, tag);
			}
			
			if (!NostrumMagica.initFinished) {
				bar.step("Doors and Exits");
			}
			
			list = nbt.getTagList(NBT_DOOR_LIST, NBT.TAG_COMPOUND);
			doors = new HashSet<>();
			while (!list.hasNoTags()) {
				NBTTagCompound tag = (NBTTagCompound) list.removeTag(0);
				doors.add(DungeonExitPoint.fromNBT(tag));
			}
			
			if (nbt.hasKey(NBT_ENTRY)) {
				entry = DungeonExitPoint.fromNBT(nbt.getCompoundTag(NBT_ENTRY));
			}
			
			if (!NostrumMagica.initFinished) {
				ProgressManager.pop(bar);
			}
		}
		
		return new RoomBlueprint(masterDims.distanceSq(0, 0, 0) == 0 ? dims : masterDims, blocks, doors, entry);
	}
	
	private static RoomBlueprint deserializeVersion1(NBTTagCompound nbt) {
		return deserializeNBTStyleInternal(nbt, (byte)0);
	}
	
	@SuppressWarnings("null")
	private static RoomBlueprint deserializeVersion2(NBTTagCompound nbt) {
		// Store blocks as int array
		// Store TileEntities separately since there are likely few of them
		
		BlockPos dims = NBTUtil.getPosFromTag(nbt.getCompoundTag(NBT_DIMS));
		// When breaking blueprints into pieces, the first one has an actual copy of the real size of the whole thing.
		// If we have one of those, allocate the FULL array size instead of the small one
		BlockPos masterDims = NBTUtil.getPosFromTag(nbt.getCompoundTag(NBT_WHOLE_DIMS));
		BlueprintBlock[] blocks = null;
		Set<DungeonExitPoint> doors = null;
		DungeonExitPoint entry = null;
		
		if (dims.distanceSq(0, 0, 0) == 0) {
			return null;
		} else {
			int[] list = nbt.getIntArray(NBT_BLOCK_LIST);
			
			final int count = dims.getX() * dims.getY() * dims.getZ();
			if (count != list.length) {
				return null;
			}
			
			ProgressBar bar = null;
			if (!NostrumMagica.initFinished) {
				bar = ProgressManager.push("Loading Room", 2);
				bar.step("Blocks");
			}
			
			if (masterDims.distanceSq(0, 0, 0) == 0) {
				blocks = new BlueprintBlock[count];
			} else {
				blocks = new BlueprintBlock[masterDims.getX() * masterDims.getY() * masterDims.getZ()];
			}
			
			for (int i = 0; i < count; i++) {
				int id = list[i];
				blocks[i] = new BlueprintBlock(id == 0 ? null : Block.getStateById(list[i]), null);
			}
			
			if (nbt.hasKey(NBT_ENTITIES)) {
				NBTTagList entities = nbt.getTagList(NBT_ENTITIES, NBT.TAG_COMPOUND);
				int entCount = entities.tagCount();
				for (int i = 0; i < entCount; i++) {
					BlueprintSavedTE te = BlueprintSavedTE.fromNBT(entities.getCompoundTagAt(i));
					
					// Find offset into data blocks, and then transfer data onto that block
					blocks[
					    (te.pos.getX() * dims.getZ() * dims.getY())
					    + (te.pos.getY() * dims.getZ())
					    + te.pos.getZ()
					   ].tileEntityData = te.nbtTagData;
				}
			}
			
			if (!NostrumMagica.initFinished) {
				bar.step("Doors and Exits");
			}
			
			NBTTagList doorList = nbt.getTagList(NBT_DOOR_LIST, NBT.TAG_COMPOUND);
			doors = new HashSet<>();
			while (!doorList.hasNoTags()) {
				NBTTagCompound tag = (NBTTagCompound) doorList.removeTag(0);
				doors.add(DungeonExitPoint.fromNBT(tag));
			}
			
			if (nbt.hasKey(NBT_ENTRY)) {
				entry = DungeonExitPoint.fromNBT(nbt.getCompoundTag(NBT_ENTRY));
			}
			
			if (!NostrumMagica.initFinished) {
				ProgressManager.pop(bar);
			}
		}
		
		return new RoomBlueprint(masterDims.distanceSq(0, 0, 0) == 0 ? dims : masterDims, blocks, doors, entry);
	}
	
	private static RoomBlueprint deserializeVersion3(NBTTagCompound nbt) {
		RoomBlueprint blueprint = deserializeNBTStyleInternal(nbt, (byte) 2);
		
		if (nbt.hasKey(NBT_PIECE_OFFSET)) {
			blueprint.partOffset = nbt.getInteger(NBT_PIECE_OFFSET);
		}
		
		return blueprint;
	}
	
	public static RoomBlueprint fromNBT(NBTTagCompound nbt) {
		byte version = nbt.getByte(NBT_VERSION);
		switch (version) {
		case 0:
			return deserializeVersion1(nbt);
		case 1:
			return deserializeVersion2(nbt);
		case 2:
			return deserializeVersion3(nbt);
		default:
			NostrumMagica.logger.fatal("Blueprint has version we don't understand");
			throw new RuntimeException("Could not parse blueprint version " + version);
		}
	}
	
//	public NBTTagCompound toNBT() {
//		NBTTagCompound nbt = new NBTTagCompound();
//		NBTTagList entList = new NBTTagList();
//		int[] blockList = new int[this.blocks.length];
//		
//		for (int i = 0; i < blocks.length; i++) {
//			IBlockState state = this.blocks[i].state;
//			if (state == null) {
//				blockList[i] = 0;
//			} else {
//				blockList[i] = Block.getStateId(this.blocks[i].state);
//			}
//			if (this.blocks[i].tileEntityData != null) {
//				BlueprintSavedTE te = new BlueprintSavedTE(
//						this.blocks[i].tileEntityData,
//						new BlockPos(i / (dimensions.getZ() * dimensions.getY()),
//								((i / dimensions.getZ()) % dimensions.getY()),
//								(i % dimensions.getZ())));
//				entList.appendTag(te.toNBT());
//			}
//		}
//		
//		nbt.setIntArray(NBT_BLOCK_LIST, blockList);
//		nbt.setTag(NBT_ENTITIES, entList);
//		nbt.setTag(NBT_DIMS, NBTUtil.createPosTag(dimensions));
//		if (this.entry != null) {
//			nbt.setTag(NBT_ENTRY, entry.toNBT());
//		}
//		if (this.doors != null && !this.doors.isEmpty()) {
//			NBTTagList doorList = new NBTTagList();
//			for (DungeonExitPoint door : doors) {
//				doorList.appendTag(door.toNBT());
//			}
//			nbt.setTag(NBT_DOOR_LIST, doorList);
//		}
//		
//		nbt.setByte(NBT_VERSION, (byte)1);
//		return nbt;
//	}
	
	// Version 1
//	public NBTTagCompound toNBT() {
//		NBTTagCompound nbt = new NBTTagCompound();
//		NBTTagList list = new NBTTagList();
//		
//		if (blocks != null) {
//			for (int i = 0; i < blocks.length; i++) {
//				list.appendTag(blocks[i].toNBT());
//			}
//		}
//		
//		nbt.setTag(NBT_BLOCK_LIST, list);
//		nbt.setTag(NBT_DIMS, NBTUtil.createPosTag(dimensions));
//		if (this.entry != null) {
//			nbt.setTag(NBT_ENTRY, entry.toNBT());
//		}
//		if (this.doors != null && !this.doors.isEmpty()) {
//			list = new NBTTagList();
//			for (DungeonExitPoint door : doors) {
//				list.appendTag(door.toNBT());
//			}
//			nbt.setTag(NBT_DOOR_LIST, list);
//		}
//		
//		nbt.setByte(NBT_VERSION, (byte)0);
//		
//		return nbt;
//	}
	
	public NBTTagCompound toNBT() {
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
				public NBTTagCompound next() {
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
			//return new NBTTagCompound[]{toNBT()};
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
			public NBTTagCompound next() {
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
	
	// Version 3
	protected NBTTagCompound toNBTInternal(int startIdx, int count) {
		
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		final int endIdx = Math.min(blocks.length, startIdx + count);
		
		if (blocks != null) {
			for (int i = startIdx; i < endIdx; i++) {
				list.appendTag(blocks[i].toNBT());
			}
		}
		
		nbt.setTag(NBT_BLOCK_LIST, list);
		{
			// If whole struct, just dump dims
			if (startIdx == 0 && endIdx == blocks.length) {
				nbt.setTag(NBT_DIMS, NBTUtil.createPosTag(dimensions));
			} else {
				// Else figure out how big it was
				final int numBlocks = endIdx - startIdx;
				final int base = dimensions.getY() * dimensions.getZ();
				nbt.setTag(NBT_DIMS, NBTUtil.createPosTag(new BlockPos(numBlocks / base, dimensions.getY(), dimensions.getZ())));
			}
		}
		if (this.entry != null) {
			nbt.setTag(NBT_ENTRY, entry.toNBT());
		}
		
		// First blueprint (when splitting) has all the extra pieces
		if (startIdx == 0) {
			if (this.doors != null && !this.doors.isEmpty()) {
				list = new NBTTagList();
				for (DungeonExitPoint door : doors) {
					list.appendTag(door.toNBT());
				}
				nbt.setTag(NBT_DOOR_LIST, list);
			}
			
			// Master ALSO has the REAL size, so we can allocate all the space up front instead of resizing
			nbt.setTag(NBT_WHOLE_DIMS, NBTUtil.createPosTag(dimensions));
		} else {
			// Others have their row offset recorded
			nbt.setInteger(NBT_PIECE_OFFSET, startIdx / (dimensions.getY() * dimensions.getZ()));
		}
		
		nbt.setByte(NBT_VERSION, (byte)2);
		
		return nbt;
	}
	
	public static interface INBTGenerator {
		
		public NBTTagCompound next();
		
		public int getTotal();
		
		public boolean hasNext();
		
	}
	
}
