package net.runelite.client.plugins.microbot.cardewsPlugins.CardewsAmoxliatlKiller;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GraphicsObject;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Cardew + " Amoxliatl Killer",
        description = "Cardews Automated Amoxliatl Killer",
        tags = {"amoxliatl", "microbot", "boss", "ice", "cd", "cardew", "killer"},
        enabledByDefault = false
)
@Slf4j
public class CardewsAmoxliatlKillerPlugin extends Plugin {

    @Inject
    private CardewsAmoxliatlKillerConfig config;
    @Provides
    CardewsAmoxliatlKillerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CardewsAmoxliatlKillerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private CardewsAmoxliatlKillerOverlay cardewsAmoxliatlKillerOverlay;

    @Inject
    CardewsAmoxliatlKillerScript cardewsAmoxliatlKillerScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(cardewsAmoxliatlKillerOverlay);
        }
        cardewsAmoxliatlKillerScript.run(config);
    }

    protected void shutDown() {
        cardewsAmoxliatlKillerScript.shutdown();
        overlayManager.remove(cardewsAmoxliatlKillerOverlay);
    }

    // Local Coordinates:
    // Each tile has an offset of 128
    // Amoxliatl ice spikes are graphics objects - ID: 2942
    // Amoxliatl Icy pools are game objects - ID: 54279
    // Amoxliatl Ice blocks "Unstable ice" are npcs - ID: 13688

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event)
    {
        if (event == null) return;

        GraphicsObject graphicsObject = event.getGraphicsObject();
        if (graphicsObject == null) return;

        // Amoxliatl ice spikes graphicsObject ID: 2942
        if (graphicsObject.getId() == 2942)
        {
            //Rs2Tile.addDangerousGraphicsObjectTileForInstances(graphicsObject, 2400);
            //Rs2Tile.addDangerousGraphicsObjectTile(graphicsObject, 2400);

        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        if (event == null) return;

        // Amoxliatl Icy pools ID: 54279
        GameObject gameObject = event.getGameObject();
        if (gameObject == null) return;

        if (gameObject.getId() == 54279)
        {
            //Microbot.log("Icy Pool Spawned; Current Login duration: " + Microbot.getLoginTime());
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        if (event == null) return;

        // Amoxliatl Icy pools ID: 54279
        GameObject gameObject = event.getGameObject();
        if (gameObject == null) return;

        if (gameObject.getId() == 54279)
        {
            //Microbot.log("Icy Pool DESPAWNED! Current Login duration: " + Microbot.getLoginTime());
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        if (event == null) return;

        // Amoxliatl Ice blocks "Unstable ice" are npcs - ID: 13688
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        if (event == null) return;

        // Amoxliatl Ice blocks "Unstable ice" are npcs - ID: 13688
    }
}
