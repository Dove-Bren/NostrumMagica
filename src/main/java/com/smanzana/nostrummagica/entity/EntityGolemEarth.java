package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem.RoseType;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

public class EntityGolemEarth extends EntityGolem {
	
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

	public EntityGolemEarth(World worldIn) {
		super(worldIn, EMagicElement.EARTH, true, false, true);
	}

	@Override
	public void doMeleeTask(EntityLivingBase target) {
		this.attackEntityAsMob(target);
	}

	@Override
	public void doRangeTask(EntityLivingBase target) {
		; // shoudln't happen
	}

	@Override
	public void doBuffTask(EntityLivingBase target) {
		EntityGolemEarth.init();
		
		EntityLivingBase targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		boolean canStrength = target.getActivePotionEffect(Potion.getPotionFromResourceLocation("strength")) == null;
		boolean canShield = target.getActivePotionEffect(PhysicalShieldPotion.instance()) == null;
		
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
	public boolean shouldDoBuff(EntityLivingBase target) {
		return target.getActivePotionEffect(Potion.getPotionFromResourceLocation("strength")) == null
				|| target.getActivePotionEffect(PhysicalShieldPotion.instance()) == null;
	}

	@Override
	public void initGolemAttributes() {
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20D);

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24.0D);

        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(12.0D);
	}

	@Override
	public String getTextureKey() {
		return "earth";
	}
	
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		if (this.getOwnerId() == null) {
			int count = this.rand.nextInt(3) + 1;
			count += lootingModifier;
			
			this.entityDropItem(EssenceItem.getEssence(
					EMagicElement.EARTH,
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
