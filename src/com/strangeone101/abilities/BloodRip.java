package com.strangeone101.abilities;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BloodAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ParticleEffect.BlockData;

public class BloodRip extends BloodAbility implements AddonAbility
{	
	
	public static double RANGE = 8;
	public static long COOLDOWN = 10000L;
	public static long CHARGETIME = 5000L;
	public static double MAXDAMAGE = 12;
	public static boolean LEAVESBLOODSOURCE = true;
	public static long BLOODSOURCETIME = 5000L;
	public static boolean CANKILL = true;
	public static int BLOODSOURCEUSES = 1;
	
	private LivingEntity entity;
	private double range;
	private long lastChargeDisplay;
	
	private boolean canOnlyBeUsedAtNight;
	//private boolean canBeUsedOnUndeadMobs;
	private boolean onlyUsableDuringMoon;
	//private boolean canBloodbendOtherBloodbenders;

	public static Random random = new Random();
	
	public static CopyOnWriteArrayList<Entity> dedPlayers = new CopyOnWriteArrayList<Entity>();
	
	public BloodRip(Player player)
	{
		super(player);
		
		this.entity = (LivingEntity) GeneralMethods.getTargetedEntity(player, RANGE);
		if (entity == null || !(entity instanceof LivingEntity)) {
			remove();
			return;
		}
		
		this.canOnlyBeUsedAtNight = getConfig().getBoolean("Abilities.Water.Bloodbending.CanOnlyBeUsedAtNight");
		//this.canBeUsedOnUndeadMobs = getConfig().getBoolean("Abilities.Water.Bloodbending.CanBeUsedOnUndeadMobs");
		this.onlyUsableDuringMoon = getConfig().getBoolean("Abilities.Water.Bloodbending.CanOnlyBeUsedDuringFullMoon");
		//this.canBloodbendOtherBloodbenders = getConfig().getBoolean("Abilities.Water.Bloodbending.CanBloodbendOtherBloodbenders");
		this.range = RANGE;
		
		if (canOnlyBeUsedAtNight && !isNight(player.getWorld()) && !bPlayer.canBloodbendAtAnytime()) {
			remove();
			return;
		} else if (onlyUsableDuringMoon && !isFullMoon(player.getWorld()) && !bPlayer.canBloodbendAtAnytime()) {
			remove();
			return;
		} else if (!bPlayer.canBend(this) && !bPlayer.isAvatarState()) {
			remove();
			return;
		}
		this.lastChargeDisplay = System.currentTimeMillis() - 1000;
		start();
	}
	
	@Override
	public void progress() 
	{	
		if (!bPlayer.canBend(this) || this.entity == null || this.entity.isDead() || 
				!this.entity.getLocation().getWorld().equals(getPlayer().getLocation().getWorld()) || 
						getPlayer().getLocation().distance(this.entity.getLocation()) > range) {
			remove();
			return;
		}
		
		
		long neededChargeTime = (long) (CHARGETIME / (this.entity.getMaxHealth() > MAXDAMAGE ? this.entity.getMaxHealth() : MAXDAMAGE) * this.entity.getHealth());
		long chargeTime = System.currentTimeMillis() - this.getStartTime();
		if (chargeTime > neededChargeTime) chargeTime = neededChargeTime;
		
		if (this.lastChargeDisplay + 1000 < System.currentTimeMillis() || chargeTime >= neededChargeTime) {
			
			displayCharge(1 / neededChargeTime * chargeTime);
			for (int i = 0; i < (chargeTime >= neededChargeTime ? 2 : 8); i++) {
				ParticleEffect.SMOKE.display(new Vector(0, 0.1, 0), 0.5F, this.entity.getLocation().add(0.5 - random.nextFloat(), 0.7 + random.nextFloat() / 2, 0.5 - random.nextFloat()), 257D);
			}
			this.lastChargeDisplay = System.currentTimeMillis();
		}
		
		if (!this.player.isSneaking())
		{
			if (chargeTime >= 1000L) {
				affect(MAXDAMAGE / neededChargeTime * chargeTime);
				bPlayer.addCooldown(this);
				remove();
			} else {
				remove();
			}
			
		}
		
		return;
	}	
	
	public double getKillDamage() {
		long neededChargeTime = (long) (CHARGETIME / (this.entity.getMaxHealth() > MAXDAMAGE ? this.entity.getMaxHealth() : MAXDAMAGE) * this.entity.getHealth());
		long chargeTime = System.currentTimeMillis() - this.getStartTime();
		
		return MAXDAMAGE / neededChargeTime * chargeTime;
	}
	
