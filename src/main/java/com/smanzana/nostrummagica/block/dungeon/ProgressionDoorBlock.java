package com.smanzana.nostrummagica.block.dungeon;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.item.ResourceCrystal;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.ProgressionDoorTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (!this.isMaster(state))
			return null;
		
		return new ProgressionDoorTileEntity();
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isClientSide)
			return ActionResultType.SUCCESS;
		
		BlockPos master = this.getMasterPos(worldIn, state, pos);
		if (master != null && worldIn.getBlockEntity(master) != null) {
			
			if (playerIn.isCreative()) {
				ProgressionDoorTileEntity te = (ProgressionDoorTileEntity) worldIn.getBlockEntity(master);
				ItemStack heldItem = playerIn.getItemInHand(hand);
				if (!heldItem.isEmpty() && heldItem.getItem() instanceof SpellRune) {
					te.require(SpellRune.toComponentWrapper(heldItem));
					return ActionResultType.SUCCESS;
				}
				if (!heldItem.isEmpty() && heldItem.getItem() instanceof ResourceCrystal) {
					te.tier(((ResourceCrystal) heldItem.getItem()).getTier());
					return ActionResultType.SUCCESS;
				}
				if (heldItem.isEmpty() && hand == Hand.MAIN_HAND) {
					te.level((te.getRequiredLevel() + 1) % 15);
					return ActionResultType.SUCCESS;
				}
			}
			
			List<ITextComponent> missingDepStrings = new LinkedList<>();
			if (!((ProgressionDoorTileEntity) worldIn.getBlockEntity(master)).meetsRequirements(playerIn, missingDepStrings)) {
				playerIn.sendMessage(new TranslationTextComponent("info.door.missing.intro"), Util.NIL_UUID);
				for (ITextComponent text : missingDepStrings) {
					playerIn.sendMessage(text, Util.NIL_UUID);
				}
				NostrumMagicaSounds.CAST_FAIL.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			} else {
				this.clearDoor(worldIn, pos, state);
			}
		}
		
		return ActionResultType.SUCCESS;
	}
}
