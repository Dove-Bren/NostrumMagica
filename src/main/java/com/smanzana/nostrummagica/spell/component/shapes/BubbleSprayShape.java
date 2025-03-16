package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.Random;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.SpellBubbleEntity;
import com.smanzana.nostrummagica.entity.SpellProjectileEntity.ISpellProjectileShape;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.FloatSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.IntSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.util.Projectiles;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.NonNullList;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

/**
 * Create a spray of magic bubbles that continue the spell on collision.
 * Each bubble has reduced efficiency but there are multiple spawned.
 * @author Skyler
 *
 */
public class BubbleSprayShape extends SpellShape {
	
	public class BubbleSprayShapeInstance extends SpellShapeInstance implements ISpellProjectileShape {

		private final Level world;
		private final Vec3 pos;
		private final float pitch;
		private final float yaw;
		private final SpellCharacteristics characteristics;
		private final float rangeScale;
		private final int bubbleCount;
		
		public BubbleSprayShapeInstance(ISpellState state, Level world, Vec3 pos, float pitch, float yaw, float rangeScale, int bubbleCount, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.rangeScale = rangeScale;
			this.bubbleCount = bubbleCount;
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
			
			final float rangeScaleSqrt = (float) Math.sqrt(this.rangeScale);
			final Random rand = NostrumMagica.rand;
			final float inaccuracy = 30f / rangeScaleSqrt;
			final float range = SPRAY_RANGE_BASE * rangeScaleSqrt;
			
			for (int i = 0; i < bubbleCount; i++) {
				final Vec3 shootDir = dir.add(rand.nextGaussian() * (double)0.0075F * (double)inaccuracy, rand.nextGaussian() * (double)0.0075F * (double)inaccuracy, rand.nextGaussian() * (double)0.0075F * (double)inaccuracy);
				final float thisRange = (range / 4f) * (.25f + ( rand.nextFloat() * 1f));
				SpellBubbleEntity bubble = new SpellBubbleEntity(BubbleSprayShapeInstance.this,
						getState().getSelf(),
						pos,
						shootDir,
						thisRange, 1f,
						(20 * 8) + (int) (rand.nextDouble() * 50));
				
				world.addFreshEntity(bubble);
			}
			NostrumMagicaSounds.BUBBLE_SPRAY.play(world, pos.x(), pos.y(), pos.z());
		}
		
		@Override
		public void onProjectileHit(SpellLocation location) {
			getState().trigger(null, Lists.newArrayList(location), .125f, true);
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new SpellLocation(this.world, this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(new SpellLocation(entity.level, entity.blockPosition()));
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null, .125f, true);
			}
		}
		
		@Override
		public void onProjectileEnd(Vec3 lastPos) {
			getState().triggerFail(new SpellLocation(world, lastPos));
		}
		
		public EMagicElement getElement() {
			// Return element on next shape
			return this.characteristics.getElement();
		}
	}

	private static final String ID = "bubblespray";
	private static final float SPRAY_RANGE_BASE = 3.0f;
	
	public static final SpellShapeProperty<Float> RANGE = new FloatSpellShapeProperty("range", 1f, 1.5f, 2f, 3f, 5f);
	public static final SpellShapeProperty<Integer> COUNT = new IntSpellShapeProperty("count", 10, 16, 20, 24);
	
	public BubbleSprayShape() {
		super(ID);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(RANGE).addProperty(COUNT);
	}
	
	protected float getRangeMod(SpellShapeProperties properties) {
		return properties.getValue(RANGE);
	}
	
	protected int getBubbleCount(SpellShapeProperties properties) {
		return properties.getValue(COUNT);
	}
	
	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 30 + (getBubbleCount(properties) - 10);
	}

	@Override
	public BubbleSprayShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		final float rangeMod = getRangeMod(params);
		final int bubbleCount = getBubbleCount(params);
		return new BubbleSprayShapeInstance(state, location.world, location.shooterPosition, pitch, yaw, rangeMod, bubbleCount, characteristics);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.of(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.MANI_DUST, 1));
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.HONEY_BOTTLE, 1);
	}

	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (property == RANGE) {
			return NonNullList.of(ItemStack.EMPTY,
					ItemStack.EMPTY,
					new ItemStack(Items.HONEY_BOTTLE),
					new ItemStack(Items.HONEYCOMB),
					new ItemStack(Items.HONEY_BLOCK),
					new ItemStack(Items.HONEYCOMB_BLOCK));
		}
		if (property == COUNT) {
			return NonNullList.of(ItemStack.EMPTY,
					ItemStack.EMPTY,
					new ItemStack(NostrumItems.crystalSmall),
					new ItemStack(NostrumItems.resourceWispPebble),
					new ItemStack(NostrumItems.resourceSpriteCore));
		}
		return super.getPropertyItemRequirements(property);
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
		return SPRAY_RANGE_BASE * this.getRangeMod(params);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		return true;
	}
	
}
