package com.smanzana.nostrummagica.effect;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.EffectRenderer;

public class MagicBuffEffect extends MobEffect {

	public static final String ID = "magicbuff";
	
	public MagicBuffEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF80805D);
	}
	
	@Override
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick actions
	}
	
	@Override
	public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
		super.addAttributeModifiers(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier) {
		NostrumMagica.magicEffectProxy.remove(SpecialEffect.MAGIC_BUFF, entityLivingBaseIn);
		super.removeAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@Override
	public void initializeClient(Consumer<EffectRenderer> consumer) {
		super.initializeClient(consumer);
		consumer.accept(new EffectRenderer() {
			@OnlyIn(Dist.CLIENT)
			@Override
		    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack matrixStackIn, int x, int y, float z) {
				final Minecraft mc = gui.getMinecraft();
				PotionIcon.ENCHANT.draw(matrixStackIn, mc, x + 6, y + 7);
				
				EffectData data = NostrumMagica.magicEffectProxy.getData(mc.player, SpecialEffect.MAGIC_BUFF);
				int count = data == null ? 0 : data.getCount();
				if (count > 0) {
					String display = "" + count;
					int width = mc.font.width(display);
					mc.font.draw(matrixStackIn, "" + count, x + 6 + (20 - width), y + 7 + (20 - mc.font.lineHeight), 0xFFFFFFFF);
				}
				
			}
			
			@OnlyIn(Dist.CLIENT)
			@Override
		    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack matrixStackIn, int x, int y, float z, float alpha) {
				Minecraft mc = Minecraft.getInstance();
				PotionIcon.ENCHANT.draw(matrixStackIn, mc, x + 3, y + 3);
				
				EffectData data = NostrumMagica.magicEffectProxy.getData(mc.player, SpecialEffect.MAGIC_BUFF);
				int count = data == null ? 0 : data.getCount();
				if (count > 0) {
					String display = "" + count;
					int width = mc.font.width(display);
					mc.font.draw(matrixStackIn, "" + count, x + 6 + (16 - width), y + 7 + (16 - mc.font.lineHeight), 0xFFFFFFFF);
				}
				
			}
		});
	}
}
