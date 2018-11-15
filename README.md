# grpc-java-chat
grpc-java example

```
compile:
gradle -q serverJar clientJar

test:

λ java -jar chatClient-1.0-SNAPSHOT.jar
please input your nickname
jack
2018-11-15 19:38:01 [main] INFO (ChatClient.java:45) - login with name jack OK!
hello
2018-11-15 19:38:07 [grpc-default-executor-0] INFO (ChatClient.java:72) - user jack:hello
2018-11-15 19:38:17 [grpc-default-executor-0] INFO (ChatClient.java:62) - user rose:login!!
2018-11-15 19:38:28 [grpc-default-executor-0] INFO (ChatClient.java:72) - user rose:Hi everyone!

λ java -jar chatClient-1.0-SNAPSHOT.jar
please input your nickname
rose
2018-11-15 19:38:17 [main] INFO (ChatClient.java:45) - login with name rose OK!
Hi everyone!
2018-11-15 19:38:28 [grpc-default-executor-0] INFO (ChatClient.java:72) - user rose:Hi everyone!

λ java -jar chatServer-1.0-SNAPSHOT.jar
2018-11-15 19:38:07 [grpc-default-executor-0] INFO (ChatRoomServiceImpl.java:65) - got message from jack :hello
2018-11-15 19:38:28 [grpc-default-executor-0] INFO (ChatRoomServiceImpl.java:65) - got message from rose :Hi everyone!
```