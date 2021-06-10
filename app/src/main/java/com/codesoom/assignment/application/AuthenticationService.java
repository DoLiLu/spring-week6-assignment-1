package com.codesoom.assignment.application;

import com.codesoom.assignment.domain.User;
import com.codesoom.assignment.domain.UserRepository;
import com.codesoom.assignment.dto.AuthorizationHeader;
import com.codesoom.assignment.dto.SessionRequestData;
import com.codesoom.assignment.errors.InvalidPasswordException;
import com.codesoom.assignment.errors.InvalidTokenException;
import com.codesoom.assignment.errors.UserNotFoundException;
import com.codesoom.assignment.utils.JwtUtil;
import com.github.dozermapper.core.Mapper;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthenticationService {
    private Mapper mapper;
    private JwtUtil jwtUtil;
    private UserRepository userRepository;

    public AuthenticationService(Mapper mapper, JwtUtil jwtUtil, UserRepository userRepository) {
        this.mapper = mapper;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    /**
     * 토큰을 생성한다.
     *
     * @param sessionRequestData 로그인 요청 정보
     * @return Access 토큰
     */
    public String login(SessionRequestData sessionRequestData) {
        User foundUser = findUser(sessionRequestData);
        User source = mapper.map(sessionRequestData, User.class);
        foundUser.changeWithExpiredLength(LocalDateTime.now());
        return jwtUtil.encode(foundUser.getId());
    }

    /**
     * Token을 받으면 토큰 유효성을 검증 후 사용자 아이디를 반환한다.
     *
     * @param accessToken Access 토큰
     * @return 사용자 아이디
     */
    public Long parseToken(String accessToken) {
        Claims claims = jwtUtil.decode(accessToken);
        return claims.get("userId", Long.class);
    }

    /**
     * 요청받은 Header을 전처리 이후 유효성을 검증한다.
     *
     * @param authorization 헤더값
     */
    public Boolean accessTokenCheck(String authorization) {
        if (authorization == null
                || authorization.length() < AuthorizationHeader.Bearer.getAuthorizationLength()) {
            throw new InvalidTokenException(authorization);
        }
        String accessToken = authorization.substring(AuthorizationHeader.Bearer.getAuthorizationLength());
        parseToken(accessToken);
        return Boolean.TRUE;
    }

    private User findUser(SessionRequestData sessionRequestData) {
        User foundUser = userRepository.findByEmail(
                sessionRequestData.getEmail()).orElseThrow(() -> new UserNotFoundException(null));
        if (!foundUser.getPassword().equals(sessionRequestData.getPassword())) {
            throw new InvalidPasswordException(foundUser.getEmail());
        }
        return foundUser;
    }
}
