package clientPre.clientViewControllers.whiteBoardController;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import clientApp.ClientAppFacade;
import clientPre.clientViewControllers.ClientGUIController;
import clientPre.pop_ups.AlertBox;
import clientPre.pop_ups.InputText;
import clientPre.pop_ups.OpenFrom;
import clientPre.pop_ups.SaveAs;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.ImageCursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class whiteBoardController {
    private final static Logger logger = Logger.getLogger(whiteBoardController.class);
    
    
    private static whiteBoardController instance;
    private final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};

    public static whiteBoardController getInstance() {
        if (instance == null) {
            instance = new whiteBoardController();
        }
        return instance;
    }

    private static Canvas canvas = new Canvas(903, 482);
    private static Canvas previewCanvas =  new Canvas(903, 482);
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Slider slider;
    @FXML
    private Label label;
    @FXML
    private ListView listView;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Pane pane;
    @FXML
    private TextField msgField;
    @FXML
    private ScrollPane msgPane;
    @FXML
    private Button undo;
    @FXML
    private Button redo;
    @FXML
    private Button clear;

    private static  List<Double> penPointsX = new ArrayList<>();
    private static  List<Double> penPointsY = new ArrayList<>();
    private static  ArrayList<List<Double>> penPointsXHistory = new ArrayList<>();
    private static  ArrayList<List<Double>> penPointsYHistory = new ArrayList<>();
    private static ObservableList<String> actionsHistory = FXCollections.observableArrayList();
    private static ObservableList<String> undoActionsHistory = FXCollections.observableArrayList();

    static private TextArea msgArea = new TextArea();
    private ImageCursor cursor;


    private String clientType = "manager";

    private String mode = "draw";

    private String saveFilePath = "";

    private double[] beginCoordinate = {0, 0};

    private double[] drawCoordinate = {0, 0};

    private double[] lineCoordinate = {0, 0};

    private void sendMsgAndRecordIt(String msg) {
        ClientAppFacade.getInstance().updateWb(msg, "");
    }
    



	private void resizeCanvas() {
    canvas.setWidth(pane.getWidth());
    previewCanvas.setWidth(pane.getWidth());
    canvas.setHeight(pane.getHeight());
    previewCanvas.setHeight(pane.getHeight());
    
}
    

    private void initLeftButtons() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        GraphicsContext previewGC = previewCanvas.getGraphicsContext2D();
        pane.setStyle("-fx-background-color: white");
        label.setText("1.0");
        undo.setDisable(true);
        redo.setDisable(true);

        colorPicker.setValue(Color.BLACK);
        colorPicker.setOnAction(e -> {
            gc.setStroke(colorPicker.getValue());
        });

        slider.setMin(1);
        slider.setMax(20);
        slider.valueProperty().addListener(e -> {
            double value = slider.getValue();
            String str = String.format("%.1f", value);
            label.setText(str);
            gc.setLineWidth(value);
        });
    }

    @FXML
    private void onClickPencil() {
        mode = "draw";
//        this.cursor = new ImageCursor(new Image(getClass().getResource("../../../assets/imgs/pencil.png").toExternalForm()));
//        canvas.setCursor(cursor);
    }

    @FXML
    private void onClickEraser() {
        mode = "erase";
//        this.cursor = new ImageCursor(new Image(getClass().getResource("../../../assets/imgs/eraser.png").toExternalForm()));
//        canvas.setCursor(cursor);
    }

    @FXML
    private void onClickRectan() {
        mode = "rectangle";
//        this.cursor = new ImageCursor(new Image(getClass().getResource("../../../assets/imgs/rectangle.png").toExternalForm()));
//        canvas.setCursor(cursor);
    }

    @FXML
    private void onClickLine() {
        mode = "line";
//        this.cursor = new ImageCursor(new Image(getClass().getResource("../../../assets/imgs/line.png").toExternalForm()));
//        canvas.setCursor(cursor);
    }

    @FXML
    private void onClickCircle() {
        mode = "circle";
//        this.cursor = new ImageCursor(new Image(getClass().getResource("../../../assets/imgs/circle.png").toExternalForm()));
//        canvas.setCursor(cursor);
    }

    @FXML
    private void onClickOval() {
        mode = "oval";
//        this.cursor = new ImageCursor(new Image(getClass().getResource("../../../assets/imgs/oval.png").toExternalForm()));
//        canvas.setCursor(cursor);
    }

    @FXML
    private void onClickText() {
        mode = "text";
//        this.cursor = new ImageCursor(new Image(getClass().getResource("../../../assets/imgs/text.png").toExternalForm()));
//        canvas.setCursor(cursor);
    }
    
    @FXML
    private void onClickFill() {
        mode = "fill";
//        this.cursor = new ImageCursor(new Image(getClass().getResource("../../../assets/imgs/text.png").toExternalForm()));
//        canvas.setCursor(cursor);
    }
    
    @FXML
    private void onClickRedo() {
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        double h = canvas.getHeight();
//        double w = canvas.getWidth();
//        gc.clearRect(0, 0, w, h);
        String msg = "redo";
        try {
			updateWhiteBoard(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    private void onClickClear() {
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        double h = canvas.getHeight();
//        double w = canvas.getWidth();
//        gc.clearRect(0, 0, w, h);
        sendMsgAndRecordIt("clearAll");
    }
    
    @FXML
    private void onClickUndo() {
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        double h = canvas.getHeight();
//        double w = canvas.getWidth();
//        gc.clearRect(0, 0, w, h);
        String msg = "undo";
        try {
			updateWhiteBoard(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }



    private void initDrawMethods() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        GraphicsContext previewGC = previewCanvas.getGraphicsContext2D();
        
        canvas.setOnMousePressed(e -> {
            double x = e.getX();
            double y = e.getY();
            String msg = "";
            if (mode.equals("draw")) {
				penPointsX.add(x);
				penPointsY.add(y);
            } else if (mode.equals("line")) {
                lineCoordinate[0] = x;
                lineCoordinate[1] = y;
            } else if (mode.equals("circle") || mode.equals("rectangle") || mode.equals("oval")) {
                beginCoordinate[0] = x;
                beginCoordinate[1] = y;
            } else if (mode.equals("text")) {
                InputText inputText = new InputText();
                String content = inputText.display();
                if (content != null) {
                    gc.fillText(content, x, y);
                    msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + x + "," + y + "," + content;
                    sendMsgAndRecordIt(msg);
                 
                }
            }
            else if (mode.equals("fill")) {
            	for (String msgShape : actionsHistory ) {
            		ArrayList<String> inst = new ArrayList<>(Arrays.asList(msgShape.split(",")));
            		if(inst.get(2).equals("rectangle") || inst.get(2).equals("circle") || inst.get(2).equals("oval") ) {
            			if(isPointInsideRectangle(x, y, Double.parseDouble(inst.get(3)), Double.parseDouble(inst.get(4)),
            					Double.parseDouble(inst.get(3)) + Double.parseDouble(inst.get(5)), Double.parseDouble(inst.get(4)) + Double.parseDouble(inst.get(6)))) {
                            msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + inst.get(2) + "," + Double.parseDouble(inst.get(3))
                                    + "," + Double.parseDouble(inst.get(4)) + "," + Double.parseDouble(inst.get(5)) + "," + Double.parseDouble(inst.get(6));
                            sendMsgAndRecordIt(msg);
                            
            			}
            		}
//            		}else if(inst.get(2).equals("circle")) {
//            			if (isPointInsideCircle(x, y, Double.parseDouble(inst.get(3)), Double.parseDouble(inst.get(4)),
//            					Double.parseDouble(inst.get(3)) + Double.parseDouble(inst.get(5)), Double.parseDouble(inst.get(4)) + Double.parseDouble(inst.get(6)))) {
//                            msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + inst.get(2) + "," + Double.parseDouble(inst.get(3))
//                            + "," + Double.parseDouble(inst.get(4)) + "," + Double.parseDouble(inst.get(5)) + "," + Double.parseDouble(inst.get(6));
//                            sendMsgAndRecordIt(msg);
//                            
//            			}
//            		}
            		
            	}
            }
            if(!(msg.isEmpty()))
            	actionsHistory.add(msg);
        });
        
//
//        canvas.setOnMousePressed(e -> {
//            double x = e.getX();
//            double y = e.getY();
//            String msg = "";
//            if (mode.equals("draw")) {
//                drawCoordinate[0] = x;
//                drawCoordinate[1] = y;
//            } else if (mode.equals("line")) {
//                lineCoordinate[0] = x;
//                lineCoordinate[1] = y;
//            } else if (mode.equals("circle") || mode.equals("rectangle") || mode.equals("oval")) {
//                beginCoordinate[0] = x;
//                beginCoordinate[1] = y;
//            } else if (mode.equals("text")) {
//                InputText inputText = new InputText();
//                String content = inputText.display();
//                if (content != null) {
//                    gc.fillText(content, x, y);
//                    msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + x + "," + y + "," + content;
//                    sendMsgAndRecordIt(msg);
//                }
//            }
//        });

        canvas.setOnMouseReleased(e -> {
            double x = e.getX();
            double y = e.getY();
            double originX = beginCoordinate[0];
            double originY = beginCoordinate[1];
            double width = Math.abs(x - originX);
            double height = Math.abs(y - originY);
            double upLeftX = (originX - x > 0) ? x : originX;
            double upLeftY = (originY - y > 0) ? y : originY;
            double distance = Math.sqrt(Math.pow(x - originX, 2) + Math.pow(y - originY, 2));
            double middleX = (originX + x) / 2;
            double middleY = (originY + y) / 2;
            String msg = "";
            if (mode.equals("line")) {
//                gc.lineTo(e.getX(), e.getY());
//                gc.stroke();
				//gc.strokeLine(((Line) newLine).getStartX(), ((Line) newLine).getStartY(), ((Line) newLine).getEndX(), ((Line) newLine).getEndY());
                msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + lineCoordinate[0] + ","
                        + lineCoordinate[1] + "," + x + "," + y;
                sendMsgAndRecordIt(msg);
            } else if (mode.equals("rectangle")) {
                gc.strokeRect(upLeftX, upLeftY, width, height);
                msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + upLeftX
                        + "," + upLeftY + "," + width + "," + height;
                sendMsgAndRecordIt(msg);
            } else if (mode.equals("circle")) {
                gc.strokeOval(middleX - distance / 2, middleY - distance / 2, distance, distance);
                msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + (middleX - distance / 2)
                        + "," + (middleY - distance / 2) + "," + distance + "," + distance;
                sendMsgAndRecordIt(msg);
            } else if (mode.equals("oval")) {
                gc.strokeOval(upLeftX, upLeftY, width, height);
                msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + upLeftX
                        + "," + upLeftY + "," + width + "," + height;
                sendMsgAndRecordIt(msg);
            }else if (mode.equals("draw")) {
//            	List<Double> penPointsXCopy = new ArrayList<>();
//            	List<Double> penPointsYCopy = new ArrayList<>();
//            	for(Double val: penPointsX){
//            		penPointsXCopy.add(val);
//            		}
//            	for(Double val: penPointsY){
//            		penPointsYCopy.add(val);
//            		}
            	penPointsXHistory.add(new ArrayList<>(penPointsX));
            	penPointsYHistory.add(new ArrayList<>(penPointsY));
//            	msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + ","  + ((penPointsXHistory.size())-1);
            	msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + ","  + penPointsXHistory + "," + penPointsYHistory;
            	sendMsgAndRecordIt(msg);
            }
            if (!(msg.isEmpty()))
            	actionsHistory.add(msg);
        });

        canvas.setOnMouseDragged(e -> {
            double x = e.getX();
            double y = e.getY();
            double originX = beginCoordinate[0];
            double originY = beginCoordinate[1];
            double width = Math.abs(x - originX);
            double height = Math.abs(y - originY);
            double upLeftX = (originX - x > 0) ? x : originX;
            double upLeftY = (originY - y > 0) ? y : originY;
            double distance = Math.sqrt(Math.pow(x - originX, 2) + Math.pow(y - originY, 2));
            double middleX = (originX + x) / 2;
            double middleY = (originY + y) / 2;
            double h = previewCanvas.getHeight();
            double w = previewCanvas.getWidth();
            previewGC.clearRect(0, 0, w, h);
        	previewGC.setGlobalAlpha(0.5);
        	previewGC.setStroke(gc.getStroke());
        	previewGC.setLineWidth(gc.getLineWidth());
            String msg = "";
            if (mode.equals("draw")) {
            	penPointsX.add(x);
            	penPointsY.add(y);
//            	previewGC.setGlobalAlpha(0.5);
//            	previewGC.setStroke(gc.getStroke());
//            	previewGC.setLineWidth(gc.getLineWidth());
            	previewGC.beginPath();
                double[] pointsX = new double[penPointsX.size()];
                double[] pointsY = new double[penPointsY.size()];
                for (int i = 0; i < penPointsX.size(); i++) {
                    pointsX[i] = penPointsX.get(i);
                    pointsY[i] = penPointsY.get(i);
                }
                previewGC.strokePolyline(pointsX, pointsY, pointsX.length);
                previewGC.closePath();
//                double x1 = drawCoordinate[0];
//                double y1 = drawCoordinate[1];
//                msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + x1
//                        + "," + y1 + "," + x + "," + y + ",";
//                drawCoordinate[0] = x;
//                drawCoordinate[1] = y;
//              msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + penPointsX
//              + "," + penPointsY + "," + x + "," + y + ",";
//                sendMsgAndRecordIt(msg);

            } else if (mode.equals("erase")) {
                gc.clearRect(x, y, slider.getValue(), slider.getValue());
                msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + x + "," + y + "," + slider.getValue();
                sendMsgAndRecordIt(msg);
            } else if (mode.equals("line")) {
//              gc.lineTo(e.getX(), e.getY());
//              gc.stroke();
				//gc.strokeLine(((Line) newLine).getStartX(), ((Line) newLine).getStartY(), ((Line) newLine).getEndX(), ((Line) newLine).getEndY());
//              msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + lineCoordinate[0] + ","
//                      + lineCoordinate[1] + "," + x + "," + y;
//              sendMsgAndRecordIt(msg);
              previewGC.strokeLine(lineCoordinate[0], lineCoordinate[1], x, y);
          } else if (mode.equals("rectangle")) {
//              gc.strokeRect(upLeftX, upLeftY, width, height);
//              msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + upLeftX
//                      + "," + upLeftY + "," + width + "," + height;
//              sendMsgAndRecordIt(msg);
//              double width = Double.parseDouble(inst.get(5));
//              double height = Double.parseDouble(inst.get(6));

        	  previewGC.strokeRect(upLeftX, upLeftY, width, height);
          } else if (mode.equals("circle")) {
        	  previewGC.strokeOval(middleX - distance / 2, middleY - distance / 2, distance, distance);
//              msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + (middleX - distance / 2)
//                      + "," + (middleY - distance / 2) + "," + distance + "," + distance;
//              sendMsgAndRecordIt(msg);
          } else if (mode.equals("oval")) {
        	  previewGC.strokeOval(upLeftX, upLeftY, width, height);
//              msg = gc.getStroke() + "," + gc.getLineWidth() + "," + mode + "," + upLeftX
//                      + "," + upLeftY + "," + width + "," + height;
//              sendMsgAndRecordIt(msg);
          }
        });
    }

    private boolean isPointInsideRectangle(double x, double y, double startX, double startY, double endX, double endY) {
        return x >= Math.min(startX, endX) && x <= Math.max(startX, endX) &&
               y >= Math.min(startY, endY) && y <= Math.max(startY, endY);
    }
    
    private boolean isPointInsideCircle(double x, double y, double centerX, double centerY, double endX, double endY) {
        double radius = Math.sqrt(Math.pow(centerX - endX, 2) + Math.pow(centerY - endY, 2));
        double distance = Math.sqrt(Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2));
        return distance <= radius;
    }

	ObservableList<String> list = FXCollections.observableArrayList(
            "Manager", "coworker1", "coworker2", "coworker3");

    private void initListView(ObservableList<String> list) {
        listView.setItems(list);
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new StringAndButtonList(clientType);
            }
        });
    }

    public void initialize() {

        
        msgArea.setPrefSize(1199, 272);
        msgArea.setWrapText(true);
        msgPane.setContent(msgArea);
        msgPane.setFitToWidth(true);
        msgArea.setEditable(false);
        pane.getChildren().add(msgArea);
        pane.getChildren().addAll(previewCanvas,canvas);
        
        // Add listeners to resize the canvas when the parent Pane is resized
        pane.widthProperty().addListener((obs, oldVal, newVal) -> resizeCanvas());
        pane.heightProperty().addListener((obs, oldVal, newVal) -> resizeCanvas());
        
        initLeftButtons();
        actionsHistory.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (actionsHistory.isEmpty()) {
                    undo.setDisable(true);
                } else {
                    undo.setDisable(false);
                }
            }
        });
        
        undoActionsHistory.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (undoActionsHistory.isEmpty()) {
                    redo.setDisable(true);
                } else {
                    redo.setDisable(false);
                }
            }
        });
        
        boolean isManager = ClientAppFacade.getInstance().isManager();
        if (isManager) {
            clientType = "manager";
        } else {
            clientType = "client";
        }
        if (!clientType.equals("manager")) {
            menuBar.setVisible(false);
            clear.setVisible(false);
        }
