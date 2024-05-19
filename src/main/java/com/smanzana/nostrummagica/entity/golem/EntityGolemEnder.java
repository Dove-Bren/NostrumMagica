package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.SpellPart;
import com.smanzana.nostrummagica.spells.SpellPartProperties;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class EntityGolemEnder extends EntityGolem {
	
	public static final String ID = "ender_golem";
	
	private static Spell spellRange;
	private static Spell spellDebuff;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellRange == null) {
			spellRange = Spell.CreateAISpell("Overwhelm");
			spellRange.addPart(new SpellPart(AITargetTrigger.instance()));
			spellRange.addPart(new SpellPart(AoEShape.instance(),
					EMagicElement.ENDER,
					1,
					null,
					new SpellPartProperties(2, false)));
			
			spellDebuff = Spell.CreateAISpell("Blind");
			spellDebuff.addPart(new SpellPart(AITargetTrigger.instance()));
			spellDebuff.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.ENDER,
					1,
					EAlteration.INFLICT));
			
			spellBuff = Spell.CreateAISpell("Cloak");
			spellBuff.addPart(new SpellPart(AITargetTrigger.instance()));
			spellBuff.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.ENDER,
					1,
					EAlteration.RESIST));
		}
	}

	public EntityGolemEnder(EntityType<EntityGolemEnder> type, World worldIn) {
		super(type, worldIn, EMagicElement.ENDER, false, true, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		;
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		EntityGolemEnder.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		if (NostrumMagica.rand.nextBoolean()) {
			spellDebuff.cast(this, 1.0f);
		} else {
			spellRange.cast(this, 1.0f);
		}
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		EntityGolemEnder.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellBuff.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getActivePotionEffect(Effects.INVISIBILITY) == null;
	}

	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return EntityGolem.BuildBaseAttributes()
	        .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.30D)
	
	        .createMutableAttribute(Attributes.MAX_HEALTH, 30.0D)
	
	        .createMutableAttribute(Attributes.ATTACK_DAMAGE, 2.0D)
	        .createMutableAttribute(Attributes.ARMOR, 4.0D);
	}

	@Override
	public String getTextureKey() {
		return "ender";
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (this.getOwnerId() == null) {
//			int count = this.rand.nextInt(3) + 1;
//			count += lootingModifier;
//			
//			this.entityDropItem(EssenceItem.getEssence(
//					EMagicElement.ENDER,
//					count), 0);
//			
//			int denom = ROSE_DROP_DENOM;
//			if (wasRecentlyHit) {
//				denom = 150;
//			}
//			
//			if (this.rand.nextInt(denom - (lootingModifier * 20)) == 0) {
//				this.entityDropItem(NostrumRoseItem.getItem(RoseType.ELDRICH, 1), 0);
//			}
//		}
//	}
}
