package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.legacy.AoEShape;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpell;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpellPart;
import com.smanzana.nostrummagica.spells.components.legacy.SingleShape;
import com.smanzana.nostrummagica.spells.components.legacy.SpellPartProperties;
import com.smanzana.nostrummagica.spells.components.legacy.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.legacy.triggers.ProjectileTrigger;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.world.World;

public class EntityGolemLightning extends EntityGolem {
	
	public static final String ID = "lightning_golem";
	
	private static final AttributeModifier MOVEMENT_STORM_MODIFIER
		= new AttributeModifier("lightning_storm_boost", .2, AttributeModifier.Operation.MULTIPLY_BASE);
	
	private static LegacySpell spellRanged1;
	private static LegacySpell spellRanged2;
	private static LegacySpell spellBuff;
	
	private static void init() {
		if (spellRanged1 == null) {
			spellRanged1 = LegacySpell.CreateAISpell("Lightning Strike");
			spellRanged1.addPart(new LegacySpellPart(AITargetTrigger.instance()));
			spellRanged1.addPart(new LegacySpellPart(SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.CONJURE));
			
			spellRanged2 = LegacySpell.CreateAISpell("Spark");
			spellRanged2.addPart(new LegacySpellPart(ProjectileTrigger.instance()));
			spellRanged2.addPart(new LegacySpellPart(AoEShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					null,
					new SpellPartProperties(1, false)));
			
			spellBuff = LegacySpell.CreateAISpell("Magic Ward");
			spellBuff.addPart(new LegacySpellPart(AITargetTrigger.instance()));
			spellBuff.addPart(new LegacySpellPart(SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.RESIST));
		}
	}

	public EntityGolemLightning(EntityType<EntityGolemLightning> type, World worldIn) {
		super(type, worldIn, EMagicElement.LIGHTNING, false, true, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		; // Shouldn't happen. Can't.
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		EntityGolemLightning.init();
		
		// Pick a spell to do
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		if (NostrumMagica.rand.nextFloat() <= 0.3f) {
			spellRanged1.cast(this, 1.0f);
		} else {
			spellRanged2.cast(this, 1.0f);
		}
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		EntityGolemLightning.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellBuff.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getActivePotionEffect(NostrumEffects.magicResist) == null;
	}

	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return EntityGolem.BuildBaseAttributes()
	        .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.25D)
	
	        .createMutableAttribute(Attributes.MAX_HEALTH, 14.0D)
	
	        .createMutableAttribute(Attributes.ATTACK_DAMAGE, 2.0D)
	        .createMutableAttribute(Attributes.ARMOR, 6.0D);
	}

	@Override
	public String getTextureKey() {
		return "lightning";
	}
	
	@Override
	public void tick() {
		if (world.isRainingAt(this.getPosition())) {
			if (!this.getAttribute(Attributes.MOVEMENT_SPEED)
					.hasModifier(MOVEMENT_STORM_MODIFIER)) {
				this.getAttribute(Attributes.MOVEMENT_SPEED)
					.applyPersistentModifier(MOVEMENT_STORM_MODIFIER);
			}
		} else {
			if (this.getAttribute(Attributes.MOVEMENT_SPEED)
					.hasModifier(MOVEMENT_STORM_MODIFIER)) {
				this.getAttribute(Attributes.MOVEMENT_SPEED)
					.removeModifier(MOVEMENT_STORM_MODIFIER);
			}
		}
		
		super.tick();
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (this.getOwnerId() == null) {
//			int count = this.rand.nextInt(3) + 1;
//			count += lootingModifier;
//			
//			this.entityDropItem(EssenceItem.getEssence(
//					EMagicElement.LIGHTNING,
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
