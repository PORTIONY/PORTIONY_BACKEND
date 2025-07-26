package com.portiony.portiony.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {
    @Value("${S3_ACCESS_KEY}")
    private String accessKey;

    @Value("${S3_SECRET_KEY}")
    private String secretKey;

    private static final String S3_URL_PREFIX = "https://%s.s3.amazonaws.com/";
    private static final Region REGION = Region.of("eu-central-1");
    private static final String bucket = "portiony-bucket";
    private S3Client s3;

    /**
     * 파일 이름을 생성하고 s3 서버에 업로드
     * @param files 업로드 할 파일들
     * @param s3Folder 업로드 할 s3 폴더명 (test면 test/123.png 식의 경로로 저장)
     * @return 이미지가 업로드 된 s3 서버의 링크
     */
    public List<String> upload(List<MultipartFile> files, String s3Folder){
        List<String> uploadedUrls = new ArrayList<>();

        //s3Folder가 null 또는 공백이면 빈 문자열로 처리
        s3Folder = (s3Folder == null || s3Folder.isBlank()) ? "" : s3Folder.trim();

        for(MultipartFile file : files) {
            try {
                String fileName = Paths.get(s3Folder, generateUniqueFileName(file.getOriginalFilename()))
                        .toString()
                        .replace("\\", "/");

                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .contentType(file.getContentType())
                        .build();
                s3.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
                uploadedUrls.add(String.format(S3_URL_PREFIX, bucket) + fileName);
            }catch(S3Exception e) {
                throw new RuntimeException("S3 업로드 실패: " + e.awsErrorDetails().errorMessage());
            }catch(IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류 발생: ", e);
            }
        }

        return uploadedUrls;
    }

    /**
     * url을 통해 s3 서버의 이미지를 삭제함
     * @param urls 삭제할 이미지 링크
     */
    public void deleteList(List<String> urls){
        if (urls == null || urls.isEmpty()) return;

        for(String url : urls){
            String key = extractKeyFromUrl(url);
            s3.deleteObject(builder -> builder.bucket(bucket).key(key));
        }
    }

    /**
     * s3 클라이언트 객체 초기화
     */
    @PostConstruct
    private void initializeS3(){
        this.s3= S3Client.builder()
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                )).build();
    }

    /**
     * 파일의 이름을 [난수.데이터형식]으로 생성하여 반환
     * @param fileName 원본 파일 이름. 이름 끝에서 데이터 형식을 추출해오기 위해 사용
     * @return 생성한 파일 이름
     */
    public String generateUniqueFileName(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 파일명입니다: " + fileName);
        }
        String ext = fileName.substring(fileName.lastIndexOf("."));
        return UUID.randomUUID() + ext;
    }

    /**
     * 전체 URL에서 버킷 도메인 제거하여 key만 추출
     * s3 서버에서 파일 삭제를 위해 사용됨
     */
    private String extractKeyFromUrl(String url) {
        return url.replace(String.format(S3_URL_PREFIX, bucket), "");
    }
}
