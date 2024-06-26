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
	protected static final VoxelShape CANDLE_AABB = Block.makeCuboidShape(16 * 0.4375D, 16 * 0.0D, 16 * 0.4375D, 16 * 0.5625D, 16 * 0.5D, 16 * 0.5625D);
	protected static final VoxelShape CANDLE_E_AABB = Block.makeCuboidShape(16 * 0.0D, 16 * 0.35D, 16 * 0.4375D, 16 * 0.25D, 16 * 0.85D, 16 * 0.5625D);
	protected static final VoxelShape CANDLE_N_AABB = Block.makeCuboidShape(16 * 0.4375D, 16 * 0.35D, 16 * 0.75D, 16 * 0.5625D, 16 * 0.85D, 16D);
	protected static final VoxelShape CANDLE_W_AABB = Block.makeCuboidShape(16 * 0.75D, 16 * 0.35D, 16 * 0.4375D, 16D, 16 * 0.85D, 16 * 0.5625D);
	protected static final VoxelShape CANDLE_S_AABB = Block.makeCuboidShape(16 * 0.4375D, 16 * 0.35D, 0D, 16 * 0.5625D, 16 * 0.85D, 16 * 0.25D);
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing", new Predicate<Direction>() {
		public boolean apply(@Nullable Direction facing) {
			return facing != Direction.DOWN;
		}
	});
	private static final int TICK_DELAY = 5;
	
	public CandleBlock() {
		super(Block.Properties.create(Material.CARPET)
				.hardnessAndResistance(.1f, 10.0f)
				.sound(SoundType.PLANT)
				.setLightLevel((state) -> state == null || !state.get(LIT) ? 0 : 10)
				);
		this.setDefaultState(this.stateContainer.getBaseState().with(LIT, false));
	}
	
	protected boolean isValidPosition(IWorldReader worldIn, BlockPos pos, Direction facing) {
		// copied from WallTorchBlock
		BlockPos blockpos = pos.offset(facing.getOpposite());
		BlockState blockstate = worldIn.getBlockState(blockpos);
		return blockstate.isSolidSide(worldIn, blockpos, facing);
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		return isValidPosition(world, pos, state.get(FACING));
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState blockstate = this.getDefaultState();
		IWorldReader iworldreader = context.getWorld();
		BlockPos blockpos = context.getPos();
		Direction[] adirection = context.getNearestLookingDirections();
		
		for(Direction direction : adirection) {
			direction = direction.getOpposite();
			if (FACING.getAllowedValues().contains(direction)) {
				Direction direction1 = direction;
//				if (Direction.Plane.HORIZONTAL.test(direction1)) {
//					direction1 = direction1.getOpposite();
//				}
				//.getOpposite(); // TODO this didn't used to be opposite?
				blockstate = blockstate.with(FACING, direction1);
				if (blockstate.isValidPosition(iworldreader, blockpos)) {
					return blockstate;
				}
			}
		}

		return null;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction facing = state.get(FACING);
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
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(LIT, FACING);
	}
	
    public static void light(World world, BlockPos pos, BlockState state) {
    	if (!state.get(LIT)) {
	    	world.setBlockState(pos, state.with(LIT, true));
			
			if (!world.getPendingBlockTicks().isTickScheduled(pos, state.getBlock())) {
				world.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), TICK_DELAY);
			}
    	}
    }
    
    public static void extinguish(World world, BlockPos pos, BlockState state) {
    	extinguish(world, pos, state, false);
    }
    
    public static void extinguish(World world, BlockPos pos, BlockState state, boolean force) {
    	
    	if (world.getTileEntity(pos) != null) {
    		world.removeTileEntity(pos);
    		world.notifyBlockUpdate(pos, state, state, 2);
    	}
    	
    	if (!world.isRemote) {
			NetworkHandler.sendToAllAround(new CandleIgniteMessage(world.getDimensionKey(), pos, null),
					new TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 64, world.getDimensionKey()));
		}
    	
    	if (!force && CandleBlock.IsCandleEnhanced(world, pos)) {
    		if (!world.getPendingBlockTicks().isTickScheduled(pos, state.getBlock())) {
				world.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), TICK_DELAY);
			}
			return;
    	}
    	
    	world.setBlockState(pos, state.with(LIT, false));
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
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (newState.getBlock() != state.getBlock()) {
			super.onReplaced(state, world, pos, newState, isMoving);
			
			TileEntity ent = world.getTileEntity(pos);
			if (ent == null)
				return;
			
			world.removeTileEntity(pos);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		
		if (null == stateIn || !stateIn.get(LIT))
			return;
		
		Direction facing = stateIn.get(FACING);
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
		if (state.get(LIT) && worldIn.getTileEntity(pos) == null) {
			List<ItemEntity> items = worldIn.getEntitiesWithinAABB(ItemEntity.class, VoxelShapes.fullCube().getBoundingBox().offset(pos).expand(0, 1, 0));
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
			
			if (!worldIn.getPendingBlockTicks().isTickScheduled(pos, this)) {
				worldIn.getPendingBlockTicks().scheduleTick(pos, this, TICK_DELAY);
			}
		}
		
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {

		if (worldIn.isRemote())
			return ActionResultType.SUCCESS;
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		if (!state.get(LIT)) {
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
			
			if (hand == Hand.MAIN_HAND && (playerIn.getHeldItemMainhand().isEmpty())) {
				// putting it out
				extinguish(worldIn, pos, state, true);
				return ActionResultType.SUCCESS;
			}
			
			return ActionResultType.FAIL;
		}

		if (!(heldItem.getItem() instanceof ReagentItem))
			return ActionResultType.FAIL;
		
		TileEntity te = worldIn.getTileEntity(pos);
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
		if (world.isRemote && type == null) {
			extinguish(world, pos, state, false);
			return;
		}
		
		light(world, pos, state);
		
		CandleTileEntity candle = null;
		if (world.getTileEntity(pos) != null) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof CandleTileEntity) {
				candle = (CandleTileEntity) te;
			} else {
				world.removeTileEntity(pos);
			}
		}
		
		if (candle == null) {
			candle = new CandleTileEntity(type);
			world.setTileEntity(pos, candle);
		}
		
		candle.setReagentType(type);
		
		if (!world.isRemote) {
			NetworkHandler.sendToAllAround(new CandleIgniteMessage(world.getDimensionKey(), pos, type),
					new TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 64, world.getDimensionKey()));
		}
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if (!isValidPosition(worldIn, pos, state.get(FACING))) {
			worldIn.destroyBlock(pos, true);
		}
	}
	
	public static boolean IsCandleEnhancingBlock(World world, BlockPos pos, BlockState state) {
		return state.getBlock().isFireSource(state, world, pos, Direction.UP);
	}
	
	public static boolean IsCandleEnhanced(World world, BlockPos candlePos) {
		BlockPos[] positions = {
				candlePos.down(),
				candlePos.down().down(),
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
