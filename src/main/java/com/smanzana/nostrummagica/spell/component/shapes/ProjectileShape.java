package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.SpellProjectileEntity;
import com.smanzana.nostrummagica.entity.SpellProjectileEntity.ISpellProjectileShape;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.BooleanSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.util.Projectiles;
import com.smanzana.nostrummagica.util.RayTrace;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
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
			
			SpellProjectileEntity projectile = new SpellProjectileEntity(ProjectileShapeInstance.this,
					getState().getSelf(),
					pos,
					dir,
					5.0f, PROJECTILE_RANGE);
			
			projectile.setFilter(new ProjectileFilter(this.getState(), hitAllies));
			
			world.addEntity(projectile);
		}
		
		@Override
		public void onProjectileHit(SpellLocation location) {
			getState().trigger(null, Lists.newArrayList(location));
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new SpellLocation(world, this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(new SpellLocation(entity.world, entity.getPosition()));
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null);
			}
		}
		
		@Override
		public void onProjectileEnd(Vector3d lastPos) {
			if (atMax)
				onProjectileHit(new SpellLocation(world, lastPos));
			else
				getState().triggerFail(new SpellLocation(world, lastPos));
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
	
	public static final SpellShapeProperty<Boolean> AFFECT_ALLIES = new BooleanSpellShapeProperty("hit_allies");
	
	public ProjectileShape() {
		super(ID);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		baseProperties.addProperty(AFFECT_ALLIES);
	}
	
	protected boolean getHitsAllies(SpellShapeProperties properties) {
		return properties.getValue(AFFECT_ALLIES);
	}
	
	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 30;
	}

	@Override
	public ProjectileShapeInstance createInstance(ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		boolean atMax = false; // legacy
		boolean hitAllies = getHitsAllies(params);
		return new ProjectileShapeInstance(state, location.world, location.shooterPosition, pitch, yaw, atMax, hitAllies, characteristics);
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
	public int getWeight(SpellShapeProperties properties) {
		return 1;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(PlayerEntity player, SpellShapeProperties params) {
		return PROJECTILE_RANGE;
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final boolean hitAllies = getHitsAllies(properties);
		final Vector3d dir;
		final LivingEntity self = state.getSelf();
		if (self instanceof MobEntity && ((MobEntity) self).getAttackTarget() != null) {
			MobEntity ent = (MobEntity) self  ;
			dir = ent.getAttackTarget().getPositionVec().add(0.0, ent.getHeight() / 2.0, 0.0)
					.subtract(self.getPosX(), self.getPosY() + self.getEyeHeight(), self.getPosZ());
		} else {
			dir = Projectiles.getVectorForRotation(pitch, yaw);
		}
		
		RayTraceResult trace = RayTrace.raytrace(location.world, state.getSelf(), location.shooterPosition, dir, (float) PROJECTILE_RANGE, new ProjectileFilter(state, hitAllies));
		if (trace.getType() == RayTraceResult.Type.BLOCK) {
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), trace.getHitVec()));
			state.trigger(null, Lists.newArrayList(new SpellLocation(location.world, trace)));
			return true;
		} else if (trace.getType() == RayTraceResult.Type.ENTITY && RayTrace.livingFromRaytrace(trace) != null) {
			final float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
			final LivingEntity living = RayTrace.livingFromRaytrace(trace);
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), living.func_242282_l(partialTicks).add(0, living.getHeight() / 2, 0)));
			state.trigger(Lists.newArrayList(living), null);
			return true;
		} else {
			//final Vector3d dest = pos.add(dir.normalize().scale(PROJECTILE_RANGE));
			//builder.add(new SpellShapePreviewComponent.Line(pos.add(0, -.25, 0), new Vector3d((int) dest.getX() + .5, (int) dest.getY() + .5, (int) dest.getZ() + .5)));
			return false;
		}
	}
	
}
