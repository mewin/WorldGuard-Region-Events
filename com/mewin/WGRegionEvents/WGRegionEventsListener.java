package com.mewin.WGRegionEvents;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.mewin.WGRegionEvents.events.RegionEnteredEvent;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.mewin.WGRegionEvents.events.RegionLeftEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 *
 * @author mewin
 */
public class WGRegionEventsListener implements Listener {
    private WorldGuardPlugin wgPlugin;
    private WGRegionEventsPlugin plugin;
    
    private Map<Player, Set<ProtectedRegion>> playerRegions;
    
    public WGRegionEventsListener(WGRegionEventsPlugin plugin, WorldGuardPlugin wgPlugin)
    {
        this.plugin = plugin;
        this.wgPlugin = wgPlugin;
        
        playerRegions = new HashMap<Player, Set<ProtectedRegion>>();
    }
    
    @EventHandler
    public void onPlayerKick(PlayerKickEvent e)
    {
        Set<ProtectedRegion> regions = playerRegions.remove(e.getPlayer());
        Location from = e.getPlayer().getLocation();

        if (regions != null)
        {
            for(ProtectedRegion region : regions)
            {
                RegionLeaveEvent leaveEvent = new RegionLeaveEvent(region, e.getPlayer(), MovementWay.DISCONNECT, from);
                RegionLeftEvent leftEvent = new RegionLeftEvent(region, e.getPlayer(), MovementWay.DISCONNECT, from);

                plugin.getServer().getPluginManager().callEvent(leaveEvent);
                plugin.getServer().getPluginManager().callEvent(leftEvent);
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        Set<ProtectedRegion> regions = playerRegions.remove(e.getPlayer());
        Location from = e.getPlayer().getLocation();

        if (regions != null)
        {
            for(ProtectedRegion region : regions)
            {
                RegionLeaveEvent leaveEvent = new RegionLeaveEvent(region, e.getPlayer(), MovementWay.DISCONNECT, from);
                RegionLeftEvent leftEvent = new RegionLeftEvent(region, e.getPlayer(), MovementWay.DISCONNECT, from);

                plugin.getServer().getPluginManager().callEvent(leaveEvent);
                plugin.getServer().getPluginManager().callEvent(leftEvent);
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e)
    {
        e.setCancelled(updateRegions(e.getPlayer(), MovementWay.MOVE, e.getTo(), e.getFrom()));
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e)
    {
        e.setCancelled(updateRegions(e.getPlayer(), MovementWay.TELEPORT, e.getTo(), e.getFrom()));
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        updateRegions(e.getPlayer(), MovementWay.SPAWN, e.getPlayer().getLocation(), null);
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e)
    {
        updateRegions(e.getPlayer(), MovementWay.SPAWN, e.getRespawnLocation(), null);
    }
    
    private synchronized boolean updateRegions(final Player player, final MovementWay movement, Location to, final Location from)
    {
        Set<ProtectedRegion> regions;
        Set<ProtectedRegion> oldRegions;
        
        if (playerRegions.get(player) == null)
        {
            regions = new HashSet<ProtectedRegion>();
        }
        else
        {
            regions = new HashSet<ProtectedRegion>(playerRegions.get(player));
        }
        
        oldRegions = new HashSet<ProtectedRegion>(regions);
        
        RegionManager rm = wgPlugin.getRegionManager(to.getWorld());
        
        if (rm == null)
        {
            return false;
        }
        
        ApplicableRegionSet appRegions = rm.getApplicableRegions(to);
        
        for (final ProtectedRegion region : appRegions)
        {
            if (!regions.contains(region))
            {
                RegionEnterEvent e = new RegionEnterEvent(region, player, movement, from);
                
                plugin.getServer().getPluginManager().callEvent(e);
                
                if (e.isCancelled())
                {
                    regions.clear();
                    regions.addAll(oldRegions);
                    
                    return true;
                }
                else
                {
                    (new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                sleep(50);
                            }
                            catch(InterruptedException ex)
                            {}
                            RegionEnteredEvent e = new RegionEnteredEvent(region, player, movement, from);
                            
                            plugin.getServer().getPluginManager().callEvent(e);
                        }
                    }).start();
                    regions.add(region);
                }
            }
        }
        
        Collection<ProtectedRegion> app = (Collection<ProtectedRegion>) getPrivateValue(appRegions, "applicable");
        Iterator<ProtectedRegion> itr = regions.iterator();
        while(itr.hasNext())
        {
            final ProtectedRegion region = itr.next();
            if (!app.contains(region))
            {
                if (rm.getRegion(region.getId()) != region)
                {
                    itr.remove();
                    continue;
                }
                RegionLeaveEvent e = new RegionLeaveEvent(region, player, movement, from);

                plugin.getServer().getPluginManager().callEvent(e);

                if (e.isCancelled())
                {
                    regions.clear();
                    regions.addAll(oldRegions);
                    return true;
                }
                else
                {
                    (new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                sleep(50);
                            }
                            catch(InterruptedException ex)
                            {}
                            RegionLeftEvent e = new RegionLeftEvent(region, player, movement, from);
                            
                            plugin.getServer().getPluginManager().callEvent(e);
                        }
                    }).start();
                    itr.remove();
                }
            }
        }
        playerRegions.put(player, regions);
        return false;
    }
    
    private Object getPrivateValue(Object obj, String name)
    {
        try {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception ex) {
            return null;
        }
        
    }
}
