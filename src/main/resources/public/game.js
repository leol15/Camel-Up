

// client side model


class GameManager {
	constructor(websocket) {
		console.log("ctor")
		// get game id
		this.gameID = window.location.pathname.split("/")[2]
		this.playerID = ""
		// websocket sync
		if (location.protocol === 'https:') {
			this.socket = new WebSocket("wss://" +
				location.hostname + ":" + location.port + "/socket")
		} else {
			this.socket = new WebSocket("ws://" +
				location.hostname + ":" + location.port + "/socket")
		}

		this.socket.onopen = () => {
			// send id
		}
		this.socket.onmessage = (msg) => {
			this.handleMessage(msg.data)
		}
		this.socket.onclose = () => {

		}
	}

	newPlayer(name) {
		this.sendMessage("id", name)
	}

	// actions
	makeRoll() {
		this.sendMessage("roll");
	}

	placeBet(el) {
		this.sendMessage("makeBet", el.style.backgroundColor);
	}

	makeWinnerGlobalBet(el) {
		this.sendMessage("makeWinnerGlobalBet", el.style.backgroundColor);
	}

	makeLoserGlobalBet(el) {
		this.sendMessage("makeLoserGlobalBet", el.style.backgroundColor);
	}

	// place a trap
	placeTrap(tile, boost="boost") {
		this.sendMessage("placeTrap", [tile, boost])
	} 

	resetGame() {
		this.sendMessage("reset")
	}

	// handle info from server
	handleMessage(msg) {
		var res = JSON.parse(msg)
		if (res.method === "id") {
			this.playerID = res.value
		} else if (res.method === "players") {
			updatePlayers(res.value)
		} else if (res.method === "camels") {
			updateCamels(res.value)
		} else if (res.method === "dice") {
			updateDice(res.value)
		} else if (res.method === "bet") {
			updateBets(res.value)
		} else if (res.method === "globalBet") {
			updateGlobalBet(res.value)
		} else if (res.method === "trap") {
			updateTraps(res.value)
		} else if (res.method === "playerScore") {
			// todo
		} else {
			console.log("unknown msg")
			console.log(res)
		}
	}

	sendMessage(method, value) {
		var o = {gameID: this.gameID, playerID: this.playerID}
		o["method"] = method
		o["value"] = value
		this.socket.send(JSON.stringify(o))
	}

}


// todo: auto start/login


// start!
var GAME = new GameManager()

// hook ups

// how to make player id able? 
// also, how to reconnect to a disconnected one?
function createPlayer(name) {
	if (name !== "" && (event.key === "Enter" || event.keyCode === 12)) {
		GAME.newPlayer(name)
	}
}

function resetGame() {
	GAME.resetGame()
}

function makeRoll() {
	GAME.makeRoll()
}

function placeBet(el) {
	GAME.placeBet(el)
}

function makeWinnerGlobalBet(el) {
	GAME.makeWinnerGlobalBet(el)
}

function makeLoserGlobalBet(el) {
	GAME.makeLoserGlobalBet(el)
}
