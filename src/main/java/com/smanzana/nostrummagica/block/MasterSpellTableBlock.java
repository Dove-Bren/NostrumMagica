package com.smanzana.nostrummagica.block;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.MasterSpellCreationGui;
import com.smanzana.nostrummagica.tile.SpellTableTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;

@SuppressWarnings("deprecation")
public class MasterSpellTableBlock extends HorizontalDirectionalBlock implements EntityBlock {
	
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final BooleanProperty MASTER = BooleanProperty.create("master");
	
	public static final String ID = "spelltable_master";
	
	public MasterSpellTableBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(3.0f, 15.0f)
				.sound(SoundType.WOOD)
				.noOcclusion()
				);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(MASTER, true)
				.setValue(FACING, Direction.NORTH));
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction direction = context.getHorizontalDirection().getOpposite();
		BlockPos blockpos = context.getClickedPos();
		BlockPos blockpos1 = blockpos.relative(direction);
		return context.getLevel().getBlockState(blockpos1).canBeReplaced(context) ? this.getSlaveState(direction) : null;
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(worldIn, pos, state, placer, stack);
		if (!worldIn.isClientSide) {
			BlockPos blockpos = pos.relative(state.getValue(FACING));
			worldIn.setBlock(blockpos, getMaster(state.getValue(FACING).getOpposite()), 3);
			worldIn.updateNeighborsAt(pos, Blocks.AIR);
			state.updateNeighbourShapes(worldIn, pos, 3);
		}
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(MASTER, FACING);
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		if (state.getValue(MASTER)) {
			BlockEntity ent = world.getBlockEntity(pos);
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
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if (state.getValue(MASTER))
			return new SpellTableTileEntity(pos, state);
		
		return null;
	}
	
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(world, pos, state);
			world.removeBlockEntity(pos);
		}
	}
	
	@Override
	public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int id, int param) {
		super.triggerEvent(state, worldIn, pos, id, param);
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity == null ? false : tileentity.triggerEvent(id, param);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand handIn, BlockHitResult hit) {
		
		if (state.getValue(MASTER) == false) {
			pos = pos.relative(state.getValue(FACING));
		}
		
		SpellTableTileEntity te = (SpellTableTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.Proxy.openContainer(playerIn, MasterSpellCreationGui.SpellCreationContainer.Make(te));
		
		return InteractionResult.SUCCESS;
	}
}
