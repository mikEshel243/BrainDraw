package dataServerApp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RemoteDb extends UnicastRemoteObject implements IRemoteDb {
    private static final long serialVersionUID = 1L;
	private DataServerFacade facade = null;

    public RemoteDb(DataServerFacade facade) throws RemoteException{
        super();
        this.facade = DataServerFacade.getInstance();
    }

    /**
     * Register a user into the database.
     * @param username  Username entered
     * @param password  Password entered
     * @return
     * @throws RemoteException
     */
    @Override
    public String addUser(String username, String password) throws RemoteException {
        String returnMessage = facade.addUser(username, password);

        JSONParser jsonParser = new JSONParser();

        //Read JSON requst
        try {
            JSONObject returnJSON = (JSONObject) jsonParser.parse(returnMessage);
            if (returnJSON.get("header").equals("Success")){
                facade.iteratePassBook();
            }
        } catch (Exception e) {
            // TODO: Exception catching
        	e.printStackTrace();
        }

        return returnMessage;
    }

    /**
     * Check if a user is authorised to perform actions. This method envokes Authentication module.
     * The return message will be a string but with json format. The message has: "header": [Success/ Fail],
     * "message": [message body]
     * @param username  username that the user entered.
     * @param password  password that the user entered.
     * @return  A string with JSON format.
     * @throws RemoteException
     */
    // Authenticate the user by using the information stored in Authenticator.
    @Override
    public String checkUser(String username, String password) throws RemoteException {
        return facade.checkUser(username, password);
    }
    @Override
    public void userExit(String username) throws RemoteException{
        facade.userExit(username);
    }


}
