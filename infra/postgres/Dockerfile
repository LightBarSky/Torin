FROM postgres:16

COPY ./infra/postgres/init /docker-entrypoint-initdb.d
COPY ./infra/postgres/pgbackrest/pgbackrest.conf /etc/pgbackrest/pgbackrest.conf
RUN chmod +x /docker-entrypoint-initdb.d/*.sh

RUN mkdir -p /build

RUN apt-get update && \
    apt-get install -y \
	    wget \
        meson \
        gcc \
        libpq-dev \
        libssl-dev \
        libxml2-dev \
        pkg-config \
        liblz4-dev \
        libzstd-dev \
        libbz2-dev \
        zlib1g-dev \
        libyaml-dev \
        libssh2-1-dev \
		postgresql-client \
		libxml2 \
		libssh2-1

RUN wget -q -O - \
       https://github.com/pgbackrest/pgbackrest/archive/release/2.58.0.tar.gz | \
       tar zx -C /build

RUN meson setup /build/pgbackrest /build/pgbackrest-release-2.58.0
RUN ninja -C /build/pgbackrest

RUN cp /build/pgbackrest/src/pgbackrest /usr/bin/pgbackrest && \
    chmod 755 /usr/bin/pgbackrest

# directories
RUN mkdir -p /var/lib/pgbackrest \
    && mkdir -p -m 770 /var/log/pgbackrest \
    && mkdir -p /etc/pgbackrest/conf.d \
	&& mkdir -p /tmp/pgbackrest \
    && chown -R postgres:postgres /var/lib/pgbackrest /var/log/pgbackrest /etc/pgbackrest /tmp/pgbackrest