package com.smanzana.nostrummagica.client.render.layer;

import java.awt.Color;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.autodungeons.util.ColorUtil;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelRendererBakedWithOffset;
import com.smanzana.nostrummagica.item.armor.KoidHelmet;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

public class LayerKoidHelm extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
	
	private static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "entity/koid");
	
	protected ModelRendererBakedWithOffset model;

	protected static final IVertexBuilder GetBuffer(IRenderTypeBuffer typeBuffer, @Nullable RenderType type) {
		if (type == null) {
			type = Atlases.translucentCullBlockSheet();
		}
		
		return typeBuffer.getBuffer(type);
	}
	
	public LayerKoidHelm(PlayerRenderer renderPlayerIn) {
		super(renderPlayerIn);
		this.model = new ModelRendererBakedWithOffset(MODEL);
	}
	
	@Override
	public void render(MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (ShouldRender(player)) {
			final float period = 20 * 60;
			final float time = player.tickCount + partialTicks;
			final float prog = ((time % period) / period);
			final float color[] = ColorUtil.ARGBToColor(Color.HSBtoRGB(prog, .8f, 1f));
			
			model.copyFrom(this.getParentModel().hat);
			model.y -= .5f;
			model.setOffsetY(-.25f);
			
			stack.pushPose();
			//stack.rotate(Vector3f.ZP.rotationDegrees(180f));
			//stack.scale(1.25f, 1.25f, 1.25f);
			model.render(stack, GetBuffer(typeBuffer, null), packedLight, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], 1f);
			stack.popPose();
		}
	}
	
	public static boolean ShouldRender(LivingEntity player) {
		if (player.hasItemInSlot(EquipmentSlotType.HEAD) && player.getItemBySlot(EquipmentSlotType.HEAD).getItem() instanceof KoidHelmet) {
			return true;
		}
		
		return false;
	}
}
