package com.smanzana.nostrummagica.item.armor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DragonArmor extends Item {

	public static enum DragonEquipmentSlot {
		HELM(EquipmentSlot.HEAD),
		BODY(EquipmentSlot.CHEST),
		WINGS(EquipmentSlot.LEGS),
		CREST(EquipmentSlot.FEET);
		
		private final EquipmentSlot mirrorSlot;
		
		private DragonEquipmentSlot(EquipmentSlot mirrorSlot) {
			this.mirrorSlot = mirrorSlot;
		}
		
		public EquipmentSlot getMirrorSlot() {
			return mirrorSlot;
		}
		
		public static final @Nullable DragonEquipmentSlot FindForSlot(EquipmentSlot vanillaSlot) {
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
	
	protected static final String ID_PREFIX = "dragonarmor_";
	protected static final String ID_MIDFIX_HELM = "helm_";
	protected static final String ID_MIDFIX_BODY = "body_";
	protected static final String ID_MIDFIX_WINGS = "wings_";
	protected static final String ID_MIDFIX_CREST = "crest_";
	protected static final String ID_SUFFIX_GOLD = "gold";
	protected static final String ID_SUFFIX_IRON = "iron";
	protected static final String ID_SUFFIX_DIAMOND = "diamond";
	
	public static final String ID_HELM_GOLD = ID_PREFIX + ID_MIDFIX_HELM + ID_SUFFIX_GOLD;
	public static final String ID_HELM_IRON = ID_PREFIX + ID_MIDFIX_HELM + ID_SUFFIX_IRON;
	public static final String ID_HELM_DIAMOND = ID_PREFIX + ID_MIDFIX_HELM + ID_SUFFIX_DIAMOND;
	
	public static final String ID_BODY_GOLD = ID_PREFIX + ID_MIDFIX_BODY + ID_SUFFIX_GOLD;
	public static final String ID_BODY_IRON = ID_PREFIX + ID_MIDFIX_BODY + ID_SUFFIX_IRON;
	public static final String ID_BODY_DIAMOND = ID_PREFIX + ID_MIDFIX_BODY + ID_SUFFIX_DIAMOND;
	
	public static final String ID_WINGS_GOLD = ID_PREFIX + ID_MIDFIX_WINGS + ID_SUFFIX_GOLD;
	public static final String ID_WINGS_IRON = ID_PREFIX + ID_MIDFIX_WINGS + ID_SUFFIX_IRON;
	public static final String ID_WINGS_DIAMOND = ID_PREFIX + ID_MIDFIX_WINGS + ID_SUFFIX_DIAMOND;
	
	public static final String ID_CREST_GOLD = ID_PREFIX + ID_MIDFIX_CREST + ID_SUFFIX_GOLD;
	public static final String ID_CREST_IRON = ID_PREFIX + ID_MIDFIX_CREST + ID_SUFFIX_IRON;
	public static final String ID_CREST_DIAMOND = ID_PREFIX + ID_MIDFIX_CREST + ID_SUFFIX_DIAMOND;
	
	public static final List<DragonArmor> GetAllArmors() {
		List<DragonArmor> list = new ArrayList<>();
		for (DragonArmorMaterial material : DragonArmorMaterial.values()) {
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				DragonArmor armor = GetArmor(slot, material);
				if (armor != null) {
					list.add(armor);
				}
			}
		}
		return list;
	}
	
	public static final DragonArmor GetArmor(DragonEquipmentSlot slot, DragonArmorMaterial material) {
		switch (slot) {
		case HELM:
			switch (material) {
			case IRON:
				return NostrumItems.dragonArmorHelmIron;
			case GOLD:
				return NostrumItems.dragonArmorHelmGold;
			case DIAMOND:
				return NostrumItems.dragonArmorHelmDiamond;
			}
			break;
		case BODY:
			switch (material) {
			case IRON:
				return NostrumItems.dragonArmorBodyIron;
			case GOLD:
				return NostrumItems.dragonArmorBodyGold;
			case DIAMOND:
				return NostrumItems.dragonArmorBodyDiamond;
			}
			break;
		case WINGS:
			switch (material) {
			case IRON:
				//return NostrumItems.dragonArmorWingsIron;
			case GOLD:
				//return NostrumItems.dragonArmorWingsGold;
			case DIAMOND:
				//return NostrumItems.dragonArmorWingsDiamond;
			}
			break;
		case CREST:
			switch (material) {
			case IRON:
				//return NostrumItems.dragonArmorCrestIron;
			case GOLD:
				//return NostrumItems.dragonArmorCrestGold;
			case DIAMOND:
				//return NostrumItems.dragonArmorCrestDiamond;
			}
			break;
		}
		
		return null;
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
	
	protected static final Rarity CalcVanillaRarity(DragonEquipmentSlot slot, DragonArmorMaterial material) {
		// Iron and Gold will be uncommon, diamond will be rare
		switch (material) {
		case GOLD:
		case IRON:
		default:
			return Rarity.UNCOMMON;
		case DIAMOND:
			return Rarity.RARE;
		}
	}
	
	protected final DragonEquipmentSlot slot;
	protected final DragonArmorMaterial material;
	
	protected final int defaultArmor;
	protected final double defaultMagicResist;
	protected final int defaultArmorToughness;
	
	public DragonArmor(DragonEquipmentSlot slot, DragonArmorMaterial material) {
		super(NostrumItems.PropEquipment().rarity(CalcVanillaRarity(slot, material)));
		this.slot = slot;
		this.material = material;
		
		defaultArmor = CalcArmor(slot, material);
		defaultMagicResist = CalcMagicResist(slot, material);
		defaultArmorToughness = CalcArmorToughness(slot, material);
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
	
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(DragonEquipmentSlot equipmentSlot, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();	

		if (equipmentSlot == this.slot) {
			multimap.put(Attributes.ARMOR, new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.ordinal()], "Armor modifier", (double)this.getArmorValue(), AttributeModifier.Operation.ADDITION));
			multimap.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.ordinal()], "Armor toughness", this.getArmorToughness(), AttributeModifier.Operation.ADDITION));
			multimap.put(NostrumAttributes.magicResist, new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.ordinal()], "Magic Resist", (double)this.getMagicResistance(), AttributeModifier.Operation.ADDITION));
		}

		return multimap;
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot, ItemStack stack) {
		final @Nullable DragonEquipmentSlot dragonSlot = DragonEquipmentSlot.FindForSlot(equipmentSlot);
		if (dragonSlot != null) {
			return getAttributeModifiers(dragonSlot, stack);
		}
		
		return super.getAttributeModifiers(equipmentSlot, stack);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		// Disable vanilla's so we can do our own
		if (!stack.hasTag() || !stack.getTag().contains("HideFlags", 99)) {
			CompoundTag tag = stack.getTag();
			if (tag == null) {
				tag = new CompoundTag();
			}
			
			tag.putInt("HideFlags", 2);
			stack.setTag(tag);
		}
		
		Player player = NostrumMagica.Proxy.getPlayer();
		
		// Copied from vanilla's ItemStack
		for (DragonEquipmentSlot dragonSlot : DragonEquipmentSlot.values())
		{
			Multimap<Attribute, AttributeModifier> multimap = this.getAttributeModifiers(dragonSlot, stack);

			if (!multimap.isEmpty())
			{
				tooltip.add(new TextComponent(""));
				tooltip.add(new TranslatableComponent("item.nostrummagica.modifiers.dragonslot." + dragonSlot.getName()));

				for (Entry<Attribute, AttributeModifier> entry : multimap.entries())
				{
					AttributeModifier attributemodifier = (AttributeModifier)entry.getValue();
					double d0 = attributemodifier.getAmount();
					boolean flag = false;

					if (attributemodifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID)
					{
						d0 = d0 + player.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
						d0 = d0 + (double)EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
						flag = true;
					}
					else if (attributemodifier.getId() == Item.BASE_ATTACK_SPEED_UUID)
					{
						d0 += player.getAttribute(Attributes.ATTACK_SPEED).getBaseValue();
						flag = true;
					}

					double d1;

					if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL)
					{
						d1 = d0;
					}
					else
					{
						d1 = d0 * 100.0D;
					}
					
					if (flag)
					{
						tooltip.add((new TextComponent(" ")).append(new TranslatableComponent("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent("attribute.name." + entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
					}
					else if (d0 > 0.0D)
					{
						tooltip.add((new TranslatableComponent("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.BLUE));
					}
					else if (d0 < 0.0D)
					{
						d1 = d1 * -1.0D;
						tooltip.add((new TranslatableComponent("attribute.modifier.take." + attributemodifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.RED));
					}
				}
			}
		}
		
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}
	
}
