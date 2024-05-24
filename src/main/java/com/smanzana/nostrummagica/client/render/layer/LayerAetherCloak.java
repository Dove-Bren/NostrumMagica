package com.smanzana.nostrummagica.client.render.layer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.armor.ICapeProvider;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class LayerAetherCloak extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

	protected static final IBakedModel GetModel(ResourceLocation model) {
		Minecraft mc = Minecraft.getInstance();
		return mc.getModelManager().getModel(model);
	}
	
	protected static final IVertexBuilder GetBuffer(IRenderTypeBuffer typeBuffer, @Nullable RenderType type) {
		if (type == null) {
			type = Atlases.getTranslucentCullBlockType();
		}
		
		return typeBuffer.getBuffer(type);
	}
	
	//protected final PlayerRenderer renderPlayer;
	
	public LayerAetherCloak(PlayerRenderer renderPlayerIn) {
		super(renderPlayerIn);
		//this.renderPlayer = renderPlayerIn;
	}
	
	@Override
	public void render(MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		ItemStack capeItem = shouldRender(player);
		if (!capeItem.isEmpty()) {
			render(stack, typeBuffer, packedLight, player, capeItem, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
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
	
	public void render(MatrixStack matrixStack, IRenderTypeBuffer typeBuffer, int packedLight, AbstractClientPlayerEntity player, ItemStack stack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		final ICapeProvider provider = ((ICapeProvider)stack.getItem());
		
		// Could dim as it gets less aether? Other effects?
		
//		GlStateManager.disableBlend();
//		GlStateManager.disableAlphaTest();
//		GlStateManager.enableBlend();
//		GlStateManager.enableAlphaTest();
//		GlStateManager.disableTexture();
//		GlStateManager.enableTexture();
//		GlStateManager.enableLighting();
//		GlStateManager.disableLighting();
//		GlStateManager.disableColorLogicOp();
//		GlStateManager.enableColorMaterial();
//		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		
		final float objScale = .425f;
		final boolean isFlying = player.isElytraFlying();
		final boolean hasChestpiece = (!player.getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty());
		final @Nullable RenderType[] renderTypes = provider.getCapeRenderTypes(player, stack);
		final ResourceLocation[] models = provider.getCapeModels(player, stack);
		if (renderTypes != null && renderTypes.length != models.length) {
			throw new RuntimeException("CapeProvider render types array must have same length as model array!");
		}
		
		// Get how 'forward' we're moving for cape rotation
		Vector3d look = player.getLook(ageInTicks % 1f);
		double motionForward = look
				.subtract(0, look.y, 0)
				.dotProduct(new Vector3d(player.getMotion().x, 0, player.getMotion().z));
		float rot = -10f;
		final float moveMaxRot = (!isFlying && motionForward > 0 ? -20f : 10f);
		//final double yVelOverride = .25;
		final float cloakAffectVelocity = limbSwingAmount;
				// Imagine your cape moving realisitically depending on if you were going up or down 
				//(entityIn.getMotion().y < -yVelOverride ? -1f : (entityIn.getMotion().y > yVelOverride ? 1f : limbSwingAmount));
		rot += (cloakAffectVelocity * moveMaxRot); // Add amount for how fast we're moving
		if (player.isSneaking()) {
			rot -= 30;
		}
		
		matrixStack.push();
		matrixStack.translate(0.0F, 0.0F, 0.125F);
		matrixStack.scale(-objScale, -objScale, objScale);
		matrixStack.translate(0, player.isSneaking() ? -.3 : 0, hasChestpiece ? .15 : 0);
		matrixStack.rotate(Vector3f.XP.rotationDegrees(rot));
		
		int index = 0;
		for (ResourceLocation model : models) {
			final IBakedModel bakedModel = GetModel(model);
			final IVertexBuilder buffer = GetBuffer(typeBuffer, renderTypes == null ? null : renderTypes[index]);
			
			matrixStack.push();
			
			final int color = provider.getColor(player, stack, index);
			
			if (provider != null) {
				provider.preRender(player, index, stack, matrixStack, netHeadYaw, ageInTicks % 1f);
			}
			
			renderCapeModel(player, provider, stack, bakedModel, matrixStack, buffer, packedLight, color);
			
			matrixStack.pop();
			index++;
		}
		//model.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		matrixStack.pop();
	}
	
	protected void renderCapeModel(AbstractClientPlayerEntity living, ICapeProvider provider, ItemStack stack, IBakedModel model,
			MatrixStack matrixStack, IVertexBuilder bufferIn, int packedLightIn, int color) {
		RenderFuncs.RenderModelWithColor(matrixStack, bufferIn, model, color, packedLightIn, OverlayTexture.NO_OVERLAY);
	}
	
}
