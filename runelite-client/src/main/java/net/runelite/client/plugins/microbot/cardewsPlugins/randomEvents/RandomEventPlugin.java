package net.runelite.client.plugins.microbot.cardewsPlugins.randomEvents;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NpcID;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Cardew + "Random Event",
        description = "Cardews Random Event plugin",
        tags = {"cd", "microbot", "random event", "random", "event", "cardew"},
        enabledByDefault = false
)
@Slf4j
public class RandomEventPlugin extends Plugin {
    @Inject
    private RandomEventConfig config;
    @Provides
    RandomEventConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(RandomEventConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private RandomEventOverlay randomEventOverlay;

    @Inject
    RandomEventScript randomEventScript;

    // Drunken Dwarf: 322
    // WidgetID DIALOG_NPC_TEXT: S 231.6
    // WidgetID DIALOG_CONTINUE (Click here to continue): S 231.5
    // WidgetID DIALOG_NPC_NAME: S 231.4
    // WidgetID DIALOG_OPTIONS - Title Text (Select an Option): D 219.1[0]
    // WidgetID DIALOG_OPTIONS - Option [1 through X]: D 219.1[X]
    // WidgetID DIALOG_OPTIONS - Sword Icons are the last 2 in the array: D 219.1[X+(1 or 2)]

    // Prison Pete Lever ID = 24296
    // Big Horned Balloon Goat ID = 5492
    // Longtail Balloon Animal ID = 5489
    // Tiny Balloon Animal ID = 370
    // Fat Balloon Animal ID = 5493
    // BalloonAnimalWidgetID = 273.3
    // Widget 273.4 ModelID for Longtail = 10750
    //              ModelID for Fat = 10751
    //              ModelID for Tiny = 11028
    // CloseWidgetID = 273.5 | Actions = [Close]

    // Drill Sergeant
    // Sign 4 ID | Loc: 16507 | 3166, 4821
    // Sign 3 ID | Loc: 16478 | 3164, 4821
    // Sign 2 ID | Loc: 16477 | 3162, 4821
    // Sign 1 ID | Loc: 16502 | 3160, 4821
    // Exercise mat 1 ID | Loc: 20810 | 3160, 4820
    // Exercise mat 2 ID | Loc: 16508 | 3162, 4820
    // Exercise mat 3 ID | Loc: 9313 | 3164, 4820
    // Exercise mat 4 ID | Loc: 20801 | 3166, 4820
    // Interact option: "Use"
    // NPC Chat Widget ID: S 231.6 DIALOG_NPC_TEXT Id: 15138822 - Contains text for next exercise to do
    // Exercise to do chat Widget ID: S 193.2 DIALOG_SPRITE_TEXT Id: 12648450 - Also contains text for next exercise to do

    // Bee Keeper
    // Talk to beekeeper, continue once, press 1.
    // Skip chat (4 dialogues)
    // Beehive part 1 ID: S 420.10 | 27525130
    // Beehive part 2 ID: S 420.11 | 27525131
    // Beehive part 3 ID: S 420.12 | 27525132
    // Beehind part 4 ID: S 420.13 | 27525133
    // BeehiveLid       ModelID: 28806
    // BeehiveBody      ModelID: 28428
    // BeehiveEntrance  ModelID: 28803
    // BeehiveLegs      ModelID: 28808
    // Beehive Lid Area ID: S 420.15 | 27525135
    // Beehive Body Area ID: S 420.17 | 27525137
    // Beehive Entrance Area ID: S 420.19 | 27525139
    // Beehive Legs Area ID: S 420.21 | 27525141

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            //overlayManager.add(randomEventOverlay);
        }
        randomEventScript.run(config);
    }

    protected void shutDown() {
        randomEventScript.shutdown();
        //overlayManager.remove(randomEventOverlay);
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
