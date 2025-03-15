package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.tile.LockedChestTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class LockedChestBlock extends HorizontalBlock {
	
	public static final String ID = "locked_chest";
	public static DirectionProperty FACING = HorizontalBlock.FACING;
	public static BooleanProperty UNLOCKABLE = BooleanProperty.create("unlockable");
	protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
	
	public LockedChestBlock() {
		super(Block.Properties.of(Material.WOOD)
				.sound(SoundType.WOOD)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				);
		
		this.registerDefaultState(this.defaultBlockState().setValue(UNLOCKABLE, false));
	}
	
	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING, UNLOCKABLE);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isClientSide()) {
			LockedChestTileEntity chest = (LockedChestTileEntity) worldIn.getBlockEntity(pos);
			
			// Creative players can dye it
			if (player.isCreative() && !player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() instanceof DyeItem) {
				DyeItem dye = (DyeItem) player.getMainHandItem().getItem();
				chest.setColor(dye.getDyeColor());
			} else {
				chest.attemptUnlock(player);
			}
		}
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new LockedChestTileEntity();
	}
	
	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			// Skip dropping items if we're changing to a chest to avoid having to take a copy, change, and then
			// use the copy in TileEntity logic
			if (!(newState.getBlock() instanceof ChestBlock)) {
				destroy(worldIn, pos, state);
			}
			worldIn.removeBlockEntity(pos);
		}
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getBlockEntity(pos);
		if (ent == null || !(ent instanceof LockedChestTileEntity))
			return;
		
		LockedChestTileEntity table = (LockedChestTileEntity) ent;
		for (int i = 0; i < table.getContainerSize(); i++) {
			ItemStack item = table.getItem(i);
			if (!item.isEmpty()) {
				double x, y, z;
				x = pos.getX() + .5;
				y = pos.getY() + .5;
				z = pos.getZ() + .5;
				world.addFreshEntity(new ItemEntity(world, x, y, z, item.copy()));
			}
		}
	}
}
