package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.SingleSpawnerTileEntity;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRed;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.golem.EntityGolemFire;
import com.smanzana.nostrummagica.entity.golem.EntityGolemIce;
import com.smanzana.nostrummagica.entity.golem.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.golem.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.golem.EntityGolemWind;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NostrumSingleSpawner extends Block implements ITileEntityProvider {
	
	public static enum Type implements IStringSerializable {
		// Do not change order. Ordinals are used
		GOLEM_EARTH,
		GOLEM_ENDER,
		GOLEM_FIRE,
		GOLEM_ICE,
		GOLEM_LIGHTNING,
		GOLEM_PHYSICAL,
		GOLEM_WIND,
		DRAGON_RED,
		PLANT_BOSS,
		;

		@Override
		public String getName() {
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}
	
	public static final int SPAWN_DIST_SQ = 900; // 30^2 
	protected static final PropertyEnum<Type> MOB = PropertyEnum.create("mob", Type.class);

	public static final String ID = "nostrum_spawner";
	
	private static NostrumSingleSpawner instance = null;
	public static NostrumSingleSpawner instance() {
		if (instance == null)
			instance = new NostrumSingleSpawner();
		
		return instance;
	}
	
	public NostrumSingleSpawner() {
		super(Material.ROCK, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(6.0f);
		this.setResistance(100.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setHarvestLevel("pickaxe", 4);
		this.setBlockUnbreakable();
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(MOB, Type.GOLEM_PHYSICAL));
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
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, PlayerEntity player) {
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
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		super.updateTick(worldIn, pos, state, rand);
		
		for (PlayerEntity player : worldIn.playerEntities) {
			if (!player.isSpectator() && !player.isCreative() && player.getDistanceSq(pos) < SPAWN_DIST_SQ) {
				this.spawn(worldIn, pos, state, rand);
				
				worldIn.setBlockToAir(pos);
				return;
			}
		}
	}
	
	public MobEntity spawn(World world, BlockPos pos, IBlockState state, Random rand) {
		Type type = state.getValue(MOB);
		MobEntity entity = getEntity(type, world, pos);
		
		entity.enablePersistence();
		entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + .5);
		
		world.spawnEntity(entity);
		return entity;
	}
	
	protected static MobEntity getEntity(Type type, World world, BlockPos pos) {
		if (type == null)
			return null;
		
		MobEntity entity = null;
		
		switch (type) {
		case GOLEM_EARTH:
			entity = new EntityGolemEarth(world);
			break;
		case GOLEM_ENDER:
			entity = new EntityGolemEnder(world);
			break;
		case GOLEM_FIRE:
			entity = new EntityGolemFire(world);
			break;
		case GOLEM_ICE:
			entity = new EntityGolemIce(world);
			break;
		case GOLEM_LIGHTNING:
			entity = new EntityGolemLightning(world);
			break;
		case GOLEM_PHYSICAL:
			entity = new EntityGolemPhysical(world);
			break;
		case GOLEM_WIND:
			entity = new EntityGolemWind(world);
			break;
		case DRAGON_RED:
			entity = new EntityDragonRed(world);
			break;
		case PLANT_BOSS:
			entity = new EntityPlantBoss(world);
			break;
		}
		
		return entity;
	}


	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new SingleSpawnerTileEntity();
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, PlayerEntity playerIn, EnumHand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		}
		
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof SingleSpawnerTileEntity)) {
			return true;
		}
		
		if (playerIn.isCreative()) {
			ItemStack heldItem = playerIn.getHeldItem(hand);
			if (heldItem.isEmpty()) {
				playerIn.sendMessage(new StringTextComponent("Currently set to " + state.getValue(MOB).getName()));
			} else if (heldItem.getItem() instanceof EssenceItem) {
				Type type = null;
				switch (EssenceItem.findType(heldItem)) {
				case EARTH:
					type = Type.GOLEM_EARTH;
					break;
				case ENDER:
					type = Type.GOLEM_ENDER;
					break;
				case FIRE:
					type = Type.GOLEM_FIRE;
					break;
				case ICE:
					type = Type.GOLEM_ICE;
					break;
				case LIGHTNING:
					type = Type.GOLEM_LIGHTNING;
					break;
				case PHYSICAL:
					type = Type.GOLEM_PHYSICAL;
					break;
				case WIND:
					type = Type.GOLEM_WIND;
					break;
				}
				
				worldIn.setBlockState(pos, state.withProperty(MOB, type));
			} else if (heldItem.getItem() instanceof NostrumSkillItem) {
				if (NostrumSkillItem.getTypeFromMeta(heldItem.getMetadata()) == SkillItemType.WING) {
					worldIn.setBlockState(pos, state.withProperty(MOB, Type.DRAGON_RED));
				}
			} else if (heldItem.getItem() == Items.REEDS) {
				worldIn.setBlockState(pos, state.withProperty(MOB, Type.PLANT_BOSS));
			}
			return true;
		}
		
		return false;
	}
}
