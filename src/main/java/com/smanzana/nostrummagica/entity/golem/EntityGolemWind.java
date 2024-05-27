package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellEffectPart;
import com.smanzana.nostrummagica.spells.components.SpellShapePart;
import com.smanzana.nostrummagica.spells.components.shapes.NostrumSpellShapes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class EntityGolemWind extends EntityGolem {
	
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

	public EntityGolemWind(EntityType<EntityGolemWind> type, World worldIn) {
		super(type, worldIn, EMagicElement.WIND, true, false, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		EntityGolemWind.init();
		
		float level = .1f;
		if (this.getHealth() < 8f)
			level = .4f;
		if (NostrumMagica.rand.nextFloat() < level) {
			spellPush.cast(this, 1.0f);
		} else {
			this.attackEntityAsMob(target);
		}
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		;
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		EntityGolemWind.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellBuff.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getActivePotionEffect(Effects.SPEED) == null;
	}

	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return EntityGolem.BuildBaseAttributes()
	        .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.33D)
	
	        .createMutableAttribute(Attributes.MAX_HEALTH, 16.0D)
	
	        .createMutableAttribute(Attributes.ATTACK_DAMAGE, 8.0D)
	        .createMutableAttribute(Attributes.ARMOR, 6.0D);
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
