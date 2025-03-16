package com.smanzana.nostrummagica.spell.component.shapes;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams.TargetBehavior;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.BooleanSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.FloatSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeSelector;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

public class FieldShape extends AreaShape {
	
	public class FieldShapeInstance extends AreaShape.AreaShapeInstance {
		
		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 10) / TICK_RATE; // 20 seconds

		private final Vec3 origin;
		private final float radius;
		private final SpellCharacteristics characteristics;
		
		public FieldShapeInstance(ISpellState state, Level world, Vec3 pos, float radius, boolean continuous, SpellShapeProperties properties, SpellCharacteristics characteristics) {
			super(state, world, pos, TICK_RATE, NUM_TICKS, radius + .75f, continuous, affectsEntities(properties), affectsBlocks(properties), characteristics);
			this.radius = radius;
			this.origin = pos;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			super.spawn(caster); // Inits listening and stuff
		}
		
		@Override
		protected boolean isInArea(LivingEntity entity) {
			return Math.abs(entity.getY() - origin.y()) < 1
					&& origin.distanceTo(new Vec3(entity.getX(), origin.y, entity.getZ())) <= radius; // compare against our y for horizontal distance.
			// .75 wiggle room in listener means you can't be way below.
		}

		@Override
		protected boolean isInArea(Level world, BlockPos pos) {
			return pos.getY() == origin.y() &&
					(Math.abs((Math.floor(origin.x) + .5) - (pos.getX() + .5))
					+ Math.abs((Math.floor(origin.z) + .5) - (pos.getZ() + .5))) <= radius;
		}

		@Override
		protected void doEffect() {
			for (int i = 0; i < radius/2 + 1; i++) {
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						1,
						origin.x,
						origin.y + .5,
						origin.z,
						0,
						60, 10, // lifetime + jitter
						origin
						).color(characteristics.getElement().getColor())
						.setTargetBehavior(TargetBehavior.ORBIT)
						.setOrbitRadius(((NostrumMagica.rand.nextFloat() * .5f) + .5f) * radius));
			}
			
			// Spawn a border one
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					1,
					origin.x,
					origin.y + .5,
					origin.z,
					0,
					60, 10, // lifetime + jitter
					origin
					).color(characteristics.getElement().getColor())
					.setTargetBehavior(TargetBehavior.ORBIT)
					.setOrbitRadius(radius));
			
			
			// Looks very cool
//			final int slices = 10;
//			final double radPerSlice = (Math.PI * 2) / slices;
//			//for (int i = 0; i < slices; i++) {
//			{
//				final double rot = i * radPerSlice;
//				Vector3d borderPos = origin.add(Math.cos(rot) * radius, 0, Math.sin(rot) * radius);
//				
//				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//						1,
//						borderPos.x, borderPos.y + .25, borderPos.z,
//						0,
//						20, 0, // lifetime + jitter
//						new Vector3d(0, .05, 0), Vector3d.ZERO
//						).color(getState().getNextElement().getColor()));
//			}
		}
	}

	private static String ID = "field";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1),
			ReagentItem.CreateStack(ReagentType.SKY_ASH, 1),
			ReagentItem.CreateStack(ReagentType.MANI_DUST, 1)));
	
	public static final SpellShapeProperty<Boolean> ONCE = new BooleanSpellShapeProperty("once");
	public static final SpellShapeProperty<Float> RADIUS = new FloatSpellShapeProperty("radius", 1.5f, 2f, 2.5f, 3f, 4f);
	
	protected FieldShape(String key) {
		super(key);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		baseProperties.addProperty(ONCE, false).addProperty(RADIUS).addProperty(SpellShapeSelector.PROPERTY);
	}
	
	public FieldShape() {
		this(ID);
	}
	
	protected float getRadius(SpellShapeProperties properties) {
		return properties.getValue(RADIUS);
	}
	
	protected boolean getContinuous(SpellShapeProperties properties) {
		return !properties.getValue(ONCE);
	}

	@Override
	public FieldShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw,
			SpellShapeProperties properties, SpellCharacteristics characteristics) {
		return new FieldShapeInstance(state, location.world, location.hitPosition,
				getRadius(properties), getContinuous(properties), properties, characteristics);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.DRAGON_BREATH);
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (costs == null) {
			costs = NonNullList.of(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.DRAGON_BREATH),
				new ItemStack(Blocks.EMERALD_BLOCK),
				new ItemStack(Blocks.DIAMOND_BLOCK),
				new ItemStack(NostrumItems.crystalMedium, 1)
				);
		}
		return property == RADIUS ? costs : super.getPropertyItemRequirements(property);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		final float radius = getRadius(properties);
		return 45 + 5 * (int) (radius / .5f);
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 2;
	}

	@Override
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return false;
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final float radius = getRadius(properties) - .25f;
		builder.add(new SpellShapePreviewComponent.Disk(location.hitPosition.add(0, .5, 0), (float) radius));
		return super.addToPreview(builder, state, entity, location, pitch, yaw, properties, characteristics);
	}

	public SpellShapeProperties makeProps(float radius, boolean once) {
		return this.getDefaultProperties().setValue(RADIUS, radius).setValue(ONCE, once);
	}

}
