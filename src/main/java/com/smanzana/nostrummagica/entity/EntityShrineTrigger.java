package com.smanzana.nostrummagica.entity;


import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.tile.ShrineTileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class EntityShrineTrigger<E extends ShrineTileEntity<?>> extends MobEntity {
	
	protected static final String ID_BASE = "entity_shrine_ent_";
	
	private static final DataParameter<Integer> HIT_COUNT =
			EntityDataManager.<Integer>createKey(EntityShrineTrigger.class, DataSerializers.VARINT);
	
	protected static final int MAX_HITS = 5;
	
	private BlockPos cachePos;
	private E cacheEntity;
	
	protected EntityShrineTrigger(EntityType<? extends EntityShrineTrigger<E>> type, World worldIn) {
		super(type, worldIn);
		cachePos = null;
		cacheEntity = null;
		this.setNoGravity(true);
		this.setInvulnerable(true);
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(HIT_COUNT, 0);
	}
	
	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return false;
	}
	
	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return MobEntity.func_233666_p_()
				.createMutableAttribute(Attributes.MAX_HEALTH, 1D);
	}
	
	
	@Override
	public void applyKnockback(float strenght, double xRatio, double zRatio) {
		return; // Do not get knocked around
	}
	
	@Override
	public boolean canBePushed() {
		return false;
	}
	
	@Override
	public void applyEntityCollision(Entity entityIn) {
		return;
	}
	
	protected void collideWithEntity(Entity entity) {
		;
	}
	
	@Override
	protected void collideWithNearbyEntities() {
		;
	}
	
	@Override
	protected int decreaseAirSupply(int air) {
		return air;
	}
	
	@Override
	public boolean isInvisibleToPlayer(PlayerEntity player) {
		@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return !player.isCreative() && !canPlayerSee(attr, player);
	}
	
	protected abstract boolean canPlayerSee(INostrumMagic attr, PlayerEntity player);
	
	protected abstract int getParticleColor();
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.world.isRemote()) {
			return true;
		}
		
		E te = getLinkedTileEntity();
		if (te == null) {
			return false;
		}
		
		if (source.getTrueSource() != null && source.getTrueSource() instanceof LivingEntity) {
			final int origHitCount = getHitCount();
			if (origHitCount+1 >= MAX_HITS) {
				this.setHitCount(0);
				te.trigger((LivingEntity) source.getTrueSource());
			} else {
				this.setHitCount(origHitCount+1);
			}
			this.getEntityWorld().playSound(null, this.getPosition(), SoundEvents.ENTITY_ENDER_DRAGON_HURT, SoundCategory.BLOCKS, 1f, 1f);
			NostrumParticles.FILLED_ORB.spawn(this.getEntityWorld(), new SpawnParams(30, this.getPosX(), this.getPosY() + this.getHeight()/2, this.getPosZ(), .3,
					40, 20, new Vector3d(0, .1, 0), new Vector3d(.1, .05, .1)).gravity(true).color(getParticleColor()));
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public E getLinkedTileEntity() {
		final BlockPos checkPos = this.getPosition().down();
		if (this.cachePos == null || this.cacheEntity == null || !checkPos.equals(cachePos) || cacheEntity.getTriggerEntity() != this) {
			cacheEntity = null;
			this.cachePos = checkPos.toImmutable();
			TileEntity te = world.getTileEntity(cachePos);
			if (te != null && te instanceof ShrineTileEntity) {
				E ent = (E) te;
				if (world.isRemote || ent.getTriggerEntity() == this) {
					cacheEntity = ent;
				}
			}
		}
		
		return this.cacheEntity;
	}
	
	@Override
	public void livingTick() {
		super.livingTick();
		
		setInvulnerable(false);
		
		if (this.isAlive() && !this.dead) {
			if (!world.isRemote && this.ticksExisted > 20) {
				
				if (this.ticksExisted % 20 == 0) {
					// Clear cache every once in a while
					this.cacheEntity = null;
				}
				
				// Should be on top of a shrine block
				if (getLinkedTileEntity() == null) {
					this.remove();
				}
			}
		}
	}
	
	public int getHitCount() {
		return this.dataManager.get(HIT_COUNT);
	}
	
	protected void setHitCount(int count) {
		dataManager.set(HIT_COUNT, count);
	}
	
	public int getMaxHitCount() {
		return MAX_HITS;
	}
	
	public static class Element extends EntityShrineTrigger<ShrineTileEntity.Element> {
		public static final String ID = ID_BASE + "element";
		
		public Element(EntityType<? extends EntityShrineTrigger<ShrineTileEntity.Element>> type, World worldIn) {
			super(type, worldIn);
		}
		
		public EMagicElement getElement() {
			if (this.getLinkedTileEntity() != null) {
				return this.getLinkedTileEntity().getElement();
			}
			return EMagicElement.PHYSICAL;
		}

		@Override
		protected boolean canPlayerSee(INostrumMagic attr, PlayerEntity player) {
			return !attr.getElementalMastery(this.getElement()).isGreaterOrEqual(EElementalMastery.NOVICE);
		}

		@Override
		protected int getParticleColor() {
			return getElement().getColor();
		}
	}
	
	public static class Shape extends EntityShrineTrigger<ShrineTileEntity.Shape> {
		public static final String ID = ID_BASE + "shape";
		
		public Shape(EntityType<? extends EntityShrineTrigger<ShrineTileEntity.Shape>> type, World worldIn) {
			super(type, worldIn);
		}
		
		protected final SpellShape getDefaultShape() {
			return SpellShape.getAllShapes().iterator().next();
		}
		
		public SpellShape getShape() {
			if (this.getLinkedTileEntity() != null) {
				return this.getLinkedTileEntity().getShape();
			}
			return getDefaultShape();
		}

		@Override
		protected boolean canPlayerSee(INostrumMagic attr, PlayerEntity player) {
			return !attr.getShapes().contains(this.getShape());
		}

		@Override
		protected int getParticleColor() {
			return 0xFF80C0A0;
		}
	}
	
	public static class Alteration extends EntityShrineTrigger<ShrineTileEntity.Alteration> {
		public static final String ID = ID_BASE + "alteration";
		
		public Alteration(EntityType<? extends EntityShrineTrigger<ShrineTileEntity.Alteration>> type, World worldIn) {
			super(type, worldIn);
		}
		
		public EAlteration getAlteration() {
			if (this.getLinkedTileEntity() != null) {
				return this.getLinkedTileEntity().getAlteration();
			}
			return EAlteration.INFLICT;
		}

		@Override
		protected boolean canPlayerSee(INostrumMagic attr, PlayerEntity player) {
			return !attr.getAlterations().getOrDefault(this.getAlteration(), false);
		}

		@Override
		protected int getParticleColor() {
			return 0xFF808ABF;
		}
	}
}
