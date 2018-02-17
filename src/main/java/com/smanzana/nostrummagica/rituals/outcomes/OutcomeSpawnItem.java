package com.smanzana.nostrummagica.rituals.outcomes;

import com.smanzana.nostrummagica.blocks.AltarBlock.AltarTileEntity;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OutcomeSpawnItem implements IRitualOutcome {

	private ItemStack stack;
	
	public OutcomeSpawnItem(ItemStack stack) {
		this.stack = stack;
	}
	
	@Override
	public void perform(World world, EntityPlayer player, BlockPos center, RitualRecipe recipe) {
		// If there's an altar, we'll place item in altar
		// Otherwise, we'll place it on the ground
		if (recipe.getTier() == 0) {
			// spawn on ground
			EntityItem entity = new EntityItem(world,
					center.getX() + .5, center.getY() + 1, center.getZ() + .5,
					stack.copy());
			world.spawnEntityInWorld(entity);
		} else {
			AltarTileEntity altar = (AltarTileEntity) world.getTileEntity(center);
			altar.setItem(stack.copy());
		}
	}

	
	
}
