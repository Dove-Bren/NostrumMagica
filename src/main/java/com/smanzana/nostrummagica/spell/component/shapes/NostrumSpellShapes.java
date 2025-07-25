package com.smanzana.nostrummagica.spell.component.shapes;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape.RegisterSpellShapeEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//@ObjectHolder(NostrumMagica.MODID) i wish, but need to register before items
public class NostrumSpellShapes {

	public static final AIShape AI = new AIShape();
	public static final AtFeetShape AtFeet = new AtFeetShape();
	public static final AuraShape Aura = new AuraShape();
	public static final BarrageShape Barrage = new BarrageShape();
	public static final BeamShape Beam = new BeamShape();
	public static final BoulderShape Boulder = new BoulderShape();
	public static final BubbleSprayShape BubbleSpray = new BubbleSprayShape();
	public static final BurstShape Burst = new BurstShape();
	public static final ChainShape Chain = new ChainShape();
	public static final MagicCutterShape Cutter = new MagicCutterShape();
	public static final MagicCyclerShape Cycler = new MagicCyclerShape();
	public static final DelayShape Delay = new DelayShape();
	public static final FieldShape Field = new FieldShape();
	public static final ImbueShape Imbue = new ImbueShape();
	public static final MortarShape Mortar = new MortarShape();
	public static final OnDamageShape OnDamage = new OnDamageShape();
	public static final OnFoodShape OnFood = new OnFoodShape();
	public static final OnHealthShape OnHealth = new OnHealthShape();
	public static final OnManaShape OnMana = new OnManaShape();
	public static final ProjectileShape Projectile = new ProjectileShape();
	public static final ProximityShape Proximity = new ProximityShape();
	public static final RingShape Ring = new RingShape();
	public static final SeekingBulletShape SeekingBullet = new SeekingBulletShape();
	public static final SelfShape Self = new SelfShape();
	public static final TouchShape Touch = new TouchShape();
	public static final WallShape Wall = new WallShape();
	public static final WaveShape Wave = new WaveShape();
	
	@SubscribeEvent
	public static void registerShapes(RegisterSpellShapeEvent event) {
		event.getRegistry().register(AI);
		event.getRegistry().register(AtFeet);
		event.getRegistry().register(Aura);
		event.getRegistry().register(Barrage);
		event.getRegistry().register(Beam);
		event.getRegistry().register(Boulder);
		event.getRegistry().register(BubbleSpray);
		event.getRegistry().register(Burst);
		event.getRegistry().register(Chain);
		event.getRegistry().register(Cutter);
		event.getRegistry().register(Cycler);
		event.getRegistry().register(Delay);
		event.getRegistry().register(Field);
		event.getRegistry().register(Imbue);
		event.getRegistry().register(Mortar);
		event.getRegistry().register(OnDamage);
		event.getRegistry().register(OnFood);
		event.getRegistry().register(OnHealth);
		event.getRegistry().register(OnMana);
		event.getRegistry().register(Projectile);
		event.getRegistry().register(Proximity);
		event.getRegistry().register(Ring);
		event.getRegistry().register(SeekingBullet);
		event.getRegistry().register(Self);
		event.getRegistry().register(Touch);
		event.getRegistry().register(Wall);
		event.getRegistry().register(Wave);
	}
	
}
