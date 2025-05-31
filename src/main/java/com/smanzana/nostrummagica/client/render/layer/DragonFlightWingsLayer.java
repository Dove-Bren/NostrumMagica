package com.smanzana.nostrummagica.client.render.layer;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.DragonFlightWingsModel;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.item.api.IDragonWingRenderItem;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Vector3f;

public class DragonFlightWingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

	protected static final ResourceLocation TEXTURE_WINGS = new ResourceLocation(NostrumMagica.MODID, "textures/entity/dragonflightwing.png");
	protected final DragonFlightWingsModel<AbstractClientPlayer> model;
	protected final PlayerRenderer renderPlayer;
	
	public DragonFlightWingsLayer(PlayerRenderer renderPlayerIn) {
		super(renderPlayerIn);
		this.renderPlayer = renderPlayerIn;
		this.model = new DragonFlightWingsModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(NostrumModelLayers.FlightWings));
	}
	
	@Override
	public void render(PoseStack stack, MultiBufferSource typeBuffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (shouldRender(player)) {
			@Nonnull ItemStack chestpiece = player.getItemBySlot(EquipmentSlot.CHEST); 
			render(stack, typeBuffer, packedLight, player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, (!chestpiece.isEmpty() && chestpiece.isEnchanted()));
		}
	}
	
	public boolean shouldRender(AbstractClientPlayer player) {
		for (ItemStack stack : player.getArmorSlots()) {
			if (!stack.isEmpty() && stack.getItem() instanceof IDragonWingRenderItem) {
				if (((IDragonWingRenderItem) stack.getItem()).shouldRenderDragonWings(stack, player)) {
					return true;
				}
			}
		}
		
		// Try bauables
		Container baubles = NostrumMagica.CuriosProxy.getCurios(player);
		
		if (baubles != null) {
			for (int i = 0; i < baubles.getContainerSize(); i++) {
				ItemStack stack = baubles.getItem(i);
				if (!stack.isEmpty() && stack.getItem() instanceof IDragonWingRenderItem) {
					if (((IDragonWingRenderItem) stack.getItem()).shouldRenderDragonWings(stack, player)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public int getColor(AbstractClientPlayer player) {
		for (ItemStack stack : player.getArmorSlots()) {
			if (!stack.isEmpty() && stack.getItem() instanceof IDragonWingRenderItem) {
				if (((IDragonWingRenderItem) stack.getItem()).shouldRenderDragonWings(stack, player)) {
					return ((IDragonWingRenderItem) stack.getItem()).getDragonWingColor(stack, player);
				}
			}
		}
		
		// Try bauables
		Container baubles = NostrumMagica.CuriosProxy.getCurios(player);
		if (baubles != null) {
			for (int i = 0; i < baubles.getContainerSize(); i++) {
				ItemStack stack = baubles.getItem(i);
				if (!stack.isEmpty() && stack.getItem() instanceof IDragonWingRenderItem) {
					if (((IDragonWingRenderItem) stack.getItem()).shouldRenderDragonWings(stack, player)) {
						return ((IDragonWingRenderItem) stack.getItem()).getDragonWingColor(stack, player);
					}
				}
			}
		}
		
		return 0xFF000000;
	}
	
	public void render(PoseStack stack, MultiBufferSource typeBuffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, boolean enchanted) {
		
		final float colors[] = ColorUtil.ARGBToColor(getColor(player));
		final VertexConsumer buffer = typeBuffer.getBuffer(RenderType.entitySolid(TEXTURE_WINGS));
		
		stack.pushPose();
		stack.mulPose(Vector3f.XP.rotationDegrees(player.isShiftKeyDown() ? 25f : 0));
		stack.translate(0, 0, .125f);
		stack.translate(0, player.getBbHeight() * .3f, player.getBbWidth() * .3f);
		stack.translate(0, player.isShiftKeyDown() ? .3 : 0, 0); // This is kind tear-y but things like elytra sore 'last' on the entity to smooth!
		model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		model.renderToBuffer(stack, buffer, packedLight, OverlayTexture.NO_OVERLAY, colors[0], colors[1], colors[2], colors[3]);
		
//		if (enchanted) {
//		LayerArmorBase.renderEnchantedGlint(this.renderPlayer, player, model, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
//	}
		
		stack.popPose();
	}
}
