FROM postgres:16

ENV POSTGRES_USER=admin
ENV POSTGRES_PASSWORD=paaspassword
ENV POSTGRES_DB=Paas-TP1

COPY init.sql /docker-entrypoint-initdb.d/

EXPOSE 5432
