package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

public class EntityGolemEnder extends EntityGolem {
	
	private static Spell spellRange;
	private static Spell spellDebuff;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellRange == null) {
			spellRange = new Spell("Overwhelm", true);
			spellRange.addPart(new SpellPart(AITargetTrigger.instance()));
			spellRange.addPart(new SpellPart(AoEShape.instance(),
					EMagicElement.ENDER,
					1,
					null,
					new SpellPartParam(2, false)));
			
			spellDebuff = new Spell("Blind", true);
			spellDebuff.addPart(new SpellPart(AITargetTrigger.instance()));
			spellDebuff.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.ENDER,
					1,
					EAlteration.INFLICT));
			
			spellBuff = new Spell("Cloak", true);
			spellBuff.addPart(new SpellPart(AITargetTrigger.instance()));
			spellBuff.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.ENDER,
					1,
					EAlteration.RESIST));
		}
	}

	public EntityGolemEnder(World worldIn) {
		super(worldIn, false, true, true);
	}

	@Override
	public void doMeleeTask(EntityLivingBase target) {
		;
	}

	@Override
	public void doRangeTask(EntityLivingBase target) {
		EntityGolemEnder.init();
		
		EntityLivingBase targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		if (NostrumMagica.rand.nextBoolean()) {
			spellDebuff.cast(this, 1.0f);
		} else {
			spellRange.cast(this, 1.0f);
		}
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public void doBuffTask(EntityLivingBase target) {
		EntityGolemEnder.init();
		
		EntityLivingBase targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellBuff.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(EntityLivingBase target) {
		return target.getActivePotionEffect(Potion.getPotionFromResourceLocation("invisibility")) == null;
	}

	@Override
	public void initGolemAttributes() {
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30D);

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);

        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
	}

	@Override
	public String getTextureKey() {
		return "ender";
	}
}
