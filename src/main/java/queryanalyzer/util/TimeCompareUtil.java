package queryanalyzer.util;

/**
 * Created with IntelliJ IDEA.
 * User: Katsiaryna Ramanouskaya
 * Date: 12/19/13
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeCompareUtil {

    private static final int dist = 15*60;

    public static boolean  compareTime(String time1, String time2){

        String h1 = time1.substring(12,14);
        String m1 = time1.substring(15,17);
        String s1 = time1.substring(18,20);
        int t1 = Integer.parseInt(h1)*3600+Integer.parseInt(m1)*60+ Integer.parseInt(s1);

        String h2 = time2.substring(12,14);
        String m2 = time2.substring(15,17);
        String s2 = time2.substring(18,20);
        int t2 = Integer.parseInt(h2)*3600+Integer.parseInt(m2)*60+ Integer.parseInt(s2);

        if (t2-t1> dist) return false;

        return true;
    }
}
