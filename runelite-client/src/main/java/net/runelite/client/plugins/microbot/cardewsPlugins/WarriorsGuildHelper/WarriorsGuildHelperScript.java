package net.runelite.client.plugins.microbot.cardewsPlugins.WarriorsGuildHelper;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Singleton
@Slf4j
public class WarriorsGuildHelperScript extends Script {
    private static WarriorsGuildHelperState state = WarriorsGuildHelperState.GOING_TO_BANK;
    int numberOfTokens = -1;

    private static WarriorsGuildDefenders currentDefender;
    private static WarriorsGuildDefenders nextDefender;

    WorldPoint cyclopsLocationUpstairsOutside = new WorldPoint(2845, 3540, 2);
    int upstairsdoorId = 24306;
    WorldPoint cyclopsLocationBasementOutside = new WorldPoint(2909, 9968, 0);
    int basementDoorId = 10043;

    LootingParameters untradeableItemParams;
    LootingParameters valueBasedItemParams;
    LootingParameters seedItemParams;
    LootingParameters herbItemParams;

    private List<Rs2NpcModel> targetList = new CopyOnWriteArrayList<>();
    List<Rs2NpcModel> npcsInteractingWithPlayer = new CopyOnWriteArrayList<>();

    /**
     *
     */
    public boolean run(WarriorsGuildHelperConfig config) {
        InitialiseLootingParameters(config);

        state = WarriorsGuildHelperState.GOING_TO_BANK;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Microbot.pauseAllScripts.get()) return;
                if (Rs2AntibanSettings.microBreakActive) return;

