package net.runelite.client.plugins.microbot.cardewsPlugins.LineFiremaker;

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
        name = PluginDescriptor.Cardew + "Line Firemaker",
        description = "Cardews Line Firemaker plugin",
        tags = {"firemaking", "microbot", "fm", "firemaker", "line", "fire", "cd", "cardew"},
        enabledByDefault = false
)
@Slf4j
public class LineFiremakerPlugin extends Plugin {
    @Inject
    private LineFiremakerConfig config;
    @Provides
    LineFiremakerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(LineFiremakerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private LineFiremakerOverlay lineFiremakerOverlay;

    @Inject
    LineFiremakerScript lineFiremakerScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(lineFiremakerOverlay);
        }
        lineFiremakerScript.run(config);
    }

    protected void shutDown() {
        lineFiremakerScript.shutdown();
        overlayManager.remove(lineFiremakerOverlay);
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
