package com.smanzana.nostrummagica.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.OutlineRenderer.Outline;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpellShapeRenderer {
	
	public static final ResourceLocation TEXTURE = NostrumMagica.Loc("textures/models/spellshape.png");
	
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
			for (Entity ent : outlinedEntities) {
				outliner.remove(ent);
			}
			outlinedEntities.clear();
		}
	}
	
	protected void addEntity(Entity ent, Outline outline) {
		this.outlinedEntities.add(ent);
		outliner.add(ent, outline);
	}
	
	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		if (!enabled) {
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		MatrixStack matrixStackIn = event.getMatrixStack();
		
		// Figure out if we should show a preview
		SpellShapePreview preview = player.randomUnused1 == 0 ? null : new SpellShapePreview();
		
		if (preview != null) {
			for (SpellShapePreviewComponent comp : preview.getComponents()) {
				renderComponent(matrixStackIn, comp, 1f, 0f, 1f, 1f);
			}
		}
	}
	
	protected <T extends SpellShapePreviewComponent> void renderComponent(MatrixStack matrixStackIn, T comp, float red, float green, float blue, float alpha) {
		ISpellShapeComponentRenderer<T> renderer = GetRenderer(comp);
		if (renderer != null) {
			renderer.render(matrixStackIn, comp, 1f, 0f, 1f, 1f);
		}
	}
	
	public static interface ISpellShapeComponentRenderer<T extends SpellShapePreviewComponent> {
		public void render(MatrixStack matrixStackIn, T component, float red, float green, float blue, float alpha);
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
