package com.smanzana.nostrummagica.trial;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

/**
 * Series of combat waves that must be overcome to complete a trial.
 * Unock the 'world trials' these are instanced PER trial run and are run by a TrailBlock/TileEntity.
 * @author Skyler
 *
 */
public abstract class CombatTrial {

	protected final ServerWorld world;
	protected final BlockPos center;
	protected final @Nullable PlayerEntity focusedPlayer;
	
	protected CombatTrial(ServerWorld world, BlockPos center, @Nullable PlayerEntity focusedPlayer) {
		this.world = world;
		this.center = center;
		this.focusedPlayer = focusedPlayer;
	}
	
	/**
	 * Perform any setup to start the trial and get things going
	 */
	public abstract void startTrial();
	
	/**
	 * End the trial, including making isComplete() start returning true;
	 */
	public abstract void endTrial();
	
	public void trialTick() {
		if (!playerIsNear()) {
			if (this.focusedPlayer != null) {
				focusedPlayer.sendMessage(new TranslationTextComponent("info.trial.fail"), Util.DUMMY_UUID);
			}
			endTrial();
		}
	}
	
	public abstract boolean isComplete();
	
	public abstract boolean wasSuccess();
	
	protected double playerRange() {
		return 30;
	}
	
	protected boolean playerIsNear() {
		if (this.focusedPlayer == null) {
			// Just check that ANY player is near
			return world.isPlayerWithin(center.getX() + .5,  center.getY(), center.getZ() + .5, playerRange());
		} else {
			return focusedPlayer.isAlive() && focusedPlayer.getDistanceSq(center.getX() + .5,  center.getY(), center.getZ() + .5) < playerRange() * playerRange();
		}
	}
	
	protected static final void playSpawnEffects(BlockPos center, LivingEntity entity) {
		((MobEntity)entity).spawnExplosionParticle();
		
		NostrumParticles.GLOW_ORB.spawn(entity.world, new SpawnParams(
				10, center.getX() + .5, center.getY() + 1.25, center.getZ() + .5, .25,
				60, 10,
				entity.getEntityId()
				).dieOnTarget(true));
	}
	
	public static final CombatTrial CreateForElement(EMagicElement element, ServerWorld world, BlockPos center, @Nullable PlayerEntity focusedPlayer) {
		switch (element) {
		case EARTH:
			return new CombatTrialEarth(world, center, focusedPlayer);
		case ENDER:
			return new CombatTrialEnder(world, center, focusedPlayer);
		case FIRE:
			return new CombatTrialFire(world, center, focusedPlayer);
		case ICE:
			return new CombatTrialIce(world, center, focusedPlayer);
		case LIGHTNING:
			return new CombatTrialLightning(world, center, focusedPlayer);
		case PHYSICAL:
			return new CombatTrialPhysical(world, center, focusedPlayer);
		case WIND:
			return new CombatTrialWind(world, center, focusedPlayer);
		}
		
		return null;
	}
}
