package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class EntityGolemEarth extends EntityGolem {
	
	public static final String ID = "earth_golem";
	
	private static Spell spellBuff1;
	private static Spell spellBuff2;
	
	private static void init() {
		if (spellBuff1 == null) {
			spellBuff1 = Spell.CreateAISpell("Earthern Shield");
			spellBuff1.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellBuff1.addPart(new SpellEffectPart(
					EMagicElement.EARTH,
					1,
					EAlteration.SUPPORT));
			
			spellBuff2 = Spell.CreateAISpell("Impact Enchantment");
			spellBuff2.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellBuff2.addPart(new SpellEffectPart(
					EMagicElement.EARTH,
					1,
					EAlteration.RESIST));
		}
	}

	public EntityGolemEarth(EntityType<EntityGolemEarth> type, World worldIn) {
		super(type, worldIn, EMagicElement.EARTH, true, false, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		this.attackEntityAsMob(target);
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		; // shoudln't happen
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		EntityGolemEarth.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		boolean canStrength = target.getActivePotionEffect(Effects.STRENGTH) == null;
		boolean canShield = target.getActivePotionEffect(NostrumEffects.physicalShield) == null;
		
		Spell spell;
		if (canStrength && canShield) {
			if (NostrumMagica.rand.nextBoolean())
				spell = spellBuff1;
			else
				spell = spellBuff2;
		} else if (canStrength)
			spell = spellBuff2;
		else
			spell = spellBuff1;	
		
		spell.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getActivePotionEffect(Effects.STRENGTH) == null
				|| target.getActivePotionEffect(NostrumEffects.physicalShield) == null;
	}

	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return EntityGolem.BuildBaseAttributes()
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.20D)

				.createMutableAttribute(Attributes.MAX_HEALTH, 24.0D)

				.createMutableAttribute(Attributes.ATTACK_DAMAGE, 4.0D)
				.createMutableAttribute(Attributes.ARMOR, 12.0D);
	}

	@Override
	public String getTextureKey() {
		return "earth";
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (this.getOwnerId() == null) {
//			int count = this.rand.nextInt(3) + 1;
//			count += lootingModifier;
//			
//			this.entityDropItem(EssenceItem.getEssence(
//					EMagicElement.EARTH,
//					count), 0);
//			
//			int denom = ROSE_DROP_DENOM;
//			if (wasRecentlyHit) {
//				denom = 150;
//			}
//			
//			if (this.rand.nextInt(denom - (lootingModifier * 20)) == 0) {
//				this.entityDropItem(NostrumRoseItem.getItem(RoseType.PALE, 1), 0);
//			}
//		}
//	}
}
