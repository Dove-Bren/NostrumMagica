package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity;
import com.smanzana.nostrummagica.util.ModelUtils;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityObeliskRenderer extends TileEntityRenderer<ObeliskTileEntity> {

	protected static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "block/orb_crystal");
	protected IBakedModel model;
	
	public TileEntityObeliskRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	protected void initModel() {
		if (model == null) {
			model = ModelUtils.GetBakedModel(MODEL);
		}
	}
	
	@Override
	public void render(ObeliskTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

		if (tileEntityIn.isMaster())
			return;
		
		initModel();
		if (this.model == null) 
			return;
		
		final BlockState state = tileEntityIn.getBlockState();
		final long time = System.currentTimeMillis();
		float rotY = (float) (time % 3000) / 3000f;
		float rotX = (float) (time % 5000) / 5000f;
		
		
		rotY *= 360f;
		rotX *= 360f;
		
		matrixStackIn.push();
		matrixStackIn.translate(.5, .5, .5);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rotY));
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(rotX));
		RenderFuncs.RenderBlockState(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn); // Used to fetch custom model and render itself
		RenderFuncs.RenderModel(matrixStackIn, bufferIn.getBuffer(RenderType.getCutoutMipped()), model, combinedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		matrixStackIn.pop();
	}
}
