package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants.NBT;

public class RoomLectern extends StaticRoom {
	
	public RoomLectern() {
		super(-18, -1, 0, 2, 4, 6,
				// Floor
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 1
				"XXXXXXXXXXXXXXXXXXCXX",
				"X                 C X",
				"X QQQT            C X",
				"XGQQQTCCCCCCCCCCCCC X",
				"X QQQT            C X",
				"X                 C X",
				"XXXXXXXXXXXXXXXXXXCXX",
				// Layer 2
				"XXXXXXXXXXXXXXXXXX XX",
				"X        S          X",
				"X                  WX",
				"X  L                X",
				"X                  WX",
				"X        N          X",
				"XXXXXXXXXXXXXXXXXX XX",
				// Layer 3
				"XXXXXXXXXXXXXXXXXXXXX",
				"X                   X",
				"XE                  X",
				"X                   X",
				"XE                  X",
				"X                   X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 4
				"XXXXXXXXXXXXXXXXXXXXX",
				"X                   X",
				"X                   X",
				"X                   X",
				"X                   X",
				"X                   X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 5
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				'X', NostrumBlocks.lightDungeonBlock,
				'W', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.WEST)),
				'E', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.EAST)),
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				'W', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.SOUTH)),
				'C', new StaticBlockState(Blocks.RED_CARPET),
				'T', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'Q', Blocks.QUARTZ_BLOCK,
				'G', new StaticBlockState(NostrumBlocks.singleSpawner.getState(NostrumSingleSpawner.Type.GOLEM_FIRE)),
				'L', NostrumBlocks.altar,
				' ', null);
		// T STAIR, Q QUARTZ, L PEDESTAL+SPELL,  NW norsou, g golem
	}

	@Override
	public int getNumExits() {
		return 1;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		BlockPos exit = new BlockPos(0, 0, 6);
		
		list.add(NostrumDungeon.asRotated(start, exit, Direction.NORTH));
		
		return list;
	}

	@Override
	public int getDifficulty() {
		return 4;
	}

	@Override
	public boolean hasEnemies() {
		return true;
	}

	@Override
	public boolean hasTraps() {
		return false;
	}

	@Override
	public boolean supportsDoor() {
		return false;
	}

	@Override
	public boolean supportsKey() {
		return false;
	}

	@Override
	public DungeonExitPoint getKeyLocation(DungeonExitPoint start) {
		return null;
	}

	@Override
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start) {
		return new LinkedList<>();
	}
	
	static private String benePrefixs[] = {
			"Kiss of",
			"Boon of",
			"Blessing of",
			"Grace of",
			"",
			"",
	};
	
	static private String negeDamagePrefixes[] = {
			"Wrath of",
			"Fist of",
			"Anger of",
			"Spit of",
			"Burst of",
			"Fury of",
			"Blade of",
			"",
	};
	
	static private String negeStatusPrefixes[] = {
			"Will of",
			"Disgrace of",
			"Burden of",
			"Curse of",
			"Scorn of",
	};
	
	static private String genSpellName(Random rand, EMagicElement elem, boolean harmful, boolean status)
	{
		StringBuilder builder = new StringBuilder();
		if (harmful) {
			if (status) {
				builder.append(negeStatusPrefixes[rand.nextInt(negeStatusPrefixes.length)]);
			} else {
				builder.append(negeDamagePrefixes[rand.nextInt(negeDamagePrefixes.length)]);
			}
		} else {
			builder.append(benePrefixs[rand.nextInt(benePrefixs.length)]);
		}
		
		String elemStr;
		switch (elem) {
		case EARTH:
			elemStr = "The Earth";
			break;
		case ENDER:
			elemStr = "The End";
			break;
		case FIRE:
			elemStr = "The Inferno";
			break;
		case ICE:
			elemStr = "The Ice";
			break;
		case LIGHTNING:
			elemStr = "The Lightning";
			break;
		case PHYSICAL:
			elemStr = "Metal";
			break;
		case WIND:
			elemStr = "The Storm";
			break;
		default:
			elemStr = "";
			break;
		
		}
		
		builder.append(" ");
		builder.append(elemStr);
		
		
		return builder.toString();
	}
	
	static private Spell genSpell(Random rand) {
		EMagicElement element;
		boolean harmful = false;
		boolean status = false;
		
		// Determine what kind of spell it's going to be!
		element = EMagicElement.values()[rand.nextInt(EMagicElement.values().length)];
		harmful = rand.nextBoolean();
		if (harmful) {
			status = rand.nextBoolean();
		}
		
		// Build the spell
		Spell spell = new Spell(genSpellName(rand, element, harmful, status), false);
		
		if (harmful) {
			
			if (rand.nextBoolean() && rand.nextBoolean()) {
				spell.addPart(new SpellPart(BeamTrigger.instance()));
			} else {
				spell.addPart(new SpellPart(ProjectileTrigger.instance()));
				
				if (rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean()) {
					spell.addPart(new SpellPart(ProximityTrigger.instance()));
				}
			}
			
			int effects = 1;
			if (rand.nextBoolean() && rand.nextBoolean())
				effects++;
			
			for (int i = 0; i < effects; i++) {
				SpellShape shape;
				if (rand.nextBoolean() && rand.nextBoolean()) {
					shape = AoEShape.instance();
				}
				else if (rand.nextBoolean() && rand.nextBoolean()) {
					shape = ChainShape.instance();
				}
				else {
					shape = SingleShape.instance();
				}
				
				int potency = 1;
				if (rand.nextBoolean())
					potency++;
				
				EAlteration alt = null;
				if (status) {
					alt = EAlteration.INFLICT;
				}
				
				spell.addPart(new SpellPart(shape, element, potency, alt));
			}
		} else {
			boolean self = rand.nextBoolean();
			if (self) {
				spell.addPart(new SpellPart(SelfTrigger.instance()));
			} else {
				spell.addPart(new SpellPart(ProjectileTrigger.instance()));
			}
			
			int effects = 1;
			if (rand.nextBoolean() && rand.nextBoolean())
				effects++;
			if (rand.nextBoolean() && rand.nextBoolean())
				effects++;
			
			List<EAlteration> alts = Lists.newArrayList(
					EAlteration.RESIST,
					EAlteration.SUPPORT,
					EAlteration.SUMMON
					);
			if (element != EMagicElement.FIRE) {
				alts.add(EAlteration.GROWTH);
			}
			
			for (int i = 0; i < effects; i++) {
			
				int potency = 1;
				if (rand.nextInt(6) == 0)
					potency = 2;
				SpellShape shape;
				if (rand.nextBoolean() && rand.nextBoolean()) {
					shape = AoEShape.instance();
				}
				else if (rand.nextBoolean() && rand.nextBoolean()) {
					shape = ChainShape.instance();
				}
				else {
					shape = SingleShape.instance();
				}
				
				spell.addPart(new SpellPart(shape, element, potency, alts.get(rand.nextInt(alts.size()))));
			}
		}
		
		return spell;
	}
	
	@Override
	public void spawn(NostrumDungeon dungeon, IWorld world, DungeonExitPoint start)
	{
		super.spawn(dungeon, world, start);
		
		// Fill out lectern!
		BlockPos offset = new BlockPos(-15, 1, 3);
		DungeonExitPoint point = NostrumDungeon.asRotated(start, offset, start.getFacing());
		BlockPos pos = point.getPos();
		TileEntity ent = world.getTileEntity(pos);
		if (null == ent)
		{
			System.out.println("Could not find lectern! (" + pos.getX() + " " + pos.getY() + " " + pos.getZ());
			world.setBlockState(pos, Blocks.BEDROCK.getDefaultState(), 2);
		}
		else
		{
			AltarTileEntity te = (AltarTileEntity) ent;
			ItemStack scroll = new ItemStack(NostrumItems.spellScroll, 1);
			Spell spell =  genSpell(world.getRandom());
			SpellScroll.setSpell(scroll, spell);
			scroll.setDamage(NostrumMagica.rand.nextInt(10));
			
			// Set description
			CompoundNBT nbt = scroll.getTag();
			ListNBT list = nbt.getList("Lore", NBT.TAG_STRING);
			if (null == list)
				list = new ListNBT();
			
			list.add(new StringNBT(spell.getDescription()));
			nbt.put("Lore", list);
			scroll.setTag(nbt);
			
			te.setItem(scroll);
		}
	}
}
