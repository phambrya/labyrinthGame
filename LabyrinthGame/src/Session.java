import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

/* Session
 *
 * The setup and mechanics of
 * the current game session.
 */
public final class Session {
    // Constants

    //    MAX_PLAYERS: The maximum player count.
    private final static int MAX_PLAYERS = 2;

    //    RENEWAL_CHANCE: The chance of a player being renewed each turn.
    private final static int RENEWAL_CHANCE = 15;

    //    TELEPORT_DISTANCE: How far to teleport the loser of a Player combat encounter away.
    private final static int TELEPORT_DISTANCE = 3;

    //    ONE_HUNDRED: One-hundred, used for random percentages.
    private final static int ONE_HUNDRED = 100;


    // Private Fields

    //    gameEventHandler: Reference to the IGameEventHandler. Used for display, sound, and input.
    private IGameEventHandler gameEventHandler;

    //    gameBoard: The GameBoard used by the game.
    private GameBoard gameBoard = new GameBoard();

    //    actors: List of actors in the current session.
    private ArrayList<Actor> actors = new ArrayList<Actor>();

    //    actorFactory: ActorFactory used for creating the different types of actors.
    private ActorFactory actorFactory = new ActorFactory();

    //    random: Random instance used for this session.
    private Random random = new Random();

    // Constructor(IGameEventHandler): Creates a new Session that uses the passed IGameEventHandler, then starts the game.
    public Session(IGameEventHandler gameEventHandler)
    {
        this.gameEventHandler = gameEventHandler;
        start();
    }

    // start(): Starts the game, including the intro selection sequence.
    public void start()
    {
        gameEventHandler.playSound(SoundType.PLAYER_ONE);
        InputPacket input = null;

        while (true) {
            boolean restarting = false;

            IModelCallback<Actor> actorCallback = (Actor actor, IGameEventHandler handler) -> handler.actorDataUpdate(actor);
            IModelCallback<GameBoard> boardCallback =  (GameBoard board, IGameEventHandler handler) -> handler.boardDataUpdate(board);

            gameBoard.setCallback(boardCallback, gameEventHandler);

            Actor foe = actorFactory.createActor(ActorType.FOE, 0);
            foe.setCallback(actorCallback, gameEventHandler);
            actors.add(foe);

            // Secret room selection sequence.
            for (int player = 1; player <= MAX_PLAYERS; player++)
            {
                gameEventHandler.displayText(String.format("PLAYER %d:\n-> Choose your home.", player));

                boolean inputValid = false;
                boolean skipPlayers = false;

                while(!inputValid) {
                    inputValid = true;

                    input = nextInput();

                    switch (input.getType()) {
                        case RESET -> {
                            restarting = true;
                        }
                        case NEXT -> {
                            if(player > 1) {
                                skipPlayers = true;
                            }
                            else {
                                inputValid = false;
                            }
                        }
                        case CELL -> {
                            // Given that the input is a Cell, make sure it isn't someone else's secret room.
                            Cell secretRoom = input.getCell();
                            boolean unique = true;

                            for(Actor actor : actors) {
                                if(actor.getType() == ActorType.PLAYER && gameBoard.getSecretRoom(actor.getId()).equals(secretRoom)) {
                                    unique = false;
                                    break;
                                }
                            }

                            if(unique) {
                                gameBoard.setHomePosition(secretRoom, player);
                                Actor actor = actorFactory.createActor(ActorType.PLAYER, player);
                                actor.setCallback(actorCallback, gameEventHandler);
                                actors.add(actor);
                                actor.move(secretRoom);
                            } else {
                                gameEventHandler.displayText("Not a valid home.");
                                inputValid = false;
                            }
                        }
                        default -> inputValid = false;
                    }
                }

                if(skipPlayers || restarting) {
                    break;
                }
            }

            gameBoard.selectTreasureRoom();
            foe.move(gameBoard.getTreasureRoom());

            // Make the player press NEXT an additional time.
            if(!restarting) {
                gameEventHandler.displayText("Press NEXT.");
                while(true) {
                    input = nextInput();

                    if(input.getType() == EventType.NEXT) {
                        break;
                    }
                    else if (input.getType() == EventType.RESET) {
                        restarting = true;
                        break;
                    }
                }
            }

            // Main game loop.
            gameEventHandler.displayText("Find the treasure!");
            while (!restarting) {
                restarting = !playRound();
            }

            // Restart the game.
            restart();
        }
    }

