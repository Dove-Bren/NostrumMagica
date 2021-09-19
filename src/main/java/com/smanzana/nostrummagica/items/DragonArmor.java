package com.smanzana.nostrummagica.items;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

public class DragonArmor extends Item {

	public static enum DragonEquipmentSlot {
		HELM(EntityEquipmentSlot.HEAD),
		BODY(EntityEquipmentSlot.CHEST),
		WINGS(EntityEquipmentSlot.LEGS),
		CREST(EntityEquipmentSlot.FEET);
		
		private final EntityEquipmentSlot mirrorSlot;
		
		private DragonEquipmentSlot(EntityEquipmentSlot mirrorSlot) {
			this.mirrorSlot = mirrorSlot;
		}
		
		public EntityEquipmentSlot getMirrorSlot() {
			return mirrorSlot;
		}
		
		public static final @Nullable DragonEquipmentSlot FindForSlot(EntityEquipmentSlot vanillaSlot) {
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				if (slot.mirrorSlot == vanillaSlot) {
					return slot;
				}
			}
			
			return null;
		}

		public String getName() {
			return this.name().toLowerCase();
		}
	}
	
	public static enum DragonArmorMaterial {
		GOLD,
		IRON,
		DIAMOND;
	}
	
	private static Map<DragonArmorMaterial, Map<DragonEquipmentSlot, DragonArmor>> items;
	
	public static final void registerArmors(final IForgeRegistry<Item> registry) {
		items = new EnumMap<DragonArmorMaterial, Map<DragonEquipmentSlot, DragonArmor>>(DragonArmorMaterial.class);
		for (DragonArmorMaterial material : DragonArmorMaterial.values()) {
			items.put(material, new EnumMap<DragonEquipmentSlot, DragonArmor>(DragonEquipmentSlot.class));
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				
				// NOT IMPLEMENTED TODO
				{
					if (slot == DragonEquipmentSlot.CREST || slot == DragonEquipmentSlot.WINGS) {
						continue;
					}
				}
				// NOT IMPLEMENTED TODO
				
				ResourceLocation location = new ResourceLocation(NostrumMagica.MODID, "dragonarmor_" + slot.name().toLowerCase() + "_" + material.name().toLowerCase());
				DragonArmor armor = new DragonArmor(location.getResourcePath(), slot, material);
				armor.setUnlocalizedName(location.getResourcePath());
				armor.setRegistryName(location);
				registry.register(armor);
				items.get(material).put(slot, armor);
			}
		}
	}
	
	public static final List<DragonArmor> GetAllArmors() {
		List<DragonArmor> list = new ArrayList<>();
		for (DragonArmorMaterial material : DragonArmorMaterial.values()) {
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				DragonArmor armor = items.get(material).get(slot);
				if (armor != null) {
					list.add(armor);
				}
			}
		}
		return list;
	}
	
	public static final DragonArmor GetArmor(DragonEquipmentSlot slot, DragonArmorMaterial material) {
		return items.get(material).get(slot);
	}
	
	// UUIDs for modifiers for base attributes from armor (index is slot enum's ordinal)
	protected static final UUID[] ARMOR_MODIFIERS = new UUID[] {UUID.fromString("922AB274-1111-56FE-19AE-365AA9758B8B"), UUID.fromString("D1459204-0E61-4716-A129-61666D432E0D"), UUID.fromString("1F7D236D-1118-6524-3375-34814505B28E"), UUID.fromString("2A632266-F4E1-2E67-7836-64FD783B7A50")};
	
	protected static final int CalcArmor(DragonEquipmentSlot slot, DragonArmorMaterial material) {
		// As of writing, only helm and body exist.
		// I want gold to give 6 armor, iron 8, and diamond 10 (on base 10).
		// This will in general be split 80/20 in favor of the body
		final float matBase;
		final float slotMod;
		switch (material) {
		case GOLD:
			matBase = 6;
			break;
		case IRON:
			matBase = 8;
			break;
		case DIAMOND:
			matBase = 10;
			break;
		default:
			matBase = 0;
			break;
		}
		
		switch (slot) {
		case BODY:
			slotMod = .8f;
			break;
		case HELM:
			slotMod = .2f;
			break;
		case CREST:
		case WINGS:
		default:
			slotMod = 0f;
			break;
		
		}
		
		return Math.round(slotMod * matBase);
	}
	
	protected static final double CalcMagicResist(DragonEquipmentSlot slot, DragonArmorMaterial material) {
		// As of writing, only helm and body exist.
		// I want gold to give 60 resist, iron 20, and diamond 30.
		// This will in general be split 60/40 in favor of the body
		final double matBase;
		final double slotMod;
		switch (material) {
		case GOLD:
			matBase = 60.0;
			break;
		case IRON:
			matBase = 20.0;
			break;
		case DIAMOND:
			matBase = 30.0;
			break;
		default:
			matBase = 0;
			break;
		}
		
		switch (slot) {
		case BODY:
			slotMod = .6;
			break;
		case HELM:
			slotMod = .4;
			break;
		case CREST:
		case WINGS:
		default:
			slotMod = 0f;
			break;
		
		}
		
		return Math.round(slotMod * matBase);
	}

	protected static final int CalcArmorToughness(DragonEquipmentSlot slot, DragonArmorMaterial material) {
		// Simple 0 for gold, 1 for iron, and 2 for diamond
		switch (material) {
		case GOLD:
			return 0;
		case IRON:
			return 1;
		case DIAMOND:
			return 2;
		default:
			return 0;
		
		}
	}
	
	private final String resourceLocation;
	protected final DragonEquipmentSlot slot;
	protected final DragonArmorMaterial material;
	
	protected final int defaultArmor;
	protected final double defaultMagicResist;
	protected final int defaultArmorToughness;
	
	public DragonArmor(String resourceLocation, DragonEquipmentSlot slot, DragonArmorMaterial material) {
		super();
		this.resourceLocation = resourceLocation;
		this.slot = slot;
		this.material = material;
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.setHasSubtypes(false);
		
		this.setCreativeTab(NostrumMagica.creativeTab);
		
		defaultArmor = CalcArmor(slot, material);
		defaultMagicResist = CalcMagicResist(slot, material);
		defaultArmorToughness = CalcArmorToughness(slot, material);
	}
	
	@SideOnly(Side.CLIENT)
	public String getResourceLocation() {
		return resourceLocation;
	}
	
	public DragonArmorMaterial getMaterial() {
		return this.material;
	}
	
	public DragonEquipmentSlot getSlot() {
		return slot;
	}
	
	protected int getArmorValue() {
		return defaultArmor;
	}
	
	protected int getArmorToughness() {
		return defaultArmorToughness;
	}
	
	protected double getMagicResistance() {
		return defaultMagicResist;
	}
	
	public Multimap<String, AttributeModifier> getAttributeModifiers(DragonEquipmentSlot equipmentSlot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();	

		if (equipmentSlot == this.slot) {
			multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.ordinal()], "Armor modifier", (double)this.getArmorValue(), 0));
			multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.ordinal()], "Armor toughness", this.getArmorToughness(), 0));
			multimap.put(AttributeMagicResist.instance().getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.ordinal()], "Magic Resist", (double)this.getMagicResistance(), 0));
		}

		return multimap;
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
		final @Nullable DragonEquipmentSlot dragonSlot = DragonEquipmentSlot.FindForSlot(equipmentSlot);
		if (dragonSlot != null) {
			return getAttributeModifiers(dragonSlot, stack);
		}
		
		return super.getAttributeModifiers(equipmentSlot, stack);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		// Disable vanilla's so we can do our own
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("HideFlags", 99)) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag == null) {
				tag = new NBTTagCompound();
			}
			
			tag.setInteger("HideFlags", 2);
			stack.setTagCompound(tag);
		}
		
		EntityPlayer player = NostrumMagica.proxy.getPlayer();
		
		// Copied from vanilla's ItemStack
		for (DragonEquipmentSlot dragonSlot : DragonEquipmentSlot.values())
		{
			Multimap<String, AttributeModifier> multimap = this.getAttributeModifiers(dragonSlot, stack);

			if (!multimap.isEmpty())
			{
				tooltip.add("");
				tooltip.add(I18n.format("item.modifiers.dragonslot." + dragonSlot.getName()));

				for (Entry<String, AttributeModifier> entry : multimap.entries())
				{
					AttributeModifier attributemodifier = (AttributeModifier)entry.getValue();
					double d0 = attributemodifier.getAmount();
					boolean flag = false;

					if (attributemodifier.getID() == Item.ATTACK_DAMAGE_MODIFIER)
					{
						d0 = d0 + player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
						d0 = d0 + (double)EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
						flag = true;
					}
					else if (attributemodifier.getID() == Item.ATTACK_SPEED_MODIFIER)
					{
						d0 += player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
						flag = true;
					}

					double d1;

					if (attributemodifier.getOperation() != 1 && attributemodifier.getOperation() != 2)
					{
						d1 = d0;
					}
					else
					{
						d1 = d0 * 100.0D;
					}

					if (flag)
					{
						tooltip.add(" " + I18n.format("attribute.modifier.equals." + attributemodifier.getOperation(), new Object[] {String.format("%.0f", d1), I18n.format("attribute.name." + (String)entry.getKey())}));
					}
					else if (d0 > 0.0D)
					{
						tooltip.add(TextFormatting.BLUE + " " + I18n.format("attribute.modifier.plus." + attributemodifier.getOperation(), new Object[] {String.format("%.0f", d1), I18n.format("attribute.name." + (String)entry.getKey())}));
					}
					else if (d0 < 0.0D)
					{
						d1 = d1 * -1.0D;
						tooltip.add(TextFormatting.RED + " " + I18n.format("attribute.modifier.take." + attributemodifier.getOperation(), new Object[] {String.format("%.0f", d1), I18n.format("attribute.name." + (String)entry.getKey())}));
					}
				}
			}
		}
		
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
}
