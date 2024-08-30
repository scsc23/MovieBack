package org.movieproject.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.movieproject.member.dto.MemberDTO;
import org.movieproject.member.entity.Member;
import org.movieproject.member.repository.MemberRepository;
import org.movieproject.movie.entity.Movie;
import org.movieproject.movie.repository.MovieRepository;
import org.movieproject.post.dto.PageRequestDTO;
import org.movieproject.post.dto.PageResponseDTO;
import org.movieproject.post.dto.PostDTO;
import org.movieproject.post.entity.Post;
import org.movieproject.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final MovieRepository movieRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Value("${org.movieproject.file.path}")
    private String basicImagePath;

    //  게시물 등록 기능
    @Override
    public void regPost(PostDTO postDTO) {
        // Movie 객체를 가져옴
        Movie movie = movieRepository.findMovieByMovieId(postDTO.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie ID"));

        // Member 객체도 가져옴
        Member member = memberRepository.findByMemberNick(postDTO.getMemberNick())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // PostDTO를 Post 객체로 변환
        Post post = modelMapper.map(postDTO, Post.class);

        // Post 객체에 각 객체 세팅
        post.setMovie(movie);
        post.setMember(member);

        // Post 객체 저장
        postRepository.save(post);
    }


    //  게시물 삭제 기능
    @Override
    public void deletePost(Integer postId) {postRepository.deleteById(postId);}

    //  페이징 처리 기능
//    @Override
//    public PageResponseDTO<PostDTO> list(PageRequestDTO pageRequestDTO) {
//
//        String[] types = pageRequestDTO.getTypes();
//        String keyword = pageRequestDTO.getKeyword();
//        Pageable pageable = pageRequestDTO.getPageable("postId");
//        Page<Post> result = postRepository.searchAll(types,keyword,pageable);
//
//        //  변환 Posts -> PostsDTO
//        List<PostDTO> dtoList = result.getContent().stream()
//                .map(post -> modelMapper.map(post, PostDTO.class))
//                .collect(Collectors.toList());
//
//        return PageResponseDTO.<PostDTO>withAll()
//                .pageRequestDTO(pageRequestDTO)
//                .dtoList(dtoList)
//                .total((int) result.getTotalElements())
//                .build();
//    }

    @Override
    public List<PostDTO> getPostByMovieId(Integer movieId) {
        List<Post> posts = postRepository.findPostsByMovieId(movieId);
        return posts.stream()
                .map(post -> {
                    PostDTO postDTO = modelMapper.map(post, PostDTO.class);
                    if (post.getMember() != null) {
                        postDTO.setMemberNo(post.getMember().getMemberNo()); // memberId 설정
                        if (post.getMember().getImage() != null) {
                            postDTO.setFilePath(post.getMember().getImage().getFilePath());
                        } else {
                            postDTO.setFilePath(basicImagePath);
                        }
                    }
                    return postDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Double getAverageRatingByMovieId(Integer movieId) {
        return postRepository.findAverageRatingByMovieId(movieId);
    }

    @Override
    public List<PostDTO> getPostsByMemberNo(Integer memberNo) {
        List<Post> posts = postRepository.findPostsByMemberNo(memberNo);

        return posts.stream()
                .map(post -> {
                    PostDTO postDTO = modelMapper.map(post, PostDTO.class);
                    if (post.getMovie() != null) {
                        postDTO.setMovieId(post.getMovie().getMovieId());
                        postDTO.setMovieTitle(post.getMovie().getMovieTitle());
                    }
                    return postDTO;
                })
                .collect(Collectors.toList());
    }
}