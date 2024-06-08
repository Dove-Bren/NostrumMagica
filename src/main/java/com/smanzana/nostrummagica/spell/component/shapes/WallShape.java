package com.smanzana.nostrummagica.spell.component.shapes;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class WallShape extends AreaShape {
	
	public class WallShapeInstance extends AreaShape.AreaShapeInstance {
		
		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 20) / TICK_RATE; // 20 seconds
		private static final int BLOCK_HEIGHT = 3;

		private final boolean northsouth;
		private final float radius;
		private final SpellCharacteristics characteristics;
		
		// For blocks
		private BlockPos minPos;
		private BlockPos maxPos;
		
		// For entities, who have width
		private double minX;
		private double maxX;
		private double minZ;
		private double maxZ;
		
		public WallShapeInstance(ISpellState state, World world, Vector3d pos, boolean northsouth, float radius, boolean ignoreBlocks, SpellCharacteristics characteristics) {
			super(state, world, new Vector3d(Math.floor(pos.x) + .5, pos.y, Math.floor(pos.z) + .5), TICK_RATE, NUM_TICKS, radius + .75f, true, !ignoreBlocks, characteristics);
			this.radius = radius;
			this.northsouth = northsouth;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// Figure out bounds
			
			final int minBlockX;
			final int maxBlockX;
			final int minBlockZ;
			final int maxBlockZ;
			if (this.northsouth) {
				minX = Math.floor(pos.x) -.25;
				maxX = minX + 1.5;
				minZ = Math.floor(pos.z) - radius;
				maxZ = minZ + 1 + (radius * 2);
				
				minBlockX = (int) Math.floor(pos.x);
				maxBlockX = (int) Math.floor(pos.x);
				minBlockZ = (int) Math.floor(Math.floor(pos.z) - radius);
				maxBlockZ = (int) Math.floor(Math.floor(pos.z) + radius);
			} else {
				minX = Math.floor(pos.x) - radius;
				maxX = minX + 1 + (radius * 2);
				minZ = Math.floor(pos.z) -.25;
				maxZ = minZ + 1.5;
				
				minBlockX = (int) Math.floor(Math.floor(pos.x) - radius);
				maxBlockX = (int) Math.floor(Math.floor(pos.x) + radius);
				minBlockZ = (int) Math.floor(pos.z);
				maxBlockZ = (int) Math.floor(pos.z);
			}
			
			this.minPos = new BlockPos(minBlockX, pos.y, minBlockZ);
			this.maxPos = new BlockPos(maxBlockX, pos.y + BLOCK_HEIGHT, maxBlockZ);
			
			super.spawn(caster); // Inits listening and stuff
		}
		
		@Override
		protected boolean isInArea(LivingEntity entity) {
			return entity.getPosX() >= this.minX && entity.getPosX() <= this.maxX
					&& entity.getPosZ() >= this.minZ && entity.getPosZ() <= this.maxZ
					&& (entity.getPosY() + entity.getHeight()) >= Math.floor(this.pos.y) && entity.getPosY() <= Math.floor(this.pos.y) + BLOCK_HEIGHT;
		}

		@Override
		protected boolean isInArea(World world, BlockPos pos) {
			return pos.getX() <= this.maxPos.getX()
					&& pos.getX() >= this.minPos.getX()
					&& pos.getZ() <= this.maxPos.getZ()
					&& pos.getZ() >= this.minPos.getZ()
					&& pos.getY() <= this.maxPos.getY()
					&& pos.getY() >= this.minPos.getY();
		}

		@Override
		protected void doEffect() {
			final double diffX = maxX - minX;
			final double diffZ = maxZ - minZ;
			for (int i = 0; i < radius/2 + 1; i++) {
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						1,
						minX + NostrumMagica.rand.nextFloat() * diffX,
						Math.floor(pos.y),
						minZ + NostrumMagica.rand.nextFloat() * diffZ,
						0, // pos + posjitter
						40, 10, // lifetime + jitter
						new Vector3d(0, .05, 0), null
						).color(characteristics.getElement().getColor()));
			}
			
			if (NostrumMagica.rand.nextBoolean()) {
				// corners
				
				for (int x : new int[] {this.minPos.getX(), this.maxPos.getX() + 1})
				for (int z : new int[] {this.minPos.getZ(), this.maxPos.getZ() + 1})
				{
					NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
							1,
							x,
							Math.floor(pos.y),
							z,
							0, // pos + posjitter
							40, 10, // lifetime + jitter
							new Vector3d(0, .05, 0), null
							).color(characteristics.getElement().getColor()));
				}
			}
		}
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
		// Get N/S or E/W from target positions
		final double dz = Math.abs(state.getCaster().getPosZ() - pos.z);
		final double dx = Math.abs(state.getCaster().getPosX() - pos.x);
		final boolean northsouth = dz < dx;
		
		// Blindly guess if trigger put us in a wall but above us isn't that t he player
		// wants us up one
		BlockPos blockPos = new BlockPos(pos);
		if (!world.isAirBlock(blockPos) && world.isAirBlock(blockPos.up())) {
			pos = pos.add(0, 1, 0);
		}
		
		return new WallShapeInstance(state, world, pos,
				northsouth, Math.max(supportedFloats()[0], params.level), params.flip, characteristics);
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
		return new float[] {0f, 1f, 2f, 3f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Blocks.GLASS),
				new ItemStack(Items.DIAMOND),
				new ItemStack(Items.EMERALD)
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
		int unused; // Revisit
		return false;
	}
}
