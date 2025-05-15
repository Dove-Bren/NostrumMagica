package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ClientEffectForm {
	
	public static void drawModel(PoseStack matrixStackIn, BakedModel model, int color, int packedLightIn) {
		RenderFuncs.RenderModelWithColorNoBatch(matrixStackIn, model, color, packedLightIn, OverlayTexture.NO_OVERLAY);
	}
	
	public static int InferLightmap(PoseStack matrixStackIn, Minecraft mc) {
		if (mc.level != null) {
			final Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
			// Get position from final transform on matrix stack
			final Matrix4f transform = matrixStackIn.last().pose();
			final Vector4f origin = new Vector4f(1, 1, 1, 1); // I think this is right...
			origin.transform(transform);
			final BlockPos pos = new BlockPos(camera.x() + origin.x(), camera.y() + origin.y(), camera.z() + origin.z());
			return LevelRenderer.getLightColor(mc.level, pos);
		} else {
			return 0; // Same default as particle
		}
	}

	public void draw(PoseStack matrixStackIn, Minecraft mc, MultiBufferSource buffersIn, float partialTicks, int color, float progress);
	
}
