package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.World;

public class EntityGolemLightning extends EntityGolem {
	
	public static final String ID = "lightning_golem";
	
	private static final AttributeModifier MOVEMENT_STORM_MODIFIER
		= new AttributeModifier("lightning_storm_boost", .2, AttributeModifier.Operation.MULTIPLY_BASE);
	
	private static Spell spellRanged1;
	private static Spell spellRanged2;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellRanged1 == null) {
			spellRanged1 = new Spell("Lightning Strike", true);
			spellRanged1.addPart(new SpellPart(AITargetTrigger.instance()));
			spellRanged1.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.CONJURE));
			
			spellRanged2 = new Spell("Spark", true);
			spellRanged2.addPart(new SpellPart(ProjectileTrigger.instance()));
			spellRanged2.addPart(new SpellPart(AoEShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					null,
					new SpellPartParam(1, false)));
			
			spellBuff = new Spell("Magic Ward", true);
			spellBuff.addPart(new SpellPart(AITargetTrigger.instance()));
			spellBuff.addPart(new SpellPart(SingleShape.instance(),
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

	@Override
	public void initGolemAttributes() {
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);

        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14.0D);

        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
	}

	@Override
	public String getTextureKey() {
		return "lightning";
	}
	
	@Override
	public void tick() {
		if (world.isRainingAt(this.getPosition())) {
			if (!this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
					.hasModifier(MOVEMENT_STORM_MODIFIER)) {
				this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
					.applyModifier(MOVEMENT_STORM_MODIFIER);
			}
		} else {
			if (this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
					.hasModifier(MOVEMENT_STORM_MODIFIER)) {
				this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
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
