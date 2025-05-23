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
import com.smanzana.nostrummagica.spell.component.SpellShapeSelector;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.util.Projectiles;
import com.smanzana.nostrummagica.util.RayTrace;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.NonNullList;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

/**
 * Projectile. Does no tracking, etc. Instead, spawns a projectile entity and
 * waits for it to collide.
 * Other is always set to current self
 * @author Skyler
 *
 */
public class ProjectileShape extends SpellShape implements ISelectableShape {
	
	public class ProjectileShapeInstance extends SpellShapeInstance implements ISpellProjectileShape {

		private final Level world;
		private final Vec3 pos;
		private final float pitch;
		private final float yaw;
		private final boolean hitEnts;
		private final boolean hitBlocks;
		private final boolean atMax;
		private final boolean hitAllies;
		private final SpellCharacteristics characteristics;
		
		public ProjectileShapeInstance(ISpellState state, Level world, Vec3 pos, float pitch, float yaw, boolean hitEnts, boolean hitBlocks, boolean atMax, boolean hitAllies, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.hitEnts = hitEnts;
			this.hitBlocks = hitBlocks;
			this.atMax = atMax;
			this.hitAllies = hitAllies;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// Do a little more work of getting a good vector for things
			// that aren't players
			final Vec3 dir;
			if (caster instanceof Mob && ((Mob) caster).getTarget() != null) {
				Mob ent = (Mob) caster  ;
				dir = ent.getTarget().position().add(0.0, ent.getBbHeight() / 2.0, 0.0)
						.subtract(caster.getX(), caster.getY() + caster.getEyeHeight(), caster.getZ());
			} else {
				dir = Projectiles.getVectorForRotation(pitch, yaw);
			}
			
			SpellProjectileEntity projectile = new SpellProjectileEntity(ProjectileShapeInstance.this,
					getState().getSelf(),
					pos,
					dir,
					5.0f, PROJECTILE_RANGE);
			
			projectile.setFilter(hitEnts ? new ProjectileFilter(this.getState(), hitAllies) : e -> false);
			
			world.addFreshEntity(projectile);
		}
		
		@Override
		public void onProjectileHit(SpellLocation location) {
			if (hitBlocks) {
				getState().trigger(null, Lists.newArrayList(location));
			} else {
				getState().triggerFail(location);
			}
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new SpellLocation(world, this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(new SpellLocation(entity.level, entity.blockPosition()));
			} else if (hitEnts) {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null);
			}
		}
		
		@Override
		public void onProjectileEnd(Vec3 lastPos) {
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
		baseProperties.addProperty(AFFECT_ALLIES).addProperty(SpellShapeSelector.PROPERTY);
	}
	
	protected boolean getHitsAllies(SpellShapeProperties properties) {
		return properties.getValue(AFFECT_ALLIES);
	}
	
	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 30;
	}

	@Override
	public ProjectileShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		final boolean hitEnts = affectsEntities(params);
		final boolean hitBlocks = affectsBlocks(params);
		final boolean atMax = false; // legacy
		final boolean hitAllies = getHitsAllies(params);
		return new ProjectileShapeInstance(state, location.world, location.shooterPosition, pitch, yaw, hitEnts, hitBlocks, atMax, hitAllies, characteristics);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.of(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.MANI_DUST, 1));
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
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(Player player, SpellShapeProperties params) {
		return PROJECTILE_RANGE;
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final boolean hitEnts = affectsEntities(properties);
		final boolean hitBlocks = affectsBlocks(properties);
		final boolean hitAllies = getHitsAllies(properties);
		final Vec3 dir;
		final LivingEntity self = state.getSelf();
		if (self instanceof Mob && ((Mob) self).getTarget() != null) {
			Mob ent = (Mob) self  ;
			dir = ent.getTarget().position().add(0.0, ent.getBbHeight() / 2.0, 0.0)
					.subtract(self.getX(), self.getY() + self.getEyeHeight(), self.getZ());
		} else {
			dir = Projectiles.getVectorForRotation(pitch, yaw);
		}
		
		HitResult trace = RayTrace.raytrace(location.world, state.getSelf(), location.shooterPosition, dir, (float) PROJECTILE_RANGE, hitEnts ? new ProjectileFilter(state, hitAllies) : e -> false);
		if (trace.getType() == HitResult.Type.BLOCK) {
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), trace.getLocation()));
			if (hitBlocks) {
				state.trigger(null, Lists.newArrayList(new SpellLocation(location.world, trace)));
			}
			return true;
		} else if (trace.getType() == HitResult.Type.ENTITY && RayTrace.livingFromRaytrace(trace) != null && hitEnts) {
			final float partialTicks = Minecraft.getInstance().getFrameTime();
			final LivingEntity living = RayTrace.livingFromRaytrace(trace);
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), living.getPosition(partialTicks).add(0, living.getBbHeight() / 2, 0)));
			state.trigger(Lists.newArrayList(living), null);
			return true;
		} else {
			//final Vector3d dest = pos.add(dir.normalize().scale(PROJECTILE_RANGE));
			//builder.add(new SpellShapePreviewComponent.Line(pos.add(0, -.25, 0), new Vector3d((int) dest.getX() + .5, (int) dest.getY() + .5, (int) dest.getZ() + .5)));
			return false;
		}
	}

	public SpellShapeProperties makeProps(boolean affectAllies) {
		return this.getDefaultProperties().setValue(AFFECT_ALLIES, affectAllies);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(false, this.affectsEntities(params), this.affectsBlocks(params));
	}
	
}
