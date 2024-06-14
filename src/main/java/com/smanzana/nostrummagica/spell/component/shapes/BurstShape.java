package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BurstShape extends InstantShape {

	private static final String ID = "burst";
	
	protected BurstShape(String id) {
		super(id);
	}
	
	public BurstShape() {
		this(ID);
	}
	
	protected float getRadius(SpellShapePartProperties properties) {
		return Math.max(supportedFloats()[0], properties.level);
	}
	
	@Override
	protected TriggerData getTargetData(ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties param, SpellCharacteristics characteristics) {
		
		if (!state.isPreview()) {
			this.spawnShapeEffect(state.getCaster(), null, world, location, param, characteristics);
		}
		
		List<LivingEntity> ret = new ArrayList<>();
		
		double radiusEnts = getRadius(param) + .5;
		final Vector3d center = location.hitPosition;
		
		for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, 
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
			list.add(new SpellLocation(centerBlock));
		} else {
			for (int i = -radiusBlocks; i <= radiusBlocks; i++) {
				// x loop. I is offset of x
				int innerRadius = radiusBlocks - Math.abs(i);
				for (int j = -innerRadius; j <= innerRadius; j++) {
					int yRadius = innerRadius - Math.abs(j);
					// 0 means just that cell. Otherwise, +- n
					if (yRadius == 0) {
						list.add(new SpellLocation(centerBlock.add(i, j, 0)));
					} else {
						for (int k = -yRadius; k <= yRadius; k++) {
							list.add(new SpellLocation(centerBlock.add(i, j, k)));
						}
					}
				}
				
			}
		}
		
		return new TriggerData(ret, world, list);
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

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {2f, 3f, 5f, 10f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY, 
				ItemStack.EMPTY,
				new ItemStack(Blocks.REDSTONE_BLOCK, 1),
				new ItemStack(NostrumItems.crystalSmall),
				new ItemStack(NostrumItems.crystalLarge)
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
		return I18n.format("modification.aoe.name", (Object[]) null);
	}
	
	@Override
	public int getWeight(SpellShapePartProperties properties) {
		return 1;
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.TNT);
	}

	@Override
	public int getManaCost(SpellShapePartProperties properties) {
		final float radius = getRadius(properties);
		return 30 + (radius > 5f ? 10 : 0);
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapePartProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}
	
	protected void addRangeRings(SpellShapePreview builder, ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		final float radius = getRadius(properties);
		builder.add(new SpellShapePreviewComponent.Disk(location.hitPosition.add(0, .5, 0), (float) radius));
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		this.addRangeRings(builder, state, world, location, pitch, yaw, properties, characteristics);
		return super.addToPreview(builder, state, world, location, pitch, yaw, properties, characteristics);
	}
	
	@Override
	protected boolean previewBlockHits(SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		return false;
	}

}
