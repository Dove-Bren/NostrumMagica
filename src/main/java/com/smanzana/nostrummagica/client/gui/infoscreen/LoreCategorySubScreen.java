package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL31;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.commonwidget.GridWidget;
import com.smanzana.nostrummagica.client.gui.commonwidget.ObscurableChildWidget;
import com.smanzana.nostrummagica.client.gui.commonwidget.Tooltip;
import com.smanzana.nostrummagica.client.render.CustomFilledColorTypeBuffer;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.IBlockLoreTagged;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.IItemLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.Color;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;

public class LoreCategorySubScreen implements IInfoSubScreen {

	private final ELoreCategory category;
	
	private int loreCount;
	private int loreMax;
	
	public LoreCategorySubScreen(ELoreCategory category) {
		this.category = category;
	}
	
	@Override
	public void draw(INostrumMagic attr, Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
		final int margin = 10;
		RenderFuncs.drawRect(matrixStackIn, x + margin, y + margin, x + width - margin, y + height - margin, 0xFF446677);
		
		Component title = category.getTitle();
		int len = mc.font.width(title);
		mc.font.drawShadow(matrixStackIn, title, x + (width / 2) + (-len / 2), y + margin + 4, 0xFFFFFFFF);
		
		final String progressString = "%d / %d (%.0f%%)".formatted(loreCount, loreMax, loreCount == 0 || loreMax == 0 ? 0f : 100 * (loreCount / (float) loreMax));
		len = mc.font.width(progressString);
		mc.font.drawShadow(matrixStackIn, progressString, x + (width / 2) + (-len / 2), y + height - (margin + 4 + mc.font.lineHeight), 0xFFAAAAAA);
	}

	@Override
	public Collection<AbstractWidget> getWidgets(INostrumMagic attr, int x, int y, int width, int height) {
		final int margin = 10;
		
		GridWidget<LoreIconWidget> grid = new GridWidget<>(x, y + 8 + margin, width - 16, height - (8 + margin), TextComponent.EMPTY);
		grid.setMargin(12);
		grid.setSpacing(8);
		
		for (ILoreTagged tag : LoreRegistry.instance().allLore()) {
			if (tag.getCategory() != this.category) {
				continue;
			}
			
			@Nullable Lore lore = attr.getLore(tag);
			grid.addChild(new LoreIconWidget(0, 0, 16, 16, tag, lore));
			
			loreMax++;
			if (lore != null) {
				loreCount++;
			}
		}
		
		return List.of(grid);
	}
	
	@Override
	public void drawForeground(INostrumMagic attr, Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
		
	}
	
	private static final class LoreIconWidget extends ObscurableChildWidget<LoreIconWidget> {
		
		private final ILoreTagged tag;
		private final @Nullable Lore lore;
		
		private ItemStack iconStack = ItemStack.EMPTY;
		private Entity iconEntity = null;
		private FluidState iconFluid = null;
		
		public LoreIconWidget(int x, int y, int width, int height, ILoreTagged tag, @Nullable Lore lore) {
			super(x, y, width, height, new TextComponent(tag.getLoreDisplayName()));
			this.tag = tag;
			this.lore = lore;
			
			if (lore != null) {
				final List<Component> lines = lore.getData().stream().map(s -> (Component) new TextComponent(s)).collect(Collectors.toCollection(ArrayList::new));
				for (int i = lines.size()-1; i > 0; i--) {
					lines.add(i, new TextComponent(" "));
				}
				lines.add(0, new TextComponent(tag.getLoreDisplayName()).withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE));
				this.tooltip(Tooltip.create(lines));
			}
			
