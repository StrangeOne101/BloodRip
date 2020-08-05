package com.strangeone101.abilities;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class BloodSource 
{
	public static ConcurrentHashMap<Block, BloodSource> instances = new ConcurrentHashMap<Block, BloodSource>();
	
	private Block block;
	private BlockData oldData;
	private BukkitRunnable task;
	private int usesLeft = 1;
	private long expirey;
	private long startTime;
	
	@SuppressWarnings("deprecation")
	public BloodSource(Block block)
	{		
		this.block = block;
		this.startTime = System.currentTimeMillis();
		this.expirey = BloodRip.BLOODSOURCETIME;
		
		/**If it's solid, don't bother*/
		if (block.getType().isSolid()) {
			return;
		}
		
		final BloodSource instance = this;
		
		if (block.getType() != Material.WATER) {
			this.oldData = block.getState().getBlockData().clone();
			/*if (block.getState() instanceof Waterlogged) {
				((Waterlogged) block.getState()).setWaterlogged(true);
			} else {*/
				block.setType(Material.WATER);
			//}

			new BukkitRunnable() {
				public void run() {
					for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendBlockChange(instance.block.getLocation(), instance.oldData);
					}
				}
			}.runTaskLater(ProjectKorra.plugin, 2L);
			
		}
		instances.put(block, this);
		
		
		task = new BukkitRunnable() {

			Random random = new Random();
			@Override
			public void run() {
				if (usesLeft <= 0 || System.currentTimeMillis() > startTime + expirey || instance.block.getType() != Material.WATER) {
					remove();
				} else {
					for (int i = 0; i < 16; i++) {
						instance.block.getWorld().spawnParticle(Particle.REDSTONE, instance.block.getLocation().clone().add(random.nextFloat(), random.nextFloat(), random.nextFloat()), 1, 0, 0, 0, new Particle.DustOptions(Color.RED, 1.5F));
					}
					ParticleEffect.BLOCK_CRACK.display(instance.block.getLocation().clone().add(0.5, 0.5, 0.5), 40, 0.3F, 0.3F, 0.3F, Material.REDSTONE_BLOCK.createBlockData());
				}
			}
			
		};
		
		((BukkitRunnable) task).runTaskTimer(ProjectKorra.plugin, 1L, 4L);
	}
	
	public void remove() {
		task.cancel();
		instances.remove(block);
		this.block.setBlockData(oldData);
	}
	
	
	public Block getBlock()
	{
		return block;
	}
	
	public void setExpirey(long expirey) {
		this.expirey = expirey;
	}
	
	public long getExpirey() {
		return expirey;
	}
	
	public void setUsesLeft(int usesLeft) {
		this.usesLeft = usesLeft;
	}
	
	public int getUsesLeft() {
		return usesLeft;
	}
}
