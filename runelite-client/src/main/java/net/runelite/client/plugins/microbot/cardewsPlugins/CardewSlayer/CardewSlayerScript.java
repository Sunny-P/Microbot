package net.runelite.client.plugins.microbot.cardewsPlugins.CardewSlayer;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.cardewsPlugins.CUtil;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.Rs2InventorySetup;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.slayer.Rs2Slayer;
import net.runelite.client.plugins.microbot.util.slayer.enums.SlayerMaster;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import org.slf4j.event.Level;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CardewSlayerScript extends Script {
    public enum States{
        MOVING_TO_SLAYER_MASTER,
        GETTING_SLAYER_TASK,
        RECEIVED_TASK_RESOLVE_STATE,
        MOVING_TO_MONSTER_LOCATION,
        SLAYING_MONSTER,
        MOVING_TO_NEAREST_BANK,
        BANKING
    }
    @Getter
    static States currentState = States.RECEIVED_TASK_RESOLVE_STATE;

    @Getter
    enum Seedbox{
        OPEN(24482),
        CLOSED(13639);

        private final int id;

        Seedbox(int _id)
        {
            this.id = _id;
        }
    }

    SlayerMaster currentMaster = SlayerMaster.NONE;

    @Getter
    static int killsLeft = 0;
    String targetMonsterName = "";
    static CUtil.SlayerTarget slayerTarget = CUtil.SlayerTarget.NONE;

    private List<Rs2NpcModel> targetList = new CopyOnWriteArrayList<>();
    List<Rs2NpcModel> npcsInteractingWithPlayer = new CopyOnWriteArrayList<>();
    float timeSinceLastListUpdate = 0;
    double listRepopulateDelay = 5;

    boolean slayerGemChecked = false;
    boolean lootBanked = false;
    boolean lightingLightSource = false;
    boolean hasRequiredItem = false;
    boolean wallBeastAppeared = false;
    @Setter
    boolean tryForceWalkToMonsterLocation = false;
    int turaelSkipProgress = -1;
    boolean isDeterminingState = false;
    boolean justLooted = false;

    float timeNotInCombat = 0;
    boolean falseWestTrueEast = false;
    boolean chosenEastOrWest = false;

    boolean currentlyFlickPrayer = false;
    Rs2PrayerEnum protectionPrayer = Rs2PrayerEnum.PROTECT_MELEE;

    Function<Rs2NpcModel, WorldPoint> locationFunc = Rs2NpcModel::getWorldLocation;

    public boolean run(CardewSlayerConfig config) {
        Microbot.enableAutoRunOn = false;
        CUtil.SetMyAntiban(0.0, 2, 15, 0.4);
        Rs2Antiban.setActivity(Activity.GENERAL_SLAYER);

        slayerGemChecked = false;
        lootBanked = false;
        lightingLightSource = false;
        hasRequiredItem = false;
        wallBeastAppeared = false;
        timeNotInCombat = 0;
        falseWestTrueEast = false;
        chosenEastOrWest = false;
        currentlyFlickPrayer = false;
        isDeterminingState = false;
        justLooted = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.pauseAllScripts.get()) return;
                if (Microbot.handlingRandomEvent) return;
                long startTime = System.currentTimeMillis();

                // If targetMonsterName equals birds
                // Then we go into a switch statement based on an enum case
                // New enum needed which will hold Alternate bird tasks
                // Let the player pick alternative task in config
                // Then set our task to that

                // Set Slayer master based on user plugin field text.
                // Will log/print if there is no master selected, so they know to correct their spelling if need be
                // This will not interrupt other parts of the script
                if (currentMaster == SlayerMaster.NONE) {
                    currentMaster = config.SlayerMaster();
                }
                // Perhaps provide a statement forcing the player to use an emergency teleport
                // When they drop below a health threshold?
                // Bot Ring of Life lol; but higher threshold.
                double playerHealthPercent = (double) Rs2Player.getBoostedSkillLevel(Skill.HITPOINTS) / Rs2Player.getRealSkillLevel(Skill.HITPOINTS);
                if (playerHealthPercent < config.EatFoodPercent()) {
                    if (!Rs2Player.eatAt(config.EatFoodPercent())) {
                        // If FALSE then we didn't eat
                        // Did we not eat because we don't have anymore food?
                        // If so, we need to save ourselves
                        if (Rs2Inventory.getInventoryFood().isEmpty()) {
                            // Handle emergency teleport/transitioning to banking state
                            if (currentState == States.SLAYING_MONSTER || currentState == States.MOVING_TO_MONSTER_LOCATION) {
                                Microbot.log("Health percent: " + playerHealthPercent);
                                // If our health drops below a danger threshold?
                                if (playerHealthPercent < 0.5) {
                                    // If we are below half health
                                    currentState = States.MOVING_TO_NEAREST_BANK;
                                }
                            }
                        }
                    }
                }
                if (Rs2AntibanSettings.microBreakActive) return;

                if (!Rs2Inventory.isFull())
                {
                    // Loot
                    HandleLooting(config);
                }
                if (config.PickupAndBuryBones())
                {
                    if (!justLooted)
                    {
                        BuryBones();
                    }
                    else
                    {
                        justLooted = false;
                    }

                }
                // Add a delay before continuing here. Delay set when TaskComplete.

                if (tryForceWalkToMonsterLocation)
                {
                    //Microbot.log("FORCE WALKING CLOSELY TO MONSTER LOCATION?");
                    WalkToMonsterLocationDirect();
                    return;
                }

                HandleStateMachine(config);

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;

                float deltaTime = (float) (totalTime * 0.001);

                if (currentState == States.SLAYING_MONSTER)
                {
                    timeSinceLastListUpdate += deltaTime;

                    if (!Rs2Player.isInCombat())
                    {
                        timeNotInCombat += deltaTime; // Track timeNotInCombat as seconds.
                        Microbot.log("Time Not In Combat: " + timeNotInCombat);

                        if (timeNotInCombat > 60)   // We have been out of combat for a minute. Walk to specific location?
                        {
                            Rs2Walker.walkTo(slayerTarget.getLocation());
                        }
                        else if (timeNotInCombat > 30)   // OUT OF COMBAT FOR HALF A MINUTE
                        {
                            if (slayerTarget != CUtil.SlayerTarget.NONE && slayerTarget.getLocation() != null && Rs2Player.getWorldLocation() != null)
                            {
                                // If we are nearby the initial slayer location
                                if (Rs2WorldPoint.quickDistance(Rs2Player.getWorldLocation(), slayerTarget.getLocation()) < 8)
                                {
                                    // Run elsewhere
                                    if (!chosenEastOrWest)
                                    {
                                        falseWestTrueEast = ThreadLocalRandom.current().nextBoolean();
                                        chosenEastOrWest = true;
                                    }
                                    else
                                    {
                                        WorldPoint runToLocation = falseWestTrueEast
                                                ? new WorldPoint(slayerTarget.getLocation().getX() + 20, slayerTarget.getLocation().getY(), slayerTarget.getLocation().getPlane())
                                                : new WorldPoint(slayerTarget.getLocation().getX() - 20, slayerTarget.getLocation().getY(), slayerTarget.getLocation().getPlane());

                                        if (Rs2Walker.canReach(runToLocation))
                                        {
                                            Rs2Walker.walkTo(runToLocation);
                                        }
                                    }
                                }
                                else    // We are not right nearby the initial slayer location
                                {
                                    Rs2Walker.walkTo(slayerTarget.getLocation());
                                }
                            }
                        }
                    }
                    else
                    {
                        timeNotInCombat = 0;
                        chosenEastOrWest = false;
                    }
                }

            } catch (Exception ex) {
                System.out.println("Error in CardewSlayerScript " + Arrays.toString(ex.getStackTrace()));
            }
        }, 0, 300, TimeUnit.MILLISECONDS);
        return true;
    }

    void HandleStateMachine(CardewSlayerConfig _config)
    {
        switch(currentState)
        {
            case MOVING_TO_SLAYER_MASTER:
                // Before we try go to a master
                // Check inventory for an enchanted gem and check if we have a slayer task.
                if (!slayerGemChecked)
                {
                    if (Rs2Inventory.hasItem("Enchanted gem"))
                    {
                        Rs2Inventory.interact("Enchanted gem", "Check");
                        // The state will automatically change to MOVING_TO_SLAYER_MONSTER if the game message from the gem is parsed to update our task
                        slayerGemChecked = true;
                        sleepGaussian(1500, 600);
                        break;
                    }
                }

                if (isDeterminingState) break;

                CheckForAndOpenSeedbox();

                if (slayerTarget != CUtil.SlayerTarget.NONE && killsLeft > 0)
                {
                    // We managed to get into this iteration of MOVING_TO_SLAYER_MASTER code
                    // with a recognised task, because we used the gem to check.
                    // Let it resolve it's state in DetermineStateFromSlayerTask() called from UpdateSlayerTaskFromGemText()
                    break;
                }

                if (currentMaster == SlayerMaster.NONE) {
                    Microbot.log("CardewSlayer: No Slayer Master!");
                    break;
                }
                WorldPoint cornerNW_MTSM = new WorldPoint(currentMaster.getWorldPoint().getX() - 8, currentMaster.getWorldPoint().getY() + 8, currentMaster.getWorldPoint().getPlane());
                WorldPoint cornerSE_MTSM = new WorldPoint(currentMaster.getWorldPoint().getX() + 8, currentMaster.getWorldPoint().getY() - 8, currentMaster.getWorldPoint().getPlane());

                if (!Rs2Walker.isInArea(cornerSE_MTSM, cornerNW_MTSM)) {
                    Rs2Walker.walkWithBankedTransports(currentMaster.getWorldPoint());
                }
                else {
                    // We are near the master
                    Microbot.log("We are near the master");
                    currentState = States.GETTING_SLAYER_TASK;
                }
                break;

            case GETTING_SLAYER_TASK:
                if (!Objects.equals(targetMonsterName, ""))
                {
                    Microbot.log("targetMonsterName: " + targetMonsterName);
                    DetermineStateFromSlayerTask(_config);
                    break;
                }
                // Assuming we have no task
                // Check if we have chat text widget active
                //if (Rs2Widget.isWidgetVisible(231, 6)) {
                //    // Get the text as a string
                //    String chatText = Rs2Widget.getChildWidgetText(231, 6);
                //    // Get number of kills
                //    // Get name of task
                //    Pattern pattern = Pattern.compile("kill (\\d+) (.+)", Pattern.CASE_INSENSITIVE);
                //    Matcher matcher = pattern.matcher(chatText);
//
                //    if (matcher.find()) {
                //        // Gained killsLeft and monster data with this pattern method. I.E new task
                //        killsLeft = Integer.parseInt(matcher.group(1));
                //        targetMonsterName = matcher.group(2).trim();
//
                //        Microbot.log("Assigned: " + killsLeft + " " + targetMonsterName + "!");
//
                //        if (Rs2Dialogue.hasContinue())
                //        {
                //            Rs2Dialogue.clickContinue();
                //        }
//
                //        DetermineStateFromSlayerTask(_config);
                //    }
                //    else {
                //        // Get killsLeft and monster info with second pattern. I.E already on task.
                //        Pattern secondPattern = Pattern.compile("hunting (.+?), you have (\\d+) to go", Pattern.CASE_INSENSITIVE);
                //        Matcher secondMatcher = secondPattern.matcher(chatText);
//
                //        if (secondMatcher.find()) {
                //            targetMonsterName = secondMatcher.group(1).trim();
                //            killsLeft = Integer.parseInt(secondMatcher.group(2));
//
                //            Microbot.log("Assigned: " + killsLeft + " " + targetMonsterName + "!");
//
                //            if (Rs2Dialogue.hasContinue())
                //            {
                //                Rs2Dialogue.clickContinue();
                //            }
//
                //            DetermineStateFromSlayerTask(_config);
                //        }
                //        else {
                //            Microbot.log("CardewSlayer: Couldn't parse slayer target information", Level.ERROR);
                //            this.shutdown();
                //        }
                //    }
                //}
                if (Rs2Slayer.hasSlayerTask())
                {
                    //if (Rs2Dialogue.hasContinue())
                    //{
                    //    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    //}
                    targetMonsterName = Rs2Slayer.getSlayerTask();
                    targetMonsterName = CUtil.SingularisePluralName(targetMonsterName);
                    killsLeft = Rs2Slayer.getSlayerTaskSize();
                    Microbot.log("Slayer target: " + targetMonsterName);

                    currentState = States.RECEIVED_TASK_RESOLVE_STATE;
                    break;
                }
                else
                {
                    if (currentMaster == SlayerMaster.NONE) {
                        Microbot.log("CardewSlayer: No Slayer Master!");
                        break;
                    }
                    // Get task from master
                    Rs2Npc.interact(currentMaster.getName(), "Assignment");

                    Rs2Dialogue.sleepUntilInDialogue();
                }
                break;

            case RECEIVED_TASK_RESOLVE_STATE:
                if (!slayerGemChecked)
                {
                    if (Rs2Inventory.hasItem("Enchanted gem"))
                    {
                        Rs2Inventory.interact("Enchanted gem", "Check");
                        // The state will automatically change to MOVING_TO_SLAYER_MONSTER if the game message from the gem is parsed to update our task
                        slayerGemChecked = true;
                        sleepGaussian(1500, 600);
                        break;
                    }
                }
                if (!isDeterminingState)
                {
                    isDeterminingState = true;
                    DetermineStateFromSlayerTask(_config);
                }
                break;

            case MOVING_TO_MONSTER_LOCATION:
                if (Rs2Dialogue.hasContinue())
                {
                    Rs2Dialogue.clickContinue();
                }

                CheckForAndOpenSeedbox();


                WorldPoint cornerNW_MTML = new WorldPoint(slayerTarget.getLocation().getX() - 10, slayerTarget.getLocation().getY() + 10, slayerTarget.getLocation().getPlane());
                WorldPoint cornerSE_MTML = new WorldPoint(slayerTarget.getLocation().getX() + 10, slayerTarget.getLocation().getY() - 10, slayerTarget.getLocation().getPlane());
                if (!Rs2Walker.isInArea(cornerSE_MTML, cornerNW_MTML)) {

                    Rs2Walker.walkWithBankedTransports(slayerTarget.getLocation());
                }
                else {
                    // We have reached our destination

                    currentState = States.SLAYING_MONSTER;
                }
                break;

            case SLAYING_MONSTER:
                if (Rs2Inventory.isFull())
                {
                    // Check inventory for food
                    if (!Rs2Inventory.getInventoryFood().isEmpty())
                    {
                        // Eat some food to clear space for loot
                        Rs2Player.eatAt(101);
                    }
                }
                if (npcsInteractingWithPlayer.isEmpty() || timeSinceLastListUpdate >= listRepopulateDelay)
                {
                    npcsInteractingWithPlayer = Rs2Npc.getNpcsForPlayer()
                            .filter(npc -> npc.getComposition() != null &&
                                    Arrays.stream(npc.getComposition().getActions())
                                            .anyMatch(action -> action != null && action.toLowerCase().contains("attack")))
                            .collect(Collectors.toList());  // Filter to make sure we only care about attackable npcs.
                }

                if (!Rs2Combat.inCombat())
                {
                    //Microbot.log("We are NOT in combat.");
                    currentlyFlickPrayer = false;
                    // Not in combat. Pick a fight.
                    // Check monsters that are already fighting the player
                    if (!npcsInteractingWithPlayer.isEmpty())
                    {
                        //Microbot.log("NPCs interacting with us list has objects");
                        Rs2NpcModel target = npcsInteractingWithPlayer.stream()
                                // Try and get an npc that matches our task if multiple things are interacting with us
                                .filter(npc -> npc.getWorldLocation() != null && Rs2Walker.canReach(npc.getWorldLocation())
                                        && npc.getName() != null && npc.getName().toLowerCase().contains(slayerTarget.getMonsterData().getMonster()))
                                .min(Comparator.comparingInt(npc -> npc.getWorldLocation().distanceTo(Rs2Player.getWorldLocation())))
                                // If it can't find that, fallback to a regular return of the list that we can reach
                                .orElse(npcsInteractingWithPlayer.stream()
                                        .filter(npc -> npc.getWorldLocation() != null && Rs2Walker.canReach(npc.getWorldLocation()))
                                        .min(Comparator.comparingInt(npc -> npc.getWorldLocation().distanceTo(Rs2Player.getWorldLocation())))
                                        .orElse(null));
                        assert target != null;

                        Microbot.log("Target: " + target);

                        if (target.getLocalLocation() != null)
                        {
                            if (!Rs2Camera.isTileOnScreen(target.getLocalLocation()))
                            {
                                Rs2Camera.turnTo(target);
                            }
                        }
                        if (!target.isDead())
                        {
                            Rs2Npc.interact(target, "Attack");
                        }
                        //Rs2Antiban.actionCooldown();
                    }
                    else
                    {
                        //Microbot.log("NPCs interacting with us list is empty");
                        // We don't have any monsters currently in combat with us
                        // Pick a fresh target
                        switch (slayerTarget)
                        {
                            case KALPHITE:
                                //Microbot.log("About to populate our target list..");
                                PopulateTargetList(_config.AlternativeKalphiteTask().getId());
                                break;
                            case MOGRE:
                                // IF WE ARE KILLING MOGRES & WE DON'T ALREADY HAVE A MOGRE ATTACKING US
                                // LURE A NEW MOGRE
                                GameObject ominousFishingSpot = Rs2GameObject.getGameObject("Ominous Fishing Spot");
                                if (ominousFishingSpot != null)
                                {
                                    Rs2Inventory.useItemOnObject(ItemID.FISHING_EXPLOSIVE, ominousFishingSpot.getId());
                                    // Probably ideally wait for our throwing animation
                                    // Until we can fight it, wait for inv change?
                                    Rs2Inventory.waitForInventoryChanges(5000);
                                }
                                break;
                            case SHADES:
                                PopulateTargetList(_config.AlternativeShadeTask().getMonsterName().toLowerCase());
                                break;
                            case WALL_BEAST:
                                WorldPoint wallBeastLocation1, wallBeastLocation2;
                                wallBeastLocation1 = new WorldPoint(3198, 9553, 0);
                                wallBeastLocation2 = new WorldPoint(3215, 9559, 0);

                                // If we don't currently have a wall beast
                                if (!wallBeastAppeared)
                                {
                                    // If we are closer to wallBeastLocation1
                                    if (Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), wallBeastLocation1) < Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), wallBeastLocation2))
                                    {
                                        // Run to spot 2 and try trigger wall beast
                                        Rs2Walker.walkTo(wallBeastLocation2, 1);
                                        Rs2Player.waitForAnimation(2500);
                                    }
                                    else
                                    {
                                        // Try and trigger the spot 1 wall beast
                                        Rs2Walker.walkTo(wallBeastLocation1, 1);
                                        // Wait for animation to complete or 2 seconds
                                        // We have either waited for 2 seconds or for the length of the wall beast grabbing us animation
                                        Rs2Player.waitForAnimation(2500);
                                    }
                                }
                                else
                                {
                                    //targetList = Rs2Npc.getAttackableNpcs()
                                    //        .filter(npc -> npc != null
                                    //                && (npc.getInteracting() == null || npc.getInteracting() == Microbot.getClient().getLocalPlayer())
                                    //                && npc.getWorldLocation() != null && Rs2Walker.canReach(npc.getWorldLocation())
                                    //                && npc.getName() != null && npc.getName().toLowerCase().contains(slayerTarget.getMonsterData().getMonster().toLowerCase()))
                                    //        .collect(Collectors.toList());
                                    PopulateTargetList(slayerTarget.getMonsterData().getMonster().toLowerCase());
                                }
                                break;
                            case WOLF:
                                PopulateTargetList(_config.AlternativeWolfTask().getMonsterName().toLowerCase());
                                break;
                            default:
                                // Cases when we should populate our target list
                                PopulateTargetList(slayerTarget.getMonsterData().getMonster().toLowerCase());
                                break;
                        }
                        //Microbot.log("TargetList: " + targetList);

                        if (!targetList.isEmpty())
                        {
                            Rs2NpcModel target = targetList.stream()
                                    .filter(npc -> npc.getComposition() != null && Arrays.stream(npc.getComposition()
                                            .getActions()).anyMatch(action -> action != null && action.toLowerCase().contains("attack")))
                                    .min(Comparator.comparingInt(npc -> npc.getWorldLocation().distanceTo(Rs2Player.getWorldLocation())))
                                    .orElse(null);

                            assert target != null;
                            if (target.getLocalLocation() != null)
                            {
                                // If monster is not in camera, turn to it
                                if (!Rs2Camera.isTileOnScreen(target.getLocalLocation()))
                                {
                                    Rs2Camera.turnTo(target);
                                }
                            }

                            if (!target.isDead())
                            {
                                Rs2Npc.interact(target, "Attack");
                            }
                            //Rs2Antiban.actionCooldown();
                        }
                    }
                }
                else
                {
                    //Microbot.log("We are in combat.");
                    Rs2NpcModel inCombatNpc;
                    // We are in combat
                    if (!npcsInteractingWithPlayer.isEmpty())
                    {
                        if (_config.EnablePrayerFlicking())
                        {
                            inCombatNpc = npcsInteractingWithPlayer.stream()
                                    .filter(npc -> npc != null
                                            && Microbot.getClient() != null && Microbot.getClient().getLocalPlayer() != null
                                            && (npc.getInteracting() == null || npc.getInteracting() == Microbot.getClient().getLocalPlayer())
                                            && npc.getWorldLocation() != null && Rs2Walker.canReach(npc.getWorldLocation()))
                                    .findFirst().orElse(null);

                            if (inCombatNpc != null)
                            {
                                String enemyAttackStyle = Rs2NpcManager.attackStyleMap.get(inCombatNpc.getId());
                                // Assuming for MELEE: returned values will be: "Crush" "Stab" "Slash"
                                Microbot.log("InCombatNpc: " + inCombatNpc.getName() + " " + enemyAttackStyle);
                                String[] meleeStyles = new String[]{"crush","stab","slash","Melee, Magical ranged"};
                                for (String style : meleeStyles)
                                {
                                    if (style.toLowerCase().contains(enemyAttackStyle.toLowerCase()))
                                    {
                                        protectionPrayer = Rs2PrayerEnum.PROTECT_MELEE;
                                        break;
                                    }
                                }

                                currentlyFlickPrayer = true;
                            }
                            else
                            {
                                currentlyFlickPrayer = false;
                            }
                        }

                        // Determine if this is something we need to use a slayer item on to kill
                        switch (slayerTarget)
                        {
                            case LIZARD:
                                // IF WE DONT HAVE AUTO LIZARD ICE COOLER SLAYER PERK
                                inCombatNpc = npcsInteractingWithPlayer.stream()
                                        .filter(npc -> npc != null
                                                && Microbot.getClient() != null && Microbot.getClient().getLocalPlayer() != null
                                                && (npc.getInteracting() == null || npc.getInteracting() == Microbot.getClient().getLocalPlayer())
                                                && npc.getWorldLocation() != null && Rs2Walker.canReach(npc.getWorldLocation())
                                                && npc.getName() != null && npc.getName().toLowerCase().contains("lizard"))
                                        .findFirst().orElse(null);
                                assert inCombatNpc != null;
                                switch (inCombatNpc.getId())
                                {
                                    case NpcID.SLAYER_LIZARD_SMALL2_SANDY:
                                    case NpcID.SLAYER_LIZARD_SMALL1_GREEN:
                                    case NpcID.SLAYER_LIZARD_LARGE1_GREEN:
                                    case NpcID.SLAYER_LIZARD_MASSIVE:
                                    case NpcID.SLAYER_LIZARD_LARGE2_SANDY:
                                    case NpcID.SLAYER_LIZARD_LARGE3_SANDY:
                                        if (Rs2Inventory.contains(ItemID.SLAYER_ICY_WATER))
                                        {
                                            if (inCombatNpc.getHealthRatio() < 4)
                                            {
                                                Rs2Inventory.useItemOnNpc(ItemID.SLAYER_ICY_WATER, inCombatNpc);
                                                Rs2Player.waitForXpDrop(Skill.SLAYER, 5000);
                                            }
                                        }
                                        else
                                        {
                                            currentState = States.MOVING_TO_NEAREST_BANK;
                                            return;
                                        }
                                        break;
                                }
                                break;
                            case ROCKSLUG:
                                // IF WE DONT HAVE AUTO SALTER SLAYER PERK
                                inCombatNpc = npcsInteractingWithPlayer.stream()
                                        .filter(npc -> npc != null
                                                && Microbot.getClient() != null && Microbot.getClient().getLocalPlayer() != null
                                                && (npc.getInteracting() == null || npc.getInteracting() == Microbot.getClient().getLocalPlayer())
                                                && npc.getWorldLocation() != null && Rs2Walker.canReach(npc.getWorldLocation())
                                                && npc.getName() != null && npc.getName().toLowerCase().contains("rockslug"))
                                        .findFirst().orElse(null);
                                assert inCombatNpc != null;
                                if (Rs2Inventory.contains(ItemID.SLAYER_BAG_OF_SALT))
                                {
                                    if (inCombatNpc.getHealthRatio() < 4)
                                    {
                                        Rs2Inventory.useItemOnNpc(ItemID.SLAYER_BAG_OF_SALT, inCombatNpc);
                                        Rs2Player.waitForXpDrop(Skill.SLAYER, 5000);
                                    }
                                }
                                else
                                {
                                    currentState = States.MOVING_TO_NEAREST_BANK;
                                    return;
                                }
                                break;
                        }
                    }
                }
                break;

            case MOVING_TO_NEAREST_BANK:
                if (Rs2Walker.walkTo(Rs2Bank.getNearestBank().getWorldPoint()))
                {
                    currentState = States.BANKING;
                    Rs2Antiban.takeMicroBreakByChance();
                }
                break;

            case BANKING:
                if (!Rs2Bank.isOpen() && !lightingLightSource)
                {
                    Rs2Bank.openBank();
                }
                else
                {
                    if (!lootBanked)
                    {
                        if (_config.InventorySetup() != null && !hasRequiredItem)
                        {
                            Rs2InventorySetup setup = new Rs2InventorySetup(_config.InventorySetup(), mainScheduledFuture);
                            setup.loadEquipment();
                            setup.loadInventory();
                            lootBanked = true;
                        }
                        //else if (_config.InventorySetup() != null)
                        //{
                        //    Rs2InventorySetup setup = new Rs2InventorySetup(_config.InventorySetup(), mainScheduledFuture);
                        //    setup.loadInventory();
                        //}
                        else if (!hasRequiredItem)
                        {
                            // No inventory setup loaded
                            Rs2Bank.depositAllExcept("Seed box", "Open seed box", "Enchanted gem", "Herb sack", "Open herb sack");
                            lootBanked = true;
                        }
                        Rs2Inventory.interact(Seedbox.OPEN.getId(), "Empty");

                        Microbot.log("Banked loot.");

                        if (killsLeft <= 0)
                        {
                            currentState = States.MOVING_TO_SLAYER_MASTER;
                            lootBanked = false;
                            hasRequiredItem = false;
                            return;
                            //break;
                        }
                    }

                    // We are regearing/banking loot for a current task
                    if (killsLeft > 0)
                    {
                        // If for some reason our task is NONE
                        if (slayerTarget == CUtil.SlayerTarget.NONE)
                        {
                            if (Rs2Inventory.hasItem(ItemID.SLAYER_GEM))
                            {
                                Rs2Inventory.interact(ItemID.SLAYER_GEM, "check");
                                sleepUntil(() -> slayerTarget != CUtil.SlayerTarget.NONE, 5000);
                            }
                        }

                        // Check if our task has any requirements in the first place
                        if (slayerTarget.getMonsterData() != null)
                        {
                            List<String> requiredItemsList = Arrays.stream(slayerTarget.getMonsterData().getItemsRequired()).collect(Collectors.toList());
                            if (!requiredItemsList.get(0).equalsIgnoreCase("none") && !hasRequiredItem)
                            {
                                boolean requireSlayerHelm = true;
                                boolean requireVsShield = true;

                                for (String requiredItem : slayerTarget.getMonsterData().getItemsRequired())
                                {
                                    Microbot.log("Required item: " + requiredItem + ". Attempting to retrieve.");
                                    if (Rs2Bank.hasItem(requiredItem))
                                    {
                                        // If our required item is a Slayer helm component
                                        // Only withdraw these if we don't have a slayer helm
                                        if ((requiredItem.toLowerCase().contains("earmuffs") || requiredItem.toLowerCase().contains("facemask")
                                                || requiredItem.toLowerCase().contains("nose peg") || requiredItem.toLowerCase().contains("spiny helmet"))
                                                && !Rs2Bank.hasItem(ItemID.SLAYER_HELM))
                                        {
                                            requireSlayerHelm = false;

                                            // If for some reason we are already wearing the required item
                                            // Withdraw our item and continue to the next loop iteration
                                            IsNotWearingItemThenWithdraw(requiredItem, _config);
                                            continue;
                                        }
                                        else if (CUtil.DoNamesMatch(requiredItem, "Mirror Shield"))
                                        {
                                            requireVsShield = false;

                                            IsNotWearingItemThenWithdraw(requiredItem, _config);
                                            continue;
                                        }
                                        else if (requiredItem.toLowerCase().contains("ice cooler"))
                                        {
                                            Rs2Bank.withdrawX(requiredItem, killsLeft, false);
                                            Rs2Inventory.waitForInventoryChanges(5000);
                                            Rs2Bank.withdrawOne(ItemID.SHANTAY_PASS);
                                            Rs2Inventory.waitForInventoryChanges(5000);
                                            Rs2Bank.withdrawX(ItemID.WATER_SKIN4, 2);
                                            Rs2Inventory.waitForInventoryChanges(5000);

                                            hasRequiredItem = true;
                                            continue;
                                        }
                                        else if (requiredItem.toLowerCase().contains("bag of salt"))
                                        {
                                            Rs2Bank.withdrawX(requiredItem, killsLeft, false);
                                            Rs2Inventory.waitForInventoryChanges(5000);

                                            hasRequiredItem = true;
                                            continue;
                                        }
                                        // Withdrawn regular non-case required item
                                        // If we are already wearing the required item no need to withdraw
                                        IsNotWearingItemThenWithdraw(requiredItem, _config);
                                    }
                                    // LOOK FOR A BULLSEYE HERE, BECAUSE requiredItem STRING WILL NEVER MATCH THE ACTUAL NAME ANYMORE
                                    else if (requiredItem.toLowerCase().contains("bullseye lantern"))
                                    {
                                        if (Rs2Bank.hasItem(ItemID.BULLSEYE_LANTERN_LIT))
                                        {
                                            Rs2Bank.withdrawOne(ItemID.BULLSEYE_LANTERN_LIT);
                                            hasRequiredItem = true;
                                        }
                                        else if (Rs2Bank.hasItem(ItemID.BULLSEYE_LANTERN_UNLIT))
                                        {
                                            lightingLightSource = true;
                                            if (Rs2Bank.withdrawOne(ItemID.BULLSEYE_LANTERN_UNLIT))
                                            {
                                                Rs2Bank.withdrawOne(ItemID.TINDERBOX);

                                                Rs2Bank.closeBank();
                                                Global.sleepUntil(() -> !Rs2Bank.isOpen());

                                                Rs2Inventory.use(ItemID.TINDERBOX);
                                                Rs2Inventory.use(ItemID.BULLSEYE_LANTERN_UNLIT);

                                                Rs2Bank.openBank();
                                                Global.sleepUntil(Rs2Bank::isOpen);

                                                Rs2Bank.depositItems(ItemID.TINDERBOX);
                                                lightingLightSource = false;

                                                hasRequiredItem = true;
                                            }
                                        }
                                    }
                                    else    // WE DO NOT HAVE THE REQUIRED ITEM IN THE BANK
                                    {
                                        // If required item is a slayer helmet
                                        if (requiredItem.toLowerCase().contains("slayer helm"))
                                        {
                                            // If we still need it, shutdown.
                                            // This is changed to false if we have withdrawn something like a facemask/nose peg/etc instead of a helm.
                                            if (requireSlayerHelm)
                                            {
                                                Microbot.log("Missing required item: " + requiredItem);
                                                this.shutdown();
                                            }
                                        }
                                        else if (CUtil.DoNamesMatch(requiredItem, "V's shield"))
                                        {
                                            if (requireVsShield)
                                            {
                                                Microbot.log("Missing required item: " + requiredItem);
                                                this.shutdown();
                                            }
                                        }
                                        else
                                        {
                                            Microbot.log("Item not found!: " + requiredItem);
                                            this.shutdown();
                                        }
                                    }
                                }
                            }
                        }

                        // Regardless of a required item
                        if (slayerTarget == CUtil.SlayerTarget.KALPHITE && !hasRequiredItem)
                        {
                            // Kalphite cave needs a shantay pass if I don't have fairy rings
                            if (Rs2Bank.hasItem(ItemID.SHANTAY_PASS))
                            {
                                Rs2Bank.withdrawOne(ItemID.SHANTAY_PASS);
                                Rs2Inventory.waitForInventoryChanges(5000);

                                hasRequiredItem = true;
                            }
                        }
                        else if (slayerTarget == CUtil.SlayerTarget.HILL_GIANT && !hasRequiredItem)
                        {
                            // If going to edgeville dungeon, get a brass key
                            if (_config.AlternativeHillGiantTask() == CUtil.AlternativeHillGiantTask.HILL_GIANT_EDGEVILLE_DUNGEON)
                            {
                                if (Rs2Bank.hasItem(ItemID.EDGEVILLEDUNGEONKEY))
                                {
                                    Rs2Bank.withdrawOne(ItemID.EDGEVILLEDUNGEONKEY);
                                    Rs2Inventory.waitForInventoryChanges(5000);

                                    hasRequiredItem = true;
                                }
                            }
                        }

                        boolean foodWithdrawn = false;
                        // Withdraw food
                        for (Rs2Food food : Arrays.stream(Rs2Food.values()).sorted(Comparator.comparingInt(Rs2Food::getHeal).reversed()).collect(Collectors.toList()))
                        {
                            if (Rs2Bank.hasBankItem(food.getId(), _config.NumberOfFood()))
                            {
                                Rs2Bank.withdrawX(food.getId(), _config.NumberOfFood());
                                foodWithdrawn = true;
                                break;
                            }
                        }
                        if (!foodWithdrawn)
                        {
                            Microbot.showMessage("No food found! Shutting down.");
                            this.shutdown();
                        }
                    }

                    // Check inv for and get all wanted items for slayer
                    if (!Rs2Inventory.hasItem(Seedbox.OPEN.getId(), Seedbox.CLOSED.getId()))
                    {
                        if (Rs2Bank.hasItem(Seedbox.OPEN.getId()))
                        {
                            Rs2Bank.withdrawOne(Seedbox.OPEN.getId());
                            Rs2Inventory.waitForInventoryChanges(5000);
                        }
                        else if (Rs2Bank.hasItem(Seedbox.CLOSED.getId()))
                        {
                            Rs2Bank.withdrawOne(Seedbox.CLOSED.getId());
                            Rs2Inventory.waitForInventoryChanges(5000);
                        }
                    }
                    if (!Rs2Inventory.hasItem("Enchanted gem"))
                    {
                        if (Rs2Bank.hasItem("Enchanted gem"))
                        {
                            Rs2Bank.withdrawOne("Enchanted gem");
                            Rs2Inventory.waitForInventoryChanges(5000);
                        }
                    }

                    currentState = States.MOVING_TO_MONSTER_LOCATION;
                    lootBanked = false;
                    hasRequiredItem = false;
                    return;
                }
                break;
        }
    }

    void PopulateTargetList(String _targetName)
    {
        if (targetList.isEmpty())
        {
            Microbot.log("CardewSlayer TargetList is Empty. Manually repopulating!");
            targetList = Rs2Npc.getAttackableNpcs()
                    .filter(npc -> npc != null
                            && Microbot.getClient() != null && Microbot.getClient().getLocalPlayer() != null
                            && (npc.getInteracting() == null || npc.getInteracting() == Microbot.getClient().getLocalPlayer())
                            && npc.getWorldLocation() != null && Rs2Walker.canReach(npc.getWorldLocation())
                            && npc.getName() != null && npc.getName().toLowerCase().contains(_targetName))
                    .collect(Collectors.toList());
        }
    }

    void PopulateTargetList(int _id)
    {
        if (targetList.isEmpty()) {
            Microbot.log("CardewSlayer TargetList is Empty. Manually repopulating!");
            targetList = Rs2Npc.getAttackableNpcs()
                    .filter(npc -> npc != null
                            && Microbot.getClient() != null && Microbot.getClient().getLocalPlayer() != null
                            && (npc.getInteracting() == null || npc.getInteracting() == Microbot.getClient().getLocalPlayer())
                            && npc.getWorldLocation() != null && Rs2Walker.canReach(npc.getWorldLocation())
                            && npc.getId() == _id)
                    .collect(Collectors.toList());
        }
    }

    public String GetCurrentSlayerTargetName()
    {
        return slayerTarget.getMonsterData().getMonster();
    }

    public void TryRemoveNpcFromTargetList(NPC _npc, CardewSlayerConfig _config)
    {
        if (_npc == null || targetList.isEmpty()) return;

        if (currentState == States.SLAYING_MONSTER)
        {
            //switch (slayerTarget)
            //{
            //    case NONE:
            //        return;
//
            //    case BIRD:
            //        RemoveNpcFromTargetList(_config.AlternativeBirdTask().getMonsterName());
            //        break;
            //    case DWARF:
            //        RemoveNpcFromTargetList(_config.AlternativeDwarfTask().getMonsterName());
            //        break;
            //    case KALPHITE:
            //        RemoveNpcFromTargetList(_config.AlternativeKalphiteTask().getMonsterName());
            //        break;
            //    case WOLF:
            //        RemoveNpcFromTargetList(_config.AlternativeWolfTask().getMonsterName());
            //        break;
            //    case CRAB:
            //        RemoveNpcFromTargetList(_config.AlternativeCrabTask().getMonsterName());
            //        break;
            //    case HILL_GIANT:
            //        RemoveNpcFromTargetList(_config.AlternativeHillGiantTask().getMonsterName());
            //        break;
            //    default:
            //        RemoveNpcFromTargetList(_npc.getName());
            //        break;
            //}
            RemoveNpcFromTargetList(_npc);
        }
    }

    private void RemoveNpcFromTargetList(NPC _npc)
    {
        //if (targetList.removeIf(model -> Objects.requireNonNull(model.getName()).equalsIgnoreCase(_name)))
        //{
            //Microbot.log("NPC despawned and removed from target list: " + _name);
        //}
        boolean removed = targetList.removeIf(model -> model.getRuneliteNpc() == _npc);
        if (removed) {
            Microbot.log("NPC despawned and removed from target list: " + _npc.getName());
        }
    }

    public void TryAddNpcToTargetList(NPC _npc, CardewSlayerConfig _config)
    {
        if (_npc == null) return;

        if (currentState == States.SLAYING_MONSTER || currentState == States.MOVING_TO_MONSTER_LOCATION)
        {
            switch (slayerTarget)
            {
                case NONE:
                    return;

                case BIRD:
                    AddNpcToTargetListWithValidation(_npc, _config.AlternativeBirdTask().getMonsterName());
                    break;
                case DWARF:
                    AddNpcToTargetListWithValidation(_npc, _config.AlternativeDwarfTask().getMonsterName());
                    break;
                case KALPHITE:
                    AddNpcToTargetListWithValidation(_npc, _config.AlternativeKalphiteTask().getMonsterName());
                    break;
                case WOLF:
                    AddNpcToTargetListWithValidation(_npc, _config.AlternativeWolfTask().getMonsterName());
                    break;
                case CRAB:
                    AddNpcToTargetListWithValidation(_npc, _config.AlternativeCrabTask().getMonsterName());
                    break;
                case HILL_GIANT:
                    AddNpcToTargetListWithValidation(_npc, _config.AlternativeHillGiantTask().getMonsterName());
                    break;
                case SHADES:
                    AddNpcToTargetListWithValidation(_npc, _config.AlternativeShadeTask().getMonsterName());
                    break;
                default:
                    AddNpcToTargetListWithValidation(_npc, slayerTarget.getMonsterData().getMonster());
                    break;
            }
        }
    }

    private void AddNpcToTargetListWithValidation(NPC _npc, String _target)
    {
        if (Objects.requireNonNull(_npc.getName()).contains(_target))
        {
            if (Microbot.getClient() != null && Microbot.getClient().getLocalPlayer() != null
                    && (_npc.getInteracting() == null || _npc.getInteracting() == Microbot.getClient().getLocalPlayer())
                    && _npc.getWorldLocation() != null && Rs2Walker.canReach(_npc.getWorldLocation())
                    && _npc.getComposition() != null && Arrays.stream(_npc.getComposition()
                    .getActions()).anyMatch(action -> action != null && action.toLowerCase().contains("attack")))
            {
                targetList.add(new Rs2NpcModel(_npc));
                Microbot.log("NPC spawned and added to target list: " + _npc.getName());
            }
        }
    }

    public boolean ShouldPrayerFlick()
    {
        return currentlyFlickPrayer;
    }

    void CheckForAndOpenSeedbox()
    {
        if (Rs2Inventory.hasItem(Seedbox.CLOSED.getId()))
        {
            Rs2Inventory.interact(Seedbox.CLOSED.getId(), "Open");
        }
    }

    /**
     * Checks if we are not wearing the requiredItem,<br>
     * Withdraws the item if we are not equipping it,<br>
     * Attempts to equip the required item if equippable
     *
     * @param _requiredItem Required Item
     */
    void IsNotWearingItemThenWithdraw(String _requiredItem, CardewSlayerConfig _config)
    {
        if (!Rs2Equipment.isWearing(_requiredItem, false))
        {
            if (Rs2Bank.hasItem(_requiredItem))
            {
                if (Rs2Bank.withdrawAndEquip(_requiredItem))
                {
                    // We did equip the item
                    Rs2InventorySetup setup = new Rs2InventorySetup(_config.InventorySetup(), mainScheduledFuture);
                    setup.loadInventory();
                }
                hasRequiredItem = true;
            }
        }
    }

    public void UpdateSlayerTaskFromGemText(String chatMsg, CardewSlayerConfig _config)
    {
        Pattern pattern = Pattern.compile("kill (.+?); only (\\d+) more");
        Matcher matcher = pattern.matcher(chatMsg);

        if (matcher.find()) {
            targetMonsterName = matcher.group(1).trim();
            killsLeft = Integer.parseInt(matcher.group(2));
            //targetMonsterName = Rs2Slayer.getSlayerTask();
            targetMonsterName = CUtil.SingularisePluralName(targetMonsterName);
            //killsLeft = Rs2Slayer.getSlayerTaskSize();

            Microbot.log("Assigned: " + killsLeft + " " + targetMonsterName + "!");

            isDeterminingState = true;
            DetermineStateFromSlayerTask(_config);
        }
        else {
            Microbot.log("CardewSlayer: Couldn't parse slayer target information", Level.ERROR);
            this.shutdown();
        }
    }

    void DetermineStateFromSlayerTask(CardewSlayerConfig _config)
    {
        Microbot.log("Entered DetermineStateFromSlayerTask()");

        for (CUtil.SlayerTarget potentialTarget : CUtil.SlayerTarget.values()){
            if (potentialTarget.getMonsterData() != null) {
                //Microbot.log("Potential Target: " + potentialTarget.getMonsterData().getMonster());

                //if (targetMonsterName.equalsIgnoreCase(potentialTarget.getMonsterData().getMonster())) {
                if (CUtil.DoNamesMatch(potentialTarget.getMonsterData().getMonster(), targetMonsterName)) {
                    switch (potentialTarget) {
                        case NONE:
                            continue;
                        case BIRD:
                            potentialTarget.SetLocation(_config.AlternativeBirdTask().getLocation());
                            break;
                        case DWARF:
                            potentialTarget.SetLocation(_config.AlternativeDwarfTask().getLocation());
                            break;
                        case KALPHITE:
                            potentialTarget.SetLocation(_config.AlternativeKalphiteTask().getLocation());
                            break;
                        case WOLF:
                            potentialTarget.SetLocation(_config.AlternativeWolfTask().getLocation());
                            break;
                        case CRAB:
                            potentialTarget.SetLocation(_config.AlternativeCrabTask().getLocation());
                            break;
                        case HILL_GIANT:
                            potentialTarget.SetLocation(_config.AlternativeHillGiantTask().getLocation());
                            break;
                        case SHADES:
                            potentialTarget.SetLocation(_config.AlternativeShadeTask().getLocation());
                            break;
                        case VAMPYRE:
                            potentialTarget.SetLocation(_config.AlternativeVampyreTask().getLocation());
                            break;
                    }

                    // Our potential target name is contained within targetMonsterName
                    // Is it just contained (i.e Goblin with Hobgoblins) or a close match (I.e Goblin to Goblins)

                    Microbot.log("Confirmed assigned target: " + potentialTarget.getMonsterData().getMonster());

                    slayerTarget = potentialTarget;
                    break;
                }
                //else if (targetMonsterName.equalsIgnoreCase("fleshcrawler"))    // No space in the detected name, unlike every other task so far
                //{
                //    slayerTarget = CUtil.SlayerTarget.FLESH_CRAWLER;
//
                //    currentState = States.MOVING_TO_NEAREST_BANK;
                //    isDeterminingState = false;
                //    break;
                //}
            }
        }

        if (slayerTarget == CUtil.SlayerTarget.NONE && killsLeft <= 0)
        {
            currentState = States.MOVING_TO_SLAYER_MASTER;
            isDeterminingState = false;
        }
        else
        {
            currentState = States.MOVING_TO_NEAREST_BANK;
            isDeterminingState = false;
        }
    }

    public void SlayerTaskCompleted(CardewSlayerConfig _config)
    {
        currentState = States.MOVING_TO_SLAYER_MASTER;
        killsLeft = 0;
        hasRequiredItem = false;
        slayerTarget = CUtil.SlayerTarget.NONE;
        targetMonsterName = "";
        lootBanked = false;
        tryForceWalkToMonsterLocation = false;
        targetList.clear();

        // Try handle looting one last time before teleporting away hopefully to loot our final kill.
        HandleLooting(_config);
    }

    public void CalculateKillsLeft()
    {
        if (Rs2Slayer.hasSlayerTask())
        {
            killsLeft = Rs2Slayer.getSlayerTaskSize();
        }
        if (slayerTarget == CUtil.SlayerTarget.WALL_BEAST)
        {
            wallBeastAppeared = false;
        }
    }

    public void WallBeastAppeared() { wallBeastAppeared = true; }

    public CardewSlayerScript.States GetCurrentState() { return currentState; }

    public void WalkToMonsterLocationDirect()
    {
        if (slayerTarget != CUtil.SlayerTarget.NONE)
        {
            if (Rs2WorldPoint.quickDistance(Rs2Player.getWorldLocation(), slayerTarget.getLocation()) > 2)
            {
                Rs2Walker.walkTo(slayerTarget.getLocation(), 2);
            }
            else
            {
                tryForceWalkToMonsterLocation = false;
            }
        }
        else
        {
            tryForceWalkToMonsterLocation = false;
        }
    }

    void BuryBones()
    {
        if (Rs2Inventory.hasItem("bones", false))
        {
            Rs2ItemModel boneItem = Rs2Inventory.get("bones", false);
            //for (String action : boneItem.getInventoryActions())
            //{
                //if (action.equalsIgnoreCase("bury"))
                //{
                    Rs2Inventory.interact(boneItem, "bury");
                    Rs2Inventory.waitForInventoryChanges(3000);
                //}
            //}
        }
        if (Rs2Inventory.hasItem(" ashes", false))
        {
            Rs2ItemModel ashItem = Rs2Inventory.get(" ashes", false);
            //for (String action : ashItem.getInventoryActions())
            //{
                //if (action.equalsIgnoreCase("scatter"))
                //{
                    Rs2Inventory.interact(ashItem, "scatter");
                    Rs2Inventory.waitForInventoryChanges(3000);
                //}
            //}
        }
    }

    void HandleLooting(CardewSlayerConfig _config)
    {
        if (_config.PickupUntradeables())
        {
            LootingParameters untradeableItemParams = new LootingParameters(
                    10,
                    1,
                    1,
                    1,
                    false,
                    _config.OnlyLootMyDrops(),
                    "untradeable"
            );
            if (Rs2GroundItem.lootUntradables(untradeableItemParams))
            {
                justLooted = true;
            }
        }

        LootingParameters valueBasedItemParams = new LootingParameters(
                _config.MinLootValue(),
                Integer.MAX_VALUE,
                10,
                1,
                1,
                false,
                _config.OnlyLootMyDrops()
        );
        if (Rs2GroundItem.lootItemBasedOnValue(valueBasedItemParams))
        {
            justLooted = true;
        }

        if (_config.PickupRunes())
        {
            LootingParameters runesItemParams = new LootingParameters(
                    10,
                    1,
                    1,
                    1,
                    false,
                    _config.OnlyLootMyDrops(),
                    " rune"
            );
            if (Rs2GroundItem.lootItemsBasedOnNames(runesItemParams))
            {
                justLooted = true;
            }
        }
        if (_config.PickupSeeds())
        {
            LootingParameters seedItemParams = new LootingParameters(
                    10,
                    1,
                    1,
                    1,
                    false,
                    _config.OnlyLootMyDrops(),
                    " seed"
            );
            if (Rs2GroundItem.lootItemsBasedOnNames(seedItemParams))
            {
                justLooted = true;
            }
        }
        if (_config.PickupGrimyHerbs())
        {
            LootingParameters herbItemParams = new LootingParameters(
                    10,
                    1,
                    1,
                    1,
                    false,
                    _config.OnlyLootMyDrops(),
                    "Grimy "
            );
            if (Rs2GroundItem.lootItemsBasedOnNames(herbItemParams))
            {
                justLooted = true;
            }
        }

        if (_config.PickupAndBuryBones())
        {
            LootingParameters boneItemParams = new LootingParameters(
                    10,
                    1,
                    1,
                    1,
                    false,
                    _config.OnlyLootMyDrops(),
                    "bones"
            );
            if (Rs2GroundItem.lootItemsBasedOnNames(boneItemParams))
            {
                justLooted = true;
            }

            LootingParameters ashItemParams = new LootingParameters(
                    10,
                    1,
                    1,
                    1,
                    false,
                    _config.OnlyLootMyDrops(),
                    new String[]{"ashes"},
                    " ashe"
            );
            // Hopefully this only loots ashes of a type. Not regular ashes.
            if (Rs2GroundItem.lootItemsBasedOnNames(ashItemParams))
            {
                justLooted = true;
            }
        }
    }
}
