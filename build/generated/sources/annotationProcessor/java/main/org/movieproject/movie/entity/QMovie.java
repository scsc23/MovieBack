package org.movieproject.movie.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMovie is a Querydsl query type for Movie
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMovie extends EntityPathBase<Movie> {

    private static final long serialVersionUID = 1917981790L;

    public static final QMovie movie = new QMovie("movie");

    public final ListPath<org.movieproject.likes.entity.Likes, org.movieproject.likes.entity.QLikes> likes = this.<org.movieproject.likes.entity.Likes, org.movieproject.likes.entity.QLikes>createList("likes", org.movieproject.likes.entity.Likes.class, org.movieproject.likes.entity.QLikes.class, PathInits.DIRECT2);

    public final NumberPath<Integer> movieId = createNumber("movieId", Integer.class);

    public final StringPath movieTitle = createString("movieTitle");

    public final ListPath<org.movieproject.post.entity.Post, org.movieproject.post.entity.QPost> post = this.<org.movieproject.post.entity.Post, org.movieproject.post.entity.QPost>createList("post", org.movieproject.post.entity.Post.class, org.movieproject.post.entity.QPost.class, PathInits.DIRECT2);

    public QMovie(String variable) {
        super(Movie.class, forVariable(variable));
    }

    public QMovie(Path<? extends Movie> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMovie(PathMetadata metadata) {
        super(Movie.class, metadata);
    }

}

