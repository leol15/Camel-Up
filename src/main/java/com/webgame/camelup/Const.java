package com.webgame.camelup;

public final class Const {
	// create player
	public static final String INFO_ID = "id";
	
	// info
	public static final String INFO_PLAYERS = "players";
	public static final String INFO_CAMELS = "camels";
	public static final String INFO_DICE = "dice";
	
	public static final String INFO_BET = "bet";
	public static final String INFO_GLOBAL_BET = "globalBet";
	
	public static final String INFO_TRAP = "trap";
	
	public static final String INFO_PLAYER_SCORE = "playerScore";
	public static final String INFO_PLAYER_TURN = "playerTurn";

	// actions
	public static final String ACTION_ROLL = "roll";
	public static final String ACTION_MAKE_BET = "makeBet";
	public static final String ACTION_WINNER_BET = "makeWinnerGlobalBet";
	public static final String ACTION_LOSER_BET = "makeLoserGlobalBet";

	public static final String ACTION_PLACE_TRAP = "placeTrap";
	public static final String ACTION_SCALAR_VALUE = "boost"; // boost == +1, otherwise it is -1
	
	public static final String ACTION_RESET = "reset";


	// server stuff

	public static final int PORT = 45678;
	public static final String GAME_ROUTE = "/game";  // /game/[gamecode] => landing page
	public static final String CREATE_GAME_ROUTE = "/create";
	public static final String HOME_ROUTE = "/";
}