package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.LoreTableGui;
import com.smanzana.nostrummagica.tiles.LoreTableEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class LoreTable extends ContainerBlock {
	
	public static final String ID = "lore_table";
	
	public LoreTable() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(2.0f, 10.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.harvestLevel(0)
				);
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		LoreTableEntity te = (LoreTableEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(player, LoreTableGui.LoreTableContainer.Make(te));
		
		return true;
	}
	
	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new LoreTableEntity();
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(worldIn, pos, state);
			worldIn.removeTileEntity(pos);
		}
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof LoreTableEntity))
			return;
		
		LoreTableEntity table = (LoreTableEntity) ent;
		ItemStack item = table.getItem();
		if (!item.isEmpty()) {
			double x, y, z;
			x = pos.getX() + .5;
			y = pos.getY() + .5;
			z = pos.getZ() + .5;
			world.addEntity(new ItemEntity(world, x, y, z, item.copy()));
		}
		
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		// TODO Auto-generated method stub
		return null;
	}
}
