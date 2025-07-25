package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.LaserBlockEntity;
import com.smanzana.nostrummagica.tile.NostrumBlockEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LaserBlock extends DirectionalBlock implements EntityBlock, ITriggeredBlock {

	public static final String ID = "mechblock_laser";
	
	public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
	
	private static final float AABB_BASE_WIDTH = 2.0f;
	private static final float AABB_BASE_LEN = 8.0f;
	private static final float AABB_GEM_MARGIN = 5.0f;
	
	private static final VoxelShape GEM = Block.box(AABB_GEM_MARGIN, AABB_GEM_MARGIN, AABB_GEM_MARGIN, 16 - AABB_GEM_MARGIN, 16 - AABB_GEM_MARGIN, 16 - AABB_GEM_MARGIN);
	private static final VoxelShape BASE_HOLLOW = Block.box(AABB_BASE_WIDTH, AABB_BASE_WIDTH, AABB_BASE_WIDTH, 16 - AABB_BASE_WIDTH, 16 - AABB_BASE_WIDTH, 16 - AABB_BASE_WIDTH);
	
	
	private static final VoxelShape SHAPE_U = Shapes.or(GEM, Shapes.join(Block.box(0, 0, 0, 16, AABB_BASE_LEN, 16), BASE_HOLLOW, BooleanOp.ONLY_FIRST));
	private static final VoxelShape SHAPE_D = Shapes.or(GEM, Shapes.join(Block.box(0, 16 - AABB_BASE_LEN, 0, 16, 16, 16), BASE_HOLLOW, BooleanOp.ONLY_FIRST));
	private static final VoxelShape SHAPE_N = Shapes.or(GEM, Shapes.join(Block.box(0, 0, 16 - AABB_BASE_LEN, 16, 16, 16), BASE_HOLLOW, BooleanOp.ONLY_FIRST));
	private static final VoxelShape SHAPE_S = Shapes.or(GEM, Shapes.join(Block.box(0, 0, 0, 16, 16, AABB_BASE_LEN), BASE_HOLLOW, BooleanOp.ONLY_FIRST));
	
	private static final VoxelShape SHAPE_E = Shapes.or(GEM, Shapes.join(Block.box(0, 0, 0, AABB_BASE_LEN, 16, 16), BASE_HOLLOW, BooleanOp.ONLY_FIRST));
	private static final VoxelShape SHAPE_W = Shapes.or(GEM, Shapes.join(Block.box(16 - AABB_BASE_LEN, 0, 0, 16, 16, 16), BASE_HOLLOW, BooleanOp.ONLY_FIRST));
	
	
	public LaserBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(-1.0F, 3600000.8F)
				.sound(SoundType.STONE)
				.noDrops()
			);
			
			this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(ENABLED, true));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ENABLED, FACING);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		switch (state.getValue(FACING)) {
		case NORTH:
			return SHAPE_N;
		case EAST:
			return SHAPE_E;
		case SOUTH:
			return SHAPE_S;
		case WEST:
			return SHAPE_W;
		case UP:
			return SHAPE_U;
		case DOWN:
		default:
			return SHAPE_D;
		}
	}
	
	public Direction getFacing(BlockState state) {
		return state.getValue(FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		// Want to point towards the block we clicked
		final Level world = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		Direction facing = context.getClickedFace();
		if (!this.canPlaceAt(world, pos, facing) && facing.get3DDataValue() > 1) {
			// Rotate and find it
			for (int i = 0; i < 3; i++) {
				facing = facing.getClockWise();
				if (this.canPlaceAt(world, pos, facing)) {
					break;
				}
			}
		}
		
		return this.defaultBlockState()
				.setValue(FACING, facing);
	}
	
	protected boolean canPlaceAt(LevelReader worldIn, BlockPos pos, Direction side) {
		BlockState state = worldIn.getBlockState(pos.relative(side.getOpposite()));
		if (state == null || !(state.getMaterial().blocksMotion())) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean canSurvive(BlockState stateIn, LevelReader worldIn, BlockPos pos) {
		for (Direction side : Direction.values()) {
			if (canPlaceAt(worldIn, pos, side)) {
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos posFrom, boolean isMoving) {
		Direction face = state.getValue(FACING);
		if (!canPlaceAt(worldIn, pos, face)) {
			worldIn.removeBlock(pos, true);
		} else {
			;
		}
		
		super.neighborChanged(state, worldIn, posFrom, blockIn, posFrom, isMoving);
	}
	
	protected @Nullable LaserBlockEntity getEntity(Level level, BlockState state, BlockPos pos) {
		return (LaserBlockEntity) level.getBlockEntity(pos);
	}
	
	protected void setLaserEnabled(LaserBlockEntity ent, boolean enabled) {
		if (!enabled) {
			ent.setToggleMode(false);
			ent.setEnabled(false);
		} else {
			ent.setToggleMode(true);
			ent.setEnabled(true);
		}
	}
	
	protected boolean isLaserEnabled(LaserBlockEntity ent) {
		return ent.getEnabled();
	}
	
	protected void toggleLaser(Level worldIn, BlockState state, BlockPos pos) {
		LaserBlockEntity ent = getEntity(worldIn, state, pos);
		setLaserEnabled(ent, !ent.getEnabled());
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (!playerIn.isCreative()) {
			return InteractionResult.PASS;
		}
		
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		
		if (worldIn.isClientSide) {
			return InteractionResult.PASS;
		}
		
		final ItemStack heldItem = playerIn.getItemInHand(hand);
		if (heldItem.isEmpty()) {
			if (playerIn.isCrouching()) {
				toggleLaser(worldIn, state, pos);
			}
			return InteractionResult.SUCCESS;
		} else if (heldItem.getItem() instanceof InfusedGemItem gem) {
			final EMagicElement element = gem.getElement();
			getEntity(worldIn, state, pos).setElement(element);
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.PASS;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LaserBlockEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumBlockEntities.Laser);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public void trigger(Level world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		toggleLaser(world, state, blockPos);
	}
	
	public void setLaserState(Level world, BlockPos pos, BlockState state, boolean enabled) {
		LaserBlockEntity ent = getEntity(world, state, pos);
		if (ent != null) {
			this.setLaserEnabled(ent, enabled);
		}
	}
	
	public boolean isLaserEnabled(Level worldIn, BlockState state, BlockPos pos) {
		LaserBlockEntity ent = getEntity(worldIn, state, pos);
		return isLaserEnabled(ent);
	}
	
	public EMagicElement getLaserElement(Level world, BlockPos pos, BlockState state) {
		LaserBlockEntity ent = getEntity(world, state, pos);
		return ent != null ? ent.getElement() : null;
	}

}
