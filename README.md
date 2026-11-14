# Assignment 3: Haunted University Basement

## Overview

We tested the supplied Haunted University Basement Java project using JUnit 5, EclEmma, and Pitest. Our combined suite includes regular and parameterized tests covering successful operations, invalid inputs, failure conditions, and boundary cases. The supplied game implementation was preserved.

## Team contributions

- **Yesenia Salbador:** Initial dungeon setup, movement, room connections, traps, quests, equipment and unlocking interactions, game win/loss conditions, and integration tests. Combined the project and exported the final coverage and mutation reports.
- **Lauren:** Combat, inventory, loot, monster combat, player health and healing, and potion tests.

## Requirements

- JDK 21
- Apache Maven
- For the Eclipse workflow: Eclipse with Maven support and EclEmma

JUnit 5 and the required test plugins are configured in `pom.xml`. Dependency declarations include comments explaining their purpose.

## Project layout

The Maven project is inside `haunted-university-basement/`:

- `pom.xml`: Build, test dependencies, and report configuration.
- `src/main/java/`: Supplied game implementation.
- `src/test/java/`: Combined JUnit 5 test suite.
- `reports/eclemma-final/`: Final exported EclEmma HTML report.
- `reports/pitest-final/`: Final Pitest HTML, CSV, and XML reports.

The other report directories contain earlier reports. Use the directories ending in `-final` for the final combined results.

## Run the tests with Maven

Open a terminal in `haunted-university-basement`, the directory containing `pom.xml`, and run:

```bash
mvn clean test
```

Maven downloads the configured dependencies, compiles the project, and runs the tests. Test results are generated in `target/surefire-reports/`; the configured JaCoCo report is generated in `target/site/jacoco/`.

## Run the tests in Eclipse

1. Choose **File > Import > Maven > Existing Maven Projects**.
2. Select the project directory containing `pom.xml` and finish the import.
3. Let Eclipse resolve the Maven dependencies.
4. Right-click `src/test/java` and select **Run As > JUnit Test** to run the combined suite.

## Generate EclEmma coverage

1. Right-click `src/test/java` and select **Coverage As > JUnit Test**.
2. In the Coverage view, expand the project and inspect `src/main/java` for production-code coverage.
3. Export the coverage session as an **HTML report** to `reports/eclemma-final/`.

Open `reports/eclemma-final/index.html` in a browser to view the submitted export. The overall session includes both production and test classes, so its overall percentage differs from the production-code percentage.

## Generate Pitest mutation reports

From the directory containing `pom.xml`, run:

```bash
mvn clean test org.pitest:pitest-maven:mutationCoverage
```

Pitest generates HTML, CSV, and XML reports in `target/pit-reports/`. The final submitted copy is in `reports/pitest-final/`.

For Eclipse, right-click the project, select **Run As > Maven build...**, and enter the following in **Goals**:

```text
clean test org.pitest:pitest-maven:mutationCoverage
```

Click **Run** in that configuration window.

## Final combined results

| Measure | Result |
| --- | --- |
| JUnit test files | 17 |
| Tests executed | 196 |
| Test failures / errors | 0 / 0 |
| EclEmma production instruction coverage | 99.5% |
| Pitest mutations generated | 250 |
| Mutations killed | 247 |
| Mutation kill rate | 98.8% (displayed as 99% by Pitest) |
| Surviving mutations | 2 |
| Mutations with no coverage | 1 |

The final Maven mutation-analysis run completed with **BUILD SUCCESS**. Coverage indicates which code executed during testing; mutation testing checks whether the tests detect changes to that code.

## View the final reports

- EclEmma: `haunted-university-basement/reports/eclemma-final/index.html`
- Pitest HTML: `haunted-university-basement/reports/pitest-final/index.html`
- Pitest CSV: `haunted-university-basement/reports/pitest-final/mutations.csv`
- Pitest XML: `haunted-university-basement/reports/pitest-final/mutations.xml`

Keep each HTML report with its supporting folders and assets so its links and formatting work when opened locally.

## AI Prompt Documentation

We used ChatGPT to help understand the game, plan and write tests, and troubleshoot testing issues. Below are the prompts we collected for each person's part.

### Lauren's prompts

Areas: combat, inventory, loot, monster and boss combat, player health and healing, and potion tests.

#### Test planning and implementation

1. "I need to create tests for combat, player health and healing, monster and boss behavior, and loot. How should I organize these tests into classes while following the project guidelines?"
2. "Using these parameters, help me implement test methods that follow our project guidelines and the topics we have discussed."
3. "Here is what I currently have for CombatTest. Does it align with the project guidelines and the topics we have discussed?"

#### JUnit testing and debugging

4. "I have implemented the test classes as discussed and ran `mvn clean test`, but the build reports that zero tests were run and no test classes were identified. Could my `pom.xml` be configured incorrectly?"
5. "I now need to create tests for inventory limits, picking up and using items, and potions. These tests will expand our existing coverage and should help improve our mutation kill rate by testing more methods."

#### PIT mutation testing

6. "I need to configure PIT for PlayerHealthTest and MonsterCombatTest, then review the mutation report to identify surviving mutations and determine which tests could improve the mutation kill rate."
7. "PIT ran all of the tests instead of only the test classes I was targeting, and it did not generate the reports. Can you review my `pom.xml` and help me identify why?"

### Yesenia's prompts

Areas: dungeon setup, movement, room connections, traps, quests, equipment and unlocking, win/loss conditions, integration tests, and the combined reports.

1. "Here are the classes in the `model` and `config` packages: Player, Room, Monster, BossMonster, Item, Weapon, Armor, Potion, Key, QuestItem, Inventory, Quest, Trap, and DungeonFactory. Explain each class and how they relate to each other."
2. "Walk through the dungeon factory in the `config` package and show me how to set up a small game world in a JUnit `@BeforeEach`."
3. "Write JUnit 5 tests for moving between rooms: a valid exit, no exit, and a locked room with and without the key."
4. "Write tests for quest progression and the win condition: Lost Gradebook recovered and Final Exam Phantom defeated, plus the cases where only one is true."
