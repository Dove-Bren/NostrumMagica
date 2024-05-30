package com.smanzana.nostrummagica.spells.components.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.SpellCharacteristics;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ProximityShape extends SpellShape {
	
	public class ProximityShapeInstance extends SpellShapeInstance implements IGenericListener {

		private final World world;
		private final Vector3d pos;
		private final float range;
		private final SpellCharacteristics characteristics;
		private boolean set;
		private boolean dead;
		
		public ProximityShapeInstance(SpellState state, World world,
				Vector3d pos, float range, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.set = false;
			this.pos = pos;
			this.range = range;
			this.characteristics = characteristics;
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
				
				NostrumMagica.instance.proxy.spawnEffect(world, new SpellComponentWrapper(NostrumSpellShapes.Proximity),
						null, null, null, this.pos, new SpellComponentWrapper(this.characteristics.getElement()), false, range);
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
					null,
					null
					);
			this.trigger(data);
			this.dead = true;
			return true;
		}
	}

	private static final String ID = "proximity";
	
	public ProximityShape() {
		super(ID);
	}
	
	@Override
	public int getManaCost() {
		return 30;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1),
				ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1));
	}

	@Override
	public ProximityShapeInstance createInstance(SpellState state, World world, Vector3d pos, float pitch, float yaw,
			SpellShapePartProperties params, SpellCharacteristics characteristics) {
		return new ProximityShapeInstance(state, world, pos,
				Math.max(supportedFloats()[0], params.level), characteristics);
	}

	@Override
	public String getDisplayName() {
		return "Proximity";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.TRIPWIRE_HOOK);
	}

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {1f, 2f, 3f, 5f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Items.DIAMOND)
				);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return null;
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.proximity.name", (Object[]) null);
	}
	
	@Override
	public int getWeight() {
		return 1;
	}

	@Override
	public boolean shouldTrace(SpellShapePartProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(false, true, false);
	}
}
