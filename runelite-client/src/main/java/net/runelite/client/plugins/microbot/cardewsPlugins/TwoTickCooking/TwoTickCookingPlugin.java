package net.runelite.client.plugins.microbot.cardewsPlugins.TwoTickCooking;

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
        name = PluginDescriptor.Cardew + " TwoTickCooking",
        description = "Cardew's Automated Two Tick Cooking",
        tags = {"tick", "microbot", "cooking", "two tick", "cd", "cardew"},
        enabledByDefault = false
)
@Slf4j
public class TwoTickCookingPlugin extends Plugin {

    @Inject
    private TwoTickCookingConfig config;
    @Provides
    TwoTickCookingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TwoTickCookingConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private TwoTickCookingOverlay twoTickCookingOverlay;

    @Inject
    TwoTickCookingScript twoTickCookingScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(twoTickCookingOverlay);
        }
        twoTickCookingScript.run(config);
    }

    protected void shutDown() {
        twoTickCookingScript.shutdown();
        overlayManager.remove(twoTickCookingOverlay);
    }
}
