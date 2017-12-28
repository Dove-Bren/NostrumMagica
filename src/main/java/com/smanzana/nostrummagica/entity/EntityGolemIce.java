package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntityGolemIce extends EntityGolem {
	
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

	public EntityGolemIce(World worldIn) {
		super(worldIn, true, true, true);
	}

	@Override
	public void doMeleeTask(EntityLivingBase target) {
		this.attackEntityAsMob(target);
	}

	@Override
	public void doRangeTask(EntityLivingBase target) {
		EntityGolemIce.init();
		
		EntityLivingBase targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellRange.cast(this);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public void doBuffTask(EntityLivingBase target) {
		EntityGolemIce.init();
		
		EntityLivingBase targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellBuff.cast(this);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(EntityLivingBase target) {
		return target.getActivePotionEffect(MagicShieldPotion.instance()) == null;
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
}
