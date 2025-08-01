/*
 * Copyright (c) 2021, Obasill <https://github.com/Obasill>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.microbot.questhelper.helpers.achievementdiaries.wilderness;

import net.runelite.client.plugins.microbot.questhelper.bank.banktab.BankSlotIcons;
import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.ComplexStateQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.microbot.questhelper.requirements.ChatMessageRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.SkillRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.SpellbookRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.quest.QuestRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.Spellbook;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarplayerRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarPlayerID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WildernessElite extends ComplexStateQuestHelper
{
	// Items required
	ItemRequirement combatGear, hammer, lawRune, waterRune, lobsterPot, darkFishingBait, coins, pickaxe, axe,
		tinderbox, godEquip, coal, runeOre, magicLog, runeBar, rawDarkCrab;

	// Items recommended
	ItemRequirement food;

	// Quests required
	Requirement desertTreasure, enterGodwars, ancientBook, gatheredLogs, caughtCrab, runiteFromGolems, barsSmelted;

	Requirement notThreeBosses, notTPGhorrock, notDarkCrab, notRuneScim, notRoguesChest, notSpiritMage, notMagicLogs;

	QuestStep claimReward, tPGhorrock, darkCrab, cookDarkCrab, runeScim, roguesChest, magicLogs,
		burnLogs, moveToResource1, moveToResource2, moveToResource3, smeltBar, moveToGodWars1, moveToGodWars2;

	NpcStep threeBosses, spiritMage, runiteGolem;

	Zone resource, godWars1, godWars2;

	ZoneRequirement inResource, inGodWars2, inGodWars1;

	ConditionalStep threeBossesTask, tpGhorrockTask, darkCrabTask, runeScimTask, roguesChestTask, spiritMageTask, magicLogsTask;

	@Override
	public QuestStep loadStep()
	{
		initializeRequirements();
		setupSteps();

		ConditionalStep doElite = new ConditionalStep(this, claimReward);

		tpGhorrockTask = new ConditionalStep(this, tPGhorrock);
		doElite.addStep(notTPGhorrock, tpGhorrockTask);

		magicLogsTask = new ConditionalStep(this, moveToResource1);
		magicLogsTask.addStep(new Conditions(inResource, gatheredLogs, magicLog), burnLogs);
		magicLogsTask.addStep(inResource, magicLogs);
		doElite.addStep(notMagicLogs, magicLogsTask);

		runeScimTask = new ConditionalStep(this, moveToResource2);
		runeScimTask.addStep(new Conditions(inResource, runeBar.quantity(2), barsSmelted), runeScim);
		runeScimTask.addStep(new Conditions(inResource, runeOre.quantity(2), coal.quantity(16), runiteFromGolems), smeltBar);
		runeScimTask.addStep(inResource, runiteGolem);
		doElite.addStep(notRuneScim, runeScimTask);

		darkCrabTask = new ConditionalStep(this, moveToResource3);
		darkCrabTask.addStep(new Conditions(inResource, caughtCrab, rawDarkCrab), cookDarkCrab);
		darkCrabTask.addStep(inResource, darkCrab);
		doElite.addStep(notDarkCrab, darkCrabTask);

		roguesChestTask = new ConditionalStep(this, roguesChest);
		doElite.addStep(notRoguesChest, roguesChestTask);

		spiritMageTask = new ConditionalStep(this, moveToGodWars1);
		spiritMageTask.addStep(inGodWars1, moveToGodWars2);
		spiritMageTask.addStep(inGodWars2, spiritMage);
		doElite.addStep(notSpiritMage, spiritMageTask);

		threeBossesTask = new ConditionalStep(this, threeBosses);
		doElite.addStep(notThreeBosses, threeBossesTask);

		return doElite;
	}

	@Override
	protected void setupRequirements()
	{
		notThreeBosses = new VarplayerRequirement(VarPlayerID.WILDERNESS_ACHIEVEMENT_DIARY2, false, 3);
		notTPGhorrock = new VarplayerRequirement(VarPlayerID.WILDERNESS_ACHIEVEMENT_DIARY2, false, 5);
		notDarkCrab = new VarplayerRequirement(VarPlayerID.WILDERNESS_ACHIEVEMENT_DIARY2, false, 7);
		notRuneScim = new VarplayerRequirement(VarPlayerID.WILDERNESS_ACHIEVEMENT_DIARY2, false, 8);
		notRoguesChest = new VarplayerRequirement(VarPlayerID.WILDERNESS_ACHIEVEMENT_DIARY2, false, 9);
		notSpiritMage = new VarplayerRequirement(VarPlayerID.WILDERNESS_ACHIEVEMENT_DIARY2, false, 10);
		notMagicLogs = new VarplayerRequirement(VarPlayerID.WILDERNESS_ACHIEVEMENT_DIARY2, false, 11);

		ancientBook = new SpellbookRequirement(Spellbook.ANCIENT);

		combatGear = new ItemRequirement("Combat gear", -1, -1).isNotConsumed();
		combatGear.setDisplayItemId(BankSlotIcons.getCombatGear());
		hammer = new ItemRequirement("Hammer", ItemID.HAMMER).showConditioned(notRuneScim).isNotConsumed();
		lawRune = new ItemRequirement("Law rune", ItemID.LAWRUNE).showConditioned(notTPGhorrock);
		waterRune = new ItemRequirement("Water rune", ItemID.WATERRUNE).showConditioned(notTPGhorrock);
		lobsterPot = new ItemRequirement("Lobster pot", ItemID.LOBSTER_POT).showConditioned(notDarkCrab).isNotConsumed();
		darkFishingBait = new ItemRequirement("Dark fish bait", ItemID.WILDERNESS_FISHING_BAIT).showConditioned(notDarkCrab);
		coins = new ItemRequirement("Coins", ItemCollections.COINS).showConditioned(new Conditions(LogicType.OR,
			notDarkCrab, notMagicLogs, notRuneScim));
		pickaxe = new ItemRequirement("Any pickaxe", ItemCollections.PICKAXES).showConditioned(notRuneScim).isNotConsumed();
		axe = new ItemRequirement("Any axe", ItemCollections.AXES).showConditioned(notMagicLogs).isNotConsumed();
		tinderbox = new ItemRequirement("Tinderbox", ItemID.TINDERBOX).showConditioned(notMagicLogs).isNotConsumed();
		godEquip = new ItemRequirement("Various god equipment (1 of each god suggested)", -1, -1)
			.showConditioned(notSpiritMage).isNotConsumed();
		coal = new ItemRequirement("Coal", ItemID.COAL).showConditioned(notRuneScim);
		runeOre = new ItemRequirement("Runite ore", ItemID.RUNITE_ORE);
		runeBar = new ItemRequirement("Runite bar", ItemID.RUNITE_BAR);
		magicLog = new ItemRequirement("Magic logs", ItemID.MAGIC_LOGS);
		rawDarkCrab = new ItemRequirement("Raw dark crab", ItemID.RAW_DARK_CRAB);

		food = new ItemRequirement("Food", ItemCollections.GOOD_EATING_FOOD, -1);

		enterGodwars = new ItemRequirement("60 Strength or Agility", -1, -1);

		inResource = new ZoneRequirement(resource);
		inGodWars1 = new ZoneRequirement(godWars1);
		inGodWars2 = new ZoneRequirement(godWars2);

		gatheredLogs = new ChatMessageRequirement(
			inResource,
			"<col=0040ff>Achievement Diary Stage Task - Current stage: 1.</col>"
		);
		((ChatMessageRequirement) gatheredLogs).setInvalidateRequirement(
			new ChatMessageRequirement(
				new Conditions(LogicType.NOR, inResource),
				"<col=0040ff>Achievement Diary Stage Task - Current stage: 1.</col>"
			)
		);

		caughtCrab = new ChatMessageRequirement(
			inResource,
			"<col=0040ff>Achievement Diary Stage Task - Current stage: 1.</col>"
		);
		((ChatMessageRequirement) caughtCrab).setInvalidateRequirement(
			new ChatMessageRequirement(
				new Conditions(LogicType.NOR, inResource),
				"<col=0040ff>Achievement Diary Stage Task - Current stage: 1.</col>"
			)
		);

		barsSmelted = new ChatMessageRequirement(
			inResource,
			"<col=0040ff>Achievement Diary Stage Task - Current stage: 4.</col>"
		);
		((ChatMessageRequirement) caughtCrab).setInvalidateRequirement(
			new ChatMessageRequirement(
				new Conditions(LogicType.NOR, inResource),
				"<col=0040ff>Achievement Diary Stage Task - Current stage: 4.</col>"
			)
		);

		runiteFromGolems = new ChatMessageRequirement(
			inResource,
			"<col=0040ff>Achievement Diary Stage Task - Current stage: 2.</col>"
		);
		((ChatMessageRequirement) caughtCrab).setInvalidateRequirement(
			new ChatMessageRequirement(
				new Conditions(LogicType.NOR, inResource),
				"<col=0040ff>Achievement Diary Stage Task - Current stage: 2.</col>"
			)
		);

		desertTreasure = new QuestRequirement(QuestHelperQuest.DESERT_TREASURE, QuestState.FINISHED);
	}

	@Override
	protected void setupZones()
	{
		resource = new Zone(new WorldPoint(3174, 3944, 0), new WorldPoint(3196, 3924, 0));
		godWars1 = new Zone(new WorldPoint(3046, 10177, 3), new WorldPoint(3076, 10138, 3));
		godWars2 = new Zone(new WorldPoint(3014, 10168, 0), new WorldPoint(3069, 10115, 0));
	}

	public void setupSteps()
	{
		moveToResource1 = new ObjectStep(this, ObjectID.WILDERNESS_RESOURCE_GATE, new WorldPoint(3184, 3944, 0),
			"Enter the Wilderness Resource Area.", coins.quantity(6000), axe, tinderbox);
		magicLogs = new ObjectStep(this, ObjectID.MAGICTREE, new WorldPoint(3190, 3926, 0),
			"Chop some magic logs.", true, axe, tinderbox);
		burnLogs = new ItemStep(this, "Burn the magic logs in the Resource Area.", tinderbox.highlighted(),
			magicLog.highlighted());

		moveToResource2 = new ObjectStep(this, ObjectID.WILDERNESS_RESOURCE_GATE, new WorldPoint(3184, 3944, 0),
			"Enter the Wilderness Resource Area.", coins.quantity(6000), combatGear, food, pickaxe, coal.quantity(16), hammer);
		runiteGolem = new NpcStep(this, NpcID.WILDERNESS_RUNE_GOLEM, new WorldPoint(3189, 3938, 0),
			"Kill and mine the Runite Golems in the Resource Area.", true, combatGear, food,
			pickaxe, coal.quantity(16), hammer);
		runiteGolem.addAlternateNpcs(NpcID.WILDERNESS_GOLEM_RUNE_ROCK);
		smeltBar = new ObjectStep(this, ObjectID.WILDERNESS_RESOURCE_FURNACE, new WorldPoint(3191, 3936, 0),
			"Smelt the ore into runite bars.", hammer, runeOre.quantity(2), coal.quantity(16), hammer);
		runeScim = new ObjectStep(this, ObjectID.ANVIL, new WorldPoint(3190, 3938, 0),
			"Smith a runite scimitar in the Resource Area.", hammer, runeBar.quantity(2));

		moveToResource3 = new ObjectStep(this, ObjectID.WILDERNESS_RESOURCE_GATE, new WorldPoint(3184, 3944, 0),
			"Enter the Wilderness Resource Area.", coins.quantity(6000), lobsterPot, darkFishingBait);
		darkCrab = new NpcStep(this, NpcID._49_61_CRABS, new WorldPoint(3187, 3927, 0),
			"Fish a dark crab in the Resource Area.", lobsterPot, darkFishingBait);
		cookDarkCrab = new ObjectStep(this, ObjectID.FIRE, new WorldPoint(3188, 3930, 0),
			"Cook the raw dark crab on the nearby fire.", rawDarkCrab);

		tPGhorrock = new DetailedQuestStep(this, "Teleport to Ghorrock.", ancientBook, lawRune.quantity(2),
			waterRune.quantity(8));

		moveToGodWars1 = new ObjectStep(this, ObjectID.WILDERNESS_GWD_ENTRANCE, new WorldPoint(3018, 3739, 0),
			"Enter the Wilderness God Wars Dungeon.", combatGear, food, godEquip);
		moveToGodWars2 = new ObjectStep(this, ObjectID.WILDERNESS_GWD_CREVICE, new WorldPoint(3066, 10142, 0),
			"Use the crevice to enter the Wilderness God Wars Dungeon. The Strength entrance is to the west.",
			combatGear, food, godEquip);
		spiritMage = new NpcStep(this, NpcID.GODWARS_SPIRITUAL_SARADOMIN_MAGE, new WorldPoint(3050, 10131, 0),
			"Kill a Spiritual Mage in the Wilderness God Wars Dungeon.", combatGear, food, godEquip);
		spiritMage.addAlternateNpcs(NpcID.GODWARS_SPIRITUAL_BANDOS_MAGE, NpcID.GODWARS_SPIRITUAL_ZAMORAK_MAGE, NpcID.GODWARS_SPIRITUAL_ARMADYL_MAGE);

		threeBosses = new NpcStep(this, NpcID.CLANCUP_CALLISTO, new WorldPoint(3291, 3844, 0),
			"Kill Callisto, Venenatis, and Vet'ion. You must complete this task fully before continuing the other " +
				"tasks.", combatGear, food);
		threeBosses.addAlternateNpcs(NpcID.VETION, NpcID.CLANCUP_VENENATIS);

		roguesChest = new ObjectStep(this, ObjectID.WILDERNESS_ROGUE_CHEST, new WorldPoint(3297, 3940, 0),
			"Steal from the chest in Rogues' Castle.");

		claimReward = new NpcStep(this, NpcID.LESSER_FANATIC_DIARY, new WorldPoint(3121, 3518, 0),
			"Talk to Lesser Fanatic in Edgeville to claim your reward!");
		claimReward.addDialogStep("I have a question about my Achievement Diary.");
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(combatGear, hammer, lawRune.quantity(2), waterRune.quantity(8), coins.quantity(3750), axe,
			tinderbox, pickaxe, coal.quantity(16), lobsterPot, darkFishingBait, godEquip);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Collections.singletonList(food);
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		List<Requirement> reqs = new ArrayList<>();
		reqs.add(enterGodwars);
		reqs.add(new SkillRequirement(Skill.COOKING, 90, true));
		reqs.add(new SkillRequirement(Skill.FIREMAKING, 75, true));
		reqs.add(new SkillRequirement(Skill.FISHING, 85, true));
		reqs.add(new SkillRequirement(Skill.MAGIC, 96, true));
		reqs.add(new SkillRequirement(Skill.MINING, 85, true));
		reqs.add(new SkillRequirement(Skill.SLAYER, 83, true));
		reqs.add(new SkillRequirement(Skill.SMITHING, 90, true));
		reqs.add(new SkillRequirement(Skill.THIEVING, 84, true));
		reqs.add(new SkillRequirement(Skill.WOODCUTTING, 75, true));

		reqs.add(desertTreasure);

		return reqs;
	}

	@Override
	public List<String> getCombatRequirements()
	{
		return Arrays.asList("Callisto (lvl 470)", "Venenatis (lvl 464)", "Vet'ion (lvl 454)",
			"Runite Golem (lvl 178)", "Spiritual Mage (lvl 121)");
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Arrays.asList(
			new ItemReward("Wilderness Sword 4", ItemID.WILDERNESS_SWORD_ELITE),
			new ItemReward("50,000 Exp. Lamp (Any skill over 70)", ItemID.THOSF_REWARD_LAMP)
		);
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
			new UnlockReward("Unlimited free teleports to the Fountain of Rune on the Wilderness Sword 4"),
			new UnlockReward("Free entry to the Resource Area"),
			new UnlockReward("All dragon bones drops in the Wilderness are noted. (Note: This does not include the King Black Dragon, as his lair is not Wilderness affected.)"),
			new UnlockReward("Noted lava dragon bones can be toggled by speaking to the Lesser Fanatic."),
			new UnlockReward("200 random free runes from Lundail once per day"),
			new UnlockReward("Increased dark crab catch rate")
		);
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();

		PanelDetails tpSteps = new PanelDetails("Teleport to Ghorrock", Collections.singletonList(tPGhorrock),
			new SkillRequirement(Skill.MAGIC, 96, true), desertTreasure, ancientBook, lawRune.quantity(2),
			waterRune.quantity(8));
		tpSteps.setDisplayCondition(notTPGhorrock);
		tpSteps.setLockingStep(tpGhorrockTask);
		allSteps.add(tpSteps);

		PanelDetails magicSteps = new PanelDetails("Chop and Burn Magic Logs", Arrays.asList(moveToResource1, magicLogs,
			burnLogs), new SkillRequirement(Skill.FIREMAKING, 75, true), new SkillRequirement(Skill.WOODCUTTING, 75, true),
			coins.quantity(6000), axe, tinderbox);
		magicSteps.setDisplayCondition(notMagicLogs);
		magicSteps.setLockingStep(magicLogsTask);
		allSteps.add(magicSteps);

		PanelDetails scimSteps = new PanelDetails("Rune Scimitar in Resource Area",
			Arrays.asList(moveToResource2, runiteGolem, smeltBar, runeScim), new SkillRequirement(Skill.MINING, 85, true),
			new SkillRequirement(Skill.SMITHING, 90, true), coins.quantity(6000), combatGear, food, pickaxe,
			coal.quantity(16), hammer);
		scimSteps.setDisplayCondition(notRuneScim);
		scimSteps.setLockingStep(runeScimTask);
		allSteps.add(scimSteps);

		PanelDetails crabSteps = new PanelDetails("Dark Crab in Resource Area", Arrays.asList(moveToResource3, darkCrab,
			cookDarkCrab), new SkillRequirement(Skill.COOKING, 90, true), new SkillRequirement(Skill.FISHING, 85, true),
			coins.quantity(6000), lobsterPot, darkFishingBait);
		crabSteps.setDisplayCondition(notDarkCrab);
		crabSteps.setLockingStep(darkCrabTask);
		allSteps.add(crabSteps);

		PanelDetails chestSteps = new PanelDetails("Rogues' Castle Chest", Collections.singletonList(roguesChest));
		chestSteps.setDisplayCondition(notRoguesChest);
		chestSteps.setLockingStep(roguesChestTask);
		allSteps.add(chestSteps);

		PanelDetails mageSteps = new PanelDetails("Spiritual Mage", Arrays.asList(moveToGodWars1, moveToGodWars2,
			spiritMage), enterGodwars, combatGear, food, godEquip);
		mageSteps.setDisplayCondition(notSpiritMage);
		mageSteps.setLockingStep(spiritMageTask);
		allSteps.add(mageSteps);

		PanelDetails bossSteps = new PanelDetails("Three bosses", Collections.singletonList(threeBosses), combatGear,
			food);
		bossSteps.setDisplayCondition(notThreeBosses);
		bossSteps.setLockingStep(threeBossesTask);
		allSteps.add(bossSteps);

		allSteps.add(new PanelDetails("Finishing off", Collections.singletonList(claimReward)));

		return allSteps;
	}
}
