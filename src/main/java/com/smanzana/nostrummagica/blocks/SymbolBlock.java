package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SymbolBlock extends Block implements ITileEntityProvider {
	
	public static final String ID = "symbol_block";
	
	private static SymbolBlock instance = null;
	public static SymbolBlock instance() {
		if (instance == null)
			instance = new SymbolBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(SymbolTileEntity.class, "nostrum_symbol_te");
	}
	
	public SymbolBlock() {
		super(Material.BARRIER, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(500.0f);
		this.setResistance(900.0f);
		this.setBlockUnbreakable();
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		
		this.isBlockContainer = true;
		this.setLightLevel(0.8f);
		this.setLightOpacity(16);
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
        return false;
    }
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null;
    }

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new SymbolTileEntity();
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
	
	public static class SymbolTileEntity extends TileEntity implements ITickable {
		
		private int alive;
		private EMagicElement element;
		private EAlteration alteration;
		private SpellTrigger trigger;
		private SpellShape shape;
		private SpellIcon icon;
		
		public SymbolTileEntity() {
			alive = 0;
			setElement(EMagicElement.PHYSICAL);
		}
		
		@Override
		public double getMaxRenderDistanceSquared() {
			return 4096.0;
		}
		
		public void setElement(EMagicElement element) {
			this.element = element;
			this.alteration = null;
			this.trigger = null;
			this.shape = null;
			icon = SpellIcon.get(element);
		}
		
		public void setAlteration(EAlteration alteration) {
			this.element = null;
			this.alteration = alteration;
			this.trigger = null;
			this.shape = null;
			icon = SpellIcon.get(alteration);
		}
		
		public void setTrigger(SpellTrigger trigger) {
			this.element = null;
			this.alteration = null;
			this.trigger = trigger;
			this.shape = null;
			icon = SpellIcon.get(trigger);
		}
		
		public void setShape(SpellShape shape) {
			this.element = null;
			this.alteration = null;
			this.trigger = null;
			this.shape = shape;
			icon = SpellIcon.get(shape);
		}
		
		@Override
		public void update() {
			alive++;
		}
		
		public ResourceLocation getSymbolModel() {
			return icon.getModelLocation();
		}
		
		public float getRotation() {
			return ((float) alive) / (20.0f * 30.0f);
		}
		
		private static final String NBT_TYPE = "type";
		private static final String NBT_KEY = "key";
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			if (element != null) {
				nbt.setString(NBT_TYPE, "element");
				nbt.setString(NBT_KEY, element.getName());
			} else if (alteration != null) {
				nbt.setString(NBT_TYPE, "alteration");
				nbt.setString(NBT_KEY, alteration.getName());
			} else if (shape != null) {
				nbt.setString(NBT_TYPE, "shape");
				nbt.setString(NBT_KEY, shape.getShapeKey());
			} else if (trigger != null) {
				nbt.setString(NBT_TYPE, "trigger");
				nbt.setString(NBT_KEY, trigger.getTriggerKey());
			}
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			if (nbt == null || !nbt.hasKey(NBT_TYPE, NBT.TAG_STRING)
					|| !nbt.hasKey(NBT_KEY, NBT.TAG_STRING))
				return;
			
			String type = nbt.getString(NBT_TYPE).toLowerCase();
			String key = nbt.getString(NBT_KEY);
			
			switch (type) {
			case "element":
				try {
					EMagicElement elem = EMagicElement.valueOf(key.toUpperCase());
					setElement(elem);
				} catch (Exception e) {
					setElement(EMagicElement.PHYSICAL);
				}
				break;
			case "alteration":
				try {
					EAlteration altr = EAlteration.valueOf(key.toUpperCase());
					setAlteration(altr);
				} catch (Exception e) {
					setAlteration(EAlteration.INFLICT);
				}
				break;
			case "shape":
				SpellShape shape = SpellShape.get(key);
				if (shape == null)
					setElement(EMagicElement.PHYSICAL);
				else
					setShape(shape);
				break;
			case "trigger":
				SpellTrigger trigger = SpellTrigger.get(key);
				if (trigger == null)
					setElement(EMagicElement.PHYSICAL);
				else
					setTrigger(trigger);
				break;
			default:
				setElement(EMagicElement.PHYSICAL);
				break;
			}
			
		}
		
	}
}
