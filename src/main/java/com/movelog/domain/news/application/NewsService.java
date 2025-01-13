package com.movelog.domain.news.application;

import com.movelog.domain.news.domain.News;
import com.movelog.domain.news.domain.repository.NewsRepository;
import com.movelog.domain.news.dto.request.CreateNewsReq;
import com.movelog.domain.news.dto.request.NewsHeadLineReq;
import com.movelog.domain.news.dto.response.HeadLineRes;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {
    private final HeadLineGeneratorService headLineGeneratorService;
    private final UserService userService;
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
