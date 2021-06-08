package com.smanzana.nostrummagica.integration.aetheria.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrumaetheria.api.blocks.AetherTickingTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.ParticleGlowOrb;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AetherInfuser extends BlockContainer {
	
	public static final String ID = "infuser_multiblk";
	
	private static final PropertyBool MASTER = PropertyBool.create("master");
	
	private static AetherInfuser instance = null;
	public static AetherInfuser instance() {
		if (instance == null)
			instance = new AetherInfuser();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(AetherInfuserTileEntity.class, ID + "_entity");
	}
	
	public AetherInfuser() {
		super(Material.ROCK, MapColor.OBSIDIAN);
		this.setUnlocalizedName(ID);
		this.setHardness(5.0f);
		this.setResistance(8.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(MASTER, false));
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if (meta != 0) {
			return new AetherInfuserTileEntity();
		}
		
		return null;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.SOLID;
    }
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return true;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isTranslucent(IBlockState state) {
		return false;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, MASTER);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(MASTER, meta != 0);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(MASTER) ? 1 : 0;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null;
    }
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
	}
	
	public static boolean IsMaster(IBlockState state) {
		return state != null && state.getBlock() instanceof AetherInfuser && state.getValue(MASTER);
	}
	
	public static void SetBlock(World world, BlockPos pos, boolean master) {
		world.setBlockState(pos, instance().getDefaultState().withProperty(MASTER, master));
	}
	
	public static class AetherInfuserTileEntity extends AetherTickingTileEntity {

		private static final String NBT_CHARGE = "charge";
		public static final int MAX_CHARGE = 5000;
		public static final int CHARGE_PER_TICK = 100;
		
		private static final int MAX_SPARKS = 20;
		
		// Synced+saved
		private int charge;
		
		// Transient
		private boolean active; // use getter+setter to sync to client
		
		// Client-only + transient
		private int effectTime; // forever-growing at rate dependent on 'active'
		@SideOnly(Side.CLIENT)
		private List<EffectSpark> sparks = new ArrayList<>();
		
		public AetherInfuserTileEntity() {
			super(0, MAX_CHARGE);
			this.setAutoSync(5);
			this.compWrapper.configureInOut(true, false);
		}
		
//		private void dirtyAndUpdate() {
//			if (worldObj != null) {
//				worldObj.notifyBlockUpdate(pos, this.worldObj.getBlockState(pos), this.worldObj.getBlockState(pos), 3);
//				markDirty();
//			}
//		}
		
		@Override
		public void update() {
			super.update();
			
			if (worldObj.isRemote) {
				effectTime++;
				
				if (this.isActive()) {
					effectTime++; // double speed
				}
				
				this.updateSparks();
				
				if (this.getCharge() > 0) {
					// extra particles
					final float CHANCE = (float) getCharge() / ((float) MAX_CHARGE * 1f);
					final float RADIUS = 3;
					if (NostrumMagica.rand.nextFloat() < CHANCE) {
						final double x = (pos.getX() + .5 + (NostrumMagica.rand.nextFloat() * RADIUS)) - (RADIUS / 2f);
						final double y = (pos.getY() + 1.5 + (NostrumMagica.rand.nextFloat() * RADIUS)) - 1;
						final double z = (pos.getZ() + .5 + (NostrumMagica.rand.nextFloat() * RADIUS)) - (RADIUS / 2f);
						worldObj.spawnParticle(EnumParticleTypes.SUSPENDED_DEPTH,
								x, y, z,
								0, 0, 0, 0);
						
						int num = (active ? 10 : NostrumMagica.rand.nextFloat() < .05f ? 1 : 0);
						
						for (int i = 0; i < num; i++)
						Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleGlowOrb(
								worldObj,
								x, y, z,
								.3f,
								1f,
								.4f,
								.1f,
								100
								));
					}
				}
				
				return;
			}
			
			// TODO look for things to charge or infuse
			// Possibly spawn particles
			
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			if (nbt == null)
				nbt = new NBTTagCompound();
			
			nbt.setInteger(NBT_CHARGE, charge);
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			if (nbt == null)
				return;
			
			this.charge = nbt.getInteger(NBT_CHARGE);
		}
		
		@Override
		public void setWorldObj(World world) {
			super.setWorldObj(world);
			
			if (!world.isRemote) {
				this.compWrapper.setAutoFill(true);
			}
		}
		
		public int getCharge() {
			// Convenience wrapper around all the optional aether bits
			return this.getHandler().getAether(null); // We require aether to work anyways so being unsafe
		}
		
		public float getChargePerc() {
			return ((float) getCharge()) / (float) MAX_CHARGE;
		}
		
		@Override
		public boolean receiveClientEvent(int id, int type) {
			if (id == 0) {
				if (this.worldObj != null && this.worldObj.isRemote) {
					setActive(type == 1);
				}
				return true;
			}
			
			return super.receiveClientEvent(id, type);
		}
		
		protected void onActiveChange() {
			
		}
		
		private void setActive(boolean active) {
			if (this.active != active && worldObj != null) {
				
				if (!worldObj.isRemote) {
					worldObj.addBlockEvent(getPos(), getBlockType(), 0, active ? 1 : 0);
				}
				
				this.active = active;
				onActiveChange();
			}
		}
		
		public boolean isActive() {
			return active;
		}
		
		@SideOnly(Side.CLIENT)
		public int getEffectTicks() {
			return effectTime;
		}
		
		@SideOnly(Side.CLIENT)
		public void spawnSpark() {
			synchronized(sparks) {
				sparks.add(new EffectSpark(
						effectTime,
						20 * (10 + NostrumMagica.rand.nextInt(5)),
						20 * (30 + NostrumMagica.rand.nextInt(20)),
						NostrumMagica.rand.nextBoolean(),
						0f, // always start at bottom
						NostrumMagica.rand.nextFloat(),
						.5f // brightness but will be adjusted right after
					));
			}
		}
		
		@SideOnly(Side.CLIENT)
		public void removeSpark() {
			synchronized(sparks) {
				if (sparks.isEmpty()) {
					return;
				}
				
				sparks.remove(NostrumMagica.rand.nextInt(sparks.size()));
			}
		}
		
		@SideOnly(Side.CLIENT)
		public List<EffectSpark> getSparks(@Nullable List<EffectSpark> storage) {
			if (storage == null) {
				storage = new ArrayList<>();
			} else {
				storage.clear();
			}
			
			synchronized(sparks) {
				storage.addAll(sparks);
			}
			
			return storage;
		}
		
		@SideOnly(Side.CLIENT)
		public void updateSparks() {
			// Spawn or despawn sparks, and adjust brightness if necessary
			float chargePerc = getChargePerc();
			int sparkCount = Math.round(chargePerc * MAX_SPARKS);
			
			// Make spawning/despawning slow and a little random
			if (sparkCount != sparks.size() && NostrumMagica.rand.nextFloat() < .05f) {
				if (sparkCount > sparks.size()) {
					spawnSpark();
				} else {
					removeSpark();
				}
			}
			
			for (EffectSpark spark : sparks) {
				spark.brightness = chargePerc;
			}
		}
		
		@SideOnly(Side.CLIENT)
		public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox() {
			return TileEntity.INFINITE_EXTENT_AABB;
		}
		
		
		@SideOnly(Side.CLIENT)
		public static class EffectSpark {
			
			public static final int BLINK_PERIOD = (20 * 4);
			public static final float BLINK_FACTOR = 1f / (float) BLINK_PERIOD;
			
			public float brightness; // [0-1]
			public final float pitchStart; // [0-1]
			public final float yawStart; // [0-1]
			public final float pitchFactor; // [-1-1]
			public final float yawFactor; // [-1-1]
			
			public final int spawnTime;
			
			public EffectSpark(int spawnTime, 
					float pitchFactor, float yawFactor, float startingPitch, float startingYaw, float brightness) {
				this.spawnTime = spawnTime;
				this.pitchStart = startingPitch;
				this.yawStart = startingYaw;
				this.pitchFactor = pitchFactor;
				this.yawFactor = yawFactor;
			}
			
			public EffectSpark(int spawnTime, float pitchPeriod, float yawPeriod, boolean forwardDir,
					float startingPitch, float startingYaw, float brightness) {
				// period is ticks for a rotation, ofc
				this(spawnTime,
					(1f / pitchPeriod) * (forwardDir ? 1 : -1),
					(1f / yawPeriod) * (forwardDir ? 1 : -1),
					startingPitch, startingYaw, brightness);
			}
			
			private static final float Clamp(float in) {
				return in % 1f;
			}
			
			public float getPitch(int ticks, float partialTicks) {
				return Clamp(pitchStart + (float) (
						((double) (ticks - spawnTime) + (double) partialTicks) * pitchFactor)
					);
			}
			
			public float getYaw(int ticks, float partialTicks) {
				return Clamp(yawStart + (float) (((double) (ticks - spawnTime) + (double) partialTicks) * yawFactor)
					);
			}
			
			public float getBrightness(int ticks, float partialTicks) {
				brightness = 1f;
				// use input brightness (0-1) at 60% to allow for glowing
				// glow based on BLINK_PERIOD
				final float t = Clamp((float) (((double) (ticks - spawnTime) + partialTicks) * BLINK_FACTOR));
				final double tRad = t * Math.PI * 2;
				final float tAdj = (float) (Math.sin(tRad) + 1f) / 2f;
				return brightness * (.2f + .8f * tAdj);
			}
		}
	}
}