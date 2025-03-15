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
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class MagicEnderGolemEntity extends MagicGolemEntity {
	
	public static final String ID = "ender_golem";
	
	private static Spell spellRange;
	private static Spell spellDebuff;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellRange == null) {
			spellRange = Spell.CreateAISpell("Overwhelm");
			spellRange.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellRange.addPart(new SpellShapePart(NostrumSpellShapes.Burst, NostrumSpellShapes.Burst.makeProps(2)));
			spellRange.addPart(new SpellEffectPart(
					EMagicElement.ENDER,
					1,
					null));
			
			spellDebuff = Spell.CreateAISpell("Blind");
			spellDebuff.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellDebuff.addPart(new SpellEffectPart(
					EMagicElement.ENDER,
					1,
					EAlteration.INFLICT));
			
			spellBuff = Spell.CreateAISpell("Cloak");
			spellBuff.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellBuff.addPart(new SpellEffectPart(
					EMagicElement.ENDER,
					1,
					EAlteration.RESIST));
		}
	}

	public MagicEnderGolemEntity(EntityType<MagicEnderGolemEntity> type, World worldIn) {
		super(type, worldIn, EMagicElement.ENDER, false, true, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		;
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		MagicEnderGolemEntity.init();
		
		LivingEntity targ = this.getTarget();
		if (targ != target)
			this.setTarget(target);
		
		if (NostrumMagica.rand.nextBoolean()) {
			spellDebuff.cast(this, 1.0f);
		} else {
			spellRange.cast(this, 1.0f);
		}
		
		if (targ != target)
			this.setTarget(targ);
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		MagicEnderGolemEntity.init();
		
		LivingEntity targ = this.getTarget();
		if (targ != target)
			this.setTarget(target);
		
		spellBuff.cast(this, 1.0f);
		
		if (targ != target)
			this.setTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getEffect(Effects.INVISIBILITY) == null;
	}

	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return MagicGolemEntity.BuildBaseAttributes()
	        .add(Attributes.MOVEMENT_SPEED, 0.30D)
	
	        .add(Attributes.MAX_HEALTH, 30.0D)
	
	        .add(Attributes.ATTACK_DAMAGE, 2.0D)
	        .add(Attributes.ARMOR, 4.0D);
	}

	@Override
	public String getTextureKey() {
		return "ender";
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (this.getOwnerId() == null) {
//			int count = this.rand.nextInt(3) + 1;
//			count += lootingModifier;
//			
//			this.entityDropItem(EssenceItem.getEssence(
//					EMagicElement.ENDER,
//					count), 0);
//			
//			int denom = ROSE_DROP_DENOM;
//			if (wasRecentlyHit) {
//				denom = 150;
//			}
//			
//			if (this.rand.nextInt(denom - (lootingModifier * 20)) == 0) {
//				this.entityDropItem(NostrumRoseItem.getItem(RoseType.ELDRICH, 1), 0);
//			}
//		}
//	}
}
