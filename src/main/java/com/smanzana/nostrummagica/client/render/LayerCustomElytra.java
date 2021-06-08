package com.smanzana.nostrummagica.client.render;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.items.ICapeProvider;
import com.smanzana.nostrummagica.items.IElytraProvider;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class LayerCustomElytra extends LayerElytra {

	protected static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");
	protected final ModelElytra modelElytra = new ModelElytra();
	protected final RenderPlayer renderPlayer;
	
	public LayerCustomElytra(RenderPlayer renderPlayerIn) {
		super(renderPlayerIn);
		this.renderPlayer = renderPlayerIn;
	}
	
	@Override
	public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (shouldRender(player)) {
			@Nullable ItemStack chestpiece = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST); 
			render(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, (chestpiece != null && chestpiece.isItemEnchanted()));
		}
	}
	
	public boolean shouldRender(AbstractClientPlayer player) {
		final boolean flying = player.isElytraFlying();
		ItemStack cape = LayerAetherCloak.ShouldRender(player);
		if (!flying && cape != null && ((ICapeProvider) cape.getItem()).shouldPreventOtherRenders(player, cape)) {
			return false;
		}
		
		for (@Nullable ItemStack stack : player.getEquipmentAndArmor()) {
			if (stack != null && stack.getItem() instanceof IElytraProvider) {
				if (((IElytraProvider) stack.getItem()).shouldRenderElyta(player, stack)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void render(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean enchanted) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();

		if (player.isPlayerInfoSet() && player.getLocationElytra() != null) {
			this.renderPlayer.bindTexture(player.getLocationElytra());
		} else if (player.hasPlayerInfo() && player.getLocationCape() != null && player.isWearing(EnumPlayerModelParts.CAPE)) {
			this.renderPlayer.bindTexture(player.getLocationCape());
		} else {
			this.renderPlayer.bindTexture(TEXTURE_ELYTRA);
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 0.125F);
		this.modelElytra.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
		this.modelElytra.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		if (enchanted) {
			LayerArmorBase.renderEnchantedGlint(this.renderPlayer, player, this.modelElytra, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		}

		GlStateManager.popMatrix();
	}
	
}
