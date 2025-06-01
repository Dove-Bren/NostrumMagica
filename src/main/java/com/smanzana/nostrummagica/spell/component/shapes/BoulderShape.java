package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.SpellBoulderEntity;
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

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

/**
 * Disc projectile with a curved trajectory
 * @author Skyler
 *
 */
public class BoulderShape extends SpellShape implements ISelectableShape {

	public static class BoulderShapeInstance extends SpellShapeInstance implements ISpellProjectileShape {

		private final Level world;
		private final Vec3 pos;
		private final float pitch;
		private final float yaw;
		private final boolean hitEnts;
		private final boolean hitBlocks;
		private final boolean bounce;
		private final SpellCharacteristics characteristics;
		
		public BoulderShapeInstance(ISpellState state, Level world, Vec3 pos, float pitch, float yaw, boolean hitEnts, boolean hitBlocks, boolean bounce, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.hitEnts = hitEnts;
			this.hitBlocks = hitBlocks;
			this.bounce = bounce;
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
			
			SpellBoulderEntity projectile = new SpellBoulderEntity(this, 
					world,
					getState().getSelf(),
					pos,
					dir,
					2f, bounce);
			
				world.addFreshEntity(projectile);
		}

		@Override
		public void onProjectileHit(SpellLocation location) {
			if (hitBlocks) {
				getState().trigger(null, Lists.newArrayList(location), 1f, true);
			}
			// else ignore and let continue
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new SpellLocation(world, this.pos));
			}
			else if (NostrumMagica.resolveLivingEntity(entity) == null) {
				onProjectileHit(new SpellLocation(entity.level, entity.blockPosition()));
			} else if (hitEnts) {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null, 1f, true);
			}
		}

		@Override
		public EMagicElement getElement() {
			return characteristics.getElement();
		}

		@Override
		public void onProjectileEnd(Vec3 pos) {
			getState().triggerFail(new SpellLocation(world, pos));
		}
	}
	
	private static final String ID = "boulder";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1), ReagentItem.CreateStack(ReagentType.MANI_DUST, 1)));
	
	public static final SpellShapeProperty<Boolean> BOUNCE = new BooleanSpellShapeProperty("bounce");
	
	protected BoulderShape(String key) {
		super(key);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(SpellShapeSelector.PROPERTY).addProperty(BOUNCE, true);
	}
	
	public BoulderShape() {
		this(ID);
	}
	
	@Override
	public BoulderShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		final boolean hitEnts = affectsEntities(params);
		final boolean hitBlocks = affectsBlocks(params);
		final boolean bounce = bounces(params);
		return new BoulderShapeInstance(state, location.world, location.shooterPosition, pitch, yaw, hitEnts, hitBlocks, bounce, characteristics);
	}
	
	public boolean bounces(SpellShapeProperties params) {
		return params.getValue(BOUNCE);
	}
	
	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 30;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.OBSIDIAN, 1);
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
		return 50;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final boolean hitEnts = affectsEntities(properties);
		final boolean hitBlocks = affectsBlocks(properties);
		final Vec3 dir;
		final LivingEntity self = state.getSelf();
		if (self instanceof Mob && ((Mob) self).getTarget() != null) {
			Mob ent = (Mob) self  ;
			dir = ent.getTarget().position().add(0.0, ent.getBbHeight() / 2.0, 0.0)
					.subtract(self.getX(), self.getY() + self.getEyeHeight(), self.getZ());
		} else {
			dir = Projectiles.getVectorForRotation(pitch, yaw);
		}
		
		HitResult trace = RayTrace.raytrace(location.world, state.getSelf(), location.shooterPosition, dir, (float) 50, hitEnts ? new RayTrace.OtherLiving(state.getSelf()) : e -> false);
		if (trace.getType() == HitResult.Type.BLOCK && hitBlocks) {
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), Vec3.atCenterOf(RayTrace.blockPosFromResult(trace))));
			state.trigger(null, Lists.newArrayList(new SpellLocation(location.world, trace)));
			return true;
		} else if (trace.getType() == HitResult.Type.ENTITY && RayTrace.livingFromRaytrace(trace) != null && hitEnts) {
			final LivingEntity living = RayTrace.livingFromRaytrace(trace);
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), living.position().add(0, living.getBbHeight() / 2, 0)));
			state.trigger(Lists.newArrayList(living), null);
			return true;
		} else {
			final Vec3 dest = location.shooterPosition.add(dir.normalize().scale(50));
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), new Vec3((int) dest.x() + .5, (int) dest.y() + .5, (int) dest.z() + .5)));
			return true;
		}
	}
}
