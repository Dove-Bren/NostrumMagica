package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.legacy.AoEShape;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpell;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpellPart;
import com.smanzana.nostrummagica.spells.components.legacy.SingleShape;
import com.smanzana.nostrummagica.spells.components.legacy.SpellPartProperties;
import com.smanzana.nostrummagica.spells.components.legacy.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.legacy.triggers.ProjectileTrigger;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class EntityGolemFire extends EntityGolem {
	
	public static final String ID = "fire_golem";
	
	private static LegacySpell spellRanged1;
	private static LegacySpell spellRanged2;
	private static LegacySpell spellRanged3;
	private static LegacySpell spellBuff;
	
	private static void init() {
		if (spellRanged1 == null) {
			spellRanged1 = LegacySpell.CreateAISpell("Fireball");
			spellRanged1.addPart(new LegacySpellPart(ProjectileTrigger.instance()));
			spellRanged1.addPart(new LegacySpellPart(SingleShape.instance(),
					EMagicElement.FIRE,
					1,
					null));
			
			spellRanged2 = LegacySpell.CreateAISpell("Blaze");
			spellRanged2.addPart(new LegacySpellPart(ProjectileTrigger.instance()));
			spellRanged2.addPart(new LegacySpellPart(AoEShape.instance(),
					EMagicElement.FIRE,
					1,
					EAlteration.CONJURE,
					new SpellPartProperties(2, false)));
			
			spellRanged3 = LegacySpell.CreateAISpell("Melt Armor");
			spellRanged3.addPart(new LegacySpellPart(ProjectileTrigger.instance()));
			spellRanged3.addPart(new LegacySpellPart(SingleShape.instance(),
					EMagicElement.FIRE,
					1,
					EAlteration.GROWTH,
					new SpellPartProperties(1, false)));
			
			spellBuff = LegacySpell.CreateAISpell("Fire Shield");
			spellBuff.addPart(new LegacySpellPart(AITargetTrigger.instance()));
			spellBuff.addPart(new LegacySpellPart(SingleShape.instance(),
					EMagicElement.FIRE,
					1,
					EAlteration.SUPPORT));
		}
	}

	public EntityGolemFire(EntityType<EntityGolemFire> type, World worldIn) {
		super(type, worldIn, EMagicElement.FIRE, false, true, true);
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
		return target.getActivePotionEffect(Effects.FIRE_RESISTANCE) == null;
	}

	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return EntityGolem.BuildBaseAttributes()
	        .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.22D)
	
	        .createMutableAttribute(Attributes.MAX_HEALTH, 18.0D)
	
	        .createMutableAttribute(Attributes.ATTACK_DAMAGE, 6.0D)
	        .createMutableAttribute(Attributes.ARMOR, 6.0D);
	}

	@Override
	public String getTextureKey() {
		return "fire";
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (this.getOwnerId() == null) {
//			int count = this.rand.nextInt(3) + 1;
//			count += lootingModifier;
//			
//			this.entityDropItem(EssenceItem.getEssence(
//					EMagicElement.FIRE,
//					count), 0);
//			
//			int denom = ROSE_DROP_DENOM;
//			if (wasRecentlyHit) {
//				denom = 150;
//			}
//			
//			if (this.rand.nextInt(denom - (lootingModifier * 20)) == 0) {
//				this.entityDropItem(NostrumRoseItem.getItem(RoseType.BLOOD, 1), 0);
//			}
//		}
//	}
}
