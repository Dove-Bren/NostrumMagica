package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.potions.MagicResistPotion;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.World;

public class EntityGolemLightning extends EntityGolem {
	
	private static final AttributeModifier MOVEMENT_STORM_MODIFIER
		= new AttributeModifier("lightning_storm_boost", .2, 1);
	
	private static Spell spellRanged1;
	private static Spell spellRanged2;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellRanged1 == null) {
			spellRanged1 = new Spell("Lightning Strike", true);
			spellRanged1.addPart(new SpellPart(AITargetTrigger.instance()));
			spellRanged1.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.CONJURE));
			
			spellRanged2 = new Spell("Spark", true);
			spellRanged2.addPart(new SpellPart(ProjectileTrigger.instance()));
			spellRanged2.addPart(new SpellPart(AoEShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					null,
					new SpellPartParam(1, false)));
			
			spellBuff = new Spell("Magic Ward", true);
			spellBuff.addPart(new SpellPart(AITargetTrigger.instance()));
			spellBuff.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.RESIST));
		}
	}

	public EntityGolemLightning(World worldIn) {
		super(worldIn, false, true, true);
	}

	@Override
	public void doMeleeTask(EntityLivingBase target) {
		; // Shouldn't happen. Can't.
	}

	@Override
	public void doRangeTask(EntityLivingBase target) {
		EntityGolemLightning.init();
		
		// Pick a spell to do
		EntityLivingBase targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		if (NostrumMagica.rand.nextFloat() <= 0.3f) {
			spellRanged1.cast(this);
		} else {
			spellRanged2.cast(this);
		}
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public void doBuffTask(EntityLivingBase target) {
		EntityGolemLightning.init();
		
		EntityLivingBase targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellBuff.cast(this);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(EntityLivingBase target) {
		return target.getActivePotionEffect(MagicResistPotion.instance()) == null;
	}

	@Override
	public void initGolemAttributes() {
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14.0D);

        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
	}

	@Override
	public String getTextureKey() {
		return "lightning";
	}
	
	private boolean activeAttribMod = false;
	@Override
	public void onUpdate() {
		if (worldObj.isRainingAt(this.getPosition())) {
			if (!activeAttribMod) {
				this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
					.applyModifier(MOVEMENT_STORM_MODIFIER);
				activeAttribMod = true;
			}
		} else {
			if (activeAttribMod) {
				this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
					.removeModifier(MOVEMENT_STORM_MODIFIER);
				activeAttribMod = false;
			}
		}
		
		super.onUpdate();
	}

}
