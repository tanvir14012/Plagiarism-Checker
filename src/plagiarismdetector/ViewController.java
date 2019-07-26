package plagiarismdetector;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author TANVIR
 */
public class ViewController implements Initializable {

    private FileChooser fileChooser;
    private List<String> extensions;
    private List<File> references;
    private File queryFile;
    @FXML
    private TabPane master;
    @FXML
    private Tab tab1;
    @FXML
    private Button add_Ref;
    @FXML
    private Tab tab2;
    @FXML
    private Tab tab3;
    @FXML
    private ListView<String> refLists;
    @FXML
    private ListView<String> queryList;
    @FXML
    private TextArea pasteBin;
    @FXML
    private TableView<Table> resultTable;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fileChooser = new FileChooser();
        extensions = new ArrayList<>();
        extensions.add("*.pdf");
        extensions.add("*.txt");
        extensions.add("*.doc");
        extensions.add("*.docx");
        references = new ArrayList<>();
        //references.add(new File("stopwords.txt"));
        TableColumn ref = new TableColumn("References");
        ref.setCellValueFactory(new PropertyValueFactory<>("refText"));

        TableColumn query = new TableColumn("Query");
        query.setCellValueFactory(new PropertyValueFactory<>("queryText"));
        TableColumn res = new TableColumn("Plagiarism Measure");
        res.setCellValueFactory(new PropertyValueFactory<>("resultPercentage"));

        resultTable.getColumns().addAll(ref, query, res);
        resultTable.autosize();
    }

    @FXML
    private void refAdd(ActionEvent event) {
        if (fileChooser != null) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text-PDF-Word Files", extensions));
            List<File> refs = fileChooser.showOpenMultipleDialog(null);
            if (refs != null) {
                references.addAll(refs);
                for (int i = 0; i < refs.size(); i++) {
                    refLists.getItems().add(refLists.getItems().size(), refs.get(i).getName());
                }
                refLists.getSelectionModel().select(refs.size() - 1);
            }
        }
    }

    @FXML
    private void refDel(ActionEvent event) {
        if (refLists.getItems().size() > 0) {
            int index = refLists.getSelectionModel().getSelectedIndex();
            refLists.getItems().remove(index);
            references.remove(index);
        }
    }

    @FXML
    private void queryAdd(ActionEvent event) {
        if (queryFile == null) {
            fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text-PDF-Word Files", extensions));
            queryFile = fileChooser.showOpenDialog(null);
            if (queryFile != null) {
                queryList.getItems().add(queryFile.getName());
                pasteBin.setDisable(true);
            }
        }
    }

    @FXML
    private void queryDelete(ActionEvent event) {
        if (queryFile != null) {
            queryList.getItems().remove(0);
            queryFile = null;
            pasteBin.setDisable(false);
        }
    }

    @FXML
    private void checkPlagiarism(ActionEvent event) {
        
        if (queryFile == null) {
            try {
                queryFile = new File("paste.txt");
                if (!queryFile.exists()) {
                    queryFile.createNewFile();
                }
                PrintWriter printWriter = new PrintWriter(queryFile);
                printWriter.println(pasteBin.getText());
                printWriter.close();
            } catch (IOException e) {

            }
        }
        resultTable.getItems().clear();
        double[] result = PlagiarismDetector.Process(references, queryFile);
        for (int i = 0; i < references.size(); i++) {
            resultTable.getItems().add(new Table(references.get(i).getName(), queryFile.getName(),
                    String.format("%.2f %%", result[i]*100)));
        }
        master.getSelectionModel().selectLast(); 
        if(queryFile != null && queryFile.getName().equals("paste.txt")) {
            queryFile = null;
        }
    }

}
