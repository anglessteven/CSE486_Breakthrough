/**
 * Team A0: Mikey Pete, Steven Angles, Raquel Gonzalez
 * Opening Book object for encoding the board and inverting moves
 */
package Insert_Text_Here;

import java.util.Scanner;

import breakthrough.BreakthroughState;

public class OpeningBook {
	private static final String W = "W";
	private static final String B = "B";
	
	/**
	 * "Compresses" a board string for storing in an move book
	 * @param brd the uncompressed board string
	 * @return the compressed board string
	 */
	public static String encode(String brd) {
		Scanner s = new Scanner(brd);
		StringBuffer sb = new StringBuffer();
		while (s.hasNext()) {
			String line = s.next();
			char next = 'N';
			int pieceCount = 1;
			for (int i=0; (i<line.length()); i++) {
				char cur = line.charAt(i);
				next = (i == line.length()-1) ? 'N' : line.charAt(i+1);
				if (cur != next) {
					if (pieceCount > 1) {
						sb.append(pieceCount);
						sb.append(cur);
					} else {
						sb.append(cur);
					}
					pieceCount = 1;
				} else if (cur == next) {
					pieceCount++;
				}
			}
		}
		s.close();
		return sb.toString();
	}
	
	/**
	 * Takes a board string and flips W and B
	 * @param brd the board
	 * @return the inverted board
	 */
	public static String invert(String brd) {
		StringBuffer inverted = new StringBuffer();
		for (int i=0; (i<brd.length()); i++) {
			String piece = ""+brd.charAt(i);
			if (piece.equals(W)) {
				inverted.append(B);
			} else if (piece.equals(B)) {
				inverted.append(W);
			} else {
				inverted.append(piece);
			}
		}
		return inverted.toString();
	}
	
	/**
	 * Takes a string move and flips it to the opposite perspective
	 * @param move the original move
	 * @return the inverted move
	 */
	public static int[] invertMove(String move) {
		int rStart = Integer.parseInt(""+move.charAt(0));
		int cStart = Integer.parseInt(""+move.charAt(2));
		int rEnd = Integer.parseInt(""+move.charAt(4));
		int cEnd = Integer.parseInt(""+move.charAt(6));
		int[] newMove = {rStart, cStart, rEnd, cEnd};
		for (int i = 0; i<newMove.length; i++) {
			newMove[i] = (BreakthroughState.N - 1) - newMove[i];
		}
		return newMove;
	}
}
