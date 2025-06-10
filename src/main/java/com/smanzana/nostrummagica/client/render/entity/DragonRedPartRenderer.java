package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.entity.boss.reddragon.RedDragonEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DragonRedPartRenderer extends InvisibleMultiPartEntityRenderer<RedDragonEntity.DragonBodyPart> {

	public DragonRedPartRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
	}
}
