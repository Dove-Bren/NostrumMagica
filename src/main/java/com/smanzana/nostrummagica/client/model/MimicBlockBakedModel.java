package com.smanzana.nostrummagica.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.blocks.MimicBlock;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;

@SuppressWarnings("deprecation")
public class MimicBlockBakedModel implements IBakedModel {

	//private final TextureAtlasSprite particle;
	private final IBakedModel undisguisedModel;
	
	public MimicBlockBakedModel(IBakedModel undisguisedModel) {
		//particle = Minecraft.getInstance().getTextureMap().getAtlasSprite(new ResourceLocation(NostrumMagica.MODID, "block/mimic_facade").toString());
		this.undisguisedModel = undisguisedModel;
	}
	
	public MimicBlockBakedModel() {
		this(Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(NostrumBlocks.mimicFacade.getDefaultState()));
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
	
	protected @Nonnull IBakedModel getModelToRender(@Nullable BlockState nestedState) {
		final IBakedModel missing = this.undisguisedModel;//Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
		IBakedModel nestedModel = null;
		
		if (nestedState != null) {
			
//			// Stupid CTM wraps up models and needs to be unwrapped
//			if (nestedState instanceof IExtendedBlockState) {
//				BlockState trueState = ((IExtendedBlockState) nestedState).getClean();
//				if (trueState != null) {
//					nestedState = trueState;
//				}
//			}
			
			nestedModel = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(nestedState);
		}
		
		return nestedModel == null ? missing : nestedModel;
	}
	
	protected @Nonnull IBakedModel getModelToRender(@Nullable IModelData extraData) {
		return getModelToRender(getNestedState(extraData));
	}
	
	//////Things based on wrapped model
	@Override
	public TextureAtlasSprite getParticleTexture(IModelData data) {
		return getModelToRender(data).getParticleTexture(data);
	}
	
	protected static final List<BakedQuad> EmptyQuads = new ArrayList<>(0);
	
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, Direction side, Random rand, IModelData extraData) {
		// Note: block says "render me in ALL layers" so that this func gets called in each layer.
		// And this model then checks the wrapped model and sees if it should be rendered in the current layer.
		
		BlockState nested = getNestedState(extraData);
		if (nested == null) {
			return this.undisguisedModel.getQuads(state, side, rand, extraData);
		}else if (RenderTypeLookup.canRenderInLayer(state, MinecraftForgeClient.getRenderLayer())) {
			return getModelToRender(nested).getQuads(nested, side, rand, extraData);
		} else {
			return EmptyQuads;
		}
	}
	
	////// Hardcoded things based on base undisguised model
	// Couldn't I just override getBakedModel ?
	@Override
	public boolean isAmbientOcclusion() {
		return this.undisguisedModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return this.undisguisedModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return this.undisguisedModel.isBuiltInRenderer();
	}
	
	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return this.undisguisedModel.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return this.undisguisedModel.getOverrides();
	}
	
	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand) {
		// Show undisguised state
		return undisguisedModel.getQuads(state, side, rand);
	}
	
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return undisguisedModel.getParticleTexture();
	}

	@Override
	public boolean isSideLit() {
		return true; // I think this is "!guiLight" aka "is this lit like a block instead of flat like a GUI item?"
	}
}
