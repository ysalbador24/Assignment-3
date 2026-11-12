package com.example.haunted.combat.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.haunted.engine.CombatEngine;
import com.example.haunted.events.CombatResult;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;
import com.example.haunted.model.Quest;
import com.example.haunted.model.Room;
import com.example.haunted.rules.DamageCalculator;
import com.example.haunted.rules.QuestTracker;

class combatTest {

	private Player player;
	private Monster monster;
	private CombatEngine combatEngine;
	private Quest quest;
	private Room room;

	@BeforeEach
	void setUp() {
		player = new Player("Bob", 100, 10, 5, new Inventory(10));
		
		monster = new Monster("Zombie", 50, 10, 5, new ArrayList<>());
		
		combatEngine = new CombatEngine(new DamageCalculator(), new QuestTracker());
		
		quest = new Quest("Main Quest", "Defeat the Final Exam Phantom and recover the Lost Gradebook.");
		
		room = new Room("R1", "Basement", "A dark basement.");
		player.setCurrentRoom(room);
	}

	// Attack succeeds and monster receives damage
	@Test
	void playerSuccessfullyAttacksMonster() {
		combatEngine.attack(player, quest, monster);
		
		assertEquals(45, monster.getHealth());
	}

	// Monster damages player after surviving
	@Test
	void monsterCounterattacksWhenStillAlive() {
		combatEngine.attack(player, quest, monster);

		assertEquals(95, player.getHealth());
	}

	@Test
	// Defeating monster prevents counterattack
	void monsterDoesNotCounterattackWhenDefeated() {
		monster.takeDamage(49);

		CombatResult result = combatEngine.attack(player, quest, monster);

		assertTrue(result.isMonsterDefeated());
		assertEquals(100, player.getHealth()); // player health should not change
		assertEquals(0, monster.getHealth());
	}

	@Test
	// Attack is rejected
	void cannotAttackAlreadyDefeatedMonster() {
		monster.takeDamage(100);

		CombatResult result = combatEngine.attack(player, quest, monster);

		assertFalse(result.isSuccess());
		assertEquals(0, monster.getHealth());
		assertEquals(100, player.getHealth());
	}

	@Test
	// Dead player cannot attack
	void cannotAttackWhenPlayerIsDead() {
		player.takeDamage(100);

		CombatResult result = combatEngine.attack(player, quest, monster);

		assertFalse(result.isSuccess());
		assertEquals(50, monster.getHealth());
	}

	@Test
	// Missing monster is handled
	void cannotAttackNullMonster() {
		CombatResult result = combatEngine.attack(player, quest, null);
		
		assertFalse(result.isSuccess());
		assertEquals("Monster not found.", result.getMessage());
	}

}