package com.smanzana.nostrummagica.client.render;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.renderer.ModelDragonFlightWings;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
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
			@Nullable ItemStack chestpiece = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST); 
			render(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, (chestpiece != null && chestpiece.isItemEnchanted()));
		}
	}
	
	public boolean shouldRender(AbstractClientPlayer player) {
		// Maybe should have an interface?
		return (
				EnchantedArmor.GetSetCount(player, EMagicElement.PHYSICAL, 3) == 4
				|| EnchantedArmor.GetSetCount(player, EMagicElement.EARTH, 3) == 4
				|| EnchantedArmor.GetSetCount(player, EMagicElement.FIRE, 3) == 4
				|| EnchantedArmor.GetSetCount(player, EMagicElement.ENDER, 3) == 4
				);
	}
	
	public void render(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean enchanted) {
		ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
		if (stack != null && stack.getItem() instanceof EnchantedArmor) {
			final int color = ((EnchantedArmor) stack.getItem()).getElement().getColor(); //ARBG
			GlStateManager.color((float)((color >> 16) & 0xFF) / 255f,
					(float)((color >> 8) & 0xFF) / 255f,
					(float)((color >> 0) & 0xFF) / 255f,
					(float)((color >> 24) & 0xFF) / 255f);
		} else {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
		
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
