FROM ubuntu:18.04

RUN apt-get update && apt-get install -y build-essential cmake curl default-jdk libgtk2.0-dev \
    pkg-config libv4l-dev libavcodec-dev libavformat-dev libswscale-dev python-dev \
    python-numpy libtbb2 libtbb-dev libjpeg-dev libpng-dev libtiff-dev libdc1394-22-dev ant

ENV OPENCV_VER 4.3.0
ENV OPENCV_URL https://github.com/opencv/opencv/archive/${OPENCV_VER}.tar.gz

RUN curl -sL $OPENCV_URL | tar xvz

ENV ANT_HOME /usr/bin/ant

RUN cd opencv-4.3.0 && mkdir build && cd build && \
    cmake \
    -D CMAKE_BUILD_TYPE=Release \
    -D BUILD_SHARED_LIBS=OFF \
    -D BUILD_EXAMPLES=OFF \
    -D BUILD_TESTS=OFF \
    -D BUILD_PERF_TESTS=OFF \
    -D BUILD_JAVA=ON \
    ..

RUN cd opencv-4.3.0/build && make -j2
RUN cd opencv-4.3.0/build && make install
RUN mv /opencv-4.3.0/build/bin/opencv-430.jar /usr/lib
RUN mv /opencv-4.3.0/build/lib/libopencv_java430.so /usr/lib