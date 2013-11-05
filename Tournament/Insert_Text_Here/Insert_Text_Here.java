package Insert_Text_Here;

import java.util.ArrayList;
import java.util.Collections;

import breakthrough.BreakthroughMove;
import breakthrough.BreakthroughState;
import game.*;

public class Insert_Text_Here extends GamePlayer {
	public final int MAX_DEPTH = 50;
	public static final int ADJACENT = 10, SUPPORTING = 4, NUM_PIECES = 10, NUM_PIECES_MID = 3, TAKES = -2, GAP_PENALTY = -6;
	public int depthLimit;
	public static final double MAX_SCORE = Integer.MAX_VALUE;
	protected ScoredBreakthroughMove[] mvStack;

	protected class ScoredBreakthroughMove extends BreakthroughMove {
		public ScoredBreakthroughMove(int r1, int c1, int r2, int c2, double s) {
			super(r1, c1, r2, c2);
			score = s;
		}

		public ScoredBreakthroughMove() {
			super(0, 0, 0, 0);
			score = 0;
		}

		public void set(int r1, int c1, int r2, int c2, double s) {
			startRow = r1;
			startCol = c1;
			endingRow = r2;
			endingCol = c2;
			score = s;
		}

		public void setScore(double s) {
			score = s;
		};

		public Object clone() {
			return new ScoredBreakthroughMove(startRow, startCol, endingRow,
					endingCol, score);
		}

		public double score;
	}

	public Insert_Text_Here(String nickname, int depthLimit) {
		super(nickname, new BreakthroughState(), false);
		this.depthLimit = depthLimit;
	}

	@Override
	public GameMove getMove(GameState brd, String lastMove) {
		alphaBeta((BreakthroughState) brd, 0, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);
		System.out.println(mvStack[0].score);
		return mvStack[0];
	}

