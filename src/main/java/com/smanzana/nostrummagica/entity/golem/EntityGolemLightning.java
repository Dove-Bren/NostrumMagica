package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.spells.components.SpellEffectPart;
import com.smanzana.nostrummagica.spells.components.SpellShapePart;
import com.smanzana.nostrummagica.spells.components.shapes.NostrumSpellShapes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
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
			spellRanged1 = Spell.CreateAISpell("Lightning Strike");
			spellRanged1.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellRanged1.addPart(new SpellEffectPart(
					EMagicElement.LIGHTNING,
					1,
					EAlteration.CONJURE));
			
			spellRanged2 = Spell.CreateAISpell("Spark");
			spellRanged2.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
			spellRanged2.addPart(new SpellShapePart(NostrumSpellShapes.Burst, new SpellShapePartProperties(1, false)));
			spellRanged2.addPart(new SpellEffectPart(
					EMagicElement.LIGHTNING,
					1,
					null));
			
			spellBuff = Spell.CreateAISpell("Magic Ward");
			spellBuff.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellBuff.addPart(new SpellEffectPart(
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
