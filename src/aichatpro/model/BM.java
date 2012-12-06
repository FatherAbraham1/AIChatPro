package aichatpro.model;

/**
 *
 * @author gmochid
 */
public class BM implements Searchable {

    @Override
    public void setup(String pattern) {
        mPattern = pattern;

        mRight = new int[lim];
        for (int i = 0; i < lim; i++)
            mRight[i] = -1;
        for (int i = 0; i < mPattern.length(); i++)
            mRight[mPattern.charAt(i)] = i;
    }

    @Override
    public int search(String s) {
        int n = mPattern.length();
        int m = s.length();
        int skip;
        for (int i = 0; i <= m - n; i += skip) {
            skip = 0;
            for (int j = n - 1; j >= 0; j--) {
                if(mPattern.charAt(j) != s.charAt(i + j)) {
                    skip = Math.max(1, j - mRight[s.charAt(i + j)]);
                    break;
                }
            }
            if(skip == 0) return i;
        }
        return -1;
    }

    private int[] mRight; // menyatakan banyak karakter yang akan diskip
    private String mPattern; //string pattern

    private static final int lim = 256; // limit integer string yang dipakai

    @Override
    public String getPattern() {
        return mPattern;
    }
}
