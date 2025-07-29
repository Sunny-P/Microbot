package net.runelite.client.plugins.microbot.cardewsPlugins.CardewSlayer;

import lombok.Getter;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.cardewsPlugins.CUtil;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.Rs2InventorySetup;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.slayer.enums.SlayerMaster;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import org.slf4j.event.Level;

import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CardewSlayerScript extends Script {
    enum States{
        MOVING_TO_SLAYER_MASTER,
        GETTING_SLAYER_TASK,
        MOVING_TO_MONSTER_LOCATION,
        SLAYING_MONSTER,
        MOVING_TO_NEAREST_BANK,
        BANKING
    }
    @Getter
    static States currentState = States.MOVING_TO_SLAYER_MASTER;

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

    static int killsLeft = 0;
    String targetMonsterName = "";
    static CUtil.SlayerTarget slayerTarget = CUtil.SlayerTarget.NONE;
    private List<Rs2NpcModel> targetList = new ArrayList<>();
    List<Rs2NpcModel> npcsInteractingWithPlayer = new ArrayList<>();
    boolean slayerGemChecked = false;
    boolean lootBanked = false;
    boolean lightingLightSource = false;
    boolean hasRequiredItem = false;

    public boolean run(CardewSlayerConfig config) {
        Microbot.enableAutoRunOn = false;
        CUtil.SetMyAntiban(0.0, 2, 15, 0.4);

        slayerGemChecked = false;
        lootBanked = false;
        lightingLightSource = false;
        hasRequiredItem = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.bankPinBeingHandled) return;
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

                // Handle eating food outside the state machine
                if (!Rs2Player.eatAt(config.EatFoodPercent()))
                {
                    // If FALSE then we didn't eat
                    // Did we not eat because we don't have anymore food?
                    // If so, we need to save ourselves
                    if (Rs2Inventory.getInventoryFood().isEmpty())
                    {
                        // Handle emergency teleport/transitioning to banking state
                        if (currentState == States.SLAYING_MONSTER || currentState == States.MOVING_TO_MONSTER_LOCATION)
                        {
                            currentState = States.MOVING_TO_NEAREST_BANK;
                        }
                    }
                }
                if (!Rs2Inventory.isFull())
                {
                    // Loot
                    HandleLooting(config);
                }
                HandleStateMachine(config);

                if (config.PickupAndBuryBones())
                {
                    BuryBones();
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
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
                        break;
                    }
                }

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
                if (Rs2Widget.isWidgetVisible(231, 6)) {
                    // Get the text as a string
                    String chatText = Rs2Widget.getChildWidgetText(231, 6);
                    // Get number of kills
                    // Get name of task
                    Pattern pattern = Pattern.compile("kill (\\d+) (.+)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(chatText);

                    if (matcher.find()) {
                        // Gained killsLeft and monster data with this pattern method. I.E new task
                        killsLeft = Integer.parseInt(matcher.group(1));
                        targetMonsterName = matcher.group(2).trim();

                        Microbot.log("Assigned: " + killsLeft + " " + targetMonsterName + "!");

                        if (Rs2Dialogue.hasContinue())
                        {
                            Rs2Dialogue.clickContinue();
                        }

                        DetermineStateFromSlayerTask(_config);
                    }
                    else {
                        // Get killsLeft and monster info with second pattern. I.E already on task.
                        Pattern secondPattern = Pattern.compile("hunting (.+?), you have (\\d+) to go", Pattern.CASE_INSENSITIVE);
                        Matcher secondMatcher = secondPattern.matcher(chatText);

                        if (secondMatcher.find()) {
                            targetMonsterName = secondMatcher.group(1).trim();
                            killsLeft = Integer.parseInt(secondMatcher.group(2));

                            Microbot.log("Assigned: " + killsLeft + " " + targetMonsterName + "!");

                            if (Rs2Dialogue.hasContinue())
                            {
                                Rs2Dialogue.clickContinue();
                            }

                            DetermineStateFromSlayerTask(_config);
                        }
                        else {
                            Microbot.log("CardewSlayer: Couldn't parse slayer target information", Level.ERROR);
                            this.shutdown();
                        }
                    }
                }
                else {
                    if (currentMaster == SlayerMaster.NONE) {
                        Microbot.log("CardewSlayer: No Slayer Master!");
                        break;
                    }
                    // Get task from master
                    Rs2Npc.interact(currentMaster.getName(), "Assignment");
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
                        Rs2Player.eatAt(1);
                    }
                }

                npcsInteractingWithPlayer = Rs2Npc.getNpcsForPlayer().collect(Collectors.toList());

                if (!Rs2Combat.inCombat())
                {
                    // Not in combat. Pick a fight.
                    // Check monsters that are already fighting the player
                    if (!npcsInteractingWithPlayer.isEmpty())
                    {
                        Rs2NpcModel target = npcsInteractingWithPlayer.stream()
                                .filter(npc -> npc.getComposition() != null &&
                                        Arrays.stream(npc.getComposition().getActions())
                                                .anyMatch(action -> action != null && action.toLowerCase().contains("attack")))
                                .findFirst().orElse(null);
                        Microbot.log("Target: " + target);

                        assert target != null;
                        if (!Rs2Camera.isTileOnScreen(target.getLocalLocation()))
                        {
                            Rs2Camera.turnTo(target);
                        }

                        Rs2Npc.interact(target, "Attack");
                        Rs2Antiban.actionCooldown();
                    }
                    else
                    {
                        // We don't have any monsters currently in combat with us
                        // Pick a fresh target
                        if (slayerTarget == CUtil.SlayerTarget.KALPHITE)
                        {
                            targetList = Rs2Npc.getAttackableNpcs()
                                    .filter(npc -> npc != null
                                            && (npc.getInteracting() == null || npc.getInteracting() == Microbot.getClient().getLocalPlayer())
                                            && npc.getWorldLocation() != null && Rs2Walker.canReach(npc.getWorldLocation())
                                            && npc.getName() != null && npc.getName().toLowerCase().contains(_config.AlternativeKalphiteTask().getMonsterName().toLowerCase()))
                                    .collect(Collectors.toList());
                        }
                        else
                        {
                            targetList = Rs2Npc.getAttackableNpcs()
                                    .filter(npc -> npc != null
                                            && (npc.getInteracting() == null || npc.getInteracting() == Microbot.getClient().getLocalPlayer())
                                            && npc.getWorldLocation() != null && Rs2Walker.canReach(npc.getWorldLocation())
                                            && npc.getName() != null && npc.getName().toLowerCase().contains(slayerTarget.getMonsterData().getMonster().toLowerCase()))
                                    .collect(Collectors.toList());
                        }
                        Microbot.log("TargetList: " + targetList);

                        if (!targetList.isEmpty())
                        {
                            Rs2NpcModel target = targetList.stream()
                                    .findFirst()
                                    .orElse(null);

                            // If monster is not in camera, turn to it
                            if (!Rs2Camera.isTileOnScreen(target.getLocalLocation()))
                            {
                                Rs2Camera.turnTo(target);
                            }

                            Rs2Npc.interact(target, "Attack");
                            Rs2Antiban.actionCooldown();
                        }
                    }
                }
                else
                {
                    Rs2NpcModel inCombatNpc;
                    // We are in combat
                    if (!npcsInteractingWithPlayer.isEmpty())
                    {
                        // Determine if this is something we need to use a slayer item on to kill
                        switch (slayerTarget)
                        {
                            case LIZARD:
                                inCombatNpc = npcsInteractingWithPlayer.stream()
                                        .filter(npc -> npc != null
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
                                        break;
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
                }
                break;

            case BANKING:
                if (!Rs2Bank.isOpen() && !lightingLightSource)
                {
                    Rs2Bank.openBank();
                }
                else
                {
                    // We are regearing/banking loot for a current task
                    if (killsLeft > 0)
                    {
                        // Check if our task has any requirements in the first place
                        List<String> requiredItemsList = Arrays.stream(slayerTarget.getMonsterData().getItemsRequired()).collect(Collectors.toList());
                        if (!requiredItemsList.get(0).equalsIgnoreCase("none"))
                        {
                            boolean requireSlayerHelm = true;

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
                                        IsNotWearingItemThenWithdraw(requiredItem);
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
                                    // Withdrawn regular non-case required item
                                    // If we are already wearing the required item no need to withdraw
                                    IsNotWearingItemThenWithdraw(requiredItem);
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
                                    else
                                    {
                                        Microbot.log("Item not found!: " + requiredItem);
                                        this.shutdown();
                                    }
                                }
                            }
                        }
                        if (slayerTarget == CUtil.SlayerTarget.KALPHITE && !hasRequiredItem)
                        {
                            Rs2Bank.withdrawOne(ItemID.SHANTAY_PASS);
                            Rs2Inventory.waitForInventoryChanges(5000);

                            hasRequiredItem = true;
                        }
                    }

                    if (!lootBanked)
                    {
                        if (_config.InventorySetup() != null && !hasRequiredItem)
                        {
                            Rs2InventorySetup setup = new Rs2InventorySetup(_config.InventorySetup(), mainScheduledFuture);
                            setup.loadEquipment();
                            setup.loadInventory();
                        }
                        //else if (_config.InventorySetup() != null)
                        //{
                        //    Rs2InventorySetup setup = new Rs2InventorySetup(_config.InventorySetup(), mainScheduledFuture);
                        //    setup.loadInventory();
                        //}
                        else
                        {
                            // No inventory setup loaded
                            Rs2Bank.depositAllExcept("Seed box", "Open seed box", "Enchanted gem", "Dramen staff", "Herb sack", "Open herb sack", "Shantay pass", "Ice cooler", "Waterskin(4)", "Bullseye lantern");
                        }
                        Rs2Inventory.interact(Seedbox.OPEN.getId(), "Empty");
                        lootBanked = true;

                        if (killsLeft <= 0)
                        {
                            currentState = States.MOVING_TO_SLAYER_MASTER;
                            lootBanked = false;
                            break;
                        }
                    }
                    // Check inv for and get all wanted items for slayer
                    if (!Rs2Inventory.hasItem(Seedbox.OPEN.getId(), Seedbox.CLOSED.getId()))
                    {
                        if (Rs2Bank.hasItem(Seedbox.OPEN.getId()))
                        {
                            Rs2Bank.withdrawOne(Seedbox.OPEN.getId());
                        }
                        else if (Rs2Bank.hasItem(Seedbox.CLOSED.getId()))
                        {
                            Rs2Bank.withdrawOne(Seedbox.CLOSED.getId());
                        }
                    }
                    if (!Rs2Inventory.hasItem("Enchanted gem"))
                    {
                        if (Rs2Bank.hasItem("Enchanted gem"))
                        {
                            Rs2Bank.withdrawOne("Enchanted gem");
                        }
                    }

                    // Withdraw food
                    for (Rs2Food food : Arrays.stream(Rs2Food.values()).sorted(Comparator.comparingInt(Rs2Food::getHeal).reversed()).collect(Collectors.toList()))
                    {
                        if (Rs2Bank.hasBankItem(food.getId(), _config.NumberOfFood()))
                        {
                            Rs2Bank.withdrawX(food.getId(), _config.NumberOfFood());
                            break;
                        }
                    }

                    currentState = States.MOVING_TO_MONSTER_LOCATION;
                    lootBanked = false;
                }
                break;
        }
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
    void IsNotWearingItemThenWithdraw(String _requiredItem)
    {
        if (!Rs2Equipment.isWearing(_requiredItem, false))
        {
            if (Rs2Bank.hasItem(_requiredItem))
            {
                if (Rs2Bank.withdrawAndEquip(_requiredItem))
                {
                    // We did equip the item
                    // Flag lootBanked as false so we will bank any items we end up swapping out for required item
                    lootBanked = false;
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

            Microbot.log("Assigned: " + killsLeft + " " + targetMonsterName + "!");

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

        // Hard coded task value because I haven't interpretted plural words that are spelt differently, like wolves when the task is looking for wolf
        if (targetMonsterName.toLowerCase().contains("wolves"))
        {
            slayerTarget = CUtil.SlayerTarget.WOLF;
            slayerTarget.SetLocation(_config.AlternativeWolfTask().getLocation());
            currentState = States.MOVING_TO_NEAREST_BANK;
            return;
        }
        else if (targetMonsterName.toLowerCase().contains("dwarves"))
        {
            slayerTarget = CUtil.SlayerTarget.DWARF;
            slayerTarget.SetLocation(_config.AlternativeDwarfTask().getLocation());
            currentState = States.MOVING_TO_NEAREST_BANK;
            return;
        }

        for (CUtil.SlayerTarget potentialTarget : CUtil.SlayerTarget.values()){
            if (potentialTarget.getMonsterData() != null) {
                Microbot.log("Potential Target: " + potentialTarget.getMonsterData().getMonster());
                if (targetMonsterName.toLowerCase().contains(potentialTarget.getMonsterData().getMonster().toLowerCase())) {
                    slayerTarget = potentialTarget;
                    Microbot.log("Assigned task contains potentialTarget.getMonster(): " + potentialTarget.getMonsterData().getMonster());

                    boolean taskDeterminedFromAlternative = false;
                    switch (potentialTarget) {
                        case NONE:
                            continue;
                        case BIRD:
                            potentialTarget.SetLocation(_config.AlternativeBirdTask().getLocation());

                            slayerTarget = potentialTarget;
                            taskDeterminedFromAlternative = true;
                            break;
                        case DWARF:
                            potentialTarget.SetLocation(_config.AlternativeDwarfTask().getLocation());

                            slayerTarget = potentialTarget;
                            taskDeterminedFromAlternative = true;
                            break;
                        case KALPHITE:
                            potentialTarget.SetLocation(_config.AlternativeKalphiteTask().getLocation());

                            slayerTarget = potentialTarget;
                            taskDeterminedFromAlternative = true;
                            break;
                        case WOLF:
                            potentialTarget.SetLocation(_config.AlternativeWolfTask().getLocation());

                            slayerTarget = potentialTarget;
                            taskDeterminedFromAlternative = true;
                            break;
                        case CRAB:
                            potentialTarget.SetLocation(_config.AlternativeCrabTask().getLocation());

                            slayerTarget = potentialTarget;
                            taskDeterminedFromAlternative = true;
                            break;
                    }
                    if (taskDeterminedFromAlternative) {
                        Microbot.log("Task has alternative task location.");
                        currentState = States.MOVING_TO_NEAREST_BANK;
                        break;
                    }

                    currentState = States.MOVING_TO_NEAREST_BANK;
                    break;
                }
            }
        }
    }

    List<Rs2NpcModel> GetCombatTargetList()
    {
        switch (slayerTarget)
        {
            case BEAR:
                return Rs2Npc.getAttackableNpcs().filter(npc -> npc.getName() != null && npc.getName().toLowerCase().contains(slayerTarget.getMonsterData().getMonster().toLowerCase()))
                        .collect(Collectors.toList());
        }
        return Rs2Npc.getAttackableNpcs(slayerTarget.getMonsterData().getMonster(), false).collect(Collectors.toList());
    }

    public void SlayerTaskCompleted()
    {
        currentState = States.MOVING_TO_SLAYER_MASTER;
        killsLeft = 0;
        hasRequiredItem = false;
        slayerTarget = CUtil.SlayerTarget.NONE;
        targetMonsterName = "";
        lootBanked = false;
    }

    public void DeductKillsLeft()
    {
        killsLeft--;
    }

    void BuryBones()
    {
        if (Rs2Inventory.hasItem("bones", false))
        {
            Rs2ItemModel boneItem = Rs2Inventory.get("bones", false);
            for (String action : boneItem.getInventoryActions())
            {
                if (action.equalsIgnoreCase("bury"))
                {
                    Rs2Inventory.interact(boneItem, "bury");
                }
            }
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
            Rs2GroundItem.lootUntradables(untradeableItemParams);
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
        Rs2GroundItem.lootItemBasedOnValue(valueBasedItemParams);

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
            Rs2GroundItem.lootItemsBasedOnNames(runesItemParams);
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
            Rs2GroundItem.lootItemsBasedOnNames(seedItemParams);
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
            Rs2GroundItem.lootItemsBasedOnNames(herbItemParams);
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
            Rs2GroundItem.lootItemsBasedOnNames(boneItemParams);
        }
    }
}
