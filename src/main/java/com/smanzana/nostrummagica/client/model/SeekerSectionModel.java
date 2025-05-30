package com.smanzana.nostrummagica.client.model;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SeekerSectionModel extends Model {

	public SeekerSectionModel(Function<ResourceLocation, RenderType> renderTypeIn) {
		super(renderTypeIn);
		
	}
	
	public SeekerSectionModel() {
		this(RenderType::entityTranslucent);
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		
		//RenderFuncs.renderSpaceQuad(matrixStackIn, bufferIn, .5f, 0, 1, 0, 1, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		//RenderFuncs.drawUnitCube(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		
		matrixStackIn.pushPose();
		matrixStackIn.scale(.5f, .5f, .5f);
		
		// renderSpaceQuad, except xz instead of xy. When rotating, had trouble when applying other x rotations too
		{
			final Matrix4f transform = matrixStackIn.last().pose();
			final Matrix3f normal = matrixStackIn.last().normal();
			
			bufferIn.vertex(transform, .5f, 0, .5f).color(red, green, blue, alpha).uv(1, 0).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1, 0).endVertex();
			bufferIn.vertex(transform, -.5f, 0, .5f).color(red, green, blue, alpha).uv(0, 0).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1, 0).endVertex();
			bufferIn.vertex(transform, -.5f, 0, -.5f).color(red, green, blue, alpha).uv(0, 1).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1, 0).endVertex();
			bufferIn.vertex(transform, .5f, 0, -.5f).color(red, green, blue, alpha).uv(1, 1).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1, 0).endVertex();
		}
		matrixStackIn.popPose();
	}
	
}
