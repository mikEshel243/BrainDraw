### Step 1
Run the user data server:

"java -jar <dataServer.jar_location> -ip <network_ipv4_address> -p <port_number>"

Example:

"java -jar dataServer.jar -ip localhost -p 1111"

### Step 2:
Run the whiteboard server:

"java -jar <wbServer.jar_location> -ip <network_ipv4_address>"

Example:

"java -jar wbServer.jar -ip localhost"

### Step 3:
Run the client application:

"java -jar <client.jar_location>"