package com.aetheri.application.result.address;

import lombok.Builder;

@Builder
public record JusoResult(
    String roadAddr,    // 전체 도로명 주소
    String jibunAddr,   // 전체 지번 주소
    String zipNo        // 우편번호
    
    // (만약 시/군/구 이름이 필요하다면 아래처럼 추가하면 됩니다)
    // String siNm,
    // String sggNm

    /*String roadAddr,
    String roadAddrPart1,
    String roadAddrPart2,
    String engAddr,
    String jibunAddr,
    String zipNo,
    String admCd,
    String rnMgtSn,
    String bdMgtSn,
    String detBdNmList,
    String bdNm,
    String bdKdcd,
    String siNm,
    String sggNm,
    String emdNm,
    String liNm,
    String rn,
    String udrtYn,
    String buldMnnm,
    String buldSlno,
    String mtYn,
    String lnbrMnnm,
    String lnbrSlno,
    String emdNo,
    String hstryYn,
    String relJibun,
    String hemdNm*/
) {
}