package com.smanzana.nostrummagica.client.render.layer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class LayerManaArmor extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

	public static final ResourceLocation TEXTURE_ARMOR = new ResourceLocation(NostrumMagica.MODID, "textures/entity/manaarmor.png");
	protected final PlayerRenderer renderPlayer;
	
	public LayerManaArmor(PlayerRenderer renderPlayerIn) {
		super(renderPlayerIn);
		this.renderPlayer = renderPlayerIn;
	}
	
	@Override
	public void render(MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (shouldRender(player)) {
			renderInternal(stack, typeBuffer, packedLight, player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
		}
	}
	
	public boolean shouldRender(AbstractClientPlayerEntity player) {
		@Nullable IManaArmor armor = NostrumMagica.getManaArmor(player);
		return armor != null && armor.hasArmor();
	}
	
	public int getColor(AbstractClientPlayerEntity player) {
		return 0x602244FF;
	}
	
	private boolean recurseMarker = false;
	public void renderInternal(MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		
		if (!recurseMarker) {
			recurseMarker = true;
			
			final float[] color = ColorUtil.ARGBToColor(getColor(player));
			final float progPeriod = 3 * 20;
			final float growScale = .03f;
			
			final float prog = partialTicks + ageInTicks;
			final float progAdj = (prog % progPeriod) / progPeriod;
			final float growAmt = (MathHelper.sin(progAdj * 3.1415f * 2) * growScale) + growScale;
			
			IVertexBuilder buffer = typeBuffer.getBuffer(NostrumRenderTypes.MANA_ARMOR);
			
			stack.pushPose();
			stack.scale(1.002f + growAmt, 1.002f + growAmt, 1.002f + growAmt);
			
			this.renderPlayer.getModel().renderToBuffer(stack, buffer, packedLight, packedLight, color[0], color[1], color[2], color[3]);
			
			stack.popPose();
			recurseMarker = false;
		}
		
	}
}
