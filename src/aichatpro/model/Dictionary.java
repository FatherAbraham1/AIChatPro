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
    /**
     * MEmbuat objek dictionary yang akan memproses pertanyaan menjadi jawaban
     * @param type tipe algoritma yang akan dipakai dalam proses string matching
     */
    public Dictionary(int type) {
        mQuestion = new ArrayList<Searchable>();
        mQA = new HashMap<String, String>();
        mType = type;
    }

    /**
     * Membaca daftar sinonim dari sebuah file
     * @param filename path ke file yang berisi daftar sinonim
     */
    public void readSynonymFromFile(String filename) {
        mSynonym = new HashMap<String, String>();
        Scanner sc = null;
        try {
            sc = new Scanner(new FileInputStream(new File(filename)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Dictionary.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(sc.hasNext()) {
            String a = sc.next().toLowerCase();
            String b = sc.next().toLowerCase();
            mSynonym.put(a, b);
            mSynonym.put(b, a);
        }
    }

    /**
     * Membaca Frequently Asked Question dari file
     * FAQ ini adalah dasar dari jawaban aplikasi AIChatBot ini
     * @param filename nama file yang berisi FAQ
     */
    public void readFAQFromFile(String filename) {
        mQA = new HashMap<String, String>();
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

    /**
     * Mengambalikan sebuah jawaban berdasarkan FAQ, Sinonim, ignored word dan
     * string matching dengan algoritma Knuth-Morris-Pratt atau Booyer-Moore
     * @param input pertanyaan dari pengguna aplikasi
     * @return jawaban dari aplikasi berdasarkan data yang telah diberikan
     */
    public String answer(String input) {
        input = input.toLowerCase();

        generateQuestionCandidate(input);

        String ret = searchOnFAQ();
        if(ret == null) {
            ArrayList<String> syn = generateSysnonymQuestion(input);
            generateQuestionCandidate(syn);
        }
        
        return ret;
    }

    /**
     * Membuat kemungkinan-kemungkinan perbedaan FAQ dengan pertanyaan user
     * dengan metode per karakter
     * @param candidate daftar kandidat kemungkinan pertanyaan user
     */
    private void generateQuestionCandidate(ArrayList<String> candidate) {
        mQuestion = new ArrayList<Searchable>();
        for(String input : candidate) {
            int half = input.length() / 2;
            String s;
            for (int i = 0; i < half / 2; i++) {
                s = input.substring(i, input.length() - i);
                //System.out.println(s);
                Searchable sb = (mType == KNUTH_MORRIS_PRATT) ? new KMP() : new BM();
                sb.setup(s);
                mQuestion.add(sb);
            }

            for (int i = 0; i < half; i++) {
                s = input.substring(0, input.length() - i);
                //System.out.println(s);
                Searchable sb = (mType == KNUTH_MORRIS_PRATT) ? new KMP() : new BM();
                sb.setup(s);
                mQuestion.add(sb);

                s = input.substring(i, input.length());
                //System.out.println(s);
                sb = (mType == KNUTH_MORRIS_PRATT) ? new KMP() : new BM();
                sb.setup(s);
                mQuestion.add(sb);
            }
        }
    }

    private void generateQuestionCandidate(String input) {
        mQuestion = new ArrayList<Searchable>();
        int half = input.length() / 2;
        String s;
        for (int i = 0; i < half / 2; i++) {
            s = input.substring(i, input.length() - i);
            //System.out.println(s);
            Searchable sb = (mType == KNUTH_MORRIS_PRATT) ? new KMP() : new BM();
            sb.setup(s);
            mQuestion.add(sb);
        }

        for (int i = 0; i < half; i++) {
            s = input.substring(0, input.length() - i);
            //System.out.println(s);
            Searchable sb = (mType == KNUTH_MORRIS_PRATT) ? new KMP() : new BM();
            sb.setup(s);
            mQuestion.add(sb);

            s = input.substring(i, input.length());
            //System.out.println(s);
            sb = (mType == KNUTH_MORRIS_PRATT) ? new KMP() : new BM();
            sb.setup(s);
            mQuestion.add(sb);
        }
    }

    /**
     * Membuat semua kemungkinan pertanyaan dari daftar sinonim yang diberikan
     * @param input input dari user
     * @return daftar kemungkinan lain pertanyaan dari user
     */
    private ArrayList<String> generateSysnonymQuestion(String input) {
        ArrayList<String> ret = new ArrayList<String>();
        Scanner sc = new Scanner(input);

        while(sc.hasNext()) {
            String x = sc.next();
            if(ret.isEmpty()) {
                if(mSynonym.containsKey(x)) {
                    ret.add(mSynonym.get(x));
                }
                ret.add(x);
            } else {
                if(mSynonym.containsKey(x)) {
                    int size = ret.size();
                    for (int i = 0; i < size; i++) {
                        String s = ret.get(i);
                        ret.add(s.concat(" " + mSynonym.get(x)));
                        s = s.concat(" " + x);
                    }
                } else {
                    for(String s : ret)
                        s = s.concat(" " + x);
                }
            }
        }
        
        return ret;
    }

    private String searchOnFAQ() {
        for (String key : mQA.keySet())
            for (Searchable sb : mQuestion)
                if(sb.search(key) != -1)
                    return mQA.get(key);
        return null;
    }

    private ArrayList<Searchable> mQuestion;
    private HashMap<String, String> mQA;
    private HashMap<String, String> mSynonym;
    private int mType;

    public static final int KNUTH_MORRIS_PRATT = 0;
    public static final int BOOYER_MOORE = 1;
}
