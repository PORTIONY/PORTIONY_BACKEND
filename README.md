# PORTIONY_BACKEND
함께 사서, 함께 나누는 **소분 공동구매 플랫폼** PORTIONY의 백엔드 리포지토리입니다.
<br>
<br>

## 기술 스택

| 구성           | 기술                      | 
| ------------ | ----------------------- |
| **Frontend** | React                   | 
| **Backend**  | Spring Boot             | 
| **통신 방식**    | REST API (JSON)         |
| **배포**       | CloudType, Netlify |
<br>

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


## Git 브랜치 전략

PORTIONY는 GitHub Flow를 기반으로 협업합니다.

- `main` : 배포 가능한 안정 코드
- `develop` : 통합 개발 브랜치 (default)
- `feature/{기능명}` : 기능 개발 브랜치  

<br>

## 커밋 컨벤션

| 타입 | 설명 |
|------|------|
| feat | 새로운 기능 추가 |
| fix  | 버그 수정 |
| style | 코드 포맷팅, 세미콜론 누락 등 |
| refactor | 코드 리팩토링 (기능 변경 없음) |
| chore | 빌드 업무, 패키지 매니저 설정 등 |

예시:

```bash
git commit -m "feat: AI 연결 추가"
```