			final Minecraft mc = Minecraft.getInstance();
			if (tag instanceof IItemLoreTagged itemTag) {
				iconStack = itemTag.makeStack();
			} else if (tag instanceof Item item) {
				if (item instanceof SpellRune) {
					iconStack = SpellRune.getRune(EMagicElement.FIRE);
				} else {
					iconStack = new ItemStack(item, 1);
				}
			} else if (tag instanceof Block block) {
				Item item = block.asItem();
				if (item != null && item != Items.AIR) {
					iconStack = new ItemStack(item, 1);
				} else {
					NostrumMagica.logger.warn("Couldn't get icon for " + block);
				}
			} else if (tag instanceof IEntityLoreTagged entTag) {
				iconEntity = entTag.makeEntity(mc.level);
				this.width = (int) (iconEntity.getBbWidth() * 32f);
				this.height = (int) (iconEntity.getBbHeight() * 24f);
			} else if (tag instanceof IBlockLoreTagged blockTag) {
				Item item = blockTag.getBlock().asItem();
				if (item != null && item != Items.AIR) {
					iconStack = new ItemStack(item, 1);
				} else if (!blockTag.getBlock().defaultBlockState().getFluidState().isEmpty()) {
					iconFluid = blockTag.getBlock().defaultBlockState().getFluidState();
				} else {
					NostrumMagica.logger.warn("Couldn't get icon for " + blockTag.getBlock());
				}
			}
			
			if (this.tag != null) {
				
			}
		}
		
		protected void drawLore(ItemStack iconStack, PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks, boolean background) {
				//int x = this.x + width / 2;
				//int y = this.y + height / 2;
				RenderFuncs.RenderGUIItem(iconStack, matrixStackIn);
				//Minecraft.getInstance().getItemRenderer().renderGuiItem(iconStack, 0, 0);
				
				if (background) {
					RenderSystem.depthFunc(GL31.GL_GEQUAL);
					RenderFuncs.drawRect(matrixStackIn, 0, 0, width, height, 0xFF101810);
					RenderSystem.depthFunc(GL31.GL_LEQUAL);
				}
		}
		
		protected void drawLore(LivingEntity iconEntity, PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks, boolean background) {
//				// Setup mask
//				{
//					RenderSystem.enableDepthTest(); // make sure this is on
//					RenderSystem.depthMask(true);
//					
//					matrixStackIn.pushPose();
//					matrixStackIn.translate(0, 0, 950); // 950 copied from achv tab vanilla renderer
//					
//					// Set the max depth up to a really high value to prevent things from rendering under it
//					RenderSystem.colorMask(false, false, false, false); // only write to depth
//					Screen.fill(matrixStackIn, 4680, 2260, -4680, -2260, 0xFF000000); // Magic width/height is from vanilla. Is that 8k?
//					
//					// Slice out the section we want to render on to allow things to render
//					matrixStackIn.translate(0, 0, -950);
//					RenderSystem.depthFunc(GL32.GL_GEQUAL); // Only render to spots with a depth already set to higher AKA our giant mask
//					Screen.fill(matrixStackIn, x, y, width, height, 0xFF000000); // "Render" no color at this depth, effectively setting the max depth drawn back to 0
//					RenderSystem.depthFunc(GL32.GL_LEQUAL); // Restore to standard 'only draw on top of things' behavior for rest of render
//					RenderSystem.colorMask(true, true, true, true); // write color again
//					
//					matrixStackIn.popPose();
//				}
			
			int x = (width / 2);
			float y = (3f/4f) * height;
			PoseStack realStack = RenderSystem.getModelViewStack();
			realStack.pushPose();
			realStack.mulPoseMatrix(matrixStackIn.last().pose());
			realStack.translate(x, y, 0); // entities grow up
			if (background) {
				//realStack.translate(0, 0, -100);
			}
			
//				if (tag != ArcaneWolfEntity.WolfTameLore.instance()) {
//					realStack.translate(0, 0, 1050); // going to reverse the add in RenderEntityInGUI with this z scale...
//					realStack.scale(1f, 1f, -1f);
//				} else {
//					
//				}
			RenderSystem.applyModelViewMatrix();
			
			if (background) {
					RenderFuncs.RenderEntityInGUI(0, 0,
							12, (float)(this.x + x) - mouseX, (float)(this.y) - mouseY, (LivingEntity)iconEntity,
						CustomFilledColorTypeBuffer.Instance().color(0f, 0f, 0f, 1f), (b) -> b.finish());
						//CustomFilledColorTypeBuffer.Instance().finish();
//					Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
//				InventoryScreen.renderEntityInInventory(0, 0,
//						(int) (width * .4), (float)(this.x) - mouseX, (float)(this.y) - mouseY, (LivingEntity)iconEntity
//						);
			} else {
//				InventoryScreen.renderEntityInInventory(0, 0,
//						(int) (width * .4), (float)(this.x) - mouseX, (float)(this.y) - mouseY, (LivingEntity)iconEntity
//						);
					RenderFuncs.RenderEntityInGUI(0, 0,
							12, (float)(this.x + x) - mouseX, (float)(this.y) - mouseY, (LivingEntity)iconEntity,
							CustomFilledColorTypeBuffer.Instance().color(1f, 1f, 1f, 1f), (b) -> b.finish());
//					Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
			}
			
			realStack = RenderSystem.getModelViewStack();
			realStack.popPose();
			RenderSystem.applyModelViewMatrix();
			
//				// Clear out mask
//				{
//					RenderSystem.enableDepthTest(); // make sure this is on
//					RenderSystem.depthMask(true);
//					matrixStackIn.pushPose();
//					matrixStackIn.translate(0, 0, 0);
//					RenderSystem.colorMask(false, false, false, false); // only write to depth
//					RenderSystem.depthFunc(GL32.GL_GEQUAL);
//					Screen.fill(matrixStackIn, 4680, 2260, -4680, -2260, 0xFF000000); // Magic width/height is from vanilla. Is that 8k?
//					RenderSystem.colorMask(true, true, true, true);
//					RenderSystem.depthFunc(GL32.GL_LEQUAL);
//					matrixStackIn.popPose();
//				}
		}
		
