package com.smanzana.nostrummagica.client.render;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.entity.ModelDragonFlightWings;
import com.smanzana.nostrummagica.items.IDragonWingRenderItem;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class LayerDragonFlightWings extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

	protected static final ResourceLocation TEXTURE_WINGS = new ResourceLocation(NostrumMagica.MODID, "textures/entity/dragonflightwing.png");
	protected final ModelDragonFlightWings<AbstractClientPlayerEntity> model = new ModelDragonFlightWings<>();
	protected final PlayerRenderer renderPlayer;
	
	public LayerDragonFlightWings(PlayerRenderer renderPlayerIn) {
		super(renderPlayerIn);
		this.renderPlayer = renderPlayerIn;
	}
	
	@Override
	public void render(AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (shouldRender(player)) {
			@Nonnull ItemStack chestpiece = player.getItemStackFromSlot(EquipmentSlotType.CHEST); 
			render(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, (!chestpiece.isEmpty() && chestpiece.isEnchanted()));
		}
	}
	
	public boolean shouldRender(AbstractClientPlayerEntity player) {
		for (ItemStack stack : player.getArmorInventoryList()) {
			if (!stack.isEmpty() && stack.getItem() instanceof IDragonWingRenderItem) {
				if (((IDragonWingRenderItem) stack.getItem()).shouldRenderDragonWings(stack, player)) {
					return true;
				}
			}
		}
		
		// Try bauables
		IInventory baubles = NostrumMagica.instance.curios.getCurios(player);
		
		if (baubles != null) {
			for (int i = 0; i < baubles.getSizeInventory(); i++) {
				ItemStack stack = baubles.getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof IDragonWingRenderItem) {
					if (((IDragonWingRenderItem) stack.getItem()).shouldRenderDragonWings(stack, player)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public int getColor(AbstractClientPlayerEntity player) {
		for (ItemStack stack : player.getArmorInventoryList()) {
			if (!stack.isEmpty() && stack.getItem() instanceof IDragonWingRenderItem) {
				if (((IDragonWingRenderItem) stack.getItem()).shouldRenderDragonWings(stack, player)) {
					return ((IDragonWingRenderItem) stack.getItem()).getDragonWingColor(stack, player);
				}
			}
		}
		
		// Try bauables
		IInventory baubles = NostrumMagica.instance.curios.getCurios(player);
		if (baubles != null) {
			for (int i = 0; i < baubles.getSizeInventory(); i++) {
				ItemStack stack = baubles.getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof IDragonWingRenderItem) {
					if (((IDragonWingRenderItem) stack.getItem()).shouldRenderDragonWings(stack, player)) {
						return ((IDragonWingRenderItem) stack.getItem()).getDragonWingColor(stack, player);
					}
				}
			}
		}
		
		return 0xFF000000;
	}
	
	public void render(AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean enchanted) {
		final int color = getColor(player);
		GlStateManager.color4f((float)((color >> 16) & 0xFF) / 255f,
				(float)((color >> 8) & 0xFF) / 255f,
				(float)((color >> 0) & 0xFF) / 255f,
					(float)((color >> 24) & 0xFF) / 255f);
		
		GlStateManager.disableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.enableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.disableTexture();
		GlStateManager.enableTexture();
		GlStateManager.enableLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableColorLogicOp();
		GlStateManager.enableColorMaterial();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

		this.renderPlayer.bindTexture(TEXTURE_WINGS);
		
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.0F, 0.0F, 0.125F);
		model.setRotationAngles(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//		if (enchanted) {
//			LayerArmorBase.renderEnchantedGlint(this.renderPlayer, player, model, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
//		}

		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
	
}
