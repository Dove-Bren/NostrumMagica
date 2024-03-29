package com.smanzana.nostrummagica.spells.components.shapes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChainShape extends SpellShape {

	private static final String SHAPE_KEY = "chain";
	private static ChainShape instance = null;
	
	public static ChainShape instance() {
		if (instance == null)
			instance = new ChainShape();
		
		return instance;
	}
	
	private ChainShape() {
		super(SHAPE_KEY);
	}
	
	@Override
	protected List<LivingEntity> getTargets(SpellPartParam param, LivingEntity target, World world, BlockPos pos) {
		List<LivingEntity> ret = new LinkedList<>();
		
		if (target == null) {
			return ret;
		}
		
		double radius = 7.0;
		if (world == null)
			world = target.world;
		
		int arc = Math.max((int) supportedFloats()[0], (int) param.level) + 1; // +1 to include center
		final boolean teamLock = param.flip;
		
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
					new AxisAlignedBB(center.posX - radius,
								center.posY - radius,
								center.posZ - radius,
								center.posX + radius,
								center.posY + radius,
								center.posZ + radius),
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
				
				// Eligible!
				next.add(living);
			}
		}
		
		return ret;
	}

	@Override
	protected List<BlockPos> getTargetLocations(SpellPartParam param, LivingEntity target, World world,
			BlockPos pos) {
		List<BlockPos> list = new LinkedList<>();
		
		list.add(pos);
		
		return list;
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

}
