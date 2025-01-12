package com.movelog.domain.news.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.movelog.domain.gpt.application.GptService;
import com.movelog.domain.news.dto.response.HeadLineRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeadlineGeneratorService {

    private final GptService gptService;

    public List<HeadLineRes> generateHeadLine(String option, String verb, String noun) {
        // 프롬프트 생성
        String prompt = generatePrompt(option, verb, noun);

        // 비동기 방식으로 GPT API 호출
        return gptService.callChatGpt(prompt)
                .flatMap(response -> {
                    if (response == null) {
                        return Mono.error(new RuntimeException("GPT API 응답이 없습니다."));
                    }

                    // 응답 파싱하여 List<CreatePracticeRes> 반환
                    List<HeadLineRes> practiceResList = parseResponse(response);
                    return Mono.just(practiceResList);
                })
                .doOnError(error -> log.error("문제 생성 중 오류 발생: ", error)).block();
    }


    // 프롬프트 생성
    private String generatePrompt(String option, String verb, String noun) {
        return String.format(
                "다음 내용을 바탕으로 뉴스 헤드라인을 생성해주세요: %s\n" +
                        "당신은 뉴스 헤드라인을 작성하는 역할이며, 다음 형식을 엄격히 따라 뉴스 헤드라인을 제공해 주세요:\n\n" +
                        "이 뉴스 헤드라인의 목적은 사용자의 기록을 기반으로 약간의 재미 요소가 들어간 헤드라인을 제공하는 것입니다.\n" +
                        "각 뉴스 헤드라인은 반드시 격식있는 어체로 구성되어야 하며, 반드시 올바른 어법을 준수해야 합니다.\n" +
                        "각 헤드라인의 내용은 주어진 옵션, 동사, 명사를 기반으로 생성되어야 하며, 헤드라인 내용에 직접적인 내용이 들어가지 않아도 됩니다." +
                        "뉴스 헤드라인은 반드시 3개의 후보를 제공해야 하며, 각 후보는 1개의 줄바꿈으로 구분됩니다.\n" +
                        "각 헤드라인 내용의 구조는 반드시 쉼표로 나뉘어야 하며, 쉼표 이전의 내용에는 헤드라인 강조 옵션에 대한 내용이 포함되어야 합니다." +
                        "쉼표 앞 뒤의 내용에 대한 글자 수는 각각 16자 이하여야 하며, 쉼표는 반드시 한글자로만 구성되어야 합니다.\n\n" +

                        "다음은 제공된 정보를 통해 출력해야 하는 뉴스 헤드라인의 출력 예시입니다:\n\n" +
                        "꾸준한 노력 끝에 완성한 마라톤 기록, 도전의 의미는?\n오랜만의 첫 도전, 무엇이 그를 움직이게 했나?\n드디어 끊어낸 술, 운동으로 극복해내" +

                        "다음은 제공된 정보를 통해 출력해야 하는 뉴스 헤드라인의 출력 형식입니다:\n\n" +
                        "첫번째 헤드라인 후보 내용\n두번째 헤드라인 후보 내용\n세번째 헤드라인 후보 내용\n" +

                        "반드시 뉴스 헤드라인 형식을 준수해 주세요. 정해진 형식을 따르지 않으면 응답을 처리할 수 없습니다." +
                        "만약 결과 헤드라인 내용이 위 형식과 다르다면, 다시 요청하여 주세요.",
                option, verb, noun
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
