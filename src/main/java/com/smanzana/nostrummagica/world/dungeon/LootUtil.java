package com.smanzana.nostrummagica.world.dungeon;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class LootUtil {
	
	public static final Random rand = new Random();

	/**
	 * Generates a chest at the given location with random loot.
	 * Equivalent to generateLoot(world, pos, rarity, "nostrum_shrine_room")
	 * @param world
	 * @param pos
	 * @param facing
	 */
	public static final void generateLoot(World world, BlockPos pos, Direction facing) {
		generateLoot(world, pos, facing, NostrumMagica.MODID + ":nostrum_shrine_room");
	}
	
	public static final void generateLoot(World world, BlockPos pos, Direction facing,
			String loottable) {
		world.setBlockState(pos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, facing));
		
		ChestTileEntity chest = (ChestTileEntity) world.getTileEntity(pos);
		
		if (chest == null) {
			world.setBlockState(pos, Blocks.GOLD_BLOCK.getDefaultState());
		} else {
			chest.setLootTable(new ResourceLocation(loottable), rand.nextLong());
		}
	}
	
	/**
	 * Sets a block to be a chest with the given loot inside of it.
	 * loot should be an array exactly 27 long. Less is ok but more is ignored
	 * @param world
	 * @param pos
	 * @param facing
	 * @param loot
	 */
	public static final void createLoot(World world, BlockPos pos, Direction facing,
			NonNullList<ItemStack> loot) {
		world.setBlockState(pos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, facing));
		
		ChestTileEntity chest = (ChestTileEntity) world.getTileEntity(pos);
		int len = Math.min(27, loot.size());
		for (int i = 0; i < len; i++) {
			chest.setInventorySlotContents(i, loot.get(i));
		}
	}
	
}
