package com.smanzana.nostrummagica.client.render.entity;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.utils.ModelUtils;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

/**
 * A BakedModel but as a ModelRenderer.
 * 
 * Used to be a wrapper for OBJ files, and even wrapped it in a GL render list to speed rendering it up.
 * 
 * This class originally adapted from 
 * https://github.com/2piradians/Minewatch/blob/1.12.1/src/main/java/twopiradians/minewatch/client/render/entity/RenderOBJModel.java.
 * @author Skyler
 *
 * @param <T>
 */
public class ModelRendererBaked extends ModelRenderer {

	private final ResourceLocation modelLocation;
	private @Nullable IBakedModel bakedModel;
	private boolean loaded;
	
	public ModelRendererBaked(Model base, ResourceLocation modelLocation) {
		super(base);
		
		this.modelLocation = modelLocation;
	}
	
	protected IBakedModel loadModel(ResourceLocation location) {
		return ModelUtils.GetBakedModel(location);
	}
	
	protected void checkAndLoadModel() {
		if (!loaded) {
			loaded = true;
			this.bakedModel = loadModel(this.modelLocation);
		}
	}
	
	// Made public with AT :)
	@Override
	public void doRender(MatrixStack.Entry matrixEntryIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		checkAndLoadModel();
		
		// Render obj model
		RenderFuncs.RenderModel(matrixEntryIn, bufferIn, bakedModel, packedLightIn, red, green, blue, alpha);
		
		// Render boxes and children renderes that may have been set up
		super.doRender(matrixEntryIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
//	protected boolean preRender(BufferBuilder buffer, float scale) {
//		return true;
//	}
	
//	protected IModel retexture(IModel model) {
//		return model;
//	}
	
//	protected int getColor() {
//		return -1;
//	}

//	// Render the model, where it will be captured to a GL compiled list for faster rendering in the future.
//	// This means things like color etc. are baked in, though.
//	@OnlyIn(Dist.CLIENT)
//    private void renderForCapture(IBakedModel model, BufferBuilder buffer, int color) {
//		Random rand = new Random();
//		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
//		GlStateManager.pushMatrix();
//		GlStateManager.rotatef(180, 1, 0, 0);
//		
//		for(Direction side : Direction.values()) {
//			List<BakedQuad> quads = model.getQuads(null, side, RenderFuncs.RenderRandom(rand), EmptyModelData.INSTANCE);
//			if(!quads.isEmpty()) 
//				for(BakedQuad quad : quads) {
//					//buffer.addVertexData(quad.getVertexData());
//					LightUtil.renderQuadColor(buffer, quad, color);
//				}
//		}
//		List<BakedQuad> quads = model.getQuads(null, null, RenderFuncs.RenderRandom(rand), EmptyModelData.INSTANCE);
//		if(!quads.isEmpty()) {
//			for(BakedQuad quad : quads) 
//				//buffer.addVertexData(quad.getVertexData());
//				LightUtil.renderQuadColor(buffer, quad, color);
//		}
//			
//		GlStateManager.popMatrix();
//		Tessellator.getInstance().draw();
//			
//			
//			//// COPY
////			GlStateManager.shadeModel(GL11.GL_SMOOTH);
////			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
////			Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
////			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
////			GlStateManager.enableRescaleNormal();
////			GlStateManager.alphaFunc(516, 0.1F);
////			GlStateManager.enableBlend();
////			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
////			GlStateManager.pushMatrix();
////
////			Tessellator tessellator = Tessellator.getInstance();
////			BufferBuilder buffer = tessellator.getBuffer();
////			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
////
////			GlStateManager.rotatef(180, 0, 0, 1);
////			GlStateManager.translatef((float)-x, (float)-y, (float)z);
////			GlStateManager.rotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, 1.0F, 0.0F);
////			if (this.preRender(entity, i, buffer, x, y, z, entityYaw, partialTicks)) {
////				int color = this.getColor(i, entity);
////				GlStateManager.rotatef(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 1.0F, 0.0F, 0.0F);
////
////				for(Direction side : Direction.values()) {
////					List<BakedQuad> quads = this.bakedModels[i].getQuads(null, side, 0);
////					if(!quads.isEmpty()) 
////						for(BakedQuad quad : quads)
////							LightUtil.renderQuadColor(buffer, quad, color == -1 ? color : color | -16777216);
////				}
////				List<BakedQuad> quads = this.bakedModels[i].getQuads(null, null, 0);
////				if(!quads.isEmpty()) {
////					for(BakedQuad quad : quads) 
////						LightUtil.renderQuadColor(buffer, quad, color == -1 ? color : color | -16777216);
////				}
////			}
////			buffer.setTranslation(0, 0, 0);
////			tessellator.draw();	
////
////			GlStateManager.cullFace(GlStateManager.CullFace.BACK);
////			GlStateManager.popMatrix();
////			GlStateManager.disableRescaleNormal();
////			GlStateManager.disableBlend();
////			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
////			Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
//	}
	
//	public static ImmutableMap<String, TextureAtlasSprite> getTextures(OBJModel model) {
//		ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
//		builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
//		TextureAtlasSprite missing = ModelLoader.defaultTextureGetter().apply(new ResourceLocation("missingno"));
//		for (String materialName : model.getMatLib().getMaterialNames()) {
//			Material material = model.getMatLib().getMaterial(materialName);
//			if (material.getTexture().getTextureLocation().getResourcePath().startsWith("#")) {
//				FMLLog.bigWarning("OBJLoaderMW: Unresolved texture '%s' for obj model '%s'", material.getTexture().getTextureLocation().getResourcePath(), model.toString());
//				builder.put(materialName, missing);
//			}
//			else
//				builder.put(materialName, ModelLoader.defaultTextureGetter().apply(material.getTexture().getTextureLocation()));
//		}
//		builder.put("missingno", missing);
//		return builder.build();
//	}
	
//	@OnlyIn(Dist.CLIENT)
//	@Override
//	public void render(float scale) {
//		
//		// Have to render .obj's, since base doesn't let us add to their drawlist!
//		// But this way we get to call our callbacks before, anyways!
//		
//		init();
//		
//		Tessellator tessellator = Tessellator.getInstance();
//		BufferBuilder buffer = tessellator.getBuffer();
//		GlStateManager.shadeModel(GL11.GL_SMOOTH);
//		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//		Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
//		//GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//		GlStateManager.enableRescaleNormal();
//		GlStateManager.alphaFunc(516, 0.1F);
//		GlStateManager.enableBlend();
//		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//		GlStateManager.pushMatrix();
//		
//		//GlStateManager.rotatef(180, 1, 0, 0); // .OBJ's tend to have flipped Zs I suppose
//		
//		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
//		
//		if (this.preRender(buffer, scale)) {
//			renderInternal(scale, compiledList);
//		}
//		
//		tessellator.draw();
//		
//		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
//		GlStateManager.popMatrix();
//		//GlStateManager.disableRescaleNormal();
//		GlStateManager.disableBlend();
//		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//		Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
//		GlStateManager.color4f(1f, 1f, 1f, 1f);
//		
//		super.render(scale); // Any boxes and stuff that was added (and children!!!!!!!)
//	}
//	
//	// This is a copy of ModelRenderer's render method, but using our display list
//	private void renderInternal(float scale, int displayList) {
//		if (!this.isHidden) {
//			if (this.showModel) {
//				
//				GlStateManager.enableBlend();
//				GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
//				GlStateManager.enableAlphaTest();
//				final int color = this.getColor();
//				GlStateManager.color4f(
//						((float) ((color >> 16) & 0xFF)) / 256f,
//						((float) ((color >> 8) & 0xFF)) / 256f,
//						((float) ((color >> 0) & 0xFF)) / 256f,
//						((float) ((color >> 24) & 0xFF)) / 256f
//						);
//				
//				GlStateManager.translatef(this.offsetX, this.offsetY, this.offsetZ);
//				GlStateManager.rotatef(180, 1, 0, 0);
//
//				if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
//					if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F) {
//						GlStateManager.callList(displayList);
//					} else {
//						GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
//						GlStateManager.callList(displayList);
//						GlStateManager.translatef(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
//					}
//				} else {
//					GlStateManager.pushMatrix();
//					GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
//
//					if (this.rotateAngleZ != 0.0F) {
//						GlStateManager.rotatef(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
//					}
//
//					if (this.rotateAngleY != 0.0F) {
//						GlStateManager.rotatef(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
//					}
//
//					if (this.rotateAngleX != 0.0F) {
//						GlStateManager.rotatef(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
//					}
//
//					GlStateManager.callList(displayList);
//
//					GlStateManager.popMatrix();
//				}
//
//				GlStateManager.translatef(-this.offsetX, -this.offsetY, -this.offsetZ);
//			}
//		}
//	}

}
