FROM ubuntu:18.04

RUN apt-get update && apt-get install -y build-essential cmake curl default-jdk libgtk2.0-dev \
    pkg-config libv4l-dev libavcodec-dev libavformat-dev libswscale-dev python-dev \
    python-numpy libtbb2 libtbb-dev libjpeg-dev libpng-dev libtiff-dev libdc1394-22-dev ant

ENV OPENCV_VERSION 4.3.0
ENV OPENCV_URL https://github.com/opencv/opencv/archive/${OPENCV_VERSION}.tar.gz
ENV ANT_HOME /usr/bin/ant

# chain commands using && to reduce number of layers
RUN curl -sL $OPENCV_URL | tar xvz \
    && cd opencv-$OPENCV_VERSION && mkdir build && cd build \
    && cmake \
    -D CMAKE_BUILD_TYPE=Release \
    -D BUILD_SHARED_LIBS=OFF \
    -D BUILD_EXAMPLES=OFF \
    -D BUILD_TESTS=OFF \
    -D BUILD_PERF_TESTS=OFF \
    -D BUILD_JAVA=ON \
    .. \
    && make -j2 \
    && make install \
    && mv bin/opencv-430.jar /usr/lib \
    && mv lib/libopencv_java430.so /usr/lib