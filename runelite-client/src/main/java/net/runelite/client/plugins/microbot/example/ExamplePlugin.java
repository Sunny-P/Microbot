package net.runelite.client.plugins.microbot.example;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.api.tileobject.Rs2TileObjectQueryable;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Example Plugin",
        description = "Performance test for GameObject composition retrieval",
        tags = {"performance", "microbot", "test", "gameobject"},
        enabledByDefault = false
)
@Slf4j
public class ExamplePlugin extends Plugin {
    @Inject
    ExampleScript exampleScript;
    @Inject
    ExampleScriptOverlay exampleScriptOverlay;
    @Inject
    OverlayManager overlayManager;


    @Override
    protected void startUp() throws AWTException {
        overlayManager.add(exampleScriptOverlay);
        exampleScript.run();
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(exampleScriptOverlay);
        exampleScript.shutdown();
    }

    // on settings change
    @Subscribe
    public void onConfigChanged(final ConfigChanged event) {
    }
}