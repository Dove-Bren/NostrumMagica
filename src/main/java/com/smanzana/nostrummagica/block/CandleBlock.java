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
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;

public class CandleBlock extends Block {

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
	
	protected boolean isValidPosition(IWorldReader worldIn, BlockPos pos, Direction facing) {
		// copied from WallTorchBlock
		BlockPos blockpos = pos.relative(facing.getOpposite());
		BlockState blockstate = worldIn.getBlockState(blockpos);
		return blockstate.isFaceSturdy(worldIn, blockpos, facing);
	}
	
	@Override
	public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
		return isValidPosition(world, pos, state.getValue(FACING));
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState blockstate = this.defaultBlockState();
		IWorldReader iworldreader = context.getLevel();
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
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
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
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(LIT, FACING);
	}
	
    public static void light(World world, BlockPos pos, BlockState state) {
    	if (!state.getValue(LIT)) {
	    	world.setBlockAndUpdate(pos, state.setValue(LIT, true));
			
			if (!world.getBlockTicks().hasScheduledTick(pos, state.getBlock())) {
				world.getBlockTicks().scheduleTick(pos, state.getBlock(), TICK_DELAY);
			}
    	}
    }
    
    public static void extinguish(World world, BlockPos pos, BlockState state) {
    	extinguish(world, pos, state, false);
    }
    
    public static void extinguish(World world, BlockPos pos, BlockState state, boolean force) {
    	
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
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return null;
		
		// We don't create when the block is placed.
	}
	
//	@Override
//	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
//		super.onBlockAdded(worldIn, pos, state);
//	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (newState.getBlock() != state.getBlock()) {
			super.onRemove(state, world, pos, newState, isMoving);
			
			TileEntity ent = world.getBlockEntity(pos);
			if (ent == null)
				return;
			
			world.removeBlockEntity(pos);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean triggerEvent(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.triggerEvent(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity == null ? false : tileentity.triggerEvent(id, param);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		
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
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		// Check for a reagent item over the candle
		if (state.getValue(LIT) && worldIn.getBlockEntity(pos) == null) {
			List<ItemEntity> items = worldIn.getEntitiesOfClass(ItemEntity.class, VoxelShapes.block().bounds().move(pos).expandTowards(0, 1, 0));
			if (items != null && !items.isEmpty()) {
				for (ItemEntity item : items) {
					ItemStack stack = item.getItem();
					if (stack.getItem() instanceof ReagentItem) {
						ReagentType type = ReagentItem.FindType(stack.split(1));
						if (type != null) {
							setReagent(worldIn, pos, state, type);
						}
						
						if (stack.getCount() <= 0) {
							item.remove();
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
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {

		if (worldIn.isClientSide())
			return ActionResultType.SUCCESS;
		
		ItemStack heldItem = playerIn.getItemInHand(hand);
		
		if (!state.getValue(LIT)) {
			if (heldItem.isEmpty())
				return ActionResultType.FAIL;
			
			if (heldItem.getItem() instanceof FlintAndSteelItem) {
				light(worldIn, pos, state);
				ItemStacks.damageItem(heldItem, playerIn, hand, 1);
				return ActionResultType.SUCCESS;
			}
			
			return ActionResultType.FAIL;
		}
		
		// it's lit
		if (heldItem.isEmpty()) {
			// only if mainhand or mainhand is null. Otherwise if offhand is
			// empty, will still put out. Dumb!
			
			if (hand == Hand.MAIN_HAND && (playerIn.getMainHandItem().isEmpty())) {
				// putting it out
				extinguish(worldIn, pos, state, true);
				return ActionResultType.SUCCESS;
			}
			
			return ActionResultType.FAIL;
		}

		if (!(heldItem.getItem() instanceof ReagentItem))
			return ActionResultType.FAIL;
		
		TileEntity te = worldIn.getBlockEntity(pos);
		if (te != null)
			return ActionResultType.FAIL;
		
		ReagentType type = ReagentItem.FindType(heldItem);
		heldItem.split(1);
		
		if (type == null)
			return ActionResultType.FAIL;
		
		setReagent(worldIn, pos, state, type);
		
		return ActionResultType.SUCCESS;
	}
	
	public static void setReagent(World world, BlockPos pos, BlockState state, ReagentType type) {
		if (world.isClientSide && type == null) {
			extinguish(world, pos, state, false);
			return;
		}
		
		light(world, pos, state);
		
		CandleTileEntity candle = null;
		if (world.getBlockEntity(pos) != null) {
			TileEntity te = world.getBlockEntity(pos);
			if (te instanceof CandleTileEntity) {
				candle = (CandleTileEntity) te;
			} else {
				world.removeBlockEntity(pos);
			}
		}
		
		if (candle == null) {
			candle = new CandleTileEntity(type);
			world.setBlockEntity(pos, candle);
		}
		
		candle.setReagentType(type);
		
		if (!world.isClientSide) {
			NetworkHandler.sendToAllAround(new CandleIgniteMessage(world.dimension(), pos, type),
					new TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 64, world.dimension()));
		}
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if (!isValidPosition(worldIn, pos, state.getValue(FACING))) {
			worldIn.destroyBlock(pos, true);
		}
	}
	
	public static boolean IsCandleEnhancingBlock(World world, BlockPos pos, BlockState state) {
		return state.getBlock().isFireSource(state, world, pos, Direction.UP);
	}
	
	public static boolean IsCandleEnhanced(World world, BlockPos candlePos) {
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
