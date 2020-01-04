package com.smanzana.nostrummagica.world.dungeon.room;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Conveniently wraps methods to serialize and deserialize rooms from BIN format
 * @author Skyler
 *
 */
public final class DungeonRoomSerializationHelper {
	
	public static final String NBT_DIMS = "dimensions";
	public static final String NBT_BLOCK_LIST = "blocks";
	public static final String NBT_BLOCK = "block_id";
	public static final String NBT_TILE_ENTITY = "te_data";

	public static NBTBase serialize(World world, BlockPos pos1, BlockPos pos2) {
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
		
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		MutableBlockPos cursor = new MutableBlockPos();
		
		for (int i = 0; i <= pos2.getX() - pos1.getX(); i++)
		for (int j = 0; j <= pos2.getY() - pos1.getY(); j++)
		for (int k = 0; k <= pos2.getZ() - pos1.getZ(); k++) {
			cursor.setPos(pos1.getX() + i, pos1.getY() + j, pos1.getZ() + k);
			list.appendTag(serializeBlock(world, cursor));
		}
		
		nbt.setTag(NBT_BLOCK_LIST, list);
		nbt.setTag(NBT_DIMS, NBTUtil.createPosTag(pos2.toImmutable().subtract(pos1)));
		return nbt;
	}
	
	public static boolean loadFromNBT(World world, BlockPos startPos, NBTBase nbtIn) {
		boolean success = false;
		
		if (nbtIn instanceof NBTTagCompound) {
			NBTTagCompound nbt = (NBTTagCompound) nbtIn;
			BlockPos dims = null;
			dims = NBTUtil.getPosFromTag(nbt.getCompoundTag(NBT_DIMS));
			
			if (dims.distanceSq(0, 0, 0) == 0) {
				success = false;
			} else {
				success = true;
				NBTTagList list = nbt.getTagList(NBT_BLOCK_LIST, NBT.TAG_COMPOUND);
				MutableBlockPos cursor = new MutableBlockPos();
				for (int i = 0; success && i <= dims.getX(); i++)
				for (int j = 0; success && j <= dims.getY(); j++)
				for (int k = 0; success && k <= dims.getZ(); k++) {
					if (list.hasNoTags()) {
						success = false;
						break;
					}
					
					NBTTagCompound tag = (NBTTagCompound) list.removeTag(0);
					cursor.setPos(startPos.getX() + i, startPos.getY() + j, startPos.getZ() + k);
					if (!loadBlock(world, cursor, tag)) {
						success = false;
						break;
					}
				}
			}
		}
		
		return success;
	}
	
	private static NBTTagCompound serializeBlock(World world, BlockPos pos) {
		NBTTagCompound tag = new NBTTagCompound();
		if (world.isAirBlock(pos)) {
			; // do nothing. Leave empty tag!
		} else {
			IBlockState state = world.getBlockState(pos);
			TileEntity te = world.getTileEntity(pos);
			tag.setInteger(NBT_BLOCK, Block.getStateId(state));
			
			if (te != null) {
				NBTTagCompound subtag = new NBTTagCompound();
				te.writeToNBT(subtag);
				tag.setTag(NBT_TILE_ENTITY, subtag);
			}
		}
		
		return tag;
	}
	
	private static boolean loadBlock(World world, BlockPos pos, NBTTagCompound blockData) {
		IBlockState state = Block.getStateById(blockData.getInteger(NBT_BLOCK));
		if (state == null) {
			return false;
		}
		
		world.setBlockState(pos, state);
		if (blockData.hasKey(NBT_TILE_ENTITY)) {
			TileEntity te = TileEntity.create(world, blockData.getCompoundTag(NBT_TILE_ENTITY));
			world.setTileEntity(pos, te);
		}
		
		return true;
	}
	
}
