package com.strangeone101.abilities;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class BloodSource 
{
	public static ConcurrentHashMap<Block, BloodSource> instances = new ConcurrentHashMap<Block, BloodSource>();
	
	private Block block;
	private MaterialData oldData;
	private BukkitRunnable task;
	private int usesLeft;
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
		
		if (block.getType() != Material.STATIONARY_WATER && block.getType() != Material.WATER) {
			this.oldData = block.getState().getData().clone();
			block.setType(Material.STATIONARY_WATER);
			new BukkitRunnable() {
				public void run() {
					for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendBlockChange(instance.block.getLocation(), instance.oldData.getItemType(), instance.oldData.getData());
					}
				}
			}.runTaskLater(ProjectKorra.plugin, 1L);
			
		}
		instances.put(block, this);
		
		
		task = new BukkitRunnable() {

			Random random = new Random();
			@Override
			public void run() {
				if (usesLeft <= 0 || System.currentTimeMillis() > startTime + expirey || instance.block.getType() != Material.STATIONARY_WATER) {
					remove();
				} else {
					for (int i = 0; i < 16; i++) {
						ParticleEffect.RED_DUST.display(0, 0, 0, 0.004F, 0, instance.block.getLocation().clone().add(random.nextFloat(), random.nextFloat(), random.nextFloat()), 80D);
					}
					ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(Material.REDSTONE_BLOCK, (byte)0), 0F, 0F, 0F, 1, 40, instance.block.getLocation().clone().add(0.5, 0.5, 0.5), 80);
				}
			}
			
		};
		
		((BukkitRunnable) task).runTaskTimer(ProjectKorra.plugin, 1L, 4L);
	}
	
	@SuppressWarnings("deprecation")
	public void remove() {
		task.cancel();
		instances.remove(block);
		this.block.setType(oldData.getItemType());
		this.block.setData(oldData.getData());
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
