package com.aetheri.infrastructure.adapter.out.kakao.dto;

/**
 * 회원 탈퇴(Unlink) 성공을 나타내는 응답 레코드입니다.
 * 탈퇴가 성공적으로 처리된 회원의 식별자(ID)를 포함하여 클라이언트에게 응답합니다.
 *
 * @param id 탈퇴가 성공적으로 처리된 회원의 고유 식별자(ID)입니다.
 */
public record UnlinkResponse(Long id) {
}