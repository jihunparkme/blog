package com.example.login.login.dto;

import com.example.login.login.domain.Member;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MemberDto {
    private String email;
    private String password;
    private String auth;

    public Member toEntity(){
        return Member.builder()
                .email(email)
                .password(password)
                .auth(auth)
                .build();
    }

    @Builder
    public MemberDto(String email, String password, String auth) {
        this.email = email;
        this.password = password;
        this.auth = auth;
    }
}
