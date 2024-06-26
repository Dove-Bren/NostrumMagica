package com.smanzana.nostrummagica.entity.golem;

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

	public MagicPhysicalGolemEntity(EntityType<MagicPhysicalGolemEntity> type, World worldIn) {
		super(type, worldIn, EMagicElement.PHYSICAL, true, true, false);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		this.attackEntityAsMob(target);
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		MagicPhysicalGolemEntity.init();
		
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
		return MagicGolemEntity.BuildBaseAttributes()
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
