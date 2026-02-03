package net.runelite.client.plugins.microbot.cardewsPlugins.TwoTickCooking;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigInformation;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;

@ConfigGroup("TwoTickCooking")
@ConfigInformation("Use Tempoross Bank/Cooking fire!")
public interface TwoTickCookingConfig extends Config {
    //@ConfigSection(
    //        name = "Use on Object",
    //        description = "Uses inventory object on a game object via specified IDs.",
    //        position = 0,
    //        closedByDefault = true
    //)
    //String useOnObjectSection = "Uses inventory object on a game object via specified IDs.";
//
    @ConfigItem(
            keyName = "Raw Item ID",
            name = "Raw item ID",
            description = "The raw fish ID",
            position = 0
    )
    default  int RawItemID() { return 0; }
}
