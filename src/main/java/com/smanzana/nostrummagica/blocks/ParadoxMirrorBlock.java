package com.smanzana.nostrummagica.blocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.ParadoxMirrorTileEntity;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Magic mirror that links to another and transports items!
 */
public class ParadoxMirrorBlock extends BlockContainer implements ILoreTagged {
	
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	private static final double BB_DEPTH = 2.0 / 16.0;
	private static final double BB_MARGIN = 1.0 / 16.0;
	private static final AxisAlignedBB AABB_N = new AxisAlignedBB(BB_MARGIN, 0, 1 - BB_DEPTH, 1 - BB_MARGIN, 1, 1); // TODO final
	private static final AxisAlignedBB AABB_E = new AxisAlignedBB(0, 0, BB_MARGIN, BB_DEPTH, 1, 1-BB_MARGIN);
	private static final AxisAlignedBB AABB_S = new AxisAlignedBB(BB_MARGIN, 0, 0, 1-BB_MARGIN, 1, BB_DEPTH);
	private static final AxisAlignedBB AABB_W = new AxisAlignedBB(1 - BB_DEPTH, 0, BB_MARGIN, 1, 1, 1-BB_MARGIN);
	
	private static final String NBT_LINKED_POS = "linked_pos";
	
	public static final String ID = "paradox_mirror";
	
	private static ParadoxMirrorBlock instance = null;
	public static ParadoxMirrorBlock instance() {
		if (instance == null)
			instance = new ParadoxMirrorBlock();
		
		return instance;
	}
	
		public ParadoxMirrorBlock() {
		super(Material.IRON, MapColor.GOLD);
		this.setUnlocalizedName(ID);
		this.setHardness(.5f);
		this.setResistance(0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.GLASS);
		//this.setHarvestLevel("pickaxe", 0);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		//return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
		EnumFacing side = placer.getHorizontalFacing().getOpposite();
		if (!this.canPlaceAt(world, pos, side)) {
			// Rotate and find it
			for (int i = 0; i < 3; i++) {
				side = side.rotateY();
				if (this.canPlaceAt(world, pos, side)) {
					break;
				}
			}
		}
		
		return this.getDefaultState()
				.withProperty(FACING, side);
	}
	
