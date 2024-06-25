package com.smanzana.nostrummagica.tile;

import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.dungeon.CursedGlass;
import com.smanzana.nostrummagica.entity.CursedGlassTriggerEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.SwitchTriggerEntity;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.MagicDamageSource;
import com.smanzana.nostrummagica.spell.SpellEffectEvent.SpellEffectEndEvent;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CursedGlassTileEntity extends SwitchBlockTileEntity {
	
	protected float requiredDamage;
	protected @Nullable EMagicElement requiredElement;
	
	protected long lastDamageTicks; // based on world game tick count
	protected float lastDamage;
	protected @Nullable LivingEntity lastAttacker;
	
	protected CursedGlassTileEntity(TileEntityType<? extends CursedGlassTileEntity> tileType) {
		super(tileType);
		requiredDamage = 4f;
		lastDamageTicks = -1;
		lastDamage = 0f;
		lastAttacker = null;
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public CursedGlassTileEntity() {
		this(NostrumTileEntities.CursedGlassType);
	}
	
	private static final String NBT_REQUIRED_DAMAGE = "required_damage";
	private static final String NBT_REQUIRED_ELEMENT = "required_element";
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.putFloat(NBT_REQUIRED_DAMAGE, this.requiredDamage);
		if (this.requiredElement != null) {
			nbt.put(NBT_REQUIRED_ELEMENT, this.requiredElement.toNBT());
		}
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		this.requiredDamage = nbt.getFloat(NBT_REQUIRED_DAMAGE);
		if (nbt.contains(NBT_REQUIRED_ELEMENT)) {
			this.requiredElement = EMagicElement.FromNBT(nbt.get(NBT_REQUIRED_ELEMENT));
		} else {
			this.requiredElement = null;
		}
	}
	
	public float getRequiredDamage() {
		return requiredDamage;
	}

	public void setRequiredDamage(float requiredDamage) {
		this.requiredDamage = requiredDamage;
		this.dirty();
	}

	public EMagicElement getRequiredElement() {
		return requiredElement;
	}

	public void setRequiredElement(EMagicElement requiredElement) {
		this.requiredElement = requiredElement;
		this.dirty();
	}

	public float getLastDamage() {
		return lastDamage;
	}

	public LivingEntity getLastAttacker() {
		return lastAttacker;
	}

	@Override
	public void tick() {
		super.tick();
		
//		if (world.isRemote) {
//			return;
//		}
//		
//		// Do tick logic based on type
//		final long gameTicks = world.getGameTime();
//		switch (this.triggerType) {
//		case ONE_TIME:
//			oneTimeTick(gameTicks);
//			break;
//		case REPEATABLE:
//			repeatableTick(gameTicks);
//			break;
//		case TIMED:
//			timedTick(gameTicks);
//			break;
//		}
	}
	
	@Override
	protected Vector3d getEntityOffset() {
		return new Vector3d(0.5, 0, 0.5);
	}
	
	@Override
	protected SwitchTriggerEntity makeTriggerEntity(World world, double x, double y, double z) {
		CursedGlassTriggerEntity ent = new CursedGlassTriggerEntity(NostrumEntityTypes.cursedGlassTrigger, world);
		ent.setPosition(x, y, z);
		return ent;
	}
	
	@Override
	protected void doTriggerInternal() {
		super.doTriggerInternal();
	}
	
	public boolean isBroken() {
		return ((CursedGlass) this.getBlockState().getBlock()).isBroken(this.getBlockState());
	}
	
	protected float getDamageProgressAmt() {
		if (this.requiredDamage == 0f) {
			return 0f;
		}
		return this.lastDamage / this.requiredDamage;
	}
	
	public float getDamageProgress(float partialTicks) {
		// 0 to 1f for how close it was to breaking, with run back to 0 when enough time has passed
		if (this.lastDamageTicks == -1) {
			return 0f;
		}
		
		final float diffTicks = (float) ((double) this.world.getGameTime() - ((double) this.lastDamageTicks + partialTicks)); 
		if (diffTicks > 20f) {
			return 0f;
		}
		
		final float realAmt = getDamageProgressAmt();
		if (diffTicks < 10f) {
			return realAmt;
		} else {
			// fade out in 10tick slide
			final float subTicks = diffTicks - 10f; // Should be 0-10
			return realAmt * (1f - (subTicks / 10f));
		}
	}
	
	protected boolean isRightType(DamageSource source) {
		final boolean isMagic = (source instanceof MagicDamageSource);
		
		if (this.getSwitchHitType() == SwitchHitType.MAGIC && !isMagic) {
			return false;
		}
		
		if (this.requiredElement != null) {
			if (!isMagic) {
				return false;
			}
			
			MagicDamageSource magicSource = (MagicDamageSource) source;
			if (magicSource.getElement() != this.requiredElement) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean shouldBreak(DamageSource source, float damage) {
		if (!isRightType(source)) {
			return false;
		}
		
		if (damage < this.requiredDamage) {
			return false;
		}
		
		return true;
	}
	
	protected void recordHit(DamageSource source, float damage) {
		if (!isRightType(source)) {
			damage = 0;
		}
		
		if (this.world != null) {
			this.world.addBlockEvent(getPos(), getBlockState().getBlock(), 0, damage < this.requiredDamage ? (int) Math.min(damage, Math.floor(this.requiredDamage)) : (int) damage);
		}
		this.recordHitInternal(damage, (source.getTrueSource() != null && source.getTrueSource() instanceof LivingEntity)
				? (LivingEntity) source.getTrueSource()
				: null);
	}
	
	protected void recordHitInternal(float damage, @Nullable LivingEntity attacker) {
		this.lastDamage = damage;
		this.lastAttacker = attacker;
		this.lastDamageTicks = this.world.getGameTime();
	}
	
	protected void doBreak() {
		final BlockState state = this.getBlockState();
		((CursedGlass) this.getBlockState().getBlock()).setBroken(world, pos, state);
		
		// VFX
	}
	
	@Override
	public void trigger(LivingEntity entity, DamageSource source, float damage) {
		if (!this.isBroken()) {
			// See if this hit breaks it
			if (shouldBreak(source, damage)) {
				this.doBreak();
				recordHit(source, damage);
				//return make sure to fall through to calling parent cause we're broken now
			} else {
				recordHit(source, damage);
				return; // Don't call parent
			}
		}
		
		super.trigger(entity, source, damage);
	}
	
	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 0) {
			if (this.world != null && this.world.isRemote()) {
				recordHitInternal(type, null);
			}
			return true;
		}
		return super.receiveClientEvent(id, type);
	}
	
	@SubscribeEvent
	public void onSpellEnd(SpellEffectEndEvent event) {
		if (event.getCaster() != null && !event.getCaster().world.isRemote()) {
			// Was our entity affected?
			@Nullable Map<EMagicElement, Float> damageMap = event.getSpellFinalResults().affectedEntities.get(this.getTriggerEntity());
			if (damageMap != null) {
				final float total;
				if (this.requiredElement == null) {
					total = damageMap.values().stream().reduce(Float::sum).orElse(0f);
				} else {
					total = damageMap.getOrDefault(this.requiredDamage, 0f);
				}
				
				if (total > 0f) {
					this.trigger(this.getTriggerEntity(), new MagicDamageSource(event.getCaster(), requiredElement), total);
				}
			}
		}
	}
}