package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCastProperties;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

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
					EAlteration.HARM));
			
			spellRanged2 = Spell.CreateAISpell("Blaze");
			spellRanged2.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
			spellRanged2.addPart(new SpellShapePart(NostrumSpellShapes.Burst, NostrumSpellShapes.Burst.makeProps(2f)));
			spellRanged2.addPart(new SpellEffectPart(
					EMagicElement.FIRE,
					1,
					null));
			
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

	public MagicFireGolemEntity(EntityType<MagicFireGolemEntity> type, Level worldIn) {
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
		LivingEntity targ = this.getTarget();
		if (targ != target)
			this.setTarget(target);
		
		boolean canBurnArmor = false;
		for (ItemStack item : target.getArmorSlots()) {
			if (item == null)
				continue;
			
			canBurnArmor = true;
			break;
		}
		
		if (canBurnArmor && NostrumMagica.rand.nextFloat() <= 0.2f) {
			spellRanged3.cast(this, SpellCastProperties.BASE);
		}else if (NostrumMagica.rand.nextFloat() < 0.3f) {
			spellRanged2.cast(this, SpellCastProperties.BASE);
		} else {
			spellRanged1.cast(this, SpellCastProperties.BASE);
		}
		
		if (targ != target)
			this.setTarget(targ);
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		MagicFireGolemEntity.init();
		
		LivingEntity targ = this.getTarget();
		if (targ != target)
			this.setTarget(target);
		
		spellBuff.cast(this, SpellCastProperties.BASE);
		
		if (targ != target)
			this.setTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getEffect(MobEffects.FIRE_RESISTANCE) == null;
	}

	public static final AttributeSupplier.Builder BuildAttributes() {
		return MagicGolemEntity.BuildBaseAttributes()
	        .add(Attributes.MOVEMENT_SPEED, 0.22D)
	
	        .add(Attributes.MAX_HEALTH, 18.0D)
	
	        .add(Attributes.ATTACK_DAMAGE, 6.0D)
	        .add(Attributes.ARMOR, 6.0D);
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
