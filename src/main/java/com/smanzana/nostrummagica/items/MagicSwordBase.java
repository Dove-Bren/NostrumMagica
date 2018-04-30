package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class MagicSwordBase extends ItemSword implements ILoreTagged {

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
	
	@Override
	public String getLoreKey() {
		return "nostrum_magic_weapon";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Weapons";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Inserting void crystals into an iron sword creates an Ethereal sword.", "Until you find a way to imbue it with the power of an element, it's largely useless.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Inserting void crystals into an iron sword creates an Ethereal sword.", "Casting an enchantment on it provides unique effects.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}

}