		protected void drawLore(FluidState state, PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks, boolean background) {
			
			final Color color = background ? new Color(0xFF101810) : new Color(1f, 1f, 1f, 1f);
			
			//int x = this.x + width / 2;
			//int y = this.y + height / 2;
			matrixStackIn.pushPose();
			matrixStackIn.translate(width/2, height/2, 0);
			matrixStackIn.scale(12, 12, 12);
			matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(25));
			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(45));
			
			RenderFuncs.renderFluidState(state, matrixStackIn, Minecraft.getInstance().renderBuffers().bufferSource(), RenderFuncs.BrightPackedLight, OverlayTexture.NO_OVERLAY, color.red, color.green, color.blue, color.alpha);
			Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
			//Minecraft.getInstance().getItemRenderer().renderGuiItem(iconStack, 0, 0);
			matrixStackIn.popPose();
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			if (!this.iconStack.isEmpty()) {
				final float outlineScale = 1.125f; // works nicely with size 16
				
				matrixStackIn.pushPose();
				matrixStackIn.translate(x, y, 0);
				
				matrixStackIn.pushPose();
				matrixStackIn.scale(outlineScale, outlineScale, 1f);
				drawLore(iconStack, matrixStackIn, mouseX, mouseY, partialTicks, true);
				matrixStackIn.popPose();
				if (lore != null) {
					// figure out how many pixels 1.1 of our width is...
					final int offset = (int) (this.width * (outlineScale - 1f)) / 2;
					matrixStackIn.translate(offset, offset, 0);
					drawLore(iconStack, matrixStackIn, mouseX, mouseY, partialTicks, false);
				}
				matrixStackIn.popPose();
			} else if (this.iconEntity != null) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(x, y, 0);
				
				drawLore((LivingEntity) iconEntity, matrixStackIn, mouseX, mouseY, partialTicks, lore == null);
				
				matrixStackIn.popPose();
			} else if (this.iconFluid != null) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(x, y, 0);
				
				drawLore(iconFluid, matrixStackIn, mouseX, mouseY, partialTicks, lore == null);
				
				matrixStackIn.popPose();
			}
		}
		
	}

}
