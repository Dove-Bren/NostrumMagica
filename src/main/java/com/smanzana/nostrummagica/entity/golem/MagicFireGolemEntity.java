package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class MagicFireGolemEntity extends MagicGolemEntity {
	
	public static final String ID = "fire_golem";
	
	private static Spell spellRanged1;
	private static Spell spellRanged2;
	private static Spell spellRanged3;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellRanged1 == null) {
			spellRanged1 = Spell.CreateAISpell("Fireball");
			spellRanged1.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
			spellRanged1.addPart(new SpellEffectPart(
					EMagicElement.FIRE,
					1,
					null));
			
			spellRanged2 = Spell.CreateAISpell("Blaze");
			spellRanged2.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
			spellRanged2.addPart(new SpellShapePart(NostrumSpellShapes.Burst, NostrumSpellShapes.Burst.makeProps(2f)));
			spellRanged2.addPart(new SpellEffectPart(
					EMagicElement.FIRE,
					1,
					EAlteration.CONJURE));
			
			spellRanged3 = Spell.CreateAISpell("Melt Armor");
			spellRanged3.addPart(new SpellShapePart(NostrumSpellShapes.Projectile, NostrumSpellShapes.Projectile.makeProps(false)));
			spellRanged3.addPart(new SpellEffectPart(
					EMagicElement.FIRE,
					1,
					EAlteration.GROWTH));
			
			spellBuff = Spell.CreateAISpell("Fire Shield");
			spellBuff.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellBuff.addPart(new SpellEffectPart(
					EMagicElement.FIRE,
					1,
					EAlteration.SUPPORT));
		}
	}

	public MagicFireGolemEntity(EntityType<MagicFireGolemEntity> type, World worldIn) {
		super(type, worldIn, EMagicElement.FIRE, false, true, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		; // Shouldn't happen. Can't.
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		MagicFireGolemEntity.init();
		
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
		MagicFireGolemEntity.init();
		
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
		return MagicGolemEntity.BuildBaseAttributes()
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
