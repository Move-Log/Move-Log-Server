package com.movelog.domain.news.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.movelog.domain.gpt.application.GptService;
import com.movelog.domain.news.dto.response.HeadLineRes;
import com.movelog.domain.record.application.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeadLineGeneratorService {

    private final GptService gptService;
    private final RecordService recordService;

    public List<HeadLineRes> generateHeadLine(Long userId, String option, String verb, String noun) {

        // 프롬프트 생성
        String prompt = generatePrompt(userId, option, verb, noun);

        // 비동기 방식으로 GPT API 호출
        return gptService.callChatGpt(prompt)
                .flatMap(response -> {
                    if (response == null) {
                        return Mono.error(new RuntimeException("GPT API 응답이 없습니다."));
                    }

                    // 응답 파싱하여 List<HeadLineRes> 반환
                    List<HeadLineRes> practiceResList = parseResponse(response).stream()
                            .map(headLine -> new HeadLineRes(headLine.getHeadLine().trim())) // 앞뒤 공백 제거
                            .toList();

                    return Mono.just(practiceResList);
                })
                .doOnError(error -> log.error("문제 생성 중 오류 발생: ", error))
                .block();
    }


    // Base 프롬프트
    private String generatePrompt(Long userId, String option, String verb, String noun) {
        String optionPrompt = generateOptionPrompt(userId, option, verb, noun); // 옵션별 프롬프트 가져오기

        return String.format(
                "다음 내용을 바탕으로 뉴스 헤드라인을 생성해주세요:\n" +
                        "당신은 뉴스 헤드라인을 작성하는 역할이며, 다음 형식을 엄격히 따라 뉴스 헤드라인을 제공해 주세요:\n\n" +
                        "이 뉴스 헤드라인의 목적은 사용자의 기록을 기반으로 약간의 재미 요소가 들어간 헤드라인을 제공하는 것입니다.\n" +
                        "각 뉴스 헤드라인은 뉴스 헤드라인 스타일이지만 약간의 재미 요소와 어그로성을 포함해야 합니다.\n" +
                        "각 뉴스 헤드라인은 반드시 격식있는 어체로 구성되어야 하며, 반드시 올바른 어법을 준수해야 합니다.\n" +
                        "각 헤드라인의 내용은 주어진 옵션, 동사, 명사를 기반으로 생성되어야 하며, 주어지는 옵션에 따라 헤드라인이 생성됩니다.\n" +
                        "각 헤드라인의 내용에는 주어진 명사에 대한 정보가 포함되어야 하며, 동사에 대한 내용은 필수가 아닙니다.\n" +
                        "뉴스 헤드라인은 반드시 3개의 후보를 제공해야 하며, 각 후보는 1개의 줄바꿈으로 구분됩니다.\n" +
                        "각 헤드라인 내용의 구조는 반드시 쉼표로 나뉘어야 하며, 쉼표 이전의 내용에는 헤드라인 강조 옵션에 대한 내용이 포함되어야 합니다." +
                        "쉼표 앞 뒤의 내용에 대한 글자 수는 쉼표를 포함하여 각각 16자 이하여야 하며, 쉼표는 반드시 한글자로만 구성되어야 합니다.\n\n" +

                        "다음은 제공된 옵션 정보를 통해 출력해야 하는 뉴스 헤드라인의 출력 요청사항입니다:\n\n" +
                        "%s\n\n" + // 옵션별 프롬프트

                        "다음은 제공된 정보를 통해 출력해야 하는 뉴스 헤드라인의 출력 형식입니다:\n" +
                        "첫번째 헤드라인 후보 내용\n두번째 헤드라인 후보 내용\n세번째 헤드라인 후보 내용\n" +

                        "문장 처음과 끝에는 불필요한 공백이 없어야 합니다.\n" +
                        "반드시 뉴스 헤드라인 형식을 준수해 주세요. 정해진 형식을 따르지 않으면 응답을 처리할 수 없습니다." +
                        "만약 결과 헤드라인 내용이 위 형식과 다르다면, 다시 요청하여 주세요.",
                optionPrompt
        );
    }

    //첫 도전, 오랜만에 다시, 꾸준히 이어온 기록, 끊어낸 습관
    // 옵션별 프롬프트 메소드 분리
    private String generateOptionPrompt(Long userId, String option, String verb,String noun) {
        if ("첫 도전".equals(option)) {
            return generateFirstAttemptPrompt(noun, verb);
        } else if ("오랜만에 다시".equals(option)) {
            int option_count = recordService.calculateAfterLongOptionCount(userId, verb, noun);
            return generateAfterLongTimePrompt(noun, verb, option_count);
        } else if ("끊어낸 습관".equals(option)) {
            int option_count = recordService.calculateBreakingHabitOptionCount(userId, verb, noun);
            return generateBreakingHabitPrompt(noun, verb, option_count);
        } else if ("꾸준히 이어온 기록".equals(option)) {
            int option_count = recordService.calculateStreakRecordOptionCount(userId, verb, noun);
            return generateStreakRecordPrompt(noun, verb, option_count);
        } else {
            return "기본 뉴스 헤드라인을 생성합니다.";
        }
    }

    // 첫 도전
    private String generateFirstAttemptPrompt(String noun, String verb) {
        return String.format(
                "이 뉴스 헤드라인은 사용자가 처음으로 명사 '%s'를 동사 '%s'한 것에 대한 내용입니다.\n" +
                " '처음', '생애 첫', '내 인생 첫', '첫' 과 같은 표현 중에 하나를 적절하게 사용하여 사용자의 첫 도전을 강조하는 헤드라인을 작성해 주세요.\n\n'" +
                "각 동사에 대한 아래 예시는 각 줄마다 하나의 예시를 나타냅니다.\n\n" +
                "동사 '했어요'에 대한 예시는 다음과 같습니다.\n" +
                        "생애 첫 클라이밍 도전, 온 몸이 아파\n 처음으로 바이올린 연주, 이게 음악인가\n평생 처음 도자기 빚다, 흥미로워\n첫 러닝 도전, 내 무릎 괜찮을까?\n\n" +

                "동사 '갔어요'에 대한 예시는 다음과 같습니다.\n" +
                        "처음으로 제주도 상륙!, 혼자옵서예\n생애 첫 콘서트 입성!, 즐겨보자\n내 인생 첫 해외여행, 설렘 폭발\n놀이공원 첫 방문!, 도파민 풀충전\n\n" +

                "동사 '먹었어요'에 대한 예시는 다음과 같습니다.\n" +
                        "내 첫 트러플 오일 파스타, 자주 먹을듯\n인생 첫 타코, 과연 내 입맛엔?\n처음 먹어본 마라탕, 혀가 얼얼!\n\n",

                noun, verb
        );
    }

    // 오랜만에 다시
    private String generateAfterLongTimePrompt(String noun, String verb, int option_count) {
        return String.format(
                "이 뉴스 헤드라인은 사용자가 오랜만에 다시 '%d'일 만에 명사 '%s'를 동사 '%s' 한 것에 대한 내용입니다.\n" +
                        "주어진 일수를 기준으로 기간에 대한 표현은 다음과 같이 작성해 주세요.\n" +
                        "다음 기준에 나타난 물음표는 숫자여야 하며, '%d'일을 다음 기준에 맞게 변환해서 헤드라인 내용에 포함되어야야 합니다.\n" +

                        "1일~30일 미만 → “?일 만에”\n" +
                        "1개월~12개월 미만 → “? 개월 만에”\n" +
                        "1년 이상 → “? 년 만에”\n\n" +

                        "'{기간}만에'와 같은 표현을 사용하여 사용자의 오랜만의 다시 한 기록을 강조하는 헤드라인을 작성해 주세요.\n" +
                        "만약 일수가 0이라면, 구체적인 일수를 숫자로 나타내지 않고, '오랜만에'라는 표현으로 대체합니다.\n\n" +
                        "각 동사에 대한 아래 예시는 각 줄마다 하나의 예시를 나타냅니다.\n\n" +

                        "동사 '했어요'에 대한 예시는 다음과 같습니다.\n" +
                        "3년 만에 그림 다시 그려봄, 실력 여전하네\n1주일 만에 런닝, 숨이 차올라\n6개월 만에 축구, 실력 여전하네\n5년 만에 자전거 타다, 넘어질 뻔\n한 달 만에 독서, 마음의 양식 쌓기\n\n" +

                        "동사 '갔어요'에 대한 예시는 다음과 같습니다.\n" +
                        "10년 만에 초등학교 방문!, 모든 것이 작아진 기분\n6개월 만에 헬스장 컴백, 다시 시작한다\n1년 만에 놀이공원, 도파민 풀충전\n1년 만에 온 오사카 맛집, 이거 먹으러 왔다\n\n" +

                        "동사 '먹었어요'에 대한 예시는 다음과 같습니다.\n" +
                        "1주일 만에 엽떡 먹다, 역시 이 맛이야\n2년 만에 마라탕, 역시 맛있어\n6개월 만에 초밥, 감동 그 자체\n1년 만에 대방어, 이것만 기다렸다\n3일 만에 버블티 또 흡입, 버블티 러버\n\n",

                option_count, noun, verb, option_count
        );
    }

    // 끊어낸 습관
    private String generateBreakingHabitPrompt(String noun, String verb, int option_count) {
        return String.format(
                "이 뉴스 헤드라인은 사용자가 '%d'일째 명사 '%s'를 동사 '%s' 하는 습관을 끊어낸 것을 기록한 것에 대한 내용입니다.\n" +
                        "주어진 일수를 기준으로 기간에 대한 표현은 다음과 같이 작성해 주세요.\n" +
                        "다음 기준에 나타난 물음표는 숫자여야 하며, '%d'일을 다음 기준에 맞게 변환해서 헤드라인 내용에 포함되어야야 합니다.\n" +
                        "1일~30일 미만 → “?일 만에”\n" +
                        "1개월~12개월 미만 → “? 개월 만에”\n" +
                        "1년 이상 → “? 년 만에”\n\n" +
                        "'~ 안 한 지' 또는 '없이 + {기간}'와 같은 표현 중 하나를 적절히 사용하여 사용자의 끊어낸 습관에 대한 기록을 강조하는 헤드라인을 작성해 주세요.\n" +
                        "만약 일수가 0이라면, 구체적인 일수를 숫자로 나타내지 않고, '1일차' 또는 '오늘부터 꾸준히'라는 표현으로 대체합니다.\n\n" +
                        "각 동사에 대한 아래 예시는 각 줄마다 하나의 예시를 나타냅니다.\n\n" +

                        "동사 '했어요'에 대한 예시는 다음과 같습니다.\n" +
                        "금연 1주일 차, 아직 괜찮음\n헬스장 10일째 안 가는 중, 너무 귀찮아\n게임 끊고 한 달째, 할만하네\n다이어트 3일 차, 아직 멀었다\n\n" +

                        "동사 '갔어요'에 대한 예시는 다음과 같습니다.\n" +
                        "카페 안 간 지 2개월째, 이게 가능?\nPC방 끊은 지 100일째!, 중독에서 벗어나다\n노래방 안 간 지 10일째, 목소리 굳겠다\n\n" +

                        "동사 '먹었어요'에 대한 예시는 다음과 같습니다.\n" +
                        "디저트 끊은 지 2주 돌파! 심심한 입\n커피 없이 5일째, 피곤한 인생\n라면 안 먹은 지 3개월, 대기록 갱신\n술 안 먹은 지 100일차, 이게 가능?\n\n",

                option_count, noun, verb, option_count
        );
    }

    // 꾸준히 이어온 기록
    private String generateStreakRecordPrompt(String noun, String verb, int option_count) {
        return String.format(
                "이 뉴스 헤드라인은 사용자가 연속으로 '%d'일째 명사 '%s'를 동사 '%s' 하는 것을 기록한 것에 대한 내용입니다.\n" +

                        "'~일 째' 또는 '~일 연속'과 같은 표현을 사용하여 사용자의 연속된 기록을 강조하는 헤드라인을 작성해 주세요.\n" +
                        "만약 일수가 0이라면, 구체적인 일수를 숫자로 나타내지 않고, '1일차' 또는 '오늘부터 꾸준히'라는 표현으로 대체합니다.\n\n" +
                        "각 동사에 대한 아래 예시는 각 줄마다 하나의 예시를 나타냅니다.\n\n" +

                        "동사 '했어요'에 대한 예시는 다음과 같습니다.\n" +
                        "매일 글쓰기 30일째, 이정도면 작가인데?\n7일 연속 아이스크림, 끊을 수 없는 달콤함!\n\n" +

                        "동사 '갔어요'에 대한 예시는 다음과 같습니다.\n" +
                        "연속 3일 산책, 힐링 타임!\n4일 연속 카페, 내 인생의 커피!\n7일 연속 노래방, 가수 데뷔 가나요?\n\n" +

                        "동사 '먹었어요'에 대한 예시는 다음과 같습니다.\n" +
                        "3일째 떡볶이 흡입, 이게 중독인가\n7일 연속 아이스크림, 끊을 수 없는 달콤함!\n\n",

                option_count, noun, verb
        );
    }


    // GPT 응답 파싱
    private List<HeadLineRes> parseResponse(JsonNode response) {
        List<HeadLineRes> headLineResList = new ArrayList<>();

        // GPT 응답에서 질문, 답변, 해설 추출
        String textResponse = response.path("choices").get(0).path("message").path("content").asText().trim();
        log.info("Full Text Response: " + textResponse); // 전체 응답 확인

        String[] headLines = textResponse.split("\n");
        // 헤드라인 생성 결과가 3개가 아닌 경우 예외 처리
        if (headLines.length != 3) {
            throw new RuntimeException("생성된 헤드라인이 3개가 아닙니다.");
        }

        // 헤드라인 후보 파싱 결과를 리스트에 저장 후 응답 리턴
        for (String headLine : headLines) {
            headLineResList.add(HeadLineRes.builder().headLine(headLine).build());
        }

        return headLineResList;
    }
}
