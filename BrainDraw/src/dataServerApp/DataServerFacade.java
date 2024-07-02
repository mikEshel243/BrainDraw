package dataServerApp;

import org.apache.log4j.Logger;
public class DataServerFacade {
    private final static Logger logger = Logger.getLogger(DataServerFacade.class);

    /** private singleton instance */
    private static DataServerFacade instance = null;

    private DataServerApplication dataServer = null;

    /**
     * Private constructor
     */
    private DataServerFacade(){
        dataServer = new DataServerApplication();
    }

    /**
     * get the singleton instance
     * @return singleton instance of DataServerFacade
     */
    public static DataServerFacade getInstance() {
        if (instance == null) {
            instance = new DataServerFacade();
        }
        return instance;
    }

    public void setupRemoteApplication(){
        // Singleton server application.
        if (this.dataServer != null){
            logger.info("Remote application starts. ");
            dataServer.setRemoteDb(this);
        }
        else{
            logger.fatal("Error. Server application does not start properly. ");
        }
    }


    /**
     * start run server
     */
    public void runDataServer() {
        dataServer.runDataServer();
    }

    /**
     * exit server program
     */
    public void exit() {
        dataServer.exit();
        logger.info("User exit data server program");
        System.exit(1);
    }

    String addUser(String username, String password){
        return dataServer.addUser(username, password);
    }

    String checkUser(String username, String password){
        return dataServer.checkUser(username, password);
    }
    public void userExit(String username){
        dataServer.userExit(username);
    }


    void iteratePassBook(){
        dataServer.iteratePassBook();
    }

    public DataServerApplication getDataServer() {
        return this.dataServer;
    }
}
