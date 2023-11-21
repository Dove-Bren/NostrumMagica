package com.smanzana.nostrummagica.tiles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.TrialBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.INostrumMagic.ElementalMastery;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.trials.CombatTrial;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

public class TrialBlockTileEntity extends SymbolTileEntity implements ITickableTileEntity {
	
	private @Nullable CombatTrial activeTrial;
	private @Nullable PlayerEntity activeTrialPlayer;
	
	// Tracks ticks after a trial is started, used for delaying startup and effects
	private int trialTicks;
	
	public TrialBlockTileEntity() {
		super(NostrumTileEntities.TrialBlockEntityType);
		//setComponent(new SpellComponentWrapper(EMagicElement.PHYSICAL));
		setScale(1f);
		
		activeTrial = null;
		activeTrialPlayer = null;
		trialTicks = 0;
	}
	
	@Override
	public double getMaxRenderDistanceSquared() {
		return super.getMaxRenderDistanceSquared();
	}
	
	@Override
	public void setComponent(SpellComponentWrapper wrapper) {
		if (wrapper.isElement()) {
			stopTrial(false);
			super.setComponent(wrapper);
			dirty();
		}
		// Reject non-elements
	}
	
	// Ease of use
	public EMagicElement getElement() {
		return this.getComponent().getElement();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		; // Anything to persist?
		
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		
		;
	}
	
	protected CombatTrial findTrial(EMagicElement element, @Nullable PlayerEntity starter) {
		return CombatTrial.CreateForElement(element, (ServerWorld) this.world, this.pos, starter);
	}
	
	public void startTrial(@Nullable PlayerEntity starter) {
		if (!world.isRemote()) {
			this.startTrial(findTrial(this.getComponent().getElement(), starter), starter);
		}
	}
	
	public boolean isTrialActive() {
		return activeTrial != null;
	}
	
	protected void onTrialEnd(CombatTrial trial, PlayerEntity player, boolean success) {
		if (success) {
			playSuccessEffects(player);
			awardTrialRewards(player);
		} else {
			playFailEffects(player);
		}
	}
	
	protected void playFailEffects(PlayerEntity player) {
		NostrumMagicaSounds.CAST_FAIL.play((Entity) player);
	}
	
	protected void playSuccessEffects(PlayerEntity player) {
		NostrumMagicaSounds.LEVELUP.play((Entity) player);
		// Message done in attr
		//player.sendMessage(new TranslationTextComponent("info.element.mastery" + mastery.intValue(), new Object[] {this.element.getName()}));
		TrialBlock.DoEffect(pos, player, this.getElement().getColor());
	}
	
	protected void playStartEffects(PlayerEntity player) {
		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.BLOCKS, 1f, 2f);
		
		NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
				100,
				pos.getX() + .5, pos.getY() + 1.25, pos.getZ() + .5, .1,
				60, 10,
				new Vec3d(0, .1, 0), new Vec3d(.1, .1, .1)
				).gravity(.05f).color(this.getElement().getColor()));
	}
	
	protected void awardTrialRewards(PlayerEntity player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		final ElementalMastery currentMastery = attr.getElementalMastery(this.getElement());
		final ElementalMastery newMastery;
		switch (currentMastery) {
		case MASTER:
		default:
			newMastery = currentMastery; // Shouldn't have gotten here
			break;
		case UNKNOWN:
		case NOVICE:
			newMastery = ElementalMastery.ADEPT;
			break;
		case ADEPT:
			newMastery = ElementalMastery.MASTER;
			break;
		}
		
		attr.endTrial(this.getElement());
		attr.setElementalMastery(this.getElement(), newMastery);
		NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) player);
	}
	
	protected void stopTrial(boolean success) {
		if (isTrialActive()) {
			CombatTrial stoppedTrial = activeTrial;
			PlayerEntity stoppedPlayer = activeTrialPlayer;
			activeTrial = null;
			activeTrialPlayer = null;
			trialTicks = 0;
			
			stoppedTrial.endTrial();
			onTrialEnd(stoppedTrial, stoppedPlayer, success);
		}
	}
	
	protected void actuallyStartTrial(CombatTrial trial) {
		trial.startTrial();
		playStartEffects(activeTrialPlayer);
	}
	
	protected void startTrial(CombatTrial trial, PlayerEntity player) {
		if (!isTrialActive()) {
			this.activeTrial = trial;
			this.activeTrialPlayer = player;
			this.trialTicks = -20 * 3;
			
			if (this.activeTrial != null) {
				
			} else {
				// Failed to create!
				NostrumMagica.logger.warn("Failed to create trial! Trial creation failed.");
			}
		}
	}
	
	protected void spawnStartupWarning() {
		((ServerWorld) world).addLightningBolt(
				(new NostrumTameLightning(NostrumEntityTypes.tameLightning, world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5))
				);
	}
	
	protected void trialStartupTick() {
		if (Math.abs(this.trialTicks) % 20 == 0) {
			spawnStartupWarning();
		}
		
		NostrumParticles.FILLED_ORB.spawn(world, new SpawnParams(
				(60 - this.trialTicks) / 10,
				pos.getX() + .5, pos.getY() + 1.25, pos.getZ() + .5, 5,
				40, 10,
				new Vec3d(pos.getX() + .5, pos.getY() + 1.25, pos.getZ() + .5)
				).color(this.getElement().getColor()));
	}
	
	protected boolean isStartingUp() {
		return this.isTrialActive() && this.trialTicks < 0;
	}
	
	protected void trialTick() {
		if (isStartingUp()) {
			trialStartupTick();
		} else {
			if (this.trialTicks == 0) {
				// Just started
				this.actuallyStartTrial(this.activeTrial);
			}
			this.activeTrial.trialTick();
		}
		trialTicks++;
	}
	
	@Override
	public void tick() {
		
		if (!world.isRemote() && isTrialActive()) {
			trialTick();
			
			if (activeTrial.isComplete()) {
				this.stopTrial(activeTrial.wasSuccess());
			}
		}
		
	}
	
}