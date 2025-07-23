package net.runelite.client.plugins.microbot.cardewsPlugins.CardewSlayer;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.cardewsPlugins.CUtil;
import net.runelite.client.plugins.microbot.globval.enums.Skill;
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
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.slayer.enums.SlayerMaster;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.slayer.SlayerPlugin;
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
    boolean handleManualMovement = false;

    public boolean run(CardewSlayerConfig config) {
        Microbot.enableAutoRunOn = false;
        CUtil.SetMyAntiban(0.0);

        slayerGemChecked = false;
        lootBanked = false;

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
                // Handle eating food outside the state machine
                if (!Rs2Player.eatAt(config.EatFoodPercent()))
                {
                    // If FALSE then we didn't eat
                    // Handle emergency teleport/transitioning to banking state
                    if (Rs2Inventory.getInventoryFood().isEmpty())
                    {
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

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    // Old method when a String was used for Slayer Master name
    SlayerMaster GetSlayerMasterFromConfigString(CardewSlayerConfig _config) {
        for (SlayerMaster master : SlayerMaster.values()) {
            if (master.getName().equalsIgnoreCase(_config.SlayerMaster().getName())) {
                return master;
            }
        }
        // No string equal to a masters name
        return SlayerMaster.NONE;
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

                if (currentMaster == SlayerMaster.NONE) {
                    Microbot.log("CardewSlayer: No Slayer Master!");
                    break;
                }
                WorldPoint cornerNW_MTSM = new WorldPoint(currentMaster.getWorldPoint().getX() - 2, currentMaster.getWorldPoint().getY() + 2, currentMaster.getWorldPoint().getPlane());
                WorldPoint cornerSE_MTSM = new WorldPoint(currentMaster.getWorldPoint().getX() + 2, currentMaster.getWorldPoint().getY() - 2, currentMaster.getWorldPoint().getPlane());

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

                if (killsLeft <= 0)
                {
                    currentState = States.MOVING_TO_NEAREST_BANK;
                }

                npcsInteractingWithPlayer = Rs2Npc.getNpcsForPlayer().collect(Collectors.toList());

                if (!Rs2Combat.inCombat())
                {
                    // Not in combat. Pick a fight.
                    // Check monsters that are already fighting the player
                    if (!npcsInteractingWithPlayer.isEmpty())
                    {
                        Rs2NpcModel target = npcsInteractingWithPlayer.stream()
                                .filter(npc -> Arrays.stream(npc.getComposition().getActions())
                                        .anyMatch(action -> action.contains("Attack")))
                                .findFirst().orElse(null);

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
                        targetList = Rs2Npc.getAttackableNpcs()
                                .filter(npc -> npc.getName() != null && npc.getName().toLowerCase().contains(slayerTarget.getMonsterData().getMonster().toLowerCase()))
                                .collect(Collectors.toList());

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
                                        .filter(npc -> npc.getName() != null && npc.getName().toLowerCase().contains("lizard"))
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
                if (Rs2Walker.walkWithBankedTransports(Rs2Bank.getNearestBank().getWorldPoint()))
                {
                    currentState = States.BANKING;
                }
                break;

            case BANKING:
                if (!Rs2Bank.isOpen())
                {
                    Rs2Bank.openBank();
                }
                else
                {
                    if (!lootBanked)
                    {
                        // Bank all loots and shit lol.
                        // Temp deposit all for now
                        Rs2Bank.depositAllExcept("Seed box", "Open seed box", "Enchanted gem");
                        Rs2Inventory.interact(Seedbox.OPEN.getId(), "Empty");
                        lootBanked = true;
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

                    // Check if our task has any requirements in the first place
                    if (slayerTarget != CUtil.SlayerTarget.NONE)
                    {
                        if (slayerTarget.getMonsterData().getItemsRequired().length > 0)
                        {

                        }

                        currentState = States.MOVING_TO_MONSTER_LOCATION;
                        lootBanked = false;
                    }
                    else
                    {
                        currentState = States.MOVING_TO_SLAYER_MASTER;
                        lootBanked = false;
                    }
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

    public void UpdateSlayerTaskFromGemText(String chatMsg, CardewSlayerConfig _config)
    {
        Pattern pattern = Pattern.compile("kill (.+?); only (\\d+) more");
        Matcher matcher = pattern.matcher(chatMsg);

        if (matcher.find()) {
            targetMonsterName = matcher.group(1).trim();
            killsLeft = Integer.parseInt(matcher.group(2));

            DetermineStateFromSlayerTask(_config);
        }
        else {
            Microbot.log("CardewSlayer: Couldn't parse slayer target information", Level.ERROR);
            this.shutdown();
        }
    }

    void DetermineStateFromSlayerTask(CardewSlayerConfig _config)
    {
        for (CUtil.SlayerTarget potentialTarget : CUtil.SlayerTarget.values()){
            boolean taskDeterminedFromAlternative = false;
            switch (potentialTarget)
            {
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
            }
            if (taskDeterminedFromAlternative)
            {
                currentState = States.MOVING_TO_NEAREST_BANK;
                break;
            }
            if (targetMonsterName.contains(potentialTarget.getMonsterData().getMonster().toLowerCase())){
                slayerTarget = potentialTarget;

                // Do we have enough food?
                int invFoodCount = Rs2Inventory.getInventoryFood().size();
                if (invFoodCount < _config.NumberOfFood())
                {
                    currentState = States.MOVING_TO_MONSTER_LOCATION;
                    break;
                }

                if (slayerTarget.getMonsterData().getItemsRequired().length > 0)
                {
                    if (Rs2Equipment.isWearing(slayerTarget.getMonsterData().getItemsRequired()))
                    {
                        Microbot.log("Has required item in worn equipment.");
                        currentState = States.MOVING_TO_NEAREST_BANK;
                        break;
                    }
                    else if (Rs2Inventory.hasItem(slayerTarget.getMonsterData().getItemsRequired()))
                    {
                        boolean equippedItem = false;
                        for (String requiredItem : slayerTarget.getMonsterData().getItemsRequired())
                        {
                            if (Rs2Inventory.hasItem(requiredItem))
                            {
                                if (Rs2Inventory.wear(requiredItem))
                                {
                                    currentState = States.MOVING_TO_NEAREST_BANK;
                                    equippedItem = true;
                                    break;
                                }
                            }
                        }
                        if (equippedItem)
                        {
                            break;
                        }
                    }
                    else
                    {
                        //Microbot.log("No required item found in inventory or worn. Aborting.");
                        //Microbot.showMessage("Required items: " + Arrays.toString(slayerTarget.getMonsterData().getItemsRequired()));
                        //shutdown();
                    }
                }

                currentState = States.MOVING_TO_NEAREST_BANK;
                break;
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
        slayerTarget = CUtil.SlayerTarget.NONE;
        lootBanked = false;
    }

    public void DeductKillsLeft()
    {
        killsLeft--;
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
    }
}
