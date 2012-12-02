package aichatpro.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gmochid
 */
public class Dictionary {
    public Dictionary(int type) {
        mQuestion = new ArrayList<Searchable>();
        mQA = new HashMap<String, String>();
    }

    public void readFAQFromFile(String filename) {
        Scanner sc = null;
        try {
            sc = new Scanner(new FileInputStream(new File(filename)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Dictionary.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(sc.hasNext()) {
            String question = sc.nextLine().toLowerCase();
            String answer = sc.nextLine();
            mQA.put(question, answer);
        }
    }

    public String answer(String input) {
        input = input.toLowerCase();
        int half = input.length() / 2;
        System.out.println(half);
        String s;
        for (int i = 0; i < half / 2; i++) {
            s = input.substring(i, input.length() - i);
            System.out.println(s);
            Searchable sb = (mType == KNUTH_MORRIS_PRATT) ? new KMP() : new BM();
            sb.setup(s);
            mQuestion.add(sb);
        }

        for (int i = 0; i < half; i++) {
            s = input.substring(0, input.length() - i);
            System.out.println(s);
            Searchable sb = (mType == KNUTH_MORRIS_PRATT) ? new KMP() : new BM();
            sb.setup(s);
            mQuestion.add(sb);

            s = input.substring(i, input.length());
            System.out.println(s);
            sb = (mType == KNUTH_MORRIS_PRATT) ? new KMP() : new BM();
            sb.setup(s);
            mQuestion.add(sb);
        }

        for (String key : mQA.keySet())
            for (Searchable sb : mQuestion)
                if(sb.search(key) != -1)
                    return mQA.get(key);
        return "Apakah yang anda maksud";
    }

    private ArrayList<Searchable> mQuestion;
    private HashMap<String, String> mQA;
    private int mType;

    public static final int KNUTH_MORRIS_PRATT = 0;
    public static final int BOOYER_MOORE = 1;

    public static void main(String[] args) {
        
    }
}
