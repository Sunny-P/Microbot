package net.runelite.client.plugins.microbot.cardewsPlugins.CardewSlayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Cardew + "Slayer",
        description = "Cardews Slayer Plugin",
        tags = {"cd", "microbot", "slayer", "cardew"},
        enabledByDefault = false
)
@Slf4j
public class CardewSlayerPlugin extends Plugin {
    @Inject
    private CardewSlayerConfig config;
    @Provides
    CardewSlayerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CardewSlayerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private CardewSlayerOverlay cardewSlayerOverlay;

    @Inject
    CardewSlayerScript cardewSlayerScript;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(cardewSlayerOverlay);
        }
        cardewSlayerScript.run(config);
    }

    protected void shutDown() {
        cardewSlayerScript.shutdown();
        overlayManager.remove(cardewSlayerOverlay);
    }

    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }
}
