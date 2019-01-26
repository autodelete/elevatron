# Elevatron Java Challenge

This is an elevator simulator program written in Java which 
simulates multiple humans using multi-elevator system across several floors
and visualizes everything on a screen in a realtime.

The simulator contains everything except an elevator controller implemented.
The controller which is included into a package - randomly blinks lights, opens 
and closes shaft doors and causes humans to fall into the elevator shaft :(

TODO: Add animated gid screenshots

Your goal is to implement such a controller, save human lives and last, but not least, learn how to
program state machines in Java!

The program is created for educational purposes to encourage everyone who
learns software engineers to practice with building state machines.

## Instructions for Ubuntu 18 and IntelliJ IDEA:
Instructions for Ubuntu 18 and:

1. Install JDK 8, JavaFX and git
```bash
sudo apt -y install openjdk-8-jdk openjfx git
```

2. Install IntelliJ IDEA: https://www.jetbrains.com/idea/

3. Fork this repository to your GitHub account and upload your Controller.java
  - Don't have GitHub account? Go to https://github.com/join and register one
  - Press `Fork` button on https://github.com/autodelete/elevatron

4. Clone repository to your machine
```bash
git clone https://github.com/YOUR-GITHUB-USER_ID/elevatron
```  

5. Open cloned repository directory with IDEA

6. Configure IntelliJ: 
  - File -> Project Structure -> Platform Settings -> SDKs -> Add openjdk-8 with `/usr/lib/jvm/java-8-openjdk-amd64`
  - File -> Project Structure -> Platform Settings -> Global Libraries -> Add -> Java -> `/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/jfxrt.jar`
  - File -> Project Structure -> Project Settings -> Project -> Project SDK -> Select openjdk-8

7. Solve the challenge
  - add your code into src/elevatron/controller/Controller.java

8. Upload your solution
```bash
git add src/elevatron/controller/Controller.java
git commit
git push
```

## Have issues ?
Submit a bug https://github.com/autodelete/elevatron/issues

## Want to contribute?
You're more than welcome! Please send your pull requests!
Areas needing improvement:
 - Adding realistic graphics
 