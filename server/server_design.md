




## Routes:

### "/" - home

### "/dev" - develop/test page

### "/create" - new game
	- return gameID

### "/game/[gameID]" - landing page of a single game

### "/gamespeaks/[gameID]" - actions on gameID
- params
	- action=[dice, camels, roll, name, bet, makeBet, globalBet]
		- [name], add player or change name, also include [PLAYER_NAME_KEY] and the name as param
			- return the actual new name in text
		- [dice], return all colors of active dice in JSON
		- [roll], roll once and return all colors of active dice in JSON
		- [camels], return camels in JSON
		- [bet], return top bet tags left to take of each color camel, return bet in JSON
		- [makeBet], take the bet tag for a specified camel, also include [COLOR_KEY] as param
		- [globalBet], return a list of strings with the global bet and the camel color
			- a list of colors left in the players pile of global bets they can make


