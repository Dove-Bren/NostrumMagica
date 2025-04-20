package com.smanzana.nostrummagica.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.item.ISelectionItem;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SelectionRenderer {
	
	public SelectionRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onRender(RenderLevelStageEvent event) {
		if (event.getStage() != Stage.AFTER_TRIPWIRE_BLOCKS) {
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		final PoseStack matrixStackIn = event.getPoseStack();
		
		final ItemStack selectionStack = this.findStackToRender(player);
		if (!selectionStack.isEmpty()) {
			final ISelectionItem provider = (ISelectionItem) selectionStack.getItem();
			final BlockPos anchor = provider.getAnchor(player, selectionStack);
			final BlockPos freePos = provider.getBoundingPos(player, selectionStack);
			
			if (anchor != null) {
				
				double minDist = player.distanceToSqr(anchor.getX(), anchor.getY(), anchor.getZ());
				if (minDist >= 5096 && freePos != null) {
					minDist = player.distanceToSqr(freePos.getX(), freePos.getY(), freePos.getZ());
				}
				
				if (minDist < 5096) {
					final MultiBufferSource.BufferSource bufferIn = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
					bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_QUADS);
					if (freePos != null) {
						final VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.WORLD_SELECT_HIGHLIGHT);
						renderSelectionBox(
								matrixStackIn, buffer,
								new BlockPos(Math.min(anchor.getX(), freePos.getX()),
										Math.min(anchor.getY(), freePos.getY()),
										Math.min(anchor.getZ(), freePos.getZ())),
								
								new BlockPos(Math.max(anchor.getX(), freePos.getX()),
										Math.max(anchor.getY(), freePos.getY()),
										Math.max(anchor.getZ(), freePos.getZ())),
								
								event.getPartialTick(),
								provider.isSelectionValid(player, selectionStack));
					}
					
					// Render anchor anchor block special
					final VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.WORLD_SELECT_HIGHLIGHT);
					renderAnchorBlock(matrixStackIn, buffer, anchor, event.getPartialTick());
					bufferIn.endBatch();
				}
			}
		}
	}
	
	protected ItemStack findStackToRender(Player player) {
		ItemStack ret = ItemStack.EMPTY;
		for (ItemStack held : player.getHandSlots()) {
			if (!held.isEmpty() && held.getItem() instanceof ISelectionItem) {
				ISelectionItem holder = (ISelectionItem) held.getItem();
				if (holder.shouldRenderSelection(player, held)) {
					ret = held;
					break;
				}
			}
		}
		return ret;
	}
	
	private void renderAnchorBlock(PoseStack matrixStackIn, VertexConsumer buffer, BlockPos pos, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		Vec3 playerPos = mc.gameRenderer.getMainCamera().getPosition();//player.getEyePosition(partialTicks).subtract(0, player.getEyeHeight(), 0);
		Vec3 offset = new Vec3(pos.getX() - playerPos.x,
				pos.getY() - playerPos.y,
				pos.getZ() - playerPos.z);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(offset.x + .5, offset.y + .5, offset.z + .5);
		matrixStackIn.scale(1.011f, 1.011f, 1.011f);
		RenderFuncs.drawUnitCube(matrixStackIn, buffer, 0, OverlayTexture.NO_OVERLAY, .4f, .7f, 1f, .4f);
		matrixStackIn.popPose();
	}
	
	private void renderSelectionBox(PoseStack matrixStackIn, VertexConsumer buffer, BlockPos min, BlockPos max, float partialTicks, boolean valid) {
		Minecraft mc = Minecraft.getInstance();
		Vec3 playerPos = mc.gameRenderer.getMainCamera().getPosition();//player.getEyePosition(partialTicks).subtract(0, player.getEyeHeight(), 0);
		Vec3 offset = new Vec3(min.getX() - playerPos.x,
				min.getY() - playerPos.y,
				min.getZ() - playerPos.z);
		
		matrixStackIn.pushPose();
		
//		// Disable cull if inside
//		if (playerPos.x > min.getX() && playerPos.y > min.getY() && playerPos.z > min.getZ()
//				&& playerPos.x < max.getX() && playerPos.y < max.getY() && playerPos.z < max.getZ()) {
//			GlStateManager.disableCull();
//		}
		
		final float red, green, blue, alpha;
		if (valid) { // If good
			red = .4f;
			green = .9f;
			blue = .4f;
			alpha = .3f;
		} else {
			red = .9f;
			green = .4f;
			blue = .4f;
			alpha = .3f;
		}
		
		// TODO apply partial tick offset to effects too! lol!
		
		final float widthX = (max.getX() - min.getX()) + 1;
		final float widthY = (max.getY() - min.getY()) + 1;
		final float widthZ = (max.getZ() - min.getZ()) + 1;
		matrixStackIn.translate(offset.x + (widthX / 2), offset.y + (widthY / 2), offset.z + (widthZ / 2));
		matrixStackIn.scale(widthX, widthY, widthZ);
		matrixStackIn.scale(1.0001f, 1.0001f, 1.0001f);
		RenderFuncs.drawUnitCube(matrixStackIn, buffer, 0, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
		matrixStackIn.popPose();
	}
}
