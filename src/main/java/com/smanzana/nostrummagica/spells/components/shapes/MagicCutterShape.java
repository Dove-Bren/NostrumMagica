package com.smanzana.nostrummagica.spells.components.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityChakramSpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer.ISpellSaucerShape;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.SpellCharacteristics;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.utils.Projectiles;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

/**
 * Disc projectile with a curved trajectory
 * @author Skyler
 *
 */
public class MagicCutterShape extends SpellShape {

	public static class MagicCutterShapeInstance extends SpellShapeInstance implements ISpellSaucerShape {

		private final World world;
		private final Vector3d pos;
		private final float pitch;
		private final float yaw;
		private final boolean piercing;
		private final SpellCharacteristics characteristics;
		
		private int trips;
		
		public MagicCutterShapeInstance(SpellState state, World world, Vector3d pos, float pitch, float yaw, boolean piercing, int trips, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.piercing = piercing;
			this.trips = trips;
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
			
			final MagicCutterShapeInstance self = this;
			
			caster.getServer().runAsync(new Runnable() {

				@Override
				public void run() {
					EntitySpellSaucer projectile = new EntityChakramSpellSaucer(NostrumEntityTypes.chakramSpellSaucer, self, 
							getState().getSelf(),
							world,
							pos.x, pos.y, pos.z,
							dir,
							5.0f, piercing ? PROJECTILE_RANGE/2 : PROJECTILE_RANGE, piercing, trips);
					
					world.addEntity(projectile);
				}
			
			});
		}

		@Override
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, world, Lists.newArrayList(pos), piercing); /// TODO only force split if piercing
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new BlockPos(this.pos));
			}
			else if (NostrumMagica.resolveLivingEntity(entity) == null) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null, null, piercing);
			}
		}

		@Override
		public EMagicElement getElement() {
			return characteristics.getElement();
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
	public MagicCutterShapeInstance createInstance(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		// We use param's flip to indicate whether we should be piercing or not
		boolean piercing = false;
		if (params != null)
			piercing = params.flip;
		int trips = 1;
		if (params != null)
			trips = Math.max(1, (int) params.level);
		
		// Add direction
		pos = new Vector3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		return new MagicCutterShapeInstance(state, world, pos, pitch, yaw, piercing, trips, characteristics);
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
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] { 1f, 2f, 5f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Blocks.REDSTONE_BLOCK),
				new ItemStack(Blocks.OBSIDIAN)
			);
		}
		return costs;
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
	public boolean isTerminal(SpellShapePartProperties params) {
		return true;
	}
}
