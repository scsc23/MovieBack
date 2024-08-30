package org.movieproject.likes.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.movieproject.likes.dto.LikesDTO;
import org.movieproject.likes.entity.Likes;
import org.movieproject.likes.repository.LikesRepository;
import org.movieproject.member.entity.Member;
import org.movieproject.member.repository.MemberRepository;
import org.movieproject.movie.entity.Movie;
import org.movieproject.movie.repository.MovieRepository;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class LikesServiceImpl implements LikesService {

    private final LikesRepository likesRepository;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;

    @Override
    public void toggleLike(LikesDTO likesDTO) {
        Member member = memberRepository.findById(likesDTO.getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));
        Movie movie = movieRepository.findById(likesDTO.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie ID"));

        Likes like = likesRepository.findByMemberAndMovie(member, movie)
                .orElseGet(() -> {
                    Likes newLike = new Likes();
                    newLike.setMember(member);
                    newLike.setMovie(movie);
                    newLike.setLiked(false);
                    return newLike;
                });

        like.setLiked(likesDTO.isLiked());
        likesRepository.save(like);
    }

    @Override
    public boolean getLikeStatus(Integer memberNo, Integer movieId) {
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie ID"));

        return likesRepository.findByMemberAndMovie(member, movie)
                .map(Likes::isLiked)
                .orElse(false);
    }

    @Override
    public Integer getMovieLikesCount(Integer movieId) {
        return likesRepository.countByMovieIdAndLiked(movieId);
    }

//    @Override
//    public Likes addLikes(LikesDTO likesDTO) {
//        Optional<Member> memberOptional = memberRepository.findById(likesDTO.getMemberNo());
//        if (memberOptional.isPresent()) {
//            Member member = memberOptional.get();
//            Likes likes = modelMapper.map(likesDTO, Likes.class);
//            likes.setMember(member);
//            return likesRepository.save(likes);
//        } else {
//            throw new IllegalArgumentException("멤버를 찾을 수 없습니다.");
//        }    }
//
//    @Override
//    public List<Likes> getLikesByMember(int memberNo) {
//        return likesRepository.findByMember_MemberNo(memberNo);
//    }
//
//    @Override
//    public void removeLikesByMember(int memberNo) {
//        List<Likes> likes = likesRepository.findByMember_MemberNo(memberNo);
//        likesRepository.deleteAll(likes);
//    }
}
