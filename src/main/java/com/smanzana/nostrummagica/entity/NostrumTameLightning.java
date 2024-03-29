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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
		super(worldIn, x, y, z, true);
		this.lightningState = 2;
		
		// type is ignored! Which I think means clients will think it's a real bolt?
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
				entity.posX, entity.posY + entity.getHeight(), entity.posZ, 1, 30, 5,
				new Vec3d(0, -0.05, 0), null
				).color(0x80000000 | (0x00FFFFFF & EMagicElement.LIGHTNING.getColor())));
	}
	
	@Override
	public void tick() {
		super.tick();

		--this.lightningState;

		if (this.lightningState >= 0) {
			List<Entity> list = this.world.getEntitiesInAABBexcluding(this, new AxisAlignedBB(this.posX - 3.0D, this.posY - 3.0D, this.posZ - 3.0D, this.posX + 3.0D, this.posY + 6.0D + 3.0D, this.posZ + 3.0D),
					Entity::isAlive);

			for (int i = 0; i < list.size(); ++i) {
				Entity entity = (Entity)list.get(i);
				if (ignoreEntity != entity && !net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, this)) { 
					entity.onStruckByLightning(this);
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
