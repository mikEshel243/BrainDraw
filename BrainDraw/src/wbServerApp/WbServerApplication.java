package wbServerApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import dataServerApp.IRemoteDb;
import javafx.application.Platform;
import wbServerData.WbServerDataStrategy;
import wbServerData.WbServerDataStrategyFactory;
import wbServerPre.wbServerViewControllers.CurrentWbListController;
import wbServerPre.wbServerViewControllers.CurrentWbMonitorController;

public class WbServerApplication {
    private final static Logger logger = Logger.getLogger(WbServerApplication.class);
    private static final Logger brokerLogger = Logger.getLogger("brokerLogger");

    private IRemoteWb remoteWb = null;
    private IRemoteDb remoteDb = null;

    private Process mosquittoProc = null;
    MqttClient mqttPublisher = null;

    private ArrayList<Whiteboard> whiteboards = new ArrayList<>();
    private HashMap<String, String> waitLists = new HashMap<>();

    /**
     * constructor
     */
    public WbServerApplication() {
        try {
            remoteWb = new RemoteWb();
        } catch (Exception e) {
            logger.fatal(e.toString());
            logger.fatal("Initialization whiteboard remote object failed");

            this.exit();
        }
    }

    /**
     * start run server at localhost
     * @param port port, String
     * @return JSON String respond
     */
    public String runWbServer(String port) {
        WbServerDataStrategy jsonStrategy = WbServerDataStrategyFactory.getInstance().getJsonStrategy();

        // parameter checking
        int portNum = 1111;
        try {
            portNum = Integer.parseInt(port);
        } catch (Exception e) {
            logger.error(e.toString());
            logger.error("port specified not valid, use default port number 1111");
            return jsonStrategy.packRespond(false, "Port number not valid!", "", "");
        }

        try {
            Registry registry = LocateRegistry.createRegistry(portNum);
            registry.rebind("Whiteboard", remoteWb);

            logger.info("Whiteboard server start running (by RMI) at port: " + portNum);
            return jsonStrategy.packRespond(true, "", "", "");
        } catch (Exception e) {
            logger.error(e.toString());
            logger.error("Whiteboard remote registry set up failed");
            return jsonStrategy.packRespond(false, "Could not start remote servants at port: " + portNum,
                    "", "");
        }
    }

    /**
     * Connect to data server
     * @param ip IP address, String
     * @param port port, String
     * @return JSON String respond
     */
    public String connectDbServer(String ip, String port) {
        WbServerDataStrategy jsonStrategy = WbServerDataStrategyFactory.getInstance().getJsonStrategy();

        // parameter checking
        int portNum = 1111;
        try {
            portNum = Integer.parseInt(port);
        } catch (Exception e) {
            logger.error(e.toString());
            logger.error("port specified not valid, use default port number 1111");
            return jsonStrategy.packRespond(false, "Port number not valid!", "", "");
        }

        try {
            //Connect to the rmiregistry that is running on localhost
            Registry registry = LocateRegistry.getRegistry(ip, portNum);

            //Retrieve the stub/proxy for the remote math object from the registry
            remoteDb = (IRemoteDb) registry.lookup("DB");

            logger.info("connect to data server at ip: " + ip + ", port: " + portNum);
            return jsonStrategy.packRespond(true, "", "", "");
        } catch (Exception e) {
            logger.error(e.toString());
            logger.error("Obtain remote service from database server(" + ip + ") failed");
            return jsonStrategy.packRespond(false, "Could not find DB remote servants at (" + ip
                    + ", " + portNum + ")", "", "");
        }
    }

