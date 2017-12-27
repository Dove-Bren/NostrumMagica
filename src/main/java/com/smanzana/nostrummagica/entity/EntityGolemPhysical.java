package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntityGolemPhysical extends EntityGolem {
	
	private static Spell spellRanged;
	private static Spell spellDebuff;
	
	private static void init() {
		if (spellRanged == null) {
			spellRanged = new Spell("Massive Blow");
			//spellRanged.addPart(new SpellPart());
			
			spellDebuff = new Spell("Corrupt Offense");
		}
	}

	protected EntityGolemPhysical(World worldIn) {
		super(worldIn, true, true, true);
	}

	@Override
	public void doMeleeTask(EntityLivingBase target) {
		target.attackEntityAsMob(this);
	}

	@Override
	public void doRangeTask(EntityLivingBase target) {
		EntityGolemPhysical.init();
		EntityLivingBase targ = this.getAttackTarget();
		this.setAttackTarget(target);
		spellRanged.cast(this);
		this.setAttackTarget(targ);
	}

	@Override
	public void doBuffTask(EntityLivingBase target) {
		EntityGolemPhysical.init();
		EntityLivingBase targ = this.getAttackTarget();
		this.setAttackTarget(target);
		spellDebuff.cast(this);
		this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(EntityLivingBase target) {
		return true;
	}

	@Override
	public void initGolemAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);

        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}

}
