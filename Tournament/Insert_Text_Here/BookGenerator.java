package Insert_Text_Here;

public class BookGenerator {
	private static int depth = 12;
	private static int moveDepth = 5;
	private static String fileName = "openingbook.dat";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Generating opening book with search depth " + depth
				+ " and move depth " + moveDepth + "...");
		long start = System.currentTimeMillis();
		generateBook();
		System.out.println("Generation took "
				+ (System.currentTimeMillis() - start) + " ms.");
	}

	private static void generateBook() {
		System.out.println(OpeningBook.encode("BBBBBBB\n" +
				"BBBBBB.\n" +
				".......\n" +
				"......B\n" +
				"..W....\n" +
				"WWWWWWW\n" +
				"W.WWWWW\n"));
	}

}