                switch(state)
                {
                    case GOING_TO_BANK:
                        MoveToBank();
                        break;

                    case BANKING:
                        if (!Rs2Bank.isOpen())
                        {
                            Rs2Bank.openBank();
                            return;
                        }

                        DetermineCurrentAndNextDefendersWhileBanking();
                        WithdrawAndEquipCurrentDefender();

                        if (!Rs2Bank.isOpen()) { return; }
                        // If the bank closed because of a previous action, return and allow it to resolve and continue in the next iteration
                        // Will only have closed if equipping a defender force closes the bank.
                        Rs2Bank.depositAllExcept(ItemID.WARGUILD_TOKENS, config.FoodToUse().getId(), ItemID.SEED_BOX_OPEN, ItemID.SLAYER_HERB_SACK_OPEN);
                        if (config.UseHerbSack())
                        {
                            Rs2Inventory.interact(ItemID.SLAYER_HERB_SACK_OPEN, "empty");
                        }
                        if (config.UseSeedBox())
                        {
                            Rs2Inventory.interact(ItemID.SEED_BOX_OPEN, "empty");
                        }

                        WithdrawTokensAndFood(config);

                        if (Rs2Bank.isOpen())
                        {
                            Rs2Bank.closeBank();
                        }
                        state = WarriorsGuildHelperState.GOING_TO_CYCLOPES;
                        break;

                    case GOING_TO_CYCLOPES:
                        if (currentDefender == WarriorsGuildDefenders.DRAGON_DEFENDER || nextDefender == WarriorsGuildDefenders.DRAGON_DEFENDER)
                        {
                            MoveToCylcopes(cyclopsLocationBasementOutside);
                        }
                        else
                        {
                            MoveToCylcopes(cyclopsLocationUpstairsOutside);
                        }
                        break;

                    case FIGHTING_CYCLOPES:
                        EatAndEscape(config);
                        HandleLooting(config);

                        if (nextDefender != WarriorsGuildDefenders.DRAGON_DEFENDER)
                        {
                            // Check if we have the next defender in our inventory, if it's not Dragon
                            // Change state to SHOW_KAMFREENA_DEFENDER
                            if (Rs2Inventory.hasItem(nextDefender.getId()))
                            {
                                UseUpstairsDoor();
                                state = WarriorsGuildHelperState.SHOW_KAMFREENA_DEFENDER;
                                return;
                            }
                        }
                        else
                        {
                            // Equip dragon defender if we get it, if we are not currently equipping one.
                            if (Rs2Inventory.hasItem(nextDefender.getId()) && !Rs2Equipment.isWearing(nextDefender.getId()))
                            {
                                Rs2Inventory.equip(nextDefender.getId());
                                if (currentDefender != nextDefender)
                                {
                                    currentDefender = nextDefender;
                                }
                            }
                        }

                        HandleCombat();

                        if (Rs2Inventory.isFull())
                        {
                            state = WarriorsGuildHelperState.GOING_TO_BANK;
                        }
                        break;

                    case SHOW_KAMFREENA_DEFENDER:
                        if (nextDefender != WarriorsGuildDefenders.DRAGON_DEFENDER)
                        {
                            if (Rs2Inventory.hasItem(nextDefender.getId()))
                            {
                                Rs2Inventory.useItemOnNpc(nextDefender.getId(), NpcID.WARGUILD_KAMFREENA);
                                Rs2Dialogue.sleepUntilInDialogue();
                                Rs2Dialogue.sleepUntilNotInDialogue();

                                currentDefender = nextDefender;
                                nextDefender = WarriorsGuildDefenders.values()[currentDefender.ordinal() + 1];
                                if (nextDefender != WarriorsGuildDefenders.DRAGON_DEFENDER)
                                {
                                    Rs2Inventory.equip(currentDefender.getId());
                                    Rs2Inventory.waitForInventoryChanges(600);

                                    // Talked to Kamfreena, enter door to fight cyclops.
                                    UseUpstairsDoor();

                                    Rs2Dialogue.sleepUntilInDialogue();
                                    Rs2Dialogue.sleepUntilNotInDialogue();

                                    //UseUpstairsDoor();
                                    state = WarriorsGuildHelperState.FIGHTING_CYCLOPES;
                                }
                                else
                                {
                                    // Move to basement cyclopes
                                    state = WarriorsGuildHelperState.GOING_TO_CYCLOPES;
                                }
                                return;
                            }

                            // If we don't have any defender, talk to Kamfreena, then go in
                            if (GetWornDefender() == WarriorsGuildDefenders.NONE && GetHighestDefenderInInventory() == WarriorsGuildDefenders.NONE)
                            {
                                Rs2Npc.interact(NpcID.WARGUILD_KAMFREENA, "Talk-to");
                                Rs2Dialogue.sleepUntilInDialogue();
                                Rs2Dialogue.sleepUntilNotInDialogue();
                            }

                            // Talked to Kamfreena, enter door to fight cyclops.
                            //GameObject door = Rs2GameObject.getGameObject(upstairsDoorIds[1]);
                            UseUpstairsDoor();

                            Rs2Dialogue.sleepUntilInDialogue();
                            Rs2Dialogue.sleepUntilNotInDialogue();

                            UseUpstairsDoor();
                            state = WarriorsGuildHelperState.FIGHTING_CYCLOPES;
                            return;
                        }
                        else
                        {
                            // BASEMENT NPC, HUNTING DRAGON DEFENDER, ETC.
                            if (currentDefender == WarriorsGuildDefenders.RUNE_DEFENDER)
                            {
                                if (Rs2Equipment.isWearing(currentDefender.getId()))
                                {
                                    Rs2Equipment.unEquip(currentDefender.getId());
                                    Rs2Inventory.waitForInventoryChanges(600);
                                }

                                Rs2Inventory.useItemOnNpc(currentDefender.getId(), NpcID.WARGUILD_LORELAI);
                                Rs2Dialogue.sleepUntilInDialogue();
                                Rs2Dialogue.sleepUntilNotInDialogue();

                                Rs2Inventory.equip(currentDefender.getId());
                                Rs2Inventory.waitForInventoryChanges(600);
                            }
                            //GameObject door = Rs2GameObject.getGameObject(basementDoorId);
                            WallObject door = Rs2GameObject.getWallObject(basementDoorId);
                            if (door != null)
                            {
                                Rs2GameObject.interact(door);
                                sleepGaussian(600, 100);
                            }
                            sleepUntil(() -> !Rs2Player.isMoving());
                            state = WarriorsGuildHelperState.FIGHTING_CYCLOPES;
                        }
                        break;
                }

            } catch (Exception ex) {
                log.error("Error test loop", ex);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        return true;
    }

    void MoveToBank()
    {
        //if (!Rs2Walker.isNear(BankLocation.WARRIORS_GUILD.getWorldPoint()))
        //{
        //    Rs2Walker.walkTo(BankLocation.WARRIORS_GUILD.getWorldPoint());
        //    return;
        //}
        //state = WarriorsGuildHelperState.BANKING;

        if (Rs2Walker.walkTo(BankLocation.WARRIORS_GUILD.getWorldPoint()))
        {
            state = WarriorsGuildHelperState.BANKING;
        }
    }

    void WithdrawTokensAndFood(WarriorsGuildHelperConfig config)
    {
        if (!Rs2Inventory.hasItemAmount(ItemID.WARGUILD_TOKENS, 100))
        {
            int numberOfTokensToWithdraw = Rs2Bank.count(ItemID.WARGUILD_TOKENS) - 100;
            if (numberOfTokensToWithdraw <= 0)
            {
                Microbot.showMessage("Not enough tokens found. Shutting down..");
                Microbot.stopPlugin(WarriorsGuildHelperPlugin.class);
                return;
            }
            Rs2Bank.withdrawX(ItemID.WARGUILD_TOKENS, numberOfTokensToWithdraw);
            Rs2Inventory.waitForInventoryChanges(600);
        }

        if (!Rs2Inventory.hasItemAmount(config.FoodToUse().getId(), config.FoodAmount()))
        {
            // Withdraw food amount based on how much food of our selected type is already in our inventory
            int numberFoodToWithdraw = config.FoodAmount() - Rs2Inventory.all(item -> item.getId() == config.FoodToUse().getId()).size();
            if (numberFoodToWithdraw <= 0)
            {
                Microbot.showMessage("Not enough food in bank. Shutting down..");
                Microbot.stopPlugin(WarriorsGuildHelperPlugin.class);
                return;
            }
            Rs2Bank.withdrawX(config.FoodToUse().getId(), numberFoodToWithdraw);
            Rs2Inventory.waitForInventoryChanges(600);
        }
    }

    WarriorsGuildDefenders GetHighestDefenderInBank()
    {
        WarriorsGuildDefenders highestDefender = WarriorsGuildDefenders.NONE;

        for (WarriorsGuildDefenders defender : WarriorsGuildDefenders.values())
        {
            if (defender == WarriorsGuildDefenders.NONE) { continue; }
            if (!Rs2Bank.hasItem(defender.getId())) { continue; }

            if (defender.ordinal() > highestDefender.ordinal())
            {
                highestDefender = defender;
            }
        }

        return highestDefender;
    }

    WarriorsGuildDefenders GetHighestDefenderInInventory()
    {
        WarriorsGuildDefenders highestDefender = WarriorsGuildDefenders.NONE;

        for (WarriorsGuildDefenders defender : WarriorsGuildDefenders.values())
        {
            if (defender == WarriorsGuildDefenders.NONE) { continue; }
            if (!Rs2Inventory.hasItem(defender.getId())) { continue; }

            if (defender.ordinal() > highestDefender.ordinal())
            {
                highestDefender = defender;
            }
        }

        return highestDefender;
    }

    WarriorsGuildDefenders GetWornDefender()
    {
        for (WarriorsGuildDefenders defender : WarriorsGuildDefenders.values())
        {
            if (defender == WarriorsGuildDefenders.NONE) { continue; }

            if (Rs2Equipment.isWearing(defender.getId()))
            {
                return defender;
            }
        }

        return WarriorsGuildDefenders.NONE;
    }

    void DetermineCurrentAndNextDefendersWhileBanking()
    {
        currentDefender = GetHighestDefenderInBank();
        if (GetWornDefender().ordinal() > currentDefender.ordinal())
        {
            currentDefender = GetWornDefender();
        }

        if (GetHighestDefenderInInventory().ordinal() > currentDefender.ordinal())
        {
            currentDefender = GetHighestDefenderInInventory();
        }

        if (currentDefender == WarriorsGuildDefenders.DRAGON_DEFENDER)
        {
            nextDefender = WarriorsGuildDefenders.DRAGON_DEFENDER;
        }
        else
        {
            nextDefender = WarriorsGuildDefenders.values()[currentDefender.ordinal() + 1];
        }
    }

    void WithdrawAndEquipCurrentDefender()
    {
        if (currentDefender == GetHighestDefenderInInventory())
        {
            // Current defender is in the inventory
            Rs2Inventory.equip(currentDefender.getId());
            Rs2Inventory.waitForInventoryChanges(600);
        }
        else if (currentDefender == GetHighestDefenderInBank())
        {
            // Current defender is left in bank
            Rs2Bank.withdrawItem(currentDefender.getId());
            Rs2Inventory.waitForInventoryChanges(600);
            Rs2Inventory.equip(currentDefender.getId());
            Rs2Inventory.waitForInventoryChanges(600);
        }
        // If our current defender is not either of the above statements, we are equipping it.
    }

    void MoveToCylcopes(WorldPoint cyclopsLocation)
    {
        //if (!Rs2Walker.isNear(cyclopsLocation))
        //{
        //    Rs2Walker.walkTo(cyclopsLocation);
        //    return;
        //}
        //state = WarriorsGuildHelperState.SHOW_KAMFREENA_DEFENDER;

        if (Rs2Walker.walkTo(cyclopsLocation))
        {
            state = WarriorsGuildHelperState.SHOW_KAMFREENA_DEFENDER;
        }
    }

    void InitialiseLootingParameters(WarriorsGuildHelperConfig config)
    {
        untradeableItemParams = new LootingParameters(
                10,
                1,
                1,
                0,
                false,
                config.LootOnlyMyItems(),
                "untradeable"
        );
        valueBasedItemParams = new LootingParameters(
                config.MinLootValue(),
                Integer.MAX_VALUE,
                10,
                1,
                0,
                false,
                config.LootOnlyMyItems()
        );
        seedItemParams = new LootingParameters(
                10,
                1,
                1,
                0,
                false,
                config.LootOnlyMyItems(),
                " seed"
        );
        herbItemParams = new LootingParameters(
                10,
                1,
                1,
                0,
                false,
                config.LootOnlyMyItems(),
                "Grimy "
        );
    }

    void HandleLooting(WarriorsGuildHelperConfig config)
    {
        //untradeableItemParams.setEatFoodForSpace(true);
        Rs2GroundItem.lootUntradables(untradeableItemParams);
        //valueBasedItemParams.setEatFoodForSpace(true);
        Rs2GroundItem.lootItemBasedOnValue(valueBasedItemParams);
        if (config.LootSeeds())
        {
            //seedItemParams.setEatFoodForSpace(true);
            Rs2GroundItem.lootItemsBasedOnNames(seedItemParams);
        }
        if (config.LootHerbs())
        {
            //herbItemParams.setEatFoodForSpace(true);
            Rs2GroundItem.lootItemsBasedOnNames(herbItemParams);
        }
    }

    void HandleCombat()
    {
        if (!Rs2Combat.inCombat())
        {
            //Microbot.log("We are NOT in combat.");
            // Not in combat. Pick a fight.
            // Check monsters that are already fighting the player
            if (!npcsInteractingWithPlayer.isEmpty())
            {
                //Microbot.log("NPCs interacting with us list has objects");
                Rs2NpcModel target = npcsInteractingWithPlayer.stream()
                        // Try and get an npc that matches our task if multiple things are interacting with us
                        .filter(npc -> npc.getWorldLocation() != null && Rs2Walker.canReach(npc.getWorldLocation())
                                && npc.getName() != null && npc.getName().toLowerCase().contains("cyclops"))
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
                //Microbot.log("TargetList: " + targetList);

                if (!targetList.isEmpty())
                {
                    Rs2NpcModel target = targetList.stream()
                            .filter(npc -> npc.getComposition() != null && Arrays.stream(npc.getComposition()
                                    .getActions()).anyMatch(action -> action != null && action.toLowerCase().contains("attack"))
                                    && Microbot.getClient() != null && Microbot.getClient().getLocalPlayer() != null
                                    && (npc.getInteracting() == null || npc.getInteracting() == Microbot.getClient().getLocalPlayer()))
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
    }

    public void TryAddNpcToTargetList(NPC _npc)
    {
        if (_npc == null) return;

        if (Objects.requireNonNull(_npc.getName()).contains("Cyclops"))
        {
            if (Microbot.getClient() != null && Microbot.getClient().getLocalPlayer() != null
                    && (_npc.getInteracting() == null || _npc.getInteracting() == Microbot.getClient().getLocalPlayer())
                    && _npc.getWorldLocation() != null && Rs2Walker.canReach(_npc.getWorldLocation())
                    && _npc.getComposition() != null && Arrays.stream(_npc.getComposition()
                    .getActions()).anyMatch(action -> action != null && action.toLowerCase().contains("attack")))
            {
                targetList.add(new Rs2NpcModel(_npc));
                //Microbot.log("NPC spawned and added to target list: " + _npc.getName());
            }
        }
    }

    public void TryRemoveNpcFromTargetList(NPC _npc)
    {
        boolean removed = targetList.removeIf(model -> model.getRuneliteNpc() == _npc);
        //if (removed) {
        //    Microbot.log("NPC despawned and removed from target list: " + _npc.getName());
        //}
    }

    void EatAndEscape(WarriorsGuildHelperConfig config)
    {
        // If we are out of food, and less than half health; Bank.
        double playerHealthPercent = (double) Rs2Player.getBoostedSkillLevel(Skill.HITPOINTS) / Rs2Player.getRealSkillLevel(Skill.HITPOINTS);
        if (playerHealthPercent < config.EatPercent()) {
            if (!Rs2Player.eatAt(config.EatPercent())) {
                // If FALSE then we didn't eat
                // Did we not eat because we don't have anymore food?
                // If so, we need to save ourselves
                if (Rs2Inventory.getInventoryFood().isEmpty()) {
                    // Handle emergency teleport/transitioning to banking state
                    if (playerHealthPercent < 0.5) {
                        // If we are below half health
                        Microbot.log("Health percent: " + playerHealthPercent);
                        Microbot.log("Escape!");
                        state = WarriorsGuildHelperState.GOING_TO_BANK;
                        return;
                    }
                }
            }
        }
    }

    public static WarriorsGuildDefenders GetCurrentDefender()
    {
        return currentDefender;
    }

    public static WarriorsGuildDefenders GetNextDefender()
    {
        return nextDefender;
    }

    public static WarriorsGuildHelperState GetCurrentState()
    {
        return state;
    }

    void UseUpstairsDoor()
    {
        WallObject door = Rs2GameObject.getWallObject(upstairsdoorId, new WorldPoint(2847, 3540, 2));
        if (door != null)
        {
            Rs2GameObject.interact(door);
            sleepGaussian(600, 100);
        }
        sleepUntil(() -> !Rs2Player.isMoving());
    }
}
