package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.block.ITriggeredBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class LogicDoorBlock extends MagicDoorBlock implements ITriggeredBlock {

	public static final String ID = "logic_door";
	
	protected LogicDoorBlock(Block.Properties props) {
		super(props);
	}
	
	public LogicDoorBlock() {
		this(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.sound(SoundType.STONE)
				);
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