    // restart(): Sets the Session up for a new round of gameplay.
    private void restart() {
	    gameBoard = new GameBoard();
        actors = new ArrayList<Actor>();
        gameEventHandler.clearDisplay();
    }

    // takeTurn: Have all actors take their turns.
    private boolean playRound() {
	    for(Actor actor : actors) {
            if(!takeTurn(actor)) {
                return false;
            }
        }

        if(random.nextInt(ONE_HUNDRED) < RENEWAL_CHANCE) {
            renewPlayer();
        }

        return true;
    }

    // renewPlayer(): Renews a random player's stats to what they were at teh start of the game.
    private void renewPlayer() {
        int index = random.nextInt(actors.size() - 1) + 1;
        Player chosen = (Player) actors.get(index);

        if(chosen.isAlive()) {
            chosen.heal();
            chosen.resetFatigue();

            gameEventHandler.displayText(String.format("PLAYER %d has been\nrejuvenated!.", chosen.getId()));
        }
    }

    // takeTurn(Actor): Have an actor take their turn.
    private boolean takeTurn(Actor actor) {
        boolean resetting = true;

        ActorType type = actor.getType();
        if(type == ActorType.FOE) {
            resetting = foeMove((Foe)actor);
        } else if (type == ActorType.PLAYER) {
            Player player = (Player)actor;
            if(player.isAlive()) {
                gameEventHandler.displayText(String.format("PLAYER %d's turn!", actor.getId()));
                if(actor.getId() == 1) {
                    gameEventHandler.playSound(SoundType.PLAYER_ONE);
                } else if (actor.getId() == 2) {
                    gameEventHandler.playSound(SoundType.PLAYER_TWO);
                }
                resetting = playerMove(player);
            }
        }

        return resetting;
    }

    // foeMove(Foe): The Foe's movement phase.
    private boolean foeMove(Foe foe) {
        if(foe.getAwake()) {
            gameEventHandler.displayText("The Foe moves...");
            Cell foePosition = foe.getPosition();
            double minDistance = Float.MAX_VALUE;
            Cell closest = gameBoard.getTreasureRoom();

            // Find the closest actor
            for (Actor actor : actors) {
                if (actor.getType() == ActorType.PLAYER && ((Player)actor).isAlive()) {
                    Player player = (Player) actor;
                    Cell playerPosition = player.getPosition();
                    Cell secretRoomPosition = gameBoard.getSecretRoom(player.getId());

                    double distance = playerPosition.distance(foePosition);

                    if (player.getTreasure()) {
                        minDistance = Double.MIN_VALUE;
                        closest = playerPosition;
                        break;
                    } else if (distance < minDistance && !player.getSafe()) {
                        minDistance = distance;
                        closest = playerPosition;
                    }
                }
            }

            // Choose what direction to move in
            if (closest != null) {
                int dX = (closest.x > foePosition.x ? 1 : 0) + (closest.x < foePosition.x ? -1 : 0);
                int dY = (closest.y > foePosition.y ? 1 : 0) + (closest.y < foePosition.y ? -1 : 0);

                Cell next = new Cell(foePosition.x + dX, foePosition.y + dY);
                foe.move(next);
            }

            if(!doActorInteractions()) {
                return false;
            }

            gameEventHandler.playSound(SoundType.FOE_MOVE);
        }
        return true;
    }

