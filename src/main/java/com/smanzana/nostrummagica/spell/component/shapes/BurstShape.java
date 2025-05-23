package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.FloatSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeSelector;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.NonNullList;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class BurstShape extends InstantShape implements ISelectableShape {

	private static final String ID = "burst";
	
	public static final SpellShapeProperty<Float> RADIUS = new FloatSpellShapeProperty("radius", 2f, 3f, 5f, 10f);
	
	protected BurstShape(String id) {
		super(id);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(RADIUS).addProperty(SpellShapeSelector.PROPERTY);
	}
	
	public BurstShape() {
		this(ID);
	}
	
	public float getRadius(SpellShapeProperties properties) {
		return properties.getValue(RADIUS);
	}
	
	@Override
	protected TriggerData getTargetData(ISpellState state, LivingEntity targetEntity, SpellLocation location, float pitch, float yaw, SpellShapeProperties param, SpellCharacteristics characteristics) {
		
		if (!state.isPreview()) {
			this.spawnShapeEffect(state.getCaster(), null, location, param, characteristics);
		}
		
		List<LivingEntity> ret = new ArrayList<>();

		if (this.affectsEntities(param)) {
			double radiusEnts = getRadius(param) + .5;
			final Vec3 center = location.hitPosition;
		
			for (Entity entity : location.world.getEntities(null, 
					new AABB(center.x() - radiusEnts,
							center.y() - radiusEnts,
							center.z() - radiusEnts,
							center.x() + radiusEnts,
							center.y() + radiusEnts,
							center.z() + radiusEnts))) {
				LivingEntity living = NostrumMagica.resolveLivingEntity(entity);
				if (living != null)
					if (Math.abs(entity.position().distanceTo(new Vec3(center.x(), center.y(), center.z()))) <= radiusEnts)
						ret.add(living);
			}
		}
		
		List<SpellLocation> list = new ArrayList<>();
		
		if (this.affectsBlocks(param)) {
			final int radiusBlocks = Math.round(Math.max(2.0f, getRadius(param)));
			
			final BlockPos centerBlock = location.hitBlockPos;
			if (radiusBlocks == 0) {
				list.add(new SpellLocation(location.world, centerBlock));
			} else {
				for (int i = -radiusBlocks; i <= radiusBlocks; i++) {
					// x loop. I is offset of x
					int innerRadius = radiusBlocks - Math.abs(i);
					for (int j = -innerRadius; j <= innerRadius; j++) {
						int yRadius = innerRadius - Math.abs(j);
						// 0 means just that cell. Otherwise, +- n
						if (yRadius == 0) {
							list.add(new SpellLocation(location.world, centerBlock.offset(i, j, 0)));
						} else {
							for (int k = -yRadius; k <= yRadius; k++) {
								list.add(new SpellLocation(location.world, centerBlock.offset(i, j, k)));
							}
						}
					}
					
				}
			}
		}
		
		return new TriggerData(ret, list);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		NonNullList<ItemStack> list = NonNullList.of(ItemStack.EMPTY,
			ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1),
			ReagentItem.CreateStack(ReagentType.MANDRAKE_ROOT, 1)
		);
		
		return list;
	}

	private static NonNullList<ItemStack> costs = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (costs == null) {
			costs = NonNullList.of(ItemStack.EMPTY, 
				ItemStack.EMPTY,
				new ItemStack(Blocks.REDSTONE_BLOCK, 1),
				new ItemStack(NostrumItems.crystalSmall),
				new ItemStack(NostrumItems.crystalLarge)
			);
		}
		return property == RADIUS ? costs : super.getPropertyItemRequirements(property);
	}

	public SpellShapeProperties makeProps(float radius) {
		return this.getDefaultProperties()
				.setValue(RADIUS, radius);
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 1;
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.TNT);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		final float radius = getRadius(properties);
		return 30 + (radius > 5f ? 10 : 0);
	}

	@Override
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, this.affectsEntities(params), this.affectsBlocks(params));
	}
	
	protected void addRangeRings(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final float radius = getRadius(properties);
		builder.add(new SpellShapePreviewComponent.Disk(location.hitPosition.add(0, .5, 0), (float) radius));
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		this.addRangeRings(builder, state, location, pitch, yaw, properties, characteristics);
		return super.addToPreview(builder, state, entity, location, pitch, yaw, properties, characteristics);
	}
	
	@Override
	protected boolean previewBlockHits(SpellShapeProperties properties, SpellCharacteristics characteristics) {
		return affectsBlocks(properties) && !affectsEntities(properties);
	}

}
