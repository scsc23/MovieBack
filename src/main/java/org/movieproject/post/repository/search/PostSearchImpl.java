package org.movieproject.post.repository.search;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.movieproject.post.entity.Post;
import org.movieproject.post.entity.QPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static org.movieproject.post.entity.QPost.post;

public class PostSearchImpl extends QuerydslRepositorySupport implements PostSearch {
    //  PostSearchImpl :  QuerydslRepositorySupport 상속받고 PostSearch 인터페이스 구현
    public PostSearchImpl() {super(Post.class);}

    @Override
    public Page<Post> search1(Pageable pageable) {

        //  Q도메인 생성
        QPost post = QPost.post;

        //  Query 작성
        JPQLQuery<Post> query = from(post);       //  select ... from posts

        //  BooleanBuilder() 사용
        BooleanBuilder booleanBuilder = new BooleanBuilder();       //   (

        booleanBuilder.or(post.postContent.contains(""));  //  content like

        query.where(booleanBuilder);        // )
        query.where(post.postId.gt(0L));   //  postId > 0

        //  Paging
        this.getQuerydsl().applyPagination(pageable, query);

        List<Post> title = query.fetch();

        long count = query.fetchCount();

        return null;
    }

//    @Override
//    public Page<Post> searchAll(String[] types, String keyword, Pageable pageable) {
//
//        QPost posts = QPost.post;
//
//        JPQLQuery<Post> query = from(post);
//
//        if( ( types != null && types.length > 0 ) && keyword != null) { //  검색 조건 및 키워드가 있는 경우
//            BooleanBuilder booleanBuilder = new BooleanBuilder();   //  (
//
//            for(String type : types) {
//                switch (type) {
////                    case "title":
////                        booleanBuilder.or(posts.postTitle.contains(keyword));
////                        break;
//                    case "content":
//                        booleanBuilder.or(posts.postContent.contains(keyword));
//                        break;
//                    case "writer":
//                        booleanBuilder.or(posts.writer.contains(keyword));
//                        break;
//                }
//            }   //  end for
//            query.where(booleanBuilder);    //  )
//
//        }   //  end if
//
//        //  postId > 0
//        query.where(posts.postId.gt(0L));
//
//        //  paging
//        this.getQuerydsl().applyPagination(pageable, query);
//
//        List<Post> list = query.fetch();
//
//        long count = query.fetchCount();
//
//        //  Page<T> 형식으로 변환 : Page<Posts>
//        //  PageImpl을 통해서 반환 : list - 실제 목록 데이터, pageable, total - 전체 개수)
//        return new PageImpl<>(list, pageable, count);
//    }
}