    // playerMove(Player): The player's movement phase, including player input.
    private boolean playerMove(Player player) {
        InputPacket input = null;

        Cell playerPosition = player.getPosition();
        Cell secretRoomPosition = gameBoard.getSecretRoom(player.getId());
        Cell treasureRoom = gameBoard.getTreasureRoom();

        boolean resetting = false;
        boolean skipTurn = false;

        int maxMoves = player.getMaxMoves();

        while(maxMoves > 0) {

            input = nextInput();

            // Get player input.
            switch(input.getType()) {
                case RESET -> resetting = true;
                case NEXT -> skipTurn = true;
                case CELL -> {
                    playerPosition = player.getPosition();
                    Cell selection = input.getCell();

                    if(playerPosition.distance(selection) != 1d) { // If the player isn't moving orthogonally.
                        gameEventHandler.displayText("You cannot move there.");
                        gameEventHandler.playSound(SoundType.PLAYER_ILLEGAL_MOVE);
                    } else if (gameBoard.moveValid(playerPosition, selection, true)) { // Valid move, make it.
                        player.move(selection);
                        --maxMoves;
                        player.tire();
                        gameEventHandler.playSound(SoundType.PLAYER_MOVE);
                    } else { // The player hit a wall.
                        gameEventHandler.displayText("You hit a wall!\nYour turn is over.");
                        skipTurn = true;
                        gameEventHandler.playSound(SoundType.WALL);
                    }
                }
            }
            playerPosition = player.getPosition();

            player.setSafe(playerPosition.equals(secretRoomPosition));


            if(playerPosition.equals(treasureRoom) && gameBoard.getTreasure()) {
                gameBoard.setTreasure(false);
                player.setTreasure(true);
                gameEventHandler.displayText("You got the treasure!\nThe Foe senses this...");
                gameEventHandler.playSound(SoundType.GOAL);
                break;
            }

            if(player.getSafe() && player.getTreasure()) {
                System.out.println("SUSSER");
                winGame(player);
                return false;
            }

            if(resetting || !doActorInteractions()) {
                return false;
            }

            boolean foughtPlayer = violentResolution(player);

            if(skipTurn || !player.isAlive() || foughtPlayer) {
                break;
            }
        }

        amicableResolution(player);

        return true;
    }

    // allPlayersDead(): Checks if all players have died.
    private boolean allPlayersDead() {
        boolean allDead = true;
        for(Actor actor : actors) {
            if(actor.getType() == ActorType.PLAYER) {
                allDead = allDead && !((Player)actor).isAlive();
            }
        }

        return allDead;
    }

    // doActorInteractions(): Checks for the predicates of and executes Actor interactions.
    private boolean doActorInteractions() {
        Foe foe = (Foe)actors.get(0);
        Cell foePosition = foe.getPosition();
        Cell treasureRoom = gameBoard.getTreasureRoom();

        if(allPlayersDead()) {
            winGame(foe);
            return false;
        }

        if(!foe.getAwake()) {
            int minDistance = Integer.MAX_VALUE;
            for(Actor actor : actors) {
                if(actor.getType() == ActorType.PLAYER && ((Player)actor).isAlive()) {
                    Player player = (Player) actor;
                    Cell playerPosition = player.getPosition();
                    int distance = playerPosition.squareDistance(foePosition);

                    if (distance < minDistance) {
                        minDistance = distance;
                    }
                }
            }

            if(minDistance <= Foe.AWAKEN_RANGE) {
                foe.setAwake(true);
                gameEventHandler.displayText("The foe has awoken!");
                gameEventHandler.playSound(SoundType.FOE_AWAKES);
            }
        }

        for(Actor actor : actors) {
            if(actor.getType() == ActorType.PLAYER && ((Player)actor).isAlive()) {
                Player player = (Player) actor;

                Cell playerPosition = player.getPosition();
                Cell secretRoomPosition = gameBoard.getSecretRoom(player.getId());

                player.setSafe(playerPosition.equals(secretRoomPosition));

                if (foePosition.equals(playerPosition)) {
                    player.wound();
                    player.move(gameBoard.getSecretRoom(player.getId()));
                    gameEventHandler.displayText("You were attacked!");
                    foe.setVisibility(true);

                    if(player.getTreasure()) {
                        player.setTreasure(false);
                        gameBoard.setTreasure(true);
                    }

                    if(!player.isAlive()) {
                        gameEventHandler.displayText(String.format("PLAYER %d has perished.", player.getId()));
                        gameEventHandler.playSound(SoundType.DEFEAT);
                    }
                    else {
                        gameEventHandler.playSound(SoundType.FOE_ATTACK);
                    }
                }
            }
        }

        if(allPlayersDead()) {
            winGame(foe);
            return false;
        }

        return true;
    }

    // getOverlappingPlayer(Player): Given a player, get another player overlapping them (if such a player exists).
    private Player getOverlappingPlayer(Player movingPlayer) {
        Cell movingPlayerPosition = movingPlayer.getPosition();
        Player overlappingPlayer = null;

        for(Actor actor : actors) {
            if(actor.getType() == ActorType.PLAYER && ((Player)actor).isAlive() && actor.getId() != movingPlayer.getId()) {
                Player otherPlayer = (Player)actor;
                if(movingPlayerPosition.equals(otherPlayer.getPosition())) {
                    overlappingPlayer = otherPlayer;
                    break;
                }
            }
        }

        return overlappingPlayer;
    }

