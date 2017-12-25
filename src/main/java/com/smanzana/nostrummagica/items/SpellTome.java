package com.smanzana.nostrummagica.items;

import java.util.LinkedList;
import java.util.List;

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
	
	public static List<Spell> getSpells(ItemStack itemStack, boolean ignoreEquipped) {
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		List<Spell> list = new LinkedList<>();
		if (nbt == null)
			return list;
		
		NBTTagList tags = nbt.getTagList(NBT_SPELLS, NBT.TAG_COMPOUND);
		
		if (tags.tagCount() == 0)
			return list;
		
		int index = nbt.getInteger(NBT_INDEX);
		if (tags.tagCount() < index)
			index = 0;
		
		String name = tags.getStringTagAt(index);
		
		
		for (int i = 0; i < tags.tagCount(); i++) {
			
		}
		
		
		
	}
}
