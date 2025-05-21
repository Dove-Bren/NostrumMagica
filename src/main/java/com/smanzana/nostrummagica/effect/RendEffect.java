package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class RendEffect extends MobEffect {

	public static final String ID = "rend";
	private static final String MOD_UUID = "251a6920-7345-4ec6-a11e-972c4035adc1";
	
	public RendEffect() {
		super(MobEffectCategory.HARMFUL, 0xFFC7B5BE);
		this.addAttributeModifier(Attributes.ARMOR, MOD_UUID, -2D, AttributeModifier.Operation.ADDITION);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onEntityAttack(LivingAttackEvent event) {
		if (event.getAmount() > 0f && !event.isCanceled()) {
			// Is this an attack from an entity?
			if (event.getSource().getEntity() != null
					&& event.getSource().getEntity() instanceof LivingEntity) {
				LivingEntity source = (LivingEntity) event.getSource().getEntity();
				LivingEntity target = event.getEntityLiving();
				MobEffectInstance effect = target.getEffect(NostrumEffects.rend);
				if (effect != null && effect.getDuration() > 0) {
					// Check for explosive rend
					INostrumMagic attr = NostrumMagica.getMagicWrapper(source);
					if (attr != null && attr.hasSkill(NostrumSkills.Physical_Corrupt) && NostrumMagica.rand.nextInt(4) == 0) {
						
						// Spread
						for (Entity ent : target.getCommandSenderWorld().getEntities(target, target.getBoundingBox().inflate(10), (ent) -> ent instanceof LivingEntity && (NostrumMagica.IsSameTeam((LivingEntity) ent, target) || (ent instanceof Monster && target instanceof Monster)))) {
							((LivingEntity) ent).addEffect(new MobEffectInstance(NostrumEffects.rend, effect.getDuration(), effect.getAmplifier()));
							
							NostrumParticles.FILLED_ORB.spawn(target.level, new SpawnParams(
									10, target.getX(), target.getY() + target.getBbHeight()/2, target.getZ(), 0,
									40, 10,
									ent.getId()
									).color(0xFFC7B5BE).setTargetBehavior(TargetBehavior.ORBIT_LAZY).dieWithTarget(true));
						}
						
						NostrumParticles.FILLED_ORB.spawn(target.level, new SpawnParams(
								50, target.getX(), target.getY() + target.getBbHeight()/2, target.getZ(), 0,
								30, 10,
								new Vec3(0, .1, 0), new Vec3(.2, .05, .2)
								).color(0xFFC7B5BE).gravity(true));
						NostrumMagicaSounds.MELT_METAL.play(event.getEntityLiving());
					}
					
					// And fall through and let event happen
				}
			}
		}
	}
}
