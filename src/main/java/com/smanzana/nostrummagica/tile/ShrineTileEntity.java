package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.ShrineTriggerEntity;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public abstract class ShrineTileEntity<E extends ShrineTriggerEntity<?>> extends EntityProxiedTileEntity<E> {

	private static final int MAX_HITS = 5;
	private static final String NBT_HITS = "hits"; // Mostly for communicating to client through regular TE send
	
	private int hitCount;
	
	protected ShrineTileEntity(TileEntityType<? extends ShrineTileEntity<E>> type) {
		super(type);
		hitCount = 0;
	}
	
	@Override
	protected abstract E makeTriggerEntity(World world, double x, double y, double z);
	
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
	
	public abstract boolean canPlayerSee(INostrumMagic attr, PlayerEntity player);
	
	public boolean canPlayerSee(PlayerEntity player) {
		return canPlayerSee(NostrumMagica.getMagicWrapper(player), player);
	}
	
	@Override
	public void trigger(LivingEntity entity, DamageSource source, float damage) {
		if (entity != null && entity instanceof PlayerEntity && canPlayerSee((PlayerEntity) entity)) {
			final int origHitCount = getHitCount();
			if (origHitCount+1 >= MAX_HITS) {
				this.setHitCount(0);
				this.doReward((PlayerEntity) entity);
			} else {
				this.setHitCount(origHitCount+1);
			}
			final Vector3d pos = Vector3d.copy(this.getPos()).add(getEntityOffset());
			final float yOffset = (this.getTriggerEntity() == null ? 0 : this.getTriggerEntity().getHeight()/2f);
			this.getWorld().playSound(null, pos.getX(), pos.getY() + yOffset, pos.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_HURT, SoundCategory.BLOCKS, 1f, 1f);
			NostrumParticles.FILLED_ORB.spawn(this.getWorld(), new SpawnParams(30, pos.getX(), pos.getY() + yOffset, pos.getZ(), .3,
					40, 20, new Vector3d(0, .1, 0), new Vector3d(.1, .05, .1)).gravity(true).color(getParticleColor()));
		}
	}
	
	protected abstract void doReward(PlayerEntity player);
	
	@Override
	protected Vector3d getEntityOffset() {
		return new Vector3d(.5, 1, .5);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.putInt(NBT_HITS, this.getHitCount());
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		this.hitCount = nbt.getInt(NBT_HITS);
	}
	
	protected static void DoEffect(BlockPos shrinePos, LivingEntity entity, int color) {
		if (entity.world.isRemote) {
			return;
		}
		
		NostrumMagicaSounds.LEVELUP.play(entity);
		NostrumParticles.FILLED_ORB.spawn(entity.world, new SpawnParams(
			50,
			shrinePos.getX() + .5, shrinePos.getY() + 1.75, shrinePos.getZ() + .5, 1, 40, 10,
			entity.getEntityId()
			).color(color));
	}
	
	public static class Element extends ShrineTileEntity<ShrineTriggerEntity.Element> {
		
		private static final String NBT_ELEMENT = "element";
		
		private EMagicElement element;
		
		public Element() {
			super(NostrumTileEntities.ElementShrineTileType);
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
		public CompoundNBT write(CompoundNBT nbt) {
			nbt = super.write(nbt);
			
			nbt.put(NBT_ELEMENT, element.toNBT());
			
			return nbt;
		}
		
		@Override
		public void read(BlockState state, CompoundNBT nbt) {
			super.read(state, nbt);
			
			if (nbt.contains(NBT_ELEMENT)) {
				this.element = EMagicElement.FromNBT(nbt.get(NBT_ELEMENT));
			}
		}

		@Override
		protected ShrineTriggerEntity.Element makeTriggerEntity(World world, double x, double y, double z) {
			ShrineTriggerEntity.Element ent = new ShrineTriggerEntity.Element(NostrumEntityTypes.elementShrine, world);
			ent.setPosition(x, y, z);
			return ent;
		}

		@Override
		protected void doReward(PlayerEntity player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			// Shrine blocks grant novice mastery of their elements
			final EMagicElement element = getElement();
			
			if (attr.getElementalMastery(element) == EElementalMastery.UNKNOWN
					&& attr.setElementalMastery(element, EElementalMastery.NOVICE)) {
				// Just learned!
				final int color = 0x80000000 | (0x00FFFFFF & element.getColor());
				DoEffect(pos, player, color);
				NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) player);
			} else {
				player.sendMessage(new TranslationTextComponent("info.shrine.seektrial"), Util.DUMMY_UUID);
			}
		}

		@Override
		protected int getParticleColor() {
			return getElement().getColor();
		}
		
		@Override
		public boolean canPlayerSee(INostrumMagic attr, PlayerEntity player) {
			return !attr.getElementalMastery(this.getElement()).isGreaterOrEqual(EElementalMastery.NOVICE);
		}
	}
	
	public static class Alteration extends ShrineTileEntity<ShrineTriggerEntity.Alteration> {
		
		private static final String NBT_ALTERATION = "alteration";
		
		private EAlteration alteration;
		
		public Alteration() {
			super(NostrumTileEntities.AlterationShrineTileType);
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
		public CompoundNBT write(CompoundNBT nbt) {
			nbt = super.write(nbt);
			
			nbt.put(NBT_ALTERATION, alteration.toNBT());
			
			return nbt;
		}
		
		@Override
		public void read(BlockState state, CompoundNBT nbt) {
			super.read(state, nbt);
			
			if (nbt.contains(NBT_ALTERATION)) {
				this.alteration = EAlteration.FromNBT(nbt.get(NBT_ALTERATION));
			}
		}

		@Override
		protected ShrineTriggerEntity.Alteration makeTriggerEntity(World world, double x, double y, double z) {
			ShrineTriggerEntity.Alteration ent = new ShrineTriggerEntity.Alteration(NostrumEntityTypes.alterationShrine, world);
			ent.setPosition(x, y, z);
			return ent;
		}

		@Override
		protected void doReward(PlayerEntity player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			final EAlteration alteration = getAlteration();
			
			if (!attr.getAlterations().getOrDefault(alteration, false)) {
				attr.unlockAlteration(alteration);
				DoEffect(pos, player, 0x80808ABF);
				player.sendMessage(new TranslationTextComponent("info.shrine.alteration", alteration.getName()), Util.DUMMY_UUID);
				NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) player);
			}
		}

		@Override
		protected int getParticleColor() {
			return 0xFF808ABF;
		}

		@Override
		public boolean canPlayerSee(INostrumMagic attr, PlayerEntity player) {
			return !attr.getAlterations().getOrDefault(this.getAlteration(), false);
		}
	}
	
	public static class Shape extends ShrineTileEntity<ShrineTriggerEntity.Shape> {
		
		private static final String NBT_SHAPE = "shape";
		
		private SpellShape shape;
		
		public Shape() {
			super(NostrumTileEntities.ShapeShrineTileType);
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
		public CompoundNBT write(CompoundNBT nbt) {
			nbt = super.write(nbt);
			
			if (getShape() != null) {
				nbt.putString(NBT_SHAPE, getShape().getShapeKey());
			}
			
			return nbt;
		}
		
		@Override
		public void read(BlockState state, CompoundNBT nbt) {
			super.read(state, nbt);
			
			if (nbt.contains(NBT_SHAPE)) {
				this.shape = SpellShape.get(nbt.getString(NBT_SHAPE));
			}
		}

		@Override
		protected ShrineTriggerEntity.Shape makeTriggerEntity(World world, double x, double y, double z) {
			ShrineTriggerEntity.Shape ent = new ShrineTriggerEntity.Shape(NostrumEntityTypes.shapeShrine, world);
			ent.setPosition(x, y, z);
			return ent;
		}

		@Override
		protected void doReward(PlayerEntity player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			final SpellShape shape = getShape();
			
			if (!attr.getShapes().contains(shape)) {
				attr.addShape(shape);
				DoEffect(pos, player, 0x8080C0A0);
				player.sendMessage(new TranslationTextComponent("info.shrine.shape", new Object[] {shape.getDisplayName()}), Util.DUMMY_UUID);
				NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) player);
			}
		}

		@Override
		protected int getParticleColor() {
			return 0xFF80C0A0;
		}

		@Override
		public boolean canPlayerSee(INostrumMagic attr, PlayerEntity player) {
			return !attr.getShapes().contains(this.getShape());
		}
	}
	
	public static class Tier extends ShrineTileEntity<ShrineTriggerEntity.Tier> {
		
		private static final String NBT_TIER = "tier";
		
		private EMagicTier tier;
		
		public Tier() {
			super(NostrumTileEntities.TierShrineTileType);
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
		public CompoundNBT write(CompoundNBT nbt) {
			nbt = super.write(nbt);
			
			nbt.put(NBT_TIER, getTier().toNBT());
			
			return nbt;
		}
		
		@Override
		public void read(BlockState state, CompoundNBT nbt) {
			super.read(state, nbt);
			this.tier = EMagicTier.FromNBT(nbt.get(NBT_TIER));
		}

		@Override
		protected ShrineTriggerEntity.Tier makeTriggerEntity(World world, double x, double y, double z) {
			ShrineTriggerEntity.Tier ent = new ShrineTriggerEntity.Tier(NostrumEntityTypes.tierShrine, world);
			ent.setPosition(x, y, z);
			return ent;
		}

		@Override
		protected void doReward(PlayerEntity player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			final EMagicTier tier = getTier();
			
			if (!attr.getTier().isGreaterOrEqual(tier)) {
				attr.setTier(tier);
				DoEffect(pos, player, 0x80666666);
				player.sendMessage(new TranslationTextComponent("info.shrine.tier", tier.getName()), Util.DUMMY_UUID);
				NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) player);
			}
		}

		@Override
		protected int getParticleColor() {
			return 0xFF666666;
		}

		@Override
		public boolean canPlayerSee(INostrumMagic attr, PlayerEntity player) {
			return !attr.getTier().isGreaterOrEqual(getTier());
		}
	}
	
}