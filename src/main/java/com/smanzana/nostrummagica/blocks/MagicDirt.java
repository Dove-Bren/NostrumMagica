package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.InfusedGemItem;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class MagicDirt extends Block {

	public static final String ID = "magic_dirt";
	
	private static MagicDirt instance = null;
	public static MagicDirt instance() {
		if (instance == null)
			instance = new MagicDirt();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.addRecipe(new ItemStack(instance()), " D ", "DCD", " D ",
				'D', new ItemStack(Blocks.DIRT, 1, OreDictionary.WILDCARD_VALUE),
				'C', InfusedGemItem.instance().getGem(null, 0));
	}
	
	
	public MagicDirt() {
		super(Material.ROCK, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(0.7f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.GROUND);
		this.setHarvestLevel("shovel", 1);
	}
}
