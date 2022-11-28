package com.smanzana.nostrummagica.items;

import java.util.List;
import java.util.UUID;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DragonSoulItem extends PetSoulItem {
	
	public static final String ID = "dragon_soul_item";
	private static final String NBT_MANA = "mana";
	
	private static DragonSoulItem instance = null;
	public static DragonSoulItem instance() {
		if (instance == null)
			instance = new DragonSoulItem();
		
		return instance;
	}
	
	private DragonSoulItem() {
		super();
		this.setUnlocalizedName(ID);
		this.setRegistryName(NostrumMagica.MODID, ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		this.setCreativeTab(NostrumMagica.creativeTab);
	}

	@Override
	public String getLoreKey() {
		return "dragon_soul_items";
	}

	@Override
	public String getLoreDisplayName() {
		return "Dragon Souls";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("An intense energy radiates from this hunk of crystaline flesh as it glows.",
				"Inside is the soul of the dragon you obtained it from.",
				"The dragon soul is powerful, but incomplete. More magical energy is needed to bring the dragon back alive...");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("An intense energy radiates from this hunk of crystaline flesh as it glows.",
				"Inside is the soul of the dragon you obtained it from.",
				"The dragon soul must be given mana before it can be used to bring the dragon back to life");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_DRAGONS;
	}

	@Override
	protected void setWorldID(EntityLivingBase pet, UUID worldID) {
		if (pet instanceof EntityTameDragonRed) { // TODO new base class lol
			((EntityTameDragonRed) pet).setWorldID(worldID);
		}
	}

	@Override
	protected void beforePetRespawn(EntityLivingBase pet, World world, Vec3d pos, ItemStack stack) {
		// Dragons spawn at 50% health and 0% mana
		if (pet instanceof EntityTameDragonRed) { // TODO new base class lol
			EntityTameDragonRed dragon = ((EntityTameDragonRed) pet);
			final int mana = dragon.getMana();
			final float maxHP = dragon.getMaxHealth();
			
			dragon.setHealth(Math.max(1, maxHP/2));
			dragon.addMana(-mana);
		}
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		if (!playerIn.isCreative()) {
			return false;
		}
		
		if (playerIn.world.isRemote) {
			return true;
		}
		
		if (target instanceof EntityTameDragonRed) {
			ItemStack newStack = MakeSoulItem((EntityTameDragonRed) target);
			target.entityDropItem(newStack, 1);
			return true;
		}
		
		return false;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		final ItemStack held = playerIn.getHeldItem(hand);
		if (getMana(held) >= getMaxMana(held)) {
			return new ActionResult<ItemStack>(EnumActionResult.PASS, held);
		}
		
		playerIn.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, held);
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 20;
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		if (worldIn.isRemote) {
			return stack;
		}
		
		// Try to add mana into it
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entityLiving);
		if (attr != null) {
			int avail = Math.min(200, attr.getMana());
			int used = addMana(stack, avail);
			if (used > 0) {
				attr.addMana(-used);
			}
		}
		
		return stack;
	}
	
	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		if (player.world.isRemote) {
			// On client, spawn particles
			if (NostrumMagica.rand.nextBoolean()) {
				Vec3d offset;
				final float rotation;
				if (player == NostrumMagica.proxy.getPlayer() && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
					offset = new Vec3d(-.1, player.getEyeHeight() -.05, .2);
					rotation = -player.rotationYaw % 360f;
				} else {
					offset = new Vec3d(-.375, player.getEyeHeight() -.05, .825);
					if (player == NostrumMagica.proxy.getPlayer()) {
						rotation = -player.renderYawOffset % 360f;
					} else {
						rotation = -player.rotationYaw % 360f;
					}
				}
				
				final float rotRad = (float) ((rotation / 360f) * 2 * Math.PI);
				offset = offset.rotateYaw(rotRad);
				offset = offset.add(player.getPositionVector());
				// Need to adjust while watching to go to hand
				NostrumParticles.FILLED_ORB.spawn(player.world, new SpawnParams(
						1, offset.x, offset.y, offset.z, 1.0, 20, 0,
						offset
						).color(1f, .4f, .8f, 1f).dieOnTarget(true));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		
		String name = getPetName(stack);
		if (name == null || name.isEmpty()) {
			name = "Unknown Pet";
		}
		tooltip.add(ChatFormatting.DARK_RED + name + ChatFormatting.RESET);
		tooltip.add("" + ChatFormatting.BLUE + getMana(stack) + " / " + getMaxMana(stack));
	}
	
	@Override
	public boolean onEntityItemUpdate(net.minecraft.entity.item.EntityItem entityItem) {
		if (entityItem.world.isRemote) {
			// Particles!
			if (NostrumMagica.rand.nextBoolean()) {
				NostrumParticles.GLOW_ORB.spawn(entityItem.world, new SpawnParams(
						1, entityItem.posX, entityItem.posY + .5, entityItem.posZ, .25, 30, 10,
						new Vec3d(0, .05, 0), new Vec3d(.025, 0, .025)
						).color(.3f, .6f, 0f, 0f));
			}
		}
		
		return false;
	}
	
	public int getMana(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		
		return nbt.getInteger(NBT_MANA);
	}
	
	public void setMana(ItemStack stack, int mana) {
		mana = Math.min(mana, getMaxMana(stack));
		
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		
		nbt.setInteger(NBT_MANA, mana);
	}
	
	/**
	 * Attempts to add some mana into the item.
	 * Returns how much was actually used.
	 * @param stack
	 * @param availableMana
	 * @return
	 */
	public int addMana(ItemStack stack, int availableMana) {
		final int mana = getMana(stack);
		final int room = getMaxMana(stack) - mana;
		if (room <= 0 || availableMana == 0) {
			return 0;
		} else if (room >= availableMana) {
			setMana(stack, mana + availableMana);
			return availableMana;
		} else {
			setMana(stack, mana + room);
			return room;
		}
	}
	
	public int getMaxMana(ItemStack stack) {
		return 3000; // Could make it dynamic
	}
	
	public static ItemStack MakeSoulItem(EntityTameDragonRed dragon) {
		return MakeSoulItem(dragon, true);
	}
	
	public static ItemStack MakeSoulItem(EntityTameDragonRed dragon, boolean register) {
		if (register) {
			instance().setWorldID(dragon, NostrumMagica.getPetSoulRegistry().registerPet(dragon));
			NostrumMagica.getPetSoulRegistry().snapshotPet(dragon);
		}
		
		ItemStack stack = new ItemStack(instance());
		instance().setPet(stack, dragon);
		
		return stack;
	}

	@Override
	public boolean canSpawnEntity(World world, EntityLivingBase spawner, Vec3d pos, ItemStack stack) {
		if (this.getMana(stack) < this.getMaxMana(stack)) {
			spawner.sendMessage(new TextComponentTranslation("info.respawn_soulbound_dragon.fail.mana", new Object[0]));
			return false;
		}
		
		return true;
	}
}
