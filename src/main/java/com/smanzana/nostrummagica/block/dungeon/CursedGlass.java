package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.CursedGlassTileEntity;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.util.WorldUtil.IBlockWalker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Houses a switch that can only be interacted with once enough damage (possibly of the right type)
 * has been done to the glass that houses it.
 * @author Skyler
 *
 */
public class CursedGlass extends SwitchBlock {
	
	protected static final VoxelShape CURSED_GLASS_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16D, 16D, 16D);
	public static final BooleanProperty BROKEN = BooleanProperty.create("broken");
	public static final BooleanProperty DUMMY = BooleanProperty.create("dummy");

	public static final String ID = "cursed_glass";
	
	public CursedGlass() {
		super();
		
		this.setDefaultState(this.getDefaultState().with(BROKEN, false).with(DUMMY, false));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(BROKEN, DUMMY);
	}
	
	public void setBrokenState(World world, BlockPos pos, BlockState state, boolean broken) {
		world.setBlockState(pos, state.with(BROKEN, broken));
	}
	
	public boolean isBroken(BlockState state) {
		return state.get(BROKEN);
	}
	
	public BlockState makeDummy() {
		return this.getDefaultState().with(DUMMY, true);
	}
	
	public boolean isDummy(BlockState state) {
		return state.get(DUMMY);
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (!isBroken(state)) {
			return VoxelShapes.fullCube();
		}
		
		// Need a non-empty cube when using dummy selection for world and particle updates
		if (context == ISelectionContext.dummy()) {
			return VoxelShapes.fullCube();
		}
		
		// If creative, still be full cube even when broken
		if (context.getEntity() != null && context.getEntity() instanceof PlayerEntity && ((PlayerEntity) context.getEntity()).isCreative()) {
			return VoxelShapes.fullCube();
		}
		
		return VoxelShapes.empty();
		//return CURSED_GLASS_AABB;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (isBroken(state)) {
			return super.getCollisionShape(state, worldIn, pos, context);
		}
		return VoxelShapes.fullCube();
	}
	
	@Override
	public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
		return 0;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return !isDummy(state);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return isDummy(state) ? null : new CursedGlassTileEntity();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (!worldIn.isRemote() && playerIn.isCreative()) {
			if (!isDummy(state)) {
				ItemStack heldItem = playerIn.getHeldItem(hand);
				
				if (!heldItem.isEmpty() && heldItem.getItem() instanceof ArmorItem) {
					TileEntity te = worldIn.getTileEntity(pos);
					if (te != null) {
						CursedGlassTileEntity ent = (CursedGlassTileEntity) te;
						ent.setRequiredDamage(ent.getRequiredDamage() + 1f);
						NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
					}
					return ActionResultType.SUCCESS;
				} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof InfusedGemItem) {
					TileEntity te = worldIn.getTileEntity(pos);
					if (te != null) {
						CursedGlassTileEntity ent = (CursedGlassTileEntity) te;
						ent.setRequiredElement(InfusedGemItem.GetElement(heldItem));
						NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
					}
					return ActionResultType.SUCCESS;
				}
			} else {
				BlockPos master = findMaster(worldIn, pos, state);
				if (master != null && worldIn.getBlockState(master).getBlock() == this) {
					return onBlockActivated(worldIn.getBlockState(master), worldIn, master, playerIn, hand, hit);
				}
				
				return ActionResultType.FAIL;
			}
		}
		
		return super.onBlockActivated(state, worldIn, pos, playerIn, hand, hit);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		for (BlockPos dummyPos : GetArea(pos)) {
			worldIn.setBlockState(dummyPos, this.makeDummy());
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
			ret[idx++] = pos.add(i, k, j);
		}
		
		return ret;
	}
	
	public void setBroken(World world, BlockPos pos, BlockState state) {
		setBrokenState(world, pos, state, true);
		if (isDummy(state)) {
			NostrumMagica.logger.warn("Setting dummy to broken instead of cascading from master...");
		} else {
			for (BlockPos dummyPos : GetArea(pos)) {
				setBrokenState(world, dummyPos, world.getBlockState(dummyPos), true);
			}
		}
	}
	
	protected void destroy(World world, BlockPos pos, BlockState state) {
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
	
	protected @Nullable BlockPos findMaster(World world, BlockPos startPos, BlockState startState) {
		if (!isDummy(startState)) {
			return startPos;
		}
		
		return WorldUtil.WalkConnectedBlocks(world, startPos, new IBlockWalker() {
			@Override
			public boolean canVisit(IBlockReader world, BlockPos startPos, BlockState startState, BlockPos pos,
					BlockState state, int distance) {
				return state.getBlock() == CursedGlass.this;
			}

			@Override
			public boolean walk(IBlockReader world, BlockPos startPos, BlockState startState, BlockPos pos,
					BlockState state, int distance, int walkCount) {
				return state.getBlock() == CursedGlass.this && !isDummy(state);
			}
		}, 48);
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		for (BlockPos check : GetArea(pos)) {
			if (!world.isAirBlock(check)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		final World world = context.getWorld();
		final BlockPos pos = context.getPos();
		final BlockState state = world.getBlockState(pos);
		if (!isValidPosition(state, world, pos)) {
			return null;
		}

		return getDefaultState();
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(worldIn, pos, state);
			worldIn.removeTileEntity(pos);
		}
	}
	
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		if (!isDummy(state)) {
			CursedGlassTileEntity tileentity = (CursedGlassTileEntity) worldIn.getTileEntity(pos);
			return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
		}
		return false;//return super.eventReceived(state, worldIn, pos, id, param);
	}
}
