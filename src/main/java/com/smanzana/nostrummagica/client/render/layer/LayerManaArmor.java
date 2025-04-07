package com.smanzana.nostrummagica.client.render.layer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LayerManaArmor extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

	public static final ResourceLocation TEXTURE_ARMOR = new ResourceLocation(NostrumMagica.MODID, "textures/entity/manaarmor.png");
	protected final PlayerRenderer renderPlayer;
	
	public LayerManaArmor(PlayerRenderer renderPlayerIn) {
		super(renderPlayerIn);
		this.renderPlayer = renderPlayerIn;
	}
	
	@Override
	public void render(PoseStack stack, MultiBufferSource typeBuffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (shouldRender(player)) {
			renderInternal(stack, typeBuffer, packedLight, player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
		}
	}
	
	public boolean shouldRender(AbstractClientPlayer player) {
		@Nullable IManaArmor armor = NostrumMagica.getManaArmor(player);
		return armor != null && armor.hasArmor();
	}
	
	public int getColor(AbstractClientPlayer player) {
		return 0xAA2244FF;
	}
	
	private boolean recurseMarker = false;
	public void renderInternal(PoseStack stack, MultiBufferSource typeBuffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		
		if (!recurseMarker) {
			recurseMarker = true;
			
			final float[] color = ColorUtil.ARGBToColor(getColor(player));
			final float progPeriod = 3 * 20;
			final float growScale = .03f;
			
			final float prog = partialTicks + ageInTicks;
			final float progAdj = (prog % progPeriod) / progPeriod;
			final float growAmt = (Mth.sin(progAdj * 3.1415f * 2) * growScale) + growScale;
			
			VertexConsumer buffer = typeBuffer.getBuffer(NostrumRenderTypes.MANA_ARMOR);
			
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			
			stack.pushPose();
			stack.scale(1.002f + growAmt, 1.002f + growAmt, 1.002f + growAmt);
			
			this.renderPlayer.getModel().renderToBuffer(stack, buffer, packedLight, packedLight, color[0], color[1], color[2], color[3]);
			
			stack.popPose();
			recurseMarker = false;
		}
		
	}
}
