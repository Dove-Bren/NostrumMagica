package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpell;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpellPart;
import com.smanzana.nostrummagica.spells.components.legacy.SingleShape;
import com.smanzana.nostrummagica.spells.components.legacy.triggers.AITargetTrigger;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.World;

public class EntityGolemIce extends EntityGolem {
	
	public static final String ID = "ice_golem";
	
	private static LegacySpell spellRange;
	private static LegacySpell spellBuff;
	
	private static void init() {
		if (spellRange == null) {
			spellRange = LegacySpell.CreateAISpell("Chill");
			spellRange.addPart(new LegacySpellPart(AITargetTrigger.instance()));
			spellRange.addPart(new LegacySpellPart(SingleShape.instance(),
					EMagicElement.ICE,
					1,
					EAlteration.INFLICT));
			
			spellBuff = LegacySpell.CreateAISpell("Aegis");
			spellBuff.addPart(new LegacySpellPart(AITargetTrigger.instance()));
			spellBuff.addPart(new LegacySpellPart(SingleShape.instance(),
					EMagicElement.ICE,
					1,
					EAlteration.SUPPORT));
		}
	}

	public EntityGolemIce(EntityType<EntityGolemIce> type, World worldIn) {
		super(type, worldIn, EMagicElement.ICE, true, true, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		this.attackEntityAsMob(target);
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		EntityGolemIce.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellRange.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		EntityGolemIce.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellBuff.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getActivePotionEffect(NostrumEffects.magicShield) == null;
	}

	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return EntityGolem.BuildBaseAttributes()
	        .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.22D)
	
	        .createMutableAttribute(Attributes.MAX_HEALTH, 20.0D)
	
	        .createMutableAttribute(Attributes.ATTACK_DAMAGE, 6.0D)
	        .createMutableAttribute(Attributes.ARMOR, 10.0D);
	}

	@Override
	public String getTextureKey() {
		return "ice";
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (this.getOwnerId() == null) {
//			int count = this.rand.nextInt(3) + 1;
//			count += lootingModifier;
//			
//			this.entityDropItem(EssenceItem.getEssence(
//					EMagicElement.ICE,
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
