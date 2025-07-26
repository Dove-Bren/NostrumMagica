package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCastProperties;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

public class MagicNeutralGolemEntity extends MagicGolemEntity {
	
	public static final String ID = "neutral_golem";
	
	private static Spell spellRanged;
	private static Spell spellDebuff;
	
	private static void init() {
		if (spellRanged == null) {
			spellRanged = Spell.CreateAISpell("Massive Blow");
			//spellRanged.addPart(new SpellPart()); should be projectile
			spellRanged.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
			spellRanged.addPart(new SpellEffectPart(
					EMagicElement.NEUTRAL,
					1,
					EAlteration.HARM));
			
			spellDebuff = Spell.CreateAISpell("Corrupt Offense");
			spellDebuff.addPart(new SpellShapePart(NostrumSpellShapes.SeekingBullet));
			spellDebuff.addPart(new SpellEffectPart(
					EMagicElement.NEUTRAL,
					1,
					EAlteration.INFLICT));
		}
	}

	public MagicNeutralGolemEntity(EntityType<MagicNeutralGolemEntity> type, Level worldIn) {
		super(type, worldIn, EMagicElement.NEUTRAL, true, true, false);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		this.doHurtTarget(target);
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		MagicNeutralGolemEntity.init();
		
		// Either do debuff or damage
		if (target.getEffect(MobEffects.WEAKNESS) == null) {
			LivingEntity targ = this.getTarget();
			if (targ != target)
				this.setTarget(target);
			spellDebuff.cast(this, SpellCastProperties.BASE);
			if (targ != target)
				this.setTarget(targ);
		} else {
			spellRanged.cast(this, SpellCastProperties.BASE);
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

	public static final AttributeSupplier.Builder BuildAttributes() {
		return MagicGolemEntity.BuildBaseAttributes(EMagicElement.NEUTRAL)
	        .add(Attributes.MOVEMENT_SPEED, 0.23D)
	
	        .add(Attributes.MAX_HEALTH, 16.0D)
	
	        .add(Attributes.ATTACK_DAMAGE, 6.0D)
	        .add(Attributes.ARMOR, 8.0D);
	}

	@Override
	public String getTextureKey() {
		return "neutral";
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (this.getOwnerId() == null) {
//			int count = this.rand.nextInt(3) + 1;
//			count += lootingModifier;
//			
//			this.entityDropItem(EssenceItem.getEssence(
//					EMagicElement.NEUTRAL,
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
