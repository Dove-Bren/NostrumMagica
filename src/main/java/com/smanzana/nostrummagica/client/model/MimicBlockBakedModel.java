package com.smanzana.nostrummagica.client.model;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.MimicBlock;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class MimicBlockBakedModel implements IBakedModel {

	private final TextureAtlasSprite particle;
	
	public MimicBlockBakedModel() {
		particle = Minecraft.getInstance().getTextureMapBlocks().getAtlasSprite(new ResourceLocation(NostrumMagica.MODID, "blocks/mimic_facade").toString());
	}
	
	protected IBlockState getNestedState(@Nullable IBlockState state) {
		if (state != null) {
			IExtendedBlockState ex = (IExtendedBlockState) state;
			IBlockState nestedState = ex.getValue(MimicBlock.NESTED_STATE);
			
			while (nestedState instanceof IExtendedBlockState && nestedState.getBlock() instanceof MimicBlock) {
				nestedState = ((IExtendedBlockState)nestedState).getValue(MimicBlock.NESTED_STATE);
			}
			
			if (nestedState != null) {
				return nestedState;
			}
		}
		
		return null;
	}
	
	protected IBakedModel getModelToRender(@Nullable IBlockState nestedState) {
		IBakedModel missing = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
		IBakedModel nestedModel = null;
		
		if (nestedState != null) {
			
			// Stupid CTM wraps up models and needs to be unwrapped
			if (nestedState instanceof IExtendedBlockState) {
				IBlockState trueState = ((IExtendedBlockState) nestedState).getClean();
				if (trueState != null) {
					nestedState = trueState;
				}
			}
			
			nestedModel = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(nestedState);
		}
		
		return nestedModel == null ? missing : nestedModel;
	}
	
	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, Direction side, long rand) {
		IBlockState nested = getNestedState(state);
		return getModelToRender(nested).getQuads(nested, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return particle;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}

}
