package com.smanzana.nostrummagica.client.render.tile;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class BlockEntityRendererBase<T extends BlockEntity> implements BlockEntityRenderer<T> {
	
	protected final BlockEntityRendererProvider.Context context;
	
	public BlockEntityRendererBase(BlockEntityRendererProvider.Context context) {
		this.context = context;
	}

}
