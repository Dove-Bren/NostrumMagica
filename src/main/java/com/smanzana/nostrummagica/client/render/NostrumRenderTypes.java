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

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
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
	private static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderStateShard.class, null, "TRANSLUCENT_TRANSPARENCY");
	//private static final RenderState.TransparencyState NO_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "NO_TRANSPARENCY");

	private static final RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING = ObfuscationReflectionHelper.getPrivateValue(RenderStateShard.class, null, "VIEW_OFFSET_Z_LAYERING");

	private static final RenderStateShard.OutputStateShard ITEM_ENTITY_TARGET = ObfuscationReflectionHelper.getPrivateValue(RenderStateShard.class, null, "ITEM_ENTITY_TARGET");
	
	private static final RenderStateShard.WriteMaskStateShard WRITE_TO_DEPTH_AND_COLOR = new RenderStateShard.WriteMaskStateShard(true, true);
	private static final RenderStateShard.WriteMaskStateShard WRITE_NO_DEPTH_BUT_COLOR = new RenderStateShard.WriteMaskStateShard(true, false);
    
	private static final RenderStateShard.CullStateShard NO_CULL = new RenderStateShard.CullStateShard(false);
    
	//private static final RenderState.DepthTestState DEPTH_EQUAL = new RenderState.DepthTestState("==", GL11.GL_EQUAL);
	private static final RenderStateShard.DepthTestStateShard NO_DEPTH = new RenderStateShard.DepthTestStateShard("none", GL11.GL_ALWAYS);
    
	private static final RenderStateShard.LightmapStateShard NO_LIGHTING = new RenderStateShard.LightmapStateShard(false);
	private static final RenderStateShard.LightmapStateShard LIGHTMAP_ENABLED = new RenderStateShard.LightmapStateShard(true);
	private static final RenderStateShard.OverlayStateShard OVERLAY_ENABLED = new RenderStateShard.OverlayStateShard(true);
	
	private static final RenderStateShard.DiffuseLightingStateShard DIFFUSE_LIGHTING_ENABLED = new RenderStateShard.DiffuseLightingStateShard(true);
	
	private static final RenderStateShard.AlphaStateShard DEFAULT_ALPHA = new RenderStateShard.AlphaStateShard(0.003921569F);
	private static final RenderStateShard.AlphaStateShard CUTOUT_ALPHA = new RenderStateShard.AlphaStateShard(.5f);
	
	//private static final RenderState.FogState NO_FOG = new RenderState.FogState("no_fog", () -> {}, () -> {});
	
	private static final RenderStateShard.LineStateShard LINE_3 = new RenderStateShard.LineStateShard(OptionalDouble.of(3f));
	private static final RenderStateShard.LineStateShard LINE_10 = new RenderStateShard.LineStateShard(OptionalDouble.of(10f));
	
    private static final RenderStateShard.TexturingStateShard MANAARMOR_GLINT = new RenderStateShard.TexturingStateShard("nostrum_manaarmor_glint", () -> {
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
    
    private static final RenderStateShard.TexturingStateShard SPELLSHAPE_TEXTURING = new RenderStateShard.TexturingStateShard("spellshape_glint", () -> {
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
		RenderType.CompositeState glState;
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(RenderHookShot.CHAIN_TEXTURE, false, true))
				.setCullState(NO_CULL)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setOutputState(ITEM_ENTITY_TARGET)
				.setAlphaState(CUTOUT_ALPHA)
			.createCompositeState(false);
		HOOKSHOT_CHAIN = RenderType.create(Name("hookshot_chain"), DefaultVertexFormat.POSITION_TEX, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(LayerManaArmor.TEXTURE_ARMOR, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(MANAARMOR_GLINT)
				// depth test?
			.createCompositeState(false);
		MANA_ARMOR = RenderType.create(Name("manaarmor"), DefaultVertexFormat.NEW_ENTITY, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(SpellShapeRenderer.TEXTURE_BLOCK, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(SPELLSHAPE_TEXTURING)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_QUADS = RenderType.create(Name("spellshape"), DefaultVertexFormat.POSITION_COLOR_TEX, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setCullState(NO_CULL)
				//.texturing(SPELLSHAPE_TEXTURING)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_ORB_CHAIN = RenderType.create(Name("spellshape_chain"), DefaultVertexFormat.POSITION_COLOR_TEX, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.CompositeState.builder()
				//.texture(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(SPELLSHAPE_TEXTURING)
				.setLineState(LINE_3)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_LINES = RenderType.create(Name("spellshape_lines"), DefaultVertexFormat.POSITION_COLOR_TEX, GL11.GL_LINES, 32, glState);
		
		glState = RenderType.CompositeState.builder()
				//.texture(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(SPELLSHAPE_TEXTURING)
				.setLineState(LINE_10)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_LINES_THICK = RenderType.create(Name("spellshape_lines_thick"), DefaultVertexFormat.POSITION_COLOR_TEX, GL11.GL_LINES, 32, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(ModelSwitchTrigger.TEXT, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
			.createCompositeState(false);
		SWITCH_TRIGGER_BASE = RenderType.create(Name("switch_trigger_base"), DefaultVertexFormat.POSITION_COLOR_TEX, GL11.GL_TRIANGLES, 64, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(ModelSwitchTrigger.CAGE_TEXT, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				//.depthTest(DEPTH_EQUAL)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
			.createCompositeState(false);
		SWITCH_TRIGGER_CAGE = RenderType.create(Name("switch_trigger_cage"), DefaultVertexFormat.POSITION_COLOR_TEX, GL11.GL_TRIANGLES, 64, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(TileEntityPortalRenderer.TEX_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setCullState(NO_CULL)
			.createCompositeState(false);
		NOSTRUM_PORTAL = RenderType.create(Name("nostrum_portal"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_TRIANGLES, 64, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(TileEntityProgressionDoorRenderer.TEX_GEM_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_TO_DEPTH_AND_COLOR)
			.createCompositeState(false);
		PROGRESSION_DOOR_LOCK = RenderType.create(Name("prog_door_lock"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_TRIANGLES, 64, glState);
		
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(TileEntityLockedChestRenderer.TEXT_LOCK_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setCullState(NO_CULL)
			.createCompositeState(false);
		LOCKEDCHEST_LOCK = RenderType.create(Name("lockedchest_lock"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 32, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(TileEntityLockedChestRenderer.TEXT_CHAINLINK_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setCullState(NO_CULL)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
			.createCompositeState(false);
		LOCKEDCHEST_CHAIN = RenderType.create(Name("lockedchest_chain"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 64, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setDepthTestState(NO_DEPTH)
			.createCompositeState(false);
		WORLD_SELECT_HIGHLIGHT_CULL = RenderType.create(Name("WorldSelectCull"), DefaultVertexFormat.POSITION_COLOR, GL11.GL_QUADS, 16, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setDepthTestState(NO_DEPTH)
				.setCullState(NO_CULL) // Previously only was no-cull if inside box
			.createCompositeState(false);
		WORLD_SELECT_HIGHLIGHT = RenderType.create(Name("WorldSelect"), DefaultVertexFormat.POSITION_COLOR, GL11.GL_QUADS, 16, glState);
	}
	
	public static final RenderType GetIconType(ResourceLocation texture) {
		RenderType.CompositeState glState;
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, false, true))
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				//.layer(VIEW_OFFSET_Z_LAYERING)
				//.target(ITEM_ENTITY_TARGET)
				//.writeMask(WRITE_TO_DEPTH_AND_COLOR)
			.createCompositeState(false);
		return RenderType.create(Name("flaticon"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 32, glState);
	}
	
	public static final RenderType GetBlendedEntity(ResourceLocation texture, boolean affectsOutline) {
		// This based on RenderType.getEntityTranslucent(locationIn) but with no depth buffer writing
		RenderType.CompositeState glState;
		
//		RenderType.State rendertype$state = RenderType.State.getBuilder()
//				.texture(new RenderState.TextureState(LocationIn, false, false))
//				.transparency(TRANSLUCENT_TRANSPARENCY)
//				.diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
//				.alpha(DEFAULT_ALPHA).cull(CULL_DISABLED)
//				.lightmap(LIGHTMAP_ENABLED)
//				.overlay(OVERLAY_ENABLED)
//			.build(outlineIn);
		
		RenderSystem.defaultBlendFunc();
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
				.setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
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
		return RenderType.create(Name("nostrum_blendedentity"), DefaultVertexFormat.NEW_ENTITY, GL11.GL_QUADS, 256, glState);
	}
	
	public static final RenderType GetBlendedEntity(ResourceLocation texture) {
		return GetBlendedEntity(texture, true);
	}
	
}
