package com.smanzana.nostrummagica.items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellAction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class EnchantedWeapon extends ItemSword implements EnchantedEquipment {

	private static Map<EMagicElement, Map<Integer, EnchantedWeapon>> items;
	
	public static final void registerWeapons() {
		items = new EnumMap<EMagicElement, Map<Integer, EnchantedWeapon>>(EMagicElement.class);
		
		for (EMagicElement element : EMagicElement.values()) {
			if (isWeaponElement(element)) {
				items.put(element, new HashMap<Integer, EnchantedWeapon>());
				for (int i = 0; i < 3; i++) {
					ResourceLocation location = new ResourceLocation(NostrumMagica.MODID, "sword_" + element.name().toLowerCase() + (i + 1));
					EnchantedWeapon weapon =  new EnchantedWeapon(location.getResourcePath(), element, i + 1);
					weapon.setUnlocalizedName(location.getResourcePath());
					GameRegistry.register(weapon, location);
					items.get(element).put(i + 1, weapon);
				}
			}
		}
	}
	
	public static boolean isWeaponElement(EMagicElement element) {
		switch (element) {
		case EARTH:
		case ENDER:
		case FIRE:
		case PHYSICAL:
		default:
			return false;
		case WIND:
		case ICE:
		case LIGHTNING:
			return true;
		}
	}
	
	private static int calcDamage(EMagicElement element, int level) {
		
		float mod;
		
		switch (element) {
		case WIND:
			mod = 1.2f;
			break;
		case LIGHTNING:
			mod = 0.9f;
			break;
		case ICE:
			mod = 1.1f;
			break;
		default:
			mod = 1.0f;
		}
		
		int base = 5 + (level * 2);
		
		return (int) ((float) base * mod);
	}
	
	private int level;
	private int damage;
	private EMagicElement element;
	
	private String modelID;
	
	public EnchantedWeapon(String modelID, EMagicElement element, int level) {
		super(ToolMaterial.DIAMOND);
		
		this.level = level;
		this.element = element;
		this.modelID = modelID;
		
		this.setMaxDamage(400);
		this.setCreativeTab(NostrumMagica.creativeTab);
		
		this.damage = calcDamage(element, level);
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND)
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
        }

        return multimap;
    }
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

	@Override
	public SpellAction getTriggerAction(EntityLivingBase user, boolean offense) {
		if (!offense)
			return null;
		
		SpellAction action = null;
		switch (element) {
		case EARTH:
		case ENDER:
		case FIRE:
		case PHYSICAL:
			break;
		case WIND:
			if (NostrumMagica.rand.nextFloat() < 0.35f * level)
				action = new SpellAction(user).propel(1);
			break;
		case LIGHTNING:
			if (NostrumMagica.rand.nextFloat() < 0.1f * level)
				action = new SpellAction(user).lightning();
			break;
		case ICE:
			if (NostrumMagica.rand.nextFloat() < 0.5f * level)
				action = new SpellAction(user).status(
						FrostbitePotion.instance(), 5 * 20, level > 2 ? level - 2 : 0);
			break;
		default:
			break;
		
		}
		
		return action;
	}

	@Override
	public boolean shouldTrigger(boolean offense) {
		return offense;
	}
	
	public static EnchantedWeapon get(EMagicElement element, int level) {
		if (items.containsKey(element))
			return items.get(element).get(level);
		
		return null;
	}
	
	public static List<EnchantedWeapon> getAll() {
		List<EnchantedWeapon> list = new LinkedList<>();
		
		for (EMagicElement element : EMagicElement.values())
		if (isWeaponElement(element)) {
			list.addAll(items.get(element).values());
		}
		
		return list;
	}
	
	public String getModelID() {
		return modelID;
	}
	
}
