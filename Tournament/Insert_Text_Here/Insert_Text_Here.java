/**
 * Team A0: Mikey Pete, Steven Angles, Raquel Gonzalez
 * Game Player
 */
package Insert_Text_Here;

import game.GameMove;
import game.GamePlayer;
import game.GameState;
import game.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import breakthrough.BreakthroughMove;
import breakthrough.BreakthroughState;

public class Insert_Text_Here extends GamePlayer {
	private static final int DEPTH_LIMIT = 7;
	private int depthLimit;
	private static final int NUMTHREADS = Runtime.getRuntime()
			.availableProcessors();
	private Scanner book = null;
	private String fileName = "openingbook.dat";
	private HashMap<String, String> map = new HashMap<String, String>();
	private double gameTime = 360.0;

	public Insert_Text_Here(String nickname, int depthLimit) {
		super(nickname, new BreakthroughState(), false);
		this.depthLimit = depthLimit;
	}

	public void endGame(int result) {
		depthLimit = DEPTH_LIMIT;
		gameTime = 360.0;
	}
	
	public void timeOfLastMove(double s) {
		gameTime -= s;
	}
	
	public GameMove getMove(GameState brd, String lastMove) {
		// go into "safe mode" if the game time is low
		if (gameTime <= 60) {
			depthLimit = 6;
		}
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		BreakthroughState tmp = (BreakthroughState) brd;
		// check if the board state is covered by the opening book
		String code = OpeningBook.encode(Util.toString(tmp.board));
		if (map.containsKey(code)) {
			String move = map.get(code);
			return new ScoredBreakthroughMove(Integer.parseInt(""
					+ move.charAt(0)), Integer.parseInt("" + move.charAt(2)),
					Integer.parseInt("" + move.charAt(4)), Integer.parseInt(""
							+ move.charAt(6)), 0);
		} else if (map.containsKey(OpeningBook.invert(code))) {
			String move = map.get(OpeningBook.invert(code));
			int[] moveMap = OpeningBook.invertMove(move);
			return new ScoredBreakthroughMove(moveMap[0], moveMap[1],
					moveMap[2], moveMap[3], 0);
		}
		// Create threads to run alpha beta if the board configuration
		// is not in the opening book
		ArrayList<ScoredBreakthroughMove> moves = new ArrayList<ScoredBreakthroughMove>();
		try {
			moves = runThreads((BreakthroughState) brd);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// choose the best move from what the threads returned
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

	/**
	 * Method to generate all possible moves from given board state
	 * and split up the work across threads
	 * @param brd the given board state
	 * @return An arraylist of each threads' best move
	 * @throws InterruptedException
	 */
	private ArrayList<ScoredBreakthroughMove> runThreads(BreakthroughState brd)
			throws InterruptedException {
		ArrayList<BreakthroughMove> moves = new ArrayList<BreakthroughMove>();
		BreakthroughMove mv = new BreakthroughMove();
		char me = brd.who == BreakthroughState.Who.HOME ? BreakthroughState.homeSym
				: BreakthroughState.awaySym;
		int dir = brd.who == BreakthroughState.Who.HOME ? +1 : -1;
		// generate all possible initial moves
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
		
		// merge threads' moves into single arraylist
		for (int i = 0; i < NUMTHREADS; i++) {
			alphaBetaMoves.add(thrList[i].getBestMove());
		}

		return alphaBetaMoves;
	}

	public void init() {
		// add all opening book's moves to hashmap
		try {
			book = new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = null;
		while (book.hasNext()) {
			line = book.nextLine();
			int space = line.indexOf(' ');
			String key = line.substring(0, space);
			String value = line.substring(space + 1, line.length());
			map.put(key, value);
		}
	}

	public static void main(String[] args) {
		int depth = DEPTH_LIMIT;
		GamePlayer p = new Insert_Text_Here("Insert_Text_Here", depth);
		p.compete(args);
	}
}