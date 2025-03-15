package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.client.gui.container.LauncherBlockGui;
import com.smanzana.nostrummagica.tile.DungeonLauncherTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class DungeonLauncherBlock extends DirectionalBlock implements ITriggeredBlock {
	
	public static final String ID = "launcher";
	
	public DungeonLauncherBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(-1.0F, 3600000.8F)
				.sound(SoundType.STONE)
				);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
	}
	
	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!player.isCreative()) {
			return ActionResultType.PASS;
		}
		DungeonLauncherTileEntity te = (DungeonLauncherTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.instance.proxy.openContainer(player, LauncherBlockGui.LauncherBlockContainer.Make(te));
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new DungeonLauncherTileEntity();
	}
	
	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(world, pos, state);
			world.removeBlockEntity(pos);
		}
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getBlockEntity(pos);
		if (ent == null || !(ent instanceof DungeonLauncherTileEntity))
			return;
		
		DungeonLauncherTileEntity putter = (DungeonLauncherTileEntity) ent;
		IInventory inv = putter.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack item = inv.getItem(i);
			if (!item.isEmpty()) {
				double x, y, z;
				x = pos.getX() + .5;
				y = pos.getY() + .5;
				z = pos.getZ() + .5;
				world.addFreshEntity(new ItemEntity(world, x, y, z, item.copy()));
			}
		}
	}

	@Override
	public void trigger(World world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		DungeonLauncherTileEntity te = (DungeonLauncherTileEntity) world.getBlockEntity(blockPos);
		te.trigger();
	}
}
