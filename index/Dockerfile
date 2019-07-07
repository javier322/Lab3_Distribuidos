FROM gradle
WORKDIR /workspace/index
 
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN ./gradlew dependencies

RUN gradle -v
