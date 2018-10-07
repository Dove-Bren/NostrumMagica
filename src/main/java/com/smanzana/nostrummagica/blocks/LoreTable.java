package com.smanzana.nostrummagica.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class LoreTable extends BlockContainer {
	
	public static final String ID = "lore_table";
	
	private static LoreTable instance = null;
	public static LoreTable instance() {
		if (instance == null)
			instance = new LoreTable();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(LoreTableEntity.class,
				new ResourceLocation(NostrumMagica.MODID, "lore_table"));
	}
	
	public LoreTable() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(2.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
	}
	
//	@Override
//	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
//		return true;
//	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.loretableID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	public static class LoreTableEntity extends TileEntity implements ITickable {

		@Nonnull
		private ItemStack item;
		private float progress;
		private String lorekey;
		
		public LoreTableEntity() {
			progress = 0f;
			lorekey = null;
			item = ItemStack.EMPTY;
		}
		
		/**
		 * Returns the current (if any) lore available for taking
		 * and clears it
		 * @return
		 */
		private String takeLore() {
			String key = this.lorekey;
			this.lorekey = null;
			return key;
		}
		
		public boolean hasLore() {
			return (this.lorekey != null);
		}
		
		public void onTakeItem(EntityPlayer player) {
			if (hasLore()) {
				String lore = takeLore();
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null) {
					ILoreTagged tag = LoreRegistry.instance().lookup(lore);
					if (tag != null) {
						attr.giveFullLore(tag);
					}
				}
			}
		}
		
		public @Nonnull ItemStack getItem() {
			return item;
		}
		
		public boolean setItem(@Nonnull ItemStack item) {
			if (item != ItemStack.EMPTY && this.item != ItemStack.EMPTY)
				return false;
			
			if (item != ItemStack.EMPTY) {
				// Make sure it has lore
				if (!(item.getItem() instanceof ILoreTagged)) {
					return false;
				}
			}
			
			this.item = item;
			progress = 0f;
			this.dirty();
			return true;
		}
		
		public float getProgress() {
			return progress;
		}
		
		private void dirty() {
			world.markBlockRangeForRenderUpdate(pos, pos);
			world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
			world.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
			markDirty();
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			if (nbt == null)
				nbt = new NBTTagCompound();
			
			if (item != null)
				nbt.setTag("item", item.serializeNBT());
			
			if (lorekey != null)
				nbt.setString("lore", lorekey);
			
			nbt.setFloat("progress", progress);
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			if (nbt == null)
				return;
			
			this.progress = nbt.getFloat("progress");
			if (nbt.hasKey("item", NBT.TAG_COMPOUND))
				this.item = new ItemStack(nbt.getCompoundTag("item"));
			
			if (nbt.hasKey("lore", NBT.TAG_STRING))
				this.lorekey = nbt.getString("lore");
				
			
		}

		@Override
		public void update() {
			if (item != null && lorekey == null) {
				progress += 1f / (30f * 20f); // 30 seconds
				if (progress >= 1f) {
					if (item.getItem() instanceof ILoreTagged) {
						this.lorekey = ((ILoreTagged) item.getItem()).getLoreKey();
						NostrumMagicaSounds.DAMAGE_ICE.play(world,
								pos.getX(), pos.getY(), pos.getZ());
					}
				}
			}
		}
		
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new LoreTableEntity();
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
		if (ent == null || !(ent instanceof LoreTableEntity))
			return;
		
		LoreTableEntity table = (LoreTableEntity) ent;
		ItemStack item = table.getItem();
		if (item != null) {
			double x, y, z;
			x = pos.getX() + .5;
			y = pos.getY() + .5;
			z = pos.getZ() + .5;
			world.spawnEntity(new EntityItem(world, x, y, z, item.copy()));
		}
		
	}
}
