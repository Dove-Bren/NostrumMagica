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
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class BurstShape extends InstantShape {

	private static final String ID = "burst";
	
	public static final SpellShapeProperty<Float> RADIUS = new FloatSpellShapeProperty("radius", 2f, 3f, 5f, 10f);
	
	protected BurstShape(String id) {
		super(id);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(RADIUS);
	}
	
	public BurstShape() {
		this(ID);
	}
	
	protected float getRadius(SpellShapeProperties properties) {
		return properties.getValue(RADIUS);
	}
	
	@Override
	protected TriggerData getTargetData(ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties param, SpellCharacteristics characteristics) {
		
		if (!state.isPreview()) {
			this.spawnShapeEffect(state.getCaster(), null, location, param, characteristics);
		}
		
		List<LivingEntity> ret = new ArrayList<>();
		
		double radiusEnts = getRadius(param) + .5;
		final Vector3d center = location.hitPosition;
		
		for (Entity entity : location.world.getEntitiesWithinAABBExcludingEntity(null, 
				new AxisAlignedBB(center.getX() - radiusEnts,
						center.getY() - radiusEnts,
						center.getZ() - radiusEnts,
						center.getX() + radiusEnts,
						center.getY() + radiusEnts,
						center.getZ() + radiusEnts))) {
			LivingEntity living = NostrumMagica.resolveLivingEntity(entity);
			if (living != null)
				if (Math.abs(entity.getPositionVec().distanceTo(new Vector3d(center.getX(), center.getY(), center.getZ()))) <= radiusEnts)
					ret.add(living);
		}
		
		List<SpellLocation> list = new ArrayList<>();
		
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
						list.add(new SpellLocation(location.world, centerBlock.add(i, j, 0)));
					} else {
						for (int k = -yRadius; k <= yRadius; k++) {
							list.add(new SpellLocation(location.world, centerBlock.add(i, j, k)));
						}
					}
				}
				
			}
		}
		
		return new TriggerData(ret, list);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		NonNullList<ItemStack> list = NonNullList.from(ItemStack.EMPTY,
			ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1),
			ReagentItem.CreateStack(ReagentType.MANDRAKE_ROOT, 1)
		);
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Burst";
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public <T> NonNullList<ItemStack> supportedFloatCosts(SpellShapeProperty<T> property) {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY, 
				ItemStack.EMPTY,
				new ItemStack(Blocks.REDSTONE_BLOCK, 1),
				new ItemStack(NostrumItems.crystalSmall),
				new ItemStack(NostrumItems.crystalLarge)
			);
		}
		return property == RADIUS ? costs : super.supportedFloatCosts(property);
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
	public boolean shouldTrace(PlayerEntity player, SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}
	
	protected void addRangeRings(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final float radius = getRadius(properties);
		builder.add(new SpellShapePreviewComponent.Disk(location.hitPosition.add(0, .5, 0), (float) radius));
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		this.addRangeRings(builder, state, location, pitch, yaw, properties, characteristics);
		return super.addToPreview(builder, state, location, pitch, yaw, properties, characteristics);
	}
	
	@Override
	protected boolean previewBlockHits(SpellShapeProperties properties, SpellCharacteristics characteristics) {
		return false;
	}

}
