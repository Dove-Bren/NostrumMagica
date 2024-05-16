package com.smanzana.nostrummagica.client.render.layer;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelDragonFlightWings;
import com.smanzana.nostrummagica.items.IDragonWingRenderItem;
import com.smanzana.nostrummagica.utils.ColorUtil;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class LayerDragonFlightWings extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

	protected static final ResourceLocation TEXTURE_WINGS = new ResourceLocation(NostrumMagica.MODID, "textures/entity/dragonflightwing.png");
	protected final ModelDragonFlightWings<AbstractClientPlayerEntity> model = new ModelDragonFlightWings<>();
	protected final PlayerRenderer renderPlayer;
	
	public LayerDragonFlightWings(PlayerRenderer renderPlayerIn) {
		super(renderPlayerIn);
		this.renderPlayer = renderPlayerIn;
	}
	
	@Override
	public void render(MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (shouldRender(player)) {
			@Nonnull ItemStack chestpiece = player.getItemStackFromSlot(EquipmentSlotType.CHEST); 
			render(stack, typeBuffer, packedLight, player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, (!chestpiece.isEmpty() && chestpiece.isEnchanted()));
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
	
	public void render(MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, boolean enchanted) {
		
		final float colors[] = ColorUtil.ARGBToColor(getColor(player));
		final IVertexBuilder buffer = typeBuffer.getBuffer(RenderType.getEntitySolid(TEXTURE_WINGS));
		
		stack.push();
		stack.rotate(Vector3f.XP.rotationDegrees(player.isSneaking() ? 25f : 0));
		stack.translate(0, 0, .125f);
		stack.translate(0, player.getHeight() * .3f, player.getWidth() * .3f);
		stack.translate(0, player.isSneaking() ? .3 : 0, 0); // This is kind tear-y but things like elytra sore 'last' on the entity to smooth!
		model.setRotationAngles(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		model.render(stack, buffer, packedLight, OverlayTexture.NO_OVERLAY, colors[0], colors[1], colors[2], colors[3]);
		
//		if (enchanted) {
//		LayerArmorBase.renderEnchantedGlint(this.renderPlayer, player, model, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
//	}
		
		stack.pop();
	}
}
