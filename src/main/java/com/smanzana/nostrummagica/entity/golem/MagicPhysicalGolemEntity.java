package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

public class MagicPhysicalGolemEntity extends MagicGolemEntity {
	
	public static final String ID = "physical_golem";
	
	private static Spell spellRanged;
	private static Spell spellDebuff;
	
	private static void init() {
		if (spellRanged == null) {
			spellRanged = Spell.CreateAISpell("Massive Blow");
			//spellRanged.addPart(new SpellPart()); should be projectile
			spellRanged.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
			spellRanged.addPart(new SpellEffectPart(
					EMagicElement.PHYSICAL,
					1,
					null));
			
			spellDebuff = Spell.CreateAISpell("Corrupt Offense");
			spellDebuff.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellDebuff.addPart(new SpellEffectPart(
					EMagicElement.PHYSICAL,
					1,
					EAlteration.INFLICT));
		}
	}

	public MagicPhysicalGolemEntity(EntityType<MagicPhysicalGolemEntity> type, Level worldIn) {
		super(type, worldIn, EMagicElement.PHYSICAL, true, true, false);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		this.doHurtTarget(target);
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		MagicPhysicalGolemEntity.init();
		
		// Either do debuff or damage
		if (target.getEffect(MobEffects.WEAKNESS) == null) {
			LivingEntity targ = this.getTarget();
			if (targ != target)
				this.setTarget(target);
			spellDebuff.cast(this, 1.0f);
			if (targ != target)
				this.setTarget(targ);
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

	public static final AttributeSupplier.Builder BuildAttributes() {
		return MagicGolemEntity.BuildBaseAttributes()
	        .add(Attributes.MOVEMENT_SPEED, 0.23D)
	
	        .add(Attributes.MAX_HEALTH, 16.0D)
	
	        .add(Attributes.ATTACK_DAMAGE, 6.0D)
	        .add(Attributes.ARMOR, 8.0D);
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
