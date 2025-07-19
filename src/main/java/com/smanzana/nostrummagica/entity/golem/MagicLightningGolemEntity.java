package com.smanzana.nostrummagica.entity.golem;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCastProperties;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class MagicLightningGolemEntity extends MagicGolemEntity {
	
	public static final String ID = "lightning_golem";
	
	private static final AttributeModifier MOVEMENT_STORM_MODIFIER
		= new AttributeModifier("lightning_storm_boost", .2, AttributeModifier.Operation.MULTIPLY_BASE);
	
	private static Spell spellRanged1;
	private static Spell spellRanged2;
	private static Spell spellBuff;
	
	private static void init() {
		if (spellRanged1 == null) {
			spellRanged1 = Spell.CreateAISpell("Lightning Strike");
			spellRanged1.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellRanged1.addPart(new SpellEffectPart(
					EMagicElement.LIGHTNING,
					1,
					null));
			
			spellRanged2 = Spell.CreateAISpell("Spark");
			spellRanged2.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
			spellRanged2.addPart(new SpellShapePart(NostrumSpellShapes.Burst, NostrumSpellShapes.Burst.makeProps(1f)));
			spellRanged2.addPart(new SpellEffectPart(
					EMagicElement.LIGHTNING,
					1,
					EAlteration.HARM));
			
			spellBuff = Spell.CreateAISpell("Magic Ward");
			spellBuff.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			spellBuff.addPart(new SpellEffectPart(
					EMagicElement.LIGHTNING,
					1,
					EAlteration.RESIST));
		}
	}

	public MagicLightningGolemEntity(EntityType<MagicLightningGolemEntity> type, Level worldIn) {
		super(type, worldIn, EMagicElement.LIGHTNING, false, true, true);
	}

	@Override
	public void doMeleeTask(LivingEntity target) {
		; // Shouldn't happen. Can't.
	}

	@Override
	public void doRangeTask(LivingEntity target) {
		MagicLightningGolemEntity.init();
		
		// Pick a spell to do
		LivingEntity targ = this.getTarget();
		if (targ != target)
			this.setTarget(target);
		
		if (NostrumMagica.rand.nextFloat() <= 0.3f) {
			spellRanged1.cast(this, SpellCastProperties.BASE);
		} else {
			spellRanged2.cast(this, SpellCastProperties.BASE);
		}
		
		if (targ != target)
			this.setTarget(targ);
	}

	@Override
	public void doBuffTask(LivingEntity target) {
		MagicLightningGolemEntity.init();
		
		LivingEntity targ = this.getTarget();
		if (targ != target)
			this.setTarget(target);
		
		spellBuff.cast(this, SpellCastProperties.BASE);
		
		if (targ != target)
			this.setTarget(targ);
	}

	@Override
	public boolean shouldDoBuff(LivingEntity target) {
		return target.getEffect(NostrumEffects.magicResist) == null;
	}

	public static final AttributeSupplier.Builder BuildAttributes() {
		return MagicGolemEntity.BuildBaseAttributes(EMagicElement.LIGHTNING)
	        .add(Attributes.MOVEMENT_SPEED, 0.25D)
	
	        .add(Attributes.MAX_HEALTH, 14.0D)
	
	        .add(Attributes.ATTACK_DAMAGE, 2.0D)
	        .add(Attributes.ARMOR, 6.0D);
	}

	@Override
	public String getTextureKey() {
		return "lightning";
	}
	
	@Override
	public void tick() {
		if (level.isRainingAt(this.blockPosition())) {
			if (!this.getAttribute(Attributes.MOVEMENT_SPEED)
					.hasModifier(MOVEMENT_STORM_MODIFIER)) {
				this.getAttribute(Attributes.MOVEMENT_SPEED)
					.addPermanentModifier(MOVEMENT_STORM_MODIFIER);
			}
		} else {
			if (this.getAttribute(Attributes.MOVEMENT_SPEED)
					.hasModifier(MOVEMENT_STORM_MODIFIER)) {
				this.getAttribute(Attributes.MOVEMENT_SPEED)
					.removeModifier(MOVEMENT_STORM_MODIFIER);
			}
		}
		
		super.tick();
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (this.getOwnerId() == null) {
//			int count = this.rand.nextInt(3) + 1;
//			count += lootingModifier;
//			
//			this.entityDropItem(EssenceItem.getEssence(
//					EMagicElement.LIGHTNING,
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
