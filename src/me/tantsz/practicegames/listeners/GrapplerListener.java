package me.tantsz.practicegames.listeners;

import me.tantsz.practicegames.PracticeGames;
import net.minecraft.server.v1_8_R3.EntityFishingHook;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntitySnowball;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSnowball;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class GrapplerListener implements Listener {
    
    private final PracticeGames plugin;
    private Map<Player, Cordinha> hooks = new HashMap<>();
    private HashMap<Player, Long> leftClickGrappler = new HashMap<>();
    private HashMap<Player, Long> rightClickGrappler = new HashMap<>();
    
    public GrapplerListener(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getGrapplerManager().isPlayerInRace(player)) {
            return;
        }
        
        if ((hooks.containsKey(player)) && (!player.getItemInHand().getType().equals(Material.LEASH))) {
            hooks.get(player).remove();
            hooks.remove(player);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (!plugin.getGrapplerManager().isPlayerInRace(player)) {
            return;
        }
        
        if (item == null) {
            return;
        }
        
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String itemName = item.getItemMeta().getDisplayName();
            
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                
                if (itemName.equals("§eREINICIAR CORRIDA")) {
                    event.setCancelled(true);
                    plugin.getGrapplerManager().restartRace(player);
                    return;
                    
                } else if (itemName.equals("§cCANCELAR CORRIDA")) {
                    event.setCancelled(true);
                    plugin.getGrapplerManager().cancelRace(player);
                    return;
                }
            }
        }
        
        if (player.getItemInHand().getType().equals(Material.LEASH)) {
            event.setCancelled(true);
            
            if ((event.getAction() == Action.LEFT_CLICK_AIR) || (event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                if (leftClickGrappler.containsKey(player) && leftClickGrappler.get(player) > System.currentTimeMillis())
                    return;
                
                if (hooks.containsKey(player))
                    hooks.get(player).remove();
                
                Cordinha nmsHook = new Cordinha(player.getWorld(), ((CraftPlayer)player).getHandle());
                nmsHook.spawn(player.getEyeLocation().add(player.getLocation().getDirection().getX(), player.getLocation().getDirection().getY(), player.getLocation().getDirection().getZ()));
                nmsHook.move(player.getLocation().getDirection().getX() * 8.0D, player.getLocation().getDirection().getY() * 8.0D, player.getLocation().getDirection().getZ() * 8.0D);
                hooks.put(player, nmsHook);
                leftClickGrappler.put(player, System.currentTimeMillis() + 250L);
                
            } else {
                if (!hooks.containsKey(player))
                    return;
                
                if (rightClickGrappler.containsKey(player) && rightClickGrappler.get(player) > System.currentTimeMillis())
                    return;
                
                if (!hooks.get(player).isHooked())
                    return;
                
                rightClickGrappler.put(player, System.currentTimeMillis() + 150L);
                double d = hooks.get(player).getBukkitEntity().getLocation().distance(player.getLocation());
                double t = d;
                double v_x = (1.2D + 0.074D * t) * (hooks.get(player).getBukkitEntity().getLocation().getX() - player.getLocation().getX()) / t;
                double v_y = (1.2D + 0.068D * t) * (hooks.get(player).getBukkitEntity().getLocation().getY() - player.getLocation().getY()) / t;
                double v_z = (1.2D + 0.074D * t) * (hooks.get(player).getBukkitEntity().getLocation().getZ() - player.getLocation().getZ()) / t;
                Vector v = new Vector(player.getVelocity().getX(), 1.2D, player.getVelocity().getZ());
                v.setX(v_x);
                v.setY(v_y);
                v.setZ(v_z);
                player.setVelocity(v);
            }
        }
    }
    
    @EventHandler
    public void removerCordaAoTrocarSlot(PlayerItemHeldEvent e) {
        if (hooks.containsKey(e.getPlayer())) {
            hooks.get(e.getPlayer()).remove();
            hooks.remove(e.getPlayer());
        }
    }
    
    @EventHandler
    public void grapplerFallNerf(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        
        Player player = (Player)e.getEntity();
        
        if (!plugin.getGrapplerManager().isPlayerInRace(player))
            return;
        
        if (!e.getCause().equals(EntityDamageEvent.DamageCause.FALL))
            return;
        
        if ((hooks.containsKey(player)) && (hooks.get(player).isHooked()) && (e.getDamage() > 5.0D))
            e.setCancelled(true);
    }
    
    public class Cordinha extends EntityFishingHook {
        private Snowball sb;
        private EntitySnowball controller;
        public EntityHuman owner;
        public Entity hooked;
        public boolean lastControllerDead, isHooked;
        
        public Cordinha(org.bukkit.World world, EntityHuman entityhuman) {
            super(((CraftWorld)world).getHandle(), entityhuman);
            this.owner = entityhuman;
        }
        
        protected void c() {}
        
        public void t_() {
            this.lastControllerDead = this.controller.dead;
            for (Entity entity : this.controller.world.getWorld().getEntities()) {
                if (entity instanceof Player) {
                    continue;
                }
                
                if (!(entity instanceof LivingEntity)) {
                    continue;
                }
                if ((!(entity instanceof Firework)) && (entity.getEntityId() != getBukkitEntity().getEntityId()) &&
                        (entity.getEntityId() != this.owner.getBukkitEntity().getEntityId()) &&
                        (entity.getEntityId() != this.controller.getBukkitEntity().getEntityId()) && (
                        (entity.getLocation().distance(this.controller.getBukkitEntity().getLocation()) < 2.0D) || (((entity instanceof Player)) &&
                                (((Player)entity).getEyeLocation().distance(this.controller.getBukkitEntity().getLocation()) < 2.0D)))) {
                    this.controller.die();
                    this.hooked = entity;
                    this.isHooked = true;
                    this.locX = entity.getLocation().getX();
                    this.locY = entity.getLocation().getY();
                    this.locZ = entity.getLocation().getZ();
                    this.motX = 0.0D;
                    this.motY = 0.04D;
                    this.motZ = 0.0D;
                }
            }
            try {
                this.locX = this.hooked.getLocation().getX();
                this.locY = this.hooked.getLocation().getY();
                this.locZ = this.hooked.getLocation().getZ();
                this.motX = 0.0D;
                this.motY = 0.04D;
                this.motZ = 0.0D;
                this.isHooked = true;
            } catch (Exception e) {
                if (this.controller.dead)
                    this.isHooked = true;
                this.locX = this.controller.locX;
                this.locY = this.controller.locY;
                this.locZ = this.controller.locZ;
            }
        }
        
        public void die() {}
        
        public void remove() {
            super.die();
        }
        
        public void spawn(Location location) {
            this.sb = (Snowball)this.owner.getBukkitEntity().launchProjectile(Snowball.class);
            this.sb.setVelocity(this.sb.getVelocity().multiply(1.2D));
            this.controller = ((CraftSnowball)this.sb).getHandle();
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] { this.controller.getId() });
            for (Player p : Bukkit.getOnlinePlayers())
                ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
            ((CraftWorld)location.getWorld()).getHandle().addEntity(this);
        }
        
        public boolean isHooked() {
            return this.isHooked;
        }
        
        public void setHookedEntity(Entity damaged) {
            this.hooked = damaged;
        }
    }
}
