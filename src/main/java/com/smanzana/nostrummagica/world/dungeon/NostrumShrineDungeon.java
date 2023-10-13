package com.smanzana.nostrummagica.world.dungeon;

import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.world.dungeon.room.ISpellComponentRoom;

import net.minecraft.world.World;

public class NostrumShrineDungeon extends NostrumDungeon {

	public static interface ComponentGenerator {
		public SpellComponentWrapper getRandom();
	}
	
	private ComponentGenerator component;
	
	public NostrumShrineDungeon(ComponentGenerator component, 
			ISpellComponentRoom starting, ISpellComponentRoom ending) {
		super(starting, ending);
		this.component = component;
	}
	
	@Override
	public void spawn(World world, DungeonExitPoint start) {
		SpellComponentWrapper comp = component.getRandom();
		((ISpellComponentRoom) this.starting).setComponent(comp);
		((ISpellComponentRoom) this.ending).setComponent(comp);
		
        //SeekerIdol.addDungeon(world, comp, start.getPos());
		
		super.spawn(world, start);
	}

}
