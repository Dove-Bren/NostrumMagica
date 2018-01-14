package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NostrumSingleSpawner extends BlockContainer implements ITileEntityProvider {
	
	public static enum Type implements IStringSerializable {
		// Do not change order. Ordinals are used
		ZOMBIE;

		@Override
		public String getName() {
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}
	
	private static final int SPAWN_DIST_SQ = 900; // 30^2 
	private static final PropertyEnum<Type> MOB = PropertyEnum.create("mob", Type.class);

	public static final String ID = "nostrum_spawner";
	
	private static NostrumSingleSpawner instance = null;
	public static NostrumSingleSpawner instance() {
		if (instance == null)
			instance = new NostrumSingleSpawner();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(SingleSpawnerTE.class, "nostrum_mob_spawner_te");
	}
	
	public NostrumSingleSpawner() {
		super(Material.ROCK, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(6.0f);
		this.setResistance(100.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setHarvestLevel("pickaxe", 4);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(MOB, Type.ZOMBIE));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, MOB);
	}
	
	public IBlockState getState(NostrumSingleSpawner.Type type) {
		return getDefaultState().withProperty(MOB, type);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta > Type.values().length || meta < 0)
			meta = 0;
		Type type = Type.values()[meta];
		
		return getDefaultState().withProperty(MOB, type);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(MOB).ordinal();
	}
	
	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}
	
	@Override
	public int quantityDroppedWithBonus(int fortune, Random random) {
		return 0;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}
	
	@Override
	public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
		return 0;
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		super.updateTick(worldIn, pos, state, rand);
		
		for (EntityPlayer player : worldIn.playerEntities) {
			System.out.print(".");
			if (player.getDistanceSq(pos) < SPAWN_DIST_SQ) {
				this.spawn(worldIn, pos, state, rand);
				
				worldIn.setBlockToAir(pos);
				return;
			}
		}
	}
	
	private void spawn(World world, BlockPos pos, IBlockState state, Random rand) {
		Type type = state.getValue(MOB);
		EntityLivingBase entity = getEntity(type, world, pos);
		
		entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + .5);
		world.spawnEntityInWorld(entity);
		
	}
	
	private static EntityLivingBase getEntity(Type type, World world, BlockPos pos) {
		if (type == null)
			return null;
		
		EntityLivingBase entity = null;
		
		switch (type) {
		case ZOMBIE:
			entity = new EntityZombie(world);
			entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, 
					new ItemStack(Items.DIAMOND_HELMET));
			break;
		default:
			break;
		}
		
		return entity;
	}


	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new SingleSpawnerTE();
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
	}
	
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
	}

	
	private static class SingleSpawnerTE extends TileEntity implements ITickable {
		
		private int life;
		
		@Override
		public void update() {
			if (!worldObj.isRemote && ++life % 32 == 0) {
				IBlockState state = this.worldObj.getBlockState(this.pos);
				if (state == null || !(state.getBlock() instanceof NostrumSingleSpawner)) {
					worldObj.removeTileEntity(pos);
					return;
				}
				
				NostrumSingleSpawner.instance().updateTick(worldObj, pos, state, RANDOM);
			}
		}
	}
}
