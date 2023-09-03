package com.smanzana.nostrummagica.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LogicDoor extends NostrumMagicDoor implements ITriggeredBlock {

	public static final String ID = "logic_door";
	
	private static LogicDoor instance = null;
	public static LogicDoor instance() {
		if (instance == null)
			instance = new LogicDoor();
		
		return instance;
	}
	
	public LogicDoor() {
		super();
		this.setUnlocalizedName(ID);
		
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			return true;
		
		// Allow creative players to open door
		if (playerIn.isCreative()) {
			ItemStack heldItem = playerIn.getHeldItem(hand);
			if (heldItem.isEmpty() && hand == Hand.MAIN_HAND) {
				this.trigger(worldIn, pos, state, null);
				return true;
			}
		}
		
		return false;
	}
	
	public void trigger(World world, BlockPos pos, BlockState state, BlockPos triggerPos) {
		this.clearDoor(world, pos, state);
	}
}
