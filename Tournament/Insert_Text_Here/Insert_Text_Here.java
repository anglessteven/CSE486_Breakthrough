package Insert_Text_Here;

import Insert_Text_Here.AlphaBetaMT.ScoredBreakthroughMove;
import breakthrough.BreakthroughState;
import game.*;

public class Insert_Text_Here extends GamePlayer {
	private AlphaBetaMT abMT = null;
	private int depthLimit = 6;
	public ScoredBreakthroughMove[] mvStack;
	
	public Insert_Text_Here(String nickname, int depthLimit) {
		super(nickname, new BreakthroughState(), false);
		this.depthLimit = depthLimit;
	}

	@Override
	public GameMove getMove(GameState brd, String lastMove) {
		AlphaBetaMT[] threads = new AlphaBetaMT[4];
		for (int i=0; (i<threads.length); i++) {
			threads[i] = new AlphaBetaMT(depthLimit);
			threads[i].start();
		}
		for (int i=0; (i<threads.length); i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initializes the stack of Moves.
	 */
	public void init() {
		/*mvStack = new ScoredBreakthroughMove[MAX_DEPTH];
		for (int i = 0; i < MAX_DEPTH; i++) {
			mvStack[i] = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
		}*/
		abMT.init();
	}

	public static void main(String[] args) {
		int depth = 6;
		GamePlayer p = new Insert_Text_Here("Insert_Text_Here", depth);
		p.compete(args);
	}

}
