package com.smanzana.nostrummagica.blocks;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.MasterSpellCreationGui;
import com.smanzana.nostrummagica.tiles.SpellTableEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

@SuppressWarnings("deprecation")
public class MasterSpellTable extends HorizontalBlock {
	
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final BooleanProperty MASTER = BooleanProperty.create("master");
	
	public static final String ID = "spelltable_master";
	
	public MasterSpellTable() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(3.0f, 15.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.harvestLevel(1)
				.notSolid()
				);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(MASTER, true)
				.with(FACING, Direction.NORTH));
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction direction = context.getPlacementHorizontalFacing().getOpposite();
		BlockPos blockpos = context.getPos();
		BlockPos blockpos1 = blockpos.offset(direction);
		return context.getWorld().getBlockState(blockpos1).isReplaceable(context) ? this.getSlaveState(direction) : null;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		if (!worldIn.isRemote) {
			BlockPos blockpos = pos.offset(state.get(HORIZONTAL_FACING));
			worldIn.setBlockState(blockpos, getMaster(state.get(HORIZONTAL_FACING).getOpposite()), 3);
			worldIn.notifyNeighborsOfStateChange(pos, Blocks.AIR);
			state.updateNeighbours(worldIn, pos, 3);
		}
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(MASTER, FACING);
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		if (state.get(MASTER)) {
			TileEntity ent = world.getTileEntity(pos);
			if (!world.isRemote && ent != null) {
				SpellTableEntity table = (SpellTableEntity) ent;
				for (int i = 0; i < table.getSizeInventory(); i++) {
					if (table.getStackInSlot(i) != null) {
						ItemEntity item = new ItemEntity(
								world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
								table.removeStackFromSlot(i));
						world.addEntity(item);
					}
				}
			}
		}
		
		world.removeBlock(getPaired(state, pos), false);
	}
	
	private BlockPos getPaired(BlockState state, BlockPos pos) {
		return pos.offset(state.get(FACING));
	}
	
//	private BlockPos getMaster(BlockState state, BlockPos pos) {
//		if (state.get(MASTER))
//			return pos;
//		
//		return pos.offset(state.get(FACING));
//	}
	
	public BlockState getSlaveState(Direction direction) {
		return this.getDefaultState().with(MASTER, false)
				.with(FACING, direction);
	}


	public BlockState getMaster(Direction enumfacing) {
		return this.getDefaultState().with(MASTER, true)
				.with(FACING, enumfacing);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (state.get(MASTER))
			return new SpellTableEntity();
		
		return null;
	}
	
	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(world, pos, state);
			world.removeTileEntity(pos);
		}
	}
	
	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand handIn, BlockRayTraceResult hit) {
		
		if (state.get(MASTER) == false) {
			pos = pos.offset(state.get(FACING));
		}
		
		SpellTableEntity te = (SpellTableEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, MasterSpellCreationGui.SpellCreationContainer.Make(te));
		
		return ActionResultType.SUCCESS;
	}
}
