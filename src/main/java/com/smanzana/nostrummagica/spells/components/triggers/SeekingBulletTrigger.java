package com.smanzana.nostrummagica.spells.components.triggers;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySpellBullet;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Projectile that slowly follows and then continues spell once it has collided.
 * This WANTS an entity to follow and doesn't go to empty positions.
 * Other is always set to current self
 * @author Skyler
 *
 */
public class SeekingBulletTrigger extends SpellTrigger {
	
	public static final float MAX_DIST = 30f;
	
	public class SeekingBulletTriggerInstance extends SpellTrigger.SpellTriggerInstance {

		private World world;
		private Vector3d pos;
		
		// Just initial parameters for setup
		private float pitch;
		private float yaw;
		private final boolean ignoreAllies;
		
		private LivingEntity target;
		
		public SeekingBulletTriggerInstance(SpellState state, World world, Vector3d pos, float pitch, float yaw, boolean ignoreAllies) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.ignoreAllies = ignoreAllies;
		}
		
		@Override
		public void init(LivingEntity caster) {
			// Do a little more work of getting a good vector for things
			// that aren't players
			final Vector3d dir;
			if (caster instanceof MobEntity && ((MobEntity) caster).getAttackTarget() != null) {
				MobEntity ent = (MobEntity) caster  ;
				target = ent.getAttackTarget(); // We already know target
				dir = null;
			} else {
				target = null; // Solve for target on main thread with raytrace
				dir = SeekingBulletTrigger.getVectorForRotation(pitch, yaw);
			}
			
			final SeekingBulletTriggerInstance self = this;
			
			caster.getServer().runAsync(new Runnable() {

				@Override
				public void run() {
					if (target == null) {
						// Ray trace
						RayTraceResult mop = RayTrace.raytraceApprox(world, getState().getSelf(), pos, dir, MAX_DIST, (ent) -> {
							if (getState().getSelf() == ent) {
								return false;
							}
							
							if (ent != null) {
								
								if (ignoreAllies) {
									// Too strong?
									LivingEntity living = NostrumMagica.resolveLivingEntity(ent);
									if (living != null
											&& NostrumMagica.IsSameTeam(getState().getSelf(), living)) {
										return false;
									}
									
									if (NostrumMagica.getOwner(ent) != null && NostrumMagica.getOwner(ent).equals(getState().getSelf())) {
										return false; // We own the target
									}
									if (NostrumMagica.getOwner(getState().getSelf()) != null && NostrumMagica.getOwner(getState().getSelf()).equals(ent)) {
										return false; // ent owns us
									}
								}
							}
							
							return true;
						}, .5);
						
						target = RayTrace.livingFromRaytrace(mop);
					}
					
					// Get axis from where target is
					Direction.Axis axis = Direction.Axis.Y;
					Vector3d forwardDir = dir;
					if (target != null) {
						Vector3d vec = target.getPositionVector().subtract(caster.getPositionVector());
						forwardDir = vec.normalize();
						axis = Direction.getFacingFromVector((float) vec.x, (float) vec.y, (float) vec.z).getAxis();
					}
					
					Vector3d startMotion;
					
					// For players, start with motion ortho to forward
					if (getState().getSelf() instanceof PlayerEntity) {
						startMotion = forwardDir
								.rotateYaw(30f * (NostrumMagica.rand.nextBoolean() ? 1 : -1))
								.rotatePitch(/*-15f*/ + NostrumMagica.rand.nextFloat() * 30f);
					}
					// For non-players, fire mostly up
					else {
						startMotion = (new Vector3d(0, 1, 0)).normalize()
								.rotateYaw(360f * NostrumMagica.rand.nextFloat());
					}
					
					startMotion = startMotion.scale(.4);
					
					EntitySpellBullet bullet = new EntitySpellBullet(NostrumEntityTypes.spellBullet, self, getState().getSelf(), target, axis);
					bullet.setMotion(startMotion);
					//bullet.setVelocity(startMotion.x, startMotion.y, startMotion.z); client only :(
					
					bullet.setFilter((ent) -> {
						if (ent != null && getState().getSelf() != ent) {
							if (NostrumMagica.getOwner(ent) != null && NostrumMagica.getOwner(ent).equals(getState().getSelf())) {
								return false; // We own the target
							}
							if (NostrumMagica.getOwner(getState().getSelf()) != null && NostrumMagica.getOwner(getState().getSelf()).equals(ent)) {
								return false; // ent owns us
							}
						}
						
						return true;
					});
					
					world.addEntity(bullet);
			
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
		
		public EMagicElement getElement() {
			// Return element on next shape
			return getState().getNextElement();
		}
	}

	private static final String TRIGGER_KEY = "bullet";
	private static SeekingBulletTrigger instance = null;
	
	public static SeekingBulletTrigger instance() {
		if (instance == null)
			instance = new SeekingBulletTrigger();
		
		return instance;
	}
	
	private SeekingBulletTrigger() {
		super(TRIGGER_KEY);
	}

	@Override
	public int getManaCost() {
		return 50;
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellPartParam params) {
		// Add direction
		pos = new Vector3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		return new SeekingBulletTriggerInstance(state, world, pos, pitch, yaw, params.flip);
	}

	// Copied from vanilla entity class
	public static final Vector3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vector3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

	@Override
	public NonNullList<ItemStack> getReagents() {
		NonNullList<ItemStack> list = NonNullList.create();
		
		list.add(ReagentItem.CreateStack(ReagentType.MANI_DUST, 1));
		list.add(ReagentItem.CreateStack(ReagentType.SPIDER_SILK, 1));
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Seeking Bullet";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.DISPENSER, 1);
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
		return I18n.format("modification.seeking_bullet.bool.name");
	}

	@Override
	public String supportedFloatName() {
		return null;
	}
	
}
