To Operate:
Run "make" in the project directory.

To run the Server, enter:
java ServerStart [portNumber]

To run the Client, enter:
java ClientStart [IPAddress] [portNumber]

Implementation Notes:

- ClientThread is a thread because originally I had a double threaded client for broadcasting. However, I ran into a great deal of problems with the broadcasting and decided not to implement it for now. I kept it as a thread because I'm going to take another stab at it later (on my own time) and I'm also interested in expanding this server project.

- I used System.currentTimeMillis() throughout the implementation because of the warning that the Java Date API would not work properly with cunix.

- User can change the number of allowed login attempts and time for IPs to be blocked through the SimpleServer console but are respectively initially set to 3 and a minute.

- For the server log, I did not implement interrupt (through control + c) catching/handling so the server logging will only work if the server administrator uses shut_logger to close the server logger stream before closing the server.

- Additionally, there are some large if--> if-else structures in my implementation because switch statements on string equality are not available in Java 6 (they are in Java 7 though).

- Broadcast is not implemented.

Extra Features:

- Server Log
  - Server status and client interaction messages are logged in server_log.txt. 
  - The log persists from sequential server shutdowns and starts as long as the shutdowns are preceded by the server admin running "shut_logger".
  - Uses appending to file and not replacing of file.
  - Will create a server_log.txt file if one is not already created.

- Logout functionality
  - Users can logout from the server and the accounts.
  - whoelse, wholasthr, and the console operations reflect these logouts

- SimpleServer Admin Console
  - Instead of having the server simply output messages and statuses (these go into the server log instead), starting the server presents the admin with a simple console that allow for some commands relating to viewing the status of the servers, clients, blocked IPs and operations regarding these.