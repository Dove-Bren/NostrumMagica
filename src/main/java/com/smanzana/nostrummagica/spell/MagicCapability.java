package com.smanzana.nostrummagica.spell;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.research.NostrumResearches;
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
	INCANT_ENABLED(EMagicTier.MANI), // Whether incanting can be performed
	SCROLLCAST_ENABLED(EMagicTier.MANI), // Whether casting from a scroll is possible
	MARKRECALL(NostrumResearches.ID_Markrecall),
	ADVANCED_MARKRECALL(NostrumResearches.ID_Adv_Markrecall),
	RITUAL_ENABLED(NostrumResearches.ID_Rituals),
	ELEMENTAL_TRIALS(NostrumResearches.ID_Elemental_Trials),
	//PORTAL_SPAWNING(),
	
	// General spell casting
	SPELLCAST_OVERCHARGE(() -> NostrumSkills.Spellcasting_Overcharge), // Can continue casting for extra time to increase potency
	SPELLCAST_FAST_OVERCHARGE(() -> NostrumSkills.Spellcasting_FastOvercharge), // Whether overcharge cast times are reduced
	SPELLCAST_STRONG_OVERCHARGE(() -> NostrumSkills.Spellcasting_StrongOvercharge), // Whether overcharge cast times are reduced
	SPELLCAST_PRECAST(EMagicTier.VANI), // Can precast a spell, allowing nearly-instant casting later
	SPELLCAST_ELEMLINGER(() -> NostrumSkills.Spellcasting_ElemLinger), // Puts elemental residue that boosts subsequent spells of the right elem
	SPELLCAST_ELEMLINGEREATER(() -> NostrumSkills.Spellcasting_ElemLingerEater), // Mana refund + swift casting when consuming
	//SPELLCAST_SPEEDACTION(),
	//SPELLCAST_POTENTACTION(),
	
	// Casting advanced actions
	INCANT_COMPONENT_SELECT(EMagicTier.MANI), // Can select shape, element, alteration incanting
	INCANT_ALTERATIONS(INCANT_COMPONENT_SELECT), // Can select alterations when incanting
	INCANT_TWOSHAPES(() -> NostrumSkills.Incanting_TwoShapes), // Can select up to 2 shapes when incanting
	INCANT_QUICKCAST(EMagicTier.KANI), // Can quick-press incant button to incant the last incantation
	INCANT_OVERCHARGE(SPELLCAST_OVERCHARGE), // Same as SPELLCAST_ variant but for incantations
	INCANT_ALLSHAPES(() -> NostrumSkills.Incanting_AllShapes), // Can select any shape instead of the 3 primary shapes when incanting
	INCANT_SELECT_INFO(() -> NostrumSkills.Incanting_SelectInfo), // Display more info when selecting incantation components
	INCANT_TOME_ENHANCEMENTS(() -> NostrumSkills.Incanting_TomeUse), // Allow tome enhancements to benefit incantation casting
	CRAFTCAST_TOME_NOHANDS(() -> NostrumSkills.Craftcast_TomeHands), // Allow casting crafted spells with no speed penalty if a tome is equipped
	
	// Spellcraft
	SPELLCRAFT_ENABLED(NostrumResearches.ID_Spellcraft),
	
	// Adventure
	ESSENCE_EATER(NostrumResearches.ID_Essence_Eater),
	
	;
	
	private @Nullable Skill skill;
	private final @Nullable Supplier<Skill> skillSupplier;
	private final @Nullable EMagicTier tier;
	private final @Nullable MagicCapability copyOf;
	private final @Nullable ResourceLocation research;
	
	private MagicCapability(Supplier<Skill> skillSupplier, EMagicTier tier, ResourceLocation research, MagicCapability copyOf) {
		this.skillSupplier = skillSupplier;
		this.tier = tier;
		this.copyOf = copyOf;
		this.research = research;
	}
	
	private MagicCapability(Supplier<Skill> skillSupplier) {
		this(skillSupplier, null, null, null);
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
		// Fix up skill
		if (this.skill == null && this.skillSupplier != null) {
			this.skill = this.skillSupplier.get();
		}
		
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
