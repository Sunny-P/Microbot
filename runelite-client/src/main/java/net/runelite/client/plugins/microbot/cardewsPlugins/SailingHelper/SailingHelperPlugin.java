package net.runelite.client.plugins.microbot.cardewsPlugins.SailingHelper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.api.boat.Rs2BoatCache;
import net.runelite.client.plugins.microbot.api.boat.models.Rs2BoatModel;
//import net.runelite.client.plugins.microbot.util.sailing.Rs2Sailing;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Cardew + " Sailing Helper",
        description = "Cardew's helper for Sailing'",
        tags = {"helper", "microbot", "sailing", "cd", "cardew"},
        enabledByDefault = false
)
@Slf4j
public class SailingHelperPlugin extends Plugin {

    @Inject
    private SailingHelperConfig config;
    @Provides
    SailingHelperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SailingHelperConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private SailingHelperOverlay sailingHelperOverlay;

    @Inject
    SailingHelperScript sailingHelperScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(sailingHelperOverlay);
        }
        sailingHelperScript.run(config);
    }

    protected void shutDown() {
        sailingHelperScript.shutdown();
        overlayManager.remove(sailingHelperOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM/* && event.getType()!= ChatMessageType.ENGINE*/)
        {
            return;
        }

        String chatMsg = Text.removeTags(event.getMessage()); //remove color and linebreaks

        if (chatMsg.contains("gust of wind"))
        {
            // Microbot.log("We have received a gust of wind!");
            if (config.ShouldAutoTrimSails())
            {
                //Microbot.log("Attempting to trim sails!");
                //Rs2Sailing.trimSails();
            }
        }
        //if (chatMsg.contains("wind dies down"))
        //{
        //    Microbot.log("The wind has died down. No longer can Trim Sails!");
        //    SailingHelperScript.receivedGustOfWind.set(false);
        //}
    }
}
