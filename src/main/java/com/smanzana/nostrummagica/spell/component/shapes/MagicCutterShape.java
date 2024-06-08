package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityChakramSpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile.ISpellProjectileShape;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
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

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

/**
 * Disc projectile with a curved trajectory
 * @author Skyler
 *
 */
public class MagicCutterShape extends SpellShape {

	public static class MagicCutterShapeInstance extends SpellShapeInstance implements ISpellProjectileShape {

		private final World world;
		private final Vector3d pos;
		private final float pitch;
		private final float yaw;
		private final SpellCharacteristics characteristics;
		
		public MagicCutterShapeInstance(ISpellState state, World world, Vector3d pos, float pitch, float yaw, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
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
			
			EntitySpellSaucer projectile = new EntityChakramSpellSaucer(this, 
					world,
					getState().getSelf(),
					pos,
					dir,
					5.0f, PROJECTILE_RANGE);
			
			world.addEntity(projectile);
		}

		@Override
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, world, Lists.newArrayList(pos), true);
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new BlockPos(this.pos));
			}
			else if (NostrumMagica.resolveLivingEntity(entity) == null) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null, null, true);
			}
		}

		@Override
		public EMagicElement getElement() {
			return characteristics.getElement();
		}

		@Override
		public void onProjectileEnd(Vector3d pos) {
			getState().triggerFail(world, pos);
		}
	}
	
	private static final String ID = "cutter";
	private static final double PROJECTILE_RANGE = 50.0;
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.SKY_ASH, 1)));
	
	protected MagicCutterShape(String key) {
		super(key);
	}
	
	public MagicCutterShape() {
		this(ID);
	}
	
	@Override
	public MagicCutterShapeInstance createInstance(ISpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		// Add direction
		pos = new Vector3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		return new MagicCutterShapeInstance(state, world, pos, pitch, yaw, characteristics);
	}
	
	@Override
	public int getManaCost() {
		return 20;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public String getDisplayName() {
		return "Mana Cutter";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.SNOWBALL, 1);
	}

	@Override
	public boolean supportsBoolean() {
		return false;
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
		return I18n.format("modification.cutter.name", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.cutter.trips", (Object[]) null);
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
		return PROJECTILE_RANGE;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}

	@Override
	public boolean supportsPreview(SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		final Vector3d dir;
		final LivingEntity self = state.getSelf();
		if (self instanceof MobEntity && ((MobEntity) self).getAttackTarget() != null) {
			MobEntity ent = (MobEntity) self  ;
			dir = ent.getAttackTarget().getPositionVec().add(0.0, ent.getHeight() / 2.0, 0.0)
					.subtract(self.getPosX(), self.getPosY() + self.getEyeHeight(), self.getPosZ());
		} else {
			dir = Projectiles.getVectorForRotation(pitch, yaw);
		}
		
		pos = new Vector3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		RayTraceResult trace = RayTrace.raytrace(world, state.getSelf(), pos, dir, (float) PROJECTILE_RANGE, new RayTrace.OtherLiving(state.getSelf()));
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
			final Vector3d dest = pos.add(dir.normalize().scale(PROJECTILE_RANGE));
			builder.add(new SpellShapePreviewComponent.Line(pos.add(0, -.25, 0), new Vector3d((int) dest.getX() + .5, (int) dest.getY() + .5, (int) dest.getZ() + .5)));
			return true;
		}
	}
}
