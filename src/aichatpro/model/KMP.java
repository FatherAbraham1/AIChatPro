package aichatpro.model;

/**
 *
 * @author gmochid
 */
public class KMP implements Searchable {
    @Override
    public void setup(String pattern) {
        mPattern = pattern;

        int n = mPattern.length();
        mDFA = new int[lim][n];
        mDFA[pattern.charAt(0)][0] = 1;
        for (int i = 0, j = 0; i < n; i++) {
            for (int k = 0; k < lim; k++)
                mDFA[k][i] = mDFA[k][j];
            mDFA[mPattern.charAt(i)][i] = i + 1;
            j = mDFA[mPattern.charAt(i)][j];
        }
    }

    @Override
    public int search(String s) {
        int n = mPattern.length();
        int m = s.length();
        int i, j;
        for (i = 0, j = 0; i < m && j < n; i++) {
            j = mDFA[s.charAt(i)][j];
        }
        if(j == n)  return i - n;
        return -1;
    }

    private String mPattern; // pattern dasar
    private int[][] mDFA; // finite automata

    private static final int lim = 256; // limit integer string yang dipakai

    public static void main(String[] args) {
        KMP s = new KMP();
        s.setup("aaaaa");
        System.out.println(s.search("aaaaaaaaa"));
        System.out.println(s.mPattern);
    }

    @Override
    public String getPattern() {
        return mPattern;
    }
}