	protected boolean canPlaceAt(World worldIn, BlockPos pos, EnumFacing side) {
		IBlockState state = worldIn.getBlockState(pos.offset(side.getOpposite()));
		if (state == null || !(state.isSideSolid(worldIn, pos.offset(side.getOpposite()), side.getOpposite()))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			if (canPlaceAt(worldIn, pos, side)) {
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos posFrom) {
		EnumFacing face = state.getValue(FACING);
		if (!canPlaceAt(worldIn, pos, face)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
		
		super.neighborChanged(state, worldIn, pos, blockIn, posFrom);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		switch (state.getValue(FACING)) {
		case NORTH:
		case UP:
		case DOWN:
		default:
			return AABB_N;
		case EAST:
			return AABB_E;
		case SOUTH:
			return AABB_S;
		case WEST:
			return AABB_W;
		
		}
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		switch (blockState.getValue(FACING)) {
		case NORTH:
		case UP:
		case DOWN:
		default:
			return AABB_N;
		case EAST:
			return AABB_E;
		case SOUTH:
			return AABB_S;
		case WEST:
			return AABB_W;
		
		}
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if (worldIn.isRemote) {
			return true;
		}
		
		ParadoxMirrorTileEntity mirror = getTileEntity(worldIn, pos);
		if (mirror != null) {
			// If sneaking, try to send item through
			if (playerIn.isSneaking()) {
				@Nonnull ItemStack held = playerIn.getHeldItem(hand);
				if (held.isEmpty()) {
					return false;
				}
				
				// If we have an item, return true even if mirror is on cooldown
				if (mirror.tryPushItem(held)) {
					// Item was pushed! Remove from hand!
					playerIn.setHeldItem(hand, ItemStack.EMPTY);
				}
				return true;
			}
			// Else try and set position from held item
			else {
				@Nonnull ItemStack held = playerIn.getHeldItem(hand);
				if (held.isEmpty()) {
					return false;
				}
				
				// If we have an item, return true only if item has a position we can use
				if (held.getItem() instanceof PositionCrystal) {
					BlockPos heldPos = PositionCrystal.getBlockPosition(held);
					if (heldPos != null && !heldPos.equals(pos)) {
						mirror.setLinkedPosition(heldPos);
						playerIn.sendMessage(new TextComponentTranslation("info.generic.block_linked"));
					}
					return true; // true even if crystal doesn't have position
				}
				return false;
			}
		}
		
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new ParadoxMirrorTileEntity();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		// No actual work to do
//		TileEntity ent = world.getTileEntity(pos);
//		if (ent == null || !(ent instanceof ParadoxMirrorTileEntity))
//			return;
//		
//		ParadoxMirrorTileEntity mirror = (ParadoxMirrorTileEntity) ent;
//		IInventory inv = putter.getInventory();
//		for (int i = 0; i < inv.getSizeInventory(); i++) {
//			ItemStack item = inv.getStackInSlot(i);
//			if (!item.isEmpty()) {
//				double x, y, z;
//				x = pos.getX() + .5;
//				y = pos.getY() + .5;
//				z = pos.getZ() + .5;
//				world.spawnEntity(new EntityItem(world, x, y, z, item.copy()));
//			}
//		}
	}
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (willHarvest) return true; // Hack to not remove block and tile entity until after getDrops() is called. See BlockFlowerPot for
		// forge patch that does this, too.
		return super.removedByPlayer(state, world, pos, player, willHarvest);
		
		//BlockFlowerPot b;
	}
	
	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		super.harvestBlock(worldIn, player, pos, state, te, stack);
		
		// Have to manually set block to air since we overrode removedByPlayer to not remove the block
		worldIn.setBlockToAir(pos);
	}
	
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		// ItemStack should carry NBT about linked position
		ItemStack drop = new ItemStack(Item.getItemFromBlock(this));
		
		BlockPos linkedPos = getLinkedPosition(world, pos);
		if (linkedPos != null) {
			NBTTagCompound tag = drop.getTagCompound();
			if (tag == null) {
				tag = new NBTTagCompound();
			}
			
			tag.setLong(NBT_LINKED_POS, linkedPos.toLong());
			drop.setTagCompound(tag);
		}
		
		drops.add(drop);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		
		// Read linked position off of item stack, if present
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey(NBT_LINKED_POS, NBT.TAG_LONG)) {
			this.setLinkedPosition(worldIn, pos, BlockPos.fromLong(tag.getLong(NBT_LINKED_POS)));
		}
	}
	
	public @Nullable BlockPos getLinkedPosition(IBlockAccess world, BlockPos pos) {
		ParadoxMirrorTileEntity mirror = getTileEntity(world, pos);
		if (mirror != null) {
			return mirror.getLinkedPosition();
		}
		
		return null;
	}
	
	public void setLinkedPosition(IBlockAccess world, @Nullable BlockPos pos, BlockPos linkedPos) {
		ParadoxMirrorTileEntity mirror = getTileEntity(world, pos);
		if (mirror != null) {
			mirror.setLinkedPosition(linkedPos);
		}
	}
	
	protected @Nullable ParadoxMirrorTileEntity getTileEntity(IBlockAccess world, BlockPos pos) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof ParadoxMirrorTileEntity))
			return null;
		
		return (ParadoxMirrorTileEntity) ent;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.isEmpty() || !stack.hasTagCompound() || !stack.getTagCompound().hasKey(NBT_LINKED_POS, NBT.TAG_LONG))
			return;
		
		BlockPos pos = BlockPos.fromLong(stack.getTagCompound().getLong(NBT_LINKED_POS));
		
		if (pos == null)
			return;
		
		tooltip.add(TextFormatting.GREEN + "<" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ">" + TextFormatting.RESET);
	}

	@Override
	public String getLoreKey() {
		return "paradox_mirror";
	}

	@Override
	public String getLoreDisplayName() {
		return "Paradox Mirrors";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add(
				"Mystic mirrors that accept items in on one side and transport them to another mirror somewhere else!"
				);
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add(
				"Mystic mirrors that accept items in on one side and transport them to another mirror somewhere else!",
				"Link a mirror to another using a geogem holding the other mirror's position.",
				"Mirrors do not have to be linked to one another to function. A mirror can point to a mirror that's pointing to a third mirror.",
				"Mirros will not teleport items they received until the item is picked and up and dropped again, or went more than 2 blocks away."
				);
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
