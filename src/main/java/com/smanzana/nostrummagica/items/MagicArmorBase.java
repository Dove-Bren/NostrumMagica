package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class MagicArmorBase extends ItemArmor {

	public static MagicArmorBase helm;
	public static MagicArmorBase chest;
	public static MagicArmorBase legs;
	public static MagicArmorBase feet;
	
	public static void init() {
		helm = new MagicArmorBase("magichelmbase", EntityEquipmentSlot.HEAD);
		chest = new MagicArmorBase("magicchestbase", EntityEquipmentSlot.CHEST);
		legs = new MagicArmorBase("magicleggingsbase", EntityEquipmentSlot.LEGS);
		feet = new MagicArmorBase("magicfeetbase", EntityEquipmentSlot.FEET);
		
		GameRegistry.register(helm, new ResourceLocation(
				NostrumMagica.MODID, "magichelmbase"));
		GameRegistry.register(chest, new ResourceLocation(
				NostrumMagica.MODID, "magicchestbase"));
		GameRegistry.register(legs, new ResourceLocation(
				NostrumMagica.MODID, "magicleggingsbase"));
		GameRegistry.register(feet, new ResourceLocation(
				NostrumMagica.MODID, "magicfeetbase"));
		
		/*
		 * For now, just leather stuff. In the future, something cooler!
		 */
		GameRegistry.addShapedRecipe(new ItemStack(helm), "CCC", "C C", " D ",
				'C', InfusedGemItem.instance().getGem(null, 1),
				'D', Items.IRON_HELMET);
		GameRegistry.addShapedRecipe(new ItemStack(chest), "CDC", "CCC", "CCC",
				'C', InfusedGemItem.instance().getGem(null, 1),
				'D', Items.IRON_CHESTPLATE);
		GameRegistry.addShapedRecipe(new ItemStack(legs), "CCC", "CDC", "C C",
				'C', InfusedGemItem.instance().getGem(null, 1),
				'D', Items.IRON_LEGGINGS);
		GameRegistry.addShapedRecipe(new ItemStack(feet), " D ", "C C", "C C",
				'C', InfusedGemItem.instance().getGem(null, 1),
				'D', Items.IRON_BOOTS);
	}
	
	private String id;

	public MagicArmorBase(String id, EntityEquipmentSlot slot) {
		super(ArmorMaterial.LEATHER, 0, slot);
		this.setUnlocalizedName(id);
		this.setMaxDamage(5);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.id = id;
	}
	
	public String getModelID() {
		return id;
	}

}
