
package plagiarismdetector;

import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.morph.WordnetStemmer;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.openxml4j.opc.OPCPackage;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

/**
 *
 * @author TANVIR
 */
public class PlagiarismDetector extends Application {

    private static final int TEXT = 1, PDF = 2, DOCX = 3;
    private static List<HashMap<String, Double>> termFrequency;
    private static Map<String, WordFrequency> bagOfWords;
    private static int numOfDocs;
    private static double[] result;
    private static Map<String, Integer> idf;
    private static HashMap<String, Integer> synIdf;
    private static final int TOTAL_DOCS = 100000;
    private static IRAMDictionary dict;
    private static WordnetStemmer wordnetStemmer;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styleSheet.css").toExternalForm());

        stage.setScene(scene);
        stage.titleProperty().set("Plagiarism Detector");
        stage.getIcons().add(new Image("file:icon.png"));
        stage.setResizable(false);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        initialize();
        launch(args);

    }

    private static void initialize() {
        try {
            Path path = Paths.get("dict");
            URL url = new URL("file", null, path.toString());
            //dict = new edu.mit.jwi.Dictionary(url);
            dict = new RAMDictionary(url, ILoadPolicy.IMMEDIATE_LOAD);

            dict.open();
            dict.load(true);

            wordnetStemmer = new WordnetStemmer(dict);
        } catch (IOException e) {

        } catch (InterruptedException ex) {
            Logger.getLogger(PlagiarismDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
        bagOfWords = new HashMap<>();

    }

    public static double[] Process(List<File> references, File query) {
        if (query == null || references.isEmpty()) {
            return null;
        }
        List<String> words;
        termFrequency = new ArrayList<>(references.size());
        bagOfWords.clear();
        for (int i = 0; i < references.size(); i++) {
            words = getWords(references.get(i));
            termFrequency.add(getWordFrequency(words));
        }
        // Suspected query document stored at last index.
        words = getWords(query);
        numOfDocs = references.size() + 1;
        termFrequency.add(getWordFrequency(words));

        computeTFIDF();
        return cosineSimilarity();

    }

    private static int getFileType(File file) {
        String name = file.getName();
        int index = name.lastIndexOf(".");
        name = name.substring(index + 1);
        //System.out.println(name);
        switch (name.toLowerCase()) {
            case "pdf":
                return PDF;
            case "txt":
                return TEXT;
            case "docx":
                return DOCX;
            default:
                return TEXT;
        }
    }

    private static List<String> getWords(File file) {
        List<String> result = new ArrayList<>();
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        int fileType = getFileType(file);
        if (fileType == TEXT) {
            try {
                Path path = file.toPath();
                String s = new String(Files.readAllBytes(path));
                result = Arrays.asList(tokenizer.tokenize(s));

            } catch (IOException e) {

            }
        } else if (fileType == PDF) {
            try {
                PDDocument pDDocument = PDDocument.load(file);
                PDFTextStripper pDFTextStripper = new PDFTextStripper();
                String s = pDFTextStripper.getText(pDDocument);
                result = Arrays.asList(tokenizer.tokenize(s));

            } catch (IOException e) {

            }
        } else if (fileType == DOCX) {
            try {
                XWPFWordExtractor wPFWordExtractor = new XWPFWordExtractor(OPCPackage.open(Files.newInputStream(file.toPath())));
                String s = wPFWordExtractor.getText();
                result = Arrays.asList(tokenizer.tokenize(s));
//                for (String word : result) {
//                    System.out.println(word);
//                }
            } catch (Exception e) {

            }
        }

        ArrayList<String> result2 = new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (String word : result) {
            //word = word.toLowerCase();

            try {
                list = wordnetStemmer.findStems(word, null);
                //System.out.println(word + " (Stems): " + list);
                if (!list.isEmpty()) {
                    //ret.addAll(list);
                    for (String s : list) {
                        result2.add(s.toLowerCase());
                    }
                }
            } catch (IllegalArgumentException e) {

            }

        }

        return result2;
    }

    private static List<String> doLemmatization(List<String> words) {
        FileInputStream fileInputStream = null;
        FileInputStream fileInputStream2 = null;
        try {
            fileInputStream2 = new FileInputStream(new File("en-pos-maxent.bin"));
            POSModel posModel = new POSModel(fileInputStream2);
            POSTaggerME posTagger = new POSTaggerME(posModel);
            String tags[] = posTagger.tag((String[]) words.toArray());
            fileInputStream = new FileInputStream(new File("en-dictionary.txt"));
            DictionaryLemmatizer dictionaryLemmatizer = new DictionaryLemmatizer(fileInputStream);
            words = Arrays.asList(dictionaryLemmatizer.lemmatize((String[]) words.toArray(), tags));
        } catch (FileNotFoundException ex) {
            System.out.println("File Not Found");
            Logger.getLogger(PlagiarismDetector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("IO");
            Logger.getLogger(PlagiarismDetector.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileInputStream.close();
                fileInputStream2.close();
            } catch (IOException ex) {
                Logger.getLogger(PlagiarismDetector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return words;
    }

    private static HashMap<String, Double> getWordFrequency(List<String> words) {
        HashMap<String, Double> freq = new HashMap<>();
        Double prev = 0.0;
        for (String s : words) {
            prev = freq.get(s);
            if (prev != null) {
                freq.put(s, prev + 1);
            } else {
                freq.put(s, 1.0);
            }

        }
        for (Map.Entry<String, Double> entry : freq.entrySet()) {
            entry.setValue(entry.getValue() / freq.size());
        }
        return freq;
    }

    private static void loadTheBag(List<String> words, int totalDocs) {
        for (String word : words) {
            if (!bagOfWords.containsKey(word)) {
                bagOfWords.put(word, new WordFrequency(word, totalDocs));
            }
        }
    }

    private static void computeTFIDF() {
        double tfIdf = 0.0;
        for (int i = 0; i < termFrequency.size(); i++) {
            for (Map.Entry<String, Double> entry : termFrequency.get(i).entrySet()) {

                tfIdf = entry.getValue() * getIDF(entry.getKey());
                if (!bagOfWords.containsKey(entry.getKey())) {
                    WordFrequency wordFrequency = new WordFrequency(entry.getKey(), numOfDocs);
                    wordFrequency.update(i, tfIdf);
                    bagOfWords.put(entry.getKey(), wordFrequency);
                } else {
                    bagOfWords.get(entry.getKey()).update(i, tfIdf);
                }
            }
        }
        //Query Document

        termFrequency = null;
    }

    private static double getIDF(String s) {
        if (idf == null) {
            idf = IDFPreprocessing.getIDFMap();
        }

        double ret = 0;
        double frequency = 1.0;
        if (idf.containsKey(s)) {
            frequency = idf.get(s);

        } else {
            return 0;
        }
        ret = Math.log(TOTAL_DOCS / frequency);
        return ret;
    }

    private static double[] cosineSimilarity() {
        result = new double[numOfDocs];
        double[] numerator = new double[numOfDocs];
        double[] denumerator1 = new double[numOfDocs];
        //double[] denumerator3 = new double[numOfDocs];
        double denumerator2 = 0;
        for (Map.Entry<String, WordFrequency> entry : bagOfWords.entrySet()) {
            for (int i = 0; i < numOfDocs - 1; i++) {
                numerator[i] += entry.getValue().getFrequency(i) * entry.getValue().getFrequency(numOfDocs - 1);
                denumerator1[i] += entry.getValue().getFrequency(i) * entry.getValue().getFrequency(i);
            }
            denumerator2 += entry.getValue().getFrequency(numOfDocs - 1) * entry.getValue().getFrequency(numOfDocs - 1);
        }
        for (int i = 0; i < numOfDocs - 1; i++) {
            denumerator1[i] = Math.sqrt(denumerator1[i]);
        }
        denumerator2 = Math.sqrt(denumerator2);
        for (int i = 0; i < numOfDocs - 1; i++) {
            result[i] = numerator[i] / (denumerator1[i] * denumerator2);
            //System.out.println("Document: " + (i + 1) + ": " + String.format("%.2f", result[i] * 100) + "%");
        }
        return result;
    }
}
