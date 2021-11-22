package com.smanzana.nostrummagica.client.render;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.renderer.ModelDragonFlightWings;
import com.smanzana.nostrummagica.items.IDragonWingRenderItem;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class LayerDragonFlightWings implements LayerRenderer<AbstractClientPlayer> {

	protected static final ResourceLocation TEXTURE_WINGS = new ResourceLocation(NostrumMagica.MODID, "textures/entity/dragonflightwing.png");
	protected final ModelDragonFlightWings model = new ModelDragonFlightWings();
	protected final RenderPlayer renderPlayer;
	
	public LayerDragonFlightWings(RenderPlayer renderPlayerIn) {
		//super(renderPlayerIn);
		this.renderPlayer = renderPlayerIn;
	}
	
	@Override
	public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (shouldRender(player)) {
			@Nonnull ItemStack chestpiece = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST); 
			render(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, (!chestpiece.isEmpty() && chestpiece.isItemEnchanted()));
		}
	}
	
	public boolean shouldRender(AbstractClientPlayer player) {
		for (ItemStack stack : player.getArmorInventoryList()) {
			if (!stack.isEmpty() && stack.getItem() instanceof IDragonWingRenderItem) {
				if (((IDragonWingRenderItem) stack.getItem()).shouldRenderDragonWings(stack, player)) {
					return true;
				}
			}
		}
		
		// Try bauables
		IInventory baubles = NostrumMagica.baubles.getBaubles(player);
		
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
	
	public int getColor(AbstractClientPlayer player) {
		for (ItemStack stack : player.getArmorInventoryList()) {
			if (!stack.isEmpty() && stack.getItem() instanceof IDragonWingRenderItem) {
				if (((IDragonWingRenderItem) stack.getItem()).shouldRenderDragonWings(stack, player)) {
					return ((IDragonWingRenderItem) stack.getItem()).getDragonWingColor(stack, player);
				}
			}
		}
		
		// Try bauables
		IInventory baubles = NostrumMagica.baubles.getBaubles(player);
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
	
	public void render(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean enchanted) {
		final int color = getColor(player);
		GlStateManager.color((float)((color >> 16) & 0xFF) / 255f,
				(float)((color >> 8) & 0xFF) / 255f,
				(float)((color >> 0) & 0xFF) / 255f,
					(float)((color >> 24) & 0xFF) / 255f);
		
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

		this.renderPlayer.bindTexture(TEXTURE_WINGS);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 0.125F);
		model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
		model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
