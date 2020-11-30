import java.util.*;

public class CamelUp {

	// static things
	public static final String[] COLORS = {
		"RED", "GREEN", "BLUE", "BLACK", "WHITE", "PURPLE", "GOLD"
	};
	public static final int[] WINNINGS = {8, 5, 3, 2, 1, 0, 0, 0};
	public static final int MAX_PLAYERS = 8;
	public static final int LAST_TILE = 20;
	public static final int DICE_MAX = 3;
	public static final int BET_MAX = 4;

	// states
	private Random rand;
	List<Integer> dice;
	private Camel[] camels;
	// slot 0 Camel1, Camel2
	// slot 1 Camel3, ...
	// slot 2
	// ...
	private List<Camel>[] playground;
	// The games betting Tags storage before players place bets
	private Map<String, Queue<Bet>> betTags;
	// Keeps track of players game record and easy to get
	private Map<String, Player> players;
	// Auxilliary data structure to sort camels
	private Queue<Camel> sortCamel;	
	// Keeps track of the winner global bets players make
	private Queue<GlobalBet> biggestWinner;
	// Keeps track of the loser global bets players make
	private Queue<GlobalBet> biggestLoser;
	// Keeps track of the traps on the board
	// private Trap[] traps;
	private Map<Integer, Trap> traps;

	private String leading;
	private String trailing;
	private String last;

	

	@SuppressWarnings("unchecked")
	public CamelUp() {
		rand = new Random();
		traps = new HashMap<>();
		dice = new ArrayList<>();
		for (int i = 0; i < COLORS.length; i++)
			dice.add(i);
		playground = new List[LAST_TILE];
		for (int i = 0; i < playground.length; i++) {
			playground[i] = new ArrayList<>();
		}
		camels = new Camel[COLORS.length];
		for (int i = 0; i < COLORS.length; i++) {
			if (COLORS[i].equals("BLACK") || COLORS[i].equals("WHITE")) {
				camels[i] = new Camel(COLORS[i], playground, rand, traps, LAST_TILE, -1);
			} else {
				camels[i] = new Camel(COLORS[i], playground, rand, traps);
			}
		}

		// Instantiates the betting system.
		// Bets range from 5 - 2 (there are 4 tags per color)
		// Cannot bet on Black or White camels
		betTags = new HashMap<>();
		for (String str : COLORS) {
			if (!str.equals("BLACK") && !str.equals("WHITE")) {
				Queue<Bet> currBet = new PriorityQueue<>(BET_MAX, new Comparator<Bet>(){
						public int compare(Bet b1, Bet b2) {
							return b2.value - b1.value;
						}
				});
				currBet.add(new Bet(str, 5));
				currBet.add(new Bet(str, 3));
				currBet.add(new Bet(str, 2));
				currBet.add(new Bet(str, 2));
				betTags.put(str, currBet);
			}
		}

		players = new HashMap<>();

		sortCamel = new PriorityQueue<>(5, new Comparator<Camel>(){
			public int compare(Camel c1, Camel c2) {
				if (c1.position() != c2.position()) {
					return c2.position() - c1.position();
				} else {
					return c2.rank() - c1.rank();
				}
			}
		});

		biggestWinner = new LinkedList<>();
		biggestLoser = new LinkedList<>();

		// traps = new Trap[LAST_TILE];
		
	}

	public boolean addPlayer(String name) {
		if (MAX_PLAYERS == players.size()) {
			return false;
		}
		players.put(name, new Player(name, betTags));
		return true;
	}

	public boolean containsPlayer(String name) {
		return players.containsKey(name);
	}

	public void changePlayer(String old, String newName) {
		players.get(old).changeName(newName);
		players.put(newName, players.get(old));
		players.remove(old);
	}

	// actions
	// make a roll
	public void rollDie() {
		// decide what color
		int idx = rand.nextInt(dice.size());
		// roll
		camels[dice.get(idx)].rollDie(rand);

		// End game if a camel crosses the finish line
		if (camels[dice.get(idx)].position() >= LAST_TILE - 1) {
			// end of game -- when a camel crosses the finish line
			// Camel that is the furthest and heighest
			gameover();
		} else { // continue game if not end game
			dice.remove(idx);
			// end of round?
			if (dice.size() == 1)
				newRound();
		}
	}

