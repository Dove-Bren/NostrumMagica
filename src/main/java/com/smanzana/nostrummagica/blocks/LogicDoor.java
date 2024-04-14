package com.smanzana.nostrummagica.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class LogicDoor extends NostrumMagicDoor implements ITriggeredBlock {

	public static final String ID = "logic_door";
	
	public LogicDoor() {
		super();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isRemote)
			return ActionResultType.SUCCESS;
		
		// Allow creative players to open door
		if (player.isCreative()) {
			ItemStack heldItem = player.getHeldItem(hand);
			if (heldItem.isEmpty() && hand == Hand.MAIN_HAND) {
				this.trigger(worldIn, pos, state, null);
				return ActionResultType.SUCCESS;
			}
		}
		
		return ActionResultType.PASS;
	}

	@Override
	public void trigger(World world, BlockPos pos, BlockState state, BlockPos triggerPos) {
		this.clearDoor(world, pos, state);
	}
}
