package com.smanzana.nostrummagica.items;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpellRequestMessage;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
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
		
		// print info about spell stored
		List<Spell> spells = getSpells(itemStackIn);
		if (spells == null || spells.isEmpty()) {
			NostrumMagica.logger.info("No stored spell!");
		} else {
			Spell spell = spells.get(0);
			NostrumMagica.logger.info("");
			NostrumMagica.logger.info("");
			NostrumMagica.logger.info("Spell Name: " + spell.getName());
			NostrumMagica.logger.info("Cost: " + spell.getManaCost());
			NostrumMagica.logger.info("Id: " + spell.getRegistryID());
			NostrumMagica.logger.info("");
			NostrumMagica.logger.info("");
		}
		
		// END TESTING -------------------
		
		return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
    }
	
	public static void addSpell(ItemStack itemStack, Spell spell) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return;
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		if (nbt == null)
			nbt = new NBTTagCompound();
		
		NBTTagList tags = nbt.getTagList(NBT_SPELLS, NBT.TAG_INT);
		
		if (tags == null)
			tags = new NBTTagList();
		
		tags.appendTag(new NBTTagInt(spell.getRegistryID()));
		nbt.setTag(NBT_SPELLS, tags);
		
		itemStack.setTagCompound(nbt);
	}
	
	private static int[] getSpellIDs(ItemStack itemStack) {
		System.out.println(" > getting ids");
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return null;
		System.out.println(" > is tome");
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		if (nbt == null)
			return null;
		System.out.println(" > has nbt");
		
		NBTTagList tags = nbt.getTagList(NBT_SPELLS, NBT.TAG_INT);
		
		if (tags == null || tags.tagCount() == 0)
			return null;
		System.out.println(" > has spell id list");
		
		int ids[] = new int[tags.tagCount()];
		
		for (int i = 0; i < tags.tagCount(); i++) {
			ids[i] = tags.getIntAt(i);
		}
		System.out.println(" > found " + tags.tagCount() + " ids");

		return ids;
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

		List<Spell> list = new LinkedList<>();
		int[] ids = getSpellIDs(itemStack);
		if (ids == null || ids.length == 0)
			return list;
		
		if (!NostrumMagica.proxy.isServer()) {
			sniffIDs(ids);
		}
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		int index = nbt.getInteger(NBT_INDEX);
		if (ids.length < index)
			index = 0;
		
		int id = ids[index];
		Spell spell = NostrumMagica.spellRegistry.lookup(id);
		
		if (spell != null)
			list.add(spell);
		
		for (int i = 0; i < ids.length; i++) {
			if (i == index)
				continue;
			
			id = ids[i];
			spell = NostrumMagica.spellRegistry.lookup(id);
			if (spell != null)
				list.add(spell);
		}
		
		return list;
	}
	
	/**
	 * Scan nested IDs for spells we don't know about and request them
	 * @param tome
	 */
	private static void sniffIDs(int ids[]) {
		System.out.println("scan");
		
		int id;
		int requests[] = new int[ids.length];
		int requestcount = 0;
		for (int i = 0; i < ids.length; i++) {
			id = ids[i];
			if (NostrumMagica.spellRegistry.lookup(id) == null) {
				System.out.println("don't know this one: " + id);
				// Create a temporary spell
				// Request spell from server
				requests[requestcount++] = id;
				getTemp(id);
			}
		}
		
		if (requestcount > 0) {
			NostrumMagica.logger.info("Requesting " + requestcount
				 + " spells from the server...");
			NetworkHandler.getSyncChannel().sendToServer(
	    			new SpellRequestMessage(requests));
		}
	}
	
	private static Spell getTemp(int id) {
		return Spell.CreateInternal("Loading...", id);
	}
}
