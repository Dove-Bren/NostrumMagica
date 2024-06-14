package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile.ISpellProjectileShape;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.util.Projectiles;
import com.smanzana.nostrummagica.util.RayTrace;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Projectile. Does no tracking, etc. Instead, spawns a projectile entity and
 * waits for it to collide.
 * Other is always set to current self
 * @author Skyler
 *
 */
public class ProjectileShape extends SpellShape {
	
	public class ProjectileShapeInstance extends SpellShapeInstance implements ISpellProjectileShape {

		private final World world;
		private final Vector3d pos;
		private final float pitch;
		private final float yaw;
		private final boolean atMax;
		private final boolean hitAllies;
		private final SpellCharacteristics characteristics;
		
		public ProjectileShapeInstance(ISpellState state, World world, Vector3d pos, float pitch, float yaw, boolean atMax, boolean hitAllies, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.atMax = atMax;
			this.hitAllies = hitAllies;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// Do a little more work of getting a good vector for things
			// that aren't players
			final Vector3d dir;
			if (caster instanceof MobEntity && ((MobEntity) caster).getAttackTarget() != null) {
				MobEntity ent = (MobEntity) caster  ;
				dir = ent.getAttackTarget().getPositionVec().add(0.0, ent.getHeight() / 2.0, 0.0)
						.subtract(caster.getPosX(), caster.getPosY() + caster.getEyeHeight(), caster.getPosZ());
			} else {
				dir = Projectiles.getVectorForRotation(pitch, yaw);
			}
			
			EntitySpellProjectile projectile = new EntitySpellProjectile(ProjectileShapeInstance.this,
					getState().getSelf(),
					pos,
					dir,
					5.0f, PROJECTILE_RANGE);
			
			projectile.setFilter(new ProjectileFilter(this.getState(), hitAllies));
			
			world.addEntity(projectile);
		}
		
		@Override
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, world, Lists.newArrayList(pos));
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new BlockPos(this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null, null);
			}
		}
		
		@Override
		public void onProjectileEnd(Vector3d lastPos) {
			if (atMax)
				onProjectileHit(new BlockPos(lastPos));
			else
				getState().triggerFail(world, lastPos);
		}
		
		public EMagicElement getElement() {
			// Return element on next shape
			return this.characteristics.getElement();
		}
	}

	private static final String ID = "projectile";
	private static final double PROJECTILE_RANGE = 30.0;
	
	private static final class ProjectileFilter implements Predicate<Entity> {

		private final ISpellState state;
		private final boolean hitAllies;
		
		public ProjectileFilter(ISpellState state, boolean hitAllies) {
			this.state = state;
			this.hitAllies = hitAllies;
		}
		
		@Override
		public boolean test(Entity ent) {
			if (ent == null) {
				return false;
			}
			
			if (ent == state.getSelf()) {
				return false;
			}
			
			if (!hitAllies) {
				if (PetFuncs.GetOwner(ent) != null && PetFuncs.GetOwner(ent).equals(state.getSelf())) {
					return false; // We own the target
				}
				if (PetFuncs.GetOwner(state.getSelf()) != null && PetFuncs.GetOwner(state.getSelf()).equals(ent)) {
					return false; // ent owns us
				}
				
				if (Projectiles.getShooter(ent) == state.getSelf()) {
					return false;
				}
			}
			
			return true;
		}
	}
	
	public ProjectileShape() {
		super(ID);
	}
	
	@Override
	public int getManaCost() {
		return 30;
	}

	@Override
	public ProjectileShapeInstance createInstance(ISpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		boolean atMax = false; // legacy
		boolean hitAllies = false;
		
		// We use param's flip to indicate whether allies should be hit
		if (params != null)
			hitAllies = params.flip;
		
		// Add direction
		pos = new Vector3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		return new ProjectileShapeInstance(state, world, pos, pitch, yaw, atMax, hitAllies, characteristics);
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
	
	@Override
	public int getWeight() {
		return 1;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(PlayerEntity player, SpellShapePartProperties params) {
		return PROJECTILE_RANGE;
	}

	@Override
	public boolean supportsPreview(SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		boolean hitAllies = false;
		final Vector3d dir;
		final LivingEntity self = state.getSelf();
		if (self instanceof MobEntity && ((MobEntity) self).getAttackTarget() != null) {
			MobEntity ent = (MobEntity) self  ;
			dir = ent.getAttackTarget().getPositionVec().add(0.0, ent.getHeight() / 2.0, 0.0)
					.subtract(self.getPosX(), self.getPosY() + self.getEyeHeight(), self.getPosZ());
		} else {
			dir = Projectiles.getVectorForRotation(pitch, yaw);
		}
		
		// We use param's flip to indicate whether allies should be hit
		if (properties != null)
			hitAllies = properties.flip;
		
		pos = new Vector3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		RayTraceResult trace = RayTrace.raytrace(world, state.getSelf(), pos, dir, (float) PROJECTILE_RANGE, new ProjectileFilter(state, hitAllies));
		if (trace.getType() == RayTraceResult.Type.BLOCK) {
			builder.add(new SpellShapePreviewComponent.Line(pos.add(0, -.25, 0), Vector3d.copyCentered(RayTrace.blockPosFromResult(trace))));
			state.trigger(null, world, Lists.newArrayList(RayTrace.blockPosFromResult(trace)));
			return true;
		} else if (trace.getType() == RayTraceResult.Type.ENTITY && RayTrace.livingFromRaytrace(trace) != null) {
			final LivingEntity living = RayTrace.livingFromRaytrace(trace);
			builder.add(new SpellShapePreviewComponent.Line(pos.add(0, -.25, 0), living.getPositionVec().add(0, living.getHeight() / 2, 0)));
			state.trigger(Lists.newArrayList(living), null, null);
			return true;
		} else {
			//final Vector3d dest = pos.add(dir.normalize().scale(PROJECTILE_RANGE));
			//builder.add(new SpellShapePreviewComponent.Line(pos.add(0, -.25, 0), new Vector3d((int) dest.getX() + .5, (int) dest.getY() + .5, (int) dest.getZ() + .5)));
			return false;
		}
	}
	
}
