package com.smanzana.nostrummagica.spell;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.progression.skill.Skill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Intended to be one spot that ties together things like tier and skills to determine if a player has a given capability.
 * This way, code doesn't have to be checking random skills in the meat of the implementation of effects and can instead
 * more clearly check whether the player has a capability.
 */
public enum MagicCapability {
	// Basics of using the mod
	INCANT_ENABLED(EMagicTier.MANI),
	SCROLLCAST_ENABLED(EMagicTier.MANI),
	
	// General spell casting
	SPELLCAST_OVERCHARGE(EMagicTier.VANI),
	//SPELLCAST_SPEEDACTION(),
	//SPELLCAST_POTENTACTION(),
	
	// Casting advanced actions
	INCANT_COMPONENT_SELECT(EMagicTier.MANI),
	INCANT_ALTERATIONS(EMagicTier.MANI),
	INCANT_TWOSHAPES(NostrumSkills.Incanting_TwoShapes), // Prev was VANI
	INCANT_QUICKCAST(INCANT_COMPONENT_SELECT),
	INCANT_OVERCHARGE(SPELLCAST_OVERCHARGE),
	INCANT_ALLSHAPES(EMagicTier.KANI),
	
	;
	
	private final @Nullable Skill skill;
	private final @Nullable EMagicTier tier;
	private final @Nullable MagicCapability copyOf;
	private final @Nullable ResourceLocation research;
	
	private MagicCapability(Skill skill, EMagicTier tier, ResourceLocation research, MagicCapability copyOf) {
		this.skill = skill;
		this.tier = tier;
		this.copyOf = copyOf;
		this.research = research;
	}
	
	private MagicCapability(Skill skill) {
		this(skill, null, null, null);
	}
	
	private MagicCapability(EMagicTier tier) {
		this(null, tier, null, null);
	}
	
	private MagicCapability(MagicCapability other) {
		this(null, null, null, other);
	}
	
	private MagicCapability(ResourceLocation research) {
		this(null, null, research, null);
	}
	
	public boolean matches(@Nullable INostrumMagic attr) {
		if (this.copyOf != null) {
			if (!copyOf.matches(attr)) {
				return false;
			}
		}
		
		if (this.tier != null) {
			if (attr == null || !attr.getTier().isGreaterOrEqual(this.tier)) {
				return false;
			}
		}
		
		if (this.skill != null) {
			if (attr == null || !attr.hasSkill(skill)) {
				return false;
			}
		}
		
		if (this.research != null) {
			if (attr == null || !attr.getCompletedResearches().contains(this.research)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean matches(LivingEntity entity) {
		return matches(NostrumMagica.getMagicWrapper(entity));
	}
	
}
