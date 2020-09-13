package com.smanzana.nostrummagica.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class PutterBlock extends BlockContainer {
	
	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	
	public static final String ID = "putter";
	
	private static PutterBlock instance = null;
	public static PutterBlock instance() {
		if (instance == null)
			instance = new PutterBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(PutterBlockTileEntity.class, "putter_entity");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WGW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'G', new ItemStack(Blocks.GLASS_PANE, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1));
	}
	
	public PutterBlock() {
		super(Material.ROCK, MapColor.STONE);
		this.setUnlocalizedName(ID);
		this.setHardness(3.5f);
		this.setResistance(3.5f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setHarvestLevel("pickaxe", 1);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		return this.getDefaultState().withProperty(FACING, BlockPistonBase.getFacingFromEntity(pos, placer));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.VALUES[meta % EnumFacing.VALUES.length];
		return getDefaultState().withProperty(FACING, facing);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).ordinal();
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.putterBlockID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	public static class PutterBlockTileEntity extends TileEntity implements ITickable {

		private static final String NBT_INVENTORY = "inventory";
		
		private final InventoryBasic inventory;
		
		private EntityItem itemEntCache = null;
		private int ticksExisted;
		
		public PutterBlockTileEntity() {
			final PutterBlockTileEntity putter = this;
			this.inventory = new InventoryBasic("Putter Block", false, 9) {
				@Override
				public void markDirty() {
					putter.markDirty();
				}
			};
		}
		
		public IInventory getInventory() {
			return inventory;
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			nbt.setTag(NBT_INVENTORY, Inventories.serializeInventory(inventory));
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			if (nbt == null)
				return;
			
			Inventories.deserializeInventory(inventory, nbt.getTag(NBT_INVENTORY));
		}

		@Override
		public void update() {
			ticksExisted++;
			if (worldObj == null || worldObj.isRemote) {
				return;
			}
			
			// If being powered, disable everything
			if (ticksExisted % 10 != 0 || worldObj.isBlockPowered(pos)) {
				return;
			}
			
			// Validate entityItem
			if (itemEntCache != null) {
				if (itemEntCache.isDead
						|| (int) itemEntCache.posX != pos.getX()
						|| (int) itemEntCache.posY != pos.getY()
						|| (int) itemEntCache.posZ != pos.getZ()) {
					itemEntCache = null;
				}
			}
			
			// Search for item if cache is busted
			if (itemEntCache == null) {
				EnumFacing direction = worldObj.getBlockState(this.pos).getValue(FACING);
				int dx = 0;
				int dy = 0;
				int dz = 0;
				switch (direction) {
				case DOWN:
					dy = -1;
					break;
				case EAST:
				default:
					dx = 1;
					break;
				case NORTH:
					dz = -1;
					break;
				case SOUTH:
					dz = 1;
					break;
				case UP:
					dy = 1;
					break;
				case WEST:
					dx = -1;
					break;
				}
				List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class, Block.FULL_BLOCK_AABB.offset(pos).offset(dx, dy, dz));
				if (items != null && !items.isEmpty()) {
					itemEntCache = items.get(0);
				}
			}
			
			// If we have an item, make sure it survives
			if (itemEntCache != null) {
				if (itemEntCache.ticksExisted > (float) itemEntCache.lifespan * .05f) {
					itemEntCache = refreshEntityItem(itemEntCache);
				}
			} else {
				// Spawn an item if we have items in our inventory
				final int size = inventory.getSizeInventory();
				final int pos = NostrumMagica.rand.nextInt(size);
				ItemStack toSpawn = null;
				for (int i = 0; i < size; i++) {
					// Get random offset, and then walk until we find an item
					final int index = (pos + i) % size; 
					ItemStack stack = inventory.getStackInSlot(index);
					if (stack == null) {
						continue;
					}
					
					toSpawn = inventory.decrStackSize(index, 1);
					break;
				}
				
				if (toSpawn != null) {
					// Play effects, and create item
					double dx = 0;
					double dy = 0;
					double dz = 0;
					EnumFacing direction = worldObj.getBlockState(this.pos).getValue(FACING);
					switch (direction) {
					case DOWN:
						dy = -.75;
						break;
					case EAST:
					default:
						dx = .75;
						break;
					case NORTH:
						dz = -.75;
						break;
					case SOUTH:
						dz = .75;
						break;
					case UP:
						dy = .75;
						break;
					case WEST:
						dx = -.75;
						break;
					}
					itemEntCache = new EntityItem(worldObj, this.pos.getX() + .5 + dx, this.pos.getY() + .5 + dy, this.pos.getZ() + .5 + dz, toSpawn);
					itemEntCache.motionX = itemEntCache.motionY = itemEntCache.motionZ = 0;
					worldObj.spawnEntityInWorld(itemEntCache);
				}
			}
		}
		
		private EntityItem refreshEntityItem(EntityItem oldItem) {
			EntityItem newItem = new EntityItem(oldItem.worldObj, oldItem.posX, oldItem.posY, oldItem.posZ, oldItem.getEntityItem().copy());
			newItem.motionX = oldItem.motionX;
			newItem.motionY = oldItem.motionY;
			newItem.motionZ = oldItem.motionZ;
			newItem.lifespan = oldItem.lifespan;
			oldItem.worldObj.spawnEntityInWorld(newItem);
			oldItem.setDead();
			return newItem;
		}
		
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new PutterBlockTileEntity();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof PutterBlockTileEntity))
			return;
		
		PutterBlockTileEntity putter = (PutterBlockTileEntity) ent;
		IInventory inv = putter.getInventory();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if (item != null) {
				double x, y, z;
				x = pos.getX() + .5;
				y = pos.getY() + .5;
				z = pos.getZ() + .5;
				world.spawnEntityInWorld(new EntityItem(world, x, y, z, item.copy()));
			}
		}
	}
}
