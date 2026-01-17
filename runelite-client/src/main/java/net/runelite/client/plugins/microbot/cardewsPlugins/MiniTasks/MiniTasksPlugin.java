package net.runelite.client.plugins.microbot.cardewsPlugins.MiniTasks;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Cardew + " Mini Tasks",
        description = "Cardews small task do-er plugin",
        tags = {"tasks", "microbot", "repeatable", "mini", "cd", "cardew"},
        enabledByDefault = false
)
@Slf4j
public class MiniTasksPlugin extends Plugin {

    @Inject
    private MiniTasksConfig config;
    @Provides
    MiniTasksConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MiniTasksConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MiniTasksOverlay miniTasksOverlay;

    @Inject
    MiniTasksScript miniTasksScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(miniTasksOverlay);
        }
        miniTasksScript.run(config);
    }

    protected void shutDown() {
        miniTasksScript.shutdown();
        overlayManager.remove(miniTasksOverlay);
    }
}
