package wbServerApp;

public class WbServerFacade {
    /** private singleton instance */
    private static WbServerFacade instance = null;

    private WbServerApplication wbServer = null;

    /**
     * Private constructor
     */
    private WbServerFacade() {
        wbServer = new WbServerApplication();
    }

    /**
     * get the singleton instance
     * @return singleton instance of WbServerFacade
     */
    public static WbServerFacade getInstance() {
        if (instance == null)
            instance = new WbServerFacade();

        return instance;
    }

    /**
     * start run server at localhost
     * @param port Port number, String
     * @return JSON String respond
     */
    public String runWbServer(String port) {
        return wbServer.runWbServer(port);
    }

    /**
     * connect to database server
     * @param ip IP address, String
     * @param port port, String
     * @return JSON String respond
     */
    public String connectDbServer(String ip, String port) {
        return wbServer.connectDbServer(ip, port);
    }

    /**
     * Create new subprocess to start mosquitto broker
     * @param port Port
     * @return JSON String respond
     */
    public String startBroker(String port) {
        return wbServer.startBroker(port);
    }

    /**
     * Register new users
     * @param username Username, String
     * @param password Password, String
     * @return JSON respond from data server, String
     */
    public String register(String username, String password) {
        return wbServer.register(username, password);
    }

    /**
     * Existing user login authentication
     * @param username Username, String
     * @param password Password, String
     * @return JSON respond from data server, String
     */
    public String login(String username, String password) {
        return wbServer.login(username, password);
    }

    /**
     * Create new whiteboard and set the user to be the manager
     * @param wbName Name of whiteboard, String
     * @param username Username, String
     * @return JSON respond, String
     */
    public String createWb(String wbName, String username) {
        return wbServer.createWb(wbName, username);
    }

    /**
     * Request to join created whiteboard on server
     * @param wbName Name of whiteboard, String
     * @param username Username, String
     * @return JSON respond, String
     */
    public String joinWb(String wbName, String username) {
        return wbServer.joinWb(wbName, username);
    }

    /**
     * Update pending join request from the specific user
     * @param username Username
     * @param isAllow True is the join request is approved
     */
    public void allowJoin(String username, boolean isAllow) {
        wbServer.allowJoin(username, isAllow);
    }

    /**
     * Get the name of all created whiteboards
     * @return JSON response, String
     */
    public String getCreatedWb() {
        return wbServer.getCreatedWb();
    }

    /**
     * Close specific whiteboard
     * @param wbName Whiteboard name, String
     * @param username Username
     */
    public void closeWb(String wbName, String username) {
        wbServer.closeWb(wbName, username);
    }

    /**
     * Kick out specific visitor
     * @param wbName Whiteboard name, String
     * @param visitor Username of visitor
     */
    public void kickUser(String wbName, String visitor) {
        wbServer.kickUser(wbName, visitor);
    }
    
    public void userExit(String username) {
    	wbServer.userExit(username);
    }

    /**
     * Render all the whiteboards
     * @param wbName Whiteboard name, String
     * @param username Username, String
     * @param wb Whiteboard, Stringee
     * @param receiver receiver
     */
    public void updateWb(String wbName, String username, String wb, String receiver) {
        wbServer.updateWb(wbName, username, wb, receiver);
    }

    /**
     * Send message
     * @param wbName Whiteboard name, String
     * @param username Username, String
     * @param msg Message, String
     */
    public void sendMsg(String wbName, String username, String msg, String time) {
        wbServer.sendMsg(wbName, username, msg, time);
    }

    /**
     * exit server program
     */
    public void exit() {
        wbServer.exit();
    }
}
