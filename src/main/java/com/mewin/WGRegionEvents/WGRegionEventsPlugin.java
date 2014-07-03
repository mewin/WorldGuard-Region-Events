package com.mewin.WGRegionEvents;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author mewin
 */
public class WGRegionEventsPlugin extends JavaPlugin {
    private WGRegionEventsListener listener;
    private WorldGuardPlugin wgPlugin;
    
    @Override
    public void onEnable()
    {
        wgPlugin = getWGPlugin();
        if (wgPlugin == null)
        {
            getLogger().warning("Could not find World Guard, disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        listener = new WGRegionEventsListener(this, wgPlugin);
        
        getServer().getPluginManager().registerEvents(listener, wgPlugin);
    }
    
    private WorldGuardPlugin getWGPlugin()
    {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        
        if (plugin == null || !(plugin instanceof WorldGuardPlugin))
        {
            return null;
        }
        
        return (WorldGuardPlugin) plugin;
    }
}
