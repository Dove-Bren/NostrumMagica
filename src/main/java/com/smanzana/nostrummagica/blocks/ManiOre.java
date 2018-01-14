package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class ManiOre extends Block {

	public static final String ID = "mani_ore";
	
	private static ManiOre instance = null;
	public static ManiOre instance() {
		if (instance == null)
			instance = new ManiOre();
		
		return instance;
	}
	
	
	public ManiOre() {
		super(Material.ROCK, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(2.0f);
		this.setResistance(30.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setHarvestLevel("pickaxe", 3);
		
	}
	
	@Override
	public int quantityDroppedWithBonus(int fortune, Random random) {
		if (fortune == 0)
			return 1;
		return fortune + 1 + (random.nextBoolean() ? 1 : 0);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ReagentItem.instance();
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return ReagentItem.ReagentType.MANI_DUST.getMeta();
	}
	
	@Override
	public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
		return RANDOM.nextInt(3);
	}
}
