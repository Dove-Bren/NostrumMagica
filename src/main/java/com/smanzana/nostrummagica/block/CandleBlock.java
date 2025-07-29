package com.smanzana.nostrummagica.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.tile.CandleTileEntity;
import com.smanzana.nostrummagica.tile.NostrumBlockEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CandleBlock extends BaseEntityBlock {

	public static final String ID = "nostrum_candle";
	public static BooleanProperty LIT = BooleanProperty.create("lit");
	protected static final VoxelShape CANDLE_AABB = Block.box(16 * 0.4375D, 16 * 0.0D, 16 * 0.4375D, 16 * 0.5625D, 16 * 0.5D, 16 * 0.5625D);
	protected static final VoxelShape CANDLE_E_AABB = Block.box(16 * 0.0D, 16 * 0.35D, 16 * 0.4375D, 16 * 0.25D, 16 * 0.85D, 16 * 0.5625D);
	protected static final VoxelShape CANDLE_N_AABB = Block.box(16 * 0.4375D, 16 * 0.35D, 16 * 0.75D, 16 * 0.5625D, 16 * 0.85D, 16D);
	protected static final VoxelShape CANDLE_W_AABB = Block.box(16 * 0.75D, 16 * 0.35D, 16 * 0.4375D, 16D, 16 * 0.85D, 16 * 0.5625D);
	protected static final VoxelShape CANDLE_S_AABB = Block.box(16 * 0.4375D, 16 * 0.35D, 0D, 16 * 0.5625D, 16 * 0.85D, 16 * 0.25D);
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing", new Predicate<Direction>() {
		public boolean apply(@Nullable Direction facing) {
			return facing != Direction.DOWN;
		}
	});
	
	public CandleBlock() {
		super(Block.Properties.of(Material.CLOTH_DECORATION)
				.strength(.1f, 10.0f)
				.sound(SoundType.GRASS)
				.lightLevel((state) -> state == null || !state.getValue(LIT) ? 0 : 10)
				);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
	}
	
	protected boolean isValidPosition(LevelReader worldIn, BlockPos pos, Direction facing) {
		// copied from WallTorchBlock
		BlockPos blockpos = pos.relative(facing.getOpposite());
		BlockState blockstate = worldIn.getBlockState(blockpos);
		return blockstate.isFaceSturdy(worldIn, blockpos, facing);
	}
	
	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		return isValidPosition(world, pos, state.getValue(FACING));
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState blockstate = this.defaultBlockState();
		LevelReader iworldreader = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		Direction[] adirection = context.getNearestLookingDirections();
		
		if (context.getPlayer() != null) {
			final InteractionHand other = context.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
			final ItemStack stack = context.getPlayer().getItemInHand(other);
			if (!stack.isEmpty() && stack.getItem() == Items.FLINT_AND_STEEL) {
				blockstate = blockstate.setValue(LIT, true);
			}
		}
		
		for(Direction direction : adirection) {
			direction = direction.getOpposite();
			if (FACING.getPossibleValues().contains(direction)) {
				Direction direction1 = direction;
//				if (Direction.Plane.HORIZONTAL.test(direction1)) {
//					direction1 = direction1.getOpposite();
//				}
				//.getOpposite(); // TODO this didn't used to be opposite?
				blockstate = blockstate.setValue(FACING, direction1);
				if (blockstate.canSurvive(iworldreader, blockpos)) {
					return blockstate;
				}
			}
		}

		return null;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		Direction facing = state.getValue(FACING);
		switch (facing) {
		case EAST:
			return CANDLE_E_AABB;
		case NORTH:
			return CANDLE_N_AABB;
		case SOUTH:
			return CANDLE_S_AABB;
		case WEST:
			return CANDLE_W_AABB;
		case UP:
		case DOWN:
		default:
			return CANDLE_AABB;
		}
		
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, FACING);
	}
	
    public static void light(Level world, BlockPos pos, BlockState state) {
    	if (!state.getValue(LIT)) {
	    	world.setBlockAndUpdate(pos, state.setValue(LIT, true));
    	}
    }
    
    public static void extinguish(Level world, BlockPos pos, BlockState state) {
    	world.setBlockAndUpdate(pos, state.setValue(LIT, false));
    	
    	if (world instanceof ServerLevel serverLevel) {
	    	serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + .5, pos.getY() + .6, pos.getZ() + .5,
					5, 0, .02, 0, .01);
			serverLevel.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1f, 1f);
    	}
    }

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CandleTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createServerTickerHelper(world, type, NostrumBlockEntities.Candle);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		
		if (null == stateIn || !stateIn.getValue(LIT))
			return;
		
		final boolean hasReagent;
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof CandleTileEntity candleEnt)) {
			hasReagent = false;
		} else {
			hasReagent = candleEnt.getReagentType() != null;
		}
		
		Direction facing = stateIn.getValue(FACING);
		double d0 = (double)pos.getX() + 0.5D;
		double d1 = (double)pos.getY() + 0.6D;
		double d2 = (double)pos.getZ() + 0.5D;
		
		final double hangOffset = .3;
		
		switch (facing) {
		case EAST:
			d0 -= hangOffset;
			d1 += .35;
	        break;
		case NORTH:
			d2 += hangOffset;
			d1 += .35;
	        break;
		case SOUTH:
	        d2 -= hangOffset;
			d1 += .35;
	        break;
		case WEST:
			d0 += hangOffset;
			d1 += .35;
	        break;
		case UP:
		case DOWN:
		default:
	        break;
		}
		
        worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        worldIn.addParticle(hasReagent ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {

		if (worldIn.isClientSide())
			return InteractionResult.SUCCESS;
		
		ItemStack heldItem = playerIn.getItemInHand(hand);
		
		if (!state.getValue(LIT)) {
			if (heldItem.isEmpty())
				return InteractionResult.FAIL;
			
			if (heldItem.getItem() instanceof FlintAndSteelItem) {
				light(worldIn, pos, state);
				ItemStacks.damageItem(heldItem, playerIn, hand, 1);
				return InteractionResult.SUCCESS;
			}
			
			return InteractionResult.FAIL;
		}
		
		// it's lit
		if (heldItem.isEmpty()) {
			// only if mainhand or mainhand is null. Otherwise if offhand is
			// empty, will still put out. Dumb!
			
			if (hand == InteractionHand.MAIN_HAND && (playerIn.getMainHandItem().isEmpty())) {
				// putting it out
				extinguish(worldIn, pos, state);
				return InteractionResult.SUCCESS;
			}
			
			return InteractionResult.FAIL;
		}

		if (!(heldItem.getItem() instanceof ReagentItem reagentItem))
			return InteractionResult.FAIL;
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof CandleTileEntity candleEnt))
			return InteractionResult.FAIL;
		
		ReagentType type = reagentItem.getType();
		if (!candleEnt.tryAddReagent(type)) {
			return InteractionResult.FAIL;
		}
		
		heldItem.split(1);
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if (!isValidPosition(worldIn, pos, state.getValue(FACING))) {
			worldIn.destroyBlock(pos, true);
		}
	}
	
	public static boolean IsCandleEnhancingBlock(Level world, BlockPos pos, BlockState state) {
		return state.getBlock().isFireSource(state, world, pos, Direction.UP);
	}
	
	public static boolean IsCandleEnhanced(Level world, BlockPos candlePos) {
		BlockPos[] positions = {
				candlePos.below(),
				candlePos.below().below(),
			};
			
		for (BlockPos cursor: positions) {
			BlockState state = world.getBlockState(cursor);
			if (state != null && CandleBlock.IsCandleEnhancingBlock(world, cursor, state)) {
				return true;
			}
		}
		
		return false;
	}
	
}
