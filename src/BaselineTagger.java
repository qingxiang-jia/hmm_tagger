import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * This class contains solutions for Question 4 (a-c) and Question 6.
 */
public class BaselineTagger
{
    /**
     * Question 4 c
     * @param trainFn The counts file produced by count_freqs.py.
     * @param keyFn This represents the file "ner_dev.dat".
     */
    public void tag(String trainFn, String keyFn)
    {
        MaxTag mt = new MaxTag();
        HashMap<String, EntryLine> emiPara = this.compEmiPara(trainFn); // trainFn refers to the _RARE_ file
        // read in the file contains only keys
        BufferedReader bfReader;
        ArrayList<String> lines = new ArrayList<String>();
        try
        {
            bfReader = new BufferedReader(new FileReader(keyFn));
            for (String line = bfReader.readLine(); line != null; line = bfReader.readLine())
                if(!line.equals(""))
                {
                    EntryLine curr;
                    if(!emiPara.containsKey("_RARE_")) // if not using rare
                        if(emiPara.containsKey(line)) // have the word
                        {
                            curr = emiPara.get(line);
                            Tag max = mt.getMax(curr.tags);
                            lines.add(line+" "+max.tag+" "+(Math.log(max.prob)/Math.log(2)));
                        } else // not have the word
                            lines.add(line+" O "+(Math.log(0)/Math.log(2)));
                    else // is using rare
                    {
                        if(emiPara.containsKey(line)) // have the word
                            curr = emiPara.get(line);
                        else
                            curr = emiPara.get("_RARE_"); // not have the word
                        Tag max = mt.getMax(curr.tags);
                        lines.add(line+" "+max.tag+" "+(Math.log(max.prob)/Math.log(2)));
                    }
                } else
                    lines.add("");
            bfReader.close();
        } catch (IOException e) { e.printStackTrace(); }
        // write back to a new file
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter("NETagged", "UTF-8");
            for(String line: lines)
                writer.println(line);
            writer.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        System.out.println("tagging success");
    }

    /**
     * Question 4 b
     * Rewrite infrequent words (count < 5) as _RARE_ in the training data (ner_train.dat).
     * @param fn File name of the counts file produced by count_freqs.py.
     */
    public void rareTagger(String fn, int threshold)
    {
        HashMap<String, Integer> count = new HashMap<String, Integer>();
        ArrayList<String> lines = new ArrayList<String>();
        // read in the file
        BufferedReader bfReader;
        try
        {
            bfReader = new BufferedReader(new FileReader(fn));
            for (String line = bfReader.readLine(); line != null; line = bfReader.readLine())
            {
                lines.add(line);
                if(!line.equals(""))
                {
                    String[] entry = line.split("\\s+");
                    if(!count.containsKey(entry[0]))
                        count.put(entry[0], 1);
                    else
                        count.put(entry[0], count.get(entry[0])+1);
                }
            }
            bfReader.close();
        } catch (IOException e) { e.printStackTrace(); }
        // write back to a new file
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter("rare_tagged.dat", "UTF-8");
            for(String line: lines)
                if(line.equals(""))
                    writer.println("");
                else
                {
                    String[] entry = line.split("\\s+");
                    if(count.containsKey(entry[0]) && count.get(entry[0]) < threshold)
                        writer.println("_RARE_ " + entry[1]);
                    else
                        writer.println(line);
                }
            writer.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        System.out.println("\nrareTagger success");
    }

    /**
     * Question 6
     * @param fn The file name of the training data file (ner_train.dat).
     * @param threshold The occurrence of the word under threshold will be considered rare.
     */
    public void pseudoWordTagger(String fn, int threshold)
    {
        HashMap<String, Integer> count = new HashMap<String, Integer>();
        ArrayList<String> lines = new ArrayList<String>();
        // read in the file
        BufferedReader bfReader;
        try
        {
            bfReader = new BufferedReader(new FileReader(fn));
            for (String line = bfReader.readLine(); line != null; line = bfReader.readLine())
            {
                lines.add(line);
                if(!line.equals(""))
                {
                    String[] entry = line.split("\\s+");
                    if(!count.containsKey(entry[0]))
                        count.put(entry[0], 1);
                    else
                        count.put(entry[0], count.get(entry[0])+1);
                }
            }
            bfReader.close();
        } catch (IOException e) { e.printStackTrace(); }
        // write back to a new file
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter("pseudo_word_tagged.dat", "UTF-8");
            for(String line: lines)
                if(line.equals(""))
                    writer.println("");
                else
                {
                    String[] entry = line.split("\\s+");
                    if(count.containsKey(entry[0]) && count.get(entry[0]) < threshold)
                        if(entry[0].matches("[A-Z][a-z]*"))
                            writer.println("_RARE_IPER_ " + entry[1]);
                        else if(entry[0].matches("[A-Z][a-z]*[-]*[A-Z][a-z]*"))
                            writer.println("_RARE_IPER_ " + entry[1]);
                        else if(entry[0].matches("[A-Z]*"))
                            writer.println("_RARE_ILOC_ " + entry[1]);
                        else if(entry[0].contains("&"))
                            writer.println("_RARE_IORG_ " + entry[1]);
                        else
                            writer.println("_RARE_ " + entry[1]);
                    else
                        writer.println(line);
                }
            writer.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        System.out.println("\npseudoWordTagger success");
    }

