package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.effects.MagicShieldEffect;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem.RoseType;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntityGolemIce extends EntityGolem {
	
	public static final String ID = "ice_golem";
	
	private static Spell spellRange;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellRange == null) {
			spellRange = new Spell("Chill", true);
			spellRange.addPart(new SpellPart(AITargetTrigger.instance()));
			spellRange.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.ICE,
					1,
					EAlteration.INFLICT));
			
			spellBuff = new Spell("Aegis", true);
			spellBuff.addPart(new SpellPart(AITargetTrigger.instance()));
			spellBuff.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.ICE,
					1,
					EAlteration.SUPPORT));
		}
	}

	public EntityGolemIce(EntityType<EntityGolemIce> type, World worldIn) {
		super(type, worldIn, EMagicElement.ICE, true, true, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		this.attackEntityAsMob(target);
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		EntityGolemIce.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellRange.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		EntityGolemIce.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellBuff.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getActivePotionEffect(MagicShieldEffect.instance()) == null;
	}

	@Override
	public void initGolemAttributes() {
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.22D);

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);

        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10.0D);
	}

	@Override
	public String getTextureKey() {
		return "ice";
	}
	
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		if (this.getOwnerId() == null) {
			int count = this.rand.nextInt(3) + 1;
			count += lootingModifier;
			
			this.entityDropItem(EssenceItem.getEssence(
					EMagicElement.ICE,
					count), 0);
			
			int denom = ROSE_DROP_DENOM;
			if (wasRecentlyHit) {
				denom = 150;
			}
			
			if (this.rand.nextInt(denom - (lootingModifier * 20)) == 0) {
				this.entityDropItem(NostrumRoseItem.getItem(RoseType.PALE, 1), 0);
			}
		}
	}
}
