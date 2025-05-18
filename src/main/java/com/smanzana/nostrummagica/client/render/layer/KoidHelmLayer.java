package com.smanzana.nostrummagica.client.render.layer;

import java.awt.Color;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.autodungeons.util.ColorUtil;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.PartBakedWithOffsetModel;
import com.smanzana.nostrummagica.item.armor.KoidHelmet;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.ResourceLocation;

public class KoidHelmLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	
	private static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "entity/koid");
	
	protected PartBakedWithOffsetModel model;

	protected static final VertexConsumer GetBuffer(MultiBufferSource typeBuffer, @Nullable RenderType type) {
		if (type == null) {
			type = Sheets.translucentCullBlockSheet();
		}
		
		return typeBuffer.getBuffer(type);
	}
	
	public KoidHelmLayer(PlayerRenderer renderPlayerIn) {
		super(renderPlayerIn);
		this.model = new PartBakedWithOffsetModel(MODEL);
	}
	
	@Override
	public void render(PoseStack stack, MultiBufferSource typeBuffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
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
		if (player.hasItemInSlot(EquipmentSlot.HEAD) && player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof KoidHelmet) {
			return true;
		}
		
		return false;
	}
}