    /**
     * Create new subprocess to start mosquitto broker
     * @param port Port
     * @return JSON String respond
     */
    public String startBroker(String port) {
        WbServerDataStrategy jsonStrategy = WbServerDataStrategyFactory.getInstance().getJsonStrategy();

        String[] cmd = new String[] {"/bin/bash", "-c", "/usr/local/sbin/mosquitto"};
        String broker = "tcp://localhost:1883";

        String os = System.getProperty("os.name");
//        if (os.startsWith("Windows")) {
//            cmd = new String[] {"D:\\mosquitto\\mosquitto", "-v"};
//        }

        if (port != null && !port.equals("")) {
            cmd = new String[] {"/bin/bash", "-c", "/usr/local/sbin/mosquitto", "-p", port};
            if (os.startsWith("Windows")) {
            	String codeLocation = System.getProperty("user.dir"); // Get the current working directory of the code
                cmd = new String[] {"cmd", "/c", codeLocation+"\\mosquitto\\mosquitto", "-v", "-p", port};
            }

            broker = "tcp://localhost:" + port;
        }

        MemoryPersistence persistence = new MemoryPersistence();

        try {
        	ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        	
        	processBuilder.redirectErrorStream(true);
            this.mosquittoProc = processBuilder.start();
            
        

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.mosquittoProc.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                    	brokerLogger.info(line);
                    }
                } catch (IOException e) {
                    brokerLogger.error(e.toString());
                }
            }).start();
            
            
            logger.info("Open mosquitto at port: " + broker);

            this.mqttPublisher  = new MqttClient(broker, "wbServer", persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setAutomaticReconnect(true);
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(1000);
            connOpts.setConnectionTimeout(1000);

            logger.info("Connecting to broker: " + broker);
            mqttPublisher.connect(connOpts);
            logger.info("Connected to broker successfully");

            return jsonStrategy.packRespond(true, "", "", "");
        } catch (Exception e) {
            logger.error(e.toString());
            logger.error("Create process to start broker and connect to it failed");

            return jsonStrategy.packRespond(false, "", "", "");
        }
    }

    /**
     * Register new users
     * @param username Username, String
     * @param password Password, String
     * @return JSON respond from data server, String
     */
    public String register(String username, String password) {
        try {
            return remoteDb.addUser(username, password);
        } catch (Exception e) {
            logger.error(e.toString());
            logger.error("New user register service from data server fail to execute");
            return "";
        }
    }

    /**
     * Existing user login authentication
     * @param username Username, String
     * @param password Password, String
     * @return JSON respond from data server, String
     */
    public String login(String username, String password) {
        try {
            return remoteDb.checkUser(username, password);
        } catch (Exception e) {
            logger.error(e.toString());
            logger.error("Existing user login authentication service from data server fail to execute");
            return "";
        }
    }

    /**
     * Create new whiteboard and set the user to be the manager
     * @param wbName Name of whiteboard, String
     * @param username Username, String
     * @return JSON respond, String
     */
    public synchronized String createWb(String wbName, String username) {
        WbServerDataStrategyFactory factory = WbServerDataStrategyFactory.getInstance();
        WbServerDataStrategy json = factory.getJsonStrategy();

        // check whether the same whiteboard is already created
        for (Whiteboard wb: whiteboards) {
            if (wb.getName().equals(wbName)) {
                return json.packRespond(false,
                        "There is one manager already sign up on this server, please join in", "", "");
            }
        }

        whiteboards.add(new Whiteboard(wbName, username));
        factory.getMqttPublish().publish(this.mqttPublisher, wbName + "/users", username, false);

        Platform.runLater(() -> {
            CurrentWbListController.getInstance().updateWbList(wbName);
        });

        return json.packRespond(true, "", "", "");
    }

    /**
     * Request to join created whiteboard on server
     * @param wbName Name of whiteboard, String
     * @param username Username, String
     * @return JSON respond, String
     */
    public synchronized String joinWb(String wbName, String username) {
        WbServerDataStrategyFactory factory = WbServerDataStrategyFactory.getInstance();
        WbServerDataStrategy json = factory.getJsonStrategy();

        for (Whiteboard wb: whiteboards) {
            if (wb.getName().equals(wbName)) {
                waitLists.put(username, wbName);

                String manager = wb.getManager();

                logger.info(username + " request to join whiteboard: " + wbName);
                Platform.runLater(() -> {
                    CurrentWbMonitorController.getInstance().updateLogger(username + " request to join whiteboard: " + wbName);
                });

                String respond = json.packRespond(true, username, "joinRequest", manager);
                factory.getMqttPublish().publish(this.mqttPublisher, wbName + "/general", respond, false);

                return json.packRespond(true, "", "", "");
            }
        }

        return json.packRespond(false,
                "No manager has signed up on this server, please sign up as manager first", "", "");
    }

    /**
     * Update pending join request from the specific user
     * @param username Username
     * @param isAllow True is the join request is approved
     */
    public void allowJoin(String username, boolean isAllow) {
        WbServerDataStrategyFactory factory = WbServerDataStrategyFactory.getInstance();
        WbServerDataStrategy json = factory.getJsonStrategy();

        String wbName = waitLists.get(username);
        String respond = json.packRespond(false, "Manager refused join request", "", username);
        String users = null;

        if (isAllow) {
            for (Whiteboard wb: whiteboards) {
                if (wb.getName().equals(wbName)) {
                    wb.addUser(username);
                    users = wb.getAllUsers();
                    respond = json.packRespond(true, "", "", username);

                    logger.info(wbName + " manager approve the join request from: " + username);
                    Platform.runLater(() -> {
                        CurrentWbMonitorController.getInstance().updateLogger(wbName + " manager approve the join request from: "
                                + username);
                        if (wb.getAllUsers() != null) {
                            factory.getMqttPublish().publish(this.mqttPublisher, wbName + "/users", wb.getAllUsers(), false);
                        }
                    });

                    break;
                }
            }
        }
        else {
            logger.info(wbName + " manager refuse the join request from: " + username);
            Platform.runLater(() -> {
                CurrentWbMonitorController.getInstance().updateLogger(wbName + " manager refuse the join request from: "
                        + username);
            });
        }

        factory.getMqttPublish().publish(this.mqttPublisher, wbName + "/join", respond, false);
        waitLists.remove(username);

        if (users != null) {
            factory.getMqttPublish().publish(this.mqttPublisher, wbName + "/users", users, false);
        }
    }

    /**
     * Get the name of all created whiteboards
     * @return JSON response, String
     */
    public String getCreatedWb() {
        String msg = "";

        for (Whiteboard wb: whiteboards) {
            msg += wb.getName() + ",";
        }

        return WbServerDataStrategyFactory.getInstance().getJsonStrategy().packRespond(true,
                msg, "", "");
    }

    /**
     * Close specific whiteboard
     * @param wbName Whiteboard name, String
     * @param username Username
     */
    public void closeWb(String wbName, String username) {
        WbServerDataStrategyFactory factory = WbServerDataStrategyFactory.getInstance();
        Whiteboard deleteWb = null;

        // get this whiteboard, notify all user using it and remove it from server
        for (Whiteboard wb: whiteboards) {
            if (wb.getName().equals(wbName)) {
                // manager close the whiteboard, notify all other users and close
                if (wb.getManager().equals(username)) {
                    deleteWb = wb;
                    String respond = factory.getJsonStrategy().packRespond(true, "Manager close the whiteboard",
                            "close", "");

                    logger.info("Manager close the whiteboard: " + wbName);
                    Platform.runLater(() -> {
                        CurrentWbMonitorController.getInstance().updateLogger("Manager close the whiteboard: " + wbName);
                    });

                    factory.getMqttPublish().publish(this.mqttPublisher, wb.getName() + "/general", respond, false);
                    factory.getMqttPublish().publish(this.mqttPublisher, wbName + "/users", "", false);
                    try {
                        logger.info("Manager has exited!!!"+username);
                        remoteDb.userExit(username);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                // visitor close the whiteboard, update the user list
                else {
                    logger.info("User: " + username + " quit from the whiteboard: " + wbName);
                    Platform.runLater(() -> {
                        CurrentWbMonitorController.getInstance().updateLogger("User: " + username
                                + " quit from the whiteboard: " + wbName);
                    });

                    wb.removeUser(username);
                    String users = wb.getAllUsers();
                    factory.getMqttPublish().publish(this.mqttPublisher, wbName + "/users", users, false);
                    try {
                        logger.info("Visitor exit!!!"+username);
                        remoteDb.userExit(username);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            else {
                try {
                    remoteDb.userExit(username);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }

        if (deleteWb != null) {
            whiteboards.remove(deleteWb);
        }
    }
    
    public void userExit (String username) {
        try {
            remoteDb.userExit(username);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    	
    }

    /**
     * Kick out specific visitor
     * @param wbName Whiteboard name, String
     * @param visitor Username of visitor
     */
    public void kickUser(String wbName, String visitor) {
        WbServerDataStrategyFactory factory = WbServerDataStrategyFactory.getInstance();
        String users = null;

        // notify this user and obtain updated users list
        for (Whiteboard wb: whiteboards) {
            if (wb.getName().equals(wbName)) {
                wb.removeUser(visitor);
                users = wb.getAllUsers();

                logger.info("Manager kick user " + visitor + " out of the whiteboard: " + wbName);
                Platform.runLater(() -> {
                    CurrentWbMonitorController.getInstance().updateLogger("Manager kick user " + visitor
                            + " out of the whiteboard: " + wbName);
                });

                String respond = factory.getJsonStrategy().packRespond(true, "Manager remove you from the group",
                        "close", visitor);

                factory.getMqttPublish().publish(this.mqttPublisher, wb.getName() + "/general", respond, false);
                break;
            }
        }

        // update users list
        if (users != null) {
            factory.getMqttPublish().publish(this.mqttPublisher, wbName + "/users", users, false);
        }
    }

    /**
     * Render all the whiteboards
     * @param wbName Whiteboard name, String
     * @param username Username, String
     * @param wb Whiteboard, String
     * @param receiver receiver
     */
    public synchronized void updateWb(String wbName, String username, String wb, String receiver) {
        logger.info(wbName + " whiteboard update of drawing: " + wb);
        Platform.runLater(() -> {
            CurrentWbMonitorController.getInstance().updateLogger(wbName + " whiteboard update of drawing: " + wb);
        });

        String msg = WbServerDataStrategyFactory.getInstance().getJsonStrategy().packRespond(true, wb, "", receiver);

        WbServerDataStrategyFactory.getInstance().getMqttPublish().publish(this.mqttPublisher,
                wbName + "/whiteboard", msg, false);
    }

    /**
     * Send message
     * @param wbName Whiteboard name, String
     * @param username Username, String
     * @param msg Message, String
     * @param time
     */
    public synchronized void sendMsg(String wbName, String username, String msg, String time) {
        logger.info(wbName + " whiteboard update of message: " + msg);
        Platform.runLater(() -> {
            CurrentWbMonitorController.getInstance().updateLogger(wbName + " whiteboard update of message: " + msg);
        });

        WbServerDataStrategyFactory.getInstance().getMqttPublish().publish(this.mqttPublisher,
                wbName + "/message", time+ " : "+ "From: "+username+" : "+msg, false);
    }

    /**
     * exit server program
     */
    public void exit() {
        try {
            UnicastRemoteObject.unexportObject(remoteWb, false);

            if (this.mosquittoProc != null) {
            	Runtime.getRuntime().exec("taskkill /F /IM mosquitto.exe");
                mosquittoProc.destroy();
                logger.info("Broker command process terminated.");
            }
            if (this.mqttPublisher != null) {
                mqttPublisher.disconnect();
            }

            logger.info("Exit successfully");
        } catch (Exception e) {
            logger.fatal(e.toString());
            logger.fatal("Whiteboard server remove remote object from rmi runtime failed");
        }

        System.exit(1);
    }
    

}
