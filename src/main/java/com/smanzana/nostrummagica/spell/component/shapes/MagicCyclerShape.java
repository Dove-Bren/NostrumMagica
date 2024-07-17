package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.CyclerSpellSaucerEntity;
import com.smanzana.nostrummagica.entity.SpellProjectileEntity.ISpellProjectileShape;
import com.smanzana.nostrummagica.entity.SpellSaucerEntity;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.IntSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeSelector;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

/**
 * Disc projectile with a curved trajectory
 * @author Skyler
 *
 */
public class MagicCyclerShape extends SpellShape implements ISelectableShape {
	
	public static class MagicCyclerShapeInstance extends SpellShapeInstance implements ISpellProjectileShape {

		private final World world;
		private final Vector3d pos;
		private final boolean hitEnts;
		private final boolean hitBlocks;
		private final float duration;
		private final SpellCharacteristics characteristics;
		
		public MagicCyclerShapeInstance(ISpellState state, World world, Vector3d pos, boolean hitEnts, boolean hitBlocks, float duration, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.hitEnts = hitEnts;
			this.hitBlocks = hitBlocks;
			this.duration = duration;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			SpellSaucerEntity projectile = new CyclerSpellSaucerEntity(getState().getSelf().world, getState().getSelf(),
					MagicCyclerShapeInstance.this,
					5.0f, (int) duration * 20, hitBlocks, false);
			
			world.addEntity(projectile);
		}

		@Override
		public void onProjectileHit(SpellLocation location) {
			if (hitBlocks) {
				getState().trigger(null, Lists.newArrayList(location), 1f, true);
			}
			// else ignore
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new SpellLocation(world, this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(new SpellLocation(entity.world, entity.getPosition()));
			} else if (hitEnts) {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null);
			}
		}

		@Override
		public EMagicElement getElement() {
			return characteristics.getElement();
		}

		@Override
		public void onProjectileEnd(Vector3d pos) {
			getState().triggerFail(new SpellLocation(world, pos));
		}
	}
	
	private static final String ID = "vortex_blade";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.GINSENG, 1),
			ReagentItem.CreateStack(ReagentType.SKY_ASH, 1)));
	
	public static final SpellShapeProperty<Integer> DURATION = new IntSpellShapeProperty("duration", 10, 20, 50);
	
	protected MagicCyclerShape(String key) {
		super(key);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		baseProperties.addProperty(DURATION).addProperty(SpellShapeSelector.PROPERTY);
	}
	
	public MagicCyclerShape() {
		this(ID);
	}
	
	protected float getDurationSecs(SpellShapeProperties properties) {
		return properties.getValue(DURATION);
	}
	
	@Override
	public MagicCyclerShapeInstance createInstance(ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		final boolean hitEnts = affectsEntities(params);
		final boolean hitBlocks = affectsBlocks(params);
		float duration = this.getDurationSecs(params);
		return new MagicCyclerShapeInstance(state, location.world, location.shooterPosition, hitEnts, hitBlocks, duration, characteristics);
	}
	
	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 25;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.COMPASS, 1);
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.COAL),
				new ItemStack(Blocks.COAL_BLOCK)
			);
		}
		return property == DURATION ? costs : super.getPropertyItemRequirements(property);
	}
	
	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 1;
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
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		float radius = (float) (CyclerSpellSaucerEntity.CYCLER_RADIUS + .5); // .5 for half the width of the saucer itself
		builder.add(new SpellShapePreviewComponent.Disk(location.hitPosition.add(0, .5, 0), (float) radius));
		return super.addToPreview(builder, state, location, pitch, yaw, properties, characteristics);
	}
}
