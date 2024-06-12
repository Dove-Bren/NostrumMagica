package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class RendEffect extends Effect {

	public static final String ID = "rend";
	private static final String MOD_UUID = "251a6920-7345-4ec6-a11e-972c4035adc1";
	
	public RendEffect() {
		super(EffectType.HARMFUL, 0xFFC7B5BE);
		this.addAttributesModifier(Attributes.ARMOR, MOD_UUID, -2D, AttributeModifier.Operation.ADDITION);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onEntityAttack(LivingAttackEvent event) {
		if (event.getAmount() > 0f && !event.isCanceled()) {
			// Is this an attack from an entity?
			if (event.getSource().getTrueSource() != null
					&& event.getSource().getTrueSource() instanceof LivingEntity) {
				LivingEntity source = (LivingEntity) event.getSource().getTrueSource();
				LivingEntity target = event.getEntityLiving();
				EffectInstance effect = target.getActivePotionEffect(NostrumEffects.rend);
				if (effect != null && effect.getDuration() > 0) {
					// Check for explosive rend
					INostrumMagic attr = NostrumMagica.getMagicWrapper(source);
					if (attr != null && attr.hasSkill(NostrumSkills.Physical_Corrupt) && NostrumMagica.rand.nextInt(4) == 0) {
						
						// Spread
						for (Entity ent : target.getEntityWorld().getEntitiesInAABBexcluding(target, target.getEntity().getBoundingBox().grow(10), (ent) -> ent instanceof LivingEntity && (NostrumMagica.IsSameTeam((LivingEntity) ent, target) || (ent instanceof MonsterEntity && target instanceof MonsterEntity)))) {
							((LivingEntity) ent).addPotionEffect(new EffectInstance(NostrumEffects.rend, effect.getDuration(), effect.getAmplifier()));
							
							NostrumParticles.FILLED_ORB.spawn(target.world, new SpawnParams(
									10, target.getPosX(), target.getPosY() + target.getHeight()/2, target.getPosZ(), 0,
									40, 10,
									ent.getEntityId()
									).color(0xFFC7B5BE).dieOnTarget(true));
						}
						
						NostrumParticles.FILLED_ORB.spawn(target.world, new SpawnParams(
								50, target.getPosX(), target.getPosY() + target.getHeight()/2, target.getPosZ(), 0,
								30, 10,
								new Vector3d(0, .1, 0), new Vector3d(.2, .05, .2)
								).color(0xFFC7B5BE).gravity(true));
						NostrumMagicaSounds.MELT_METAL.play(event.getEntityLiving());
					}
					
					// And fall through and let event happen
				}
			}
		}
	}
}
