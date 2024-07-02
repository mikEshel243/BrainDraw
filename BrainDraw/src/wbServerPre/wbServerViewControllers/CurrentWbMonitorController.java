package wbServerPre.wbServerViewControllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

public class CurrentWbMonitorController {
    public static boolean flag = false;
    public static CurrentWbMonitorController instance = null;
    @FXML private ScrollPane scrollPane;
    private static TextArea loggerArea;

    public static CurrentWbMonitorController getInstance() {
        if (instance == null)
            instance = new CurrentWbMonitorController();

        return instance;
    }

    public void initialize() {

        loggerArea = new TextArea();
        loggerArea.setPrefSize(575, 256);
        loggerArea.setEditable(false);
        this.scrollPane.setContent(loggerArea);
    }
    
    @FXML
    private void onClickBack() {
        try {
			WbServerGUIController.getInstance().showCurrentWbView();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }

    public void updateLogger(String logger) {
        if(flag == true){
            loggerArea.appendText(logger + "\n");
        }
    }
}
