From mysql:5.7

EXPOSE "3306:3306"

VOLUME [ "/var/lib/mysql" ]

ENV MYSQL_ROOT_PASSWORD=P@ssW0rd \
    MYSQL_DATABASE=iaasimov \
    MYSQL_USER=iaasimov \
    MYSQL_PASSWORD=iaasimov07 \
    MYSQL_DATA_DIR=/var/lib/mysql \
    MYSQL_RUN_DIR=/run/mysqld \
    MYSQL_LOG_DIR=/var/log/mysql

COPY db_scripts /docker-entrypoint-initdb.d

RUN service mysql start
