package com.smanzana.nostrummagica.client.model;

import com.smanzana.nostrummagica.entity.LuxEntity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class LuxModel extends HierarchicalModel<LuxEntity> {
	
	private static final String MESH_CORE = "core";
	private static final String MESH_LIP = "lip";
	
	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		root.addOrReplaceChild(MESH_CORE,
				CubeListBuilder.create().addBox(-2f, -16f, -2f, 4, 32, 4),
				PartPose.offset(0, 0, 0)
		);
		root.addOrReplaceChild(MESH_LIP,
				CubeListBuilder.create().addBox(-4, -2, -4, 8, 4, 8),
				PartPose.offset(0, 0, 0)
		);
		
		
		return LayerDefinition.create(mesh, 64, 64);
	}
	
	private ModelPart main;
	
	public LuxModel(ModelPart root) {
//		main = new ModelPart(this);
//		main.setTexSize(64, 64);
//
//		main.y = 0;
//		main.addBox(-2f, -16f, -2f, 4, 32, 4);
//		main.addBox(-4, -2, -4, 8, 4, 8);
		main = root;
	}
	
	private float getSwingRot(float swingProgress) {
		return (float) (Math.sin(Math.PI * 2 * swingProgress) * Math.PI * .166666);
	}

	@Override
	public void setupAnim(LuxEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		if (!entityIn.isRoosting()) {
			final float angle = getSwingRot(entityIn.getAttackAnim(ageInTicks % 1f));
			main.zRot = angle;
		}
		
	}

	@Override
	public ModelPart root() {
		return main;
	}
	
}
