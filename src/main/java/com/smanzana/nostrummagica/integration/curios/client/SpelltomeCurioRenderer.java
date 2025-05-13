package com.smanzana.nostrummagica.integration.curios.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.render.entity.SpelltomeRenderer;
import com.smanzana.nostrummagica.item.SpellTome;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class SpelltomeCurioRenderer implements ICurioRenderer {
	
	private SpelltomeRenderer tomeRenderer;
	
	public SpelltomeCurioRenderer() {
		this.tomeRenderer = new SpelltomeRenderer();
	}

	@Override
	public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext,
			PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer,
			int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch) {
		
		if (stack.isEmpty() || !(stack.getItem() instanceof SpellTome tomeItem)) {
			return;
		}
		
		tomeRenderer.render(matrixStack, slotContext.entity(), renderTypeBuffer, partialTicks, ageInTicks, light, tomeItem.getTomeStyle());
	}

}
