package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.CursedGlassTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
		return !state.get(DUMMY);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return state.get(DUMMY) ? null : new CursedGlassTileEntity();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (!worldIn.isRemote() && playerIn.isCreative() && !state.get(DUMMY)) {
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
		return new BlockPos[] {pos.south(), pos.east(), pos.south().east(), pos.up(), pos.up().south(), pos.up().east(), pos.up().south().east()};
	}
	
	public void setBroken(World world, BlockPos pos, BlockState state) {
		setBrokenState(world, pos, state, true);
		if (state.get(DUMMY)) {
			NostrumMagica.logger.warn("Setting dummy to broken instead of cascading from master...");
		} else {
			for (BlockPos dummyPos : GetArea(pos)) {
				setBrokenState(world, dummyPos, world.getBlockState(dummyPos), true);
			}
		}
	}
	
	protected void destroy(World world, BlockPos pos, BlockState state) {
		if (!state.get(DUMMY)) {
			for (BlockPos dummyPos : GetArea(pos)) {
				if (world.getBlockState(dummyPos).getBlock() == this) {
					world.setBlockState(dummyPos, Blocks.AIR.getDefaultState(), 3);
				}
			}
		}
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
		if (!state.get(DUMMY)) {
			CursedGlassTileEntity tileentity = (CursedGlassTileEntity) worldIn.getTileEntity(pos);
			return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
		}
		return false;//return super.eventReceived(state, worldIn, pos, id, param);
	}
}
