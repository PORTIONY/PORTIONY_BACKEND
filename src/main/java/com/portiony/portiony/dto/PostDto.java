//임시 PostDto

package com.portiony.portiony.dto;

import com.portiony.portiony.entity.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostDto {

    private Long id;
    private String title;
    private String description;
    private String categoryTitle;
    private String writerNickname;

    @Builder
    public PostDto(Long id, String title, String description, String categoryTitle, String writerNickname) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryTitle = categoryTitle;
        this.writerNickname = writerNickname;
    }

    public static PostDto from(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .categoryTitle(post.getCategory().getTitle())
                .writerNickname(post.getUser().getNickname())
                .build();
    }
}
