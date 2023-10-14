package com.smanzana.nostrummagica.integration.aetheria.blocks;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.WispBlockGui;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.SpellScroll;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class WispBlock extends Block {
	
	public static final String ID = "wisp_block";
	protected static final VoxelShape SELECT_AABB = Block.makeCuboidShape(.2, .9, .2, .8, 1.3, .8);
	protected static final VoxelShape COLLIDE_AABB = Block.makeCuboidShape(.2, 0, .2, .8, 1.3, .8);
	
	public WispBlock() {
		super(Block.Properties.create(Material.GLASS)
				.hardnessAndResistance(3.0f, 10.0f)
				.sound(SoundType.GLASS)
				);
	}
	
//	@Override
//	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
//		return true;
//	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		// Automatically handle scrolls and reagents
		if (!playerIn.isSneaking() && !heldItem.isEmpty()) {
			WispBlockTileEntity te = (WispBlockTileEntity) worldIn.getTileEntity(pos);
			if (heldItem.getItem() instanceof SpellScroll
					&& te.getScroll() == null
					&& SpellScroll.getSpell(heldItem) != null) {
				// Take scroll
				te.setScroll(heldItem.copy());
				heldItem.shrink(1);
				return true;
			} else if (heldItem.getItem() instanceof ReagentItem) {
				
				if (te.getReagent().isEmpty()) {
					te.setReagent(heldItem.split(heldItem.getCount()));
					return true;
				} else if (ReagentItem.FindType(heldItem) == ReagentItem.FindType(te.getReagent())) {
					int avail = Math.max(0, Math.min(64, 64 - te.getReagent().getCount()));
					if (avail != 0) {
						int take = Math.min(avail, heldItem.getCount());
						heldItem.shrink(take);
						te.getReagent().grow(take);
						return true;
					}
				}
			}
		}
		
		WispBlockTileEntity te = (WispBlockTileEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, WispBlockGui.WispBlockContainer.Make(te));
		
		return true;
	}
	
	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new WispBlockTileEntity();
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SELECT_AABB;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState blockState, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return COLLIDE_AABB;
	}
	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public boolean isTranslucent(BlockState state) {
//		return true;
//	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(worldIn, pos, state);
			worldIn.removeTileEntity(pos);
			//super.onReplaced(world, pos, state);
		}
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof WispBlockTileEntity))
			return;
		
		WispBlockTileEntity table = (WispBlockTileEntity) ent;
		ItemStack item = table.getScroll();
		if (!item.isEmpty()) {
			double x, y, z;
			x = pos.getX() + .5;
			y = pos.getY() + .5;
			z = pos.getZ() + .5;
			world.addEntity(new ItemEntity(world, x, y, z, item.copy()));
		}
		
		item = table.getReagent();
		if (!item.isEmpty()) {
			double x, y, z;
			x = pos.getX() + .5;
			y = pos.getY() + .5;
			z = pos.getZ() + .5;
			world.addEntity(new ItemEntity(world, x, y, z, item.copy()));
		}
		
		table.deactivate();
	}
	
	public @Nonnull ItemStack getScroll(World world, BlockPos pos) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof WispBlockTileEntity))
			return ItemStack.EMPTY;
		
		WispBlockTileEntity table = (WispBlockTileEntity) ent;
		return table.getScroll();
	}
	
	public int getWispCount(World world, BlockPos pos) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof WispBlockTileEntity))
			return 0;
		
		WispBlockTileEntity table = (WispBlockTileEntity) ent;
		return table.getWispCount();
	}
	
	public int getMaxWisps(World world, BlockPos pos) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof WispBlockTileEntity))
			return 0;
		
		WispBlockTileEntity table = (WispBlockTileEntity) ent;
		return table.getMaxWisps();
	}
}
