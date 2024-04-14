package com.smanzana.nostrummagica.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

// Copy of vanilla with no fire
public class NostrumTameLightning extends LightningBoltEntity {
	
	public static final String ID = "nostrum_lightning";
	
	/** Declares which state the lightning bolt is in. Whether it's in the air, hit the ground, etc. */
	private int lightningState;
	
	private @Nullable LivingEntity ignoreEntity;
	
	public NostrumTameLightning(EntityType<? extends NostrumTameLightning> type, World worldIn) {
		this(type, worldIn, 0, 0, 0);
	}

	public NostrumTameLightning(EntityType<? extends NostrumTameLightning> type, World worldIn, double x, double y, double z) {
		super(type, worldIn);
		this.setPosition(x, y, z);
		this.setEffectOnly(true);
		this.lightningState = 2;
	}
	
	public NostrumTameLightning setEntityToIgnore(@Nullable LivingEntity entity) {
		this.ignoreEntity = entity;
		return this;
	}

	public static void doEffect(LivingEntity entity) {
		if (entity.world.isRemote) {
			return;
		}
		
		NostrumParticles.LIGHTNING_STATIC.spawn(entity.world, new SpawnParams(
				3,
				entity.getPosX(), entity.getPosY() + entity.getHeight(), entity.getPosZ(), 1, 30, 5,
				new Vector3d(0, -0.05, 0), null
				).color(0x80000000 | (0x00FFFFFF & EMagicElement.LIGHTNING.getColor())));
	}
	
	@Override
	public void tick() {
		super.tick();

		--this.lightningState;

		if (this.lightningState >= 0 && this.world instanceof ServerWorld) {
			List<Entity> list = this.world.getEntitiesInAABBexcluding(this, new AxisAlignedBB(this.getPosX() - 3.0D, this.getPosY() - 3.0D, this.getPosZ() - 3.0D, this.getPosX() + 3.0D, this.getPosY() + 6.0D + 3.0D, this.getPosZ() + 3.0D),
					Entity::isAlive);

			for (int i = 0; i < list.size(); ++i) {
				Entity entity = (Entity)list.get(i);
				if (ignoreEntity != entity && !net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, this)) { 
					entity.func_241841_a((ServerWorld) world, this); //onStruckByLightning(this);
					entity.setInvulnerable(false);
					entity.hurtResistantTime = 0;
				}
			}
		}
	}
	
	@Override
	public SoundCategory getSoundCategory() {
		return SoundCategory.WEATHER;
	}
}