    /**
     * Question 4 a
     * @param fn File name of the counts file that is produced by count_freqs.py.
     * @return A HashMap in which each entry contains the emission probability for all tags of a given word.
     */
    public HashMap<String, EntryLine> compEmiPara(String fn)
    {
        int totalIPER = 0, totalIORG = 0, totalILOC = 0, totalIMISC = 0, totalBPER = 0, totalBORG = 0, totalBLOC = 0, totalBMISC = 0, totalO = 0;
        HashMap<String, EntryLine> emiPara = new HashMap<String, EntryLine>();
        // read counts line by line
        BufferedReader bfReader = null;
        try
        {
            bfReader = new BufferedReader(new FileReader(fn));
            for (String line = bfReader.readLine(); line != null; line = bfReader.readLine())
            {
                String[] entry = line.split("\\s+");
                if(entry[1].equals("WORDTAG")) // is emission parameter entry
                {
                    EntryLine curr;
                    if(emiPara.containsKey(entry[3]))
                        curr = emiPara.get(entry[3]);
                    else
                    {
                        curr = new EntryLine(entry[3]);
                        emiPara.put(entry[3], curr);
                        curr = emiPara.get(entry[3]);
                    }
                    // update total number of each tag, and update curr
                    if(entry[2].equals("O"))
                    {
                        totalO += Integer.parseInt(entry[0]);
                        curr.tags.get(8).prob += Integer.parseInt(entry[0]);
                    } else if(entry[2].equals("I-PER"))
                    {
                        totalIPER += Integer.parseInt(entry[0]);
                        curr.tags.get(0).prob += Integer.parseInt(entry[0]);
                    } else if(entry[2].equals("I-ORG"))
                    {
                        totalIORG += Integer.parseInt(entry[0]);
                        curr.tags.get(1).prob += Integer.parseInt(entry[0]);
                    } else if(entry[2].equals("I-LOC"))
                    {
                        totalILOC += Integer.parseInt(entry[0]);
                        curr.tags.get(2).prob += Integer.parseInt(entry[0]);
                    } else if(entry[2].equals("I-MISC"))
                    {
                        totalIMISC += Integer.parseInt(entry[0]);
                        curr.tags.get(3).prob += Integer.parseInt(entry[0]);
                    } else if(entry[2].equals("B-PER"))
                    {
                        totalBPER += Integer.parseInt(entry[0]);
                        curr.tags.get(4).prob += Integer.parseInt(entry[0]);
                    } else if(entry[2].equals("B-ORG"))
                    {
                        totalBORG += Integer.parseInt(entry[0]);
                        curr.tags.get(5).prob += Integer.parseInt(entry[0]);
                    } else if(entry[2].equals("B-LOC"))
                    {
                        totalBLOC += Integer.parseInt(entry[0]);
                        curr.tags.get(6).prob += Integer.parseInt(entry[0]);
                    } else
                    {
                        totalBMISC += Integer.parseInt(entry[0]);
                        curr.tags.get(7).prob += Integer.parseInt(entry[0]);
                    }
                } else { break; }
            }
            bfReader.close();
        } catch (IOException e) { e.printStackTrace(); }
        // divide each field of each entry by the total number of corresponding tag
        for (Entry<String, EntryLine> entry : emiPara.entrySet())
        {
            EntryLine curr = entry.getValue();
            curr.tags.get(8).prob = curr.tags.get(8).prob/totalO;
            curr.tags.get(0).prob = curr.tags.get(0).prob/totalIPER;
            curr.tags.get(1).prob = curr.tags.get(1).prob/totalIORG;
            curr.tags.get(2).prob = curr.tags.get(2).prob/totalILOC;
            curr.tags.get(3).prob = curr.tags.get(3).prob/totalIMISC;
            curr.tags.get(4).prob = curr.tags.get(4).prob/totalBPER;
            curr.tags.get(5).prob = curr.tags.get(5).prob/totalBORG;
            curr.tags.get(6).prob = curr.tags.get(6).prob/totalBLOC;
            curr.tags.get(7).prob = curr.tags.get(7).prob/totalBMISC;
        }
        System.out.println("\ncompEmiPara success");
        return emiPara;
    }

    /**
     * This main method includes the tests you can run. You should look at section "Test for TA".
     * Notice you may NOT run all tests at a time since they produce files, and will cause side effect.
     * Detailed instruction in in the inline comments.
     * @param args No arguments!
     */
    public static void main(String[] args)
    {
        BaselineTagger tagger = new BaselineTagger();

        /** Tests for TA on CLIC machines **/

        /**
         * This test is for Question 4 b. It generates a file with certain words being tagged
         * as _RARE_. The generated file will be named rare_tagged.dat. Please use count_freq.py
         * to generate new counts (named as ner.rare.counts).
         * */
//        tagger.rareTagger("./ner_train.dat", 5);

        /**
         * This test is for Question 4 c. It reads in ner.rare.counts and ner_dev.dat, and outputs
         * the file with each line having word, tag, and log probability. The output file is named
         * NETagged.
         */
//        tagger.tag("./ner.rare.counts", "./ner_dev.dat");

        /**
         * This test is for Question 6. It reads in ner_train.data, and produces the file with
         * words whose occurrence is lower than 5 times being tagged into five heuristic pseudo
         * words. The output file is named pseudo_word_tagged.dat. You need then use count_freq.py
         * to generate the counts file (named ner.pseudo_word.counts).
         */
//        tagger.pseudoWordTagger("./ner_train.dat", 5);

        /**
         * The method compEmiPara does not have a test because all other methods will also use it.
         * So, it is tested implicitly when you run other methods in this file (and outside this
         * file). This method is for Question 4 a.
         */
    }
}