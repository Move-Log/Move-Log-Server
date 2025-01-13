package com.movelog.domain.auth.presentation;

import com.movelog.domain.auth.dto.AuthRes;
import com.movelog.domain.auth.dto.IdTokenReq;
import com.movelog.domain.auth.application.AuthService;
import com.movelog.domain.auth.dto.NicknameRes;
import com.movelog.domain.user.domain.User;
import com.movelog.domain.user.domain.repository.UserRepository;
import com.movelog.global.config.security.jwt.JWTUtil;
import com.movelog.global.config.security.oidc.OidcProviderFactory;
import com.movelog.global.config.security.oidc.Provider;
import com.movelog.global.config.security.token.CurrentUser;
import com.movelog.global.config.security.token.UserPrincipal;
import com.movelog.global.payload.ErrorResponse;
import com.movelog.global.payload.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Authorization", description = "Authorization API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OidcProviderFactory oidcProviderFactory;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;


    private final AuthService authService;


    @Operation(summary = "소셜 로그인", description = "idToken과 provider(ex. kakao)로 소셜 로그인을 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AuthRes.class)))}),
            @ApiResponse(responseCode = "400", description = "로그인 실패", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @PostMapping("/login")
    public ResponseEntity<AuthRes> login(@RequestBody IdTokenReq idTokenReq) {
        System.out.println("idTokenReq = " + idTokenReq.idToken());
        String providerId = oidcProviderFactory.getProviderId(
                Provider.valueOf(idTokenReq.provider().toUpperCase()), idTokenReq.idToken());

        if (providerId == null) {
            return ResponseEntity.badRequest().build();
        }

        authService.findOrCreateUser(idTokenReq.provider(), idTokenReq.idToken());
        String email = authService.findEmail(providerId);

        String accessToken = jwtUtil.createJwt("access", providerId, "ROLE_USER", 2592000000L, email);
        System.out.println("accessToken = " + accessToken);
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(EntityNotFoundException::new);


        AuthRes authRes = AuthRes.builder()
                .accessToken(accessToken)
                .isRegistered(user.isRegistered())
                .build();

        return ResponseEntity.ok(authRes);
    }
    @Operation(summary = "회원 가입", description = "회원가입을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AuthRes.class)))}),
            @ApiResponse(responseCode = "400", description = "로그인 실패", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        authService.changeIsRegistered(userPrincipal.getId());

        return ResponseEntity.ok().build();
    }


    @Operation(summary = "회원 탈퇴", description = "해당 유저의 가입을 탈퇴합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Message.class)))}),
            @ApiResponse(responseCode = "400", description = "회원 탈퇴 실패", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @DeleteMapping()
    public ResponseEntity<Message> unlinkSocialAccount(
            @Parameter(description = "Accesstoken을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(authService.unlinkAccount(userPrincipal.getId()));
    }

}
