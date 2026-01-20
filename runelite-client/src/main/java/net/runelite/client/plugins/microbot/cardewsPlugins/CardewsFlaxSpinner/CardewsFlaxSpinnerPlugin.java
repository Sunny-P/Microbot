package net.runelite.client.plugins.microbot.cardewsPlugins.CardewsFlaxSpinner;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Cardew + " Flax Spinner",
        description = "Cardews Automatic Flax Spinning",
        tags = {"flax", "microbot", "spinning wheel", "crafting", "cd", "cardew", "bow string"},
        enabledByDefault = false
)
@Slf4j
public class CardewsFlaxSpinnerPlugin extends Plugin {

    @Inject
    private CardewsFlaxSpinnerConfig config;
    @Provides
    CardewsFlaxSpinnerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CardewsFlaxSpinnerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private CardewsFlaxSpinnerOverlay cardewsFlaxSpinnerOverlay;

    @Inject
    CardewsFlaxSpinnerScript cardewsFlaxSpinnerScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(cardewsFlaxSpinnerOverlay);
        }
        cardewsFlaxSpinnerScript.run(config);
    }

    protected void shutDown() {
        cardewsFlaxSpinnerScript.shutdown();
        overlayManager.remove(cardewsFlaxSpinnerOverlay);
    }
}
