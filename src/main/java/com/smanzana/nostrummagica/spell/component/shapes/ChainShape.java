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
import com.smanzana.nostrummagica.spell.component.BooleanSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.IntSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeSelector;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.util.WorldUtil.IBlockWalker;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ChainShape extends InstantShape implements ISelectableShape {

	private static final String ID = "chain";
	
	public static final SpellShapeProperty<Boolean> TEAM_LOCK = new BooleanSpellShapeProperty("team_lock");
	public static final SpellShapeProperty<Integer> JUMPS = new IntSpellShapeProperty("jumps", 3, 4, 6, 8);
	
	protected ChainShape(String id) {
		super(id);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(TEAM_LOCK).addProperty(JUMPS).addProperty(SpellShapeSelector.PROPERTY);
	}
	
	public ChainShape() {
		this(ID);
	}
	
	protected int getMaxArcs(SpellShapeProperties properties) {
		return properties.getValue(JUMPS);
	}
	
	protected boolean getTeamLock(SpellShapeProperties properties) {
		return properties.getValue(TEAM_LOCK);
	}
	
	protected static class ChainTriggerData extends TriggerData {

		private final Map<LivingEntity, List<LivingEntity>> entLinks;
		private final Map<SpellLocation, List<SpellLocation>> blockLinks;
		
		public ChainTriggerData(List<LivingEntity> targets, World world, List<SpellLocation> locations,
				Map<LivingEntity, List<LivingEntity>> entityLinks,
				Map<SpellLocation, List<SpellLocation>> blockLinks) {
			super(targets, locations);
			this.entLinks = entityLinks;
			this.blockLinks = blockLinks;
		}
		
		public Map<LivingEntity, List<LivingEntity>> getEntityLinks() {
			return entLinks;
		}
		
		public Map<SpellLocation, List<SpellLocation>> getBlockLinks() {
			return blockLinks;
		}
	}
	
	protected void getEntLinks(List<LivingEntity> entsAffected, Map<LivingEntity, List<LivingEntity>> entLinks, LivingEntity target, World world, double radius, int arc, boolean teamLock) {
		final Set<Entity> seen = new HashSet<>();
		final List<LivingEntity> next = new ArrayList<>(arc * 2);
		
		next.add(target);
		
		while (!next.isEmpty() && arc > 0) {
			final LivingEntity center = next.remove(0);
			
			if (seen.contains(center)) {
				continue;
			}
			
			seen.add(center);
			entsAffected.add(center); // Assume all entities put in `cur` that haven't been seen yet count in terms of teams, living, etc.
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
				entLinks.computeIfAbsent(center, (k) -> new ArrayList<>()).add(living);
			}
		}
	}
	
	protected boolean blocksAreSimilar(BlockState state, BlockState state2) {
		return state == state2
				|| state.getBlock() == state2.getBlock();
	}
	
	protected void getBlockLinks(List<SpellLocation> locsAffected, Map<SpellLocation, List<SpellLocation>> locLinks, SpellLocation location, double radius, int arc) {
		int[] arcs = {arc};
		
		
		WorldUtil.WalkConnectedBlocks(location.world, location.selectedBlockPos, new IBlockWalker() {

			@Override
			public boolean canVisit(IBlockReader world, BlockPos startPos, BlockState startState, BlockPos pos, BlockState state, int distance) {
				return blocksAreSimilar(startState, state);
			}

			@Override
			public boolean walk(IBlockReader world, BlockPos startPos, BlockState startState, BlockPos pos, BlockState state, int distance, int walkCount) {
				final SpellLocation loc = new SpellLocation((World) world, pos);
				locsAffected.add(loc);
				locLinks.computeIfAbsent(location, (l) -> new ArrayList<>()).add(loc);
				
				if (--arcs[0] <= 0) {
					return true; // stop walking; all done
				}
				
				return false;
			}
			
		}, arc * arc);
	}
	
	@Override
	protected ChainTriggerData getTargetData(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		double radius = 7.0;
		World world = location.world;
		if (world == null)
			world = state.getSelf().getEntityWorld();
		int arc = getMaxArcs(params) + 1; // +1 to include center

		Map<LivingEntity, List<LivingEntity>> entLinks = new HashMap<>();
		Map<SpellLocation, List<SpellLocation>> blockLinks = new HashMap<>();
		List<LivingEntity> entsAffected = new ArrayList<>();
		List<SpellLocation> locsAffected = new ArrayList<>();

		if (this.affectsEntities(params)) {
			final LivingEntity firstEnt;
			if (entity != null) {
				firstEnt = entity;
			} else {
				// Find nearest start entity
				final Vector3d center = location.hitPosition;
				List<Entity> entities = world.getEntitiesInAABBexcluding(null, 
						new AxisAlignedBB(center.getX() - radius,
									center.getY() - radius,
									center.getZ() - radius,
									center.getX() + radius,
									center.getY() + radius,
									center.getZ() + radius),
						(ent) -> {
							return ent != null && NostrumMagica.resolveLivingEntity(ent) != null;
						});
				Collections.sort(entities, (a, b) -> {
					return (int) (a.getDistanceSq(center) - b.getDistanceSq(center));
				});
				
				// First element is shortest distance
				firstEnt = entities.isEmpty() ? null : (LivingEntity) entities.get(0);
			}
			
			if (firstEnt != null) {
				this.getEntLinks(entsAffected, entLinks, firstEnt, world, radius, arc, getTeamLock(params));
			}
		}
		if (this.affectsBlocks(params)) {
			this.getBlockLinks(locsAffected, blockLinks, location, radius, arc);
		}
		
		return new ChainTriggerData(entsAffected, world, locsAffected, entLinks, blockLinks);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		NonNullList<ItemStack> list = NonNullList.from(ItemStack.EMPTY,
			ReagentItem.CreateStack(ReagentType.SKY_ASH, 1),
			ReagentItem.CreateStack(ReagentType.MANDRAKE_ROOT, 1)
		);
		
		return list;
	}

	private static NonNullList<ItemStack> JUMP_COSTS = null;
	private static NonNullList<ItemStack> LOCK_COSTS = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (JUMP_COSTS == null) {
			JUMP_COSTS = NonNullList.from(ItemStack.EMPTY, 
				ItemStack.EMPTY,
				new ItemStack(Items.STRING),
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Items.ENDER_PEARL)
			);
			LOCK_COSTS = NonNullList.from(ItemStack.EMPTY, 
					ItemStack.EMPTY,
					new ItemStack(Items.COMPASS)
				);
		}
		return property == JUMPS ? JUMP_COSTS 
				: property == TEAM_LOCK ? LOCK_COSTS
				: super.getPropertyItemRequirements(property);
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 1;
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.QUARTZ);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 35;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, this.affectsEntities(params), this.affectsBlocks(params));
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		// Add link links between all links that wind up showing up
		ChainTriggerData data = this.getTargetData(state, entity, location, pitch, yaw, properties, characteristics);
		if (data.targets != null) {
			final float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
			for (LivingEntity source : data.targets) {
				for (LivingEntity target : data.targets) {
					if (source == target) {
						continue;
					}
					
					final List<LivingEntity> affected = data.entLinks.get(source);
					if (affected != null && affected.contains(target)) {
						final Vector3d sourcePos = source.func_242282_l(partialTicks).add(0, source.getHeight() / 2, 0);
						final Vector3d targetPos = target.func_242282_l(partialTicks).add(0, target.getHeight() / 2, 0);
						builder.add(new SpellShapePreviewComponent.Line(sourcePos, targetPos));
					}
				}
			}
		}
		
		// This part copied from parent
		state.trigger(data.targets, data.locations);
		return (data.targets != null && !data.targets.isEmpty()) || (data.locations != null && !data.locations.isEmpty());
	}

	public SpellShapeProperties makeProps(int jumps, boolean teamLock) {
		return this.getDefaultProperties()
				.setValue(JUMPS, jumps)
				.setValue(TEAM_LOCK, teamLock);
	}

}
