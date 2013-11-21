SimpleServer (W4119 : Computer Networks)

This is a Java implementation of a multi-threaded server and client that offers such features as logging in/out, messaging, broadcasting, checking information about other logged on client users.

Additionally, file transmission is available through simulated TCP by way of UDP and TCP protocol.

Some other implemented features include server logging, guaranteed file delivery, and a basic, working console for the server admin that implements some admin functions.

- jrw2175

HW Assignment II:

To Operate:
Run "make" in the project directory.

To run the Server/Sender, enter:
java ServerStart [portNumber]

To run the Client/Receiver, enter:
java ClientStart [IPAddress] [portNumber]

How to Run:
Client/Receiver-side:
- Log in to an account (ex. Username: Columbia, Password: 116bway)
- Enter the "file" user option when prompted with menu.
- Enter command with arguments as follows: "receiver [file name] [listening port] [remote IP] [remote port] [log file name]"
- Client/Receiver now waits to receive packets.

Server/Sender-side:
- Use the server console provided to type in the "file_send" command.
- Enter comamand with arguments as follows: "sender [file name] [remote IP] [remote port] [ack port number] [window size] [log file name]"

General Command Flow:

Client --> Selects file option
Interfacing Server Thread --> Waits for TCP transaction to be finished (waits for Client to finish)
ConsoleThread --> Server Admin types in command to start sending file through "TCP"

Server --> sends UDP packets to specified port and IP of client
Client --> sends back UDP packets that are ACK or CORR indication messages
(Server-side = If Client UDP ACK does not make it back or ACK is corrupt or ACK indicates file is corrupt, server resends the UDP data packet) 
   if server times out without receiving ack
    resends data packet
  if server receives a corrupt ACK or ACK-corrupt
    recents data packet
  if receives correct ACK
    moves on
(Client-side = If Server UDP data does not make it to client or server UDP data, then server waits for ACK and eventually times out and resends UDP data)
  if client senses it is repeat
    ignores it and resends its ACK for the previous packet
  if client senses it is corrupt
    sends ACK - corrupt
  if client receives an uncorrupted packet
    processes it and sends its ACK for that packet
Repeats until entire file has been sent
  Server --> sends FIN message to Client and ends method
  Client --> receives FIN message and option menu and interaction can continue

Client 
one socket -->  sends ACK datagrams and receives file datagrams from server
(listening_port)

Server
one socket --> sends file datagrams and receives ACK datagrams from client 
(ack_port_number)

- 576 = Segment size

Header Format:
Source Port Number = 16 bits = 2 bytes
Destination Port Number = 16 bits = 2 bytes
Sequence Number = 32 bits = 4 bytes
ACK # = 32 bits = 4 bytes
Purpose Code = 8 bits = 1 byte   (ACK, FIN, and DATA)
Data Length = 16 bits = 2 bytes
TCP CheckSum = 64 bits = 5 bytes

Payload indices are as follows:
 * Packet payload - Byte Array Structure:
 * Index = Contents
 * 0-1 = sourcePortNumber
 * 2-3 = destPortNumber
 * 4-7 = sequenceNumber
 * 8-11 = ACK #
 * 12 = purpose code
 * 13-14 = data length (in bytes) 
 * 15-19 = TCP CheckSum
 * (if data) 20-575 = data

 Purpose Code representations are as follows:
 * Byte Representation = Meaning
 * 00000000 = DATA
 * 00000001 = ACK
 * 00000010 = CORR (Received packet was corrupt)
 * 00000011 = FIN

ACK Assignment:
if (purposeCode is ACK), then ACK # = sequence number of last packet received
if (purposeCode is DATA), then ACK# = sequence number of last packet received (packet before this one)

Server Data Message
Seq # = first byte of data part of this packet
ACK # = Seq # + 556

Client ACK Message
Seq # = matches the ACK of the data packet received
ACK # = Seq # of data packet received + 556

Example:
Server --> Client = Seq# = 1 ACK = 557
Server increases sequence number (from 1 --> 557)
Server is looking for ACK with ACK# 557 now
Client receives it and checks if message seq # matches its seq number (1)
Client's seq number changed to received message's ACK (557)
Client is now looking for message with seq 557
Client sends back ACK with ACK# = current client seq number
Client --> Server = ACK Seq # = 0 ACK # = 557

