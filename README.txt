The project was configured to Eclipse IDE. 

Plugins used: 
- Subversive (SVN)
- Sysdeo Tomcat plugin
- Eclipse-javacc (Compiles cc files)

Tip: 

You can change server.xml tomcat configuration file adding this line before the </HOST> tag: 
<Context path="/priki" reloadable="true" docBase="xxx/workspace/priki" workDir="xxx/workspace/priki/work" />
Thus, you can't need deploy priki all the time. Tomcat simply reload it.