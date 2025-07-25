/*
 *
 *  * Copyright (c) 2021, Senmori
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice, this
 *  *    list of conditions and the following disclaimer.
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.runelite.client.plugins.microbot.questhelper.helpers.quests.fishingcontest;

import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirements;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.SkillRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.quest.QuestRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarbitRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.ConditionalStep;
import net.runelite.client.plugins.microbot.questhelper.steps.NpcStep;
import net.runelite.client.plugins.microbot.questhelper.steps.ObjectStep;
import net.runelite.client.plugins.microbot.questhelper.steps.QuestStep;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

import static net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicHelper.and;
import static net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicHelper.nor;

public class FishingContest extends BasicQuestHelper
{
	// Required
	ItemRequirement coins, fishingPass, garlic, fishingRod, spade, redVineWorm, winningFish, trophy;

	// Recommended
	ItemRequirement combatBracelet, camelotTeleport, food;
	QuestRequirement fairyRingAccess;

	// Steps
	QuestStep talkToVestriStep, getGarlic, goToMcGruborWood, goToRedVine, grandpaJack, runToJack, goToHemenster, teleToHemenster;
	QuestStep putGarlicInPipe, speakToBonzo, fishForFish, speakToBonzoWithFish, speaktoVestri;

	ConditionalStep goToHemensterStep, getWorms, fishNearPipes;

	ItemRequirement noCombatBracelet, noFishingRod, noWorms;
	Requirement hasEverything, notNearWorms, inWoods, notInWoods, enteredContestArea, hasPutGarlicInPipe, needsGarlic;

	// Zones
	Zone mcGruborWoodEntrance, cmbBraceletTeleportZone, nearRedVineWorms, contestGroundsEntrance;
	ZoneRequirement passedThroughMcGruborEntrance, atCmbBraceletTeleportZone, onContestGrounds;

	@Override
	protected void setupZones()
	{
		mcGruborWoodEntrance = new Zone(new WorldPoint(2662, 3500, 0));
		passedThroughMcGruborEntrance = new ZoneRequirement(mcGruborWoodEntrance);

		cmbBraceletTeleportZone = new Zone(new WorldPoint(2651, 3444, 0), new WorldPoint(2657, 3439, 0));
		atCmbBraceletTeleportZone = new ZoneRequirement(cmbBraceletTeleportZone);

		nearRedVineWorms = new Zone(new WorldPoint(2634, 3491, 0), new WorldPoint(2626, 3500, 0));

		// the contest grounds are from where the fence turns north-westa, to the square before the willow tree on the coast
		contestGroundsEntrance = new Zone(new WorldPoint(2642, 3445, 0), new WorldPoint(2631, 3434, 0));
		// the 3 tiles east of the pipe, to ensure we have the whole area
		Zone tilesEastOfPipe = new Zone(new WorldPoint(2641, 3446, 0), new WorldPoint(2638, 3446, 0));
		onContestGrounds = new ZoneRequirement(contestGroundsEntrance, tilesEastOfPipe);
	}

	@Override
	protected void setupRequirements()
	{
		coins = new ItemRequirement("Coins", ItemCollections.COINS, 5);
		coins.setTooltip("10 if you buy a fishing rod from Jack");
		fishingPass = new ItemRequirement("Fishing Pass", ItemID.FISHING_COMPETITION_PASS);
		fishingPass.setTooltip("This can be obtained during the quest\n\nIf you lose this you can get another from Vestri.");
		garlic = new ItemRequirement("Garlic", ItemID.GARLIC);
		garlic.setTooltip("This can be obtained during the quest.");
		garlic.setHighlightInInventory(true);
		fishingRod = new ItemRequirement("Fishing Rod", ItemID.FISHING_ROD).isNotConsumed();
		fishingRod.setTooltip("This can be obtained during the quest for 5gp.");
		spade = new ItemRequirement("Spade", ItemID.SPADE).isNotConsumed();
		redVineWorm = new ItemRequirement("Red Vine Worm", ItemID.RED_VINE_WORM, 1);
		redVineWorm.setTooltip("This can be obtained during the quest.");
		food = new ItemRequirement("Food for low levels", ItemCollections.GOOD_EATING_FOOD, -1);
		winningFish = new ItemRequirement("Raw Giant Carp", ItemID.RAW_GIANT_CARP);
		winningFish.setHighlightInInventory(true);
		trophy = new ItemRequirement("Fishing Trophy", ItemID.HEMENSTER_FISHING_TROPHY);
		trophy.setHighlightInInventory(true);
		trophy.setTooltip("You can get another from Bonzo in Hemenster if you lost this.");

		// Recommended
		combatBracelet = new ItemRequirement("Combat Bracelet", ItemCollections.COMBAT_BRACELETS);
		combatBracelet.setHighlightInInventory(true);
		combatBracelet.setTooltip("Highly recommended!");
		camelotTeleport = new ItemRequirement("Camelot Teleport", ItemID.POH_TABLET_CAMELOTTELEPORT);
		fairyRingAccess = new QuestRequirement(QuestHelperQuest.FAIRYTALE_II__CURE_A_QUEEN, QuestState.IN_PROGRESS,
			"Fairy ring access");
		fairyRingAccess.setTooltip(QuestHelperQuest.FAIRYTALE_II__CURE_A_QUEEN.getName() + " is required to at least be started in order to use fairy rings");
	}

	public void setupSteps()
	{
		talkToVestriStep = new NpcStep(this, NpcID.TUNNEL_DWARF1, new WorldPoint(2821, 3486, 0), "Talk to Vestri just north of Catherby.");
		talkToVestriStep.addDialogStep("I was wondering what was down those stairs?");
		talkToVestriStep.addDialogStep("Why not?");
		talkToVestriStep.addDialogStep("If you were my friend I wouldn't mind it.");
		talkToVestriStep.addDialogStep("Well, let's be friends!");
		talkToVestriStep.addDialogStep("And how am I meant to do that?");
		talkToVestriStep.addDialogStep("Yes.");

		getGarlic = new ObjectStep(this, ObjectID.KR_SEERS_TABLE2, new WorldPoint(2714, 3478, 0), "");
		getGarlic.setText("Pick the garlic up on the table in Seers' Village.");
		getGarlic.addText("If it is not there it spawns about every 30 seconds.");

		goToMcGruborWood = new ObjectStep(this, ObjectID.MCGRUBORLOOSERAILING, new WorldPoint(2662, 3500, 0), "", spade);
		goToMcGruborWood.setText("Enter McGrubor's Woods via the northern entrance.");
		goToMcGruborWood.addDialogStep("Be careful of the Guard Dogs (level 44). They are aggressive!");

		goToRedVine = new ObjectStep(this, ObjectID.RED_WORM_JUNCTION, new WorldPoint(2631, 3496, 0), "", spade);
		goToRedVine.setText("Use your spade on the red vines to gather 1 Red Vine Worm.");
		goToRedVine.addIcon(ItemID.SPADE);
		((ObjectStep)goToRedVine).addAlternateObjects(ObjectID.RED_WORM_CORNER, ObjectID.RED_WORM_DIAG1, ObjectID.RED_WORM_DIAG3, ObjectID.RED_WORM_END_DIAG);
		((ObjectStep)goToRedVine).addAlternateObjects(ObjectID.RED_WORM_VINE, ObjectID.RED_WORM_DIAGFILLER, ObjectID.RED_WORM_END);

		goToHemenster = new ObjectStep(this, ObjectID.FISHINGGATECLOSEDR, new WorldPoint(2642, 3441, 0), "Enter Hemenster with your fishing pass.");
		((ObjectStep)goToHemenster).addAlternateObjects(ObjectID.FISHINGGATECLOSEDL);
		grandpaJack = new NpcStep(this, NpcID.GRANDPA_JACK, new WorldPoint(2649, 3451, 0), "Talk to Grandpa Jack to get a fishing rod.");
		grandpaJack.addDialogStep("Can I buy one of your fishing rods?");
		grandpaJack.addDialogStep("Very fair, I'll buy that rod!");

		putGarlicInPipe = new ObjectStep(this, ObjectID.GARLICPIPE, new WorldPoint(2638, 3446, 0), "Put garlic in the pipes.", garlic);
		putGarlicInPipe.addIcon(ItemID.GARLIC);

		speakToBonzo = new NpcStep(this, NpcID.BONZO, new WorldPoint(2641, 3437, 0), "Speak to Bonzo to start the competition.", coins);
		speakToBonzo.addDialogStep("I'll enter the competition please.");

		speakToBonzoWithFish = new NpcStep(this, NpcID.BONZO, new WorldPoint(2641, 3437, 0), "", winningFish);
		speakToBonzoWithFish.setText("Speak to Bonzo again after you have caught the winning fish.");
		speakToBonzoWithFish.addDialogStep("I have this big fish. Is it enough to win?");

		fishForFish = new NpcStep(this, NpcID._0_41_53_SINISTERFISHSPOT, new WorldPoint(2637, 3444, 0),
			"Catch the winning fish at the fishing spot near the pipes.", fishingRod, redVineWorm);
		fishNearPipes = new ConditionalStep(this, fishForFish);
		fishNearPipes.addStep(winningFish, speakToBonzoWithFish);

		teleToHemenster = new NpcStep(this, NpcID.GRANDPA_JACK, "", coins);
		teleToHemenster.addText("\nTeleport to Hemenster via the combat bracelet.\n\nSpeak to Grandpa Jack to buy a fishing rod.");
		teleToHemenster.addDialogStep("Ranging Guild");
		teleToHemenster.addDialogStep("Can I buy one of your fishing rods?");
		teleToHemenster.addDialogStep("Very fair, I'll buy that rod!");

		runToJack = new NpcStep(this, NpcID.GRANDPA_JACK, new WorldPoint(2649, 3451, 0), "", coins);
		runToJack.setText("Speak to Grandpa Jack to get a fishing rod.\nYou can leave McGrubor's Woods via the northern entrance.");
		runToJack.addDialogStep("Can I buy one of your fishing rods?");
		runToJack.addDialogStep("Very fair, I'll buy that rod!");

		speaktoVestri = new NpcStep(this, NpcID.TUNNEL_DWARF1, new WorldPoint(2821, 3486, 0),
			"Bring Vestri just north of Catherby the trophy.", trophy);

		goToHemensterStep = new ConditionalStep(this, goToHemenster, "Enter Hemenster with your fishing pass.");
		goToHemensterStep.addDialogStep("Ranging Guild");

		getWorms = new ConditionalStep(this, goToRedVine, "Gather 1 Red Vine Worm in McGrubor's Woods.");
		getWorms.addStep(new Conditions(noWorms, notNearWorms, notInWoods), goToMcGruborWood);
		getWorms.addStep(inWoods, goToRedVine);
	}

	public void setupConditions()
	{
		noCombatBracelet = new ItemRequirements(LogicType.NOR, "", combatBracelet);
		noFishingRod = new ItemRequirements(LogicType.NOR, "", fishingRod);

		noWorms = new ItemRequirements(LogicType.NOR, "", redVineWorm);

		// Conditions
		notNearWorms = new Conditions(LogicType.NOR, new ZoneRequirement(nearRedVineWorms));
		inWoods = new Conditions(true, passedThroughMcGruborEntrance); // passed through northern entrance
		notInWoods = new Conditions(LogicType.NOR, inWoods);

		// 2051 0->1 also set for garlic in pipe
		hasPutGarlicInPipe = new VarbitRequirement(2054, 1);
		needsGarlic = and(LogicHelper.nor(hasPutGarlicInPipe), new ItemRequirements(LogicType.NOR, "", garlic));
		hasEverything = new Conditions(LogicHelper.nor(needsGarlic), redVineWorm, fishingRod);
		enteredContestArea = new Conditions(hasEverything, onContestGrounds);
	}

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();

		Map<Integer, QuestStep> steps = new HashMap<>();

		goToHemensterStep.addStep(needsGarlic, getGarlic);
		goToHemensterStep.addStep(noWorms, getWorms);
		goToHemensterStep.addStep(fishingRod, goToHemenster);
		goToHemensterStep.addStep(new Conditions(noCombatBracelet, redVineWorm, noFishingRod), runToJack);
		goToHemensterStep.addStep(new Conditions(combatBracelet, redVineWorm, noFishingRod), teleToHemenster);
		goToHemensterStep.addStep(new Conditions(noFishingRod, redVineWorm), grandpaJack);

		ConditionalStep goEnterCompetition = new ConditionalStep(this, goToHemensterStep);
		goEnterCompetition.addStep(enteredContestArea, speakToBonzo);
		steps.put(0, talkToVestriStep);
		steps.put(1, goEnterCompetition);
		steps.put(2, putGarlicInPipe);
		steps.put(3, fishNearPipes);
		steps.put(4, speaktoVestri);
		return steps;
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(1);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Collections.singletonList(new ExperienceReward(Skill.FISHING, 2437));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
				new UnlockReward("Access to the underground White Wolf Mountain passage"),
				new UnlockReward("Ability to catch minnows in The Fishing Guild."));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> panels = new ArrayList<>();
		List<QuestStep> steps = Arrays.asList(talkToVestriStep, goToHemensterStep, speakToBonzo, putGarlicInPipe, fishForFish,
			speakToBonzoWithFish, speaktoVestri);
		PanelDetails fisingContest = new PanelDetails("Fishing Contest", steps, fishingRod, garlic, coins, redVineWorm, spade);
		panels.add(fisingContest);
		return panels;
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		return Collections.singletonList(new SkillRequirement(Skill.FISHING, 10));
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(coins, redVineWorm, garlic, spade, fishingRod);
	}


	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(combatBracelet, camelotTeleport);
	}
}
