package com.smanzana.nostrummagica.blocks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.SpellScroll;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class WispBlock extends BlockContainer {
	
	public static final String ID = "wisp_block";
	
	private static WispBlock instance = null;
	public static WispBlock instance() {
		if (instance == null)
			instance = new WispBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(WispBlockTileEntity.class, ID + "_entity");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WGW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'G', new ItemStack(Blocks.GLASS_PANE, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1));
	}
	
	public WispBlock() {
		super(Material.GLASS, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.GLASS);
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.wispblockID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new WispBlockTileEntity();
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
		if (ent == null || !(ent instanceof WispBlockTileEntity))
			return;
		
		WispBlockTileEntity table = (WispBlockTileEntity) ent;
		ItemStack item = table.getScroll();
		if (item != null) {
			double x, y, z;
			x = pos.getX() + .5;
			y = pos.getY() + .5;
			z = pos.getZ() + .5;
			world.spawnEntityInWorld(new EntityItem(world, x, y, z, item.copy()));
		}
		
		item = table.getReagent();
		if (item != null) {
			double x, y, z;
			x = pos.getX() + .5;
			y = pos.getY() + .5;
			z = pos.getZ() + .5;
			world.spawnEntityInWorld(new EntityItem(world, x, y, z, item.copy()));
		}
		
		table.deactivate();
	}
	
	public ItemStack getScroll(World world, BlockPos pos) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof WispBlockTileEntity))
			return null;
		
		WispBlockTileEntity table = (WispBlockTileEntity) ent;
		return table.getScroll();
	}
	
	public int getWispCount(World world, BlockPos pos) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof WispBlockTileEntity))
			return 0;
		
		WispBlockTileEntity table = (WispBlockTileEntity) ent;
		return table.getWispCount();
	}
	
	public int getMaxWisps(World world, BlockPos pos) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof WispBlockTileEntity))
			return 0;
		
		WispBlockTileEntity table = (WispBlockTileEntity) ent;
		return table.getMaxWisps();
	}
	
	public static class WispBlockTileEntity extends TileEntity implements ITickable {

		// Synced+saved
		private ItemStack scroll;
		private ItemStack reagent;
		private float reagentPartial;
		private boolean activated;
		
		// Transient
		private List<EntityWisp> wisps; // on server
		private int numWisps;
		
		// TODO add progression. Maybe insert essences or mani crystals?
		// TODO add some cool like mani crystal generation. That'd be neat :)
		// TODO part of progression: be able to spawn more wisps!
		private static final int MAX_WISPS = 3;
		private static final float REAGENT_PER_SECOND = (1f / 120f);  // 1 per 2 minutes
		
		private int ticksExisted;
		
		public WispBlockTileEntity() {
			scroll = null;
			reagent = null;
			reagentPartial = 0f;
			wisps = new LinkedList<>();
			ticksExisted = 0;
			activated = false;
		}
		
		public ItemStack getScroll() {
			return scroll;
		}
		
		public boolean setScroll(ItemStack item) {
			if (item != null && this.scroll != null)
				return false;
			
			if (item != null) {
				// Valid scroll?
				if (!(item.getItem() instanceof SpellScroll) || SpellScroll.getSpell(item) == null) {
					return false;
				}
			}
			
			this.scroll = item;
			this.dirty();
			return true;
		}
		
		public ItemStack getReagent() {
			return reagent;
		}
		
		public boolean setReagent(ItemStack item) {
			if (item != null && this.reagent != null)
				return false;
			
			if (item != null) {
				// Valid reagent?
				if (!(item.getItem() instanceof ReagentItem)) {
					return false;
				}
			}
			
			this.reagent = item;
			this.dirty();
			return true;
		}
		
		public float getPartialReagent() {
			return reagentPartial;
		}
		
		public int getWispCount() {
			return this.worldObj.isRemote ? this.numWisps : this.wisps.size();
		}
		
		public int getMaxWisps() {
			return MAX_WISPS;
		}
		
		private void dirty() {
			worldObj.markBlockRangeForRenderUpdate(pos, pos);
			worldObj.notifyBlockUpdate(pos, this.worldObj.getBlockState(pos), this.worldObj.getBlockState(pos), 3);
			worldObj.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
			markDirty();
		}
		
		// Cleans up any wisps as soon as we deactivate
		private void deactivate() {
			this.activated = false;
			
			for (EntityWisp wisp : this.wisps) {
				wisp.setDead();
			}
			wisps.clear();
			
			dirty();
		}
		
		private void activate() {
			this.activated = true;
			dirty();
		}
		
		private void spawnWisp() {
			
			BlockPos spawnPos = null;
			
			// Try to find a safe place to spawn the wisp
			int attempts = 20;
			do {
				spawnPos = this.pos.add(
						NostrumMagica.rand.nextInt(10) - 5,
						NostrumMagica.rand.nextInt(5),
						NostrumMagica.rand.nextInt(10) - 5);
			} while (!worldObj.isAirBlock(spawnPos) && attempts-- >= 0);
			
			if (worldObj.isAirBlock(spawnPos)) {
				EntityWisp wisp = new EntityWisp(this.worldObj, this.pos);
				wisp.setPosition(spawnPos.getX() + .5, spawnPos.getY(), spawnPos.getZ() + .5);
				this.wisps.add(wisp);
				this.worldObj.spawnEntityInWorld(wisp);
				this.dirty();
			}
		}

		@Override
		public void update() {
			ticksExisted++;
			
			if (worldObj.isRemote) {
				return;
			}
			
			if (!activated) {
				if (this.getScroll() != null && (this.getReagent() != null || this.reagentPartial >= REAGENT_PER_SECOND)) {
					activate();
				} else {
					return;
				}
			}
			
			// If no scroll is present, deactivate
			if (this.getScroll() == null) {
				deactivate();
				return;
			}
			
			// Passively burn reagents. If there are none, kill all wisps and deactivate
			if (ticksExisted % 20 == 0 && !wisps.isEmpty()) {
				float debt = REAGENT_PER_SECOND;
				if (reagentPartial < debt) {
					// Not enough partial. Try to consume from reagent stack.
					// If not there, next bit of logic will turn reagentPartial negative and
					// know to deactivate
					if (getReagent() != null && getReagent().stackSize > 0) {
						reagentPartial += 1f;
						if (getReagent().stackSize > 1) {
							getReagent().splitStack(1);
						} else {
							setReagent(null);
						}
					}
				}
				
				// Regardless of if we have enough, subtract debt
				reagentPartial -= debt;
				
				// If negative, we didn't have enough to run for another tick! Deactivate!
				if (reagentPartial < 0) {
					deactivate();
				} else {
					// Update client
					this.dirty();
				}
			}
			
			if (!activated) {
				return;
			}
			
			Iterator<EntityWisp> it = wisps.iterator();
			while (it.hasNext()) {
				EntityWisp wisp = it.next();
				if (wisp.isDead) {
					it.remove();
					this.dirty();
				}
			}
			
			// If not at max wisps, maybe spawn one every once in a while
			if (ticksExisted % (20 * 3) == 0 && wisps.size() < getMaxWisps()) {
				if (NostrumMagica.rand.nextInt(10) == 0) {
					spawnWisp();
				}
			}
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			if (nbt == null)
				nbt = new NBTTagCompound();
			
			if (scroll != null)
				nbt.setTag("scroll", scroll.serializeNBT());
			
			if (reagent != null)
				nbt.setTag("reagent", reagent.serializeNBT());
			
			if (reagentPartial != 0f)
				nbt.setFloat("partial", reagentPartial);
			
			if (activated) {
				nbt.setBoolean("active", activated);
				nbt.setInteger("wisps", wisps.size());
			}
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			if (nbt == null)
				return;
			
			this.scroll = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("scroll"));
			this.reagent = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("reagent"));
			this.reagentPartial = nbt.getFloat("partial");
			this.activated = nbt.getBoolean("active");
			this.numWisps = nbt.getInteger("wisps");
		}
		
		@Override
		public NBTTagCompound getUpdateTag() {
			return this.writeToNBT(new NBTTagCompound());
		}
		
		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new SPacketUpdateTileEntity(this.pos, 3, nbt);
		}
		
		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			super.onDataPacket(net, pkt);
			handleUpdateTag(pkt.getNbtCompound());
		}
	}
}
