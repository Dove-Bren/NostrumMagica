package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem.RoseType;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

public class EntityGolemWind extends EntityGolem {
	
	public static final String ID = "wind_golem";
	
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

	public EntityGolemWind(EntityType<EntityGolemWind> type, World worldIn) {
		super(type, worldIn, EMagicElement.WIND, true, false, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		EntityGolemWind.init();
		
		float level = .1f;
		if (this.getHealth() < 8f)
			level = .4f;
		if (NostrumMagica.rand.nextFloat() < level) {
			spellPush.cast(this, 1.0f);
		} else {
			this.attackEntityAsMob(target);
		}
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		;
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		EntityGolemWind.init();
		
		LivingEntity targ = this.getAttackTarget();
		if (targ != target)
			this.setAttackTarget(target);
		
		spellBuff.cast(this, 1.0f);
		
		if (targ != target)
			this.setAttackTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getActivePotionEffect(Potion.getPotionFromResourceLocation("speed")) == null;
	}

	@Override
	public void initGolemAttributes() {
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);

        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
	}

	@Override
	public String getTextureKey() {
		return "wind";
	}
	
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		if (this.getOwnerId() == null) {
			int count = this.rand.nextInt(3) + 1;
			count += lootingModifier;
			
			this.entityDropItem(EssenceItem.getEssence(
					EMagicElement.WIND,
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
