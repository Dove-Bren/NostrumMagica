package com.smanzana.nostrummagica.tiles;

import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.shapes.SpellShape;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.Constants.NBT;

public class SymbolTileEntity extends TileEntity {
	
	private EMagicElement element;
	private EAlteration alteration;
	private SpellShape shape;
	private float scale;
	
	protected SymbolTileEntity(TileEntityType<? extends SymbolTileEntity> type) {
		super(type);
		setElement(EMagicElement.PHYSICAL);
		this.scale = 1.0f;
	}
	
	public SymbolTileEntity() {
		this(NostrumTileEntities.SymbolTileEntityType);
	}
	
	public SymbolTileEntity(float scale) {
		this();
		this.scale = scale;
	}
	
	public float getScale() {
		return scale;
	}
	
	protected void setScale(float scale) {
		this.scale = scale;
	}
	
	@Override
	public double getMaxRenderDistanceSquared() {
		return 8192.0;
	}
	
	public void setComponent(SpellComponentWrapper wrapper) {
		if (wrapper.isElement())
			setElement(wrapper.getElement());
		else if (wrapper.isAlteration())
			setAlteration(wrapper.getAlteration());
		else if (wrapper.isShape())
			setShape(wrapper.getShape());
		
		dirty();
	}
	
	public SpellComponentWrapper getComponent() {
		if (element != null)
			return new SpellComponentWrapper(element);
		if (alteration != null)
			return new SpellComponentWrapper(alteration);
		if (shape != null)
			return new SpellComponentWrapper(shape);
		
		return null;
	}
	
	private void setElement(EMagicElement element) {
		this.element = element;
		this.alteration = null;
		this.shape = null;
	}
	
	private void setAlteration(EAlteration alteration) {
		this.element = null;
		this.alteration = alteration;
		this.shape = null;
	}
	
	private void setShape(SpellShape shape) {
		this.element = null;
		this.alteration = null;
		this.shape = shape;
	}
	
	private static final String NBT_TYPE = "type";
	private static final String NBT_KEY = "key";
	private static final String NBT_SCALE = "scale";
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		if (element != null) {
			nbt.putString(NBT_TYPE, "element");
			nbt.putString(NBT_KEY, element.name());
		} else if (alteration != null) {
			nbt.putString(NBT_TYPE, "alteration");
			nbt.putString(NBT_KEY, alteration.name());
		} else if (shape != null) {
			nbt.putString(NBT_TYPE, "shape");
			nbt.putString(NBT_KEY, shape.getShapeKey());
		}
		
		nbt.putFloat(NBT_SCALE, scale);
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		if (nbt == null || !nbt.contains(NBT_TYPE, NBT.TAG_STRING)
				|| !nbt.contains(NBT_KEY, NBT.TAG_STRING))
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
		default:
			setElement(EMagicElement.PHYSICAL);
			break;
		}
		
		this.scale = nbt.getFloat(NBT_SCALE);
		if (Math.abs(scale) < 0.01)
			this.scale = 1.0f;
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());
	}
	
	protected void dirty() {
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}
	
}