package com.portiony.portiony.service;

import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostImage;
import com.portiony.portiony.repository.PostImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostImageService {
    private final PostImageRepository postImageRepository;

    /**
     * DB에 이미지 메타 데이터(s3 서버에 업로드한 파일 URL 정보, 관련 게시글) 저장
     * @param urls s3 서버에 업로드한 파일 링크
     * @param post 연관된 게시글
     */
    public void saveNewPostImages(List<String> urls, Post post) {
        List<PostImage> images = new ArrayList<>();

        for (int i = 0; i < urls.size(); i++) {
            images.add(new PostImage(urls.get(i), i, post));
        }

        postImageRepository.saveAll(images);
    }

    /**
     * DB에서 이미지 메타 데이터 삭제
     * @param post 이미지를 삭제할 게시글 객체
     */
    public void deletePostImagesByPost(Post post) {
        List<String> urls = postImageRepository.findAllByPostId(post.getId())
                .stream()
                .map(PostImage::getImageUrl)
                .toList();
        postImageRepository.deleteAllByImageUrlIn(urls);
    }

    //TODO : 이미지 수정


}
