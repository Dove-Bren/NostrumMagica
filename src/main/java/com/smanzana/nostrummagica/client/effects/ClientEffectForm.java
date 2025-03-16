package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import com.mojang.math.Matrix4f;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ClientEffectForm {
	
	public static void drawModel(PoseStack matrixStackIn, BakedModel model, int color, int packedLightIn) {
//		GlStateManager.disableBlend();
//		//GlStateManager.disableAlphaTest();
//		GlStateManager.disableTexture();
//		GlStateManager.enableBlend();
//		//GlStateManager.enableAlphaTest();
//		GlStateManager.enableTexture();
//		//GlStateManager.depthMask(false);
//		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA.param, DestFactor.ONE_MINUS_SRC_ALPHA.param);
//		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		
//		{
//			List<BakedQuad> listQuads = model.getQuads(null, null, 0);
//			Tessellator tessellator = Tessellator.getInstance();
//	        BufferBuilder vertexbuffer = tessellator.getBuffer();
//	        int i = 0;
//	
//	        for (int j = listQuads.size(); i < j; ++i)
//	        {
//	            BakedQuad bakedquad = (BakedQuad)listQuads.get(i);
//	            vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
//	            
//	            vertexbuffer.addVertexData(bakedquad.getVertexData());
//	            vertexbuffer.putColor4(color); // Vanilla bug! This forces alpha to be 100%!
//	            //RenderFuncs.putColor4(vertexbuffer, RenderFuncs.getIntBuffer(vertexbuffer), color); // flashes. Why?
//	
//	            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
//	            vertexbuffer.putNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
//	            tessellator.draw();
//			}
//		}
		{
			RenderFuncs.RenderModelWithColorNoBatch(matrixStackIn, model, color, packedLightIn, OverlayTexture.NO_OVERLAY);
		}
		//GlStateManager.depthMask(true);
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

	public void draw(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int color);
	
}
