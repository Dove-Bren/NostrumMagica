package com.smanzana.nostrummagica.client.effects;

import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ClientEffectForm {
	
	public static void drawModel(IBakedModel model, int color) {
		GlStateManager.disableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();
		//GlStateManager.depthMask(false);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		
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
			RenderFuncs.RenderModelWithColor(model, color);
		}
		//GlStateManager.depthMask(true);
	}

	public void draw(Minecraft mc, float partialTicks, int color);
	
}
