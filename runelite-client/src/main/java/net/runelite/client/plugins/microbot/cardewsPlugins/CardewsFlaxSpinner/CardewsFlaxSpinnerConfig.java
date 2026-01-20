package net.runelite.client.plugins.microbot.cardewsPlugins.CardewsFlaxSpinner;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.cardewsPlugins.CardewsFlaxSpinner.enums.SpinningLocation;

@ConfigGroup("CardewsFlaxSpinner")
public interface CardewsFlaxSpinnerConfig extends Config {
    //@ConfigSection(
    //        name = "Use on Object",
    //        description = "Uses inventory object on a game object via specified IDs.",
    //        position = 0,
    //        closedByDefault = true
    //)
    //String useOnObjectSection = "Uses inventory object on a game object via specified IDs.";
//
    @ConfigItem(
            keyName = "spinningLocation",
            name = "Spinning Location",
            description = "Where to spin flax at",
            position = 0
    )
    default SpinningLocation SpinningLocation() { return SpinningLocation.LUMBRIDGE; }

    @ConfigItem(
            keyName = "usingAirStaff",
            name = "Using Air Staff",
            description = "Whether or not you are using an air staff for unlimited air runes",
            position = 1
    )
    default boolean IsUsingAirStaff() { return false; }
}
