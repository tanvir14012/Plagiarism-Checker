package plagiarismdetector;


public class WordFrequency {
    private double[] tf;
    private String word;
    private int length;
    
    public WordFrequency(String term, int size) {
        tf = new double[size];
        word = term;
        length = size;
    }
    public String getWord() {
        return word;
    }
    public double getFrequency(int index) {
        if(index < 0 || index >= tf.length) {
            return 0;
        }
        return tf[index];
    }
    public double[] getFreqList() {
        return tf;
    }
    public void update(int index, double value) {
        if(index >= 0  && index < tf.length) {
            tf[index] = value;
        }
    }
    
}
