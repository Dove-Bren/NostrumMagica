package com.smanzana.nostrummagica.attribute;

import java.util.UUID;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * Intended to be put on weapons to cause magic damage when used
 */
public class MagicAttackDamageAttribute extends RangedAttribute {
	
	public static final String ID_PREFIX = "magic_attack_damage_";
	
	public static final UUID UUID_BASE_WEAPON_ATTACK_NEUTRAL = UUID.fromString("e741f787-74e0-47ed-ad63-1372649fe542");
	public static final UUID UUID_BASE_WEAPON_ATTACK_FIRE = UUID.fromString("5c9e1e36-b28a-4b59-9c12-b6324084d02a");
	public static final UUID UUID_BASE_WEAPON_ATTACK_ICE = UUID.fromString("f1fcda27-1e29-4186-a776-067f9a536f86");
	public static final UUID UUID_BASE_WEAPON_ATTACK_EARTH = UUID.fromString("5175e535-c5c9-4ad6-bbc2-9bc4cf3a686c");
	public static final UUID UUID_BASE_WEAPON_ATTACK_WIND = UUID.fromString("c072e6d0-c990-4b8b-bc0b-24c8a83789e4");
	public static final UUID UUID_BASE_WEAPON_ATTACK_ENDER = UUID.fromString("33f0e377-c333-4c88-8b65-d028d061d627");
	public static final UUID UUID_BASE_WEAPON_ATTACK_LIGHTNING = UUID.fromString("dfb3d94c-5c60-4dfd-9aac-73a96afa6894");
	
	private final EMagicElement element;
	
	public MagicAttackDamageAttribute(EMagicElement elem, String name) {
		super(name, 0, 0.0D, 100.0D);
		this.element = elem;
		this.setSyncable(true);
	}
	
	public EMagicElement getElement() {
		return element;
	}
}
