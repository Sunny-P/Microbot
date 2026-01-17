package net.runelite.client.plugins.microbot.cardewsPlugins.CrabTrapping;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Cardew + " Crab Trapping",
        description = "Cardew's solution for hunting crabs'",
        tags = {"hunter", "microbot", "crabs", "trapping", "cd", "cardew"},
        enabledByDefault = false
)
@Slf4j
public class CrabTrappingPlugin extends Plugin {

    @Inject
    private CrabTrappingConfig config;
    @Provides
    CrabTrappingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CrabTrappingConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private CrabTrappingOverlay crabTrappingOverlay;

    @Inject
    CrabTrappingScript crabTrappingScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(crabTrappingOverlay);
        }
        crabTrappingScript.run(config);
    }

    protected void shutDown() {
        crabTrappingScript.shutdown();
        overlayManager.remove(crabTrappingOverlay);
    }
}
