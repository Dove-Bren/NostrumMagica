package com.smanzana.nostrummagica.ritual;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRitualLayout extends IRitualIngredients {
	
	public ItemStack getCenterItem(World world, BlockPos center);
	
	public List<ItemStack> getExtraItems(World world, BlockPos center);
	
	public List<ItemStack> getReagentItems(World world, BlockPos center);
	
	public void clearIngredients(World world, BlockPos center, RitualRecipe recipePerformed);
	
	public void setOutputItems(World world, BlockPos center, Iterable<ItemStack> output);

}
