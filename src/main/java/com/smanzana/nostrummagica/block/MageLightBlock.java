package com.smanzana.nostrummagica.block;

import java.util.Random;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MageLightBlock extends Block {

	public static final String ID = "mage_light";
	protected static final VoxelShape AABB = Block.box(4, 4, 4, 12, 12, 12);
	
	public MageLightBlock() {
		super(Block.Properties.of(Material.DECORATION)
				.noCollission()
				.instabreak()
				.lightLevel(s -> 13)
				);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AABB;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
        NostrumParticles.GLOW_ORB.spawn(worldIn, new SpawnParams(
				1,
				pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, .01, 30, 10,
				new Vec3(0, 0, 0), new Vec3(.005, .005, .005)
				).color(0xFFFFDF93));
	}
}
