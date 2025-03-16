package com.smanzana.nostrummagica.ritual;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IRitualLayout extends IRitualIngredients {
	
	public ItemStack getCenterItem(Level world, BlockPos center);
	
	public List<ItemStack> getExtraItems(Level world, BlockPos center);
	
	public List<ItemStack> getReagentItems(Level world, BlockPos center);
	
	public void clearIngredients(Level world, BlockPos center, RitualRecipe recipePerformed);
	
	public void setOutputItems(Level world, BlockPos center, Iterable<ItemStack> output);

}
