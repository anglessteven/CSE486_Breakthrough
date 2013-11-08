/**
 * Team A0: Mikey Pete, Steven Angles, Raquel Gonzalez
 * Scored Breakthrough Move
 */
package Insert_Text_Here;

import breakthrough.BreakthroughMove;

public class ScoredBreakthroughMove extends BreakthroughMove{

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
