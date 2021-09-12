package com.smanzana.nostrummagica.blocks.tiles;

import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;

public class SymbolTileEntity extends TileEntity {
	
	private EMagicElement element;
	private EAlteration alteration;
	private SpellTrigger trigger;
	private SpellShape shape;
	private float scale;
	
	public SymbolTileEntity() {
		setElement(EMagicElement.PHYSICAL);
		this.scale = 1.0f;
	}
	
	public SymbolTileEntity(float scale) {
		this();
		this.scale = scale;
	}
	
	public float getScale() {
		return scale;
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
		else
			setTrigger(wrapper.getTrigger());
		
		dirty();
	}
	
	public SpellComponentWrapper getComponent() {
		if (element != null)
			return new SpellComponentWrapper(element);
		if (alteration != null)
			return new SpellComponentWrapper(alteration);
		if (trigger != null)
			return new SpellComponentWrapper(trigger);
		if (shape != null)
			return new SpellComponentWrapper(shape);
		
		return null;
	}
	
	private void setElement(EMagicElement element) {
		this.element = element;
		this.alteration = null;
		this.trigger = null;
		this.shape = null;
	}
	
	private void setAlteration(EAlteration alteration) {
		this.element = null;
		this.alteration = alteration;
		this.trigger = null;
		this.shape = null;
	}
	
	private void setTrigger(SpellTrigger trigger) {
		this.element = null;
		this.alteration = null;
		this.trigger = trigger;
		this.shape = null;
	}
	
	private void setShape(SpellShape shape) {
		this.element = null;
		this.alteration = null;
		this.trigger = null;
		this.shape = shape;
	}
	
	private static final String NBT_TYPE = "type";
	private static final String NBT_KEY = "key";
	private static final String NBT_SCALE = "scale";
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		if (element != null) {
			nbt.setString(NBT_TYPE, "element");
			nbt.setString(NBT_KEY, element.name());
		} else if (alteration != null) {
			nbt.setString(NBT_TYPE, "alteration");
			nbt.setString(NBT_KEY, alteration.name());
		} else if (shape != null) {
			nbt.setString(NBT_TYPE, "shape");
			nbt.setString(NBT_KEY, shape.getShapeKey());
		} else if (trigger != null) {
			nbt.setString(NBT_TYPE, "trigger");
			nbt.setString(NBT_KEY, trigger.getTriggerKey());
		}
		
		nbt.setFloat(NBT_SCALE, scale);
		
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
		
		this.scale = nbt.getFloat(NBT_SCALE);
		if (Math.abs(scale) < 0.01)
			this.scale = 5.0f;
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	private void dirty() {
		world.markBlockRangeForRenderUpdate(pos, pos);
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		world.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
		markDirty();
	}
	
}