	// Communicates with the server about which player rolled a dice
	// Player who rolled the dice gets 1 coin
	public void rollDie(String player) {
		rollDie();
		players.get(player).rollDie();
	}

	// A player can choose to place a bet on a camel
	public boolean placeBets(String player, String color) {
		// Shouldn't be allowed to do that but if they do...
		if (color.equals("BLACK") || color.equals("WHITE")) {
			return false;
		}
		return players.get(player).placeBet(color);
	}

	// A player can choose to place a winning global bet on a camel
	// Players bet what camel will be in first place at the end of the
	// game
	public boolean placeWinnerGlobalBet(String player, String color) {
		return players.get(player).placeGlobalBet(biggestWinner, color);
	}

	// A player can choose to place a loser global bet on a camel
	// Players bet what camel will be in last place at the end of the
	// game
	public boolean placeLoserGlobalBet(String player, String color) {
		return players.get(player).placeGlobalBet(biggestLoser, color);
	}

	// A player can place a trap on the board
	// Traps can only be placed on empty tiles on the board (no camel there yet)
	public boolean placeTrap(String player, int tile, boolean boost) {
		// Has to be on the board
		if (tile <= 0 || tile >= LAST_TILE) {
			return false;
		}
		// No camels on that tile
		if (playground[tile].size() != 0) {
			return false;
		}
		// No traps already on the tile or next to the tile
		if (traps.containsKey(tile) || traps.containsKey(tile + 1) 
									|| traps.containsKey(tile - 1)) {
			return false;
		}
		Trap currTrap = players.get(player).getTrap();
		traps.remove(currTrap.getTile());
		// traps[currTrap.getTile()] = null; 
		if (boost) {
			currTrap.changeTrap(tile, 1);
		} else {
			currTrap.changeTrap(tile, -1);
		}
		traps.put(tile, currTrap);
		return true;
	}

	// state change
	public void newRound() {
		dice.clear();
		for (int i = 0; i < COLORS.length; i++)
			dice.add(i);

		updateLeaderBoard();
		refreshBettingTags();
		clearTrap();
	}

	public void clearTrap() {
		for (Trap t : traps.values()) {
			t.changeTrap(0, 0);
		}
		traps.clear();
	}

	// Sorts the camels and finds the first place camel and the second place camel
	public void updateLeaderBoard() {
		for (Camel c : camels) {
			sortCamel.add(c);
		}
		leading = sortCamel.poll().color();
		trailing = sortCamel.poll().color();
		
		// 3rd and 4th place
		sortCamel.poll();
		sortCamel.poll();

		last = sortCamel.poll().color();
	}

	// Resolves all the betting tags of the players
	private void refreshBettingTags() {
		for (Map.Entry<String, Player> p : players.entrySet()) {
			p.getValue().resolveBets(leading, trailing);
		}
	}

	// Implemented the globalBetting resolving.
	private void resolveGlobalBets() {
		resolveGlobalBetsHelper(biggestWinner, leading);
		resolveGlobalBetsHelper(biggestLoser, last);
	}
	
	// Refactored redundant code to make it work for winner or loser
	private void resolveGlobalBetsHelper(Queue<GlobalBet> q, String camel) {
		int idx = 0;
		for (GlobalBet b : q) {
			if (b.color.equals(camel)) {
				players.get(b.player).addCoin(WINNINGS[idx]);
				idx++;
			} else {
				players.get(b.player).addCoin(-1);
			}
			players.get(b.player).globalBets.put(b.color, b);
		}
		q.clear();
	}

	// End game situation
	public void gameover() {
		updateLeaderBoard();
		refreshBettingTags();
		resolveGlobalBets();
		clearTrap();
	}

	public void reset() {
		for (List<Camel> list : playground) {
			list.clear();
		}
		for (Camel c : camels) {
			c.reset();
		}
		for (Player p : players.values()) {
			p.reset();
		}
		updateLeaderBoard();
	}

	//////////////////////////////////////////////////////////////////////////
	// observers
	/////////////////////////////////////////////////
	public Map<String, int[]> getCamels() {
		Map<String, int[]> colorToPosition = new TreeMap<>();
		for (Camel c : camels) {
			int[] pos = new int[]{c.position(), c.rank()};
			colorToPosition.put(c.color(), pos);
		}
		return colorToPosition;
	}

