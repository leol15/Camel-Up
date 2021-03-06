




## Routes:

### "/" - home

### "/dev" - develop/test page

### "/create" - new game
	- return gameID

### "/game/[gameID]" - landing page of a single game

### "/gamespeaks/[gameID]" - actions on gameID
- params
	- action=[dice, camels, roll, name, bet, makeBet, globalBet, makeWinnerGlobalBet, makeLoserGlobalBet, trap, placeTrap, reset]
		- [name], add player or change name, also include [PLAYER_NAME_KEY] and the name as param
			- return the actual new name in text
		- [dice], return all colors of active dice in JSON
		- [roll], roll once and return all colors of active dice in JSON
		- [camels], return camels in JSON
		- [bet], return top bet tags left to take of each color camel, return bet in JSON
		- [makeBet], take the bet tag for a specified camel, also include [COLOR_KEY] as param
		- [globalBet], return a list of global bet tags
			- global bet tag = player name and the camel color
			- a list of colors left in the players pile of global bets they can make
		- [makeWinnerGlobalBet], places a bet on the specified camel
		- [makeLoserGlobalBet], places a bet on the specified camel
		- [trap], return a list of traps on the board
		- [placeTrap], places a trap on the board
		- [reset], resets the game to start state, players are untouched though.
		- [timestamp] return a number indicating current game state
		- [playerTurn] return name of player whose turn this is 

## WebSocket communications (JSON)


### Server to client

message string: "{method:str, value:...}"

- method: 
	+ id
		* user_assigned_id
	+ players
		* 
	+ dice 
		* [active_color_1, active_color_2...]
	+ camels
		*  
	+ bet
		* 
	+ globalBet
		* 	
	+ trap
		* 
	+ playerTurn
		*
	+ playerScore
		*

### Client to Server (only actions!)

message string: "{gameID:..., playerID:..., method:..., value:...}"


- method:
	+ roll
	+ makeBet
	+ makeWinnerGlobalBet
	+ makeLoserGlobalBet
	+ placeTrap
	+ reset