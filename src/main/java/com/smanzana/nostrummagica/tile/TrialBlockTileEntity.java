package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.TrialBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.TameLightning;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.trial.CombatTrial;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

public class TrialBlockTileEntity extends BlockEntity implements TickableBlockEntity {
	
	private static final String NBT_ELEMENT = "element";
	
	private @Nullable CombatTrial activeTrial;
	private @Nullable Player activeTrialPlayer;
	private EMagicElement element;
	private float scale;
	
	// Tracks ticks after a trial is started, used for delaying startup and effects
	private int trialTicks;
	
	public TrialBlockTileEntity() {
		super(NostrumTileEntities.TrialBlockEntityType);
		setScale(1f);
		
		activeTrial = null;
		activeTrialPlayer = null;
		trialTicks = 0;
		element = EMagicElement.PHYSICAL;
	}
	
	public float getScale() {
		return scale;
	}
	
	protected void setScale(float scale) {
		this.scale = scale;
	}
	
	@Override
	public double getViewDistance() {
		return super.getViewDistance();
	}
	
	public void setElement(EMagicElement element) {
		stopTrial(false);
		this.element = element;
		dirty();
	}
	
	// Ease of use
	public EMagicElement getElement() {
		return element;
	}
	
	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt = super.save(nbt);
		
		nbt.put(NBT_ELEMENT, this.getElement().toNBT());
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundTag nbt) {
		super.load(state, nbt);
		
		this.element = EMagicElement.FromNBT(nbt.get(NBT_ELEMENT));
	}
	
	protected CombatTrial findTrial(EMagicElement element, @Nullable Player starter) {
		return CombatTrial.CreateForElement(element, (ServerLevel) this.level, this.worldPosition, starter);
	}
	
	public void startTrial(@Nullable Player starter) {
		if (!level.isClientSide()) {
			this.startTrial(findTrial(this.getElement(), starter), starter);
		}
	}
	
	public boolean isTrialActive() {
		return activeTrial != null;
	}
	
	protected void onTrialEnd(CombatTrial trial, Player player, boolean success) {
		if (success) {
			playSuccessEffects(player);
			awardTrialRewards(player);
		} else {
			playFailEffects(player);
		}
	}
	
	protected void playFailEffects(Player player) {
		NostrumMagicaSounds.CAST_FAIL.play((Entity) player);
	}
	
	protected void playSuccessEffects(Player player) {
		NostrumMagicaSounds.LEVELUP.play((Entity) player);
		// Message done in attr
		//player.sendMessage(new TranslationTextComponent("info.element.mastery" + mastery.intValue(), new Object[] {this.element.getName()}));
		TrialBlock.DoEffect(worldPosition, player, this.getElement().getColor());
	}
	
	protected void playStartEffects(Player player) {
		level.playSound(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1f, 2f);
		
		NostrumParticles.GLOW_ORB.spawn(level, new SpawnParams(
				100,
				worldPosition.getX() + .5, worldPosition.getY() + 1.25, worldPosition.getZ() + .5, .1,
				60, 10,
				new Vec3(0, .1, 0), new Vec3(.1, .1, .1)
				).gravity(.05f).color(this.getElement().getColor()));
	}
	
	protected void awardTrialRewards(Player player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		final EElementalMastery currentMastery = attr.getElementalMastery(this.getElement());
		final EElementalMastery newMastery;
		switch (currentMastery) {
		case MASTER:
		default:
			newMastery = currentMastery; // Shouldn't have gotten here
			break;
		case UNKNOWN:
		case NOVICE:
			newMastery = EElementalMastery.ADEPT;
			break;
		case ADEPT:
			newMastery = EElementalMastery.MASTER;
			break;
		}
		
		attr.endTrial(this.getElement());
		attr.setElementalMastery(this.getElement(), newMastery);
		NostrumMagica.instance.proxy.syncPlayer((ServerPlayer) player);
	}
	
	protected void stopTrial(boolean success) {
		if (isTrialActive()) {
			CombatTrial stoppedTrial = activeTrial;
			Player stoppedPlayer = activeTrialPlayer;
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
	
	protected void startTrial(CombatTrial trial, Player player) {
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
		((ServerLevel) level).addFreshEntity(
				(new TameLightning(NostrumEntityTypes.tameLightning, level, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5))
				);
	}
	
	protected void trialStartupTick() {
		if (Math.abs(this.trialTicks) % 20 == 0) {
			spawnStartupWarning();
		}
		
		NostrumParticles.FILLED_ORB.spawn(level, new SpawnParams(
				(60 - this.trialTicks) / 10,
				worldPosition.getX() + .5, worldPosition.getY() + 1.25, worldPosition.getZ() + .5, 5,
				40, 10,
				new Vec3(worldPosition.getX() + .5, worldPosition.getY() + 1.25, worldPosition.getZ() + .5)
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
		
		if (!level.isClientSide() && isTrialActive()) {
			trialTick();
			
			if (activeTrial.isComplete()) {
				this.stopTrial(activeTrial.wasSuccess());
			}
		}
		
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getTag());
	}
	
	protected void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
}