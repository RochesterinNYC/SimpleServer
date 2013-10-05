JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
 
 
CLASSES = \
	Account.java \
	BlockedIP.java \
	Client.java \
	ClientStart.java \
	ClientThread.java \
	ConsoleThread.java \
	Server.java \
	ServerStart.java \
	ServerThread.java \
	ServerThreadType.java \
	Unblocker.java \

default: classes
 
classes: $(CLASSES:.java=.class)
clean:
	$(RM) *.class