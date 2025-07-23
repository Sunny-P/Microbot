package net.runelite.client.plugins.microbot.cardewsPlugins.randomEvents;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.globval.enums.Skill;

@ConfigGroup("RandomEvent")
@ConfigInformation("Handled Events: <br>Beekeeper, Count Check, Drill Demon, Genie, Gravedigger, Sandwich lady<br>Also handles xp lamps received.")
public interface RandomEventConfig extends Config {
    @ConfigItem(
            name = "Disable Auto Continue Dialogue",
            keyName = "disableAutoContinue",
            position = 0,
            description = "Disables auto continue dialogue. Only enable if you have conflicts with other plugins such as QoL's Auto continue dialogue feature."
    )
    default boolean disableAutoContinue() { return false; }

    @ConfigItem(
            name = "Skill XP Reward",
            keyName = "skillXPReward",
            position = 1,
            description = "Uses lamps in inventory on chosen skill"
    )
    default Skill skillXPReward() { return Skill.ATTACK; }

    @ConfigSection(
            name = "Dismissed Events",
            description = "Select which Random Events will be dismissed, instead of completed",
            position = 2,
            closedByDefault = true
    )
    String dismissedEventsSection = "Configure which Random Events to dismiss, instead of complete";
    @ConfigItem(
            name = "Beekeeper",
            keyName = "dismissBeekeeper",
            position = 1,
            description = "Dismisses Beekeeper random event",
            section = dismissedEventsSection
    )
    default boolean dismissBeekeeper() {
        return false;
    }
    @ConfigItem(
            name = "Capt' Arnav",
            keyName = "dismissCaptArnav",
            position = 2,
            description = "Dismisses Capt' Arnav random event",
            section = dismissedEventsSection
    )
    default boolean dismissArnav() {
        return true;
    }
    @ConfigItem(
            name = "Certers (Giles, Miles, & Niles)",
            keyName = "dismissCerters",
            position = 3,
            description = "Dismisses Giles, Miles, and Niles Certer random events",
            section = dismissedEventsSection
    )
    default boolean dismissCerters() {
        return true;
    }
    @ConfigItem(
            name = "Count Check",
            keyName = "dismissCountCheck",
            position = 4,
            description = "Dismisses Count Check random event",
            section = dismissedEventsSection
    )
    default boolean dismissCountCheck() {
        return false;
    }
    @ConfigItem(
            name = "Drill Demon",
            keyName = "dismissDrillDemon",
            position = 5,
            description = "Dismisses Drill Demon random event",
            section = dismissedEventsSection
    )
    default boolean dismissDrillDemon() {
        return false;
    }
    @ConfigItem(
            name = "Drunken Dwarf",
            keyName = "dismissDrunkenDwarf",
            position = 6,
            description = "Dismisses Drunken Dwarf random event",
            section = dismissedEventsSection
    )
    default boolean dismissDrunkenDwarf() {
        return true;
    }
    // TODO: Evil Bob has the Prison Pete event as well. See if you can implement both in this toggle
    @ConfigItem(
            name = "Evil Bob",
            keyName = "dismissEvilBob",
            position = 7,
            description = "Dismisses Evil Bob random event",
            section = dismissedEventsSection
    )
    default boolean dismissEvilBob() {
        return true;
    }
    @ConfigItem(
            name = "Evil Bob: Prison Pete",
            keyName = "dismissEvilBobPrison",
            position = 8,
            description = "Dismisses Evil Bob random event that takes you to Prison Pete",
            section = dismissedEventsSection
    )
    default boolean dismissEvilBobPrisonPete() {
        return true;
    }
    @ConfigItem(
            name = "Evil Twin",
            keyName = "dismissEvilTwin",
            position = 9,
            description = "Dismisses Evil Twin random event",
            section = dismissedEventsSection
    )
    default boolean dismissEvilTwin() {
        return true;
    }
    @ConfigItem(
            name = "Freaky Forester",
            keyName = "dismissFreakyForester",
            position = 10,
            description = "Dismisses Freaky Forester random event",
            section = dismissedEventsSection
    )
    default boolean dismissFreakyForester() {
        return true;
    }
    @ConfigItem(
            name = "Genie",
            keyName = "dismissGenie",
            position = 11,
            description = "Dismisses Genie random event",
            section = dismissedEventsSection
    )
    default boolean dismissGenie() {
        return false;
    }
    @ConfigItem(
            name = "Gravedigger",
            keyName = "dismissGravedigger",
            position = 12,
            description = "Dismisses Gravedigger random event",
            section = dismissedEventsSection
    )
    default boolean dismissGravedigger() {
        return false;
    }
    @ConfigItem(
            name = "Jekyll and Hyde",
            keyName = "dismissJekyllAndHyde",
            position = 13,
            description = "Dismisses Jekyll and Hyde random events",
            section = dismissedEventsSection
    )
    default boolean dismissJekyllAndHyde() {
        return true;
    }
    @ConfigItem(
            name = "Kiss the Frog",
            keyName = "dismissKissTheFrog",
            position = 14,
            description = "Dismisses Kiss the Frog random event",
            section = dismissedEventsSection
    )
    default boolean dismissKissTheFrog() {
        return true;
    }
    // TODO: Check if whatever event the Mysterious Old Man is offering can be found out by having to talk to him
    // TODO: Or if we can tell what event it will be without talking to him
    @ConfigItem(
            name = "Mysterious Old Man",
            keyName = "dismissMysteriousOldMan",
            position = 15,
            description = "Dismisses Mysterious Old Man random event",
            section = dismissedEventsSection
    )
    default boolean dismissMysteriousOldMan() {
        return true;
    }
    @ConfigItem(
            name = "Mysterious Old Man: Maze",
            keyName = "dismissMysteriousOldManMaze",
            position = 16,
            description = "Dismisses Mysterious Old Man Maze random event",
            section = dismissedEventsSection
    )
    default boolean dismissMysteriousOldManMaze() {
        return true;
    }
    @ConfigItem(
            name = "Mysterious Old Man: Mime",
            keyName = "dismissMysteriousOldManMime",
            position = 17,
            description = "Dismisses Mysterious Old Man Mime random event",
            section = dismissedEventsSection
    )
    default boolean dismissMysteriousOldManMime() {
        return true;
    }
    @ConfigItem(
            name = "Pillory",
            keyName = "dismissPillory",
            position = 18,
            description = "Dismisses Pillory random event",
            section = dismissedEventsSection
    )
    default boolean dismissPillory() {
        return true;
    }
    @ConfigItem(
            name = "Pinball",
            keyName = "dismissPinball",
            position = 19,
            description = "Dismisses Pinball random events",
            section = dismissedEventsSection
    )
    default boolean dismissPinball() {
        return true;
    }
    @ConfigItem(
            name = "Quiz Master",
            keyName = "dismissQuizMaster",
            position = 20,
            description = "Dismisses Quiz Master random event",
            section = dismissedEventsSection
    )
    default boolean dismissQuizMaster() {
        return true;
    }
    @ConfigItem(
            name = "Rick Turpentine",
            keyName = "dismissRickTurpentine",
            position = 21,
            description = "Dismisses Rick Turpentine random event",
            section = dismissedEventsSection
    )
    default boolean dismissRickTurpentine() {
        return true;
    }
    @ConfigItem(
            name = "Sandwich Lady",
            keyName = "dismissSandwichLady",
            position = 22,
            description = "Dismisses Sandwich Lady random event",
            section = dismissedEventsSection
    )
    default boolean dismissSandwichLady() {
        return false;
    }
    @ConfigItem(
            name = "Strange Plant",
            keyName = "dismissStrangePlant",
            position = 23,
            description = "Dismisses Strange Plant random event",
            section = dismissedEventsSection
    )
    default boolean dismissStrangePlant() {
        return true;
    }
    @ConfigItem(
            name = "Surprise Exam",
            keyName = "dismissSurpriseExam",
            position = 24,
            description = "Dismisses Surprise Exam random event",
            section = dismissedEventsSection
    )
    default boolean dismissSurpriseExam() {
        return true;
    }
}
