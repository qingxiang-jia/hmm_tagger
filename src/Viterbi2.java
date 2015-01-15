import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class contains solutions to Question 6. I used an approach that
 * is similar to the one discussed in Advanced Topic of this file.
 * (http://www.cs.columbia.edu/~mcollins/hmms-spring2013.pdf)
 * This is also what is required by Question 6.
 */
public class Viterbi2
{
    /**
     * Define a set of integers to represent tags.
     */
    static final int STAR  = 0;
    static final int IPER  = 1;
    static final int IORG  = 2;
    static final int ILOC  = 3;
    static final int IMISC = 4;
    static final int BPER  = 5;
    static final int BORG  = 6;
    static final int BLOC  = 7;
    static final int BMISC = 8;
    static final int O     = 9;
    static final int STOP  = 10;

    static final int maxSenLen = 200; // Maximum sentence length, you can increase it if sentences are too long

    HashMap<String, EntryLine> emiPara; // The hash table stores emission probability
    HashMap<Integer, String> g; // Define a set of hash tables to convert the
    HashMap<Integer, Integer> e; // tag integer representation to string representation, and vice versa.
    double[][][] tranProb; // Three-dimensional array to store transition probability

    int[] K_minus_1, K0, Kk; // tag sets

    public Viterbi2()
    {
        /** Prepare tag sets **/
        K_minus_1 = new int[]{STAR};
        K0        = K_minus_1;
        Kk        = new int[]{IPER, IORG, ILOC, IMISC, BPER, BORG, BLOC, BMISC, O}; // for n = 1...
        /** Set up int to tag hash table **/
        g = new HashMap<Integer, String>();
        g.put(STAR, "*");
        g.put(IPER, "I-PER");
        g.put(IORG, "I-ORG");
        g.put(ILOC, "I-LOC");
        g.put(IMISC, "I-MISC");
        g.put(BPER, "B-PER");
        g.put(BORG, "B-ORG");
        g.put(BLOC, "B-LOC");
        g.put(BMISC, "B-MISC");
        g.put(O, "O");
        g.put(STOP, "STOP");
        /** Set up tag translation table between Entry and others **/
        e = new HashMap<Integer, Integer>();
        e.put(IPER, 0);
        e.put(IORG, 1);
        e.put(ILOC, 2);
        e.put(IMISC, 3);
        e.put(BPER, 4);
        e.put(BORG, 5);
        e.put(BLOC, 6);
        e.put(BMISC, 7);
        e.put(O, 8);
        init();
    }

    public void init()
    {
        /** Retrieve emission probability **/
        BaselineTagger bTagger = new BaselineTagger();
        //emiPara = bTagger.compEmiPara("/Users/lee/Dropbox/NLP/Problem Set 1/Programming/ner.pseudo_word.counts");
        emiPara = bTagger.compEmiPara("./ner.pseudo_word.counts");
        /** Retrieve transitional probability **/
        HMMTagger hTagger = new HMMTagger();
        //tranProb = hTagger.compTranProb("/Users/lee/Dropbox/NLP/Problem Set 1/Programming/ner.pseudo_word.counts");
        tranProb = hTagger.compTranProb("./ner.pseudo_word.counts");
    }

    /**
     * Read in the file contains sentences, ner_dev.dat.
     * @param fn File name of the file containing sentences, usually named ner_dev.dat.
     * @return An array list of lines of the file content
     */
    public ArrayList<String> readSentenceFile(String fn)
    {
        ArrayList<String> sentences = new ArrayList<String>();
        // read in sentence file
        BufferedReader bfReader;
        try
        {
            bfReader = new BufferedReader(new FileReader(fn));
            StringBuilder strGen = new StringBuilder();
            for (String line = bfReader.readLine(); line != null; line = bfReader.readLine())
                if(!line.equals(""))
                    strGen.append(line+" ");
                else
                {
                    String sen = strGen.toString();
                    sentences.add(sen.substring(0, sen.length() - 1)); // not including the trailing space
                    strGen = new StringBuilder();
                }
            bfReader.close();
        } catch (IOException e) { e.printStackTrace(); }
        return sentences;
    }

    /**
     * Output the tagged sentences in a file, named Viterbi2Tagged.
     * @param results An array list of ViterbiReturn objects.
     */
    public void writeResultToFile(ArrayList<ViterbiReturn> results)
    {
        ArrayList<String> lines = new ArrayList<String>();
        /** Repackage results **/
        for(ViterbiReturn result: results)
        {
            String[] sentence = result.sentence.split("\\s+");
            for(int i = 0; i < sentence.length; i++)
                lines.add(sentence[i]+" "+result.viterbiResult.get(i)+" "+Math.log(result.viterbiProb.get(i))/Math.log(2));
            lines.add(""); // add a new line after each sentence
        }
        // write back to a new file
        PrintWriter writer;
        try
        {
            writer = new PrintWriter("Viterbi2Tagged", "UTF-8");
            for(String line: lines)
                writer.println(line);
            writer.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); }
    }

    /**
     * Return value of the Viterbi algorithm.
     * viterbiResult - stores the tags.
     * viterbiProb   - stores the log probability up to that word.
     * sentence      - the original sentence
     */
    private class ViterbiReturn
    {
        ArrayList<String> viterbiResult;
        ArrayList<Double> viterbiProb;
        String sentence;
    }

    /**
     * Implementation of Viterbi algorithm
     * @param sentence A sentence to be tagged.
     * @return ViterbiReturn, see ViterbiReturn block.
     */
    public ViterbiReturn act(String sentence)
    {
        /** Initialize return array **/
        ArrayList<String> viterbiResult = new ArrayList<String>();
        ArrayList<Double> viterbiProb   = new ArrayList<Double>();
        /** Initialize pi and bp **/
        double[][][] pi = new double[maxSenLen][Kk.length+2][Kk.length+2];
        int   [][][] bp = new int   [maxSenLen][Kk.length+2][Kk.length+2];
        pi[1][STAR][STAR] = 1.0;
        /** Tokenize sentence **/
        String[] temp = sentence.split("\\s+");
        String[] sen = new String[temp.length+3];
        for(int i = 2; i < sen.length-1; i++) { sen[i] = temp[i-2]; }
        sen[0] = "*"; sen[1] = "*";
        sen[sen.length-1] = "STOP";

        int maxUTag = 0;
        int maxVTag = 0;
        /** Special case: one word sentence **/
        if(temp.length == 1)
        {
            MaxTag mt = new MaxTag();
            Tag maxTag;
            double prob;
            if(emiPara.get(sentence) == null)
                if(sentence.matches("[A-Z][a-z]*"))
                    maxTag = mt.getMax(emiPara.get("_RARE_IPER_").tags);
                else if(sentence.matches("[A-Z][a-z]*[-]*[A-Z][a-z]*"))
                    maxTag = mt.getMax(emiPara.get("_RARE_IPER_").tags);
                else if(sentence.matches("[A-Z]*"))
                    maxTag = mt.getMax(emiPara.get("_RARE_ILOC_").tags);
                else if(sentence.contains("&"))
                    maxTag = mt.getMax(emiPara.get("_RARE_IORG").tags);
                else
                    maxTag = mt.getMax(emiPara.get("_RARE_").tags);
            else
                maxTag = mt.getMax(emiPara.get(sentence).tags);
            viterbiResult.add(maxTag.tag);
            viterbiProb.add(maxTag.prob);
            ViterbiReturn result = new ViterbiReturn();
            result.viterbiProb = viterbiProb;
            result.viterbiResult = viterbiResult;
            result.sentence = sentence;
            return result;
        }
        /** Viterbi **/
        int k;
        for(k = 2; k < sen.length-1; k++)
            for (int u : chooseSet(k - 1))
                for (int v : chooseSet(k))
                {
                    double max = -Double.MAX_VALUE;
                    int maxW = 0;
                    double candidate;
                    /** Find w that belongs to K_k-2 that maximizes pi[k][u][v] **/
                    for (int w : chooseSet(k - 2))
                    {
                        double emiProb;
                        if(emiPara.get(sen[k]) == null)
                            if(sen[k].matches("[A-Z][a-z]*"))
                                emiProb = emiPara.get("_RARE_IPER_").tags.get(e.get(v)).prob;
                            else if(sen[k].matches("[A-Z][a-z]*[-]*[A-Z][a-z]*"))
                                emiProb = emiPara.get("_RARE_IPER_").tags.get(e.get(v)).prob;
                            else if(sen[k].matches("[A-Z]*"))
                                emiProb = emiPara.get("_RARE_ILOC_").tags.get(e.get(v)).prob;
                            else if(sen[k].contains("&"))
                                emiProb = emiPara.get("_RARE_IORG").tags.get(e.get(v)).prob;
                            else
                                emiProb = emiPara.get("_RARE_").tags.get(e.get(v)).prob;
                        else
                            emiProb = emiPara.get(sen[k]).tags.get(e.get(v)).prob;

                        if ((candidate = pi[k - 1][w][u] * tranProb[w][u][v] * emiProb) > max)
                        {
                            max = candidate;
                            maxW = w;
                        }
                    }
                    pi[k][u][v] = max;
                    bp[k][u][v] = maxW;
                }
        /** Find u, v such that we have max probability trigram [u, v, STOP] **/
        double max = -Double.MIN_VALUE;
        for(int u: chooseSet(k-2))
            for(int v: chooseSet(k-1))
            {
                double candidate = pi[sen.length-2][u][v] * tranProb[u][v][STOP];
                if(candidate > max)
                {
                    max = candidate;
                    maxUTag = u;
                    maxVTag = v;
                }
            }
        viterbiProb.add(max);
        viterbiProb.add(max);
        /** Collecting solutions **/
        int a = maxUTag, b = maxVTag;
        viterbiResult.add(g.get(b));
        viterbiResult.add(g.get(a));
        for(k = sen.length-4; k >= 2; k--)
        {
            viterbiResult.add(g.get(bp[k + 2][a][b]));
            viterbiProb.add(pi[k + 2][a][b]);
            int tmp = b; // <-- This one takes one late day from me! :(
            b = a;
            a = bp[k+2][a][tmp];
        }
        Collections.reverse(viterbiResult);
        Collections.reverse(viterbiProb);
        ViterbiReturn result = new ViterbiReturn();
        result.viterbiResult = viterbiResult;
        result.viterbiProb = viterbiProb;
        result.sentence = sentence;
        return result;
    }

    /**
     * Return the correct set given k.
     * @param k The k value in Viterbi algorithm
     * @return An array of int, representing the set of available tags
     */
    private int[] chooseSet(int k)
    {
        if(k == 0) return K_minus_1;
        else if(k == 1) return K0;
        else return Kk;
    }

    public static void main(String[] args)
    {
        Viterbi2 v = new Viterbi2();

        /** Tests for TA to run on CLIC machines **/
        /** First read in the sentences **/
        ArrayList<String> sens = v.readSentenceFile("./ner_dev.dat");
        /** Create a place to hold results **/
        ArrayList<ViterbiReturn> results = new ArrayList<ViterbiReturn>();
        /** Invoke viterbi for each sentence in sens **/
        for(String sen: sens)
            results.add(v.act(sen));
        /** Output results to file, named Viterbi2Tagged **/
        v.writeResultToFile(results);
    }
}
