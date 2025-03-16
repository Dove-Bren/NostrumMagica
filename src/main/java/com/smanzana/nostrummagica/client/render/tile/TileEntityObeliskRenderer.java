package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity;
import com.smanzana.nostrummagica.util.ModelUtils;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityObeliskRenderer extends BlockEntityRendererBase<ObeliskTileEntity> {

	protected static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "block/orb_crystal");
	protected BakedModel model;
	
	public TileEntityObeliskRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}
	
	protected void initModel() {
		if (model == null) {
			model = ModelUtils.GetBakedModel(MODEL);
		}
	}
	
	@Override
	public void render(ObeliskTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

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
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(.5, .5, .5);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rotY));
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(rotX));
		RenderFuncs.RenderBlockState(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn); // Used to fetch custom model and render itself
		RenderFuncs.RenderModel(matrixStackIn, bufferIn.getBuffer(RenderType.cutoutMipped()), model, combinedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		matrixStackIn.popPose();
	}
}
