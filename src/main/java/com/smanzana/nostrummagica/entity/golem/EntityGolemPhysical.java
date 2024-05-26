package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpell;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpellPart;
import com.smanzana.nostrummagica.spells.components.legacy.SingleShape;
import com.smanzana.nostrummagica.spells.components.legacy.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.legacy.triggers.ProjectileTrigger;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class EntityGolemPhysical extends EntityGolem {
	
	public static final String ID = "physical_golem";
	
	private static LegacySpell spellRanged;
	private static LegacySpell spellDebuff;
	
	private static void init() {
		if (spellRanged == null) {
			spellRanged = LegacySpell.CreateAISpell("Massive Blow");
			//spellRanged.addPart(new SpellPart()); should be projectile
			spellRanged.addPart(new LegacySpellPart(ProjectileTrigger.instance()));
			spellRanged.addPart(new LegacySpellPart(SingleShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					null));
			
			spellDebuff = LegacySpell.CreateAISpell("Corrupt Offense");
			spellDebuff.addPart(new LegacySpellPart(AITargetTrigger.instance()));
			spellDebuff.addPart(new LegacySpellPart(SingleShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					EAlteration.INFLICT));
		}
	}

	public EntityGolemPhysical(EntityType<EntityGolemPhysical> type, World worldIn) {
		super(type, worldIn, EMagicElement.PHYSICAL, true, true, false);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		this.attackEntityAsMob(target);
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		EntityGolemPhysical.init();
		
		// Either do debuff or damage
		if (target.getActivePotionEffect(Effects.WEAKNESS) == null) {
			LivingEntity targ = this.getAttackTarget();
			if (targ != target)
				this.setAttackTarget(target);
			spellDebuff.cast(this, 1.0f);
			if (targ != target)
				this.setAttackTarget(targ);
		} else {
			spellRanged.cast(this, 1.0f);
		}
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		; // shouldn't happen
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return true;
	}

	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return EntityGolem.BuildBaseAttributes()
	        .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.23D)
	
	        .createMutableAttribute(Attributes.MAX_HEALTH, 16.0D)
	
	        .createMutableAttribute(Attributes.ATTACK_DAMAGE, 6.0D)
	        .createMutableAttribute(Attributes.ARMOR, 8.0D);
	}

	@Override
	public String getTextureKey() {
		return "physical";
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (this.getOwnerId() == null) {
//			int count = this.rand.nextInt(3) + 1;
//			count += lootingModifier;
//			
//			this.entityDropItem(EssenceItem.getEssence(
//					EMagicElement.PHYSICAL,
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
