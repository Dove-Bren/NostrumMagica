package com.smanzana.nostrummagica.item.armor;

import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MageArmor extends ArmorItem implements ILoreTagged {
	
	private static final UUID[] ARMOR_MODIFIERS = new UUID[]{UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};

	private static final String ID_PREFIX = "magearmor_";
	public static final String ID_HELM = ID_PREFIX + "helm";
	public static final String ID_CHEST = ID_PREFIX + "chest";
	public static final String ID_LEGS = ID_PREFIX + "legs";
	public static final String ID_FEET = ID_PREFIX + "feet";

	public MageArmor(EquipmentSlotType slot, Item.Properties properties) {
		super(ArmorMaterial.LEATHER, slot, properties.maxDamage(250));
	}
	
//	public String getModelID() {
//		return id;
//	}
	
	@Override
	public int getItemEnchantability() {
		return 20; // not as much as gold but very good otherwise
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return !repair.isEmpty() && NostrumTags.Items.CrystalSmall.contains(repair.getItem());
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();

		if (equipmentSlot == this.slot) {
			final int armor;
			final int potency = 5;
			switch (slot) {
			case CHEST:
				armor = 6;
				break;
			case FEET:
				armor = 3;
				break;
			case HEAD:
				armor = 3;
				break;
			case LEGS:
				armor = 5;
				break;
			case MAINHAND:
			case OFFHAND:
			default:
				armor = 0;
				break;
			
			}
			
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(multimap);
			builder.put(Attributes.ARMOR, new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()],
					"Armor modifier", (double) armor, AttributeModifier.Operation.ADDITION));
			builder.put(NostrumAttributes.magicPotency, new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()],
					"Magic Potency", (double) potency, AttributeModifier.Operation.ADDITION));
			multimap = builder.build();
		}

		return multimap;
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_magic_armor";
	}

	@Override
	public String getLoreDisplayName() {
		return "Mage Armor";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("By modifying leather armor, you've made armor that seems must more suitable to be used for mages!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("By modifying leather armor, you've made armor that seems must more suitable to be used for mages!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	public static MageArmor get(EquipmentSlotType slot) {
		switch (slot) {
		case CHEST:
			return NostrumItems.mageArmorChest;
		case FEET:
			return NostrumItems.mageArmorFeet;
		case HEAD:
			return NostrumItems.mageArmorHelm;
		case LEGS:
			return NostrumItems.mageArmorLegs;
		default:
			break;
		}
		
		return null;
	}

}
