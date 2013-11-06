package Insert_Text_Here;

import java.util.Scanner;

public class OpeningBook {
	private static final String W = "W";
	private static final String B = "B";
	
	public static String encode(String brd) {
		Scanner s = new Scanner(brd);
		StringBuffer sb = new StringBuffer();
		while (s.hasNext()) {
			String line = s.next();
			char prev = 'N';
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
					prev = cur;
				} else if (cur == next) {
					pieceCount++;
				}
			}
		}
		s.close();
		return sb.toString();
	}
	
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
}
