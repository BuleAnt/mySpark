##start hadoop
$HADOOP_HOME/sbin/start-all.sh
##start hiveClient interface. hive-site.xml
# bin/hive --service hiveserver2 &
sudo service mysqld start
$HIVE_HOME/bin/hive --service metastore &

无法local运行