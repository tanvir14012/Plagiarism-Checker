package plagiarismdetector;

import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.tokenize.SimpleTokenizer;

public class IDFPreprocessing {

    private static IRAMDictionary dict;
    private static WordnetStemmer wordnetStemmer;
    private static HashMap<String, Integer> idf;
    private static HashMap<String, Integer> synIdf;

    public static void main(String[] args) throws Exception {

        try {
            idf = new HashMap<>();
            loadDictionary();
            FileReader fileReader = new FileReader(new File("E:\\WikiPedia_txt\\en_txt\\en.txt")); // Wikipedia raw text dump
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String str = "", content = "";
            int count = 0, total_docs = 0;
            boolean flag = false;
            while ((str = bufferedReader.readLine()) != null && total_docs <= 100000) {
                //System.out.println(str);

                if (str.isEmpty()) {
                    flag = true;
                    count++;
                } else {
                    if (count >= 3) {
                        if (content.length() > 300 * 6) {
                            computeIDF(content);
                            System.out.println("Cur Doc: " + total_docs);
                            total_docs++;
                        }
                        content = "";
                    }
                    flag = false;
                    count = 0;
                }

                content += " " + str;
            }
            //System.out.println("Total Doc: " + total_docs);
            serializeIDF();
            storeToFile();
            System.out.println("The process has completed successfully");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IDFPreprocessing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException io) {
        } catch (InterruptedException ex) {
            Logger.getLogger(IDFPreprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void loadDictionary() throws MalformedURLException, InterruptedException, IOException {
        Path path = Paths.get("dict");
        URL url = new URL("file", null, path.toString());
        dict = new RAMDictionary(url, ILoadPolicy.IMMEDIATE_LOAD);
        dict.open();
        dict.load(true);
        wordnetStemmer = new WordnetStemmer(dict);
    }

    private static void computeIDF(String text) {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer.tokenize(text);
        HashSet<String> set = new HashSet<>();
        List<String> stems;
        for (String s : tokens) {
            try {
                stems = wordnetStemmer.findStems(s, null);
                set.addAll(stems);
            } catch (IllegalArgumentException e) {
                if (s.matches("[a-zA-Z0-9]+")) {
                    set.add(s);
                }
            }
        }
        for (String s : set) {
            s = s.toLowerCase();
            if (idf.containsKey(s)) {
                idf.put(s, idf.get(s) + 1);
            } else {
                idf.put(s, 1);
            }
        }
    }

    private static void serializeIDF() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("idf.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(idf);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException ex) {

        } catch (IOException ex) {

        }
    }

    private static void deSerializeIDF() {
        try {
            FileInputStream fileInputStream = new FileInputStream("idf.ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            idf = (HashMap<String, Integer>) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
//            for (Map.Entry entry : idf.entrySet()) {
//                System.out.println(entry.getKey() + ": " + entry.getValue());
//            }
        } catch (FileNotFoundException ex) {

        } catch (IOException ex) {

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IDFPreprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void storeToFile() {
        try {
            FileWriter fileWriter = new FileWriter("idf.txt");
            for (Map.Entry entry : idf.entrySet()) {
                fileWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(IDFPreprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void calcSynonymIDF() throws InterruptedException, IOException {
        deSerializeIDF();
        loadDictionary();
        synIdf = new HashMap<>();
        for (Map.Entry entry : idf.entrySet()) {
            for (POS pos : POS.values()) {
                IIndexWord iIndexWord = dict.getIndexWord((String) entry.getKey(), pos);
                if (iIndexWord == null) {
                    continue;
                }
                if (iIndexWord.getWordIDs().size() > 0) {
                    for (IWordID iWordID : iIndexWord.getWordIDs()) {
                        IWord iWord = dict.getWord(iWordID);
                        ISynset iSynset = iWord.getSynset();
                        for (IWord iWord1 : iSynset.getWords()) {
                            String lemma = iWord1.getLemma();
                            if (lemma.contains("_") && !synIdf.containsKey(lemma)) {
                                synIdf.put(lemma, (Integer) entry.getValue());
                            }
                        }
                    }
                }
            }
        }
//        for(Map.Entry entry: synIdf.entrySet()) {
//            System.out.println(entry.getKey() + " :" + entry.getValue());
//        }
    }

    private static void serializeSynIdf() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("Synidf.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(synIdf);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException ex) {

        } catch (IOException ex) {

        }
    }

    private static void deSerializeSynIdf() {
        try {
            FileInputStream fileInputStream = new FileInputStream("Synidf.ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            synIdf = (HashMap<String, Integer>) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
//            for (Map.Entry entry : idf.entrySet()) {
//                System.out.println(entry.getKey() + ": " + entry.getValue());
//            }
        } catch (FileNotFoundException ex) {

        } catch (IOException ex) {

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IDFPreprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static HashMap<String, Integer> getIDFMap() {
        if (idf == null) {
            deSerializeIDF();
        }
        return idf;
    }

    public static HashMap<String, Integer> getSynIDFMap() {
        if (synIdf == null) {
            deSerializeSynIdf();
        }
        return synIdf;
    }
}
// Total Doc: 1484640.
