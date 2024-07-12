package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.DungeonAirBlock;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.SpellMortarEntity;
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
import com.smanzana.nostrummagica.util.Curves;
import com.smanzana.nostrummagica.util.Projectiles;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Projectile that flies in an arc / straight down on a target instead in a straight line.
 * Does no tracking. Spawns an entity and waits for it to collide.
 * Other is always set to current self
 * @author Skyler
 *
 */
public class MortarShape extends SpellShape implements ISelectableShape {
	
	public static final float MaxHDist = 40;
	public static final double OverworldGravity = 0.025D;
	
	public static class MortarShapeInstance extends SpellShapeInstance implements ISpellProjectileShape {
		
		protected static final double HVel = .5; 

		private final World world;
		private final Vector3d pos;
		private final float pitch;
		private final float yaw;
		private final boolean hitEnts;
		private final boolean hitBlocks;
		private final boolean noArc;
		private final SpellCharacteristics characteristics;
		
		public MortarShapeInstance(ISpellState state, World world, Vector3d pos, float pitch, float yaw, boolean hitEnts, boolean hitBlocks, boolean noArc, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.hitEnts = hitEnts;
			this.hitBlocks = hitBlocks;
			this.noArc = noArc;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			
			// Do a little more work of getting a good vector for things
			// that aren't players
			final Vector3d dir;
			final LivingEntity target;
			if (caster instanceof MobEntity && ((MobEntity) caster).getAttackTarget() != null) {
				MobEntity ent = (MobEntity) caster  ;
				target = ent.getAttackTarget(); // We already know target
				dir = null;
			} else {
				target = null; // Solve for target on main thread with raytrace
				dir = Projectiles.getVectorForRotation(pitch, yaw);
			}

			
			// If we have entity target, set that as dest. Otherwise, raytrace
			final Vector3d dest;
			if (target != null) {
				dest = target.getPositionVec().add(0, target.getHeight()/2, 0);
			} else {
				RayTraceResult mop = RayTrace.raytraceApprox(world, getState().getSelf(), pos, dir, MaxHDist, (ent) -> {
					if (!hitEnts) {
						return false;
					}
					if (!(ent instanceof LivingEntity)) {
						return false;
					}
					
					if (getState().getSelf() == NostrumMagica.resolveLivingEntity(ent)) {
						return false;
					}
					
					return true;
				}, .5);
				
				// Note: not opting out of block MOP dest setting based on params because we fizzle on blocks even if we don't affect them
				if (mop.getType() == RayTraceResult.Type.ENTITY && hitEnts) {
					final LivingEntity hitEntity = RayTrace.livingFromRaytrace(mop);
					dest = hitEntity.getPositionVec().add(0, hitEntity.getHeight()/2, 0);
				} else if (mop.getType() == RayTraceResult.Type.BLOCK) {
					dest = mop.getHitVec();
				} else {
					Vector3d actual = pos.add(dir.scale(MaxHDist));
					dest = new Vector3d(Math.floor(actual.x) + .5, Math.floor(actual.y) + .5, Math.floor(actual.z) + .5);
				}
			}
			
			// Figure out angle to hit destination from source. Ignore blocks and stuff
			final Vector3d startVelocity;
			final Vector3d startPos;
			if (MortarShapeInstance.this.noArc) {
				// Drop from above
				// Try not to start in the ceiling
				BlockPos.Mutable cursor = new BlockPos.Mutable();
				cursor.setPos(dest.x, dest.y + 2, dest.z); // start 2 (+1) above; best we can do
				
				for (int i = 0; i < 7; i++) {
					cursor.move(Direction.UP);
					BlockState state = world.getBlockState(cursor);
					if (!(state.getBlock() instanceof DungeonAirBlock) && !world.isAirBlock(cursor)) {
						// can't go here. Go back down and bail
						cursor.move(Direction.DOWN);
						break;
					}
				}
				
				startPos = new Vector3d(dest.x, cursor.getY(), dest.z);
				startVelocity = new Vector3d(0, -.25, 0);
			} else {
				startPos = pos;
				startVelocity = Curves.getMortarArcVelocity(pos, dest, HVel, OverworldGravity);
			}
			
			SpellMortarEntity projectile = new SpellMortarEntity(NostrumEntityTypes.spellMortar, MortarShapeInstance.this,
					getState().getSelf(),
					world,
					startPos,
					startVelocity,
					1.0f, OverworldGravity);
			
			projectile.setFilter((ent) -> {
				if (!hitEnts) {
					return false;
				}
				
				if (ent == null) {
					return false;
				}
				
				if (ent == getState().getSelf()) {
					return false;
				}
				
				if (Projectiles.getShooter(ent) == getState().getSelf()) {
					return false;
				}
				
				return true;
			});
			
			world.addEntity(projectile);
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
				onProjectileHit(new SpellLocation(entity.world, entity.getPosition()));
			} else if (hitEnts) {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null);
			}
		}
		
		@Override
		public EMagicElement getElement() {
			// Return element on next shape
			return characteristics.getElement();
		}

		@Override
		public void onProjectileEnd(Vector3d pos) {
			getState().triggerFail(new SpellLocation(world, pos));
		}
	}

	private static final String ID = "mortar";
	
	public static final SpellShapeProperty<Boolean> ARCLESS = new BooleanSpellShapeProperty("no_arc");
	
	public MortarShape() {
		super(ID);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(ARCLESS).addProperty(SpellShapeSelector.PROPERTY);
	}
	
	protected boolean getNoArc(SpellShapeProperties properties) {
		return properties.getValue(ARCLESS);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 30;
	}

	@Override
	public MortarShapeInstance createInstance(ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		boolean noArc = getNoArc(params);
		final boolean hitEnts = affectsEntities(params);
		final boolean hitBlocks = affectsBlocks(params);
		return new MortarShapeInstance(state, location.world, location.shooterPosition, pitch, yaw, hitEnts, hitBlocks, noArc, characteristics);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.SKY_ASH, 1));
	}

	@Override
	public String getDisplayName() {
		return "Mortar";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.FIRE_CHARGE, 1);
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
		return MaxHDist;
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		boolean noArc = getNoArc(properties);
		final boolean hitEnts = affectsEntities(properties);
		final boolean hitBlocks = affectsBlocks(properties);
		
		// Do a little more work of getting a good vector for things
		// that aren't players
		final Vector3d dir;
		final LivingEntity target;
		if (state.getSelf() instanceof MobEntity && ((MobEntity) state.getSelf()).getAttackTarget() != null) {
			MobEntity ent = (MobEntity) state.getSelf()  ;
			target = ent.getAttackTarget(); // We already know target
			dir = null;
		} else {
			target = null; // Solve for target on main thread with raytrace
			dir = Projectiles.getVectorForRotation(pitch, yaw);
		}

		
		// If we have entity target, set that as dest. Otherwise, raytrace
		boolean success = false;
		final Vector3d dest;
		if (target != null) {
			dest = target.getPositionVec();
			success = true;
		} else {
			RayTraceResult mop = RayTrace.raytraceApprox(location.world, state.getSelf(), location.shooterPosition, dir, MaxHDist, (ent) -> {
				if (!hitEnts) {
					return false;
				}
				if (!(ent instanceof LivingEntity)) {
					return false;
				}
				if (state.getSelf() == NostrumMagica.resolveLivingEntity(ent)) {
					return false;
				}
				
				return true;
			}, .5);

			// Note: not opting out of block MOP dest setting based on params because we fizzle on blocks even if we don't affect them
			if (mop.getType() == RayTraceResult.Type.ENTITY && hitEnts) {
				final LivingEntity hit = RayTrace.livingFromRaytrace(mop);
				dest = hit.getPositionVec().add(0, hit.getHeight() / 2, 0);
				state.trigger(Lists.newArrayList(hit), null);
				success = true;
			} else if (mop.getType() == RayTraceResult.Type.BLOCK) {
				dest = mop.getHitVec();
				if (hitBlocks) {
					state.trigger(null, Lists.newArrayList(new SpellLocation(location.world, mop)));
				}
				success = true;
			} else {
				dest = Vector3d.copyCentered(new BlockPos(location.shooterPosition.add(dir.scale(MaxHDist))));
				// Don't 'trigger' at spot because we'll probably keep flying through it and not hit there
				//state.trigger(null, world, Lists.newArrayList(new BlockPos(dest)));
				success = false;
			}
		}
		
		if (noArc) {
			// Drop from above
			// Try not to start in the ceiling
			BlockPos.Mutable cursor = new BlockPos.Mutable();
			cursor.setPos(dest.x, dest.y + 2, dest.z); // start 2 (+1) above; best we can do
			
			for (int i = 0; i < 7; i++) {
				cursor.move(Direction.UP);
				BlockState blockstate = location.world.getBlockState(cursor);
				if (!(blockstate.getBlock() instanceof DungeonAirBlock) && !location.world.isAirBlock(cursor)) {
					// can't go here. Go back down and bail
					cursor.move(Direction.DOWN);
					break;
				}
			}
			
			Vector3d startPos = new Vector3d(dest.x, cursor.getY(), dest.z);
			builder.add(new SpellShapePreviewComponent.Line(startPos, dest));
		} else {
			Vector3d start = location.shooterPosition;
			if (dir != null) {
				// Offset so curve isn't in line with player vision
				start = start.add(dir.normalize().rotateYaw(90f));
			}
			builder.add(new SpellShapePreviewComponent.Curve(start, null, new Curves.Mortar(MortarShapeInstance.HVel, dest.subtract(start), OverworldGravity)));
		}
		return success;
	}

	public SpellShapeProperties makeProps(boolean arcless) {
		return this.getDefaultProperties().setValue(ARCLESS, arcless);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(false, this.affectsEntities(params), this.affectsBlocks(params));
	}
	
}
