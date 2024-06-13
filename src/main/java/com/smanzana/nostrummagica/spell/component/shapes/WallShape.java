package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class WallShape extends AreaShape {
	
	private static class WallFacing {
		// Is this wall facing north/south? False means east/west.
		public final boolean northSouth;
		// Is this wall vertical? If not, it's lying down like when the user is facing up/down.
		public final boolean vertical;
		// Is this wall based off a grounded block? If not, it should be centered around the placement area.
		public final boolean grounded;
		public WallFacing(boolean northSouth, boolean vertical, boolean grounded) {
			this.northSouth = northSouth;
			this.vertical = vertical;
			this.grounded = grounded;
		}
	}
	private static final int WALL_HEIGHT = 3;
	
	public class WallShapeInstance extends AreaShape.AreaShapeInstance {
		
		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 20) / TICK_RATE; // 20 seconds

		private final WallFacing facing;
		private final float radius;
		private final SpellCharacteristics characteristics;
		
		private MutableBoundingBox bounds;
		
		public WallShapeInstance(ISpellState state, World world, Vector3d pos, WallFacing facing, float radius, boolean ignoreBlocks, SpellCharacteristics characteristics) {
			super(state, world, new Vector3d(Math.floor(pos.x) + .5, pos.y, Math.floor(pos.z) + .5), TICK_RATE, NUM_TICKS, 2*radius, true, !ignoreBlocks, characteristics);
			this.radius = radius;
			this.facing = facing;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// Figure out bounds
			this.bounds = MakeBounds(new BlockPos(this.pos), facing, radius);
			super.spawn(caster); // Inits listening and stuff
		}
		
		@Override
		protected boolean isInArea(LivingEntity entity) {
			final float hRadius = entity.getWidth() / 2;
			final double entMinX = entity.getPosX() - hRadius;
			final double entMaxX = entity.getPosX() + hRadius;
			final double entMinZ = entity.getPosZ() - hRadius;
			final double entMaxZ = entity.getPosZ() + hRadius;
			final double entMinY = entity.getPosY();
			final double entMaxY = entity.getPosY() + entity.getHeight();
			return entMinX < (bounds.maxX+1) && entMaxX > bounds.minX
					&& entMinY < (bounds.maxY+1) && entMaxY >= bounds.minY
					&& entMinZ < (bounds.maxZ+1) && entMaxZ > bounds.minZ
					;
		}

		@Override
		protected boolean isInArea(World world, BlockPos pos) {
			return bounds.isVecInside(pos);
		}

		@Override
		protected void doEffect() {
			final double diffX = (bounds.maxX+1) - bounds.minX;
			final double diffZ = (bounds.maxZ+1) - bounds.minZ;
			final double diffY = (bounds.maxY+1) - bounds.minY;
			
			final double yVel = (diffY / (30f));
			for (int i = 0; i < radius/2 + 1; i++) {
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						1,
						bounds.minX + NostrumMagica.rand.nextFloat() * diffX,
						bounds.minY,
						bounds.minZ + NostrumMagica.rand.nextFloat() * diffZ,
						0, // pos + posjitter
						40, 10, // lifetime + jitter
						new Vector3d(0, yVel, 0), null
						).color(characteristics.getElement().getColor()));
			}
			
			if (NostrumMagica.rand.nextBoolean()) {
				// corners
				
				for (int x : new int[] {bounds.minX, bounds.maxX + 1})
				for (int z : new int[] {bounds.minZ, bounds.maxZ + 1})
				{
					NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
							1,
							x,
							bounds.minY,
							z,
							0, // pos + posjitter
							40, 10, // lifetime + jitter
							new Vector3d(0, yVel, 0), null
							).color(characteristics.getElement().getColor()));
				}
			}
		}
	}
	
	protected static final MutableBoundingBox MakeBounds(BlockPos start, WallFacing facing, float radius) {
		final int minBlockX;
		final int maxBlockX;
		final int minBlockY;
		final int maxBlockY;
		final int minBlockZ;
		final int maxBlockZ;
		
		if (facing.vertical) {
			if (facing.northSouth) {
				minBlockX = start.getX();
				maxBlockX = start.getX();
				minBlockZ = (int) Math.floor(start.getZ() - radius);
				maxBlockZ = (int) Math.floor(start.getZ() + radius);
			} else {
				minBlockX = (int) Math.floor(start.getX() - radius);
				maxBlockX = (int) Math.floor(start.getX() + radius);
				minBlockZ = start.getZ();
				maxBlockZ = start.getZ();
			}
			
			if (facing.grounded) {
				minBlockY = start.getY();
				maxBlockY = start.getY() + (WALL_HEIGHT-1);
			} else {
				minBlockY = start.getY() - ((WALL_HEIGHT-1)/2);
				maxBlockY = start.getY() + ((WALL_HEIGHT-1)/2);
			}
		} else {
			if (facing.northSouth) {
				// use x as y
				minBlockX = start.getX() - ((WALL_HEIGHT-1)/2);
				maxBlockX = start.getX() + ((WALL_HEIGHT-1)/2);
				minBlockZ = (int) Math.floor(start.getZ() - radius);
				maxBlockZ = (int) Math.floor(start.getZ() + radius);
				minBlockY = start.getY();
				maxBlockY = start.getY();
			} else {
				// use z as y
				// range on x
				// y is just 1
				minBlockX = (int) Math.floor(start.getX() - radius);
				maxBlockX = (int) Math.floor(start.getX() + radius);
				minBlockZ = start.getZ() - ((WALL_HEIGHT-1)/2);
				maxBlockZ = start.getZ() + ((WALL_HEIGHT-1)/2);
				minBlockY = start.getY();
				maxBlockY = start.getY();
			}
		}
		
		return new MutableBoundingBox(minBlockX, minBlockY, minBlockZ,
				maxBlockX, maxBlockY, maxBlockZ);
	}
	
	protected static final WallFacing MakeFacing(LivingEntity caster, Vector3d pos, float pitch, float yaw, boolean grounded) {
		// Get N/S or E/W from target positions
		final double dz = Math.abs(caster.getPosZ() - pos.z);
		final double dx = Math.abs(caster.getPosX() - pos.x);
		final boolean northsouth = dz < dx;
		
		// Get vertical based on target positions as well.
		// Can't use pitch as it may be several shapes in
		final double casterY = (caster.getPosY() + caster.getEyeHeight());
		final double dy = Math.abs(casterY - pos.y);
		final double dh = Math.sqrt(dz * dz + dx * dx);
		// We want to require a steeper angle when aiming down
		final boolean vertical;
		if (casterY <= pos.y) {
			vertical = dy < dh;
		} else {
			vertical = dy/4 < dh;
		}
		//System.out.println("dy: " + dy + "  dh: " + dh + " casterY: " + casterY + "   => " + (vertical ? "vertical" : "not"));
		
		return new WallFacing(northsouth, vertical, grounded);
	}

	private static final String ID = "wall";
	
	public WallShape() {
		super(ID);
	}
	
	@Override
	public int getManaCost() {
		return 50;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1),
				ReagentItem.CreateStack(ReagentType.MANDRAKE_ROOT, 1));
	}

	@Override
	public WallShapeInstance createInstance(ISpellState state, World world, Vector3d pos, float pitch, float yaw,
			SpellShapePartProperties params, SpellCharacteristics characteristics) {
		BlockPos blockPos = new BlockPos(pos);
		WallFacing facing = MakeFacing(state.getCaster(), pos, pitch, yaw, !world.isAirBlock(blockPos) && world.isAirBlock(blockPos.up()));
		
		// Blindly guess if trigger put us in a wall but above us isn't that t he player
		// wants us up one
		if (facing.grounded) {
			pos = pos.add(0, 1, 0);
		}
		
		return new WallShapeInstance(state, world, pos,
				facing, Math.max(supportedFloats()[0], params.level), params.flip, characteristics);
	}

	@Override
	public String getDisplayName() {
		return "Wall";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.GLASS);
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {1f, 2f, 3f, 0f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.DIAMOND),
				new ItemStack(Items.EMERALD),
				new ItemStack(Blocks.GLASS)
				);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.wall.bool.name", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.wall.float.name", (Object[]) null);
	}
	
	@Override
	public int getWeight() {
		return 2;
	}

	@Override
	public boolean shouldTrace(SpellShapePartProperties params) {
		return false;
	}

	@Override
	public boolean supportsPreview(SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		final float radius = Math.max(supportedFloats()[0], properties.level);
		
		BlockPos blockPos = new BlockPos(pos);
		WallFacing facing = MakeFacing(state.getCaster(), pos, pitch, yaw, !world.isAirBlock(blockPos) && world.isAirBlock(blockPos.up()));
		
		// Blindly guess if trigger put us in a wall but above us isn't that t he player
		// wants us up one
		if (facing.grounded) {
			pos = pos.add(0, 1, 0);
		}
		
		MutableBoundingBox bounds = MakeBounds(new BlockPos(pos), facing, radius);
		
		List<BlockPos> positions = new ArrayList<>();
		for (int x = bounds.minX; x <= bounds.maxX; x++)
		for (int y = bounds.minY; y <= bounds.maxY; y++)
		for (int z = bounds.minZ; z <= bounds.maxZ; z++) {
			// should trigger?
			positions.add(new BlockPos(x, y, z));
		}
		state.trigger(null, world, positions);
		
		return true;
	}
}
