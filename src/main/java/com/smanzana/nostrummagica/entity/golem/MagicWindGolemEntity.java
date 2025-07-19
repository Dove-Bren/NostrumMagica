package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
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

public class MagicWindGolemEntity extends MagicGolemEntity {
	
	public static final String ID = "wind_golem";
	
	private static Spell spellPush;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellPush == null) {
			spellPush = Spell.CreateAISpell("Gust");
			spellPush.addPart(new SpellEffectPart(
					EMagicElement.WIND,
					1,
					EAlteration.RESIST));
			
			spellBuff = Spell.CreateAISpell("Speed");
			spellBuff.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellBuff.addPart(new SpellEffectPart(
					EMagicElement.WIND,
					1,
					EAlteration.SUPPORT));
		}
	}

	public MagicWindGolemEntity(EntityType<MagicWindGolemEntity> type, Level worldIn) {
		super(type, worldIn, EMagicElement.WIND, true, false, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		MagicWindGolemEntity.init();
		
		float level = .1f;
		if (this.getHealth() < 8f)
			level = .4f;
		if (NostrumMagica.rand.nextFloat() < level) {
			spellPush.cast(this, SpellCastProperties.BASE);
		} else {
			this.doHurtTarget(target);
		}
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		;
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		MagicWindGolemEntity.init();
		
		LivingEntity targ = this.getTarget();
		if (targ != target)
			this.setTarget(target);
		
		spellBuff.cast(this, SpellCastProperties.BASE);
		
		if (targ != target)
			this.setTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getEffect(MobEffects.MOVEMENT_SPEED) == null;
	}

	public static final AttributeSupplier.Builder BuildAttributes() {
		return MagicGolemEntity.BuildBaseAttributes(EMagicElement.WIND)
	        .add(Attributes.MOVEMENT_SPEED, 0.33D)
	
	        .add(Attributes.MAX_HEALTH, 16.0D)
	
	        .add(Attributes.ATTACK_DAMAGE, 8.0D)
	        .add(Attributes.ARMOR, 6.0D);
	}

	@Override
	public String getTextureKey() {
		return "wind";
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (this.getOwnerId() == null) {
//			int count = this.rand.nextInt(3) + 1;
//			count += lootingModifier;
//			
//			this.entityDropItem(EssenceItem.getEssence(
//					EMagicElement.WIND,
//					count), 0);
//			
//			int denom = ROSE_DROP_DENOM;
//			if (wasRecentlyHit) {
//				denom = 150;
//			}
//			
//			if (this.rand.nextInt(denom - (lootingModifier * 20)) == 0) {
//				this.entityDropItem(NostrumRoseItem.getItem(RoseType.BLOOD, 1), 0);
//			}
//		}
//	}
}
