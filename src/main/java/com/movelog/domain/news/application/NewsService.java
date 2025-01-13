package com.movelog.domain.news.application;

import com.movelog.domain.news.dto.request.NewsHeadLineReq;
import com.movelog.domain.news.dto.response.HeadLineRes;
import com.movelog.domain.user.application.UserService;
import com.movelog.domain.user.domain.User;
import com.movelog.domain.user.domain.repository.UserRepository;
import com.movelog.domain.user.exception.UserNotFoundException;
import com.movelog.global.DefaultAssert;
import com.movelog.global.config.security.token.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {
    private final HeadlineGeneratorService headlineGeneratorService;
    private final UserService userService;
    private final UserRepository userRepository;

    public List<HeadLineRes> createHeadLine(UserPrincipal userPrincipal, NewsHeadLineReq newsHeadLineReq) {
        User user = validateUser(userPrincipal);
        // id가 5인 유저 정보(테스트용)
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);
        String option = newsHeadLineReq.getOption();
        String verb = newsHeadLineReq.getVerb();
        String noun = newsHeadLineReq.getNoun();
        return headlineGeneratorService.generateHeadLine(option, verb, noun);
    }


    private User validateUser(UserPrincipal userPrincipal) {
        Optional<User> userOptional = userService.findById(userPrincipal.getId());
        DefaultAssert.isOptionalPresent(userOptional);
        return userOptional.get();
    }
}
