package com.smanzana.nostrummagica.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

// Copy of vanilla with no fire
public class NostrumTameLightning extends EntityLightningBolt {
	
	/** Declares which state the lightning bolt is in. Whether it's in the air, hit the ground, etc. */
	private int lightningState;

	public NostrumTameLightning(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z, true);
		this.lightningState = 2;
	}

	
	@Override
	public void onUpdate() {
		super.onUpdate();

		--this.lightningState;

		if (this.lightningState >= 0) {
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(this.posX - 3.0D, this.posY - 3.0D, this.posZ - 3.0D, this.posX + 3.0D, this.posY + 6.0D + 3.0D, this.posZ + 3.0D));

			for (int i = 0; i < list.size(); ++i) {
				Entity entity = (Entity)list.get(i);
				if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, this))
					entity.onStruckByLightning(this);
			}
		}
	}
	
	@Override
	public SoundCategory getSoundCategory() {
		return SoundCategory.WEATHER;
	}
}
