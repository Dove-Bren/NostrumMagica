package com.smanzana.nostrummagica.items;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpellRequestMessage;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class SpellTome extends Item {

	private static final String NBT_SPELLS = "nostrum_spells";
	private static final String NBT_INDEX = "spell_index";
	private static SpellTome instance = null;
	
	public static SpellTome instance() {
		if (instance == null)
			instance = new SpellTome();
		
		return instance;
	}
	
	public static final String id = "spellTome";
	public static final String textureName = "tome1";
	
	private SpellTome() {
		super();
		this.setUnlocalizedName(id);
		// this.setCreativeTab(NostrumMagica.creativeTab);
		// Is icon. Handled special in NostrumMagica
		this.setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        // TODO display GUI with stats?
		
		// TESTING DELETE ME -------------
		
		Spell spell = new Spell("Weaken");
		spell.addPart(new SpellPart(SelfTrigger.instance(), new SpellPartParam(0, false)));
		spell.addPart(new SpellPart(SingleShape.instance(), 
				EMagicElement.PHYSICAL, 1, null, new SpellPartParam(0, false)));
		spell.cast(playerIn);
		
		// END TESTING -------------------
		
		return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
    }
	
	/**
	 * Retrieves a list of spells stored in the spell tome.
	 * The active 'currently selected' spell is always first in the list.
	 * @param itemStack
	 * @return
	 */
	public static List<Spell> getSpells(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return null;
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		List<Spell> list = new LinkedList<>();
		if (nbt == null)
			return list;
		
		NBTTagList tags = nbt.getTagList(NBT_SPELLS, NBT.TAG_INT);
		
		if (tags == null || tags.tagCount() == 0)
			return list;
		
		int index = nbt.getInteger(NBT_INDEX);
		if (tags.tagCount() < index)
			index = 0;
		
		int id = tags.getIntAt(index);
		Spell spell = NostrumMagica.spellRegistry.lookup(id);
		
		if (spell != null)
			list.add(spell);
		
		for (int i = 0; i < tags.tagCount(); i++) {
			if (i == index)
				continue;
			
			id = tags.getIntAt(i);
			spell = NostrumMagica.spellRegistry.lookup(id);
			if (spell != null)
				list.add(spell);
		}
		
		return list;
	}
	
	/**
	 * Call on client side when a tome is picked up to scan the tome
	 * and make sure we have all the spells we need.
	 * If called from server, will crash.
	 * @param tome
	 */
	public static void onPickup(ItemStack tome) {
		if (tome == null || !(tome.getItem() instanceof SpellTome))
			return;
		
		NBTTagCompound nbt = tome.getTagCompound();
		
		if (nbt == null)
			return;
		
		NBTTagList tags = nbt.getTagList(NBT_SPELLS, NBT.TAG_INT);
		
		if (tags == null || tags.tagCount() == 0)
			return;
		
		int id;
		int requests[] = new int[tags.tagCount()];
		int requestcount = 0;
		for (int i = 0; i < tags.tagCount(); i++) {
			id = tags.getIntAt(i);
			if (NostrumMagica.spellRegistry.lookup(id) == null) {
				// Create a temporary spell
				// Request spell from server
				requests[requestcount++] = id;
				getTemp(id);
			}
		}
		
		if (requestcount > 0) {
			NetworkHandler.getSyncChannel().sendToServer(
	    			new SpellRequestMessage(requests));
		}
	}
	
	private static Spell getTemp(int id) {
		return Spell.CreateInternal("Loading...", id);
	}
}
