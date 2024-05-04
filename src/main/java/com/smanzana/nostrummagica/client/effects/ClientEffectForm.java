package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ClientEffectForm {
	
	public static void drawModel(MatrixStack matrixStackIn, IBakedModel model, int color, int packedLightIn) {
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
			RenderFuncs.RenderModelWithColorNoBatch(matrixStackIn, model, color, packedLightIn);
		}
		//GlStateManager.depthMask(true);
	}
	
	public static int InferLightmap(MatrixStack matrixStackIn, Minecraft mc) {
		if (mc.world != null) {
			final Vector3d camera = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
			// Get position from final transform on matrix stack
			final Matrix4f transform = matrixStackIn.getLast().getMatrix();
			final Vector4f origin = new Vector4f(1, 1, 1, 1); // I think this is right...
			origin.transform(transform);
			final BlockPos pos = new BlockPos(camera.getX() + origin.getX(), camera.getY() + origin.getY(), camera.getZ() + origin.getZ());
			return WorldRenderer.getCombinedLight(mc.world, pos);
		} else {
			return 0; // Same default as particle
		}
	}

	public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int color);
	
}
