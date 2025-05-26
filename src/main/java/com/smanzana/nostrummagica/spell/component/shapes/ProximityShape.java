package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listener.PlayerListener.Event;
import com.smanzana.nostrummagica.listener.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.FloatSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.NonNullList;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class ProximityShape extends SpellShape {
	
	public class ProximityShapeInstance extends SpellShapeInstance implements IGenericListener {

		private final Level world;
		private final Vec3 pos;
		private final float range;
		private final SpellCharacteristics characteristics;
		private final SpellShapeProperties properties;
		private boolean set;
		private boolean dead;
		
		public ProximityShapeInstance(ISpellState state, Level world,
				Vec3 pos, float range, SpellShapeProperties properties, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.set = false;
			this.pos = pos;
			this.range = range;
			this.characteristics = characteristics;
			this.properties = properties;
			dead = false;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// We are instant! Whoo!
			NostrumMagica.playerListener.registerTimer(this, 20, 100);
			
		}

		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object empty) {
			// We first wait 20 ticks to allow people to move around.
			if (type == Event.TIME) {
				if (dead)
					return true;
				
				NostrumMagica.Proxy.spawnSpellShapeVfx(world, ProximityShape.this, this.properties, 
						null, null, null, this.pos, this.characteristics);
				if (!set) {
					// Trap is now set!
					set = true;
					NostrumMagica.playerListener.registerProximity(this, world, pos, range / 2f);
					return false;
				}
				
				return false;
			}
			
			
			// Else we've already been set. Just BOOM
			TriggerData data = new TriggerData(
					Lists.newArrayList(entity),
					null
					);
			this.trigger(data);
			this.dead = true;
			return true;
		}
	}

	private static final String ID = "proximity";
	
	public static final SpellShapeProperty<Float> RANGE = new FloatSpellShapeProperty("range", 1f, 2f, 3f, 5f);
	
	public ProximityShape() {
		super(ID);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(RANGE);
	}
	
	public float getRange(SpellShapeProperties properties) {
		return properties.getValue(RANGE);
	}
	
	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 20;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.of(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1),
				ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1));
	}

	@Override
	public ProximityShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw,
			SpellShapeProperties params, SpellCharacteristics characteristics) {
		return new ProximityShapeInstance(state, location.world, location.hitPosition,
				getRange(params), params, characteristics);
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.TRIPWIRE_HOOK);
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (costs == null) {
			costs = NonNullList.of(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Items.DIAMOND)
				);
		}
		return property == RANGE ? costs : super.getPropertyItemRequirements(property);
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 1;
	}

	@Override
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(false, true, false);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final float radius = getRange(properties);
		builder.add(new SpellShapePreviewComponent.Disk(location.hitPosition.add(0, .5, 0), radius/2));
		return true;
	}
}
