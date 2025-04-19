package com.smanzana.nostrummagica.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

// Copy of vanilla with no fire
public class TameLightning extends LightningBolt {
	
	public static final String ID = "nostrum_lightning";
	
	/** Declares which state the lightning bolt is in. Whether it's in the air, hit the ground, etc. */
	private int lightningState;
	
	private @Nullable LivingEntity ignoreEntity;
	
	public TameLightning(EntityType<? extends TameLightning> type, Level worldIn) {
		this(type, worldIn, 0, 0, 0);
	}

	public TameLightning(EntityType<? extends TameLightning> type, Level worldIn, double x, double y, double z) {
		super(type, worldIn);
		this.setPos(x, y, z);
		this.setVisualOnly(true);
		this.lightningState = 2;
	}
	
	public TameLightning setEntityToIgnore(@Nullable LivingEntity entity) {
		this.ignoreEntity = entity;
		return this;
	}

	public static void doEffect(LivingEntity entity) {
		if (entity.level.isClientSide) {
			return;
		}
		
		NostrumParticles.LIGHTNING_STATIC.spawn(entity.level, new SpawnParams(
				3,
				entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ(), 1, 30, 5,
				new Vec3(0, -0.05, 0), null
				).color(0x80000000 | (0x00FFFFFF & EMagicElement.LIGHTNING.getColor())));
	}
	
	@Override
	public void tick() {
		super.tick();

		--this.lightningState;

		if (this.lightningState >= 0 && this.level instanceof ServerLevel) {
			List<Entity> list = this.level.getEntities(this, new AABB(this.getX() - 3.0D, this.getY() - 3.0D, this.getZ() - 3.0D, this.getX() + 3.0D, this.getY() + 6.0D + 3.0D, this.getZ() + 3.0D),
					e -> e.isAlive() && !(e instanceof ItemEntity));

			for (int i = 0; i < list.size(); ++i) {
				Entity entity = (Entity)list.get(i);
				if (ignoreEntity != entity && !net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, this)) { 
					entity.thunderHit((ServerLevel) level, this); //onStruckByLightning(this);
					entity.setInvulnerable(false);
					entity.invulnerableTime = 0;
				}
			}
		}
	}
	
	@Override
	public SoundSource getSoundSource() {
		return SoundSource.WEATHER;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
