package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.LoreTableGui;
import com.smanzana.nostrummagica.tile.LoreTableTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class LoreTableBlock extends Block {
	
	public static final String ID = "lore_table";
	
	public LoreTableBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(2.0f, 10.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.harvestLevel(0)
				);
	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isClientSide()) {
			LoreTableTileEntity te = (LoreTableTileEntity) worldIn.getBlockEntity(pos);
			NostrumMagica.instance.proxy.openContainer(player, LoreTableGui.LoreTableContainer.Make(te));
		}
		
		return ActionResultType.SUCCESS;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean triggerEvent(BlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
		super.triggerEvent(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity == null ? false : tileentity.triggerEvent(eventID, eventParam);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new LoreTableTileEntity();
	}
	
	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(worldIn, pos, state);
			worldIn.removeBlockEntity(pos);
		}
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getBlockEntity(pos);
		if (ent == null || !(ent instanceof LoreTableTileEntity))
			return;
		
		LoreTableTileEntity table = (LoreTableTileEntity) ent;
		ItemStack item = table.getItem();
		if (!item.isEmpty()) {
			double x, y, z;
			x = pos.getX() + .5;
			y = pos.getY() + .5;
			z = pos.getZ() + .5;
			world.addFreshEntity(new ItemEntity(world, x, y, z, item.copy()));
		}
		
	}
}
