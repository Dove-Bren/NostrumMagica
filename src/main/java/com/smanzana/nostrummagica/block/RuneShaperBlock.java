package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.RuneShaperGui;
import com.smanzana.nostrummagica.tile.RuneShaperTileEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolType;

public class RuneShaperBlock extends Block {
	
	public static final String ID = "rune_shaper";
	
	public RuneShaperBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(2.0f, 10.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.harvestLevel(0)
				);
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
		return false;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		RuneShaperTileEntity te = (RuneShaperTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.instance.proxy.openContainer(player, RuneShaperGui.RuneShaperContainer.Make(te));
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return new RuneShaperTileEntity();
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
		if (ent == null || !(ent instanceof RuneShaperTileEntity))
			return;
		
		RuneShaperTileEntity table = (RuneShaperTileEntity) ent;
		for (int i = 0; i < table.getContainerSize(); i++) {
			if (table.getItem(i) != null) {
				ItemEntity item = new ItemEntity(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeItemNoUpdate(i));
				world.addFreshEntity(item);
			}
		}
		
	}
}
