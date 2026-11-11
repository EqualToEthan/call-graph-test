#构建此镜像的基础镜像
FROM persagy2021-docker.pkg.coding.net/yushu/common/centos:v-jdk1.8
#指定作者名称
MAINTAINER wangchenghong<wangchenghong@yushu.biz>

#定义标签属性
LABEL project=dtp-electricity-server

#ARG 可以接收docker build命令中以--build-arg 指定的参数
ARG WORKER_HOME=/app

#设置环境变量
ENV JAVA_OPTS -Dfile.encoding=UTF-8 -Xms2024m -Xmx2024m -XX:+PrintGCDateStamps -XX:+PrintGCDetails
ENV TZ Asia/Shanghai

#执行的命令
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime	&& echo 'Asia/Shanghai' > /etc/timezone

RUN mkdir -p $WORKER_HOME/config

#复制文件到容器内
COPY *.jar $WORKER_HOME/app.jar
COPY entrypoint.sh $WORKER_HOME/
#设置工作目录
WORKDIR $WORKER_HOME

#查看上述复制的文件，是否复制成功
RUN ls

#启动执行
ENTRYPOINT ["sh","./entrypoint.sh"]