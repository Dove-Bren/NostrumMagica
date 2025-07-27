package com.smanzana.nostrummagica.item.armor;

import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KoidHelmet extends ArmorItem implements ILoreTagged {
	
	private static final UUID ARMOR_MODIFIER = UUID.fromString("d09e284f-baae-4d4f-81c2-787f49a93ef2");

	public static final String ID = "koid_helm";
	
	protected Multimap<Attribute, AttributeModifier> attributes;

	public KoidHelmet(Item.Properties properties) {
		super(ArmorMaterials.TURTLE, EquipmentSlot.HEAD, properties.durability(100));
	}
	
	protected Multimap<Attribute, AttributeModifier> makeAttributes() {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(Attributes.ARMOR, new AttributeModifier(ARMOR_MODIFIER,
				"Armor modifier", 2.0, AttributeModifier.Operation.ADDITION));
		builder.put(NostrumAttributes.magicDamage, new AttributeModifier(ARMOR_MODIFIER,
				"Magic damage", 20.0, AttributeModifier.Operation.ADDITION));
		return builder.build();
	}
	
//	public String getModelID() {
//		return id;
//	}
	
	@Override
	public int getEnchantmentValue() {
		return 18; // not as much as gold but very good otherwise
	}
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return !repair.isEmpty() && repair.is(NostrumTags.Items.InfusedGem);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		if (equipmentSlot == this.slot) {
			if (attributes == null) {
				attributes = this.makeAttributes();
			}
			return attributes;
		}
		
		return ImmutableMultimap.of();
	}
	
	@Override
	public String getLoreKey() {
		return "koid_helm";
	}

	@Override
	public String getLoreDisplayName() {
		return "Koid Helm";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("A strange piece of headgear that is nothing more than the shell of a koid.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("A strange piece of headgear that is nothing more than the shell of a koid.");
	}

	@Override
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		// Make this render the helm invisible, since I can't figure out how to make helmet just not render

		return NostrumMagica.MODID + ":textures/models/empty.png";
	}
}
