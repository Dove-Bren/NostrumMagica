package com.smanzana.nostrummagica.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.MimicBlock;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;

@SuppressWarnings("deprecation")
public class MimicBlockBakedModel implements BakedModel {

	//private final TextureAtlasSprite particle;
	private final BakedModel undisguisedModel;
	
	public MimicBlockBakedModel(BakedModel undisguisedModel) {
		//particle = Minecraft.getInstance().getTextureMap().getAtlasSprite(new ResourceLocation(NostrumMagica.MODID, "block/mimic_facade").toString());
		this.undisguisedModel = undisguisedModel;
	}
	
	public MimicBlockBakedModel() {
		this(Minecraft.getInstance().getBlockRenderer().getBlockModel(NostrumBlocks.mimicFacade.defaultBlockState()));
	}
	
	protected MimicBlock.MimicBlockData getNestedData(@Nullable IModelData data) {
		MimicBlock.MimicBlockData ret = null;
		if (data != null) {
			ret = data.getData(MimicBlock.MIMIC_MODEL_PROPERTY);
		}
		
		return ret;
	}
	
	protected @Nullable BlockState getNestedState(@Nullable IModelData data) {
		BlockState state = null;
		if (data != null) {
			MimicBlock.MimicBlockData mimicData = getNestedData(data);
			if (mimicData != null) {
				state = mimicData.getBlockState();
			}
		}
		
		return state;
	}
	
	protected @Nonnull BakedModel getModelToRender(@Nullable BlockState nestedState) {
		final BakedModel missing = this.undisguisedModel;//Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
		BakedModel nestedModel = null;
		
		if (nestedState != null) {
			
//			// Stupid CTM wraps up models and needs to be unwrapped
//			if (nestedState instanceof IExtendedBlockState) {
//				BlockState trueState = ((IExtendedBlockState) nestedState).getClean();
//				if (trueState != null) {
//					nestedState = trueState;
//				}
//			}
			
			nestedModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(nestedState);
		}
		
		return nestedModel == null ? missing : nestedModel;
	}
	
	protected @Nonnull BakedModel getModelToRender(@Nullable IModelData extraData) {
		return getModelToRender(getNestedState(extraData));
	}
	
	//////Things based on wrapped model
	@Override
	public TextureAtlasSprite getParticleIcon(IModelData data) {
		return getModelToRender(data).getParticleIcon(data);
	}
	
	protected static final List<BakedQuad> EmptyQuads = new ArrayList<>(0);
	
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, Direction side, Random rand, IModelData extraData) {
		// Note: block says "render me in ALL layers" so that this func gets called in each layer.
		// And this model then checks the wrapped model and sees if it should be rendered in the current layer.
		
		BlockState nested = getNestedState(extraData);
		if (nested == null || nested.isAir()) {
			return this.undisguisedModel.getQuads(state, side, rand, extraData);
		}else if (ItemBlockRenderTypes.canRenderInLayer(state, MinecraftForgeClient.getRenderType())) {
			// this is where we'd overwrite the tint index :(
			return getModelToRender(nested).getQuads(nested, side, rand, extraData);
		} else {
			return EmptyQuads;
		}
	}
	
	////// Hardcoded things based on base undisguised model
	// Couldn't I just override getBakedModel ?
	@Override
	public boolean useAmbientOcclusion() {
		return this.undisguisedModel.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return this.undisguisedModel.isGui3d();
	}

	@Override
	public boolean isCustomRenderer() {
		return this.undisguisedModel.isCustomRenderer();
	}
	
	@Override
	public ItemTransforms getTransforms() {
		return this.undisguisedModel.getTransforms();
	}

	@Override
	public ItemOverrides getOverrides() {
		return this.undisguisedModel.getOverrides();
	}
	
	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand) {
		// Show undisguised state
		return undisguisedModel.getQuads(state, side, rand);
	}
	
	@Override
	public TextureAtlasSprite getParticleIcon() {
		return undisguisedModel.getParticleIcon();
	}

	@Override
	public boolean usesBlockLight() {
		return true; // I think this is "!guiLight" aka "is this lit like a block instead of flat like a GUI item?"
	}
}
