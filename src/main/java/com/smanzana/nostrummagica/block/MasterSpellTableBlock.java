package com.smanzana.nostrummagica.block;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.MasterSpellCreationGui;
import com.smanzana.nostrummagica.tile.SpellTableTileEntity;

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
public class MasterSpellTableBlock extends HorizontalBlock {
	
	public static final DirectionProperty FACING = HorizontalBlock.FACING;
	private static final BooleanProperty MASTER = BooleanProperty.create("master");
	
	public static final String ID = "spelltable_master";
	
	public MasterSpellTableBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(3.0f, 15.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.harvestLevel(1)
				.noOcclusion()
				);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(MASTER, true)
				.setValue(FACING, Direction.NORTH));
	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction direction = context.getHorizontalDirection().getOpposite();
		BlockPos blockpos = context.getClickedPos();
		BlockPos blockpos1 = blockpos.relative(direction);
		return context.getLevel().getBlockState(blockpos1).canBeReplaced(context) ? this.getSlaveState(direction) : null;
	}

	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(worldIn, pos, state, placer, stack);
		if (!worldIn.isClientSide) {
			BlockPos blockpos = pos.relative(state.getValue(FACING));
			worldIn.setBlock(blockpos, getMaster(state.getValue(FACING).getOpposite()), 3);
			worldIn.updateNeighborsAt(pos, Blocks.AIR);
			state.updateNeighbourShapes(worldIn, pos, 3);
		}
	}
	
	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(MASTER, FACING);
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		if (state.getValue(MASTER)) {
			TileEntity ent = world.getBlockEntity(pos);
			if (!world.isClientSide && ent != null) {
				SpellTableTileEntity table = (SpellTableTileEntity) ent;
				for (int i = 0; i < table.getContainerSize(); i++) {
					if (table.getItem(i) != null) {
						ItemEntity item = new ItemEntity(
								world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
								table.removeItemNoUpdate(i));
						world.addFreshEntity(item);
					}
				}
			}
		}
		
		world.removeBlock(getPaired(state, pos), false);
	}
	
	private BlockPos getPaired(BlockState state, BlockPos pos) {
		return pos.relative(state.getValue(FACING));
	}
	
//	private BlockPos getMaster(BlockState state, BlockPos pos) {
//		if (state.get(MASTER))
//			return pos;
//		
//		return pos.offset(state.get(FACING));
//	}
	
	public BlockState getSlaveState(Direction direction) {
		return this.defaultBlockState().setValue(MASTER, false)
				.setValue(FACING, direction);
	}


	public BlockState getMaster(Direction enumfacing) {
		return this.defaultBlockState().setValue(MASTER, true)
				.setValue(FACING, enumfacing);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (state.getValue(MASTER))
			return new SpellTableTileEntity();
		
		return null;
	}
	
	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(world, pos, state);
			world.removeBlockEntity(pos);
		}
	}
	
	@Override
	public boolean triggerEvent(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.triggerEvent(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity == null ? false : tileentity.triggerEvent(id, param);
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand handIn, BlockRayTraceResult hit) {
		
		if (state.getValue(MASTER) == false) {
			pos = pos.relative(state.getValue(FACING));
		}
		
		SpellTableTileEntity te = (SpellTableTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, MasterSpellCreationGui.SpellCreationContainer.Make(te));
		
		return ActionResultType.SUCCESS;
	}
}
