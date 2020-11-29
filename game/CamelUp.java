
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

	public CamelUp() {
		rand = new Random();
		dice = new ArrayList<>();
		for (int i = 0; i < 6; i++)
			dice.add(i);
		playground = new ArrayList[20];
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
	}

	// actions
	// make a roll
	public void rollDie() {
		// end of round?
		if (dice.size() == 1)
			newRound();
		// decide what color
		int idx = rand.nextInt(dice.size());
		// roll
		camels[dice.get(idx)].rollDie(rand);
		dice.remove(idx);
	}

	// state change
	public void newRound() {
		dice.clear();
		for (int i = 0; i < 6; i++)
			dice.add(i);
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
			for (int i = height; i < playground[oldIdx].size(); i++) {
				playground[oldIdx].get(i).insertAt(newIdx);
			}
			// remove
			while (oldHeight < playground[oldIdx].size())
				playground[oldIdx].remove(oldHeight);
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
	
}