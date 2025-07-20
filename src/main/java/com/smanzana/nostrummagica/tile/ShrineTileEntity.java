package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.listener.NostrumTutorialClient.ClientTutorial;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.ShrineTriggerEntity;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.TutorialMessage;
import com.smanzana.nostrummagica.progression.tutorial.NostrumTutorial;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.MagicCapability;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class ShrineTileEntity<E extends ShrineTriggerEntity<?>> extends EntityProxiedTileEntity<E> {

	private static final int MAX_HITS = 5;
	private static final String NBT_HITS = "hits"; // Mostly for communicating to client through regular TE send
	
	private int hitCount;
	
	protected ShrineTileEntity(BlockEntityType<? extends ShrineTileEntity<E>> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		hitCount = 0;
	}
	
	@Override
	protected abstract E makeTriggerEntity(Level world, double x, double y, double z);
	
	public int getHitCount() {
		return this.hitCount;
	}
	
	public int getMaxHitCount() {
		return MAX_HITS;
	}
	
	protected void setHitCount(int count) {
		this.hitCount = count;
		this.dirty();
	}
	
	protected abstract int getParticleColor();
	
	public abstract boolean canPlayerSee(INostrumMagic attr, Player player);
	
	public boolean canPlayerSee(Player player) {
		return canPlayerSee(NostrumMagica.getMagicWrapper(player), player);
	}
	
	@Override
	public void trigger(LivingEntity entity, DamageSource source, float damage) {
		if (entity != null && entity instanceof Player && canPlayerSee((Player) entity)) {
			final Vec3 pos = Vec3.atLowerCornerOf(this.getBlockPos()).add(getEntityOffset());
			final float yOffset = (this.getTriggerEntity() == null ? 0 : this.getTriggerEntity().getBbHeight()/2f);
			final int origHitCount = getHitCount();
			if (origHitCount+1 >= MAX_HITS) {
				this.setHitCount(0);
				this.doReward((Player) entity);
				
				NostrumParticles.LIGHT_EXPLOSION.spawn(level, new SpawnParams(
						50, pos.x(), pos.y() + yOffset, pos.z(), 0, 100, 0, Vec3.ZERO, Vec3.ZERO
						).color(getParticleColor()));
			} else {
				this.setHitCount(origHitCount+1);
			}
			
			this.getLevel().playSound(null, pos.x(), pos.y() + yOffset, pos.z(), SoundEvents.ENDER_DRAGON_HURT, SoundSource.BLOCKS, 1f, 1f);
			NostrumParticles.FILLED_ORB.spawn(this.getLevel(), new SpawnParams(30, pos.x(), pos.y() + yOffset, pos.z(), .3,
					40, 20, new Vec3(0, .1, 0), new Vec3(.1, .05, .1)).gravity(true).color(getParticleColor()));
		}
	}
	
	@Override
	public void trigger(LivingEntity caster, SpellEffectPart effect, SpellAction action) {
		this.trigger(caster, null, 0);
	}
	
	protected abstract void doReward(Player player);
	
	@Override
	protected Vec3 getEntityOffset() {
		return new Vec3(.5, 1, .5);
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		
		nbt.putInt(NBT_HITS, this.getHitCount());
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		this.hitCount = nbt.getInt(NBT_HITS);
	}
	
	protected static void DoEffect(BlockPos shrinePos, LivingEntity entity, int color) {
		if (entity.level.isClientSide) {
			return;
		}
		
		NostrumMagicaSounds.LEVELUP.play(entity);
		NostrumParticles.FILLED_ORB.spawn(entity.level, new SpawnParams(
			50,
			shrinePos.getX() + .5, shrinePos.getY() + 1.75, shrinePos.getZ() + .5, 1, 40, 10,
			new TargetLocation(entity, true)
			).setTargetBehavior(TargetBehavior.ORBIT_LAZY).color(color));
	}
	
	public static class Element extends ShrineTileEntity<ShrineTriggerEntity.Element> {
		
		private static final String NBT_ELEMENT = "element";
		
		private EMagicElement element;
		
		public Element(BlockPos pos, BlockState state) {
			super(NostrumBlockEntities.ElementShrine, pos, state);
			this.element = EMagicElement.PHYSICAL;
		}
		
		public EMagicElement getElement() {
			return this.element;
		}
		
		public void setElement(EMagicElement element) {
			this.element = element;
			this.dirty();
		}
		
		@Override
		public void saveAdditional(CompoundTag nbt) {
			super.saveAdditional(nbt);
			
			nbt.put(NBT_ELEMENT, element.toNBT());
		}
		
		@Override
		public void load(CompoundTag nbt) {
			super.load(nbt);
			
			if (nbt.contains(NBT_ELEMENT)) {
				this.element = EMagicElement.FromNBT(nbt.get(NBT_ELEMENT));
			}
		}

		@Override
		protected ShrineTriggerEntity.Element makeTriggerEntity(Level world, double x, double y, double z) {
			ShrineTriggerEntity.Element ent = new ShrineTriggerEntity.Element(NostrumEntityTypes.elementShrine, world);
			ent.setPos(x, y, z);
			return ent;
		}

		@Override
		protected void doReward(Player player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			// Shrine blocks grant novice mastery of their elements
			final EMagicElement element = getElement();
			
			if (attr.getElementalMastery(element) == EElementalMastery.UNKNOWN
					&& NostrumMagica.UnlockElementalMastery(player, this.element, EElementalMastery.NOVICE)) {
				// Just learned!
				final int color = 0x80000000 | (0x00FFFFFF & element.getColor());
				DoEffect(worldPosition, player, color);
				NostrumMagica.Proxy.syncPlayer((ServerPlayer) player);
				
				final EElementalMastery mastery = attr.getElementalMastery(element);
				final Component msg = new TranslatableComponent("info.element_mastery." + mastery.getTranslationKey(), element.getDisplayName().copy().withStyle(element.getChatColor()));
				
				((ServerPlayer)player).connection.send(new ClientboundSetSubtitleTextPacket(msg));
				((ServerPlayer)player).connection.send(new ClientboundSetTitleTextPacket(TextComponent.EMPTY));
				
				if (attr.getKnownElements().values().stream().mapToInt(b -> b ? 1 : 0).sum() == 2 && MagicCapability.INCANT_COMPONENT_SELECT.matches(attr)) {
					NetworkHandler.sendTo(new TutorialMessage(NostrumTutorial.FORM_INCANTATION), ((ServerPlayer)player));
				}
			} else {
				player.sendMessage(new TranslatableComponent("info.shrine.seektrial"), Util.NIL_UUID);
			}
		}

		@Override
		protected int getParticleColor() {
			return getElement().getColor();
		}
		
		@Override
		public boolean canPlayerSee(INostrumMagic attr, Player player) {
			return attr != null && !attr.getElementalMastery(this.getElement()).isGreaterOrEqual(EElementalMastery.NOVICE);
		}
	}
	
	public static class Alteration extends ShrineTileEntity<ShrineTriggerEntity.Alteration> {
		
		private static final String NBT_ALTERATION = "alteration";
		
		private EAlteration alteration;
		
		public Alteration(BlockPos pos, BlockState state) {
			super(NostrumBlockEntities.AlterationShrine, pos, state);
			this.alteration = EAlteration.INFLICT;
		}
		
		public EAlteration getAlteration() {
			return this.alteration;
		}
		
		public void setAlteration(EAlteration alteration) {
			this.alteration = alteration;
			this.dirty();
		}
		
		@Override
		public void saveAdditional(CompoundTag nbt) {
			super.saveAdditional(nbt);
			
			nbt.put(NBT_ALTERATION, alteration.toNBT());
		}
		
		@Override
		public void load(CompoundTag nbt) {
			super.load(nbt);
			
			if (nbt.contains(NBT_ALTERATION)) {
				this.alteration = EAlteration.FromNBT(nbt.get(NBT_ALTERATION));
			}
		}

		@Override
		protected ShrineTriggerEntity.Alteration makeTriggerEntity(Level world, double x, double y, double z) {
			ShrineTriggerEntity.Alteration ent = new ShrineTriggerEntity.Alteration(NostrumEntityTypes.alterationShrine, world);
			ent.setPos(x, y, z);
			return ent;
		}

		@Override
		protected void doReward(Player player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			final EAlteration alteration = getAlteration();
			
			if (!attr.getAlterations().getOrDefault(alteration, false)) {
				attr.unlockAlteration(alteration);
				DoEffect(worldPosition, player, 0x80808ABF);
				
				final Component msg = new TranslatableComponent("info.shrine.alteration", alteration.getDisplayName());
				player.sendMessage(msg, Util.NIL_UUID);
				NostrumMagica.Proxy.syncPlayer((ServerPlayer) player);
				
				((ServerPlayer)player).connection.send(new ClientboundSetSubtitleTextPacket(msg));
				((ServerPlayer)player).connection.send(new ClientboundSetTitleTextPacket(TextComponent.EMPTY));
			}
		}

		@Override
		protected int getParticleColor() {
			return 0xFF808ABF;
		}

		@Override
		public boolean canPlayerSee(INostrumMagic attr, Player player) {
			return attr != null && !attr.getAlterations().getOrDefault(this.getAlteration(), false);
		}
	}
	
	public static class Shape extends ShrineTileEntity<ShrineTriggerEntity.Shape> {
		
		private static final String NBT_SHAPE = "shape";
		
		private SpellShape shape;
		
		public Shape(BlockPos pos, BlockState state) {
			super(NostrumBlockEntities.ShapeShrine, pos, state);
			this.shape = null;
		}
		
		public SpellShape getShape() {
			if (this.shape == null) {
				this.shape = SpellShape.getAllShapes().iterator().next();
			}
			return this.shape;
		}
		
		public void setShape(SpellShape shape) {
			this.shape = shape;
			this.dirty();
		}
		
		@Override
		public void saveAdditional(CompoundTag nbt) {
			super.saveAdditional(nbt);
			
			if (getShape() != null) {
				nbt.putString(NBT_SHAPE, getShape().getShapeKey());
			}
		}
		
		@Override
		public void load(CompoundTag nbt) {
			super.load(nbt);
			
			if (nbt.contains(NBT_SHAPE)) {
				this.shape = SpellShape.get(nbt.getString(NBT_SHAPE));
			}
		}

		@Override
		protected ShrineTriggerEntity.Shape makeTriggerEntity(Level world, double x, double y, double z) {
			ShrineTriggerEntity.Shape ent = new ShrineTriggerEntity.Shape(NostrumEntityTypes.shapeShrine, world);
			ent.setPos(x, y, z);
			return ent;
		}

		@Override
		protected void doReward(Player player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			final SpellShape shape = getShape();
			
			if (!attr.getShapes().contains(shape)) {
				attr.addShape(shape);
				DoEffect(worldPosition, player, 0x8080C0A0);
				final Component msg = new TranslatableComponent("info.shrine.shape", new Object[] {shape.getDisplayName()});
				player.sendMessage(msg, Util.NIL_UUID);
				NostrumMagica.Proxy.syncPlayer((ServerPlayer) player);
				
				((ServerPlayer)player).connection.send(new ClientboundSetSubtitleTextPacket(msg));
				((ServerPlayer)player).connection.send(new ClientboundSetTitleTextPacket(TextComponent.EMPTY));
			}
		}

		@Override
		protected int getParticleColor() {
			return 0xFF80C0A0;
		}

		@Override
		public boolean canPlayerSee(INostrumMagic attr, Player player) {
			return attr != null && !attr.getShapes().contains(this.getShape());
		}
	}
	
	public static class Tier extends ShrineTileEntity<ShrineTriggerEntity.Tier> {
		
		private static final String NBT_TIER = "tier";
		
		private EMagicTier tier;
		
		public Tier(BlockPos pos, BlockState state) {
			super(NostrumBlockEntities.TierShrine, pos, state);
			this.tier = EMagicTier.LOCKED;
		}
		
		public EMagicTier getTier() {
			return this.tier;
		}
		
		public void setTier(EMagicTier tier) {
			this.tier = tier;
			this.dirty();
		}
		
		@Override
		public void saveAdditional(CompoundTag nbt) {
			super.saveAdditional(nbt);
			
			nbt.put(NBT_TIER, getTier().toNBT());
		}
		
		@Override
		public void load(CompoundTag nbt) {
			super.load(nbt);
			this.tier = EMagicTier.FromNBT(nbt.get(NBT_TIER));
		}

		@Override
		protected ShrineTriggerEntity.Tier makeTriggerEntity(Level world, double x, double y, double z) {
			ShrineTriggerEntity.Tier ent = new ShrineTriggerEntity.Tier(NostrumEntityTypes.tierShrine, world);
			ent.setPos(x, y, z);
			return ent;
		}

		@Override
		protected void doReward(Player player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			final EMagicTier tier = getTier();
			
			if (!attr.getTier().isGreaterOrEqual(tier)) {
				attr.setTier(tier);
				DoEffect(worldPosition, player, 0x80666666);
				final Component msg = new TranslatableComponent("info.shrine.tier", tier.getName());
				player.sendMessage(msg, Util.NIL_UUID);
				NostrumMagica.Proxy.syncPlayer((ServerPlayer) player);
				
				((ServerPlayer)player).connection.send(new ClientboundSetSubtitleTextPacket(msg));
				((ServerPlayer)player).connection.send(new ClientboundSetTitleTextPacket(TextComponent.EMPTY));
				
				if (tier.isGreaterOrEqual(EMagicTier.VANI)) {
					int unused; // should be save tutorial
					NetworkHandler.sendTo(new TutorialMessage(NostrumTutorial.OVERCHARGE), ((ServerPlayer)player));
				} else if (tier.isGreaterOrEqual(EMagicTier.KANI)) {
					NetworkHandler.sendTo(new TutorialMessage(NostrumTutorial.QUICK_INCANT), ((ServerPlayer)player));
				}
			}
		}

		@Override
		protected int getParticleColor() {
			return 0xFF666666;
		}

		@Override
		public boolean canPlayerSee(INostrumMagic attr, Player player) {
			return attr != null && !attr.getTier().isGreaterOrEqual(getTier());
		}
	}
	
}