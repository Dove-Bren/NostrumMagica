package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MagicBoostEffect extends MobEffect {

	public static final String ID = "magboost";
	private static final String POTENCY_UUID = "718e46ce-f549-4f18-8dcb-d690590e9ba5";
	
	public MagicBoostEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF47FFAF);
		
		this.addAttributeModifier(NostrumAttributes.magicDamage, POTENCY_UUID, 50.D, AttributeModifier.Operation.ADDITION);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick effects
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack matrixStackIn, int x, int y, float z) {
		PotionIcon.MAGICBOOST.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.MAGICBOOST.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
}
