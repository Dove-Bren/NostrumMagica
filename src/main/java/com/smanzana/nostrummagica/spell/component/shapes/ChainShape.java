package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ChainShape extends InstantShape {

	private static final String ID = "chain";
	
	protected ChainShape(String id) {
		super(id);
	}
	
	public ChainShape() {
		this(ID);
	}
	
	protected static class ChainTriggerData extends TriggerData {

		private final Map<LivingEntity, List<LivingEntity>> links;
		
		public ChainTriggerData(List<LivingEntity> targets, World world, List<SpellLocation> locations, Map<LivingEntity, List<LivingEntity>> links) {
			super(targets, world, locations);
			this.links = links;
		}
		
		public Map<LivingEntity, List<LivingEntity>> getLinks() {
			return links;
		}
	}
	
	@Override
	protected ChainTriggerData getTargetData(ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		List<LivingEntity> ret = new ArrayList<>();
		Map<LivingEntity, List<LivingEntity>> links = new HashMap<>();
		LivingEntity target = state.getSelf();
		
		double radius = 7.0;
		if (world == null)
			world = target.world;
		
		int arc = Math.max((int) supportedFloats()[0], (int) params.level) + 1; // +1 to include center
		final boolean teamLock = params.flip;
		
		final Set<Entity> seen = new HashSet<>();
		final List<LivingEntity> next = new ArrayList<>(arc * 2);
		
		next.add(target);
		
		while (!next.isEmpty() && arc > 0) {
			final LivingEntity center = next.remove(0);
			
			if (seen.contains(center)) {
				continue;
			}
			
			seen.add(center);
			ret.add(center); // Assume all entities put in `cur` that haven't been seen yet count in terms of teams, living, etc.
			arc--;
			
			if (arc == 0) {
				break; // No sense in continuing
			}
			
			// Find any other eligible entities around them
			List<Entity> entities = world.getEntitiesInAABBexcluding(null, 
					new AxisAlignedBB(center.getPosX() - radius,
								center.getPosY() - radius,
								center.getPosZ() - radius,
								center.getPosX() + radius,
								center.getPosY() + radius,
								center.getPosZ() + radius),
					(ent) -> {
						return ent != null && NostrumMagica.resolveLivingEntity(ent) != null;
					});
			Collections.sort(entities, (a, b) -> {
				return (int) (a.getDistanceSq(center) - b.getDistanceSq(center));
			});
			
			// Note: Could do this filtering inside the entity iteration. Just filtering to living is probably okay.
			final double radiusSq = radius * radius;
			for (Entity ent : entities) {
				LivingEntity living = NostrumMagica.resolveLivingEntity(ent);
				if (living == null) {
					continue;
				}
				
				if (seen.contains(ent)) {
					continue;
				}
				
				// Check actual distance
				if (Math.abs(living.getDistanceSq(center)) > radiusSq) {
					// since things are sorted, any after this are bad
					break;
				}
				
				// Check team requirements, if required
				if (teamLock && !NostrumMagica.IsSameTeam(living, center)) {
					continue;
				}
				
				// More expensive seen check if already in next list
				if (next.contains(living)) {
					continue;
				}
				
				// Eligible!
				next.add(living);
				links.computeIfAbsent(center, (k) -> new ArrayList<>()).add(living);
			}
		}
		
		return new ChainTriggerData(ret, world, null, links);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		NonNullList<ItemStack> list = NonNullList.from(ItemStack.EMPTY,
			ReagentItem.CreateStack(ReagentType.SKY_ASH, 1),
			ReagentItem.CreateStack(ReagentType.MANDRAKE_ROOT, 1)
		);
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Chain";
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {3f, 4f, 6f, 8f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY, 
				ItemStack.EMPTY,
				new ItemStack(Items.STRING),
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Items.ENDER_PEARL)
			);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.chain.bool.name", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.chain.name", (Object[]) null);
	}
	
	@Override
	public int getWeight(SpellShapePartProperties properties) {
		return 1;
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.QUARTZ);
	}

	@Override
	public int getManaCost(SpellShapePartProperties properties) {
		return 35;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapePartProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(true, true, false);
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		// Add link links between all links that wind up showing up
		ChainTriggerData data = this.getTargetData(state, world, location, pitch, yaw, properties, characteristics);
		if (data.targets != null) {
			final float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
			for (LivingEntity source : data.targets) {
				for (LivingEntity target : data.targets) {
					if (source == target) {
						continue;
					}
					
					final List<LivingEntity> affected = data.links.get(source);
					if (affected != null && affected.contains(target)) {
						final Vector3d sourcePos = source.func_242282_l(partialTicks).add(0, source.getHeight() / 2, 0);
						final Vector3d targetPos = target.func_242282_l(partialTicks).add(0, target.getHeight() / 2, 0);
						builder.add(new SpellShapePreviewComponent.Line(sourcePos, targetPos));
					}
				}
			}
		}
		
		// This part copied from parent
		state.trigger(data.targets, data.world, data.locations);
		return (data.targets != null && !data.targets.isEmpty()) || (data.locations != null && !data.locations.isEmpty());
	}

}
