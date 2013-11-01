package clobber;
import game.*;

public abstract class BaseClobberPlayer extends GamePlayer {
	public static int ROWS = ClobberState.ROWS;
	public static int COLS = ClobberState.COLS;
	public static final int MAX_SCORE = 1432894;
	public BaseClobberPlayer(String nickname, 
			boolean isDeterministic) {
		super(nickname, new ClobberState(), isDeterministic);
	}

	public static int evalBoard(ClobberState brd)
	{
		return eval(brd, ClobberState.homeSym) - eval(brd, ClobberState.awaySym);
	}
	private static int other(ClobberState brd, char who, int r, int c)
	{
		if (r >= 0 && r < ClobberState.ROWS &&
			c >= 0 && c < ClobberState.COLS && brd.board[r][c] == who) {
			return 1;
		} else {
			return 0;
		}
	}
	private static int eval(ClobberState brd, char who) {
		int count = 0;
		for(int r = 0; r < ROWS; r++) {
			for(int c = 0; c < COLS; c++) {
				if (brd.board[r][c] == who) {
					count += other(brd, who, r+1, c);
					count += other(brd, who, r-1, c);
					count += other(brd, who, r, c+1);
					count += other(brd, who, r, c-1);
				}
			}
		}
		
		return count;
	}
}