	private void displayCharge(float charge) {
		Location location = GeneralMethods.getRightSide(this.player.getLocation(), 0.55D).add(0.0D, 1.3D, 0.0D).toVector().add(this.player.getEyeLocation().getDirection().clone().multiply(0.75D)).toLocation(this.player.getWorld());
		ParticleEffect.RED_DUST.display(255 * charge, 0, 0, 0.004F, 0, location, 257D);
		ParticleEffect.RED_DUST.display(255 * charge, 0, 0, 0.004F, 0, location, 257D);
	}
	
	public void affect(double damage)
	{
		if (this.entity.getHealth() <= damage && damage > 0.5D)
		{
			if (!CANKILL) {
				damage = this.entity.getHealth() - 0.5D;
			} else if (LEAVESBLOODSOURCE) {
				dedPlayers.add(this.entity);
				
				new BukkitRunnable() {
					@Override
					public void run() {
						dedPlayers.remove(entity);
					}
				}.runTaskLater(ProjectKorra.plugin, 2L);
			}
			
		}
		DamageHandler.damageEntity(entity, damage, this);
		for (int i = 0; i < 16; i++) {
			ParticleEffect.RED_DUST.display(0, 0, 0, 0.004F, 0, this.entity.getLocation().add(0.5 - random.nextFloat(), -0.25D + random.nextFloat(), 0.5 - random.nextFloat()), 257D);
		}
		ParticleEffect.BLOCK_CRACK.display(new BlockData(Material.REDSTONE_BLOCK, (byte)0), 0, 0.4F, 0, 0.5F, 48, this.entity.getLocation(), 256);
		
			
	}

	@Override
	public long getCooldown() {
		return COOLDOWN;
	}

	@Override
	public String getName() 
	{
		return "BloodRip";
	}

	@Override
	public boolean isHarmlessAbility() 
	{
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public String getAuthor() {
		return "StrangeOne101";
	}

	@Override
	public String getVersion() 
	{
		return "1.0";
	}

	@Override
	public void load() {
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new BloodRipListener(), ProjectKorra.plugin);
		
		ProjectKorra.plugin.getLogger().log(Level.INFO, getName() + " v" + getVersion() + " by " + getAuthor() + " loaded!");
		ProjectKorra.plugin.getLogger().log(Level.INFO, "You're a good person for installing this ability. Take that how you will. :)");
		
		ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.StrangeOne101.BloodRip.ChargeTime", CHARGETIME);
		ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.StrangeOne101.BloodRip.Range", RANGE);
		//ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.StrangeOne101.CactusBlast.ThrowRange", 36);
		ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.StrangeOne101.BloodRip.Cooldown", COOLDOWN);
		ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.StrangeOne101.BloodRip.BloodSource.Enabled", LEAVESBLOODSOURCE);
		//ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.StrangeOne101.BloodRip.BloodSource.MaxUses", BLOODSOURCETIME);
		ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.StrangeOne101.BloodRip.BloodSource.MaxTime", BLOODSOURCETIME);
		ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.StrangeOne101.BloodRip.CanKill", CANKILL);
		ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.StrangeOne101.BloodRip.MaxDamage", MAXDAMAGE);

		CHARGETIME = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.StrangeOne101.BloodRip.ChargeTime");
		RANGE = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.StrangeOne101.BloodRip.Range");
		MAXDAMAGE = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.StrangeOne101.BloodRip.MaxDamage");
		COOLDOWN = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.StrangeOne101.BloodRip.Cooldown");
		CANKILL = ConfigManager.defaultConfig.get().getBoolean("ExtraAbilities.StrangeOne101.BloodRip.CanKill");
		BLOODSOURCETIME = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.StrangeOne101.BloodRip.BloodSource.MaxTime");
		//BLOODSOURCEUSES = ConfigManager.defaultConfig.get().getInt("ExtraAbilities.StrangeOne101.BloodRip.BloodSource.MaxUses");
		LEAVESBLOODSOURCE = ConfigManager.defaultConfig.get().getBoolean("ExtraAbilities.StrangeOne101.BloodRip.BloodSource.Enabled");
		
		ConfigManager.defaultConfig.save();

		ConfigManager.languageConfig.get().addDefault("Abilities.Water.BloodRip.DeathMessage", "{victim} was purged by {attacker}'s {ability}");
		
		ConfigManager.languageConfig.save();
	}

	@Override
	public void stop() {
		for (BloodSource source : BloodSource.instances.values()) {
			source.remove();
		}
	}
	
	@Override
	public String getDescription() {
		return "BloodRip is another very dark Bloodbending move. Hold Sneak to charge up the move while looking at a player or a mob, then release Sneak to rip the blood out of them." + (LEAVESBLOODSOURCE ? " If you kill them, you can even use their blood as water." : "");
	}

	@Override
	public Location getLocation() {
		return this.entity != null ? this.entity.getLocation() : this.player.getLocation();
	}
	
	@Override
	public void remove() {
		super.remove();
	}
	
}
