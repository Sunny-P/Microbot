package net.runelite.client.plugins.microbot.cardewsPlugins.ToLCreatureCreation;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.example.ExampleConfig;
import net.runelite.client.plugins.microbot.example.ExampleOverlay;
import net.runelite.client.plugins.microbot.example.ExampleScript;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Cardew + "Tower Of Life",
        description = "Automates the creature creation killing.",
        tags = {"Tower of life", "creature", "creature creation", "creation", "cd", "cardew"},
        enabledByDefault = false
)
@Slf4j
public class TowerOfLifeCCPlugin extends Plugin {
    @Inject
    private TowerOfLifeCCConfig config;
    @Provides
    TowerOfLifeCCConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TowerOfLifeCCConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private TowerOfLifeCCOverlay towerOfLifeCCOverlayOverlay;

    @Inject
    TowerOfLifeCCScript towerOfLifeCCScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(towerOfLifeCCOverlayOverlay);
        }
        towerOfLifeCCScript.run(config);
    }

    protected void shutDown() {
        towerOfLifeCCScript.shutdown();
        overlayManager.remove(towerOfLifeCCOverlayOverlay);
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = (NPC) event.getActor();

        towerOfLifeCCScript.TryAddNpcToTargets(npc, config);
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        NPC npc = (NPC) event.getActor();

        towerOfLifeCCScript.RemoveNpcFromTargets(npc);
    }

}
