package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.Random;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySpellBubble;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile.ISpellProjectileShape;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.util.Projectiles;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Create a spray of magic bubbles that continue the spell on collision.
 * Each bubble has reduced efficiency but there are multiple spawned.
 * @author Skyler
 *
 */
public class BubbleSprayShape extends SpellShape {
	
	public class BubbleSprayShapeInstance extends SpellShapeInstance implements ISpellProjectileShape {

		private final World world;
		private final Vector3d pos;
		private final float pitch;
		private final float yaw;
		private final SpellCharacteristics characteristics;
		private final float rangeScale;
		
		public BubbleSprayShapeInstance(ISpellState state, World world, Vector3d pos, float pitch, float yaw, float rangeScale, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.rangeScale = rangeScale;
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
			
			final float rangeScaleSqrt = (float) Math.sqrt(this.rangeScale);
			final Random rand = NostrumMagica.rand;
			final float inaccuracy = 30f / rangeScaleSqrt;
			final float range = SPRAY_RANGE_BASE * rangeScaleSqrt;
			
			for (int i = 0; i < 10; i++) {
				final Vector3d shootDir = dir.add(rand.nextGaussian() * (double)0.0075F * (double)inaccuracy, rand.nextGaussian() * (double)0.0075F * (double)inaccuracy, rand.nextGaussian() * (double)0.0075F * (double)inaccuracy);
				final float thisRange = (range / 4f) * (.25f + ( rand.nextFloat() * 1f));
				EntitySpellBubble bubble = new EntitySpellBubble(BubbleSprayShapeInstance.this,
						getState().getSelf(),
						pos,
						shootDir,
						thisRange, 1f,
						(20 * 8) + (int) (rand.nextDouble() * 50));
				
				world.addEntity(bubble);
			}
			NostrumMagicaSounds.BUBBLE_SPRAY.play(world, pos.getX(), pos.getY(), pos.getZ());
		}
		
		@Override
		public void onProjectileHit(SpellLocation location) {
			getState().trigger(null, world, Lists.newArrayList(location), .125f, true);
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new SpellLocation(this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(new SpellLocation(entity.getPosition()));
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null, null, .125f, true);
			}
		}
		
		@Override
		public void onProjectileEnd(Vector3d lastPos) {
			getState().triggerFail(world, new SpellLocation(lastPos));
		}
		
		public EMagicElement getElement() {
			// Return element on next shape
			return this.characteristics.getElement();
		}
	}

	private static final String ID = "bubblespray";
	private static final float SPRAY_RANGE_BASE = 3.0f;
	
	public BubbleSprayShape() {
		super(ID);
	}
	
	@Override
	public int getManaCost() {
		return 30;
	}

	@Override
	public BubbleSprayShapeInstance createInstance(ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		// Range is the float param
		final float rangeMod = Math.max(1f, params.level);
		
		return new BubbleSprayShapeInstance(state, world, location.shooterPosition, pitch, yaw, rangeMod, characteristics);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.MANI_DUST, 1));
	}

	@Override
	public String getDisplayName() {
		return "Bubble Spray";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.HONEY_BOTTLE, 1);
	}

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {1f, 1.25f, 1.5f, 2f};
	}

	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		return NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.HONEY_BOTTLE),
				new ItemStack(Items.HONEYCOMB),
				new ItemStack(Items.HONEY_BLOCK));
	}

	@Override
	public String supportedBooleanName() {
		return null;
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.bubblespray.name");
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
		return SPRAY_RANGE_BASE * Math.max(1f, params.level);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}

	@Override
	public boolean supportsPreview(SpellShapePartProperties params) {
		return false;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		return true;
	}
	
}
