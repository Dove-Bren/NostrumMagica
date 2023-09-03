package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class EssenceOre extends Block {

	public static final String ID = "essore";
	
	private static EssenceOre instance = null;
	public static EssenceOre instance() {
		if (instance == null)
			instance = new EssenceOre();
		
		return instance;
	}
	
	
	public EssenceOre() {
		super(Material.ROCK, MapColor.NETHERRACK);
		this.setUnlocalizedName(ID);
		this.setHardness(1.7f);
		this.setResistance(30.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setHarvestLevel("pickaxe", 3);
		
	}
	
	@Override
	public int quantityDroppedWithBonus(int fortune, Random random) {
		int base = 1;
		while (fortune-- >= 0) {
			base += random.nextInt(2);
		}
		
		return base;
	}
	
	@Override
	public Item getItemDropped(BlockState state, Random rand, int fortune) {
		return EssenceItem.instance();
		
	}
	
	@Override
	public int damageDropped(BlockState state) {
		return NostrumMagica.rand.nextInt(
				EMagicElement.values().length
				);
	}
	
	@Override
	public int getExpDrop(BlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
		return RANDOM.nextInt(5);
	}
}
