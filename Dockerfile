FROM incanter/incanter:1.9.1-SNAPSHOT

# boot
RUN wget https://github.com/boot-clj/boot-bin/releases/download/2.4.2/boot.sh
RUN mv boot.sh boot && chmod a+x boot && mv boot /usr/local/bin

ENV BOOT_AS_ROOT yes

# PVA-Parrot
RUN mkdir -p /usr/src/pva
WORKDIR /usr/src/pva
COPY build.boot /usr/src/pva/
RUN boot deps

COPY . /usr/src/pva

ENTRYPOINT ["boot", "tests"]
