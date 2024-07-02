package clientApp;

import clientData.ClientDataStrategy;
import clientData.ClientDataStrategyFactory;
import clientPre.clientViewControllers.ClientGUIController;
import clientPre.clientViewControllers.whiteBoardController.whiteBoardController;
import javafx.application.Platform;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;

/**
 * Client call back implementation
 */
public class ClientMqttCallBack implements MqttCallback {
    /** logger */
    private final static Logger logger = Logger.getLogger(ClientMqttCallBack.class);

    // settings
    private final static String WB_PANEL = "whiteboard";
    private final static String MSG_PANEL = "message";
    private final static String USER_PANEL = "users";
    private final static String GENERAL_PANEL = "general";
    private final static String JOIN_PANEL = "join";

    @Override
    public void connectionLost(Throwable throwable) {
        logger.error(throwable.getCause());
        logger.error(throwable.getMessage());
        logger.error(throwable.toString());
        logger.error("Connection to MQTT broker lost");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        String msg = new String(mqttMessage.getPayload());
        logger.info("Message received: " + s + ", " + msg);

        if (s.contains(JOIN_PANEL)) {
            this.joinPanelHandle(s, msg);
        }

        if (s.contains(WB_PANEL)) {
            // TODO call whiteboard update function in clientGUI
            String user = ClientDataStrategyFactory.getInstance().getJsonStrategy().getUser(msg);
            String content = ClientDataStrategyFactory.getInstance().getJsonStrategy().getMsg(msg);

            if (user.equals("") || user.equals(ClientAppFacade.getInstance().getUsername())) {
                // msg contains the string version of updated whiteboard
                Platform.runLater(() -> {
                    try {
                        whiteBoardController.getInstance().
                                updateWhiteBoard(content);
                    } catch (IOException e) {
                        logger.error(e.toString());
                        logger.error("Unable to call whiteboard update function");
                    }
                });
            }
        }

        if (s.contains(MSG_PANEL)) {
            // TODO call message update function in clientGUI
            // msg contains the string version of updated texts communication

             Platform.runLater(()-> {
                 whiteBoardController.getInstance().updateMessage(msg);

            });
        }

        if (s.contains(USER_PANEL)) {
            // TODO call user list update function in clientGUI
            // msg contains the list of users: manager,user1,user2,user3
             Platform.runLater(()-> {
                 whiteBoardController.getInstance().updateUserList(msg);
            });
        }

        if (s.contains(GENERAL_PANEL)) {
            this.generalPanelHandle(msg);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        // currently not used
    }

    /**
     * join request results notification handling
     * @param msg Server respond message, JSON String
     */
    private void joinPanelHandle(String s, String msg) throws IOException {
        ClientAppFacade clientApp = ClientAppFacade.getInstance();
        ClientDataStrategy jsonStrategy = ClientDataStrategyFactory.getInstance().getJsonStrategy();

        String user = ClientDataStrategyFactory.getInstance().getJsonStrategy().getUser(msg);

        clientApp.setWbName(s.split("/")[0]);
        String wbName = clientApp.getWbName();
        clientApp.unsubscribeTopic(wbName, ClientAppFacade.nonUserTopics);

        if (user.equals("") || user.equals(clientApp.getUsername())) {
            if (jsonStrategy.getHeader(msg)) {
                logger.info("join request approved");
                clientApp.subscribeTopic(wbName, ClientAppFacade.UserTopics, ClientAppFacade.UserQos);

                Platform.runLater(()-> {
                    try {
//                    	whiteBoardController.getInstance().updateUserList(msg);
                        ClientGUIController.getInstance().showWhiteBoardView();
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                        logger.error("Show whiteboard view failed");
                    }
                });
            }
            else {
                logger.info("join request refused");
                clientApp.setWbName("");

                Platform.runLater(()-> {
                    try {
                        ClientGUIController.getInstance().showJoinDeniedView(jsonStrategy.getMsg(msg));
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                        logger.error("Show join denied view failed");
                    }
                });
            }
        }
    }

    /**
     * General notification handling, including new user join (only for manager), manager kick out users or quit the whiteboard
     * @param msg Server respond message, JSON String
     */
    private void generalPanelHandle(String msg) throws IOException {
        ClientAppFacade clientApp = ClientAppFacade.getInstance();
        ClientDataStrategy jsonStrategy = ClientDataStrategyFactory.getInstance().getJsonStrategy();

        String user = ClientDataStrategyFactory.getInstance().getJsonStrategy().getUser(msg);

        if (user.equals("") || user.equals(ClientAppFacade.getInstance().getUsername())) {
            String category = ClientDataStrategyFactory.getInstance().getJsonStrategy().getCategory(msg);
            String message = jsonStrategy.getMsg(msg);

            if (category.equals("joinRequest")) {
                Platform.runLater(()-> {
                    ClientGUIController.getInstance().showJoinRequestView(message);
                });
            }

            else if (category.equals("close")) {
                String wbName = clientApp.getWbName();
                clientApp.unsubscribeTopic(wbName, ClientAppFacade.UserTopics);

                Platform.runLater(()-> {
                    try {
                        ClientGUIController.getInstance().showCloseView(message);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                        logger.error("Show close view failed");
                    }
                });
            }
        }
    }
}
