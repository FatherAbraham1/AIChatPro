package aichatpro.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

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
        mStopWords = new ArrayList<String>();
        Scanner sc = null;
        try {
            sc = new Scanner(new FileInputStream(new File(filename)));
        } catch (FileNotFoundException ex) {
        }
        while(sc.hasNext()) {
            String stopword = sc.nextLine().toLowerCase();
            mStopWords.add(stopword);
        }
    }

    /**
     * Menghilangkan substring dari input yang termasuk dalam kata-kata stopwords
     * @param input string yang akan dihilangkan stopwordsnya
     * @return string input yang telah dihilangkan stopwords
     */
    public String removeStopWords(String input) {
        for(String s: mStopWords) {
            CharSequence cs = new StringBuffer(s);
            input = input.replace(cs, "");
        }
        input = input.replace("  ", " ");
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
                    int size = question.size();
                    for(int i = 0; i < size; i++) {
                        String s = question.get(i);
                        s = s.concat(" " + x);
                        question.remove(i);
                        question.add(i, s);
                    }
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

    /**
     * Memilih kandidat pertanyaan yang bersesuaian dengan FAQ yang ada
     * @param questionCandidate kandidat pertanyaan beserta semua sinonimnya
     * @return pertanyaan yang bersesuaian dengan FAQ terurut dari yang
     * paling tinggi kesesuaiannya (max 3)
     */
    private ArrayList<String> generateQuestionMatch(ArrayList<Searchable> questionCandidate) {
        ArrayList<String> matchFAQ = new ArrayList<String>();
        confidence = new ArrayList<Integer>();

        // komputasi tiap kandidat dengan FAQ
        for(String qFAQ: mQA.keySet()) {
            for(Searchable S: questionCandidate) {
                if(S.search(qFAQ) != -1) { // match
                    Double d = (double) S.getPattern().length() / (double) qFAQ.length() * (double) 100;
                    matchFAQ.add(qFAQ);
                    confidence.add(d.intValue());
                }
            }
        }
        
        // cari 3 teratas,kemiripan bila ada kemiripan
        for(int i = 0; i < matchFAQ.size(); i++) {
            int imin = i;
            for (int j = i + 1; j < matchFAQ.size(); j++) {
                if(confidence.get(imin) < confidence.get(j)) {
                    imin = j;
                }
            }
            String temp = matchFAQ.get(imin);
            Integer temp2 = confidence.get(imin);
            matchFAQ.set(imin, matchFAQ.get(i));
            confidence.set(imin, confidence.get(i));
            matchFAQ.set(imin, temp);
            confidence.set(imin, temp2);
        }
        if(matchFAQ.size() > 3) {
            ArrayList<String> ret = new ArrayList<String>();
            ret.add(matchFAQ.get(0));
            ret.add(matchFAQ.get(1));
            ret.add(matchFAQ.get(2));
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
        ArrayList<String> q = generateQuestionMatch(questionCandidate); // berat

        ArrayList<String> ans = new ArrayList<String>();
        if(q.size() == 1) {
            if(confidence.get(0) < 90) {
                ans.add(q.get(0));
                ans.add(null);
                ans.add(null);
            } else {
                ans.add(mQA.get(q.get(0)));
            }
        } else if(q.size() == 2) {
            if(((confidence.get(0) >= 90) && (confidence.get(1) >= 90)) ||
                    ((confidence.get(0) < 90) && (confidence.get(1) < 90))){
                ans.add(q.get(0));
                ans.add(q.get(1));
                ans.add(null);
            } else  {
                ans.add(mQA.get(q.get(0)));
            }
        } else if(q.size() == 3) {
            if(((confidence.get(0) >= 90) && (confidence.get(1) >= 90) && (confidence.get(2) >= 90)) ||
                    ((confidence.get(0) < 90) && (confidence.get(1) < 90) && (confidence.get(2) < 90))){
                ans.add(q.get(0));
                ans.add(q.get(1));
                ans.add(q.get(2));
            } else if((confidence.get(0) >= 90) && (confidence.get(1) >= 90) && (confidence.get(2) < 90)){
                ans.add(q.get(0));
                ans.add(q.get(1));
                ans.add(null);
            } else { //((confidence.get(0) >= 90) && (confidence.get(1) < 90) && (confidence.get(2) < 90))
                ans.add(mQA.get(q.get(0)));
            }
        }
        return ans;
    }

    /**
     * Cari pada FAQ, pertanyaan yang bersesuaian
     * @param input input dijamin ada pada FAQ
     * @return jawaban berdasarkan FAQ
     */
    public String justAnswer(String input) {
        return mQA.get(input);
    }

    /**
     * Mengembalikan persentase keyakinan dari urutan match FAQ ke i
     * @return persentase keyakinan dari urutan match FAQ ke i
     */
    public Integer getConfidence(Integer i) {
        return confidence.get(i);
    }

    public static void main(String[] args) {
        Dictionary dict = new Dictionary(BOOYER_MOORE);
        dict.readStopwordsFromFile("D:/stopwords.txt");
        System.out.println(dict.removeStopWords("apakah itu dia"));
    }

    private HashMap<String, String> mQA;
    private HashMap<String, String> mSynonym;
    private ArrayList<Integer> confidence;
    private ArrayList<String> mStopWords;
    private int mType;
    private int sure;

    public static final int KNUTH_MORRIS_PRATT = 0;
    public static final int BOOYER_MOORE = 1;
}