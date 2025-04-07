package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.LoreTableGui;
import com.smanzana.nostrummagica.tile.LoreTableTileEntity;
import com.smanzana.nostrummagica.tile.NostrumTileEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;

public class LoreTableBlock extends BaseEntityBlock {
	
	public static final String ID = "lore_table";
	
	public LoreTableBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(2.0f, 10.0f)
				.sound(SoundType.WOOD)
				);
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
		return false;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (!worldIn.isClientSide()) {
			LoreTableTileEntity te = (LoreTableTileEntity) worldIn.getBlockEntity(pos);
			NostrumMagica.instance.proxy.openContainer(player, LoreTableGui.LoreTableContainer.Make(te));
		}
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LoreTableTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumTileEntities.LoreTableEntityType);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(worldIn, pos, state);
			worldIn.removeBlockEntity(pos);
		}
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		BlockEntity ent = world.getBlockEntity(pos);
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
