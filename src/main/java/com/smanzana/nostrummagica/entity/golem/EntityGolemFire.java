package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem.RoseType;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

public class EntityGolemFire extends EntityGolem {
	
	private static Spell spellRanged1;
	private static Spell spellRanged2;
	private static Spell spellRanged3;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellRanged1 == null) {
			spellRanged1 = new Spell("Fireball", true);
			spellRanged1.addPart(new SpellPart(ProjectileTrigger.instance()));
			spellRanged1.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.FIRE,
					1,
					null));
			
			spellRanged2 = new Spell("Blaze", true);
			spellRanged2.addPart(new SpellPart(ProjectileTrigger.instance()));
			spellRanged2.addPart(new SpellPart(AoEShape.instance(),
					EMagicElement.FIRE,
					1,
					EAlteration.CONJURE,
					new SpellPartParam(2, false)));
			
			spellRanged3 = new Spell("Melt Armor", true);
			spellRanged3.addPart(new SpellPart(ProjectileTrigger.instance()));
			spellRanged3.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.FIRE,
					1,
					EAlteration.GROWTH,
					new SpellPartParam(1, false)));
			
			spellBuff = new Spell("Fire Shield", true);
			spellBuff.addPart(new SpellPart(AITargetTrigger.instance()));
			spellBuff.addPart(new SpellPart(SingleShape.instance(),
					EMagicElement.FIRE,
					1,
					EAlteration.SUPPORT));
		}
	}

	public EntityGolemFire(World worldIn) {
		super(worldIn, EMagicElement.FIRE, false, true, true);
        this.isImmuneToFire = true;
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		; // Shouldn't happen. Can't.
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		EntityGolemFire.init();
		
		// Pick a spell to do
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		boolean canBurnArmor = false;
		for (ItemStack item : target.getArmorInventoryList()) {
			if (item == null)
				continue;
			
			canBurnArmor = true;
			break;
		}
		
		if (canBurnArmor && NostrumMagica.rand.nextFloat() <= 0.2f) {
			spellRanged3.cast(this, 1.0f);
		}else if (NostrumMagica.rand.nextFloat() < 0.3f) {
			spellRanged2.cast(this, 1.0f);
		} else {
			spellRanged1.cast(this, 1.0f);
		}
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		EntityGolemFire.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellBuff.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getActivePotionEffect(Potion.getPotionFromResourceLocation("fire_resistance")) == null;
	}

	@Override
	public void initGolemAttributes() {
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.22D);

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(18.0D);

        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
	}

	@Override
	public String getTextureKey() {
		return "fire";
	}
	
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		if (this.getOwnerId() == null) {
			int count = this.rand.nextInt(3) + 1;
			count += lootingModifier;
			
			this.entityDropItem(EssenceItem.getEssence(
					EMagicElement.FIRE,
					count), 0);
			
			int denom = ROSE_DROP_DENOM;
			if (wasRecentlyHit) {
				denom = 150;
			}
			
			if (this.rand.nextInt(denom - (lootingModifier * 20)) == 0) {
				this.entityDropItem(NostrumRoseItem.getItem(RoseType.BLOOD, 1), 0);
			}
		}
	}
}
