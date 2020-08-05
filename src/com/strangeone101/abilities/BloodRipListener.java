package com.strangeone101.abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.ParticleEffect;

import net.md_5.bungee.api.ChatColor;

public class BloodRipListener implements Listener {	
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent e) {
		if (!e.isSneaking()) return;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(e.getPlayer());
		if (bPlayer == null) return;
		if (bPlayer.canBend(CoreAbility.getAbility("BloodRip"))) {
			new BloodRip(e.getPlayer());
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		if (BloodRip.dedPlayers.contains(event.getEntity())) {		
			int i = 0;
			int y = -1;
			BloodSource source = null;
			while (i < 3 && y == -1) {
				if (!event.getEntity().getLocation().clone().add(0, -i, 0).getBlock().getType().isSolid() &&
						event.getEntity().getLocation().clone().add(0, -(i + 1), 0).getBlock().getType().isSolid()) {
					source = new BloodSource(event.getEntity().getLocation().clone().add(0, -i, 0).getBlock());
				}
				i++;
			}
			
			if (source == null) return; //Don't continue if this won't work
			
			if (!(event.getEntity() instanceof Player)) {
				
				long time = source.getExpirey() - 1000;
				
				if (event.getEntity().getMaxHealth() <= 10) time /= 2;
				if (event.getEntity().getMaxHealth() <= 5) time /= 2;
				if (time < 1500) time = 1500;
				
				source.setUsesLeft(1);
				source.setExpirey(time); //Halve the time it remains 
			}
		}
	}
	
	@EventHandler
	public void onSwing(PlayerAnimationEvent event) {
		if (event.isCancelled()) return;
		
		if (CoreAbility.hasAbility(event.getPlayer(), BloodRip.class)) {
			BloodRip bloodrip = CoreAbility.getAbility(event.getPlayer(), BloodRip.class);
			
			bloodrip.affect(bloodrip.getKillDamage());
			bloodrip.remove();
			bloodrip.getBendingPlayer().addCooldown(bloodrip);
		}
	}
	
	@EventHandler
	public void onFlow(BlockFromToEvent event) {
		if (event.isCancelled()) return;
		
		//Prevent our "blood" sources from flowing.
		if (BloodSource.instances.containsKey(event.getBlock())) event.setCancelled(true);
	}
	
	@EventHandler
	public void onDamge(EntityDamageEvent event) {
		if (event.isCancelled()) return;
		
		if (event.getEntity() instanceof Player && CoreAbility.hasAbility((Player)event.getEntity(), BloodRip.class)) {
			CoreAbility.getAbility((Player)event.getEntity(), BloodRip.class).remove();
			
			ActionBar.sendActionBar(ChatColor.RED + "* You lose focus *", (Player)event.getEntity());
			ParticleEffect.VILLAGER_ANGRY.display(event.getEntity().getLocation(), 4, 0.5F, 0.5F, 0.5F);
		}
	}
}
