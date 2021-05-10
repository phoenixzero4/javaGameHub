

public class Animation {
	
	private final int NUMBER_OF_ANIM_STEPS = 10;
	private int steps = 0;
	
	public void inc() {
		steps++;
	}
	
	public void reset() {
		steps = 0;
	}
	
	public boolean finished() {
		return steps == NUMBER_OF_ANIM_STEPS;
	}
	
	
}
