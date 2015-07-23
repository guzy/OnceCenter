package oncecenter.util;

public class MathUtil {
	
	public static String Rounding(double d,int i){
		 long a=(long)d;
		 long b=(long)((d-a)*Math.pow(10, i));
		 return a+"."+b;
	 }
	
	public static double RoundingDouble(double d,int i){
		 long a=(long)d;
		 long b=(long)((d-a)*Math.pow(10, i));
		 return Double.parseDouble(a+"."+b);
	 }
}
