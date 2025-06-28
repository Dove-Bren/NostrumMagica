package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.capabilities.ILaserReactive;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.LaserTriggerBlockEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LaserTrigger extends DirectionalBlock implements EntityBlock, ILaserReactive {
	
	public static final String ID = "laser_trigger";
	
	public static final BooleanProperty TRIGGERED = BooleanProperty.create("triggered");
	
	private static final double SENSOR_HEIGHT = 6.0; // Copied from Daylight Detector shape definitions
	
	protected static final VoxelShape U_AABB = Block.box(0, 0, 0, 16, SENSOR_HEIGHT, 16);
	protected static final VoxelShape D_AABB = Block.box(0, 16 - SENSOR_HEIGHT, 0, 16, 16, 16);
	protected static final VoxelShape E_AABB = Block.box(0, 0, 0, SENSOR_HEIGHT, 16, 16);
	protected static final VoxelShape W_AABB = Block.box(16 - SENSOR_HEIGHT, 0, 0, 16, 16, 16);
	protected static final VoxelShape S_AABB = Block.box(0, 0, 0, 16, 16, SENSOR_HEIGHT);
	protected static final VoxelShape N_AABB = Block.box(0, 0, 16 - SENSOR_HEIGHT, 16, 16, 16);
	
	
	public LaserTrigger() {
		super(Block.Properties.copy(Blocks.DAYLIGHT_DETECTOR));
		
		this.registerDefaultState(defaultBlockState().setValue(TRIGGERED, false));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, TRIGGERED);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		Direction facing = state.getValue(FACING);
		switch (facing) {
		case EAST:
			return E_AABB;
		case NORTH:
			return N_AABB;
		case SOUTH:
			return S_AABB;
		case WEST:
			return W_AABB;
		case UP:
		default:
			return U_AABB;
		case DOWN:
			return D_AABB;
		}
		
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (worldIn.isClientSide() || !player.isCreative()) {
			return player.isCreative() ? InteractionResult.SUCCESS : InteractionResult.FAIL; // in creative, we still want the client to think we ate the interact
		}
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof LaserTriggerBlockEntity ent)) {
			return InteractionResult.FAIL;
		}
		
		ItemStack heldItem = player.getItemInHand(handIn);
		
		if (heldItem.isEmpty()) {
			if (state.getValue(TRIGGERED)) {
				worldIn.setBlockAndUpdate(pos, state.setValue(TRIGGERED, false));
			}
		} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof PositionCrystal) {
			BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
			if (heldPos != null && DimensionUtils.DimEquals(PositionCrystal.getDimension(heldItem), worldIn.dimension())) {
				ent.addTriggerPoint(heldPos, false);
				player.sendMessage(new TextComponent("Set offset to " + heldPos), Util.NIL_UUID);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LaserTriggerBlockEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return null;
	}

	@Override
	public LaserHitResult laserPassthroughTick(LevelAccessor level, BlockPos pos, BlockState state, BlockPos laserPos,
			EMagicElement element) {
		if (!state.getValue(TRIGGERED)) {
			BlockEntity te = level.getBlockEntity(pos);
			if (te != null && te instanceof LaserTriggerBlockEntity ent) {
				ent.trigger(laserPos);
			}
			level.setBlock(pos, state.setValue(TRIGGERED, true), 3);
		}
		return LaserHitResult.PASSTHROUGH;
	}

	@Override
	public void laserNearbyTick(LevelAccessor level, BlockPos pos, BlockState state, BlockPos laserPos, EMagicElement element, int beamDistance) {
		;
	}
}
