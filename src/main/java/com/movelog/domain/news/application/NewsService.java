package com.movelog.domain.news.application;

import com.movelog.domain.news.domain.News;
import com.movelog.domain.news.domain.repository.NewsRepository;
import com.movelog.domain.news.dto.request.CreateNewsReq;
import com.movelog.domain.news.dto.request.NewsHeadLineReq;
import com.movelog.domain.news.dto.response.*;
import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.VerbType;
import com.movelog.domain.record.exception.KeywordNotFoundException;
import com.movelog.domain.record.repository.KeywordRepository;
import com.movelog.domain.user.application.UserService;
import com.movelog.domain.user.domain.User;
import com.movelog.domain.user.domain.repository.UserRepository;
import com.movelog.domain.user.exception.UserNotFoundException;
import com.movelog.global.config.security.token.UserPrincipal;
import com.movelog.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {
    private final HeadLineGeneratorService headLineGeneratorService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final KeywordRepository keywordRepository;
    private final NewsRepository newsRepository;
    private final S3Util s3Util;

    public List<HeadLineRes> createHeadLine(UserPrincipal userPrincipal, Long keywordId, NewsHeadLineReq newsHeadLineReq) {
        User user = validateUser(userPrincipal);
        // id가 5인 유저 정보(테스트용)
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);
        Keyword keyword = validateKeyword(keywordId);
        String option = newsHeadLineReq.getOption();
        String verb = VerbType.getStringVerbType(keyword.getVerbType());
        String noun = keyword.getKeyword();

        return headLineGeneratorService.generateHeadLine(option, verb, noun);
    }

    @Transactional
    public void createNews(UserPrincipal userPrincipal, Long keywordId, CreateNewsReq createNewsReq, MultipartFile img) {
        User user = validateUser(userPrincipal);
        // id가 5인 유저 정보(테스트용)
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);
        Keyword keyword = validateKeyword(keywordId);

        String newsImgUrl = s3Util.uploadToNewsFolder(img);

        News news = News.builder()
                .headLine(createNewsReq.getHeadLine())
                .newsUrl(newsImgUrl)
                .keyword(keyword)
                .build();

        newsRepository.save(news);

    }

    public List<RecentKeywordsRes> getRecentKeywords(UserPrincipal userPrincipal) {
        User user = validateUser(userPrincipal);
        // id가 5인 유저 정보(테스트용)
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);
        List<Keyword> recentKeywords = keywordRepository.findTop5ByUserOrderByCreatedAtDesc(user);

        return recentKeywords.stream()
                .map(keyword -> RecentKeywordsRes.builder()
                        .keywordId(keyword.getKeywordId())
                        .verb(VerbType.getStringVerbType(keyword.getVerbType()))
                        .noun(keyword.getKeyword())
                        .build())
                .toList();
    }

    public List<RecentNewsRes> getRecentNews(UserPrincipal userPrincipal, Integer page) {
        User user = validateUser(userPrincipal);
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);

        //페이징 객체 생성
        Pageable pageable = PageRequest.of(page, 15);

        // 최근 일주일간 생성한 뉴스 목록 조회
        LocalDateTime createdAt = LocalDateTime.now().minusDays(7);
        List<News> recentNews = newsRepository.findRecentNewsByUser(user, createdAt, pageable);

        // 최신순 정렬
        recentNews.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));

        return recentNews.stream()
                .map(news -> RecentNewsRes.builder()
                        .newsId(news.getNewsId())
                        .newsImageUrl(news.getNewsUrl())
                        .headLine(news.getHeadLine())
                        .noun(news.getKeyword().getKeyword())
                        .verb(VerbType.getStringVerbType(news.getKeyword().getVerbType()))
                        .createdAt(news.getCreatedAt())
                        .build())
                .toList();
    }

    public TodayNewsStatusRes getTodayNewsStatus(UserPrincipal userPrincipal) {
        User user = validateUser(userPrincipal);
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);

        // 사용자가 생성한 모든 뉴스 개수 조회
        List<Keyword> keywords = user.getKeywords();
        long totalNewsCount = keywords.stream()
                .mapToLong(newsRepository::countByKeyword)
                .sum();

        long newsStatus = totalNewsCount % 5;
        LocalDateTime today = LocalDateTime.now();

        // 오늘 생성한 뉴스가 있으면 true, 없으면 false
        boolean isTodayNews = !newsRepository.findRecentNewsByUser(user, today, PageRequest.of(0, 1)).isEmpty();

        int result;
        if(newsStatus == 0 && isTodayNews) {
            result = 5;
        }
        else if(newsStatus == 0) {
            result = 0;
        }
        else {
            result = (int) newsStatus;
        }

        return TodayNewsStatusRes.builder()
                .newsStatus(result)
                .build();
    }

    public List<NewsCalendarRes> getNewsByDate(UserPrincipal userPrincipal, String date) {
        User user = validateUser(userPrincipal);
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);

        LocalDateTime start = LocalDateTime.parse(date + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(date + "T23:59:59");

        List<News> newsList = newsRepository.findNewsByUserAndCreatedAtBetween(user, start, end);

        return newsList.stream()
                .map(news -> NewsCalendarRes.builder()
                        .newsId(news.getNewsId())
                        .newsImageUrl(news.getNewsUrl())
                        .noun(news.getKeyword().getKeyword())
                        .verb(VerbType.getStringVerbType(news.getKeyword().getVerbType()))
                        .createdAt(news.getCreatedAt())
                        .build())
                .toList();
    }

    // User 정보 검증
    private User validateUser(UserPrincipal userPrincipal) {
        Optional<User> userOptional = userService.findById(userPrincipal.getId());
        if (userOptional.isEmpty()) { throw new UserNotFoundException(); }
        return userOptional.get();
    }

    // Keyword 정보 검증
    private Keyword validateKeyword(Long keywordId) {
        Optional<Keyword> keywordOptional = keywordRepository.findById(keywordId);
        if (keywordOptional.isEmpty()) { throw new KeywordNotFoundException(); }
        return keywordOptional.get();
    }

}