	public Map<String, Player> getPlayers() {
		Map<String, Player> ret = new HashMap<>();
		for (String name : players.keySet()) {
			ret.put(name, players.get(name));
		}
		return ret;
	}

	public List<String> getDice() {
		List<String> ret = new ArrayList<>();
		for (int i : dice)
			ret.add(COLORS[i]);
		return ret;
	}

	public List<Bet> getBet() {
		List<Bet> ret = new ArrayList<>();
		for (String str : COLORS) {
			if (!str.equals("BLACK") || !str.equals("WHITE")) {
				if (!betTags.get(str).isEmpty()) {
					ret.add(betTags.get(str).peek());
				}
			}
		}
		return ret;
	}

	public List<GlobalBet> getGlobalBet(String player) {
		List<GlobalBet> ret = new ArrayList<>();
		for (GlobalBet b : players.get(player).getGlobalBetTickets().values()) {
			ret.add(b);
			// ret.add("Global Bet Token = " + b.color);
		}
		return ret;
	}

	public List<Trap> getTrap() {
		// List<String> ret = new ArrayList<>();
		// for (Trap t : traps.values()) {
		// 	ret.add(t.getPlayer() + " has a trap at " + t.tile + " and it is a " + t.getValue() );
		// }
		// return ret;

		List<Trap> ret = new ArrayList<>();
		for (Trap t : traps.values()) {
			ret.add(t);
		}
		return ret;
	}

	public Map<String, Integer> getPlayerScore() {
		Map<String, Integer> ret = new HashMap<>();
		for (Map.Entry<String, Player> kv : players.entrySet()) {
			ret.put(kv.getKey(), kv.getValue().getScore());
		}
		return ret;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("---------\n");
		sb.append("Game State\n");
		for (Camel c : camels) {
			sb.append(c.toString());
			sb.append("\n");
		}
		sb.append("---------\n");
		return sb.toString();
	}


	// internal Camel class
	private class Camel {

		// states
		private String color;
		private List<Camel>[] playground;
		private int start; // the start of every game
		private int index; // the index in playground
		private int height; // the height in playground 0 - 6
		Map<Integer, Trap> traps;
		private Random r;

		// for the camels that go backwards
		private int mult = 1;

		public Camel(String col, List<Camel>[] playground, Random r, 
						Map<Integer, Trap> traps, int index, int scaler) {
			this.color = col;
			this.playground = playground;
			this.traps = traps;
			this.r = r;
			this.start = index;
			// add self
			this.index = index + scaler * (1 + r.nextInt(DICE_MAX));
			playground[this.index].add(this);
			this.height = playground[this.index].size() - 1;
			mult = scaler;
		}

		public Camel(String col, List<Camel>[] playground, Random r, Map<Integer, Trap> traps) {
			this(col, playground, r, traps, 0, 1);
		}

		public void rollDie(Random r) {
			System.out.println(this + ": moving");
			// already at end??
			if ((index == 19 && mult > 0) || (index == 0 && mult < 0))
				return;
			// new index
			int newIdx = index + (1 + r.nextInt(DICE_MAX)) * mult;
			newIdx = Math.max(0, Math.min(LAST_TILE - 1, newIdx));

			// Check if index has a trap
			if (traps.containsKey(newIdx)) {
				newIdx += traps.get(newIdx).value;
				players.get(traps.get(newIdx).getPlayer()).addCoin(1);
			}

			System.out.println(this + ": moving to " + newIdx);

			// end of line
			// move everything
			if (this.index == newIdx && height == 0) {
				// Do nothing 
			} else if (this.index == newIdx) {
				// If camel is pushed back then it is modified the stacking order
				Stack<Camel> temp = new Stack<>();
				for (int i = height; i < playground[this.index].size(); i++)
					temp.push(playground[this.index].get(i));
				
				// Remove camels on this list
				for (int i = 0; i < temp.size(); i++) {
					playground[this.index].remove(height);
				}

				// Put the camels at the bottom of this tile
				while (!temp.isEmpty()) {
					playground[this.index].add(0, temp.pop());
				}
				
				// Update the new heights of the camels
				for (int i = 0; i < playground[this.index].size(); i++) {
					playground[this.index].get(i).height = i;
				}
			} else {
				int oldIdx = this.index;
				int oldHeight = height;
				for (int i = height; i < playground[oldIdx].size(); i++)
					playground[oldIdx].get(i).insertAt(newIdx);
				// remove
				while (oldHeight < playground[oldIdx].size())
					playground[oldIdx].remove(oldHeight);
			}
		}

