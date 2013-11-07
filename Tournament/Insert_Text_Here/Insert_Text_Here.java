package Insert_Text_Here;

import game.GameMove;
import game.GamePlayer;
import game.GameState;

import java.util.ArrayList;

import breakthrough.BreakthroughMove;
import breakthrough.BreakthroughState;

public class Insert_Text_Here extends GamePlayer {
	public int depthLimit = 6;
	private static final int NUMTHREADS = 4;

	public Insert_Text_Here(String nickname, int depthLimit) {
		super(nickname, new BreakthroughState(), false);
		this.depthLimit = depthLimit;
	}

	public GameMove getMove(GameState brd, String lastMove) {
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		ArrayList<ScoredBreakthroughMove> moves = new ArrayList<ScoredBreakthroughMove>();
		try {
			moves = runThreads((BreakthroughState) brd);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ScoredBreakthroughMove best = moves.get(0);
		for (ScoredBreakthroughMove sbm : moves) {
			if (toMaximize) {
				best = (sbm.score > best.score) ? sbm : best;
			} else {
				best = (sbm.score < best.score) ? sbm : best;
			}
		}
		return best;
	}

	private ArrayList<ScoredBreakthroughMove> runThreads(BreakthroughState brd)
			throws InterruptedException {
		ArrayList<BreakthroughMove> moves = new ArrayList<BreakthroughMove>();
		BreakthroughMove mv = new BreakthroughMove();
		char me = brd.who == BreakthroughState.Who.HOME ? BreakthroughState.homeSym
				: BreakthroughState.awaySym;
		int dir = brd.who == BreakthroughState.Who.HOME ? +1 : -1;
		for (int r = 0; r < BreakthroughState.N; r++) {
			for (int c = 0; c < BreakthroughState.N; c++) {
				mv.startRow = r;
				mv.startCol = c;
				if (brd.board[r][c] == me) {
					mv.endingRow = r + dir;
					mv.endingCol = c;
					if (brd.moveOK(mv)) {
						moves.add((BreakthroughMove) mv.clone());
					}
					mv.endingRow = r + dir;
					mv.endingCol = c + 1;
					if (brd.moveOK(mv)) {
						moves.add((BreakthroughMove) mv.clone());
					}
					mv.endingRow = r + dir;
					mv.endingCol = c - 1;
					if (brd.moveOK(mv)) {
						moves.add((BreakthroughMove) mv.clone());
					}
				}
			}
		}

		int wholeNumber = moves.size() / NUMTHREADS;
		int remainder = moves.size() % NUMTHREADS;
		double batchIncrement = remainder / ((double) NUMTHREADS);
		double batchRemainder = 0.0;
		int index = 0, end = 0;
		AlphaBetaMT[] thrList = new AlphaBetaMT[NUMTHREADS];
		// Create the necessary number of threads.
		for (int i = 0; i < NUMTHREADS; i++) {
			end += wholeNumber;
			batchRemainder += batchIncrement;
			if (batchRemainder >= 1.0) {
				end += Math.floor(batchRemainder);
				batchRemainder -= Math.floor(batchRemainder);
			}
			thrList[i] = new AlphaBetaMT(index, end, depthLimit, brd, moves);
			index = end;
			thrList[i].start();
		}
		// Wait for the threads to finish.
		for (int i = 0; i < NUMTHREADS; i++) {
			thrList[i].join();
		}

		ArrayList<ScoredBreakthroughMove> alphaBetaMoves = new ArrayList<ScoredBreakthroughMove>();

		for (int i = 0; i < NUMTHREADS; i++) {
			alphaBetaMoves.add(thrList[i].getBestMove());
		}

		return alphaBetaMoves;
	}

	public static void main(String[] args) {
		int depth = 6;
		GamePlayer p = new Insert_Text_Here("Insert_Text_Here", depth);
		p.compete(args);
	}
}
