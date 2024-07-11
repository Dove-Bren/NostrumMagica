package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.ChakramSpellSaucerEntity;
import com.smanzana.nostrummagica.entity.SpellProjectileEntity.ISpellProjectileShape;
import com.smanzana.nostrummagica.entity.SpellSaucerEntity;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.util.Projectiles;
import com.smanzana.nostrummagica.util.RayTrace;

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
			
			SpellSaucerEntity projectile = new ChakramSpellSaucerEntity(this, 
					world,
					getState().getSelf(),
					pos,
					dir,
					5.0f, PROJECTILE_RANGE);
			
			world.addEntity(projectile);
		}

		@Override
		public void onProjectileHit(SpellLocation location) {
			getState().trigger(null, Lists.newArrayList(location), 1f, true);
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new SpellLocation(world, this.pos));
			}
			else if (NostrumMagica.resolveLivingEntity(entity) == null) {
				onProjectileHit(new SpellLocation(entity.world, entity.getPosition()));
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null, 1f, true);
			}
		}

		@Override
		public EMagicElement getElement() {
			return characteristics.getElement();
		}

		@Override
		public void onProjectileEnd(Vector3d pos) {
			getState().triggerFail(new SpellLocation(world, pos));
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
	public MagicCutterShapeInstance createInstance(ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		return new MagicCutterShapeInstance(state, location.world, location.shooterPosition, pitch, yaw, characteristics);
	}
	
	@Override
	public int getManaCost(SpellShapeProperties properties) {
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
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final Vector3d dir;
		final LivingEntity self = state.getSelf();
		if (self instanceof MobEntity && ((MobEntity) self).getAttackTarget() != null) {
			MobEntity ent = (MobEntity) self  ;
			dir = ent.getAttackTarget().getPositionVec().add(0.0, ent.getHeight() / 2.0, 0.0)
					.subtract(self.getPosX(), self.getPosY() + self.getEyeHeight(), self.getPosZ());
		} else {
			dir = Projectiles.getVectorForRotation(pitch, yaw);
		}
		
		RayTraceResult trace = RayTrace.raytrace(location.world, state.getSelf(), location.shooterPosition, dir, (float) PROJECTILE_RANGE, new RayTrace.OtherLiving(state.getSelf()));
		if (trace.getType() == RayTraceResult.Type.BLOCK) {
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), Vector3d.copyCentered(RayTrace.blockPosFromResult(trace))));
			state.trigger(null, Lists.newArrayList(new SpellLocation(location.world, trace)));
			return true;
		} else if (trace.getType() == RayTraceResult.Type.ENTITY && RayTrace.livingFromRaytrace(trace) != null) {
			final LivingEntity living = RayTrace.livingFromRaytrace(trace);
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), living.getPositionVec().add(0, living.getHeight() / 2, 0)));
			state.trigger(Lists.newArrayList(living), null);
			return true;
		} else {
			final Vector3d dest = location.shooterPosition.add(dir.normalize().scale(PROJECTILE_RANGE));
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), new Vector3d((int) dest.getX() + .5, (int) dest.getY() + .5, (int) dest.getZ() + .5)));
			return true;
		}
	}
}
