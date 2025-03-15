package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// From made from loading a model and rendering it
@OnlyIn(Dist.CLIENT)
public class ClientEffectFormBasic implements ClientEffectForm {
	
	private IBakedModel model;
	private Vector3d offset;
	
	public ClientEffectFormBasic(String key) {
		this(key, 0d, 0d, 0d);
	}
	
	public ClientEffectFormBasic(String key, double x, double y, double z) {
		if (x != 0 || y != 0 || z != 0)
			this.offset = new Vector3d(x, y, z);
		else
			this.offset = null;
		
		BlockRendererDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
		
		final String modelLoc = "effect/" + key;
		model = renderer.getBlockModelShaper().getModelManager().getModel(NostrumMagica.Loc(modelLoc));
	}
	
	public ClientEffectFormBasic(ClientEffectIcon icon, double x, double y, double z) {
		this(icon.getKey(), x, y, z);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int color) {
		matrixStackIn.pushPose();
		if (this.offset != null) {
			matrixStackIn.translate(offset.x, offset.y, offset.z);
		}
		
		int unused; // make this be a passed in thing! Not all are objs!
		mc.getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS);
		
		final int light = ClientEffectForm.InferLightmap(matrixStackIn, mc);
		RenderSystem.disableCull();
		RenderSystem.enableDepthTest();
		ClientEffectForm.drawModel(matrixStackIn, model, color, light);
		RenderSystem.enableCull();
		matrixStackIn.popPose();
	}
}
