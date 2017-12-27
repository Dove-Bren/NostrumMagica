package com.smanzana.nostrummagica.spells.components.triggers;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Projectile. Does no tracking, etc. Instead, spawns a projectile entity and
 * waits for it to collide.
 * Other is always set to current self
 * @author Skyler
 *
 */
public class ProjectileTrigger extends SpellTrigger {
	
	public class ProjectileTriggerInstance extends SpellTrigger.SpellTriggerInstance {

		private World world;
		private Vec3d pos;
		private float pitch;
		private float yaw;
		private boolean atMax;
		
		public ProjectileTriggerInstance(SpellState state, World world, Vec3d pos, float pitch, float yaw, boolean atMax) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.atMax = atMax;
		}
		
		@Override
		public void init(EntityLivingBase caster) {
			// We are instant! Whoo!
			EntitySpellProjectile projectile = new EntitySpellProjectile(this,
					getState().getSelf(),
					world,
					pos.xCoord, pos.yCoord, pos.zCoord,
					ProjectileTrigger.getVectorForRotation(pitch, yaw),
					5.0f, PROJECTILE_RANGE);
			
			world.spawnEntityInWorld(projectile);
		}
		
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, Lists.newArrayList(getState().getOther()), world, Lists.newArrayList(pos));
		}
		
		public void onProjectileHit(Entity entity) {
			if (entity == null || !(entity instanceof EntityLivingBase)) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList((EntityLivingBase) entity), Lists.newArrayList(getState().getOther()), null, null);
			}
		}
		
		public void onFizzle(BlockPos lastPos) {
			if (atMax)
				onProjectileHit(lastPos);
			else
				getState().triggerFail();
		}
	}

	private static final String TRIGGER_KEY = "trigger_projectile";
	private static ProjectileTrigger instance = null;
	
	public static ProjectileTrigger instance() {
		if (instance == null)
			instance = new ProjectileTrigger();
		
		return instance;
	}
	
	private ProjectileTrigger() {
		super(TRIGGER_KEY);
	}

	private static final double PROJECTILE_RANGE = 30.0;
	
	@Override
	public int getManaCost() {
		return 30;
	}

	// TODO could add element & count to trigger so it can do things differently
	// based on element and power
	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw, SpellPartParam params) {
		// We use param's flip to indicate whether we should continue on max range
		boolean atMax = false;
		if (params != null)
			atMax = params.flip;
		
		// Add direction
		pos = new Vec3d(pos.xCoord, pos.yCoord + state.getSelf().getEyeHeight(), pos.zCoord);
		return new ProjectileTriggerInstance(state, world, pos, pitch, yaw, atMax);
	}

	// Copied from vanilla entity class
	public static final Vec3d getVectorForRotation(float pitch, float yaw)
    {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }
	
}
