package com.smanzana.nostrummagica.client.render.layer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.armor.ICapeProvider;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Vector3f;

public class LayerAetherCloak extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

	protected static final BakedModel GetModel(ResourceLocation model) {
		Minecraft mc = Minecraft.getInstance();
		return mc.getModelManager().getModel(model);
	}
	
	protected static final VertexConsumer GetBuffer(MultiBufferSource typeBuffer, @Nullable RenderType type) {
		if (type == null) {
			type = Sheets.translucentCullBlockSheet();
		}
		
		return typeBuffer.getBuffer(type);
	}
	
	//protected final PlayerRenderer renderPlayer;
	
	public LayerAetherCloak(PlayerRenderer renderPlayerIn) {
		super(renderPlayerIn);
		//this.renderPlayer = renderPlayerIn;
	}
	
	@Override
	public void render(PoseStack stack, MultiBufferSource typeBuffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		ItemStack capeItem = shouldRender(player);
		if (!capeItem.isEmpty()) {
			render(stack, typeBuffer, packedLight, player, capeItem, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
		}
	}
	
	public static @Nonnull ItemStack ShouldRender(LivingEntity player) {
		Iterable<ItemStack> equipment = player.getArmorSlots();
		for (ItemStack stack : equipment) {
			if (!stack.isEmpty() && stack.getItem() instanceof ICapeProvider) {
				if (((ICapeProvider) stack.getItem()).shouldRenderCape(player, stack)) {
					return stack;
				}
			}
		}
		
		// Nothing so far. Check baubles if there're there.
		if (player instanceof Player) {
			Container inventory = NostrumMagica.instance.curios.getCurios((Player) player);
			if (inventory != null) {
				for (int i = 0; i < inventory.getContainerSize(); i++) {
					ItemStack stack = inventory.getItem(i);
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
	
	public void render(PoseStack matrixStack, MultiBufferSource typeBuffer, int packedLight, AbstractClientPlayer player, ItemStack stack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
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
		final boolean isFlying = player.isFallFlying();
		final boolean hasChestpiece = (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty());
		final @Nullable RenderType[] renderTypes = provider.getCapeRenderTypes(player, stack);
		final ResourceLocation[] models = provider.getCapeModels(player, stack);
		if (renderTypes != null && renderTypes.length != models.length) {
			throw new RuntimeException("CapeProvider render types array must have same length as model array!");
		}
		
		// Get how 'forward' we're moving for cape rotation
		Vec3 look = player.getViewVector(ageInTicks % 1f);
		double motionForward = look
				.subtract(0, look.y, 0)
				.dot(new Vec3(player.getDeltaMovement().x, 0, player.getDeltaMovement().z));
		float rot = -10f;
		final float moveMaxRot = (!isFlying && motionForward > 0 ? -20f : 10f);
		//final double yVelOverride = .25;
		final float cloakAffectVelocity = limbSwingAmount;
				// Imagine your cape moving realisitically depending on if you were going up or down 
				//(entityIn.getMotion().y < -yVelOverride ? -1f : (entityIn.getMotion().y > yVelOverride ? 1f : limbSwingAmount));
		rot += (cloakAffectVelocity * moveMaxRot); // Add amount for how fast we're moving
		if (player.isShiftKeyDown()) {
			rot -= 30;
		}
		
		matrixStack.pushPose();
		matrixStack.translate(0.0F, 0.0F, 0.125F);
		matrixStack.scale(-objScale, -objScale, objScale);
		matrixStack.translate(0, player.isShiftKeyDown() ? -.3 : 0, hasChestpiece ? .15 : 0);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(rot));
		
		int index = 0;
		for (ResourceLocation model : models) {
			final BakedModel bakedModel = GetModel(model);
			final VertexConsumer buffer = GetBuffer(typeBuffer, renderTypes == null ? null : renderTypes[index]);
			
			matrixStack.pushPose();
			
			final int color = provider.getColor(player, stack, index);
			
			if (provider != null) {
				provider.preRender(player, index, stack, matrixStack, netHeadYaw, ageInTicks % 1f);
			}
			
			renderCapeModel(player, provider, stack, bakedModel, matrixStack, buffer, packedLight, color);
			
			matrixStack.popPose();
			index++;
		}
		//model.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		matrixStack.popPose();
	}
	
	protected void renderCapeModel(AbstractClientPlayer living, ICapeProvider provider, ItemStack stack, BakedModel model,
			PoseStack matrixStack, VertexConsumer bufferIn, int packedLightIn, int color) {
		RenderFuncs.RenderModelWithColor(matrixStack, bufferIn, model, color, packedLightIn, OverlayTexture.NO_OVERLAY);
	}
	
}
