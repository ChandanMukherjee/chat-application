# chat-application Socket Server supporting multiple awt clients

This is a multithreaded chat application with a server and client. When a client connects to the server, the server requests a screen name by sending the client the text "SUBMITNAME", and keeps requesting a name until a unique one is received.  After a client submits a unique name, the server acknowledges with "NAMEACCEPTED".  Then server can accept two commands LIST and LOGOUT and send messages to other clients. When server receives a LIST command it sends the list of names of all connected contacts except the user requesting it. When the server receives a LOGOUT Command it closes the connection associated with the contact name requesting logout. The servers extracts client name preceding a message to send to the respective client with same screen name. The messages are prefixed with the word "MESSAGE ". Without any name mentioned the servers broadcasts the message to everyone.

To run the build and generate deployable jar files performing following three steps

1. Define Environment variable name ANT_HOME pointing to the folder where Apache ANT is installed.

2. SET PATH to point to ANT and JDK
if C:\Program Files\Java\jdk1.7.0_67\bin is the path for your JDK installation run the following script at windows command prompt. from the directory of the build.xml file.

SET PATH=%ANT_HOME%\bin;C:\Program Files\Java\jdk1.7.0_67\bin
OR
SET PATH=%PATH%;%ANT_HOME%\bin;C:\Program Files\Java\jdk1.7.0_67\bin

3. Run ANT from the directory where build.xml is located - ChatServer and ChatClient
ant 

The report is produced in the folder named ChatServer\testreport in a xml format

The runnable jar files are deployed in the folders named ChatServer\deploy and ChatClient\deploy

To run Server from Command prompt where the jar file is located -
java -jar com.ibm.chat.Server.jar

To run Client from Command prompt where the jar file is located -
java -jar com.ibm.chat.Client.jar