    // violentResolution(Player): Executes Player vs Player combat. Winner gets the treasure, loser is teleported away.
    private boolean violentResolution(Player movingPlayer) {
        Player otherPlayer = getOverlappingPlayer(movingPlayer);

        if(otherPlayer != null && (movingPlayer.getTreasure() || otherPlayer.getTreasure())) {
            boolean hadTreasure = movingPlayer.getTreasure();

            Player winner = null;
            Player loser = null;

            if(movingPlayer.getFatigue() > otherPlayer.getFatigue()) {
                winner = movingPlayer;
                loser = otherPlayer;
            } else {
                winner = otherPlayer;
                loser = movingPlayer;
            }

            System.out.println(winner.getId());
            winner.setTreasure(true);
            loser.setTreasure(false);

            Cell winnerPosition = winner.getPosition();
            Cell foePosition = actors.get(0).getPosition();
            Cell candidate = null;
            boolean suitable = false;

            while(!suitable) {
                candidate = new Cell(random.nextInt(GameBoard.SIZE), random.nextInt(GameBoard.SIZE));

                int distance = winnerPosition.squareDistance(candidate);

                if(distance >= TELEPORT_DISTANCE && distance <= TELEPORT_DISTANCE + 1 && !candidate.equals(foePosition)) {
                    suitable = true;
                }
            }

            loser.move(candidate);
            if(movingPlayer.getTreasure() && hadTreasure) {
                gameEventHandler.displayText(String.format("PLAYER %d defended against\nfrom PLAYER %d!", winner.getId(), loser.getId()));
            } else {
                gameEventHandler.displayText(String.format("PLAYER %d stole the \ntreasure from PLAYER %d!", winner.getId(), loser.getId()));
            }
            return true;
        }

        return false;
    }

    // amicableResolution(Player): Resolve one player being on top of the other at the end of a turn.
    private void amicableResolution(Player movingPlayer) {
        Player otherPlayer = getOverlappingPlayer(movingPlayer);

        if(otherPlayer != null) {
            Cell moverSecretRoom = gameBoard.getSecretRoom(movingPlayer.getId());
            Player playerToMove = (movingPlayer.getPosition().equals(moverSecretRoom) ? otherPlayer : movingPlayer);
            Cell playerToMovePosition = playerToMove.getPosition();

            Cell[] orthogonal = new Cell[] {
                    new Cell(playerToMovePosition.x + 1, playerToMovePosition.y),
                    new Cell(playerToMovePosition.x - 1, playerToMovePosition.y),
                    new Cell(playerToMovePosition.x, playerToMovePosition.y + 1),
                    new Cell(playerToMovePosition.x, playerToMovePosition.y - 1)
            };

            ArrayList<Cell> choices = new ArrayList<Cell>();

            for(Cell option : orthogonal) {
                if(gameBoard.moveValid(playerToMovePosition, option, false)) {
                    choices.add(option);
                }
            }

            Cell selection = choices.get(random.nextInt(choices.size()));
            playerToMove.move(selection);
        }
    }

    // winGame(Actor): Have one Actor win the game, whether that be the Foe or a Player.
    private void winGame(Actor actor) {
        String message = "";
        ActorType type = actor.getType();
        if(type == ActorType.PLAYER) {
            message += String.format("PLAYER %d WINS!\nCongratulations!", actor.getId());
            gameEventHandler.playSound(SoundType.WIN);
        } else if (type == ActorType.FOE) {
            message += String.format("No one left alive...\nThe Foe slumbers.", actor.getId());
            gameEventHandler.playSound(SoundType.DEFEAT);
        }

        gameEventHandler.displayText(message + "\n(RESET or NEXT)");
        while(true) {
            InputPacket input = nextInput();

            if(input.getType() == EventType.NEXT || input.getType() == EventType.RESET) {
                break;
            }
        }
    }


    private InputPacket nextInput() {
        InputPacket inputPacket = new InputPacket();
        gameEventHandler.subscribeInput(inputPacket);
        inputPacket.awaitSigned();

        return inputPacket;
    }
}
