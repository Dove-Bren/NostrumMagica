package com.smanzana.nostrummagica.blocks;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.tiles.ProgressionDoorTileEntity;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ProgressionDoor extends NostrumMagicDoor {

	public static final String ID = "progression_door";
	
	private static ProgressionDoor instance = null;
	public static ProgressionDoor instance() {
		if (instance == null)
			instance = new ProgressionDoor();
		
		return instance;
	}
	
	public ProgressionDoor() {
		super();
		this.setUnlocalizedName(ID);
		
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return this.isMaster(state);
	}
	
	@Override
	public TileEntity createTileEntity(World world, BlockState state) {
		if (!this.isMaster(state))
			return null;
		
		return new ProgressionDoorTileEntity();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			return true;
		
		BlockPos master = this.getMasterPos(worldIn, state, pos);
		if (master != null && worldIn.getTileEntity(master) != null) {
			
			// Mostly debug code, but could be useful for map devs as well
			if (playerIn.isCreative()) {
				ProgressionDoorTileEntity te = (ProgressionDoorTileEntity) worldIn.getTileEntity(master);
				ItemStack heldItem = playerIn.getHeldItem(hand);
				if (!heldItem.isEmpty() && heldItem.getItem() instanceof SpellRune) {
					te.require(SpellRune.toComponentWrapper(heldItem));
					return true;
				}
				if (heldItem.isEmpty() && hand == Hand.MAIN_HAND) {
					te.level((te.getRequiredLevel() + 1) % 15);
					return true;
				}
			}
			
			List<ITextComponent> missingDepStrings = new LinkedList<>();
			if (!((ProgressionDoorTileEntity) worldIn.getTileEntity(master)).meetsRequirements(playerIn, missingDepStrings)) {
				playerIn.sendMessage(new TranslationTextComponent("info.door.missing.intro"));
				for (ITextComponent text : missingDepStrings) {
					playerIn.sendMessage(text);
				}
				NostrumMagicaSounds.CAST_FAIL.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			} else {
				this.clearDoor(worldIn, pos, state);
			}
		}
		
		return true;
	}
}
