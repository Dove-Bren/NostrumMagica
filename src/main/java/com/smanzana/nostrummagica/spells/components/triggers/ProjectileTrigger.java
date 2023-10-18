package com.smanzana.nostrummagica.spells.components.triggers;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.utils.Projectiles;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
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
		private boolean hitAllies;
		
		public ProjectileTriggerInstance(SpellState state, World world, Vec3d pos, float pitch, float yaw, boolean atMax, boolean hitAllies) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.atMax = atMax;
			this.hitAllies = hitAllies;
		}
		
		@Override
		public void init(LivingEntity caster) {
			// Do a little more work of getting a good vector for things
			// that aren't players
			final Vec3d dir;
			if (caster instanceof MobEntity && ((MobEntity) caster).getAttackTarget() != null) {
				MobEntity ent = (MobEntity) caster  ;
				dir = ent.getAttackTarget().getPositionVector().add(0.0, ent.getHeight() / 2.0, 0.0)
						.subtract(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
			} else {
				dir = ProjectileTrigger.getVectorForRotation(pitch, yaw);
			}
			
			final ProjectileTriggerInstance self = this;
			
			caster.getServer().runAsync(new Runnable() {

				@Override
				public void run() {
					EntitySpellProjectile projectile = new EntitySpellProjectile(self,
							getState().getSelf(),
							world,
							pos.x, pos.y, pos.z,
							dir,
							5.0f, PROJECTILE_RANGE);
					
					projectile.setFilter((ent) -> {
						
						if (ent == null) {
							return false;
						}
						
						if (ent == getState().getSelf()) {
							return false;
						}
						
						if (!hitAllies) {
							if (NostrumMagica.getOwner(ent).equals(getState().getSelf())) {
								return false; // We own the target
							}
							if (NostrumMagica.getOwner(getState().getSelf()).equals(ent)) {
								return false; // ent owns us
							}
							
							if (Projectiles.getShooter(ent) == getState().getSelf()) {
								return false;
							}
						}
						
						return true;
					});
					
					world.addEntity(projectile);
			
				}
			
			});
		}
		
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, Lists.newArrayList(getState().getOther()), world, Lists.newArrayList(pos));
		}
		
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new BlockPos(this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), Lists.newArrayList(getState().getOther()), null, null);
			}
		}
		
		public void onFizzle(BlockPos lastPos) {
			if (atMax)
				onProjectileHit(lastPos);
			else
				getState().triggerFail();
		}
		
		public EMagicElement getElement() {
			// Return element on next shape
			return getState().getNextElement();
		}
	}

	private static final String TRIGGER_KEY = "projectile";
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

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw, SpellPartParam params) {
		boolean atMax = false; // legacy
		boolean hitAllies = false;
		
		// We use param's flip to indicate whether allies should be hit
		if (params != null)
			hitAllies = params.flip;
		
		// Add direction
		pos = new Vec3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		return new ProjectileTriggerInstance(state, world, pos, pitch, yaw, atMax, hitAllies);
	}

	// Copied from vanilla entity class
	public static final Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.MANI_DUST, 1));
	}

	@Override
	public String getDisplayName() {
		return "Projectile";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.BOW, 1);
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return null;
	}

	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		return null;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.projectile.bool", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return null;
	}
	
}
