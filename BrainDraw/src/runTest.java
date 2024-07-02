//-ip localhost -p 1111
public class runTest {
	public static void main(String[] args) {
		Thread dataServerThread = new Thread(() -> {
			runDataServer.main(new String[] {"-ip" ,"localhost" , "-p" ,"1111"});
		});
		dataServerThread.start();
		
		Thread WbServerThread = new Thread(() -> {
			runWbServer.main(new String[] {"-ip" , "localhost"});
		});
		WbServerThread.start();
		
//		Thread clientThread = new Thread(() -> {
//			runClient.main(args);
//		});
//		clientThread.start();
		
	}
}