//        initSendMessage();
        StringAndButtonList.list = list;
        initListView(StringAndButtonList.list);
        StringAndButtonList.list.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                initListView(StringAndButtonList.list);
            }
        });
        initDrawMethods();
    }

    private boolean isFileNameValid(String fileName) {

        if (fileName == null || fileName.length() > 255) {
            return false;
        } else {

            for (int i = 0; i < ILLEGAL_CHARACTERS.length; i++) {
                if (fileName.contains(Character.toString(ILLEGAL_CHARACTERS[i]))) {
                    return false;
                }
            }
        }
        return true;
    }

    public void saveAs() {
        SaveAs saveAsBox = new SaveAs();
        List<String> saveInfo = saveAsBox.display();

        if (saveInfo.size() == 3) {
            String fileLocation = saveInfo.get(0);
            String fileName = saveInfo.get(1);
            String fileType = saveInfo.get(2);

            if (this.isFileNameValid(fileName)) {
                SnapshotParameters sp = new SnapshotParameters();
                sp.setFill(Color.TRANSPARENT);
                WritableImage image = canvas.snapshot(sp, null);
                String filePath = fileLocation + "/" + fileName + "." + fileType;
                File file = new File(filePath);

                if (fileType.equals("gif") || fileType.equals("png")) {
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                        AlertBox box = new AlertBox();
                        box.display("information", "You have successfully saved the picture");
                    } catch (IOException ex) {
                        AlertBox box = new AlertBox();
                        box.display("information", "Saving failed!");
                    }
                }

                saveFilePath = filePath;
            } else {
                ClientGUIController.getInstance().showErrorView("fileName", "Invalid filename", "");
            }
        }
    }

    public void newCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double h = canvas.getHeight();
        double w = canvas.getWidth();
        gc.clearRect(0, 0, w, h);
        saveFilePath = "";
        String msg = "newWB";
        sendMsgAndRecordIt(msg);
        actionsHistory.clear();
        undoActionsHistory.clear();
        penPointsXHistory.clear();
        penPointsYHistory.clear();
        penPointsX.clear();
        penPointsY.clear();
    }

    public void save() {
        if (!saveFilePath.equals("")) {
            SnapshotParameters sp = new SnapshotParameters();
            sp.setFill(Color.TRANSPARENT);
            WritableImage image = canvas.snapshot(sp, null);
            File file = new File(saveFilePath);
            String[] array = saveFilePath.split("[.]");
            String fileType = array[1];
            if (!fileType.equals("wb")) {
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), fileType, file);
                    AlertBox box = new AlertBox();
                    box.display("information", "You have successfully saved the picture");
                } catch (IOException ex) {
                    AlertBox box = new AlertBox();
                    box.display("information", "Saving failed!");
                }
            }


        } else {
            saveAs();
        }
    }

    public void open() throws Exception {
        OpenFrom openFrom = new OpenFrom();
        String filePath = openFrom.display();
        if (!filePath.isEmpty()) {
            File f = new File(filePath);
            String encodstring = encodeFileToBase64Binary(f);
            ClientAppFacade.getInstance().updateWb("open" + "," + encodstring, "");
            saveFilePath = filePath;
        }
    }

    private static String encodeFileToBase64Binary(File file) throws Exception {
        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStreamReader.read(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public void close() {
        ClientAppFacade.getInstance().closeWb();
    }

    public void updateUserList(String msg) {
        StringAndButtonList.list.setAll(Arrays.asList(msg.split(",")));
    }

    public void updateWhiteBoard(String msg) throws IOException {
    	GraphicsContext gc = canvas.getGraphicsContext2D();
        GraphicsContext previewGC = previewCanvas.getGraphicsContext2D();
        previewGC.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
//        if (!(penPointsXHistory.contains(penPointsX))) {
//            penPointsXHistory.add(penPointsX);
//            penPointsYHistory.add(penPointsY);
//        }
        Paint originalColor = gc.getStroke();
        double originLineWidth = gc.getLineWidth();
//        ArrayList<String> inst = new ArrayList<>(Arrays.asList(msg.split(",")));
        ArrayList<String> inst = new ArrayList<>(splitIgnoringDoubleBrackets(msg));

        if (inst.get(0).equals("newWB")) {
            double h = canvas.getHeight();
            double w = canvas.getWidth();
            gc.clearRect(0, 0, w, h);
            saveFilePath = "";
        } else if (inst.get(0).equals("open")) {
            String imageString = inst.get(1);
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] imageByte = decoder.decodeBuffer(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            BufferedImage bufferedImage = ImageIO.read(bis);
            double h = canvas.getHeight();
            double w = canvas.getWidth();
            gc.clearRect(0, 0, w, h);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            gc.drawImage(image, 0, 0, w, h);
        } else if (inst.get(0).equals("undo")) {
        	undoActionsHistory.add(actionsHistory.remove(actionsHistory.size()-1));
//        	redo.setDisable(false);
//        	if(actionsHistory.isEmpty())
//        		undo.setDisable(true);
//            double h = canvas.getHeight();
//            double w = canvas.getWidth();
//            gc.clearRect(0, 0, w, h);
            sendMsgAndRecordIt("clear");
            sendMsgAndRecordIt("refresh");
//        	for (int i = 0; i < actionsHistory.size(); i++) {
//        		sendMsgAndRecordIt(actionsHistory.get(i));
////				updateWhiteBoard(actionsHistory.get(i));
//				
//			}
        } else if (inst.get(0).equals("redo")) {
        	actionsHistory.add(undoActionsHistory.remove(undoActionsHistory.size()-1));
//        	undo.setDisable(false);
//        	if(undoActionsHistory.isEmpty())
//        		redo.setDisable(true);
//            double h = canvas.getHeight();
//            double w = canvas.getWidth();
//            gc.clearRect(0, 0, w, h);
        	sendMsgAndRecordIt("clear");
        	sendMsgAndRecordIt("refresh");
//        	for (int i = 0; i < actionsHistory.size(); i++) {
//        		sendMsgAndRecordIt(actionsHistory.get(i));
//				
//			}
        } else if (inst.get(0).equals("clear")) {
            double h = canvas.getHeight();
            double w = canvas.getWidth();
            gc.clearRect(0, 0, w, h);
        } else if (inst.get(0).equals("clearAll")) {
            double h = canvas.getHeight();
            double w = canvas.getWidth();
            gc.clearRect(0, 0, w, h);
            actionsHistory.clear();
            undoActionsHistory.clear();
            //disableUndoRedoButtons();
        } else if (inst.get(0).equals("refresh")) {
        	for (int i = 0; i < actionsHistory.size(); i++) {
        		sendMsgAndRecordIt(actionsHistory.get(i));
//				updateWhiteBoard(actionsHistory.get(i));
				
			}
        } else {
            Color c = Color.web(inst.get(0), 1.0);
            gc.setStroke(c);
            gc.setLineWidth(Double.parseDouble(inst.get(1)));
        	double x = 0;
        	double y = 0;
            if(!(inst.get(2).equals("draw") || inst.get(2).equals("fill"))) {
            	x = Double.parseDouble(inst.get(3));
            	y = Double.parseDouble(inst.get(4));
            }
            if (inst.get(2).equals("oval")) {
                double width = Double.parseDouble(inst.get(5));
                double height = Double.parseDouble(inst.get(6));

                gc.strokeOval(x, y, width, height);
            } else if (inst.get(2).equals("rectangle")) {
                double width = Double.parseDouble(inst.get(5));
                double height = Double.parseDouble(inst.get(6));

                gc.strokeRect(x, y, width, height);
            } else if (inst.get(2).equals("circle")) {
                double width = Double.parseDouble(inst.get(5));
                double height = Double.parseDouble(inst.get(6));

                gc.strokeOval(x, y, width, height);
            } else if (inst.get(2).equals("draw")) {
            
//            	penPointsX = penPointsXHistory.get(Integer.parseInt(inst.get(3)));
            	penPointsX = convertStringToDoubleList(inst.get(3));
            	penPointsY = convertStringToDoubleList(inst.get(4));
            	
            	gc.beginPath();
                double[] pointsX = new double[penPointsX.size()];
                double[] pointsY = new double[penPointsY.size()];
                for (int i = 0; i < penPointsX.size(); i++) {
                    pointsX[i] = penPointsX.get(i);
                    pointsY[i] = penPointsY.get(i);
                }
                gc.strokePolyline(pointsX, pointsY, pointsX.length);
                gc.closePath();

//				penPointsX = new ArrayList<>();
//				penPointsY = new ArrayList<>();
                penPointsX.clear();
                penPointsY.clear();
				penPointsXHistory.clear();
				penPointsYHistory.clear();
//                double x2 = Double.parseDouble(inst.get(5));
//                double y2 = Double.parseDouble(inst.get(6));
//                gc.strokeLine(x, y, x2, y2);
            } else if (inst.get(2).equals("text")) {
                String content = inst.get(5);
                gc.strokeText(content, x, y);
            } else if (inst.get(2).equals("line")) {
                double x2 = Double.parseDouble(inst.get(5));
                double y2 = Double.parseDouble(inst.get(6));
                gc.strokeLine(x, y, x2, y2);
            } else if (inst.get(2).equals("erase")) {
                gc.clearRect(x, y, Double.parseDouble(inst.get(5)), Double.parseDouble(inst.get(5)));
            } else if(inst.get(2).equals("fill")) {
            	x = Double.parseDouble(inst.get(4));
            	y = Double.parseDouble(inst.get(5));
                double width = Double.parseDouble(inst.get(6));
                double height = Double.parseDouble(inst.get(7));
                gc.setFill(c);
            	if(inst.get(3).equals("rectangle")) {
            		gc.fillRect(x, y, width, height);
            	}else if(inst.get(3).equals("circle") || inst.get(3).equals("oval")) {
            		 gc.fillOval(x, y, width, height);
            	}
            	
            }
        }


        gc.setStroke(originalColor);
        gc.setLineWidth(originLineWidth);

    }

    /**
     * For a single user, he press btn to send the msg to his own text area
     */
    public void controlSendMsg() {

        String msg = this.msgField.getText();
        boolean isEmpty = msg == null || msg.isEmpty();


        ClientAppFacade clientApp = ClientAppFacade.getInstance();
        String userName = clientApp.getUsername();

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        logger.info(time);

        if (!isEmpty) {
            clientApp.sendMsg(time, msg);
            this.msgField.clear();
        }
    }

    public void updateNewUserWB(String username) {
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        WritableImage image = canvas.snapshot(sp, null);
        String encodstring = encodeToString(image, "png");
        ClientAppFacade.getInstance().updateWb("open," + encodstring, username);

    }
    
    private List<Double> convertStringToDoubleList(String input) {
        // Remove the leading and trailing double square brackets
        input = input.replaceAll("^\\[\\[", "").replaceAll("\\]\\]$", "");

        // Split the string by commas and trim whitespace
        return Arrays.stream(input.split(","))
                     .map(String::trim) // Trim whitespace from each substring
                     .map(Double::parseDouble) // Convert each substring to a Double
                     .collect(Collectors.toList()); // Collect into a List<Double>
    }
    
    private ArrayList<String> splitIgnoringDoubleBrackets(String input) {
        ArrayList<String> result = new ArrayList<>();

        // Regular expression to match commas outside double square brackets
        Pattern pattern = Pattern.compile(",(?![^\\[]*\\])");
        Matcher matcher = pattern.matcher(input);

        // Split the input string based on the pattern
        int start = 0;
        while (matcher.find()) {
            String part = input.substring(start, matcher.start()).trim();
            result.add(part);
            start = matcher.end();
        }

        // Add the last part of the string
        if (start < input.length()) {
            String lastPart = input.substring(start).trim();
            result.add(lastPart);
        }

        return result;
    }

    private static String encodeToString(WritableImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), type, bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }


    public void updateMessage(String msg) {
        msgArea.appendText(msg + "\n");
    }

    public void clearTextArea() {
        msgArea.clear();
    }
    public void disableUndoRedoButtons() {
        undo.setDisable(true);
        redo.setDisable(true);
    }
}

