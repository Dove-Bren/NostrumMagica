package com.smanzana.nostrummagica.client.render;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.items.ICapeProvider;
import com.smanzana.nostrummagica.items.IElytraProvider;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArmorLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class LayerCustomElytra<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {

	protected static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");
	protected final ElytraModel<T> modelElytra = new ElytraModel<>();
	protected final IEntityRenderer<T, M> renderer;
	
	public LayerCustomElytra(IEntityRenderer<T, M> rendererIn) {
		super(rendererIn);
		this.renderer = rendererIn;
	}
	
	@Override
	public void render(T player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (shouldRender(player)) {
			@Nonnull ItemStack chestpiece = player.getItemStackFromSlot(EquipmentSlotType.CHEST); 
			render(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, (!chestpiece.isEmpty() && chestpiece.isEnchanted()));
		}
	}
	
	public boolean shouldRender(T player) {
		final boolean flying = player.isElytraFlying();
		ItemStack cape = LayerAetherCloak.ShouldRender(player);
		if (!flying && !cape.isEmpty() && ((ICapeProvider) cape.getItem()).shouldPreventOtherRenders(player, cape)) {
			return false;
		}
		
		for (@Nonnull ItemStack stack : player.getEquipmentAndArmor()) {
			if (!stack.isEmpty() && stack.getItem() instanceof IElytraProvider) {
				if (((IElytraProvider) stack.getItem()).shouldRenderElyta(player, stack)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void render(T player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean enchanted) {
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();

		if (player instanceof AbstractClientPlayerEntity) {
			AbstractClientPlayerEntity clientPlayer = (AbstractClientPlayerEntity) player;
			if (clientPlayer.isPlayerInfoSet() && clientPlayer.getLocationElytra() != null) {
				this.renderer.bindTexture(clientPlayer.getLocationElytra());
			} else if (clientPlayer.hasPlayerInfo() && clientPlayer.getLocationCape() != null && clientPlayer.isWearing(PlayerModelPart.CAPE)) {
				this.renderer.bindTexture(clientPlayer.getLocationCape());
			} else {
				this.renderer.bindTexture(TEXTURE_ELYTRA);
			}
		} else {
			this.renderer.bindTexture(TEXTURE_ELYTRA);
		}

		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.0F, 0.0F, 0.125F);
		this.modelElytra.setRotationAngles(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		this.modelElytra.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		if (enchanted) {
			ArmorLayer.func_215338_a(this::bindTexture, player, this.modelElytra, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		}

		GlStateManager.popMatrix();
	}
	
}
