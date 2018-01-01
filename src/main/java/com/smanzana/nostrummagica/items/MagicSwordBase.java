package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class MagicSwordBase extends ItemSword {

	public static void init() {
		GameRegistry.register(instance(), new ResourceLocation(
				NostrumMagica.MODID, "magicswordbase"));
		
		instance().setUnlocalizedName("magicswordbase");
		
		/*
		 * In the future, something cool.
		 * For now, just a wooden sword </3
		 */
		GameRegistry.addRecipe(new ItemStack(instance), " C ", " C ", " S ",
				'S', Items.IRON_SWORD, 
				'C', InfusedGemItem.instance().getGem(null, 1));
	}
	
	private static MagicSwordBase instance = null;

	public static MagicSwordBase instance() {
		if (instance == null)
			instance = new MagicSwordBase();
	
		return instance;

	}

	public MagicSwordBase() {
		super(ToolMaterial.WOOD);
		this.setMaxDamage(5);
		this.setCreativeTab(NostrumMagica.creativeTab);
	}
	
	public String getModelID() {
		return "magicswordbase";
	}

}
