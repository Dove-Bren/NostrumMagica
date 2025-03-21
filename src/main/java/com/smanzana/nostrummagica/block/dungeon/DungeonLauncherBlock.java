package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.client.gui.container.LauncherBlockGui;
import com.smanzana.nostrummagica.tile.DungeonLauncherTileEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class DungeonLauncherBlock extends DirectionalBlock implements ITriggeredBlock {
	
	public static final String ID = "launcher";
	
	public DungeonLauncherBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(-1.0F, 3600000.8F)
				.sound(SoundType.STONE)
				);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (!player.isCreative()) {
			return InteractionResult.PASS;
		}
		DungeonLauncherTileEntity te = (DungeonLauncherTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.instance.proxy.openContainer(player, LauncherBlockGui.LauncherBlockContainer.Make(te));
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DungeonLauncherTileEntity();
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(world, pos, state);
			world.removeBlockEntity(pos);
		}
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent == null || !(ent instanceof DungeonLauncherTileEntity))
			return;
		
		DungeonLauncherTileEntity putter = (DungeonLauncherTileEntity) ent;
		Container inv = putter.getInventory();
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
	public void trigger(Level world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		DungeonLauncherTileEntity te = (DungeonLauncherTileEntity) world.getBlockEntity(blockPos);
		te.trigger();
	}
}
