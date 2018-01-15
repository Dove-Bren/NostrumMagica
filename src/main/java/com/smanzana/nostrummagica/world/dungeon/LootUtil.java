package com.smanzana.nostrummagica.world.dungeon;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.BlockChest;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
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
	public static final void generateLoot(World world, BlockPos pos, EnumFacing facing) {
		generateLoot(world, pos, facing, NostrumMagica.MODID + ":nostrum_shrine_room");
	}
	
	public static final void generateLoot(World world, BlockPos pos, EnumFacing facing,
			String loottable) {
		world.setBlockState(pos, Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, facing));
		
		TileEntityChest chest = (TileEntityChest) world.getTileEntity(pos);
		chest.setLootTable(new ResourceLocation(loottable), rand.nextLong());
	}
	
}
