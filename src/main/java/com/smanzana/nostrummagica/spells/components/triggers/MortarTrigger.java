package com.smanzana.nostrummagica.spells.components.triggers;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.DungeonAir;
import com.smanzana.nostrummagica.entity.EntitySpellMortar;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.utils.Curves;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Projectile that flies in an arc / straight down on a target instead in a straight line.
 * Does no tracking. Spawns an entity and waits for it to collide.
 * Other is always set to current self
 * @author Skyler
 *
 */
public class MortarTrigger extends SpellTrigger {
	
	public static final float MaxHDist = 40;
	public static final double OverworldGravity = 0.025D;
	
	public class MortarTriggerInstance extends SpellTrigger.SpellTriggerInstance {
		
		protected static final double HVel = .5; 

		private World world;
		private Vec3d pos;
		private float pitch;
		private float yaw;
		private boolean noArc;
		
		public MortarTriggerInstance(SpellState state, World world, Vec3d pos, float pitch, float yaw, boolean noArc) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.noArc = noArc;
		}
		
		@Override
		public void init(EntityLivingBase caster) {
			
			// Do a little more work of getting a good vector for things
			// that aren't players
			final Vec3d dir;
			final EntityLivingBase target;
			if (caster instanceof EntityLiving && ((EntityLiving) caster).getAttackTarget() != null) {
				EntityLiving ent = (EntityLiving) caster  ;
				target = ent.getAttackTarget(); // We already know target
				dir = null;
			} else {
				target = null; // Solve for target on main thread with raytrace
				dir = SeekingBulletTrigger.getVectorForRotation(pitch, yaw);
			}

			
			final MortarTriggerInstance self = this;
			
			caster.getServer().addScheduledTask(new Runnable() {
			
				@Override
				public void run() {
					
					// If we have entity target, set that as dest. Otherwise, raytrace
					final Vec3d dest;
					if (target != null) {
						dest = target.getPositionVector();
					} else {
						RayTraceResult mop = RayTrace.raytraceApprox(world, pos, dir, MaxHDist, (ent) -> {
							if (getState().getSelf() == NostrumMagica.resolveEntityLiving(ent)) {
								return false;
							}
							
							return true;
						}, .5);
						
						if (mop.typeOfHit == RayTraceResult.Type.ENTITY) {
							dest = mop.entityHit.getPositionVector();
						} else if (mop.typeOfHit == RayTraceResult.Type.BLOCK) {
							dest = mop.hitVec;
						} else {
							dest = dir.scale(MaxHDist);
						}
					}
					
					// Figure out angle to hit destination from source. Ignore blocks and stuff
					final Vec3d startVelocity;
					final Vec3d startPos;
					if (self.noArc) {
						// Drop from above
						// Try not to start in the ceiling
						MutableBlockPos cursor = new MutableBlockPos();
						cursor.setPos(dest.x, dest.y + 3, dest.z); // start 3 above; best we can do
						
						for (int i = 0; i < 7; i++) {
							cursor.move(EnumFacing.UP);
							IBlockState state = world.getBlockState(cursor);
							if (!(state.getBlock() instanceof DungeonAir) && world.isBlockNormalCube(cursor, false)) {
								// can't go here. Go back down and bail
								cursor.move(EnumFacing.DOWN);
								break;
							}
						}
						
						startPos = new Vec3d(dest.x, cursor.getY(), dest.z);
						startVelocity = new Vec3d(0, -.25, 0);
					} else {
						startPos = pos;
						startVelocity = Curves.getMortarArcVelocity(pos, dest, HVel, OverworldGravity);
					}
					
					EntitySpellMortar projectile = new EntitySpellMortar(self,
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
//							if (Projectiles.getShooter(ent) == getState().getSelf()) {
//								return false;
//							}
//						}
						
						return true;
					});
					
					world.spawnEntity(projectile);
			
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
			else if (null == NostrumMagica.resolveEntityLiving(entity)) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveEntityLiving(entity)), Lists.newArrayList(getState().getOther()), null, null);
			}
		}
		
		public EMagicElement getElement() {
			// Return element on next shape
			return getState().getNextElement();
		}
	}

	private static final String TRIGGER_KEY = "trigger_mortar";
	private static MortarTrigger instance = null;
	
	public static MortarTrigger instance() {
		if (instance == null)
			instance = new MortarTrigger();
		
		return instance;
	}
	
	private MortarTrigger() {
		super(TRIGGER_KEY);
	}

	@Override
	public int getManaCost() {
		return 30;
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw, SpellPartParam params) {
		boolean noArc = false;
		
		// We use param's flip to indicate whether to drop from the sky or not
		if (params != null)
			noArc = params.flip;
		
		// Add direction
		pos = new Vec3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		return new MortarTriggerInstance(state, world, pos, pitch, yaw, noArc);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1));
	}

	@Override
	public String getDisplayName() {
		return "Mortar";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.FIRE_CHARGE, 1, OreDictionary.WILDCARD_VALUE);
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
	
}
