package org.movieproject.post.repository.search;

import org.movieproject.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostSearch {

    //  특정 게시물만 검색
    Page<Post> search1(Pageable pageable);

    //  title, content 내용을 검색
//    Page<Post> searchAll(String[] types, String keyword, Pageable pageable);
}
