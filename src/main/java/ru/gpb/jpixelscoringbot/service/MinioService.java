package ru.gpb.jpixelscoringbot.service;

import java.io.InputStream;

public interface MinioService {

    String getImageUrl(String imagePath);

    InputStream getImage(String imagePath);
}
