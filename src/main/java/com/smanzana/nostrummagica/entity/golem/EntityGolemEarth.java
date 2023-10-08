package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class EntityGolemEarth extends EntityGolem {
	
	public static final String ID = "earth_golem";
	
	private static Spell spellBuff1;
	private static Spell spellBuff2;
	
	private static void init() {
		if (spellBuff1 == null) {
			spellBuff1 = new Spell("Earthern Shield", true);
			spellBuff1.addPart(new SpellPart(AITargetTrigger.instance()));
			spellBuff1.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.EARTH,
					1,
					EAlteration.SUPPORT));
			
			spellBuff2 = new Spell("Impact Enchantment", true);
			spellBuff2.addPart(new SpellPart(AITargetTrigger.instance()));
			spellBuff2.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.EARTH,
					1,
					EAlteration.RESIST));
		}
	}

	public EntityGolemEarth(EntityType<EntityGolemEarth> type, World worldIn) {
		super(type, worldIn, EMagicElement.EARTH, true, false, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		this.attackEntityAsMob(target);
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		; // shoudln't happen
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		EntityGolemEarth.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		boolean canStrength = target.getActivePotionEffect(Effects.STRENGTH) == null;
		boolean canShield = target.getActivePotionEffect(NostrumEffects.physicalShield) == null;
		
		Spell spell;
		if (canStrength && canShield) {
			if (NostrumMagica.rand.nextBoolean())
				spell = spellBuff1;
			else
				spell = spellBuff2;
		} else if (canStrength)
			spell = spellBuff2;
		else
			spell = spellBuff1;	
		
		spell.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getActivePotionEffect(Effects.STRENGTH) == null
				|| target.getActivePotionEffect(NostrumEffects.physicalShield) == null;
	}

	@Override
	public void initGolemAttributes() {
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20D);

        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24.0D);

        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(12.0D);
	}

	@Override
	public String getTextureKey() {
		return "earth";
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (this.getOwnerId() == null) {
//			int count = this.rand.nextInt(3) + 1;
//			count += lootingModifier;
//			
//			this.entityDropItem(EssenceItem.getEssence(
//					EMagicElement.EARTH,
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
