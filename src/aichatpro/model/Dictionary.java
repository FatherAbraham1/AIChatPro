package aichatpro.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import sun.misc.Sort;

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
        mSynonym = new HashMap<String, String>();
        mStopWords = new ArrayList<String>();
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
        }
        while(sc.hasNext()) {
            String question = sc.nextLine().toLowerCase();
            String answer = sc.nextLine();
            mQA.put(question, answer);
        }
    }

    /**
     * Membaca daftar stopwords dari file
     * @param filename nama file yang berisi stopwords
     */
    public void readStopwordsFromFile(String filename) {
        mQA = new HashMap<String, String>();
        Scanner sc = null;
        try {
            sc = new Scanner(new FileInputStream(new File(filename)));
        } catch (FileNotFoundException ex) {
        }
        while(sc.hasNext()) {
            String question = sc.nextLine().toLowerCase();
            String answer = sc.nextLine();
            mQA.put(question, answer);
        }
    }

    /**
     * Menghilangkan substring dari input yang termasuk dalam kata-kata stopwords
     * @param input string yang akan dihilangkan stopwordsnya
     * @return string input yang telah dihilangkan stopwords
     */
    public String removeStopWords(String input) {
        return input;
    }

    /**
     * Membuat kandidat pertanyaan dengan mensubtitusi sinonim dari input
     * @return kandidat pertanyaan
     */
    private ArrayList<String> generateQuestionCandidateFromSynonym(String input) {
        ArrayList<String> question = new ArrayList<String>();
        Scanner sc = new Scanner(input);

        while(sc.hasNext()) {
            String x = sc.next();
            if(question.isEmpty()) {
                if(mSynonym.containsKey(x)) {
                    question.add(mSynonym.get(x));
                }
                question.add(x);
            } else {
                if(mSynonym.containsKey(x)) {
                    int size = question.size();
                    for (int i = 0; i < size; i++) {
                        String s = question.get(i);
                        question.add(s.concat(" " + mSynonym.get(x)));
                        s = s.concat(" " + x);
                    }
                } else {
                    for(String s : question)
                        s = s.concat(" " + x);
                }
            }
        }

        return question;
    }

    /**
     * Membuat searchable dari kandidat pertanyaan
     * @param questionCandidate kandidat pertanyaan dari user yang siap diproses
     * @return searcable yang siap untuk dibandingkan dengan FAQ
     */
    private ArrayList<Searchable> generateSearchable(ArrayList<String> questionCandidate) {
        ArrayList<Searchable> ret = new ArrayList<Searchable>();
        for(String s: questionCandidate) {
            Searchable search;
            if(mType == BOOYER_MOORE)
                search = new BM();
            else
                search = new KMP();
            search.setup(s);
            ret.add(search);
        }
        return ret;
    }

    private ArrayList<String> generateQuestionMatch(ArrayList<Searchable> questionCandidate) {
        ArrayList<String> matchFAQ = new ArrayList<String>();
        ArrayList<Integer> confidence = new ArrayList<Integer>();

        // komputasi tiap kandidat dengan FAQ
        for(String qFAQ: mQA.keySet()) {
            for(Searchable S: questionCandidate) {
                if(S.search(qFAQ) != -1) { // match
                    matchFAQ.add(qFAQ);
                    Double d = (double) S.getPattern().length() / (double) qFAQ.length();
                    confidence.add(d.intValue());
                }
            }
        }

        // cari 3 teratas,kemiripan bila ada kemiripan
        String[] matchFAQs = (String[]) matchFAQ.toArray();
        Integer[] confidenceS = (Integer[]) confidence.toArray();
        
        for(int i = 0; i < matchFAQ.size(); i++) {
            int imin = i;
            for (int j = i + 1; j < matchFAQ.size(); j++) {
                if(confidenceS[imin] < confidenceS[j]) {
                    imin = j;
                }
            }
            String temp = matchFAQs[imin];
            Integer temp2 = confidenceS[imin];
            matchFAQs[imin] = matchFAQs[i];
            confidenceS[imin] = confidenceS[i];
            matchFAQs[i] = temp;
            confidenceS[i] = temp2;
        }
        if(matchFAQ.size() > 3) {
            ArrayList<String> ret = new ArrayList<String>();
            ret.add(matchFAQs[0]);
            ret.add(matchFAQs[1]);
            ret.add(matchFAQs[2]);
        }
        return matchFAQ;
    }

    /**
     * Mengambalikan sebuah jawaban berdasarkan FAQ, Sinonim, ignored word dan
     * string matching dengan algoritma Knuth-Morris-Pratt atau Booyer-Moore
     * @param input pertanyaan dari pengguna aplikasi
     * @return jawaban dari aplikasi berdasarkan data yang telah diberikan
     */
    public ArrayList<String> answer(String input) {
        input = input.toLowerCase();
        input = removeStopWords(input);
        ArrayList<Searchable> questionCandidate = generateSearchable(
                generateQuestionCandidateFromSynonym(input));
        ArrayList<String> ans = generateQuestionMatch(questionCandidate); // berat
        return ans;
    }

    private ArrayList<Searchable> mQuestion;
    private HashMap<String, String> mQA;
    private HashMap<String, String> mSynonym;
    private ArrayList<String> mStopWords;
    private int mType;

    public static final int KNUTH_MORRIS_PRATT = 0;
    public static final int BOOYER_MOORE = 1;
}