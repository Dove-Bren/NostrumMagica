package com.smanzana.nostrummagica.client.render;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class LayerManaArmor implements LayerRenderer<AbstractClientPlayer> {

	protected static final ResourceLocation TEXTURE_ARMOR = new ResourceLocation(NostrumMagica.MODID, "textures/entity/manaarmor.png");
	protected final RenderPlayer renderPlayer;
	
	public LayerManaArmor(RenderPlayer renderPlayerIn) {
		//super(renderPlayerIn);
		this.renderPlayer = renderPlayerIn;
	}
	
	@Override
	public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (shouldRender(player)) {
			render(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		}
	}
	
	public boolean shouldRender(AbstractClientPlayer player) {
		@Nullable IManaArmor armor = NostrumMagica.getManaArmor(player);
		return armor != null && armor.hasArmor();
	}
	
	public int getColor(AbstractClientPlayer player) {
		return 0x602244FF;
	}
	
	private boolean recurseMarker = false;
	public void render(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		
		if (!recurseMarker) {
			recurseMarker = true;
		
			final int color = getColor(player);
			GlStateManager.color((float)((color >> 16) & 0xFF) / 255f,
					(float)((color >> 8) & 0xFF) / 255f,
					(float)((color >> 0) & 0xFF) / 255f,
						(float)((color >> 24) & 0xFF) / 255f);
			
			final float progPeriod = 3 * 20;
			final float growScale = .03f;
			
			final float prog = partialTicks + ageInTicks;
			final float progAdj = (prog % progPeriod) / progPeriod;
			final float growAmt = (MathHelper.sin(progAdj * 3.1415f * 2) * growScale) + growScale;
			
			GlStateManager.disableBlend();
			GlStateManager.disableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.disableTexture2D();
			GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableColorLogic();
			GlStateManager.enableColorMaterial();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
	
			this.renderPlayer.bindTexture(TEXTURE_ARMOR);
			
			GlStateManager.pushMatrix();
			GlStateManager.scale(1.0 + growAmt, 1.0 + growAmt, 1.0 + growAmt);
			
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			GlStateManager.translate(0 + (ageInTicks + partialTicks) * .001, 0, 0);
			
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			
			{
				this.renderPlayer.getMainModel().render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale + .002f);
				
				//this.renderPlayer.doRender(player, 0, 0, 0, netHeadYaw, partialTicks);
			}
			
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
	//		GlStateManager.translate(0.0F, 0.0F, 0.125F);
	//		model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
	//		model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
	
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	//		if (enchanted) {
	//			LayerArmorBase.renderEnchantedGlint(this.renderPlayer, player, model, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
	//		}
	
			GlStateManager.popMatrix();
			recurseMarker = false;
		}
		
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
	
}
