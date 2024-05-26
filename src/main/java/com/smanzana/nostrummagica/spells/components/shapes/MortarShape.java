package com.smanzana.nostrummagica.spells.components.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.DungeonAir;
import com.smanzana.nostrummagica.entity.EntitySpellMortar;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.SpellCharacteristics;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.utils.Curves;
import com.smanzana.nostrummagica.utils.Projectiles;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
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
public class MortarShape extends SpellShape {
	
	public static final float MaxHDist = 40;
	public static final double OverworldGravity = 0.025D;
	
	public class MortarShapeInstance extends SpellShapeInstance {
		
		protected static final double HVel = .5; 

		private final World world;
		private final Vector3d pos;
		private final float pitch;
		private final float yaw;
		private final boolean noArc;
		private final SpellCharacteristics characteristics;
		
		public MortarShapeInstance(SpellState state, World world, Vector3d pos, float pitch, float yaw, boolean noArc, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
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

			
			caster.getServer().runAsync(new Runnable() {
			
				@Override
				public void run() {
					
					// If we have entity target, set that as dest. Otherwise, raytrace
					final Vector3d dest;
					if (target != null) {
						dest = target.getPositionVec();
					} else {
						RayTraceResult mop = RayTrace.raytraceApprox(world, getState().getSelf(), pos, dir, MaxHDist, (ent) -> {
							if (getState().getSelf() == NostrumMagica.resolveLivingEntity(ent)) {
								return false;
							}
							
							return true;
						}, .5);
						
						if (mop.getType() == RayTraceResult.Type.ENTITY) {
							dest = RayTrace.entFromRaytrace(mop).getPositionVec();
						} else if (mop.getType() == RayTraceResult.Type.BLOCK) {
							dest = mop.getHitVec();
						} else {
							dest = dir.scale(MaxHDist);
						}
					}
					
					// Figure out angle to hit destination from source. Ignore blocks and stuff
					final Vector3d startVelocity;
					final Vector3d startPos;
					if (MortarShapeInstance.this.noArc) {
						// Drop from above
						// Try not to start in the ceiling
						BlockPos.Mutable cursor = new BlockPos.Mutable();
						cursor.setPos(dest.x, dest.y + 3, dest.z); // start 3 above; best we can do
						
						for (int i = 0; i < 7; i++) {
							cursor.move(Direction.UP);
							BlockState state = world.getBlockState(cursor);
							if (!(state.getBlock() instanceof DungeonAir) && !world.isAirBlock(cursor)) {
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
					
					EntitySpellMortar projectile = new EntitySpellMortar(NostrumEntityTypes.spellMortar, MortarShapeInstance.this,
							getState().getSelf(),
							world,
							startPos,
							startVelocity,
							1.0f, OverworldGravity);
					
					projectile.setFilter((ent) -> {
						
						if (ent == null) {
							return false;
						}
						
						if (ent == getState().getSelf()) {
							return false;
						}
						
//						if (!hitAllies) {
//							if (ent instanceof IEntityTameable) {
//								if (getState().getSelf().getUniqueID().equals(((IEntityTameable) ent).getOwnerId())) {
//									return false; // We own the target entity
//								}
//							}
//							
//							if (getState().getSelf() instanceof IEntityTameable) {
//								if (ent.getUniqueID().equals(((IEntityTameable) getState().getSelf()).getOwnerId())) {
//									return false; // We own the target entity
//								}
//							}
//							
						if (Projectiles.getShooter(ent) == getState().getSelf()) {
							return false;
						}
//						}
						
						return true;
					});
					
					world.addEntity(projectile);
			
				}
			
			});
		}
		
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, world, Lists.newArrayList(pos));
		}
		
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
		
		public EMagicElement getElement() {
			// Return element on next shape
			return characteristics.getElement();
		}
	}

	private static final String ID = "mortar";
	
	public MortarShape() {
		super(ID);
	}

	@Override
	public int getManaCost() {
		return 30;
	}

	@Override
	public MortarShapeInstance createInstance(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		boolean noArc = false;
		
		// We use param's flip to indicate whether to drop from the sky or not
		if (params != null)
			noArc = params.flip;
		
		// Add direction
		pos = new Vector3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		return new MortarShapeInstance(state, world, pos, pitch, yaw, noArc, characteristics);
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
		return I18n.format("modification.mortar.bool.name", (Object[]) null);
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
	public boolean shouldTrace(SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(SpellShapePartProperties params) {
		return MaxHDist;
	}
	
}
