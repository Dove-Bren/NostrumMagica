package com.smanzana.nostrummagica.entity.renderer;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Material;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This class taken from 
 * https://github.com/2piradians/Minewatch/blob/1.12.1/src/main/java/twopiradians/minewatch/client/render/entity/RenderOBJModel.java
 * @author Skyler
 *
 * @param <T>
 */
public class RenderObj extends ModelRenderer {

	private ResourceLocation resource;
	
	// The GL display list rendered by the Tessellator for this model
	// ModelRenderer does not expose, so we have to basically dupe
	// One display list per model. Matches resources[]
    private int compiledList;
    
    private boolean initted;

	protected RenderObj(ModelBase base, ResourceLocation loc) {
		super(base);
		
		this.resource = loc;
	}
	
	private void init() {
		if (!initted) {
			
			compiledList = GLAllocation.generateDisplayLists(1);
			GlStateManager.glNewList(compiledList, GL11.GL_COMPILE);
			
			VertexBuffer vertexbuffer = Tessellator.getInstance().getBuffer();
			
			IModel model = ModelLoaderRegistry.getModelOrLogError(this.resource, "Nostrum Magica is missing a model. Please report this to the mod authors.");
			model = this.retexture(model);
			IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
			if (bakedModel instanceof OBJBakedModel && model instanceof OBJModel) {
				int color = this.getColor();
				renderModel(bakedModel, vertexbuffer, color);
			}

	        GlStateManager.glEndList();
	        initted = true;
		}
	}

	protected boolean preRender(VertexBuffer buffer) {
		return true;
	}
	
	protected IModel retexture(IModel model) {
		return model;
	}
	
	protected int getColor() {
		return -1;
	}

	@SideOnly(Side.CLIENT)
    private void renderModel(IBakedModel model, VertexBuffer buffer, int color) {
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		GlStateManager.pushMatrix();
		GlStateManager.rotate(180, 1, 0, 0);
		
		for(EnumFacing side : EnumFacing.values()) {
			List<BakedQuad> quads = model.getQuads(null, side, 0);
			if(!quads.isEmpty()) 
				for(BakedQuad quad : quads)
					LightUtil.renderQuadColor(buffer, quad, color);
		}
		List<BakedQuad> quads = model.getQuads(null, null, 0);
		if(!quads.isEmpty()) {
			for(BakedQuad quad : quads) 
				LightUtil.renderQuadColor(buffer, quad, color);
		}
			
		GlStateManager.popMatrix();
		Tessellator.getInstance().draw();
			
			
			//// COPY
//			GlStateManager.shadeModel(GL11.GL_SMOOTH);
//			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//			Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
//			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//			GlStateManager.enableRescaleNormal();
//			GlStateManager.alphaFunc(516, 0.1F);
//			GlStateManager.enableBlend();
//			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//			GlStateManager.pushMatrix();
//
//			Tessellator tessellator = Tessellator.getInstance();
//			VertexBuffer buffer = tessellator.getBuffer();
//			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
//
//			GlStateManager.rotate(180, 0, 0, 1);
//			GlStateManager.translate((float)-x, (float)-y, (float)z);
//			GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, 1.0F, 0.0F);
//			if (this.preRender(entity, i, buffer, x, y, z, entityYaw, partialTicks)) {
//				int color = this.getColor(i, entity);
//				GlStateManager.rotate(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 1.0F, 0.0F, 0.0F);
//
//				for(EnumFacing side : EnumFacing.values()) {
//					List<BakedQuad> quads = this.bakedModels[i].getQuads(null, side, 0);
//					if(!quads.isEmpty()) 
//						for(BakedQuad quad : quads)
//							LightUtil.renderQuadColor(buffer, quad, color == -1 ? color : color | -16777216);
//				}
//				List<BakedQuad> quads = this.bakedModels[i].getQuads(null, null, 0);
//				if(!quads.isEmpty()) {
//					for(BakedQuad quad : quads) 
//						LightUtil.renderQuadColor(buffer, quad, color == -1 ? color : color | -16777216);
//				}
//			}
//			buffer.setTranslation(0, 0, 0);
//			tessellator.draw();	
//
//			GlStateManager.cullFace(GlStateManager.CullFace.BACK);
//			GlStateManager.popMatrix();
//			GlStateManager.disableRescaleNormal();
//			GlStateManager.disableBlend();
//			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//			Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
	}
	
	public static ImmutableMap<String, TextureAtlasSprite> getTextures(OBJModel model) {
		ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
		builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
		TextureAtlasSprite missing = ModelLoader.defaultTextureGetter().apply(new ResourceLocation("missingno"));
		for (String materialName : model.getMatLib().getMaterialNames()) {
			Material material = model.getMatLib().getMaterial(materialName);
			if (material.getTexture().getTextureLocation().getResourcePath().startsWith("#")) {
				FMLLog.bigWarning("OBJLoaderMW: Unresolved texture '%s' for obj model '%s'", material.getTexture().getTextureLocation().getResourcePath(), model.toString());
				builder.put(materialName, missing);
			}
			else
				builder.put(materialName, ModelLoader.defaultTextureGetter().apply(material.getTexture().getTextureLocation()));
		}
		builder.put("missingno", missing);
		return builder.build();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void render(float scale) {
		
		// Have to render .obj's, since base doesn't let us add to their drawlist!
		// But this way we get to call our callbacks before, anyways!
		
		init();
		
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.pushMatrix();
		
		//GlStateManager.rotate(180, 1, 0, 0); // .OBJ's tend to have flipped Zs I suppose
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		
		if (this.preRender(buffer)) {
			renderInternal(scale, compiledList);
		}
		
		tessellator.draw();
		
		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		
		super.render(scale); // Any boxes and stuff that was added (and children!!!!!!!)
	}
	
	// This is a copy of ModelRenderer's render method, but using our display list
	private void renderInternal(float scale, int displayList) {
		if (!this.isHidden) {
			if (this.showModel) {

				
				GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);
				GlStateManager.rotate(180, 1, 0, 0);

				if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
					if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F) {
						GlStateManager.callList(displayList);
					} else {
						GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
						GlStateManager.callList(displayList);
						GlStateManager.translate(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
					}
				} else {
					GlStateManager.pushMatrix();
					GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

					if (this.rotateAngleZ != 0.0F) {
						GlStateManager.rotate(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
					}

					if (this.rotateAngleY != 0.0F) {
						GlStateManager.rotate(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
					}

					if (this.rotateAngleX != 0.0F) {
						GlStateManager.rotate(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
					}

					GlStateManager.callList(displayList);

					GlStateManager.popMatrix();
				}

				GlStateManager.translate(-this.offsetX, -this.offsetY, -this.offsetZ);
			}
		}
	}

}
