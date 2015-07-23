package oncecenter.util.decryptPassword;

public class Decrypt {
	private static final char C1 = 'a';
	private static final char C2 = 'z';
	private static final char C3 = 'A';
	private static final char C4 = 'Z';
	private static int key = 2;

	public static String getString(String str) {
		key %= 26;
		if (key == 0) {
			return str;
		}
		char[] chars = str.toCharArray();
		for (int i = chars.length - 1; i >= 0; i--) {
			if (chars[i] >= C3 && chars[i] <= C4) {
				chars[i] += key;
				if (chars[i] > C4) {
					chars[i] = (char) (chars[i] - C4 + C3 - 1);
				} else if (chars[i] < C3) {
					chars[i] = (char) (C4 - (C3 - chars[i]) + 1);
				}
			} else if (chars[i] >= C1 && chars[i] <= C2) {
				chars[i] += key;
				if (chars[i] > C2) {
					chars[i] = (char) (chars[i] - C2 + C1 - 1);
				} else if (chars[i] < C1) {
					chars[i] = (char) (C2 - (C1 - chars[i]) + 1);
				}
			}
		}
		return new String(chars);
	}
	
	public static void main(String[] args) {
		System.out.println(Decrypt.getString("kw fcypr ugjj em ml"));
	}
}
