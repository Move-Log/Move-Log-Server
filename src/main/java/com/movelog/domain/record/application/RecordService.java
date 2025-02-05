package com.movelog.domain.record.application;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import com.movelog.domain.record.domain.VerbType;
import com.movelog.domain.record.dto.request.CreateRecordReq;
import com.movelog.domain.record.dto.response.*;
import com.movelog.domain.record.domain.repository.KeywordRepository;
import com.movelog.domain.record.domain.repository.RecordRepository;
import com.movelog.domain.user.application.UserService;
import com.movelog.domain.user.domain.User;
import com.movelog.domain.user.domain.repository.UserRepository;
import com.movelog.domain.user.exception.UserNotFoundException;
import com.movelog.global.config.security.token.UserPrincipal;
import com.movelog.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Collator;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecordService {
    private final RecordRepository recordRepository;
    private final UserService userService;

    private final KeywordRepository keywordRepository;
    private final S3Util s3Util;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;


    private static final String DAILY_PREFIX = "stats:daily:";
    private static final String WEEKLY_PREFIX = "stats:weekly:";
    private static final String MONTHLY_PREFIX = "stats:monthly:";
    private static final String TOTAL_PREFIX = "stats:total";


    @Transactional
    public void createRecord(UserPrincipal userPrincipal, CreateRecordReq createRecordReq, MultipartFile img) {
        // User user = validUserById(userPrincipal);
        User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);
        validateCreateRecordReq(createRecordReq);

        String recordImgUrl;
        if(img != null){
            recordImgUrl = s3Util.uploadToRecordFolder(img);
            log.info("recordImgUrl: {}", recordImgUrl);
        }
        else{
            recordImgUrl = null;
        }

        String verb = createRecordReq.getVerbType();
        try {
            VerbType verbType = VerbType.fromValue(verb);
            String noun = createRecordReq.getNoun();

            Keyword keyword;

            // 사용자의 키워드에 존재하지 않는 경우 (새로 등록)
            if (!keywordRepository.existsByUserAndKeywordAndVerbType(user, noun, verbType)) {
                keyword = Keyword.builder()
                        .user(user)
                        .keyword(noun)
                        .verbType(verbType)
                        .build();

                keywordRepository.save(keyword);
            }
            else{
                keyword = keywordRepository.findByUserAndKeywordAndVerbType(user, noun, verbType);
            }

            Record record = Record.builder()
                    .keyword(keyword)
                    .recordImage(recordImgUrl)
//                .actionTime(LocalDateTime.now())
                    .build();

            recordRepository.save(record);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid verb type: " + verb, e);
        }

    }


    public TodayRecordStatus retrieveTodayRecord(UserPrincipal userPrincipal) {
        // 유저 유효성 검사 및 조회
        User user = validUserById(userPrincipal);

        // 오늘의 시작 시간과 끝 시간 계산
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // 유저가 소유한 키워드 가져오기
        List<Keyword> keywords = keywordRepository.findByUser(user);

        // 키워드에 연결된 기록 중 오늘 생성된 기록 가져오기
        List<Record> records = recordRepository.findByKeywordInAndActionTimeBetween(keywords, startOfDay, endOfDay);

        log.info("Retrieved Records: {}", records);

        // Keyword에서 VerbType 추출
        Set<VerbType> todayVerbTypes = records.stream()
                .map(Record::getKeyword)      // Record -> Keyword
                .map(Keyword::getVerbType)   // Keyword -> VerbType
                .collect(Collectors.toSet()); // 중복 제거

        log.info("Today VerbTypes: {}", todayVerbTypes);

        // 모든 VerbType에 대해 존재 여부를 반환
        TodayRecordStatus todayRecordStatus = TodayRecordStatus.builder()
                .isDo(verbTypeExists(todayVerbTypes, VerbType.DO))
                .isEat(verbTypeExists(todayVerbTypes, VerbType.EAT))
                .isGo(verbTypeExists(todayVerbTypes, VerbType.GO))
                        .build();

        return todayRecordStatus;

    }

    public List<RecentRecordImagesRes> retrieveRecentRecordImages(UserPrincipal userPrincipal, Long keywordId) {
        User user = validUserById(userPrincipal);
        // User user = validUserById(5L);
        Keyword keyword = validKeywordById(keywordId);
        List<Record> records = recordRepository.findTop5ByKeywordOrderByActionTimeDesc(keyword);

        return records.stream()
                .map(record -> RecentRecordImagesRes.builder()
                        .imageUrl(record.getRecordImage())
                        .createdAt(record.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

    }


    public List<SearchKeywordRes> searchKeyword(UserPrincipal userPrincipal, String keyword) {
        User user = validUserById(userPrincipal);
        // User user = validUserById(5L);
        List<Keyword> keywords = keywordRepository.findAllByUserAndKeywordContaining(user, keyword);
        keywords.forEach(k -> log.info("Keyword in DB: {}", k.getKeyword()));

        // Collator 생성
        Collator collator = Collator.getInstance(Locale.KOREA);

        // Collator를 이용한 오름차순 정렬
        List<SearchKeywordRes> sortedResults = keywords.stream()
                .map(k -> SearchKeywordRes.builder()
                        .keywordId(k.getKeywordId())
                        .noun(k.getKeyword())
                        .verb(VerbType.getStringVerbType(k.getVerbType()))
                        .build())
                .sorted((o1, o2) -> collator.compare(o1.getNoun(), o2.getNoun())) // Collator로 비교
                .collect(Collectors.toList());

        // 정렬된 명사 목록 출력
        sortedResults.forEach(r -> log.info("Sorted Noun: {}", r.getNoun()));

        return sortedResults;
    }

    public Page<RecordCalendarRes> getRecordByDate(UserPrincipal userPrincipal, String date, Integer page) {
        User user = validUserById(userPrincipal);
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);
        LocalDateTime start = LocalDateTime.parse(date + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(date + "T23:59:59");

        Pageable pageable = PageRequest.of(0, 15); // 원하는 페이지와 크기를 지정
        Page<Record> recordList = recordRepository.findRecordByUserAndCreatedAtBetween(user, start, end, pageable);

        return recordList.map(record -> RecordCalendarRes.builder()
                .recordId(record.getRecordId())
                .recordImageUrl(record.getRecordImage())
                .noun(record.getKeyword().getKeyword())
                .verb(VerbType.getStringVerbType(record.getKeyword().getVerbType()))
                .createdAt(record.getCreatedAt())
                .build());

    }

    public List<Recent5RecordImagesRes> retrieveCurrentRecordImages(UserPrincipal userPrincipal) {
        User user = validUserById(userPrincipal);
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);

        List<Record> records = recordRepository.findTop5ByKeywordUserAndRecordImageNotNullOrderByActionTimeDesc(user);

        return records.stream()
                .map(record -> Recent5RecordImagesRes.builder()
                        .imageUrl((record.getRecordImage()))
                        .build())
                .toList();
    }

    /**
     * 전체 사용자 기록 통계 조회 (월별 조회 및 일간 조회 개선)
     */
    public AllUserRecordStatsRes getAllUserRecordStats(UserPrincipal userPrincipal, String category, String period, String month) {
        validUserById(userPrincipal);
        String redisKey = getRedisKey(period);

        // 총 기록 횟수 조회
        int totalRecords = getTotalRecords(redisKey, category);

        // 최고 연속 기록 조회
        int maxConsecutiveDays = getMaxConsecutiveDays(category);

        // 평균 일간 기록 계산
        double avgDailyRecord = calculateAvgDailyRecord(category);

        // 하루 동안 가장 많이 기록한 횟수 조회
        int maxDailyRecord = getMaxDailyRecord(redisKey, category);

        // TOP 5 키워드 조회
        List<Map<String, Object>> topRecords = getTopRecords(category);

        // 날짜별 기록 개수 조회 (달력 표시용, 월별인지 확인 후 호출)
        Map<LocalDate, Integer> dailyRecords = "monthly".equals(period) ?
                getMonthlyRecords(category, month) :
                getDailyRecords(category);

        return AllUserRecordStatsRes.builder()
                .category(category)
                .totalRecords(totalRecords)
                .maxConsecutiveDays(maxConsecutiveDays)
                .avgDailyRecord(avgDailyRecord)
                .maxDailyRecord(maxDailyRecord)
                .topRecords(topRecords)
                .dailyRecords(dailyRecords)
                .build();
    }


    /**
     * Redis Key 생성
     */
    private String getRedisKey(String period) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String week = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-ww"));
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return switch (period) {
            case "daily" -> DAILY_PREFIX + today;
            case "weekly" -> WEEKLY_PREFIX + week;
            case "monthly" -> MONTHLY_PREFIX + month;
            default -> TOTAL_PREFIX;
        };
    }

    /**
     * 총 기록 횟수 조회 (Redis 조회 개선)
     */
    private int getTotalRecords(String redisKey, String category) {
        Set<String> records = redisTemplate.opsForZSet().reverseRange(redisKey, 0, -1);

        // Redis에서 데이터가 없을 경우 DB 데이터 기반으로 계산
        if (records == null || records.isEmpty()) {
            System.out.println("⚠️ Redis is empty for key: " + redisKey + " → Fetching from DB");

            // DB에서 전체 기록 횟수 계산
            List<Record> recordsFromDb = recordRepository.findAllWithKeyword();
            return (int) recordsFromDb.stream()
                    .filter(record -> record.getKeyword() != null && record.getKeyword().getVerbType().getVerbType().equals(category))
                    .count();
        }

        return records.size();
    }


    /**
     * 최대 연속 기록일 계산
     */
    private int getMaxConsecutiveDays(String category) {
        VerbType verbType = VerbType.fromValue(category);
        List<Record> records = recordRepository.findAllWithKeyword();

        // `VerbType`에 맞는 데이터만 필터링하여 날짜만 추출
        List<LocalDate> recordDates = records.stream()
                .filter(record -> record.getKeyword() != null && record.getKeyword().getVerbType() == verbType)
                .map(record -> record.getActionTime().toLocalDate())
                .distinct() // 중복 제거
                .sorted() // 날짜순 정렬
                .collect(Collectors.toList());

        if (recordDates.isEmpty()) return 0; // 기록이 없으면 0 반환

        int maxStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < recordDates.size(); i++) {
            if (recordDates.get(i).equals(recordDates.get(i - 1).plusDays(1))) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return maxStreak;
    }


    /**
     * 평균 일간 기록 계산 (DB 데이터 기반)
     */
    private double calculateAvgDailyRecord(String category) {
        Map<LocalDate, Integer> dailyRecords = getDailyRecords(category); // 날짜별 기록 개수 조회

        if (dailyRecords.isEmpty()) return 0.0; // 기록이 없으면 0 반환

        int totalRecords = dailyRecords.values().stream().mapToInt(Integer::intValue).sum();
        int totalDays = dailyRecords.size();

        // 반올림
        return Math.round((double) totalRecords / totalDays * 10) / 10.0;

    }


    /**
     * 하루 동안 가장 많이 기록한 횟수 조회 (Redis + DB 조회)
     */
    private int getMaxDailyRecord(String redisKey, String category) {
        Set<String> records = redisTemplate.opsForZSet().reverseRange(redisKey, 0, -1);

        if (records == null || records.isEmpty()) {
            System.out.println("⚠️ Redis is empty for key: " + redisKey + " → Fetching from DB");

            // DB에서 날짜별 기록 개수 가져와서 최대값 찾기
            Map<LocalDate, Integer> dailyRecords = getDailyRecords(category);
            return dailyRecords.values().stream().max(Integer::compareTo).orElse(0);
        }

        return records.size();
    }


    /**
     * TOP 5 키워드 조회 (모든 데이터 가져온 후 서비스 단에서 필터링)
     */
    private List<Map<String, Object>> getTopRecords(String category) {
        // 한글 문자열을 VerbType Enum으로 변환
        VerbType verbType = VerbType.fromValue(category);

        // 모든 데이터 한 번에 조회
        List<Record> records = recordRepository.findAllWithKeyword();
        Map<String, Integer> keywordCounts = new HashMap<>();

        for (Record record : records) {
            if (record.getKeyword() != null && record.getKeyword().getVerbType() == verbType) {
                String keyword = record.getKeyword().getKeyword();
                keywordCounts.put(keyword, keywordCounts.getOrDefault(keyword, 0) + 1);
            }
        }

        return keywordCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // 내림차순 정렬
                .limit(5)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("record", entry.getKey());
                    map.put("count", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }


    /**
     * 날짜별 기록 개수 조회 (평균 일간 기록 정보 조회를 위함)
     */
    private Map<LocalDate, Integer> getDailyRecords(String category) {
        // 한글 문자열을 VerbType Enum으로 변환
        VerbType verbType = VerbType.fromValue(category);

        // 모든 데이터를 조회 후, 서비스 단에서 필터링
        List<Record> records = recordRepository.findAllWithKeyword();
        Map<LocalDate, Integer> dailyRecordCount = new HashMap<>();

        // `VerbType`에 맞는 데이터만 필터링하여 날짜별 개수 카운트
        for (Record record : records) {
            if (record.getKeyword() != null && record.getKeyword().getVerbType() == verbType) {
                LocalDate date = record.getActionTime().toLocalDate();
                dailyRecordCount.put(date, dailyRecordCount.getOrDefault(date, 0) + 1);
            }
        }
        return dailyRecordCount;
    }

    private Map<LocalDate, Integer> getMonthlyRecords(String category, String month) {
        VerbType verbType = VerbType.fromValue(category);
        Map<LocalDate, Integer> dailyRecordCount = new LinkedHashMap<>();

        if (month == null || month.isBlank()) {
            log.info("⚠️ Invalid month input: [{}]", month);
            return dailyRecordCount;
        }

        // month 값 정리: 공백 제거 + 숫자와 '-'만 유지
        month = month.trim().replaceAll("[^0-9-]", "");

        // 🕒 월 시작일 & 종료일 설정
        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay(); // ex) 2025-02-01 00:00:00
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59); // ex) 2025-02-28 23:59:59

        log.info("🔍 Fetching records for category: [{}] between [{}] and [{}]", category, startDate, endDate);

        // 📌 DB에서 해당 월의 데이터만 필터링하여 조회
        List<Record> records = recordRepository.findRecordsByMonth(verbType, startDate, endDate);
        log.info("📊 Retrieved {} records from DB", records.size());

        if (records.isEmpty()) {
            log.info("⚠️ No records found for category: {}", category);
            return dailyRecordCount;
        }

        // 📅 날짜별 개수 카운트 (타임존 변환 추가)
        for (Record record : records) {
            log.info("⏳ Raw ActionTime: {}", record.getActionTime());
            ZonedDateTime zonedDateTime = record.getActionTime().atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.of("Asia/Seoul"));
            LocalDate date = zonedDateTime.toLocalDate();
            log.info("📅 Converted LocalDate: {}", date);

            dailyRecordCount.put(date, dailyRecordCount.getOrDefault(date, 0) + 1);
        }

        log.info("✅ Final Monthly Records: {}", dailyRecordCount);

        return dailyRecordCount;
    }



    private User validUserById(UserPrincipal userPrincipal) {
        Optional<User> userOptional = userService.findById(userPrincipal.getId());
        if (userOptional.isEmpty()) { throw new UserNotFoundException(); }
        return userOptional.get();
    }

    private Keyword validKeywordById(Long keywordId) {
        Optional<Keyword> keywordOptional = keywordRepository.findById(keywordId);
        return keywordOptional.get();
    }

    private boolean verbTypeExists(Set<VerbType> todayVerbTypes, VerbType verbType) {
        return todayVerbTypes.contains(verbType);
    }

    private void validateCreateRecordReq(CreateRecordReq createRecordReq) {
        if (createRecordReq.getVerbType() == null || createRecordReq.getVerbType().isEmpty()) {
            throw new IllegalArgumentException("verbType is required.");
        }
        if (createRecordReq.getNoun() == null || createRecordReq.getNoun().isEmpty()) {
            throw new IllegalArgumentException("noun is required.");
        }
    }


}
