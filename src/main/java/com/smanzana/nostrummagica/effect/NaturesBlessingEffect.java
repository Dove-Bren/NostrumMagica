package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NaturesBlessingEffect extends MobEffect {

	public static final String ID = "naturesblessing";
	
	public NaturesBlessingEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF38810D);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		// 10, 5, 2.5, ...
		final int interval = Math.max(1, (int) (20.0 * (10.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 10 seconds, 5 second, 2.5 seconds, ...
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		if (!entity.level.isClientSide) {
			final float amt = 1; // Doesn't depend on amp
			
			if (entity.getHealth() < entity.getMaxHealth() && entity.getRandom().nextBoolean()) {
				// Health
				entity.heal(amt);
			} else {
				// Food
				if (entity instanceof Player) {
					Player player = (Player) entity;
					player.getFoodData().eat((int) amt, 0);
				} else if (entity instanceof Animal) {
					((Animal) entity).setInLove(null);
				}
			}
		}
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack matrixStackIn, int x, int y, float z) {
		PotionIcon.NATURESBLESSING.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.NATURESBLESSING.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
	
}
