package com.smanzana.nostrummagica.block.dungeon;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.loretag.LoreRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RootingAirBlock extends DungeonAirBlock {

	public static final String ID = "rooting_air";
	
	public RootingAirBlock() {
		super();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		if (rand.nextBoolean() && rand.nextBoolean()) {
			final float brightness = rand.nextFloat();
			final float alpha = rand.nextFloat();
			final int color = 0x40200020
					+ (((int) (alpha * 40f) & 0xFF) << 24)
					+ (((int) (brightness * 60f) & 0xFF) << 16)
					+ (((int) (brightness * 60f) & 0xFF) << 0);
			
			NostrumParticles.GLOW_ORB.spawn(worldIn, new SpawnParams(
					1,
					pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, .5, 40, 20,
					new Vec3(0, -.1, 0), new Vec3(.01, .025, .01)
					).color(color));
		}
	}

	@Override
	public int getOverlayColor(BlockState inBlock) {
		return 0x2000BD00;
	}
	
	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (entity instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(NostrumEffects.rooted, 20));
			NostrumMagica.awardLore(entity, LoreRegistry.RootingAirLore, true);
		} else {
			entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, 0, 1).add(0, -.2, 0));
		}
	}
	
}
