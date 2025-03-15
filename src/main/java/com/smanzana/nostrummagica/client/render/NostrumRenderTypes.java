package com.smanzana.nostrummagica.client.render;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.client.model.ModelSwitchTrigger;
import com.smanzana.nostrummagica.client.render.entity.RenderHookShot;
import com.smanzana.nostrummagica.client.render.layer.LayerManaArmor;
import com.smanzana.nostrummagica.client.render.tile.TileEntityLockedChestRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityPortalRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityProgressionDoorRenderer;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class NostrumRenderTypes {

	
	public static final RenderType HOOKSHOT_CHAIN;
	public static final RenderType MANA_ARMOR;
	public static final RenderType SWITCH_TRIGGER_BASE;
	public static final RenderType SWITCH_TRIGGER_CAGE;
	public static final RenderType NOSTRUM_PORTAL;
	public static final RenderType PROGRESSION_DOOR_LOCK;
	public static final RenderType LOCKEDCHEST_LOCK;
	public static final RenderType LOCKEDCHEST_CHAIN;
	public static final RenderType SPELLSHAPE_QUADS;
	public static final RenderType SPELLSHAPE_ORB_CHAIN;
	public static final RenderType SPELLSHAPE_LINES;
	public static final RenderType SPELLSHAPE_LINES_THICK;
	public static final RenderType WORLD_SELECT_HIGHLIGHT;
	public static final RenderType WORLD_SELECT_HIGHLIGHT_CULL;
	
	private static final String Name(String suffix) {
		return "nostrumrender_" + suffix;
	}
	
	// Could make func that took a texture and returns a render type for flat, unlit icons
	
	// Set up states that we use. Pull some from RenderState itself, and make some custom ones/ones not worth the effort to pull out.
	//private static final RenderState.TextureState BLOCKATLAS_MIPMAP = new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, true);
	
	//private static final RenderState.TransparencyState LIGHTNING_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "LIGHTNING_TRANSPARENCY");
	private static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "TRANSLUCENT_TRANSPARENCY");
	//private static final RenderState.TransparencyState NO_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "NO_TRANSPARENCY");

	private static final RenderState.LayerState VIEW_OFFSET_Z_LAYERING = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "VIEW_OFFSET_Z_LAYERING");

	private static final RenderState.TargetState ITEM_ENTITY_TARGET = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "ITEM_ENTITY_TARGET");
	
	private static final RenderState.WriteMaskState WRITE_TO_DEPTH_AND_COLOR = new RenderState.WriteMaskState(true, true);
	private static final RenderState.WriteMaskState WRITE_NO_DEPTH_BUT_COLOR = new RenderState.WriteMaskState(true, false);
    
	private static final RenderState.CullState NO_CULL = new RenderState.CullState(false);
    
	//private static final RenderState.DepthTestState DEPTH_EQUAL = new RenderState.DepthTestState("==", GL11.GL_EQUAL);
	private static final RenderState.DepthTestState NO_DEPTH = new RenderState.DepthTestState("none", GL11.GL_ALWAYS);
    
	private static final RenderState.LightmapState NO_LIGHTING = new RenderState.LightmapState(false);
	private static final RenderState.LightmapState LIGHTMAP_ENABLED = new RenderState.LightmapState(true);
	private static final RenderState.OverlayState OVERLAY_ENABLED = new RenderState.OverlayState(true);
	
	private static final RenderState.DiffuseLightingState DIFFUSE_LIGHTING_ENABLED = new RenderState.DiffuseLightingState(true);
	
	private static final RenderState.AlphaState DEFAULT_ALPHA = new RenderState.AlphaState(0.003921569F);
	private static final RenderState.AlphaState CUTOUT_ALPHA = new RenderState.AlphaState(.5f);
	
	//private static final RenderState.FogState NO_FOG = new RenderState.FogState("no_fog", () -> {}, () -> {});
	
	private static final RenderState.LineState LINE_3 = new RenderState.LineState(OptionalDouble.of(3f));
	private static final RenderState.LineState LINE_10 = new RenderState.LineState(OptionalDouble.of(10f));
	
    private static final RenderState.TexturingState MANAARMOR_GLINT = new RenderState.TexturingState("nostrum_manaarmor_glint", () -> {
    	//setupGlintTexturing(0.16F);
		RenderSystem.matrixMode(GL11.GL_TEXTURE);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		final long ms = Util.getMillis();
		final long ticks = ms / (1000/20); // whole ticks
		final long remain = ms % (1000/20); // partial ticks in ms
		
		// old formula for xoffset was "0 + (ageInTicks + partialTicks) * .001"
		// So we wanted to shift .001 unit for every tick
		
		final float offset = (.001f * ticks) + (.000001f * remain);
		RenderSystem.translatef(offset, 0f, 0f);
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    }, () -> {
    	RenderSystem.matrixMode(GL11.GL_TEXTURE);
    	RenderSystem.popMatrix();
    	RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    });
    
    private static final RenderState.TexturingState SPELLSHAPE_TEXTURING = new RenderState.TexturingState("spellshape_glint", () -> {
    	//setupGlintTexturing(0.16F);
		RenderSystem.matrixMode(GL11.GL_TEXTURE);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		final long ms = Util.getMillis();
		final long ticks = ms / (1000/20); // whole ticks
		final long remain = ms % (1000/20); // partial ticks in ms
		
		// old formula for xoffset was "0 + (ageInTicks + partialTicks) * .001"
		// So we wanted to shift .001 unit for every tick
		
		final float offset = (.001f * ticks) + (.000001f * remain);
		RenderSystem.translatef(offset, 0f, 0f);
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    }, () -> {
    	RenderSystem.matrixMode(GL11.GL_TEXTURE);
    	RenderSystem.popMatrix();
    	RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    });
		

	static {
	    // Define render types
		RenderType.State glState;
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(RenderHookShot.CHAIN_TEXTURE, false, true))
				.setCullState(NO_CULL)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setOutputState(ITEM_ENTITY_TARGET)
				.setAlphaState(CUTOUT_ALPHA)
			.createCompositeState(false);
		HOOKSHOT_CHAIN = RenderType.create(Name("hookshot_chain"), DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(LayerManaArmor.TEXTURE_ARMOR, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(MANAARMOR_GLINT)
				// depth test?
			.createCompositeState(false);
		MANA_ARMOR = RenderType.create(Name("manaarmor"), DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_BLOCK, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(SPELLSHAPE_TEXTURING)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_QUADS = RenderType.create(Name("spellshape"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setCullState(NO_CULL)
				//.texturing(SPELLSHAPE_TEXTURING)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_ORB_CHAIN = RenderType.create(Name("spellshape_chain"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.State.builder()
				//.texture(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(SPELLSHAPE_TEXTURING)
				.setLineState(LINE_3)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_LINES = RenderType.create(Name("spellshape_lines"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_LINES, 32, glState);
		
		glState = RenderType.State.builder()
				//.texture(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(SPELLSHAPE_TEXTURING)
				.setLineState(LINE_10)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_LINES_THICK = RenderType.create(Name("spellshape_lines_thick"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_LINES, 32, glState);
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(ModelSwitchTrigger.TEXT, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
			.createCompositeState(false);
		SWITCH_TRIGGER_BASE = RenderType.create(Name("switch_trigger_base"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_TRIANGLES, 64, glState);
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(ModelSwitchTrigger.CAGE_TEXT, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				//.depthTest(DEPTH_EQUAL)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
			.createCompositeState(false);
		SWITCH_TRIGGER_CAGE = RenderType.create(Name("switch_trigger_cage"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_TRIANGLES, 64, glState);
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(TileEntityPortalRenderer.TEX_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setCullState(NO_CULL)
			.createCompositeState(false);
		NOSTRUM_PORTAL = RenderType.create(Name("nostrum_portal"), DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_TRIANGLES, 64, glState);
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(TileEntityProgressionDoorRenderer.TEX_GEM_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_TO_DEPTH_AND_COLOR)
			.createCompositeState(false);
		PROGRESSION_DOOR_LOCK = RenderType.create(Name("prog_door_lock"), DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_TRIANGLES, 64, glState);
		
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(TileEntityLockedChestRenderer.TEXT_LOCK_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setCullState(NO_CULL)
			.createCompositeState(false);
		LOCKEDCHEST_LOCK = RenderType.create(Name("lockedchest_lock"), DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 32, glState);
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(TileEntityLockedChestRenderer.TEXT_CHAINLINK_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setCullState(NO_CULL)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
			.createCompositeState(false);
		LOCKEDCHEST_CHAIN = RenderType.create(Name("lockedchest_chain"), DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 64, glState);
		
		glState = RenderType.State.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setDepthTestState(NO_DEPTH)
			.createCompositeState(false);
		WORLD_SELECT_HIGHLIGHT_CULL = RenderType.create(Name("WorldSelectCull"), DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 16, glState);
		
		glState = RenderType.State.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setDepthTestState(NO_DEPTH)
				.setCullState(NO_CULL) // Previously only was no-cull if inside box
			.createCompositeState(false);
		WORLD_SELECT_HIGHLIGHT = RenderType.create(Name("WorldSelect"), DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 16, glState);
	}
	
	public static final RenderType GetIconType(ResourceLocation texture) {
		RenderType.State glState;
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(texture, false, true))
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				//.layer(VIEW_OFFSET_Z_LAYERING)
				//.target(ITEM_ENTITY_TARGET)
				//.writeMask(WRITE_TO_DEPTH_AND_COLOR)
			.createCompositeState(false);
		return RenderType.create(Name("flaticon"), DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 32, glState);
	}
	
	public static final RenderType GetBlendedEntity(ResourceLocation texture, boolean affectsOutline) {
		// This based on RenderType.getEntityTranslucent(locationIn) but with no depth buffer writing
		RenderType.State glState;
		
//		RenderType.State rendertype$state = RenderType.State.getBuilder()
//				.texture(new RenderState.TextureState(LocationIn, false, false))
//				.transparency(TRANSLUCENT_TRANSPARENCY)
//				.diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
//				.alpha(DEFAULT_ALPHA).cull(CULL_DISABLED)
//				.lightmap(LIGHTMAP_ENABLED)
//				.overlay(OVERLAY_ENABLED)
//			.build(outlineIn);
		
		RenderSystem.defaultBlendFunc();
		
		glState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(texture, false, false))
				.setTransparencyState(new RenderState.TransparencyState("translucent_transparency", () -> {
				      RenderSystem.enableBlend();
				      RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				   }, () -> {
				      RenderSystem.disableBlend();
				      RenderSystem.defaultBlendFunc();
				   }))
				.setDiffuseLightingState(DIFFUSE_LIGHTING_ENABLED)
				.setAlphaState(DEFAULT_ALPHA).setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setOverlayState(OVERLAY_ENABLED)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
			.createCompositeState(affectsOutline);
		return RenderType.create(Name("nostrum_blendedentity"), DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 256, glState);
	}
	
	public static final RenderType GetBlendedEntity(ResourceLocation texture) {
		return GetBlendedEntity(texture, true);
	}
	
}
