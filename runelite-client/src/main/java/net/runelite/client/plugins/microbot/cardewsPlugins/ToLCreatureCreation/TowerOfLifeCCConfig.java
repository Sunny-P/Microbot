package net.runelite.client.plugins.microbot.cardewsPlugins.ToLCreatureCreation;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigInformation;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.microbot.cardewsPlugins.CUtil;

@ConfigGroup("TowerOfLifeCC")
@ConfigInformation("<center>" +
        "Requires:<br>" +
        "Items to summon selected creature stocked in bank.<br>" +
        "Ardougne teleport and cloak are good to have.<br>" +
        "<br>" +
        "It will use your currently equipped gear to kill creatures with.<br>" +
        "Ignores inventory // Start with an empty inventory except your teleport runes/tab<br>" +
        "<br>" +
        "<b>! WARNING !</b><br>" +
        "This is not a particularly dangerous activity, I have not handled safety if you are low level.<br>" +
        "<br>" +
        "--> Start at the bank <--" +
        "</center>")
public interface TowerOfLifeCCConfig extends Config {
    @ConfigItem(
            name = "Ardougne Medium Diary Done",
            keyName = "ardougneMediumDiaryDone",
            description = "Is the Ardougne Medium Diary complete? NOT YET IMPLEMENTED TO DO ANYTHING",
            position = 0
    )
    default boolean ArdougneMediumDiaryDone() { return false; }

    @ConfigItem(
            name = "Selected Creature",
            keyName = "selectedCreature",
            description = "Which creature to create, kill, and loot.",
            position = 1
    )
    default CUtil.ToLCreature SelectedCreature() { return CUtil.ToLCreature.UNICOW; }
}
