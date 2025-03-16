package com.smanzana.nostrummagica.block.dungeon;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.item.ResourceCrystal;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.ProgressionDoorTileEntity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class ProgressionDoorBlock extends MagicDoorBlock {

	public static final String ID = "progression_door";
	
	public ProgressionDoorBlock() {
		super();
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return this.isMaster(state);
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		if (!this.isMaster(state))
			return null;
		
		return new ProgressionDoorTileEntity();
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;
		
		BlockPos master = this.getMasterPos(worldIn, state, pos);
		if (master != null && worldIn.getBlockEntity(master) != null) {
			
			if (playerIn.isCreative()) {
				ProgressionDoorTileEntity te = (ProgressionDoorTileEntity) worldIn.getBlockEntity(master);
				ItemStack heldItem = playerIn.getItemInHand(hand);
				if (!heldItem.isEmpty() && heldItem.getItem() instanceof SpellRune) {
					te.require(SpellRune.toComponentWrapper(heldItem));
					return InteractionResult.SUCCESS;
				}
				if (!heldItem.isEmpty() && heldItem.getItem() instanceof ResourceCrystal) {
					te.tier(((ResourceCrystal) heldItem.getItem()).getTier());
					return InteractionResult.SUCCESS;
				}
				if (heldItem.isEmpty() && hand == InteractionHand.MAIN_HAND) {
					te.level((te.getRequiredLevel() + 1) % 15);
					return InteractionResult.SUCCESS;
				}
			}
			
			List<Component> missingDepStrings = new LinkedList<>();
			if (!((ProgressionDoorTileEntity) worldIn.getBlockEntity(master)).meetsRequirements(playerIn, missingDepStrings)) {
				playerIn.sendMessage(new TranslatableComponent("info.door.missing.intro"), Util.NIL_UUID);
				for (Component text : missingDepStrings) {
					playerIn.sendMessage(text, Util.NIL_UUID);
				}
				NostrumMagicaSounds.CAST_FAIL.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			} else {
				this.clearDoor(worldIn, pos, state);
			}
		}
		
		return InteractionResult.SUCCESS;
	}
}
