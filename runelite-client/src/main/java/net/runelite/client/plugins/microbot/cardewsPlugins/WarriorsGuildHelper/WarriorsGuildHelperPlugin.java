package net.runelite.client.plugins.microbot.cardewsPlugins.WarriorsGuildHelper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.cardewsPlugins.SailingHelper.SailingHelperConfig;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Cardew + "Warriors Guild Helper",
        description = "Completes the Warriors Guild defender progression.",
        tags = {"warriors guild", "dragon defender", "cardew", "cd"},
        enabledByDefault = false
)
@Slf4j
public class WarriorsGuildHelperPlugin extends Plugin {
    @Inject
    private WarriorsGuildHelperConfig config;
    @Provides
    WarriorsGuildHelperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(WarriorsGuildHelperConfig.class);
    }

    @Inject
    WarriorsGuildHelperScript warriorsGuildHelperScript;
    @Inject
    WarriorsGuildHelperOverlay warriorsGuildHelperOverlay;
    @Inject
    OverlayManager overlayManager;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null)
        {
            overlayManager.add(warriorsGuildHelperOverlay);
        }
        warriorsGuildHelperScript.run(config);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(warriorsGuildHelperOverlay);
        warriorsGuildHelperScript.shutdown();
    }

    // on settings change
    //@Subscribe
    //public void onConfigChanged(final ConfigChanged event) {
    //}

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = (NPC) event.getActor();
        warriorsGuildHelperScript.TryAddNpcToTargetList(npc);
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        NPC npc = (NPC) event.getActor();
        warriorsGuildHelperScript.TryRemoveNpcFromTargetList(npc);
    }
}