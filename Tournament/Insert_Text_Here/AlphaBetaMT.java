/**
 * 
 */
package Insert_Text_Here;

import game.GameState;

import java.util.ArrayList;
import java.util.Collections;

import breakthrough.BreakthroughMove;
import breakthrough.BreakthroughState;

public class AlphaBetaMT extends Thread {
	public final int MAX_DEPTH = 50;
	public static final int MAX_SCORE = Integer.MAX_VALUE;
	public static final int ADJACENT = 3, SUPPORTING = 2, NUM_PIECES = 12, NUM_PIECES_MID = 1, GAP_PENALTY = 3;
	private ScoredBreakthroughMove[] mvStack;
	private ArrayList<BreakthroughMove> moves;
	private int start, end, depthLimit;
	private BreakthroughState brd;

	public AlphaBetaMT(int start, int end, int depthLimit,
			BreakthroughState brd, ArrayList<BreakthroughMove> moves) {
		this.start = start;
		this.end = end;
		this.brd = (BreakthroughState) brd.clone();
		this.depthLimit = depthLimit;
		this.moves = moves;
	}
	
	public ScoredBreakthroughMove getBestMove(){
		return mvStack[0];
	}
	
  	@Override
	public void run() {
  		mvStack = new ScoredBreakthroughMove[MAX_DEPTH];
		for (int i = 0; i < MAX_DEPTH; i++) {
			mvStack[i] = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
		}

		alphaBeta(this.brd, 0, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, true);
	}

	private void alphaBeta(BreakthroughState brd, int currDepth, double alpha,
			double beta, boolean firstLevel) {
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean toMinimize = !toMaximize;

		boolean isTerminal = terminalValue(brd, mvStack[currDepth]);

		if (isTerminal) {
			return;
		} else if (currDepth == depthLimit) {
			mvStack[currDepth].setScore(evalBoard(brd));
		} else {
			double bestScore = (toMaximize ? Double.NEGATIVE_INFINITY
					: Double.POSITIVE_INFINITY);
			ScoredBreakthroughMove bestMove = mvStack[currDepth];
			ScoredBreakthroughMove nextMove = mvStack[currDepth + 1];

			bestMove.setScore(bestScore);
			GameState.Who currTurn = brd.getWho();

			char me = brd.who == BreakthroughState.Who.HOME ? BreakthroughState.homeSym
					: BreakthroughState.awaySym;
			int dir = brd.who == BreakthroughState.Who.HOME ? +1 : -1;
			ArrayList<BreakthroughMove> moves;

			if (firstLevel) {
				moves = this.moves;
			} else {
				// Find valid moves
				moves = new ArrayList<BreakthroughMove>();
				BreakthroughMove mv = new BreakthroughMove();
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
			}
			Collections.shuffle(moves);
			int i = (firstLevel) ? this.start : 0;
			int end = (firstLevel) ? this.end : moves.size();
			for (; i < end; i++) {
				BreakthroughMove tempMv = moves.get(i);
				// Before move, store what type of board square existed there
				char prevPiece = brd.board[tempMv.endingRow][tempMv.endingCol];
				brd.makeMove(tempMv);

				alphaBeta(brd, currDepth + 1, alpha, beta, false); // Check out
				// move

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
		
		// various eval functions for loop
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
                                score += (SUPPORTING);
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
                ///////////////////////////////////PENALTIES////////////////////////////////////////////
				/* Check for _x_ in last row */
				int left = c - 1;
				int right = c + 1;
				if (left >= 0 && right < BreakthroughState.N) {
					if (brd.board[r][left] == BreakthroughState.emptySym
							&& brd.board[r][right] == BreakthroughState.emptySym) {
						if ((who == BreakthroughState.awaySym
								&& brd.board[r][c] == who && r == 6)
								|| (who == BreakthroughState.homeSym
										&& brd.board[r][c] == who && r == 0)) {
							score -= GAP_PENALTY;
						}
					}
				}
				
				/* Last row flanking defense */
				if (c == 2 || c == 4) {
					if ((who == BreakthroughState.awaySym
							&& brd.board[r][c] == who && r == 6)
							|| (who == BreakthroughState.homeSym
									&& brd.board[r][c] == who && r == 0)) {
						if ((brd.board[r][1] == BreakthroughState.emptySym && brd.board[r][0] == BreakthroughState.emptySym)
								|| (brd.board[r][5] == BreakthroughState.emptySym && brd.board[r][6] == BreakthroughState.emptySym)) {
							score-= GAP_PENALTY;
						}
					}
				}
				
			} //end c for
		} //end r for
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
}
