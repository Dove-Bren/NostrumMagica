package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class TomeWorkshopBlock extends Block {
	
	public static final String ID = "tome_workshop";
	
	public TomeWorkshopBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(2.0f, 10.0f)
				.sound(SoundType.WOOD)
				);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (worldIn.isClientSide()) {
			NostrumMagica.instance.proxy.openTomeWorkshopScreen();
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
}
