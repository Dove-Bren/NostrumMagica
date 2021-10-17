package com.smanzana.nostrummagica.client.effects;

import java.util.List;

import com.enderio.core.client.render.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ClientEffectForm {
	
	public static void drawModel(IBakedModel model, int color) {
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderUtil.bindBlockTexture();
		
		{
			List<BakedQuad> listQuads = model.getQuads(null, null, 0);
			Tessellator tessellator = Tessellator.getInstance();
	        BufferBuilder vertexbuffer = tessellator.getBuffer();
	        int i = 0;
	
	        for (int j = listQuads.size(); i < j; ++i)
	        {
	            BakedQuad bakedquad = (BakedQuad)listQuads.get(i);
	            vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
	            
	            vertexbuffer.addVertexData(bakedquad.getVertexData());
	            vertexbuffer.putColor4(color); // Vanilla bug! This forces alpha to be 100%!
	            //RenderFuncs.putColor4(vertexbuffer, RenderFuncs.getIntBuffer(vertexbuffer), color); // flashes. Why?
	
	            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
	            vertexbuffer.putNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
	            tessellator.draw();
			}
		}
//		{
//			RenderFuncs.RenderModelWithColor(model, color);
//		}
		GlStateManager.depthMask(true);
	}

	public void draw(Minecraft mc, float partialTicks, int color);
	
}
