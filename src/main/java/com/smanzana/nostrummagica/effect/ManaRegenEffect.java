package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ManaRegenEffect extends MobEffect {

	public static final String ID = "mana-regen";
	
	public ManaRegenEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFFBB6DFF);
		this.addAttributeModifier(NostrumAttributes.manaRegen,
				"74149d64-b22a-4dd9-ab68-030fc195ecfc", 50D, AttributeModifier.Operation.ADDITION);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No special effects
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		;
    }
	
	public String getEffectName() {
		return "mana-regen";
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack matrixStackIn, int x, int y, float z) {
		PotionIcon.MANAREGEN.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.MANAREGEN.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
	
}
