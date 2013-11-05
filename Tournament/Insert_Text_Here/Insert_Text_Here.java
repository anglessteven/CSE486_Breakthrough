package Insert_Text_Here;

import java.util.ArrayList;
import java.util.Collections;

import breakthrough.BreakthroughMove;
import breakthrough.BreakthroughState;
import game.*;

public class Insert_Text_Here extends GamePlayer {
	public final int MAX_DEPTH = 50;
	public int depthLimit = 6;
	public static final int MAX_SCORE = Integer.MAX_VALUE;
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

	public GameMove getMove(GameState brd, String lastMove) {
		alphaBeta((BreakthroughState) brd, 0,
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		System.out.println(mvStack[0].score);
		return mvStack[0];
		// return null;
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

	private void alphaBeta(BreakthroughState brd, int currDepth, double alpha,
			double beta) {
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean toMinimize = !toMaximize;

		boolean isTerminal = terminalValue(brd, mvStack[currDepth]);

		if (isTerminal) {
			return;
		} else if (currDepth == depthLimit) {
			synchronized (mvStack) {
				mvStack[currDepth].setScore(evalBoard(brd));
				mvStack.notifyAll();
			}
		} else {
			double bestScore = (toMaximize ? Double.NEGATIVE_INFINITY
					: Double.POSITIVE_INFINITY);
			ScoredBreakthroughMove bestMove = mvStack[currDepth];
			ScoredBreakthroughMove nextMove = mvStack[currDepth + 1];

			bestMove.setScore(bestScore);
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
							synchronized (moves) {
								moves.add((BreakthroughMove) mv.clone());
								moves.notifyAll();
							}
						}
						mv.endingRow = r + dir;
						mv.endingCol = c + 1;
						if (brd.moveOK(mv)) {
							synchronized (moves) {
								moves.add((BreakthroughMove) mv.clone());
								moves.notifyAll();
							}
							// System.out.println("Move: " + mv.endingRow +
							// " | " + mv.endingCol);
						}
						mv.endingRow = r + dir;
						mv.endingCol = c - 1;
						if (brd.moveOK(mv)) {
							synchronized (moves) {
								moves.add((BreakthroughMove) mv.clone());
								moves.notifyAll();
							}
							// System.out.println("Move: " + mv.endingRow +
							// " | " + mv.endingCol);
						}
					}
				}
			}

			synchronized (moves) {
				Collections.shuffle(moves);
				moves.notifyAll();
			}
			// for (BreakthroughMove tempMv : moves) {
			while (moves.size() > 0) {
				BreakthroughMove tempMv = null;
				synchronized (moves) {
					tempMv = moves.remove(0);
					moves.notifyAll();
				}
				// Before move, store what type of board square existed there
				char prevPiece = brd.board[tempMv.endingRow][tempMv.endingCol];
				synchronized (brd) {
					brd.makeMove(tempMv);

					alphaBeta(brd, currDepth + 1, alpha, beta); // Check out
																// move

					// Undo move
					brd.board[tempMv.endingRow][tempMv.endingCol] = prevPiece;
					brd.board[tempMv.startRow][tempMv.startCol] = me;
					brd.numMoves--;
					brd.status = GameState.Status.GAME_ON;
					brd.who = currTurn;

					brd.notifyAll();
				}

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
	 * Determines if a board represents a completed game. If it is, the
	 * evaluation values for these boards is recorded (i.e., 0 for a draw +X,
	 * for a HOME win and -X for an AWAY win.
	 * 
	 * @param brd
	 *            Breakthrough board to be examined
	 * @param mv
	 *            where to place the score information; column is irrelevant
	 * @return true if the brd is a terminal state
	 */
	protected boolean terminalValue(GameState brd, ScoredBreakthroughMove mv) {
		GameState.Status status = brd.getStatus();
		boolean isTerminal = true;

		if (status == GameState.Status.HOME_WIN) {
			mv.setScore(MAX_SCORE); // 0?
			// System.out.println("HOME_WIN:" + mv.score);
		} else if (status == GameState.Status.AWAY_WIN) {
			mv.setScore(-MAX_SCORE);
			// System.out.println("AWAY_WIN:" + mv.score);
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
	private static int eval(BreakthroughState brd, char who) {
		// initializing all the variables I need on order to count the score,
		int score = 0;

		// various eval functions for loop
		// ideas: check different offensive/defensive configs?
		// check furthest player?
		// clumping
		for (int r = 0; r < BreakthroughState.N; r++) {
			for (int c = 0; c < BreakthroughState.N; c++) {
				int left = c - 1;
				int right = c + 1;
				/* Check for x_x_x in last row */
				if (left >= 0 && right < BreakthroughState.N) {
					if (brd.board[r][left] == BreakthroughState.emptySym
							&& brd.board[r][right] == BreakthroughState.emptySym) {
						if ((who == BreakthroughState.awaySym
								&& brd.board[r][c] == who && r == 6)
								|| (who == BreakthroughState.homeSym
										&& brd.board[r][c] == who && r == 0)) {
							score--;
						}
					}
				}
				/* check adjacent */
				int next = c + 1;
				if (next < BreakthroughState.N && brd.board[r][c] == who
						&& brd.board[r][next] == who)
					score++;
				/* check behind */
				int nextFront = r + 1;
				if (nextFront < BreakthroughState.N && brd.board[r][c] == who
						&& brd.board[nextFront][c] == who)
					score++;
				/* value having more players */
				if (who == brd.board[r][c])
					score++;
			}
		}
		return score;
	}

	/**
	 * The evaluation function
	 * 
	 * @param brd
	 *            board to be evaluated
	 * @return Home evaluation - Away evaluation
	 */
	public static int evalBoard(BreakthroughState brd) {
		int score = eval(brd, BreakthroughState.homeSym)
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
