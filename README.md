## 폴더 구조

```
.github
└── workflows
    └── deploy.yml      # 깃허브 액션 배포 설정 파일
com.portiony.portiony
├── config              # swagger, security 등 프로젝트 전역 설정 클래스
├── controller          # API 컨트롤러 클래스(http 요청을 처리)
├── converter           # 객체 변환 로직 (DTO ↔ Entity)
├── dto                 # DTO 클래스 (요청, 응답, 내부 전달용)
├── entity              # JPA 엔티티 클래스
├── jwt                 # JWT 관련 엔티티 또는 유틸
├── repository          # JPA Repository 인터페이스
├── security            # 시큐리티 설정 및 필터
├── service             # 비즈니스 로직 서비스 클래스
└── PortionyApplication.java  #  메인 실행 클래스


```
