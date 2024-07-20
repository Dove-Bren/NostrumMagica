package com.smanzana.nostrummagica.world.blueprints;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.IDirectionalBlock;
import com.smanzana.nostrummagica.block.IHorizontalBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class BlueprintBlock {
		
	private static Map<BlockState, BlueprintBlock> BLUEPRINT_CACHE = new HashMap<>();
	
	private static BlueprintBlock CHECK_BLUEPRINT_CACHE(BlockState state) {
		return BLUEPRINT_CACHE.get(state);
	}
	
	private static void SET_BLUEPRINT_CACHE(BlockState state, BlueprintBlock block) {
		BLUEPRINT_CACHE.put(state, block);
	}
	
	public static BlueprintBlock getBlueprintBlock(BlockState state, CompoundNBT teData) {
		BlueprintBlock block = null;
		if (teData == null) {
			block = CHECK_BLUEPRINT_CACHE(state);
		}
		
		if (block == null) {
			if (state == null || state.getBlock() == Blocks.AIR) {
				block = Air;
			} else {
				block = new BlueprintBlock(state, teData);
			}
			if (teData == null) {
				SET_BLUEPRINT_CACHE(state, block);
			}
		}
		
		return block;
	}
	
	private BlockState state;
	private CompoundNBT tileEntityData;
	
	private BlueprintBlock(BlockState state, CompoundNBT teData) {
		this.state = state;
		this.tileEntityData = teData;
		
		// Refuse to store air
		if (state != null && state.getBlock() == Blocks.AIR) {
			state = null;
			tileEntityData = null;
		}
	}
	
	public BlueprintBlock(IWorld world, BlockPos pos) {
		if (world.isAirBlock(pos)) {
			; //leave null
		} else {
			this.state = world.getBlockState(pos);
			TileEntity te = world.getTileEntity(pos);
			if (te != null) {
				this.tileEntityData = new CompoundNBT();
				te.write(this.tileEntityData);
			}
		}
	}
	
//	public static BlueprintBlock MakeFromData(BlockState state, CompoundNBT teData) {
//		return new BlueprintBlock(state, teData);
//	}
	
	public static BlueprintBlock Air = new BlueprintBlock((BlockState) null, null);
	
	private static final String NBT_TILE_ENTITY = "te_data";
	private static final String NBT_BLOCKSTATE_TAG = "blockstate";
	
	public static BlueprintBlock fromNBT(byte version, CompoundNBT nbt) {
		BlockState state = null;
		CompoundNBT teData = null;
		switch (version) {
		case 0:
//				state = Block.getStateById(nbt.getInt(NBT_BLOCK));
//				
//				// Block.getStateById defaults to air. Remove it!
//				if (state != null && state.getBlock() == Blocks.AIR) {
//					state = null;
//				}
//				
//				if (state != null && nbt.contains(NBT_TILE_ENTITY)) {
//					teData = nbt.getCompound(NBT_TILE_ENTITY);
//				}
//				break;
			// was int id for blockstate; deprecated (and not minecraft save portable)
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		case 1:
			// I don't remember what this version was
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		case 2:
			// Was block name + (int) meta saving.
			// No more meta, so this doesn't work anymore
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		case 3:
			state = NBTUtil.readBlockState(nbt.getCompound(NBT_BLOCKSTATE_TAG));
			
			if (state != null && nbt.contains(NBT_TILE_ENTITY)) {
				teData = nbt.getCompound(NBT_TILE_ENTITY);
			}
			break;
		default:
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		}
		
		return BlueprintBlock.getBlueprintBlock(state, teData);
	}
	
	public CompoundNBT toNBT() {
		CompoundNBT tag = new CompoundNBT();
		
		// Version 0
//			if (state != null) {
//				tag.putInt(NBT_BLOCK, Block.getStateId(state));
//				if (tileEntityData != null) {
//					tag.put(NBT_TILE_ENTITY, tileEntityData);
//				}
//			}
		
//			// Version 2
//			if (state != null) {
//				tag.putString(NBT_BLOCK_TYPE, state.getBlock().getRegistryName().toString());
//				tag.putInt(NBT_BLOCK_STATE, state.getBlock().getMetaFromState(state));
//				if (tileEntityData != null) {
//					tag.put(NBT_TILE_ENTITY, tileEntityData);
//				}
//			}
		
		// Version 3
		if (state != null) {
			tag.put(NBT_BLOCKSTATE_TAG, NBTUtil.writeBlockState(state));
			if (tileEntityData != null) {
				tag.put(NBT_TILE_ENTITY, tileEntityData);
			}
		}
		
		
		return tag;
	}
	
	private static Direction rotate(Direction in, Direction mod) {
		if (in != Direction.UP && in != Direction.DOWN) {
			int count = mod.getOpposite().getHorizontalIndex();
			while (count > 0) {
				count--;
				in = in.rotateY();
			}
		}
		
		return in;
	}
	
	public BlockState getSpawnState(Direction facing) {
		if (state != null) {
			BlockState placeState = state;
			
			if (facing != null && facing.getOpposite().getHorizontalIndex() != 0) {
				
				Block block = placeState.getBlock();
				if (block instanceof HorizontalBlock) {
					Direction cur = placeState.get(HorizontalBlock.HORIZONTAL_FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(HorizontalBlock.HORIZONTAL_FACING, cur);
				} else if (block instanceof WallTorchBlock) {
					Direction cur = placeState.get(WallTorchBlock.HORIZONTAL_FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(WallTorchBlock.HORIZONTAL_FACING, cur);
				} else if (block instanceof RedstoneWallTorchBlock) {
					Direction cur = placeState.get(RedstoneWallTorchBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(RedstoneWallTorchBlock.FACING, cur);
				} else if (block instanceof LadderBlock) {
					Direction cur = placeState.get(LadderBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(LadderBlock.FACING, cur);
				} else if (block instanceof StairsBlock) {
					Direction cur = placeState.get(StairsBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(StairsBlock.FACING, cur);
				} else if (block instanceof DirectionalBlock) {
					// Only want to rotate horizontally
					Direction cur = placeState.get(DirectionalBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(DirectionalBlock.FACING, cur);
				} else if (block instanceof IHorizontalBlock) {
					// Only want to rotate horizontally
					Direction cur = placeState.get(IHorizontalBlock.HORIZONTAL_FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(IHorizontalBlock.HORIZONTAL_FACING, cur);
				} else if (block instanceof IDirectionalBlock) {
					// Only want to rotate horizontally
					Direction cur = placeState.get(IDirectionalBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(IDirectionalBlock.FACING, cur);
				} else if (block instanceof ChestBlock) {
					// Doesn't implement directional interfaces
					// Only want to rotate horizontally
					Direction cur = placeState.get(ChestBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(ChestBlock.FACING, cur);
				}
			}
			
			return placeState;
		} else {
			return null;
		}
	}
	
	public CompoundNBT getTileEntityData() {
		return tileEntityData;
	}
	
	public Direction getFacing() {
		Direction ret = null;
		Block block = state.getBlock();
		if (block instanceof HorizontalBlock) {
			ret = state.get(HorizontalBlock.HORIZONTAL_FACING);
			
			// HACK: Reverse if special enterance block cause they're backwards LOL
			if (block instanceof RedstoneDiodeBlock || block instanceof ComparatorBlock) {
				ret = ret.getOpposite();
			}
			
		} else if (block instanceof WallTorchBlock) {
			ret = state.get(WallTorchBlock.HORIZONTAL_FACING);
		} else if (block instanceof RedstoneWallTorchBlock) {
			ret = state.get(RedstoneWallTorchBlock.FACING);
		} else if (block instanceof LadderBlock) {
			ret = state.get(LadderBlock.FACING);
		} else if (block instanceof StairsBlock) {
			ret = state.get(StairsBlock.FACING);
		} else if (block instanceof DirectionalBlock) {
			ret = state.get(DirectionalBlock.FACING);
		} else if (block instanceof IDirectionalBlock) {
			ret = state.get(IDirectionalBlock.FACING);
		} else if (block instanceof ChestBlock) {
			ret = state.get(ChestBlock.FACING);
		}
		return ret;
	}
	
	public BlockState getState() {
		return this.state;
	}
	
	public @Nullable CompoundNBT getRawTileEntityData() {
		return this.tileEntityData;
	}
}
