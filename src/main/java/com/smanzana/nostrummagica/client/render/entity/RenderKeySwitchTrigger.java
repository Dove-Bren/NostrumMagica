package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelBillboard;
import com.smanzana.nostrummagica.client.model.ModelKeySwitchTrigger;
import com.smanzana.nostrummagica.entity.EntityKeySwitchTrigger;
import com.smanzana.nostrummagica.item.WorldKeyItem;
import com.smanzana.nostrummagica.tile.KeySwitchBlockTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;

public class RenderKeySwitchTrigger extends LivingRenderer<EntityKeySwitchTrigger, ModelKeySwitchTrigger> {

	private static final ResourceLocation CAGE_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/block/key_cage.png");
	private static final ResourceLocation KEY_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/item/key.png");

	protected ModelBillboard iconModel;
	
	public RenderKeySwitchTrigger(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelKeySwitchTrigger(), .1f);
		iconModel = new ModelBillboard();
		iconModel.setRadius(.25f);
	}

	@Override
	public ResourceLocation getEntityTexture(EntityKeySwitchTrigger entity) {
		return CAGE_TEXT;
	}
	
	@Override
	protected boolean canRenderName(EntityKeySwitchTrigger entity) {
		return entity.hasCustomName() || NostrumMagica.instance.proxy.getPlayer().isCreative();
	}
	
	@Override
	protected void renderName(EntityKeySwitchTrigger entityIn, ITextComponent displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		final Minecraft mc = Minecraft.getInstance();
		final double ticks = entityIn.world.getGameTime();
		final String info;
		boolean matches = false;
		KeySwitchBlockTileEntity te = (KeySwitchBlockTileEntity) entityIn.getLinkedTileEntity();
		if (te != null) {
			if (te.getWorldKey() != null) {
				NostrumWorldKey key = te.getWorldKey();
				info = mc.player.isSneaking() ? key.toString() : key.toString().substring(0, 8);
				
				final ItemStack held = mc.player.getHeldItemMainhand();
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
		RenderFuncs.drawNameplate(matrixStackIn, bufferIn, entityIn, info, this.getFontRendererFromRenderManager(), packedLightIn, yOffset, this.renderManager.info);
	}
	
	@Override
	public void render(EntityKeySwitchTrigger entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		// Draw switch box, which is model passed to parent
		final double spinIdle = 3.0; // seconds per turn
		final double spinActivated = 3.0;
		final double ticks = (partialTicks % 1) + entityIn.world.getGameTime();
		final KeySwitchBlockTileEntity te = (KeySwitchBlockTileEntity) entityIn.getLinkedTileEntity();
		final boolean fast = (te != null && te.isTriggered());
		final double period = (float) (20 * (fast ? spinActivated * 2 : spinIdle));
		final float angle = 360f * (float)((ticks % period) / period);
		
		final float bobAngle = (float) (2 * Math.PI * (ticks % 60 / 60));
		final double bob = Math.sin(bobAngle) * .1;
		
		matrixStackIn.push();
		matrixStackIn.translate(0, bob, 0);
		
		matrixStackIn.push();
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(angle));
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		matrixStackIn.pop();
		
		// Draw key
		matrixStackIn.push();
		// replicate the transforms from living rendere. Could be in preRenderCallback
		matrixStackIn.translate(0, 1.5f, 0);
		IVertexBuilder buffer = bufferIn.getBuffer(iconModel.getRenderType(KEY_TEXT));
		iconModel.render(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		matrixStackIn.pop();
		matrixStackIn.pop();
	}
	
}
