package com.smanzana.nostrummagica.client.render;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.smanzana.nostrummagica.client.effects.ClientEffectBeam;
import com.smanzana.nostrummagica.client.model.SwitchTriggerModel;
import com.smanzana.nostrummagica.client.render.entity.HookShotRenderer;
import com.smanzana.nostrummagica.client.render.layer.ManaArmorLayer;
import com.smanzana.nostrummagica.client.render.tile.LockedChestBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.PortalBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.ProgressionDoorBlockEntityRenderer;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class NostrumRenderTypes extends RenderType {

	
	public static RenderType HOOKSHOT_CHAIN;
	public static RenderType MANA_ARMOR;
	public static RenderType SWITCH_TRIGGER_BASE;
	public static RenderType SWITCH_TRIGGER_CAGE;
	public static RenderType NOSTRUM_PORTAL;
	public static RenderType PROGRESSION_DOOR_LOCK;
	public static RenderType LOCKEDCHEST_LOCK;
	public static RenderType LOCKEDCHEST_CHAIN;
	public static RenderType SPELLSHAPE_QUADS;
	public static RenderType SPELLSHAPE_ORB_CHAIN;
	public static RenderType SPELLSHAPE_LINES;
	public static RenderType SPELLSHAPE_LINES_THICK;
	public static RenderType WORLD_SELECT_HIGHLIGHT;
	public static RenderType WORLD_SELECT_HIGHLIGHT_CULL;
	public static RenderType SPELL_BEAM_SOLID;
	public static RenderType SPELL_BEAM_TRANSLUCENT;
	
	private static final String Name(String suffix) {
		return "nostrumrender_" + suffix;
	}
	
	// Could make func that took a texture and returns a render type for flat, unlit icons
	
	// Set up states that we use. Pull some from RenderState itself, and make some custom ones/ones not worth the effort to pull out.
	//private static final RenderState.TextureState BLOCKATLAS_MIPMAP = new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, true);
	
	//private static final RenderState.TransparencyState LIGHTNING_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "LIGHTNING_TRANSPARENCY");
	private static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = RenderStateShard.TRANSLUCENT_TRANSPARENCY;
	//private static final RenderState.TransparencyState NO_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "NO_TRANSPARENCY");

	private static final RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING = RenderStateShard.VIEW_OFFSET_Z_LAYERING;

	private static final RenderStateShard.OutputStateShard ITEM_ENTITY_TARGET = RenderStateShard.ITEM_ENTITY_TARGET;
	
	private static final RenderStateShard.WriteMaskStateShard WRITE_TO_DEPTH_AND_COLOR = new RenderStateShard.WriteMaskStateShard(true, true);
	private static final RenderStateShard.WriteMaskStateShard WRITE_NO_DEPTH_BUT_COLOR = new RenderStateShard.WriteMaskStateShard(true, false);
    
	private static final RenderStateShard.CullStateShard NO_CULL = new RenderStateShard.CullStateShard(false);
    
	//private static final RenderState.DepthTestState DEPTH_EQUAL = new RenderState.DepthTestState("==", GL11.GL_EQUAL);
	private static final RenderStateShard.DepthTestStateShard NO_DEPTH = new RenderStateShard.DepthTestStateShard("none", GL11.GL_ALWAYS);
    
	private static final RenderStateShard.LightmapStateShard NO_LIGHTING = new RenderStateShard.LightmapStateShard(false);
	private static final RenderStateShard.LightmapStateShard LIGHTMAP_ENABLED = new RenderStateShard.LightmapStateShard(true);
	private static final RenderStateShard.OverlayStateShard OVERLAY_ENABLED = new RenderStateShard.OverlayStateShard(true);
	
	//private static final RenderState.FogState NO_FOG = new RenderState.FogState("no_fog", () -> {}, () -> {});
	
	private static final RenderStateShard.LineStateShard LINE_3 = new RenderStateShard.LineStateShard(OptionalDouble.of(3f));
	private static final RenderStateShard.LineStateShard LINE_10 = new RenderStateShard.LineStateShard(OptionalDouble.of(10f));
	
    private static final RenderStateShard.TexturingStateShard MANAARMOR_GLINT = new RenderStateShard.TexturingStateShard("nostrum_manaarmor_glint", () -> {
    	//setupGlintTexturing(0.16F);
		final long ms = Util.getMillis();
		final long ticks = ms / (1000/20); // whole ticks
		final long remain = ms % (1000/20); // partial ticks in ms
		
		// old formula for xoffset was "0 + (ageInTicks + partialTicks) * .001"
		// So we wanted to shift .001 unit for every tick
		
		final float offset = (.001f * ticks) + (.000001f * remain);
		
		Matrix4f matrix = Matrix4f.createTranslateMatrix(offset, 0, 0);
		RenderSystem.setTextureMatrix(matrix);
		
		
    }, () -> {
    	RenderSystem.resetTextureMatrix();
    });
    
    private static final RenderStateShard.TexturingStateShard SPELLSHAPE_TEXTURING = new RenderStateShard.TexturingStateShard("spellshape_glint", () -> {
    	//setupGlintTexturing(0.16F);
		final long ms = Util.getMillis();
		final long ticks = ms / (1000/20); // whole ticks
		final long remain = ms % (1000/20); // partial ticks in ms
		
		// old formula for xoffset was "0 + (ageInTicks + partialTicks) * .001"
		// So we wanted to shift .001 unit for every tick
		
		final float offset = (.001f * ticks) + (.000001f * remain);
		Matrix4f matrix = Matrix4f.createTranslateMatrix(offset, 0, 0);
		RenderSystem.setTextureMatrix(matrix);
    }, () -> {
    	RenderSystem.resetTextureMatrix();
    });
		

	public static void InitRenderStates() {
	    // Define render types
		RenderType.CompositeState glState;
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(HookShotRenderer.CHAIN_TEXTURE, false, true))
				.setCullState(NO_CULL)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setOutputState(ITEM_ENTITY_TARGET)
				.setShaderState(POSITION_TEX_SHADER)
			.createCompositeState(false);
		HOOKSHOT_CHAIN = RenderType.create(Name("hookshot_chain"), DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 128, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(ManaArmorLayer.TEXTURE_ARMOR, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(MANAARMOR_GLINT)
				.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
				// depth test?
			.createCompositeState(false);
		MANA_ARMOR = RenderType.create(Name("manaarmor"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 128, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(SpellShapeRenderer.TEXTURE_BLOCK, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(SPELLSHAPE_TEXTURING)
				.setShaderState(POSITION_COLOR_TEX_SHADER)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_QUADS = RenderType.create(Name("spellshape"), DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 128, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setCullState(NO_CULL)
				.setShaderState(POSITION_COLOR_TEX_SHADER)
				//.texturing(SPELLSHAPE_TEXTURING)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_ORB_CHAIN = RenderType.create(Name("spellshape_chain"), DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 128, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				//.texture(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(SPELLSHAPE_TEXTURING)
				.setLineState(LINE_3)
				.setShaderState(RENDERTYPE_LINES_SHADER)
				.setCullState(NO_CULL)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_LINES = RenderType.create(Name("spellshape_lines"), DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 32, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				//.texture(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setTexturingState(SPELLSHAPE_TEXTURING)
				.setLineState(LINE_10)
				.setShaderState(RENDERTYPE_LINES_SHADER)
				.setCullState(NO_CULL)
				// depth test?
			.createCompositeState(false);
		SPELLSHAPE_LINES_THICK = RenderType.create(Name("spellshape_lines_thick"), DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 32, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(SwitchTriggerModel.TEXT, false, true))
				.setTransparencyState(NO_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				//.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				//.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setShaderState(POSITION_COLOR_TEX_SHADER)
			.createCompositeState(true);
		SWITCH_TRIGGER_BASE = RenderType.create(Name("switch_trigger_base"), DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 64, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(SwitchTriggerModel.CAGE_TEXT, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				//.depthTest(DEPTH_EQUAL)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setShaderState(POSITION_COLOR_TEX_SHADER)
			.createCompositeState(false);
		SWITCH_TRIGGER_CAGE = RenderType.create(Name("switch_trigger_cage"), DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 64, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(PortalBlockEntityRenderer.TEX_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setCullState(NO_CULL)
				.setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
			.createCompositeState(false);
		NOSTRUM_PORTAL = RenderType.create(Name("nostrum_portal"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.TRIANGLES, 64, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(ProgressionDoorBlockEntityRenderer.TEX_GEM_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setWriteMaskState(WRITE_TO_DEPTH_AND_COLOR)
				.setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
			.createCompositeState(false);
		PROGRESSION_DOOR_LOCK = RenderType.create(Name("prog_door_lock"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.TRIANGLES, 64, false, false, glState);
		
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(LockedChestBlockEntityRenderer.TEXT_LOCK_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setCullState(NO_CULL)
				.setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
			.createCompositeState(false);
		LOCKEDCHEST_LOCK = RenderType.create(Name("lockedchest_lock"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 32, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(LockedChestBlockEntityRenderer.TEXT_CHAINLINK_LOC, false, true))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setCullState(NO_CULL)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
			.createCompositeState(false);
		LOCKEDCHEST_CHAIN = RenderType.create(Name("lockedchest_chain"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 64, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setDepthTestState(NO_DEPTH)
				.setShaderState(POSITION_COLOR_SHADER)
			.createCompositeState(false);
		WORLD_SELECT_HIGHLIGHT_CULL = RenderType.create(Name("WorldSelectCull"), DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 16, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setDepthTestState(NO_DEPTH)
				.setCullState(NO_CULL) // Previously only was no-cull if inside box
				.setShaderState(POSITION_COLOR_SHADER)
			.createCompositeState(false);
		WORLD_SELECT_HIGHLIGHT = RenderType.create(Name("WorldSelect"), DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 16, false, false, glState);
		
		{
			RenderType.CompositeState.CompositeStateBuilder beamBuilder = RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_BEACON_BEAM_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(ClientEffectBeam.TEX_BEAM, false, false))
					.setTransparencyState(NO_TRANSPARENCY)
					.setWriteMaskState(COLOR_DEPTH_WRITE)
					;
			
			glState = beamBuilder.createCompositeState(false);
			SPELL_BEAM_SOLID = RenderType.create(Name("SpellBeam_Solid"), DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 32, false, false, glState);
			
			glState = beamBuilder
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setWriteMaskState(COLOR_WRITE)
				.createCompositeState(false);
			SPELL_BEAM_TRANSLUCENT = RenderType.create(Name("SpellBeam_Trans"), DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 32, false, true, glState);
		}
	}
	
	static {
		InitRenderStates();
	}
	
	private NostrumRenderTypes(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
		super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
		throw new UnsupportedOperationException("Should not be instantiated");
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
				.setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
			.createCompositeState(false);
		return RenderType.create(Name("flaticon"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 32, false, false, glState);
	}
	
	public static final RenderType GetBlendedEntity(ResourceLocation texture, boolean affectsOutline) {
		// This based on RenderType.getEntityTranslucent(locationIn) but with no depth buffer writing
		RenderType.CompositeState glState;
		
//		RenderType.State rendertype$state = RenderType.State.getBuilder()
//				.texture(new RenderState.TextureState(LocationIn, false, false))
//				.transparency(TRANSLUCENT_TRANSPARENCY)
//				.diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
//				.alpha(DEFAULT_ALPHA)
//				.cull(CULL_DISABLED)
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
				//.setDiffuseLightingState(DIFFUSE_LIGHTING_ENABLED)
				//.setAlphaState(DEFAULT_ALPHA)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setOverlayState(OVERLAY_ENABLED)
				.setWriteMaskState(WRITE_NO_DEPTH_BUT_COLOR)
				.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
			.createCompositeState(affectsOutline);
		return RenderType.create(Name("nostrum_blendedentity"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, glState);
	}
	
	public static final RenderType GetBlendedEntity(ResourceLocation texture) {
		return GetBlendedEntity(texture, true);
	}
	
}
