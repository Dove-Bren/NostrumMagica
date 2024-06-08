package com.smanzana.nostrummagica.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.OutlineRenderer.Outline;
import com.smanzana.nostrummagica.item.ISpellContainerItem;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.util.Curves.ICurve3d;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpellShapeRenderer {
	
	public static final ResourceLocation TEXTURE_BLOCK = NostrumMagica.Loc("textures/models/spellshape.png");
	public static final ResourceLocation TEXTURE_FLOW = NostrumMagica.Loc("textures/models/spellshape_flow.png");
	
	protected static final Outline DEFAULT_OUTLINE = new Outline(1f, 0f, 1f, 1f);
	
	private final OutlineRenderer outliner;
	private final List<Entity> outlinedEntities;
	private boolean enabled;

	public SpellShapeRenderer(OutlineRenderer outliner) {
		MinecraftForge.EVENT_BUS.register(this);
		this.outliner = outliner;
		this.outlinedEntities = new ArrayList<>();
		enabled = false;
	}
	
	public void toggle() {
		this.enabled = !enabled;
		if (!this.enabled) {
			clearOutlines();
		}
		
		else { // TESTING CODE: move to ClientInit#registerSpellShapeRenderers
			
			SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.BLOCKPOS, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
				final BlockPos pos = comp.getPos();
				
				final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_QUADS);
				final int combinedLight = 15728880;
				final int combinedOverlay = OverlayTexture.NO_OVERLAY;
				
				matrixStackIn.push();
				matrixStackIn.translate(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
				RenderFuncs.drawUnitCube(matrixStackIn, buffer, combinedLight, combinedOverlay, red, green, blue, alpha);
				matrixStackIn.pop();
			});

			SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.ENTITY, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
				final Entity ent = comp.getEntity();
				this.addEntity(ent, DEFAULT_OUTLINE);
			});

			SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.LINE, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
				final Vector3d start = comp.getStart();
				final Vector3d end = comp.getEnd();
				final int combinedLight = 15728880;
				final int combinedOverlay = OverlayTexture.NO_OVERLAY;
				
				final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_LINES);
				RenderFuncs.renderLine(matrixStackIn, buffer, start, end, 10, combinedOverlay, combinedLight, red, green, blue, alpha);
			});

			SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.AOE_LINE, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
				final Vector3d start = comp.getStart();
				final Vector3d end = comp.getEnd();
				final float width = comp.getWidth();
				final int combinedLight = 15728880;
				final int combinedOverlay = OverlayTexture.NO_OVERLAY;
				
				final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_LINES);
				RenderFuncs.renderLine(matrixStackIn, buffer, start, end, width, combinedOverlay, combinedLight, red, green, blue, alpha);
			});

			SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.CURVE, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
				final Vector3d start = comp.getStart();
				final ICurve3d curve = comp.getCurve();
				final int combinedLight = 0;
				final int combinedOverlay = OverlayTexture.NO_OVERLAY;
				
//				final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_LINES);
//				RenderFuncs.renderCurve(matrixStackIn, buffer, start, curve, 50, combinedOverlay, combinedLight, red, green, blue, alpha);
				
				final float prog = (float) (System.currentTimeMillis() % 500) / 500f;
				
				final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_ORB_CHAIN);
				RenderFuncs.renderRibbon(matrixStackIn, buffer, start, curve, 50, .2f, prog, combinedOverlay, combinedLight, red, green, blue, alpha);
			});
		}
	}
	
	protected void addEntity(Entity ent, Outline outline) {
		this.outlinedEntities.add(ent);
		outliner.add(ent, outline);
	}
	
	protected void clearOutlines() {
		for (Entity ent : outlinedEntities) {
			outliner.remove(ent);
		}
		outlinedEntities.clear();
	}
	
	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		if (!enabled) {
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		MatrixStack matrixStackIn = event.getMatrixStack();
		final float partialTicks = event.getPartialTicks();
		
		// Figure out if we should show a preview
		SpellShapePreview preview = null;
		
		clearOutlines();
		
		ItemStack held = player.getHeldItemMainhand();
		if (!held.isEmpty() && held.getItem() instanceof ISpellContainerItem) {
			Spell spell = ((ISpellContainerItem) held.getItem()).getSpell(held);
			if (spell != null && spell.supportsPreview()) {
				preview = spell.getPreview(player, partialTicks);
			}
		}
		
		if (preview == null) {
			held = player.getHeldItemOffhand();
			if (!held.isEmpty() && held.getItem() instanceof ISpellContainerItem) {
				Spell spell = ((ISpellContainerItem) held.getItem()).getSpell(held);
				if (spell != null && spell.supportsPreview()) {
					preview = spell.getPreview(player, partialTicks);
				}
			}
		}
		
		if (preview != null) {
			IRenderTypeBuffer bufferIn = mc.getRenderTypeBuffers().getBufferSource();

			Vector3d cameraPos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
			//Vector3d playerPosOffset = mc.player.getEyePosition(partialTicks).subtract(cameraPos);
			
			matrixStackIn.push();
			matrixStackIn.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
			//matrixStackIn.translate(-playerPosOffset.x, -playerPosOffset.y, -playerPosOffset.z);
			for (SpellShapePreviewComponent comp : preview.getComponents()) {
				renderComponent(matrixStackIn, bufferIn, partialTicks, comp, 1f, 0f, 1f, 1f);
			}
			matrixStackIn.pop();
			mc.getRenderTypeBuffers().getBufferSource().finish();
		}
	}
	
	protected <T extends SpellShapePreviewComponent> void renderComponent(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float partialTicks, T comp, float red, float green, float blue, float alpha) {
		ISpellShapeComponentRenderer<T> renderer = GetRenderer(comp);
		if (renderer != null) {
			renderer.render(matrixStackIn, bufferIn, partialTicks, comp, 1f, 0f, 1f, .6f);
		}
	}
	
	public static interface ISpellShapeComponentRenderer<T extends SpellShapePreviewComponent> {
		public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float partialTicks, T component, float red, float green, float blue, float alpha);
	}
	
	private static final Map<SpellShapePreviewComponent.Type<?>, ISpellShapeComponentRenderer<?>> ComponentRenderers = new HashMap<>();
	
	public static <T extends SpellShapePreviewComponent> void RegisterRenderer(SpellShapePreviewComponent.Type<T> type, ISpellShapeComponentRenderer<T> renderer) {
		ComponentRenderers.put(type, renderer);
	}
	
	@SuppressWarnings("unchecked")
	protected static <T extends SpellShapePreviewComponent> @Nullable ISpellShapeComponentRenderer<T> GetRenderer(T comp) {
		return (ISpellShapeComponentRenderer<T>) ComponentRenderers.get(comp.getType());
	}
}
