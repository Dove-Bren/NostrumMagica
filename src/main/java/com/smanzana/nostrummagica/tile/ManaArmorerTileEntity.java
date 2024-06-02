package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ManiCrystal;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams.TargetBehavior;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class ManaArmorerTileEntity extends TileEntity implements ITickableTileEntity {
	
	private static final int MAX_CRYSTALS = 8;
	private static final int MAX_CRYSTAL_RADIUS = 8;
	private static final int MANA_PER_CRYSTAL = 1;
	private static final int MANA_PER_DIRECT = 1;
	private static final int MANA_DRAIN_PER_TICK = 1;
	private static final int MAX_ENTITY_RADIUS = 8;
	
	private @Nullable LivingEntity activeEntity;
	private int currentMana;
	private int targetMana;
	private List<BlockPos> activeCrystals;
	
	protected int ticksExisted;
	protected float rotationProg;
	
	public ManaArmorerTileEntity() {
		super(NostrumTileEntities.ManaArmorerTileEntityType);
		this.activeCrystals = new ArrayList<>(MAX_CRYSTALS);
		this.ticksExisted = 0;
		this.rotationProg = 0;
	}
	
	@Override
	public double getMaxRenderDistanceSquared() {
		return super.getMaxRenderDistanceSquared();
	}
	
	public boolean isActive() {
		return getActiveEntity() != null;
	}
	
	public @Nullable LivingEntity getActiveEntity() {
		return this.activeEntity;
	}
	
	public int getCurrentMana() {
		return this.currentMana;
	}
	
	public int getTargetMana() {
		return this.targetMana;
	}
	
	public float getManaProgress() {
		return this.getTargetMana() == 0
				? 0f
				: ((float) this.getCurrentMana()) / ((float) this.getTargetMana());
	}
	
	public List<BlockPos> getLinkedCrystals() {
		return activeCrystals;
	}
	
	protected void addMana(int mana) {
		this.currentMana += mana;
	}
	
	public int getTicksExisted() {
		return this.ticksExisted;
	}
	
	/**
	 * Get amount (from 0 to 1) the entity should be rotated.
	 * Stored here because we speed up and slow down based on progress, so there's not simple
	 * calc to perform based on ticks existed.
	 * @param partialTicks
	 * @return
	 */
	public float getRenderRotation(float partialTicks) {
		return this.rotationProg + getRotationForThisTick(partialTicks);
	}
	
	// Tick and mechanics
	public void startEntity(LivingEntity entity) {
		refreshCrystals();
		this.targetMana = calcTargetMana(entity);
		// Note: don't reset current mana in case the entity is returning
		this.activeEntity = entity;
	}
	
	public void stop() {
		this.activeEntity = null;
	}
	
	protected void onLoseEntity(LivingEntity oldEntity) {
		
		this.dirty();
	}
	
	protected void onFinish(LivingEntity entity, int mana) {
		this.currentMana = 0;
		
		IManaArmor armor = NostrumMagica.getManaArmor(entity);
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (armor != null && attr != null) {
			// Re-check requirement before continuing in case player's mana has changed!
			if (attr.getMaxMana() > this.calcManaBurnAmt(entity)) {
				armor.setHasArmor(true, calcManaBurnAmt(entity));
				if (entity instanceof ServerPlayerEntity) {
					NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity)entity);
				}
			}
		}
		
		doFinishEffect(entity, mana);
		this.dirty();
	}
	
	protected void onManaExhausted() {
		this.dirty();
	}
	
	public int calcTargetMana(LivingEntity entity) {
		// Could look for ritual augments like special crystals and blocks
		return 500 * 2;
	}
	
	public int calcManaBurnAmt(LivingEntity entity) {
		// Could look at surrounding blocks for augments that make this cheaper
		return 500;
	}
	
	protected boolean entityIsValid(LivingEntity entity) {
		return entity != null
				&& entity.isAlive()
				&& entity.getDistanceSq(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= (MAX_ENTITY_RADIUS * MAX_ENTITY_RADIUS)
				;
	}
	
	protected int drainEntity(LivingEntity entity, int desiredMana) {
		int drained = 0;
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null && attr.isUnlocked()) {
			if (attr.getMana() >= desiredMana) {
				drained = desiredMana;
				attr.addMana(-desiredMana);
			} else {
				drained = attr.getMana();
				attr.addMana(-attr.getMana());
			}
		}
		
		return drained;
	}
	
	protected int getManaDrainAmt(LivingEntity entity) {
		final int draw = MANA_PER_DIRECT
				+ MANA_PER_CRYSTAL * this.getLinkedCrystals().size();
		final int room = this.getTargetMana() - this.getCurrentMana();
		return Math.min(draw, room);
	}
	
	protected boolean isCrystal(@Nullable BlockState state) {
		return state != null
				&& state.getBlock() instanceof ManiCrystal;
	}
	
	private void scanCrystalLayer(Set<BlockPos> positions, int radius) {
		// Lazy way; just make sure one coord is == radius
		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int x = -radius; x <= radius; x++)
		for (int y = -radius; y <= radius; y++)
		for (int z = -radius; z <= radius; z++) {
			final boolean outer = x == radius || x == -radius
					|| y == radius || y == -radius
					|| z == radius || z == -radius
					;
			if (!outer) {
				continue;
			}
			
			BlockPos center = this.getPos();
			pos.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
			
			if (pos.getY() <= 0 || pos.getY() >= this.world.getHeight()) {
				continue;
			}
					
			BlockState state = world.getBlockState(pos);
			if (isCrystal(state)) {
				positions.add(pos.toImmutable());
			}
		}
	}
	
	protected void refreshCrystals() {
		// Scan out from center to give precedence to nearer crystals and to stop early if we hit max
		this.activeCrystals.clear();
		Set<BlockPos> candidates = new HashSet<>(MAX_CRYSTALS);
		for (int i = 1; i <= MAX_CRYSTAL_RADIUS && this.activeCrystals.size() < MAX_CRYSTALS; i++) {
			candidates.clear();
			this.scanCrystalLayer(candidates, i);
			
			for (BlockPos candidate : candidates) {
				this.activeCrystals.add(candidate);
				if (this.activeCrystals.size() == MAX_CRYSTALS) {
					break;
				}
			}
		}
	}
	
	protected void doManaEffectDirect(LivingEntity entity) {
		NostrumParticles.FILLED_ORB.spawn(entity.world, new SpawnParams(
				1,
				entity.getPosX(), entity.getPosY() + entity.getEyeHeight() / 2f, entity.getPosZ(), .25,
				40, 0,
				new Vector3d(pos.getX() + .5, pos.getY() + .75, pos.getZ() + .5)
				).color(0xFF5511FF));
	}
	
	protected void doManaEffectCrystal(LivingEntity entity, BlockPos crystal) {
		BlockState state = entity.world.getBlockState(crystal);
		Vector3d offset = NostrumBlocks.maniCrystalBlock.getCrystalTipOffset(state);
		Vector3d crystalPos = new Vector3d(crystal.getX() + offset.x, crystal.getY() + offset.y, crystal.getZ() + offset.z);
		
		NostrumParticles.FILLED_ORB.spawn(entity.world, new SpawnParams(
				1,
				entity.getPosX(), entity.getPosY() + entity.getEyeHeight() / 2f, entity.getPosZ(), .25,
				40, 0,
				crystalPos
				).color(0x805511FF));
		
		NostrumParticles.FILLED_ORB.spawn(entity.world, new SpawnParams(
				1,
				crystalPos.x, crystalPos.y, crystalPos.z, 0,
				40, 0,
				new Vector3d(pos.getX() + .5, pos.getY() + .75, pos.getZ() + .5)
				).color(0xFF5511FF));
	}
	
	protected void doFinishEffect(LivingEntity entity, int manaUsed) {
		NostrumMagicaSounds.AMBIENT_WOOSH2.play(this.world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		
		NostrumParticles.FILLED_ORB.spawn(entity.world, new SpawnParams(
				100,
				pos.getX() + .5, pos.getY() + .75, pos.getZ() + .5, 0,
				40, 20,
				entity.getEntityId()
				).color(0xFF5511FF)
				.setTargetBehavior(TargetBehavior.JOIN));
		
		NostrumParticles.WARD.spawn(entity.world, new SpawnParams(
				100,
				entity.getPosX(), entity.getPosY() + entity.getEyeHeight() / 2f, entity.getPosZ(), 1,
				40, 20,
				Vector3d.ZERO, new Vector3d(0, .01, 0)
				).color(0xFF5511FF));
	}
	
	protected void checkEntity() {
		if (this.activeEntity != null) {
			if (!entityIsValid(this.activeEntity)) {
				LivingEntity old = this.activeEntity;
				this.activeEntity = null;
				this.onLoseEntity(old);
			}
		}
	}
	
	protected void inactiveTick() {
		if (this.currentMana > 0) {
			this.currentMana = Math.max(0, this.currentMana - MANA_DRAIN_PER_TICK);
			if (this.currentMana <= 0) {
				onManaExhausted();
			}
			
			; // Could do display thing
		}
	}
	
	protected void activeTick() {
		
		if (this.ticksExisted % 10 == 0) {
			refreshCrystals();
		
			int draw = getManaDrainAmt(this.activeEntity);
			int drawn = this.drainEntity(this.activeEntity, draw);
			this.addMana(drawn);
			if (this.getCurrentMana() >= this.getTargetMana()) {
				this.onFinish(this.getActiveEntity(), this.getCurrentMana());
				this.activeEntity = null;
				this.currentMana = 0;
			} else if (drawn > 0) {
				// Might be too spammy....
				doManaEffectDirect(this.getActiveEntity());
				
				// For every chunk of mana above, show mana coming from a crystal
				final int crystalsUsed = ((drawn - MANA_PER_DIRECT) / MANA_PER_CRYSTAL);
				for (int i = 0; i < crystalsUsed && i < this.getLinkedCrystals().size(); i++) {
					doManaEffectCrystal(this.getActiveEntity(), this.getLinkedCrystals().get(i));
				}
			}
		}
	}
	
	protected float getRotationForThisTick(float partialTick) {
		// Spin some amount based on mana
		final float prog = this.getManaProgress();
		
		// Pick some amount of rotations to spin based on prog
		final float revsPerSecondFull = 2f;
		final float revsPerTickFull = revsPerSecondFull / 20f;
		
		final float revsPerTick = revsPerTickFull * prog; // Scale down to where we're at
		
		return revsPerTick * partialTick;
	}
	
	protected void clientTick() {
		if (this.currentMana > 0) {
			this.rotationProg += getRotationForThisTick(1f);
			this.rotationProg %= 1f; // Otherwise gets too big and we lose precision :P
			
			if (this.ticksExisted % 20 == 1) {
				// play sound!
				
				float freq = .5f + .5f * this.getManaProgress();
				world.playSound(NostrumMagica.instance.proxy.getPlayer(), pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, SoundCategory.BLOCKS,
						.25f, freq + .5f);
				world.playSound(NostrumMagica.instance.proxy.getPlayer(), pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.BLOCKS,
						.25f, freq + .25f);
			}
		} else if (this.rotationProg > 0f) {
			// Return back to 0
			if (this.rotationProg < .01f) {
				this.rotationProg = 0f;
			} else {
				this.rotationProg = this.rotationProg * .8f;
			}
		}
	}

	@Override
	public void tick() {
		this.ticksExisted++;
		
		if (!world.isRemote) {
			checkEntity();
			if (this.isActive()) {
				activeTick();
			} else {
				inactiveTick();
			}
			
			if (this.ticksExisted % 10 == 0
					&& this.currentMana > 0) {
				this.dirty();
			}
		} else {
			clientTick();
		}
	}
	
	
	// Serialization
	
	private static final String NBT_ENTITY_ID = "entity_id";
	private static final String NBT_MANA = "mana";
	private static final String NBT_TARGET_MANA = "target_mana";
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		if (this.getActiveEntity() != null) {
			nbt.putUniqueId(NBT_ENTITY_ID, this.getActiveEntity().getUniqueID());
		}
		
		nbt.putInt(NBT_MANA, this.getCurrentMana());
		nbt.putInt(NBT_TARGET_MANA, this.getTargetMana());
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		if (nbt == null)
			return;
		
		this.targetMana = nbt.getInt(NBT_TARGET_MANA);
		this.currentMana = nbt.getInt(NBT_MANA);
		
		if (nbt.hasUniqueId(NBT_ENTITY_ID)) {
			UUID id = nbt.getUniqueId(NBT_ENTITY_ID);
			if (id != null && this.world != null) {
				PlayerEntity player = this.world.getPlayerByUuid(id);
				this.activeEntity = player;
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
	
	private void dirty() {
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}
	
}