package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

public class EntityGolemWind extends EntityGolem {
	
	private static Spell spellPush;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellPush == null) {
			spellPush = new Spell("Gust", true);
			spellPush.addPart(new SpellPart(SelfTrigger.instance()));
			spellPush.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.WIND,
					1,
					EAlteration.RESIST));
			
			spellBuff = new Spell("Speed", true);
			spellBuff.addPart(new SpellPart(AITargetTrigger.instance()));
			spellBuff.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.WIND,
					1,
					EAlteration.SUPPORT));
		}
	}

	public EntityGolemWind(World worldIn) {
		super(worldIn, true, false, true);
	}

	@Override
	public void doMeleeTask(EntityLivingBase target) {
		EntityGolemWind.init();
		
		float level = .1f;
		if (this.getHealth() < 8f)
			level = .4f;
		if (NostrumMagica.rand.nextFloat() < level) {
			spellPush.cast(this);
		} else {
			this.attackEntityAsMob(target);
		}
	}

	@Override
	public void doRangeTask(EntityLivingBase target) {
		;
	}

	@Override
	public void doBuffTask(EntityLivingBase target) {
		EntityGolemWind.init();
		
		EntityLivingBase targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellBuff.cast(this);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(EntityLivingBase target) {
		return target.getActivePotionEffect(Potion.getPotionFromResourceLocation("speed")) == null;
	}

	@Override
	public void initGolemAttributes() {
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);

        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
	}

	@Override
	public String getTextureKey() {
		return "wind";
	}
}
