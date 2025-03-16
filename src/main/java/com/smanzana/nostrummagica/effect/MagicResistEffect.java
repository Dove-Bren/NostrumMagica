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

public class MagicResistEffect extends MobEffect {

	public static final String ID = "magres";
	
	public MagicResistEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFFA5359A);
		this.addAttributeModifier(NostrumAttributes.magicResist, "662c96d6-19d7-4fe8-a6ff-b46befaa16a2", 20.D, AttributeModifier.Operation.ADDITION);
	}
	
	@Override
	public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
		// Effect used to be a (... * .75 ^ (amp+1)) on damage.
		return super.getAttributeModifierValue(amplifier, modifier);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return duration > 0; // Every tick
	}
	
	// This is an attribute now!
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack matrixStackIn, int x, int y, float z) {
		PotionIcon.MAGICRESIST.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.MAGICRESIST.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
}
