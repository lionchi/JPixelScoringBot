package ru.gpb.jpixelscoringbot.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gpb.jpixelscoringbot.config.MinioProperties;
import ru.gpb.jpixelscoringbot.exception.JPixelScoringBotException;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public String getImageUrl(String imagePath) {
        try {
            var found = minioClient.bucketExists(
                            BucketExistsArgs.builder()
                                    .bucket(minioProperties.getNameBucket())
                                    .build());

            if (!found) {
                throw new JPixelScoringBotException("Bucket c названием " + minioProperties.getNameBucket() + " не найден в Minio");
            }

            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioProperties.getNameBucket())
                    .object(imagePath)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
        } catch (Exception e) {
            throw new JPixelScoringBotException("Не удалось загрузить файл из Minio " + imagePath, e);
        }
    }

    @Override
    public InputStream getImage(String imagePath) {
        try {
            var found = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioProperties.getNameBucket())
                            .build());

            if (!found) {
                throw new JPixelScoringBotException("Bucket c названием " + minioProperties.getNameBucket() + " не найден в Minio");
            }

            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(minioProperties.getNameBucket()).object(imagePath).build());
        } catch (Exception e) {
            throw new JPixelScoringBotException("Не удалось загрузить файл из Minio " + imagePath, e);
        }
    }
}
