package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;

//약관 항목(예: 서비스 이용약관, 개인정보 처리방침 등)을 DB에서 조회하기 위한 리포지토리.
//
//사용 목적:n클라이언트가 선택한 agreementIds에 해당하는 약관들을 DB에서 찾음

public interface AgreementRepository extends JpaRepository<Agreement, Long> {
}
