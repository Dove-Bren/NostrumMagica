package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.BillboardModel;
import com.smanzana.nostrummagica.client.model.KeySwitchTriggerModel;
import com.smanzana.nostrummagica.entity.KeySwitchTriggerEntity;
import com.smanzana.nostrummagica.item.WorldKeyItem;
import com.smanzana.nostrummagica.tile.KeySwitchBlockTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class KeySwitchTriggerRenderer extends LivingEntityRenderer<KeySwitchTriggerEntity, KeySwitchTriggerModel> {

	private static final ResourceLocation CAGE_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/block/key_cage.png");
	private static final ResourceLocation KEY_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/item/key.png");

	protected BillboardModel iconModel;
	
	public KeySwitchTriggerRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn, new KeySwitchTriggerModel(), .1f);
		iconModel = new BillboardModel();
		iconModel.setRadius(.25f);
	}

	@Override
	public ResourceLocation getTextureLocation(KeySwitchTriggerEntity entity) {
		return CAGE_TEXT;
	}
	
	@Override
	protected boolean shouldShowName(KeySwitchTriggerEntity entity) {
		return entity.hasCustomName() || NostrumMagica.instance.proxy.getPlayer().isCreative();
	}
	
	@Override
	protected void renderNameTag(KeySwitchTriggerEntity entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		final Minecraft mc = Minecraft.getInstance();
		final double ticks = entityIn.level.getGameTime();
		final String info;
		boolean matches = false;
		KeySwitchBlockTileEntity te = (KeySwitchBlockTileEntity) entityIn.getLinkedTileEntity();
		if (te != null) {
			if (te.getWorldKey() != null) {
				WorldKey key = te.getWorldKey();
				info = mc.player.isShiftKeyDown() ? key.toString() : key.toString().substring(0, 8);
				
				final ItemStack held = mc.player.getMainHandItem();
				if ((held.getItem() instanceof WorldKeyItem && key.equals(((WorldKeyItem) held.getItem()).getKey(held)))) {
					matches = true;
				}
			} else {
				info = "No lock info found";
			}
		} else {
			 info = "Missing TileEntity";
		}
		
		float yOffset = 0;
		if (matches) {
			final double matchWigglePeriod = 20;
			final double matchWiggleProg = 1 - ((ticks % matchWigglePeriod) / matchWigglePeriod);
			yOffset += (float) (.05 * Math.sin(2 * Math.PI * matchWiggleProg));
		}
		
		//renderLivingLabel(entityIn, info, x, y + yOffset, z, 64);
//		{
//			boolean flag = !entityIn.isDiscrete();
//			float y = entityIn.getHeight() + 0.5F;
//			
//			matrixStackIn.push();
//			matrixStackIn.translate(0.0D, (double)y + yOffset, 0.0D);
//			matrixStackIn.rotate(this.renderManager.getCameraOrientation());
//			matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
//			
//			Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
//			float f1 = mc.gameSettings.getTextBackgroundOpacity(0.25F);
//			int j = (int)(f1 * 255.0F) << 24;
//			FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
//			float f2 = (float)(-fontrenderer.getStringWidth(info) / 2);
//			fontrenderer.renderString(info, f2, 0, 553648127, false, matrix4f, bufferIn, flag, j, packedLightIn);
//			if (flag) {
//				fontrenderer.renderString(info, f2, 0, -1, false, matrix4f, bufferIn, false, 0, packedLightIn);
//			}
//			
//			matrixStackIn.pop();
//		}
		RenderFuncs.drawNameplate(matrixStackIn, bufferIn, entityIn, info, this.getFont(), packedLightIn, yOffset, this.entityRenderDispatcher.camera);
	}
	
	@Override
	public void render(KeySwitchTriggerEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		// Draw switch box, which is model passed to parent
		final double spinIdle = 3.0; // seconds per turn
		final double spinActivated = 3.0;
		final double ticks = (partialTicks % 1) + entityIn.level.getGameTime();
		final KeySwitchBlockTileEntity te = (KeySwitchBlockTileEntity) entityIn.getLinkedTileEntity();
		final boolean fast = (te != null && te.isTriggered());
		final double period = (float) (20 * (fast ? spinActivated * 2 : spinIdle));
		final float angle = 360f * (float)((ticks % period) / period);
		
		final float bobAngle = (float) (2 * Math.PI * (ticks % 60 / 60));
		final double bob = Math.sin(bobAngle) * .1;
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, bob, 0);
		
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(angle));
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		matrixStackIn.popPose();
		
		// Draw key
		matrixStackIn.pushPose();
		// replicate the transforms from living rendere. Could be in preRenderCallback
		matrixStackIn.translate(0, 1.5f, 0);
		VertexConsumer buffer = bufferIn.getBuffer(iconModel.renderType(KEY_TEXT));
		iconModel.renderToBuffer(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		matrixStackIn.popPose();
		matrixStackIn.popPose();
	}
	
}
