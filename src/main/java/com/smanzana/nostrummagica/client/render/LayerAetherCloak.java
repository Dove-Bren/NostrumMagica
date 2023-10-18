package com.smanzana.nostrummagica.client.render;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.entity.ModelAetherCloak;
import com.smanzana.nostrummagica.items.ICapeProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class LayerAetherCloak extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

	protected static final Map<Integer, ModelAetherCloak<AbstractClientPlayerEntity>> ModelCache = new HashMap<>();
	
	protected static final ModelAetherCloak<AbstractClientPlayerEntity> GetModel(ModelResourceLocation models[]) {
		int hash = Arrays.deepHashCode(models);
		ModelAetherCloak<AbstractClientPlayerEntity> cloak = ModelCache.get(hash);
		if (cloak == null) {
			IBakedModel[] bakedModels = new IBakedModel[models.length];
			int i = 0;
			for (ModelResourceLocation modelLoc : models) {
				Minecraft mc = Minecraft.getInstance();
				bakedModels[i++] = mc.getModelManager().getModel(modelLoc);
			}
			
			cloak = new ModelAetherCloak<>(bakedModels, 64, 64); // TODO make texture size configurable
			ModelCache.put(hash, cloak);
		}
		return cloak;
	}
	
	//protected final PlayerRenderer renderPlayer;
	
	public LayerAetherCloak(PlayerRenderer renderPlayerIn) {
		super(renderPlayerIn);
		//this.renderPlayer = renderPlayerIn;
	}
	
	@Override
	public void render(AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack capeItem = shouldRender(player);
		if (!capeItem.isEmpty()) {
			render(player, capeItem, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		}
	}
	
	public static @Nonnull ItemStack ShouldRender(LivingEntity player) {
		Iterable<ItemStack> equipment = player.getArmorInventoryList();
		for (ItemStack stack : equipment) {
			if (!stack.isEmpty() && stack.getItem() instanceof ICapeProvider) {
				if (((ICapeProvider) stack.getItem()).shouldRenderCape(player, stack)) {
					return stack;
				}
			}
		}
		
		// Nothing so far. Check baubles if there're there.
		if (player instanceof PlayerEntity) {
			IInventory inventory = NostrumMagica.instance.curios.getCurios((PlayerEntity) player);
			if (inventory != null) {
				for (int i = 0; i < inventory.getSizeInventory(); i++) {
					ItemStack stack = inventory.getStackInSlot(i);
					if (!stack.isEmpty() && stack.getItem() instanceof ICapeProvider) {
						if (((ICapeProvider) stack.getItem()).shouldRenderCape(player, stack)) {
							return stack;
						}
					}
				}
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	public @Nonnull ItemStack shouldRender(LivingEntity player) {
		return ShouldRender(player);
	}
	
	public void render(AbstractClientPlayerEntity player, ItemStack stack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		final ICapeProvider provider = ((ICapeProvider)stack.getItem());
		final ModelAetherCloak<AbstractClientPlayerEntity> model = GetModel(provider.getCapeModels(player, stack));
		
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.9F);
		
		// Could dim as it gets less aether? Other effects?
		
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

		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.0F, 0.0F, 0.125F);
		model.setRotationAngles(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		model.renderEx(player, provider, stack, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
	
}
