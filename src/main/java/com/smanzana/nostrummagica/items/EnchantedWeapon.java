package com.smanzana.nostrummagica.items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellAction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
	
	protected static final UUID OFFHAND_ATTACK_SPEED_MODIFIER = UUID.fromString("B2879ABC-4180-1234-B01B-487954A3BAC4");
	
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
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

		if (slot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage, 0));
			
			final double amt;
			switch (element) {
			case ICE:
				if (level == 1) {
					amt = 2.6; // Mace only slightly slower than sword
				} else if (level == 2) {
					amt = 3.3; // Morning star slower than vanilla axe!
				} else {
					amt = 2.8; // Scepter slower than mace, but not TOO slow
				}
				break;
			case LIGHTNING:
				if (level == 1) {
					amt = 1.5; // Knife is very fast! 2.5 attacks per second (vanilla sword is 1.6)
				} else if (level == 2) {
					amt = 2.0; // Dagger slower, but still faster than sword
				} else {
					amt = 1.6; // Stiletto very fast! Lightning fast, even! lol
				}
				break;
			case WIND:
				if (level == 1) {
					amt = 2.4; // Slasher is same as vanilla sword. Heavier, but powered by wind!
				} else if (level == 2) {
					amt = 2.6; // Slave is a tad slower
				} else {
					amt = 2.1; // Striker is very fast :)
				}
				break;
			default:
				amt = 2.4;
			}
			
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -amt, 0));
		} else if (slot == EntityEquipmentSlot.OFFHAND && element == EMagicElement.WIND) {
			final double amt = level * 0.1;
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(OFFHAND_ATTACK_SPEED_MODIFIER, "Weapon modifier", amt, 0));
		}

		return multimap;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

	@Override
	public SpellAction getTriggerAction(EntityLivingBase user, boolean offense, ItemStack stack) {
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
			// now just does on right click
//			if (NostrumMagica.rand.nextFloat() < 0.35f * level)
//				action = new SpellAction(user).push(5f, level);
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
	public boolean shouldTrigger(boolean offense, ItemStack stack) {
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
	
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (element == EMagicElement.WIND) {
			SpellAction fly = new SpellAction(playerIn);
			fly.push(5.0f, level);
			fly.apply(worldIn, pos, 1.0f);
			stack.damageItem(3, playerIn);
			return EnumActionResult.SUCCESS;
		}

        return EnumActionResult.PASS;
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		if (element == EMagicElement.WIND) {
			SpellAction fly = new SpellAction(playerIn);
			fly.push(5.0f, level);
			fly.apply(target, 1.0f);
			stack.damageItem(2, playerIn);
			return true;
		}

        return false;
	}
	
}
