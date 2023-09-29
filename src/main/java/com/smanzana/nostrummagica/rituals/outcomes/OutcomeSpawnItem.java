package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.tiles.AltarTileEntity;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OutcomeSpawnItem implements IItemRitualOutcome {

	protected ItemStack stack;
	protected @Nullable ItemStack leftoverStack;
	
	public OutcomeSpawnItem(ItemStack stack) {
		this(stack, ItemStack.EMPTY);
	}
	
	public OutcomeSpawnItem(@Nonnull ItemStack stack, @Nonnull ItemStack leftoverStack) {
		this.stack = stack;
		this.leftoverStack = leftoverStack;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		// If there's an altar, we'll place item in altar
		// Otherwise, we'll place it on the ground
		
		if (recipe.getTier() == 0) {
			// spawn on ground
			ItemEntity entity = new ItemEntity(world,
					center.getX() + .5, center.getY() + 1, center.getZ() + .5,
					stack.copy());
			world.addEntity(entity);
		} else {
			AltarTileEntity altar = (AltarTileEntity) world.getTileEntity(center);
			altar.setItem(stack.copy());
		}
		
		// repeat for any leftover stack
		if (!leftoverStack.isEmpty()) {
			ItemEntity entity = new ItemEntity(world,
					center.getX() + .5, center.getY() + 1, center.getZ() + .5,
					leftoverStack.copy());
			world.addEntity(entity);
		}
	}

	@Override
	public ItemStack getResult() {
		return stack;
	}
	
	@Override
	public String getName() {
		return "spawn_item";
	}

	@Override
	public List<String> getDescription() {
		return Lists.newArrayList(I18n.format("ritual.outcome.spawn_item.desc",
				new Object[]{stack.getDisplayName()})
				.split("\\|"));
	}
}
