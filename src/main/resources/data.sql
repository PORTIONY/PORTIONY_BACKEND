-- location.csv 파일 db로드 오류가 미해결이라 지역3종 id를 임의로 삽입하도록 했습니다!
-- id 3개는 Run실행할때마다 db에 저장됩니다

-- region
INSERT IGNORE INTO region (id, city) VALUES (22, '서울특별시');

-- Subregion
INSERT IGNORE INTO subregion (id, district, region_id) VALUES (287, '강남구', 22);

-- Dong
INSERT IGNORE INTO dong (id, dong, subregion_id) VALUES (7486, '역삼동', 287);

