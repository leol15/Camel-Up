

public class CamelUp {

	// static things
	public static final String[] COLORS = {
		"RED", "GREEN", "BLUE", "BLACK", "WHITE", "PURPLE", "GOLD"
	};

	// states
	private Camel[] camels;

	public CamelUp() {
		camels = new Camel[COLORS.length];
		for (int i = 0; i < COLORS.length; i++) {
			camels[i] = new Camel(COLORS[i]);
		}
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
		private int location;
		private String color;

		public Camel(String col) {
			this.color = col;
			this.location = 0;
		}

		@Override
		public String toString() {
			return "Camel [" + this.color + "] at [" + location + "]"; 
		}
	}
	
}