Other Notes:
- Start the sequence numbers with 1
- Took number of segments resent to purely mean number of times any segment was retransmitted, not number of segment parts that required some type of retransmitting
- Repeated simulations allowed due to datagram sockets being closed after successful TCP simulated transfer of file

Extension of Part I:
- The TCP Functionality is added as a functional module to part I. All the operations in part I are still intact.

HW Part I:

To Operate:
Run "make" in the project directory.

To run the Server, enter:
java ServerStart [portNumber]

To run the Client, enter:
java ClientStart [IPAddress] [portNumber]

Implementation Notes:

- The client is double threaded to implement broadcasting. One thread interacts with the server so that the client can perform operations and access the server menu while the other is listening to the server for broadcast information and prints it to the client system if a broadcast message with content is sent from the server.

- Server is also multithreaded with four different types of server threads.
  1. Base - creates console server thread on server startup and then listens in on argument-specified port and handles creation of either server client threads or server broadcast threads on receiving new client connections.
  2. Client - Handles interfacing (login, blocked IP notification, option menu, and client commands/input) with client.
  3. Broadcast - Runs concurrent and in parallel with a server client thread and is connected to the broadcast thread of a client. Sends server broadcast messages and receives acknowledgement messages from client.
  4. Console - Handles server admin console functionality and operations.

- I used System.currentTimeMillis() throughout the implementation because of the warning that the Java Date API would not work properly with cunix.

- User can change the number of allowed login attempts and time for IPs to be blocked through the SimpleServer console but are respectively initially set to 3 and a minute.

- For the server log, I did not implement interrupt (through control + c) catching/handling so the server logging will only work if the server administrator uses shut_logger to close the server logger stream before closing the server.

- Additionally, there are some large if--> if-else structures in my implementation because switch statements on string equality are not available in Java 6 (they are in Java 7 though).

- Very meticulous detail spent in making sure no loose threads (like server or broadcast threads) remain and all sockets, scanners, and printwriters are closed after clients log out.

Protocols:

- Broadcast Protocol
  - Client broadcast thread is constantly listening to server for broadcast info just as the server is constantly sending broadcast info. Whenever a client broadcast thread receives this info, it sends an acknowledgement message "ack" to the server broadcast thread that it's connected to and the server keeps count of the number of these ack messages received. When the number of acknowledgement messages received by the server's server broadcast threads matches the number of server broadcast threads, then the server knows that each client broadcast thread has received the broadcasted message and resets the broadcast message and count. 
  When a user or server admin is not trying to send a broadcast out, empty broadcast info is being sent out and the client broadcast threads still receive and acknowledge receiving this info but knows not to print it or display it to the client.

- Server Thread Creation Protocol
  - When the server is started up, that main thread is the base server thread that listens on the argument-specified port for connection attempts by clients. It also creates a console server thread that handles and allows the server admin to view and perform server admin operations. Whenever a new client connects, a server client thread is created and passed the socket connection and this thread now handles the interfacing with the clients (input and output of server options and client input/commands). The baseWaiting variable of the server indicates whether the base server thread is essentially going to create a server client thread or server broadcast thread on receiving the next client connection attempt.
  - baseWaiting is set to false (next connection to server will create a server broadcast thread) when a client successfully logins and creates its concurrently running client broadcast thread that will initiate a connection with the server. This connection will be passed to the newly created server broadcast thread and baseWaiting will be set back to true.

- Logout Protocol
  - When a client initiates the logout command while logged into an account, the client's toLogOut boolean switch is set to true and logout proceeds with the main client thread sending the logout selection to the server and the appropriate server operations relating to modifying the number of connected clients, setting the accounts to inactive if applicable, ending of the previously connected server client and broadcast threads, etc. are carried out. The main client thread then ends and the client's concurrently running broadcast thread on its next iteration sees that the client is set to log out and ends.

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
  - Instead of having the server simply output messages and statuses (these go into the server log instead), starting the server presents the admin with a simple console that allow for some commands relating to broadcasting messages, viewing the status of the servers, clients, blocked IPs, messages, and operations regarding these.

- Account Messaging System (messages and send)
  - Users logged in on an account can send messages from that account to another account (can be to themselves) and read the messages that the account they're logged in as has received.
  - Currently, one line subjects and bodies are implemented.
  - Messages are logged in the server log and server admin has the ability to view all messages on the server.

