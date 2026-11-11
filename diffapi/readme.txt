使用说明
1、讲需要对比的jar，放在./lib目录下，新建对应的应用名称appName，old/new两个目录分别放置新旧jar
2、diff-Code.jar目录下运行 java -jar diff-Code.jar appName（appName替换成新建的appName）
3、有两个示例
	java -jar diff-Code.jar diagnosis
	java -jar diff-Code.jar dic
4、对比出的结果以日志形式输出在命令行界面