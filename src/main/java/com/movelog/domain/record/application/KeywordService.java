package com.movelog.domain.record.application;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.dto.response.SearchKeywordInStatsRes;
import com.movelog.domain.record.exception.KeywordNotFoundException;
import com.movelog.domain.record.domain.repository.KeywordRepository;
import com.movelog.domain.user.application.UserService;
import com.movelog.domain.user.domain.User;
import com.movelog.domain.user.exception.UserNotFoundException;
import com.movelog.global.config.security.token.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class KeywordService {

    private final UserService userService;
    private final KeywordRepository keywordRepository;

    public List<SearchKeywordInStatsRes> searchKeywordInStats(UserPrincipal userPrincipal, String keyword) {
        User user = validUserById(userPrincipal);
        // 검색어를 포함한 키워드 리스트 조회
        List<Keyword> keywords = keywordRepository.findAllByUserAndKeywordContaining(user, keyword);

        // 기록이 많은 순서대로 정렬
        keywords = sortKeywordByRecordCount(keywords);

        return keywords.stream()
                .map(keyword1 -> SearchKeywordInStatsRes.builder()
                        .keywordId(keyword1.getKeywordId())
                        .noun(keyword1.getKeyword())
                        .build())
                .toList();

    }


    // 키워드 내 기록 개수를 반환
    private int keywordRecordCount(Long keywordId){
        Keyword keyword = validKeywordById(keywordId);
        return keyword.getRecords().size();
    }

    // 키워드 내 기록이 많은 순서대로 정렬
    private List<Keyword> sortKeywordByRecordCount(List<Keyword> keywords) {
        return keywords.stream()
                .sorted((k1, k2) -> keywordRecordCount(k2.getKeywordId()) - keywordRecordCount(k1.getKeywordId()))
                .toList();
    }

    private User validUserById(UserPrincipal userPrincipal) {
        Optional<User> userOptional = userService.findById(userPrincipal.getId());
        if (userOptional.isEmpty()) { throw new UserNotFoundException(); }
        return userOptional.get();
    }

    private Keyword validKeywordById(Long keywordId) {
        Optional<Keyword> keywordOptional = keywordRepository.findById(keywordId);
        if(keywordOptional.isEmpty()) { throw new KeywordNotFoundException(); }
        return keywordOptional.get();
    }

}
