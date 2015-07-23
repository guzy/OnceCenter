package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class regexTest {

	public static void main(String[] args)
	{
		regexTest cp = new regexTest();
		cp.check("f");
	}
	public boolean check(String path) {
		Pattern pattern = Pattern.compile("(.+):(.+)@([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+):(.*)");
		Matcher matcher = pattern.matcher("dhf:isevil@133.133.134.44:/haha/haha");
		if (matcher.find()) {
			System.out.println(matcher.group(1));
			System.out.println(matcher.group(2));
			System.out.println(matcher.group(3));
			System.out.println(matcher.group(4));
//			System.out.println(matcher.group(5));
//			System.out.println(matcher.group(6));
//			System.out.println(matcher.group(7));

		}
		return true;
	}
}
