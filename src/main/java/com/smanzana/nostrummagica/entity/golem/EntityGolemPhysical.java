package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem.RoseType;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

public class EntityGolemPhysical extends EntityGolem {
	
	public static final String ID = "physical_golem";
	
	private static Spell spellRanged;
	private static Spell spellDebuff;
	
	private static void init() {
		if (spellRanged == null) {
			spellRanged = new Spell("Massive Blow", true);
			//spellRanged.addPart(new SpellPart()); should be projectile
			spellRanged.addPart(new SpellPart(ProjectileTrigger.instance()));
			spellRanged.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					null));
			
			spellDebuff = new Spell("Corrupt Offense", true);
			spellDebuff.addPart(new SpellPart(AITargetTrigger.instance()));
			spellDebuff.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					EAlteration.INFLICT));
		}
	}

	public EntityGolemPhysical(EntityType<EntityGolemPhysical> type, World worldIn) {
		super(type, worldIn, EMagicElement.PHYSICAL, true, true, false);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		this.attackEntityAsMob(target);
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		EntityGolemPhysical.init();
		
		// Either do debuff or damage
		if (target.getActivePotionEffect(Potion.getPotionFromResourceLocation("weakness")) == null) {
			LivingEntity targ = this.getAttackTarget();
			if (targ != target)
				this.setAttackTarget(target);
			spellDebuff.cast(this, 1.0f);
			if (targ != target)
				this.setAttackTarget(targ);
		} else {
			spellRanged.cast(this, 1.0f);
		}
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		; // shouldn't happen
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return true;
	}

	@Override
	public void initGolemAttributes() {
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);

        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);

        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0D);
	}

	@Override
	public String getTextureKey() {
		return "physical";
	}
	
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		if (this.getOwnerId() == null) {
			int count = this.rand.nextInt(3) + 1;
			count += lootingModifier;
			
			this.entityDropItem(EssenceItem.getEssence(
					EMagicElement.PHYSICAL,
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
