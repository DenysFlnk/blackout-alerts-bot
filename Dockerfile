FROM eclipse-temurin:21

WORKDIR /bot

#Install dependencies for Chrome
RUN apt-get update \
    && apt-get install -y  \
        wget \
        unzip \
        libgconf-2-4 \
        libnss3 \
        libnspr4 \
        libpango-1.0-0 \
        libu2f-udev \
        libvulkan1 \
        libxcomposite1 \
        libxdamage1 \
        libxext6 \
        libxfixes3 \
        libxkbcommon0 \
        libxrandr2 \
        libgtk-3-0 \
        libgbm-dev \
        libglib2.0-0 \
        libgtk-4-1 \
        libx11-6 \
        libxcb1 \
        fonts-liberation \
        libasound2 \
        libatk-bridge2.0-0 \
        libatk1.0-0 \
        libatspi2.0-0 \
        libcairo2 \
        libcups2 \
        libdbus-1-3 \
        libdrm2 \
        xdg-utils \
    && apt-get clean

COPY ./src/main/resources/chrome/chrome_125.0.6422.141-1.deb chrome.deb

RUN dpkg -i chrome.deb && rm chrome.deb

COPY target/*.jar  blackout_alerts.jar

ENTRYPOINT ["java", "-jar", "blackout_alerts.jar"]