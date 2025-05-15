package com.movelog.practice.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberController {

    @PostMapping
    public ResponseEntity<String> register(@RequestBody MemberDto dto) {
        return ResponseEntity.ok("회원 등록 완료: " + dto.getName());
    }
}
