package com.portiony.portiony.repository;

import com.portiony.portiony.entity.User;
import com.portiony.portiony.entity.UserAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//유저가 어떤 약관에 동의했는지를 저장하는 연결 테이블(UserAgreement) 관리용 리포지토리
//사용 목적:회원가입 시, 유저와 약관 간의 관계를 저장하기 위해 필요
//findAllByUserIsNull()을 통해 가입 전 임시 저장된 동의 항목을 조회하고, 가입 완료 시 user 연결

public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {

    // 만약 임시로 저장한 동의 항목이 특정 유저 없이 저장된 상태라면,
    // 이 메서드를 통해 회원가입 완료 시 유저를 연결해줄 수 있음
    List<UserAgreement> findAllByUserIsNull();

    default void updateUserReference(User user) {
        List<UserAgreement> agreements = findAllByUserIsNull();
        for (UserAgreement ua : agreements) {
            ua.setUser(user);
        }
        saveAll(agreements);
    }
}
