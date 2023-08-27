package com.smanzana.nostrummagica.client.render.entity;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Material;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.FMLLog;

/**
 * This class taken from 
 * https://github.com/2piradians/Minewatch/blob/1.12.1/src/main/java/twopiradians/minewatch/client/render/entity/RenderOBJModel.java
 * @author Skyler
 *
 * @param <T>
 */
public abstract class RenderOBJModel<T extends Entity> extends Render<T> {

	// Note: Make sure to register new textures in ClientProxy#stitchEventPre
	private IBakedModel[] bakedModels;

	protected RenderOBJModel(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity) {
		return null;
	}

	protected abstract ResourceLocation[] getEntityModels();
	protected abstract boolean preRender(T entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks);
	protected IModel retexture(int i, IModel model) {return model;}
	protected int getColor(int i, T entity) {return -1;}

	/**Adapted from ForgeBlockModelRenderer#render*/
	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {	
		if (this.bakedModels == null) {
			this.bakedModels = new IBakedModel[this.getEntityModels().length];
			for (int i=0; i<this.getEntityModels().length; ++i) {
				IModel model = ModelLoaderRegistry.getModelOrLogError(this.getEntityModels()[i], "Nostrum Magica is missing a model. Please report this to the mod authors.");
				model = this.retexture(i, model);
				IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
				if (bakedModel instanceof OBJBakedModel && model instanceof OBJModel)
					this.bakedModels[i] = ((OBJModel) model).new OBJBakedModel((OBJModel) model, ((OBJBakedModel) bakedModel).getState(), DefaultVertexFormats.ITEM, getTextures((OBJModel) model));
				
			}
		}

		for (int i=0; i<this.bakedModels.length; ++i) {
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableRescaleNormal();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.pushMatrix();

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

			GlStateManager.rotatef(180, 0, 0, 1);
			GlStateManager.translatef((float)-x, (float)-y, (float)z);
			GlStateManager.rotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, 1.0F, 0.0F);
			if (this.preRender(entity, i, buffer, x, y, z, entityYaw, partialTicks)) {
				int color = this.getColor(i, entity);
				GlStateManager.rotatef(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 1.0F, 0.0F, 0.0F);

				for(Direction side : Direction.values()) {
					List<BakedQuad> quads = this.bakedModels[i].getQuads(null, side, 0);
					if(!quads.isEmpty()) 
						for(BakedQuad quad : quads)
							LightUtil.renderQuadColor(buffer, quad, color == -1 ? color : color | -16777216);
				}
				List<BakedQuad> quads = this.bakedModels[i].getQuads(null, null, 0);
				if(!quads.isEmpty()) {
					for(BakedQuad quad : quads) 
						LightUtil.renderQuadColor(buffer, quad, color == -1 ? color : color | -16777216);
				}
			}
			buffer.setTranslation(0, 0, 0);
			tessellator.draw();	

			GlStateManager.cullFace(GlStateManager.CullFace.BACK);
			GlStateManager.popMatrix();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableBlend();
			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		}
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

}
