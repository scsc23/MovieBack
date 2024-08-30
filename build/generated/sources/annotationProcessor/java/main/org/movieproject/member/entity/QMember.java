package org.movieproject.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -1811445460L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMember member = new QMember("member1");

    public final org.movieproject.upload.entity.QImage image;

    public final ListPath<org.movieproject.likes.entity.Likes, org.movieproject.likes.entity.QLikes> likes = this.<org.movieproject.likes.entity.Likes, org.movieproject.likes.entity.QLikes>createList("likes", org.movieproject.likes.entity.Likes.class, org.movieproject.likes.entity.QLikes.class, PathInits.DIRECT2);

    public final StringPath memberEmail = createString("memberEmail");

    public final StringPath memberName = createString("memberName");

    public final StringPath memberNick = createString("memberNick");

    public final NumberPath<Integer> memberNo = createNumber("memberNo", Integer.class);

    public final StringPath memberPhone = createString("memberPhone");

    public final StringPath memberPw = createString("memberPw");

    public final ListPath<org.movieproject.post.entity.Post, org.movieproject.post.entity.QPost> posts = this.<org.movieproject.post.entity.Post, org.movieproject.post.entity.QPost>createList("posts", org.movieproject.post.entity.Post.class, org.movieproject.post.entity.QPost.class, PathInits.DIRECT2);

    public final SetPath<Role, EnumPath<Role>> roleSet = this.<Role, EnumPath<Role>>createSet("roleSet", Role.class, EnumPath.class, PathInits.DIRECT2);

    public final BooleanPath social = createBoolean("social");

    public QMember(String variable) {
        this(Member.class, forVariable(variable), INITS);
    }

    public QMember(Path<? extends Member> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMember(PathMetadata metadata, PathInits inits) {
        this(Member.class, metadata, inits);
    }

    public QMember(Class<? extends Member> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.image = inits.isInitialized("image") ? new org.movieproject.upload.entity.QImage(forProperty("image"), inits.get("image")) : null;
    }

}

