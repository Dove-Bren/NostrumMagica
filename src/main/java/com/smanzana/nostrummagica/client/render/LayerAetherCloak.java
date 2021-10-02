package com.smanzana.nostrummagica.client.render;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.renderer.ModelAetherCloak;
import com.smanzana.nostrummagica.items.ICapeProvider;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class LayerAetherCloak implements LayerRenderer<AbstractClientPlayer> {

	protected static final Map<Integer, ModelAetherCloak> ModelCache = new HashMap<>();
	
	protected static final ModelAetherCloak GetModel(ResourceLocation models[]) {
		int hash = Arrays.deepHashCode(models);
		ModelAetherCloak cloak = ModelCache.get(hash);
		if (cloak == null) {
			IBakedModel[] bakedModels = new IBakedModel[models.length];
			int i = 0;
			for (ResourceLocation modelLoc : models) {
				IModel rawModel = ModelLoaderRegistry.getModelOrLogError(modelLoc, "Nostrum Magica is missing a model. Please report this to the mod authors.");
				bakedModels[i++] = rawModel.bake(rawModel.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
			}
			
			cloak = new ModelAetherCloak(bakedModels, 64, 64); // TODO make texture size configurable
			ModelCache.put(hash, cloak);
		}
		return cloak;
	}
	
	protected final RenderPlayer renderPlayer;
	
	public LayerAetherCloak(RenderPlayer renderPlayerIn) {
		//super(renderPlayerIn);
		this.renderPlayer = renderPlayerIn;
	}
	
	@Override
	public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack capeItem = shouldRender(player);
		if (!capeItem.isEmpty()) {
			render(player, capeItem, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		}
	}
	
	public static @Nonnull ItemStack ShouldRender(EntityLivingBase player) {
		Iterable<ItemStack> equipment = player.getArmorInventoryList();
		for (ItemStack stack : equipment) {
			if (!stack.isEmpty() && stack.getItem() instanceof ICapeProvider) {
				if (((ICapeProvider) stack.getItem()).shouldRenderCape(player, stack)) {
					return stack;
				}
			}
		}
		
		// Nothing so far. Check baubles if there're there.
		if (player instanceof EntityPlayer) {
			IInventory inventory = NostrumMagica.baubles.getBaubles((EntityPlayer) player);
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
	
	public @Nonnull ItemStack shouldRender(EntityLivingBase player) {
		return ShouldRender(player);
	}
	
	public void render(AbstractClientPlayer player, ItemStack stack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		final ICapeProvider provider = ((ICapeProvider)stack.getItem());
		final ModelAetherCloak model = GetModel(provider.getCapeModels(player, stack));
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
		
		// Could dim as it gets less aether? Other effects?
		
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

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 0.125F);
		model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
		model.renderEx(player, provider, stack, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
	
}
