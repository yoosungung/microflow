cd ~/microflow

CURRENT_PID=$(pgrep -f microflow-1.0-SNAPSHOT-jar-with-dependencies.jar)
if [ -z "$CURRENT_PID" ]; then
	  echo "> Current Server is nothing"
	else
		  kill -15 $CURRENT_PID
fi

git pull
git checkout master

mvn clean package

nohup java -jar /home/dulle2/microflow/target/microflow-1.0-SNAPSHOT-jar-with-dependencies.jar & 


