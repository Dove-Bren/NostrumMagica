package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.tile.PedestalBlockEntity;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

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
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// If there's an altar, we'll place item in altar
		// Otherwise, we'll place it on the ground
		
		if (recipe.getTier() == 0) {
			// spawn on ground
			ItemEntity entity = new ItemEntity(world,
					center.getX() + .5, center.getY() + 1, center.getZ() + .5,
					stack.copy());
			world.addFreshEntity(entity);
		} else {
			PedestalBlockEntity altar = (PedestalBlockEntity) world.getBlockEntity(center);
			altar.setItem(stack.copy());
		}
		
		// repeat for any leftover stack
		if (!leftoverStack.isEmpty()) {
			ItemEntity entity = new ItemEntity(world,
					center.getX() + .5, center.getY() + 1, center.getZ() + .5,
					leftoverStack.copy());
			world.addFreshEntity(entity);
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
	public List<Component> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.spawn_item.desc",
				new Object[]{stack.getHoverName()});
	}
}
