package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PhysicalShieldEffect extends MobEffect {

	public static final String ID = "shieldp";
	
	public PhysicalShieldEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF80805D);
	}
	
	@Override
	public boolean isDurationEffectTick(int duration, int amp) {
		return duration % 10 == 0; // Every tick
	}
	
	@Override
	public void applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entityLivingBaseIn);
		if (attr != null && attr.hasSkill(NostrumSkills.Earth_Master)) {
			entityLivingBaseIn.heal(.25f);
		}
	}
	
	@Override
	public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
		// Sneaky! We've just been applied
		//NostrumMagica.specialEffectProxy
		int armor = 4 * (amplifier + 1);
		NostrumMagica.magicEffectProxy.applyPhysicalShield(entity, (double) armor);
		
		NostrumMagicaSounds.SHIELD_APPLY.play(entity);
		
		super.addAttributeModifiers(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier) {
		NostrumMagica.magicEffectProxy.remove(SpecialEffect.SHIELD_PHYSICAL, entityLivingBaseIn);
		super.removeAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack matrixStackIn, int x, int y, float z) {
		PotionIcon.PHYSICALSHIELD.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.PHYSICALSHIELD.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
}
