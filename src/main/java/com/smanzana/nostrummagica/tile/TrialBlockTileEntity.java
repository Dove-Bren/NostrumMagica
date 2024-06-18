package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.TrialBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.trial.CombatTrial;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class TrialBlockTileEntity extends TileEntity implements ITickableTileEntity {
	
	private static final String NBT_ELEMENT = "element";
	
	private @Nullable CombatTrial activeTrial;
	private @Nullable PlayerEntity activeTrialPlayer;
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
	public double getMaxRenderDistanceSquared() {
		return super.getMaxRenderDistanceSquared();
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
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.put(NBT_ELEMENT, this.getElement().toNBT());
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		this.element = EMagicElement.FromNBT(nbt.get(NBT_ELEMENT));
	}
	
	protected CombatTrial findTrial(EMagicElement element, @Nullable PlayerEntity starter) {
		return CombatTrial.CreateForElement(element, (ServerWorld) this.world, this.pos, starter);
	}
	
	public void startTrial(@Nullable PlayerEntity starter) {
		if (!world.isRemote()) {
			this.startTrial(findTrial(this.getElement(), starter), starter);
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
				new Vector3d(0, .1, 0), new Vector3d(.1, .1, .1)
				).gravity(.05f).color(this.getElement().getColor()));
	}
	
	protected void awardTrialRewards(PlayerEntity player) {
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
		((ServerWorld) world).addEntity(
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
				new Vector3d(pos.getX() + .5, pos.getY() + 1.25, pos.getZ() + .5)
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
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());
	}
	
	protected void dirty() {
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}
	
}