		// return the position of the camel
		public int position() {
			return index;
		}

		// Return the height of the camel
		public int rank() {
			return height;
		}

		// Return the camel color
		public String color() {
			return color;
		}

		// helper to move
		// only adds self, not remove
		private void insertAt(int index) {
			this.index = index;
			playground[index].add(this);
			this.height = playground[index].size() - 1;
		}

		public void reset() {
			this.index = start + mult * (1 + r.nextInt(DICE_MAX));
			playground[this.index].add(this);
			this.height = playground[this.index].size() - 1;
		}

		@Override
		public String toString() {
			return "Camel [" + color + "] at [" + index + "]" + "[" + height + "]"; 
		}
	}

	
	// Bet keeps track of color and value
	// Color to place it back into our betting management system
	// Value so each player knows how many coins they win if they do win
	private class Bet {
		String color;
		int value;
		
		public Bet(String color, int value) {
			this.color = color;
			this.value = value;
		}

		// class BetComparator implements Comparator<Bet>{
		// 	public int compare(Bet b1, Bet b2) {
		// 		return b2.value - b1.value;
		// 	}
		// }
	}

	private class GlobalBet {
		String color;
		String player;

		public GlobalBet(String player, String color) {
			this.player = player;
			this.color = color;
		}
	}

	private class Player {
		private String name;
		private int coin;
		private List<Bet> bets;
		private Map<String, Queue<Bet>> betTags;
		private Map<String, GlobalBet> globalBets;
		private Trap trap;

		public Player(String name, Map<String, Queue<Bet>> betTags) {
			this.name = name;
			coin = 3;
			trap = new Trap(name);
			bets = new ArrayList<>();
			this.betTags = betTags;
			globalBets = new HashMap<>(5);
			for (String str : COLORS) {
				if (!str.equals("BLACK") && !str.equals("WHITE")) {
					globalBets.put(str, new GlobalBet(name, str));
				}
			}
		}

		public void changeName(String name) {
			this.name = name;
		}

		public void rollDie() {
			coin++;
		}

		// Any better way to do this?
		public void addCoin(int val) {
			coin += val;
		}

		public Trap getTrap() {
			return trap;
		}

		public Map<String, GlobalBet> getGlobalBetTickets() {
			return globalBets;
		}

		public int getScore() {
			return coin;
		}

		// Called when a player bets on a camel
		// @param color, a player chooses a color camel to bet on
		// @return true if there are bet tags still left
		//		   false if there are 0 bet tags left.
		public boolean placeBet(String color) {
			if (betTags.get(color).isEmpty()) {
				return false;
			}
			Bet b = betTags.get(color).poll();
			bets.add(b);

			// Notify other players who bet on which camel
			System.out.println(name + " placed a bet on " + color + 
								" and has taken the $" + b.value + " bet");
			return true;
		}

		// Called at the end of each round, used to reset the betting tags
		// Resolves all the bets made and add or subtracts from their bank.
		// @param winner, pass in the winner camel for the round
		// @param secondPlace, pass in the second place camel for the round
		public void resolveBets(String winner, String secondPlace) {
			Iterator<Bet> itr = bets.iterator();
			while (itr.hasNext()) {
				Bet b = itr.next();
				itr.remove();
				if (b.color.equals(winner)) {
					coin += b.value;
				} else if (b.color.equals(secondPlace)) {
					coin++;
				} else {
					coin--;
				}
				betTags.get(b.color).add(b);
			}
		}

		public boolean placeGlobalBet(Queue<GlobalBet> q, String color) {
			if (globalBets.containsKey(color)) {
				q.add(globalBets.get(color));
				globalBets.remove(color);
				return true;
			}
			return false;
		}

		public void reset() {
			coin = 3;
		}

		@Override
		public String toString() {
			return name + " and has $" + coin;
		}
	}
	
	private class Trap {
		int value;
		String player;
		int tile;

		public Trap(String player) {
			this.player = player;
			this.value = 0;
			this.tile = 0;
		}

		public void changeTrap(int tile, int scalar) {
			this.tile = tile;
			value = scalar;
		}

		public int getTile() {
			return tile;
		}

		public String getPlayer() {
			return player;
		}

		public int getValue() {
			return value;
		}
	}
}
