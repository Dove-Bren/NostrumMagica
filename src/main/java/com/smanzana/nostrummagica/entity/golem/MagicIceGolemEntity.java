package com.smanzana.nostrummagica.entity.golem;

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
import net.minecraft.world.World;

public class MagicIceGolemEntity extends MagicGolemEntity {
	
	public static final String ID = "ice_golem";
	
	private static Spell spellRange;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellRange == null) {
			spellRange = Spell.CreateAISpell("Chill");
			spellRange.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellRange.addPart(new SpellEffectPart(
					EMagicElement.ICE,
					1,
					EAlteration.INFLICT));
			
			spellBuff = Spell.CreateAISpell("Aegis");
			spellBuff.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellBuff.addPart(new SpellEffectPart(
					EMagicElement.ICE,
					1,
					EAlteration.SUPPORT));
		}
	}

	public MagicIceGolemEntity(EntityType<MagicIceGolemEntity> type, World worldIn) {
		super(type, worldIn, EMagicElement.ICE, true, true, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		this.attackEntityAsMob(target);
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		MagicIceGolemEntity.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellRange.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		MagicIceGolemEntity.init();
		
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
		return MagicGolemEntity.BuildBaseAttributes()
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
