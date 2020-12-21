

// client side model


class GameManager {
	constructor(websocket) {
		console.log("ctor")
		// get game id
		this.gameID = window.location.pathname.split("/")[2]
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
			console.log("sending connect")
			this.sendMessage("id", "oreo-test")
		}
		this.socket.onmessage = (msg) => {
			this.handleMessage(msg.data)
		}
		this.socket.onclose = () => {

		}
		// setTimeout(()=>{this.socket.send("hello???")}, 1000)
	}

	handleMessage(msg) {
		console.log(msg)
	}

	sendMessage(method, value) {
		var o = {}
		o["gameID"] = this.gameID
		o["method"] = method
		o["value"] = value
		this.socket.send(JSON.stringify(o))
	}

}




GAME = new GameManager()