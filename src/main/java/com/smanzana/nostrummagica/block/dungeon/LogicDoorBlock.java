package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.block.ITriggeredBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class LogicDoorBlock extends MagicDoorBlock implements ITriggeredBlock {

	public static final String ID = "logic_door";
	
	protected LogicDoorBlock(Block.Properties props) {
		super(props);
	}
	
	public LogicDoorBlock() {
		this(Block.Properties.of(Material.STONE)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.sound(SoundType.STONE)
				);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;
		
		// Allow creative players to open door
		if (player.isCreative()) {
			ItemStack heldItem = player.getItemInHand(hand);
			if (heldItem.isEmpty() && hand == InteractionHand.MAIN_HAND) {
				this.trigger(worldIn, pos, state, null);
				return InteractionResult.SUCCESS;
			}
		}
		
		return InteractionResult.PASS;
	}

	@Override
	public void trigger(Level world, BlockPos pos, BlockState state, BlockPos triggerPos) {
		this.clearDoor(world, pos, state);
	}
}
