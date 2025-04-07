package com.smanzana.nostrummagica.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.CandleIgniteMessage;
import com.smanzana.nostrummagica.tile.CandleTileEntity;
import com.smanzana.nostrummagica.tile.NostrumTileEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.PacketDistributor.TargetPoint;

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
	private static final int TICK_DELAY = 5;
	
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
			
			if (!world.getBlockTicks().hasScheduledTick(pos, state.getBlock())) {
				world.getBlockTicks().scheduleTick(pos, state.getBlock(), TICK_DELAY);
			}
    	}
    }
    
    public static void extinguish(Level world, BlockPos pos, BlockState state) {
    	extinguish(world, pos, state, false);
    }
    
    public static void extinguish(Level world, BlockPos pos, BlockState state, boolean force) {
    	
    	if (world.getBlockEntity(pos) != null) {
    		world.removeBlockEntity(pos);
    		world.sendBlockUpdated(pos, state, state, 2);
    	}
    	
    	if (!world.isClientSide) {
			NetworkHandler.sendToAllAround(new CandleIgniteMessage(world.dimension(), pos, null),
					new TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 64, world.dimension()));
		}
    	
    	if (!force && CandleBlock.IsCandleEnhanced(world, pos)) {
    		if (!world.getBlockTicks().hasScheduledTick(pos, state.getBlock())) {
				world.getBlockTicks().scheduleTick(pos, state.getBlock(), TICK_DELAY);
			}
			return;
    	}
    	
    	world.setBlockAndUpdate(pos, state.setValue(LIT, false));
    }

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return null;
		
		// We don't create when the block is placed.
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumTileEntities.CandleTileEntityType);
	}
	
//	@Override
//	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
//		super.onBlockAdded(worldIn, pos, state);
//	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (newState.getBlock() != state.getBlock()) {
			super.onRemove(state, world, pos, newState, isMoving);
			
			BlockEntity ent = world.getBlockEntity(pos);
			if (ent == null)
				return;
			
			world.removeBlockEntity(pos);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		
		if (null == stateIn || !stateIn.getValue(LIT))
			return;
		
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
        worldIn.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
	}
	
	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
		// Check for a reagent item over the candle
		if (state.getValue(LIT) && worldIn.getBlockEntity(pos) == null) {
			List<ItemEntity> items = worldIn.getEntitiesOfClass(ItemEntity.class, Shapes.block().bounds().move(pos).expandTowards(0, 1, 0));
			if (items != null && !items.isEmpty()) {
				for (ItemEntity item : items) {
					ItemStack stack = item.getItem();
					if (stack.getItem() instanceof ReagentItem) {
						ReagentType type = ReagentItem.FindType(stack.split(1));
						if (type != null) {
							setReagent(worldIn, pos, state, type);
						}
						
						if (stack.getCount() <= 0) {
							item.discard();
						}
						
						break;
					}
				}
			}
			
			if (!worldIn.getBlockTicks().hasScheduledTick(pos, this)) {
				worldIn.getBlockTicks().scheduleTick(pos, this, TICK_DELAY);
			}
		}
		
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
				extinguish(worldIn, pos, state, true);
				return InteractionResult.SUCCESS;
			}
			
			return InteractionResult.FAIL;
		}

		if (!(heldItem.getItem() instanceof ReagentItem))
			return InteractionResult.FAIL;
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te != null)
			return InteractionResult.FAIL;
		
		ReagentType type = ReagentItem.FindType(heldItem);
		heldItem.split(1);
		
		if (type == null)
			return InteractionResult.FAIL;
		
		setReagent(worldIn, pos, state, type);
		
		return InteractionResult.SUCCESS;
	}
	
	public static void setReagent(Level world, BlockPos pos, BlockState state, ReagentType type) {
		if (world.isClientSide && type == null) {
			extinguish(world, pos, state, false);
			return;
		}
		
		light(world, pos, state);
		
		CandleTileEntity candle = null;
		if (world.getBlockEntity(pos) != null) {
			BlockEntity te = world.getBlockEntity(pos);
			if (te instanceof CandleTileEntity) {
				candle = (CandleTileEntity) te;
			} else {
				world.removeBlockEntity(pos);
			}
		}
		
		if (candle == null) {
			candle = new CandleTileEntity(type, pos, state);
			world.setBlockEntity(candle);
		}
		
		candle.setReagentType(type);
		
		if (!world.isClientSide) {
			NetworkHandler.sendToAllAround(new CandleIgniteMessage(world.dimension(), pos, type),
					new TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 64, world.dimension()));
		}
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