	private void alphaBeta(BreakthroughState brd, int currDepth, double alpha,
			double beta) {
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean toMinimize = !toMaximize;

		boolean isTerminal = terminalValue(brd, mvStack[currDepth]);

		if (isTerminal) {
			return;
		} else if (currDepth == depthLimit) {
			mvStack[currDepth].setScore(evalBoard(brd)); // 0?
		} else {
			double bestScore = (toMaximize ? Double.NEGATIVE_INFINITY
					: Double.POSITIVE_INFINITY);
			ScoredBreakthroughMove bestMove = mvStack[currDepth];
			ScoredBreakthroughMove nextMove = mvStack[currDepth + 1];

			bestMove.setScore(bestScore); // 0?
			GameState.Who currTurn = brd.getWho();

			// Find valid moves
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
							// System.out.println("Move: " + mv.endingRow +
							// " | " + mv.endingCol);
							moves.add((BreakthroughMove) mv.clone());
						}
						mv.endingRow = r + dir;
						mv.endingCol = c + 1;
						if (brd.moveOK(mv)) {
							moves.add((BreakthroughMove) mv.clone());
							// System.out.println("Move: " + mv.endingRow +
							// " | " + mv.endingCol);
						}
						mv.endingRow = r + dir;
						mv.endingCol = c - 1;
						if (brd.moveOK(mv)) {
							moves.add((BreakthroughMove) mv.clone());
							// System.out.println("Move: " + mv.endingRow +
							// " | " + mv.endingCol);
						}
					}
				}
			}
			
			//Perform Move Ordering
			//Collections.sort(moves, new Comparator<move>(){public int compare( ScoredBreakthroughMove m1, ScoredBreakthroughMove m2){
				//return m1.score > m2.score;
			//}});

			// System.out.println("suffle");

			Collections.shuffle(moves);
			for (BreakthroughMove tempMv : moves) {
				// Before move, store what type of board square existed there
				char prevPiece = brd.board[tempMv.endingRow][tempMv.endingCol];
				brd.makeMove(tempMv);

				alphaBeta(brd, currDepth + 1, alpha, beta); // Check out move

				// Undo move
				brd.board[tempMv.endingRow][tempMv.endingCol] = prevPiece;
				brd.board[tempMv.startRow][tempMv.startCol] = me;
				brd.numMoves--;
				brd.status = GameState.Status.GAME_ON;
				brd.who = currTurn;

				// Check out the results, relative to what we've seen before
				if (toMaximize && nextMove.score > bestMove.score) {
					bestMove.set(tempMv.startRow, tempMv.startCol,
							tempMv.endingRow, tempMv.endingCol, nextMove.score);
				} else if (!toMaximize && nextMove.score < bestMove.score) {
					bestMove.set(tempMv.startRow, tempMv.startCol,
							tempMv.endingRow, tempMv.endingCol, nextMove.score);
				}

				// Update alpha and beta. Perform pruning, if possible.
				if (toMinimize) {
					beta = Math.min(bestMove.score, beta);
					if (bestMove.score <= alpha || bestMove.score == -MAX_SCORE) {
						return;
					}
				} else {
					alpha = Math.max(bestMove.score, alpha);
					if (bestMove.score >= beta || bestMove.score == MAX_SCORE) {
						return;
					}
				}
			}
		}
	}

	/**
	 * Initializes the stack of Moves.
	 */
	public void init() {
		mvStack = new ScoredBreakthroughMove[MAX_DEPTH];
		for (int i = 0; i < MAX_DEPTH; i++) {
			mvStack[i] = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
		}
	}

	/**
	 * Determines if a board represents a completed game. If it is, the
	 * evaluation values for these boards is recorded (i.e., 0 for a draw +X,
	 * for a HOME win and -X for an AWAY win.
	 * 
	 * @param brd
	 *            Connect4 board to be examined
	 * @param mv
	 *            where to place the score information; column is irrelevant
	 * @return true if the brd is a terminal state
	 */
	protected boolean terminalValue(GameState brd, ScoredBreakthroughMove mv) {
		GameState.Status status = brd.getStatus();
		boolean isTerminal = true;

		if (status == GameState.Status.HOME_WIN) {
			mv.setScore(MAX_SCORE);
		} else if (status == GameState.Status.AWAY_WIN) {
			mv.setScore(-MAX_SCORE);
		} else if (status == GameState.Status.DRAW) {
			mv.setScore(0);
		} else {
			isTerminal = false;
		}
		return isTerminal;
	}

	/**
	 * Counts the number of adjacent pairs of spots with same player's piece.
	 * 
	 * @param brd
	 *            board to be evaluated
	 * @param who
	 *            'R' or 'B'
	 * @return number of adjacent pairs equal to who
	 */
	private static double eval(BreakthroughState brd, char who) {
		// initializing all the variables I need on order to count the score,
		double score = 0;
		

		int dir = who == BreakthroughState.homeSym ? +1 : -1;
		int finish = (dir == -1) ?  0 : (BreakthroughState.N -1);
		for(int i = 0; i < BreakthroughState.N; i++){
			if(brd.board[finish][i] == who){
				return MAX_SCORE;
			}
		}
		// various eval functions for loop
		// ideas: check different offensive/defensive configs?
		// check furthest player?
		// clumping
		for (int r = 0; r < BreakthroughState.N; r++) {
			for (int c = 0; c < BreakthroughState.N; c++) {			
				/* Supporting pieces */
				if((dir == -1 && r != BreakthroughState.N-1) || (dir == 1 && r != 0)){
					int row = r + dir;
					int col = c;
					if(inBounds(row, col) && brd.board[r][c] == who && brd.board[r][col] == who){
						score += (SUPPORTING);
					}
					col = c + 1;
					if(inBounds(row, col) && brd.board[r][c] == who && brd.board[r][col] == who){
						score += (SUPPORTING );
					}
					col = c -1;
					if(inBounds(row, col) && brd.board[r][c] == who && brd.board[r][col] == who){
						score += (SUPPORTING);
					}
				}
				/* check for pieces advancing */
				if (who == BreakthroughState.awaySym) {
					if (r < (BreakthroughState.N / 2)
							&& brd.board[r][c] == BreakthroughState.awaySym) {
						score += NUM_PIECES_MID;
					}
				} else if (who == BreakthroughState.awaySym) {
					if (r > (BreakthroughState.N / 2)
							&& brd.board[r][c] == BreakthroughState.homeSym) {
						score += NUM_PIECES_MID;
					}
				}
				
				/* check adjacent */
				int next = c + 1;
				if (next < BreakthroughState.N && brd.board[r][c] == who && brd.board[r][next] == who) {
					score += ADJACENT;
				}
				/* raw # of pieces */
				if (brd.board[r][c] == who) {
					score += NUM_PIECES;
				}
				
//				int left = c - 1;
//				int right = c + 1;
//				/* Check for _x_ in last row */
//				if (left >= 0 && right < BreakthroughState.N) {
//					if (brd.board[r][left] == BreakthroughState.emptySym
//							&& brd.board[r][right] == BreakthroughState.emptySym) {
//						if ((who == BreakthroughState.awaySym
//								&& brd.board[r][c] == who && r == 6)
//								|| (who == BreakthroughState.homeSym
//										&& brd.board[r][c] == who && r == 0)) {
//							score--;
//						}
//					}
//				}
//				/* Last row flanking defense */
//				if (c == 2 || c == 4) {
//					if ((who == BreakthroughState.awaySym
//							&& brd.board[r][c] == who && r == 6)
//							|| (who == BreakthroughState.homeSym
//									&& brd.board[r][c] == who && r == 0)) {
//						if ((brd.board[r][1] == BreakthroughState.emptySym && brd.board[r][0] == BreakthroughState.emptySym)
//								|| (brd.board[r][5] == BreakthroughState.emptySym && brd.board[r][6] == BreakthroughState.emptySym)) {
//							score--;
//						}
//					}
//				}
			}
		}
		return score;
	}
	
	private static boolean inBounds(int row, int col){
		return (col < BreakthroughState.N && col >= 0)&& (row < BreakthroughState.N && row >= 0);
	}
	/**
	 * The evaluation function
	 * 
	 * @param brd
	 *            board to be evaluated
	 * @return Home evaluation - Away evaluation
	 */
	public static double evalBoard(BreakthroughState brd) {
		double score = eval(brd, BreakthroughState.homeSym)
				- eval(brd, BreakthroughState.awaySym);
		if (Math.abs(score) > MAX_SCORE) {
			System.err.println("Problem with eval");
			System.exit(0);
		}
		return score;
	}

	public static void main(String[] args) {
		int depth = 6;
		GamePlayer p = new Insert_Text_Here("Insert_Text_Here", depth);
		p.compete(args);
	}
}
