




## Routes:

### "/" - home

### "/dev" - develop/test page

### "/create" - new game
	- return gameID

### "/game/[gameID]" - landing page of a single game

### "/gamespeaks/[gameID]" - actions on gameID
- params
	- action=[dice, camels, roll, name]
		- [name], add player or change name, also include [PLAYER_NAME_KEY] and the name as param
			- return the actual new name in text
		- [dice], return all colors of active dice in JSON
		- [roll], roll once and return all colors of active dice in JSON
		- [camels], return camels in JSON


