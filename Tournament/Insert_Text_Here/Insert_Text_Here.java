package Insert_Text_Here;

import breakthrough.BreakthroughState;
import game.*;

public class Insert_Text_Here extends GamePlayer {
	private AlphaBetaMT abMT = null;
	public Insert_Text_Here(String nickname, int depthLimit) {
		super(nickname, new BreakthroughState(), false);
		abMT = new AlphaBetaMT(depthLimit);
	}

	@Override
	public GameMove getMove(GameState brd, String lastMove) {
		/*alphaBeta((BreakthroughState) brd, 0, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);
		System.out.println(mvStack[0].score);
		return mvStack[0];*/
		return abMT.getMove(brd, lastMove);
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
