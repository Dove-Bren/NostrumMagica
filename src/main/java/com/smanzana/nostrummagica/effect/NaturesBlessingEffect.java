package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NaturesBlessingEffect extends Effect {

	public static final String ID = "naturesblessing";
	
	public NaturesBlessingEffect() {
		super(EffectType.BENEFICIAL, 0xFF38810D);
	}
	
	public boolean isReady(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		// 10, 5, 2.5, ...
		final int interval = Math.max(1, (int) (20.0 * (10.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 10 seconds, 5 second, 2.5 seconds, ...
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		if (!entity.world.isRemote) {
			final float amt = 1; // Doesn't depend on amp
			
			if (entity.getHealth() < entity.getMaxHealth() && entity.getRNG().nextBoolean()) {
				// Health
				entity.heal(amt);
			} else {
				// Food
				if (entity instanceof PlayerEntity) {
					PlayerEntity player = (PlayerEntity) entity;
					player.getFoodStats().addStats((int) amt, 0);
				} else if (entity instanceof AnimalEntity) {
					((AnimalEntity) entity).setInLove(null);
				}
			}
		}
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		PotionIcon.NATURESBLESSING.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.NATURESBLESSING.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
	
}
