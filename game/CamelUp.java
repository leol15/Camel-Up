import java.util.Random;

import java.util.*;

public class CamelUp {

	// static things
	public static final String[] COLORS = {
		"RED", "GREEN", "BLUE", "BLACK", "WHITE", "PURPLE", "GOLD"
	};

	// states
	private Random rand;
	List<Integer> dice;
	private Camel[] camels;
	// slot 0 Camel1, Camel2
	// slot 1 Camel3, ...
	// slot 2
	// ...
	private List<Camel>[] playground;
	private Map<String, Queue<Bet>> betTags;
	private Map<String, Player> players;

	@SuppressWarnings("unchecked")
	public CamelUp() {
		rand = new Random();
		dice = new ArrayList<>();
		for (int i = 0; i < COLORS.length; i++)
			dice.add(i);
		playground = new List[20];
		for (int i = 0; i < playground.length; i++) {
			playground[i] = new ArrayList<>();
		}
		camels = new Camel[COLORS.length];
		for (int i = 0; i < COLORS.length; i++) {
			if (COLORS[i].equals("BLACK") || COLORS[i].equals("WHITE")) {
				camels[i] = new Camel(COLORS[i], playground, -1);
			} else {
				camels[i] = new Camel(COLORS[i], playground);
			}
		}

		// Instantiates the betting system.
		// Bets range from 5 - 2 (there are 4 tags per color)
		// Cannot bet on Black or White camels
		betTags = new HashMap<>();
		for (String str : COLORS) {
			if (!str.equals("BLACK") && !str.equals("WHITE")) {
				Queue<Bet> currBet = new PriorityQueue<>(4, new Comparator<Bet>(){
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
	}

	// Starts a game of camel up
	// Each camel starts with a roll.
	public void startGame() {
		for (int i = 0; i < COLORS.length; i++) {
			camels[i].rollDie(rand);
		}
	}

	public void addPlayers(String name) {
		players.put(name, new Player(name, betTags));
	}

	public boolean containsPlayer(String name) {
		return players.containsKey(name);
	}

	// actions
	// make a roll
	public void rollDie() {
		// decide what color
		int idx = rand.nextInt(dice.size());
		// roll
		camels[dice.get(idx)].rollDie(rand);

		// End game if a camel crosses the finish line
		if (camels[dice.get(idx)].position() >= 20) {
			gameover();
		} else { // continue game if not end game
			dice.remove(idx);
			// end of game -- when a camel crosses the finish line
			// Camel that is the furthest and heighest
			//if ()

			// end of round?
			if (dice.size() == 1)
				newRound();
		}
	}

	// state change
	public void newRound() {
		dice.clear();
		for (int i = 0; i < COLORS.length; i++)
			dice.add(i);
	}


	// observers
	public void getCamels() {

	}

	public List<String> getDice() {
		List<String> ret = new ArrayList<>();
		for (int i : dice)
			ret.add(COLORS[i]);
		return ret;
	}

	public void gameover() {

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
		private int index; // the index in playground
		private int height; // the height in playground 0 - 6

		// for the camels that go backwards
		private int mult = 1;

		public Camel(String col, List<Camel>[] playground) {
			this.color = col;
			this.playground = playground;
			// add self
			playground[0].add(this);
			this.index = 0;
			this.height = playground[0].size() - 1;
		}

		public Camel(String col, List<Camel>[] playground, int scaler) {
			this(col, playground);
			mult = scaler;
		}

		public void rollDie(Random r) {
			System.out.println(this + ": moving");
			// already at end??
			if (index == 19 || (index == 0 && mult < 0))
				return;
			// new index
			int newIdx = index + (1 + r.nextInt(3)) * mult;
			newIdx = Math.max(0, Math.min(19, newIdx));
			System.out.println(this + ": moving to " + newIdx);

			// end of line
			// move everything
			int oldIdx = this.index;
			int oldHeight = height;
			for (int i = height; i < playground[oldIdx].size(); i++)
				playground[oldIdx].get(i).insertAt(newIdx);
			// remove
			while (oldHeight < playground[oldIdx].size())
				playground[oldIdx].remove(oldHeight);
		}

		// return the position of the camel
		public int position() {
			return index;
		}

		// helper to move
		// only adds self, not remove
		private void insertAt(int index) {
			this.index = index;
			playground[index].add(this);
			this.height = playground[index].size() - 1;
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

	private class Player {
		private String name;
		private int coin;
		private List<Bet> bets;
		private Map<String, Queue<Bet>> betTags;

		public Player(String name, Map<String, Queue<Bet>> betTags) {
			this.name = name;
			coin = 3;
			bets = new ArrayList<>();
			this.betTags = betTags;
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

		@Override
		public String toString() {
			return name + " and has $" + coin;
		}
	}
	
}
