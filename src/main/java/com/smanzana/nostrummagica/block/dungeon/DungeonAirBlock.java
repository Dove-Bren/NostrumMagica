package com.smanzana.nostrummagica.block.dungeon;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.util.ShapeUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DungeonAirBlock extends HalfTransparentBlock {

	public static final String ID = "dungeon_air";
	
	public DungeonAirBlock() {
		super(Block.Properties.of(Material.BARRIER)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.noCollission()
				);
	}
	
	// GetHowMuchLightGoesThrough?? Not sure.
	@Override
	@OnlyIn(Dist.CLIENT)
	public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 1.0F;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		super.animateTick(stateIn, worldIn, pos, rand);
		
		if (rand.nextBoolean() && rand.nextBoolean()
				&& rand.nextBoolean() && rand.nextBoolean()
				 && rand.nextBoolean() && rand.nextBoolean()) {
			final float brightness = rand.nextFloat();
			final float alpha = rand.nextFloat();
			final int color = 0x40200020
					+ (((int) (alpha * 40f) & 0xFF) << 24)
					+ (((int) (brightness * 60f) & 0xFF) << 16)
					+ (((int) (brightness * 60f) & 0xFF) << 0);
			
			NostrumParticles.GLOW_ORB.spawn(worldIn, new SpawnParams(
					1,
					pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, .5, 40, 20,
					new Vec3(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05 - .025, rand.nextFloat() * .05 - .025), null
					).color(color));
		}
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
		if (context != CollisionContext.empty() && context instanceof EntityCollisionContext) {
			@Nullable Entity entity = ((EntityCollisionContext) context).getEntity();
			if (entity == null || !(entity instanceof Player) || !((Player) entity).isCreative()) {
				return ShapeUtil.EMPTY_NOCRASH;
			}
		}
		
		return Shapes.block();
	}

	public int getOverlayColor(BlockState inBlock) {
		return 0x304D004D;
	}
}
