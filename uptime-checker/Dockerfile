FROM alpine:3.22
RUN apk add --no-cache curl postgresql17-client
COPY check.sh /usr/local/bin/check.sh
RUN chmod +x /usr/local/bin/check.sh
USER nobody
ENTRYPOINT ["/usr/local/bin/check.sh"]