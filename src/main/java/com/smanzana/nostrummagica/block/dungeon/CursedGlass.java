package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.CursedGlassTileEntity;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.util.WorldUtil.IBlockWalker;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Houses a switch that can only be interacted with once enough damage (possibly of the right type)
 * has been done to the glass that houses it.
 * @author Skyler
 *
 */
public class CursedGlass extends SwitchBlock {
	
	protected static final VoxelShape CURSED_GLASS_AABB = Block.box(0.0D, 0.0D, 0.0D, 16D, 16D, 16D);
	public static final BooleanProperty BROKEN = BooleanProperty.create("broken");
	public static final BooleanProperty DUMMY = BooleanProperty.create("dummy");

	public static final String ID = "cursed_glass";
	
	public CursedGlass() {
		super();
		
		this.registerDefaultState(this.defaultBlockState().setValue(BROKEN, false).setValue(DUMMY, false));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BROKEN, DUMMY);
	}
	
	public void setBrokenState(Level world, BlockPos pos, BlockState state, boolean broken) {
		world.setBlockAndUpdate(pos, state.setValue(BROKEN, broken));
	}
	
	public boolean isBroken(BlockState state) {
		return state.getValue(BROKEN);
	}
	
	public BlockState makeDummy() {
		return this.defaultBlockState().setValue(DUMMY, true);
	}
	
	public boolean isDummy(BlockState state) {
		return state.getValue(DUMMY);
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (!isBroken(state)) {
			return Shapes.block();
		}
		
		// Need a non-empty cube when using dummy selection for world and particle updates
		if (context == CollisionContext.empty()) {
			return Shapes.block();
		}
		
		// If creative, still be full cube even when broken
		if (context instanceof EntityCollisionContext
				&& ((EntityCollisionContext) context).getEntity().isPresent()
				&& ((EntityCollisionContext)context).getEntity().get() instanceof Player
				&& ((Player) ((EntityCollisionContext)context).getEntity().get()).isCreative()) {
			return Shapes.block();
		}
		
		return Shapes.empty();
		//return CURSED_GLASS_AABB;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (isBroken(state)) {
			return super.getCollisionShape(state, worldIn, pos, context);
		}
		return Shapes.block();
	}
	
	@Override
	public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
		return 0;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return isDummy(state) ? null : new CursedGlassTileEntity();
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (!worldIn.isClientSide() && playerIn.isCreative()) {
			if (!isDummy(state)) {
				ItemStack heldItem = playerIn.getItemInHand(hand);
				
				if (!heldItem.isEmpty() && heldItem.getItem() instanceof ArmorItem) {
					BlockEntity te = worldIn.getBlockEntity(pos);
					if (te != null) {
						CursedGlassTileEntity ent = (CursedGlassTileEntity) te;
						ent.setRequiredDamage(ent.getRequiredDamage() + 1f);
						NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
					}
					return InteractionResult.SUCCESS;
				} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof InfusedGemItem) {
					BlockEntity te = worldIn.getBlockEntity(pos);
					if (te != null) {
						CursedGlassTileEntity ent = (CursedGlassTileEntity) te;
						ent.setRequiredElement(InfusedGemItem.GetElement(heldItem));
						NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
					}
					return InteractionResult.SUCCESS;
				} else if (!heldItem.isEmpty() && heldItem.getItem() == Items.GLASS) {
					BlockEntity te = worldIn.getBlockEntity(pos);
					if (te != null) {
						CursedGlassTileEntity ent = (CursedGlassTileEntity) te;
						ent.setNoSwitch(!ent.isNoSwitch());
						NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
					}
					return InteractionResult.SUCCESS;
				}
			} else {
				BlockPos master = findMaster(worldIn, pos, state);
				if (master != null && worldIn.getBlockState(master).getBlock() == this && !master.equals(pos)) {
					return use(worldIn.getBlockState(master), worldIn, master, playerIn, hand, hit);
				}
				
				return InteractionResult.FAIL;
			}
		}
		
		return super.use(state, worldIn, pos, playerIn, hand, hit);
	}
	
	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		for (BlockPos dummyPos : GetArea(pos)) {
			worldIn.setBlockAndUpdate(dummyPos, this.makeDummy());
		}
	}
	
	private static final BlockPos[] GetArea(BlockPos pos) {
		final int radius = 1;
		final int total = 27-1;//((radius*2 + 1) ^ 3) - 1;
		BlockPos[] ret = new BlockPos[total];
		int idx = 0;
		for (int i = -radius; i <= radius; i++)
		for (int j = -radius; j <= radius; j++)
		for (int k = 0; k <= 2*radius; k++) {
			if (i == 0 && j == 0 && k == 0) {
				continue;
			}
			ret[idx++] = pos.offset(i, k, j);
		}
		
		return ret;
	}
	
	public void setBroken(Level world, BlockPos pos, BlockState state) {
		setBrokenState(world, pos, state, true);
		if (isDummy(state)) {
			NostrumMagica.logger.warn("Setting dummy to broken instead of cascading from master...");
		} else {
			for (BlockPos dummyPos : GetArea(pos)) {
				setBrokenState(world, dummyPos, world.getBlockState(dummyPos), true);
			}
		}
	}
	
	protected void destroy(Level world, BlockPos pos, BlockState state) {
		if (state.getBlock() != this) {
			return;
		}
		
		if (!isDummy(state)) {
			for (BlockPos dummyPos : GetArea(pos)) {
				if (world.getBlockState(dummyPos).getBlock() == this) {
					world.destroyBlock(dummyPos, false);
				}
			}
		} else {
			BlockPos master = findMaster(world, pos, state);
			BlockState masterState = world.getBlockState(master);
			if (master != null && masterState.getBlock() == this && !isDummy(masterState)) {
				world.destroyBlock(master, false);
				//destroy(world, master, masterState);
			}
		}
	}
	
	protected @Nullable BlockPos findMaster(Level world, BlockPos startPos, BlockState startState) {
		if (!isDummy(startState)) {
			return startPos;
		}
		
		return WorldUtil.WalkConnectedBlocks(world, startPos, new IBlockWalker() {
			@Override
			public boolean canVisit(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
					BlockState state, int distance) {
				return state.getBlock() == CursedGlass.this;
			}

			@Override
			public boolean walk(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
					BlockState state, int distance, int walkCount) {
				return state.getBlock() == CursedGlass.this && !isDummy(state);
			}
		}, 48);
	}
	
	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		for (BlockPos check : GetArea(pos)) {
			if (!world.isEmptyBlock(check)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		final Level world = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final BlockState state = world.getBlockState(pos);
		if (!canSurvive(state, world, pos)) {
			return null;
		}

		return defaultBlockState();
	}
	
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(worldIn, pos, state);
			worldIn.removeBlockEntity(pos);
		}
	}
	
	public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int id, int param) {
		if (!isDummy(state)) {
			CursedGlassTileEntity tileentity = (CursedGlassTileEntity) worldIn.getBlockEntity(pos);
			return tileentity == null ? false : tileentity.triggerEvent(id, param);
		}
		return false;//return super.eventReceived(state, worldIn, pos, id, param);
	}
}
