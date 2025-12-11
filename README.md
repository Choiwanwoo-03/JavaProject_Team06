# 📚 JavaProject_Team06 '님 없으면 지구 망해요' 프로젝트 계획서
선문대학교 2학년 JAVA응용프로젝트 12분반 

6조 조별프로젝트_탄소배출량 계산 및 가이드 프로그램

---
📖 프로젝트 소개
-
본 프로젝트는 환경 오염 문제의 심각성 인지 부재와 동기부여 부족을 해결하기 위해 개발,
사용자의 일상 환경 행동을 실시간 탄소 배출량($\text{CO}_2\text{e}$)으로 환산하고, 친환경 이모티콘 수집 요소를 통해 지속적인 환경 활동 참여를 유도하는 학습 및 동기 부여 프로그램.

---

🎯 개발 배경
-
- 매년 꾸준히 생겨나는 환경 오염 피해 사례들 인지 및 심각성 환기.

- 사람들의 심각성 인지 부재 또는 동기부여 부족 해소.

- 쉽게 접근 가능하고 지속적으로 참여할만한 동기부여 제공.

- 기존 프로그램과 차별화된 실시간 수치 피드백 및 수집 요소 도입.

---

🎯 프로젝트 목표
-
재미있고 쉽게 접근 가능한 방식으로 시민들의 지속적인 친환경 행동 참여를 유도하고 동기부여를 제공하는 프로그램 개발.

---

✨ 주요 기능 
-
1. 탄소 배출량 실시간 환산 및 기록 (동작 등록 탭 & 대시보드)
- 사용자의 일상 행동(걷기, 자동차 주행, 쓰레기 처리 등)을 실시간 예상 탄소 배출량($\text{CO}_2\text{e}$)으로 즉시 환산하여 기록.

- 일일 환경 행동 기록 목록 표시 및 오늘의 총 예상 탄소 배출량 표시.

- 행동 기록의 필터링, 수정, 삭제 기능 제공.

---
  
2. 목표 달성 및 동기 부여 (목표 달성 트래커 탭)
- 일일 탄소 배출 목표 설정 및 현재 달성률 시각화.

- 일일 친환경 미션 제공 및 완료 시 랜덤 이모티콘 보상 지급.

- 일별, 주간, 월간 단위로 집계된 탄소 배출량 및 활동 횟수 종합 그래프 표시.

---
  
3. 학습 및 가이드 제공 (가이드 탭)
- 분리수거 방법, 일상생활 탄소 절감 팁 등 환경 정보를 제공 (읽기 전용).

- 항목 선택 시 해당 행동에 대한 상세 가이드 제공.

---

4. 수집 요소 (도감 탭)
- 사용자가 친환경 행동을 통해 수집한 이모티콘의 현황을 시각적으로 확인.

- 수집 현황 표시 및 이모티콘에 대한 상세 설명 열람 기능 제공.

---
👥 팀 구성 (자바 프로젝트 6조)
-
최한결 - 2022243046

최완우 - 2022243101

이한초아 - 2023243048

정상윤 - 2024310022

---

🛠 기술 스택
-
개발 언어 : Java


GUI 프레임워크 : java Swing


데이터베이스 : MySQL


라이브러리 : MySQL JDBC Driver


통신 : java Socket


개발 프로그램 : Eclipse / VSCODE


협업 도구 : Git, SourceTree, Notion


---

🛠 코드 설치 및 실행 방법
-

1. 데이터베이스 생성.

2. SQL 명령어를 실행해 각 테이블을 생성. (아래 첨부)

3. PROGRAM_Server/database/dataReader,
   
   PROGRAM_Server/database/dataWriter
   파일에 자신이 생성한 데이터베이스 키와 아이디, 패스워드를 입력.

4. Server의 Main 파일을 실행하여 서버를 열고 Client의 Main 파일을 실행하여 클라이언트를 서버에 접속.

5. 로그인 화면이 뜨면 아이디는 3자 이상, 패스워드는 6자 이상을 입력하여 회원가입을 해준 뒤 로그인.


테이블 생성 코드
-
- 유저 테이블

CREATE TABLE USER_TABLE ( 

USER_ID VARCHAR(10) NOT NULL PRIMARY KEY, 

USER_PWD VARCHAR(20) NOT NULL, 

USER_NICKNAME VARCHAR(10)  NOT NULL UNIQUE  

);



- 대시보드 테이블

CREATE TABLE DASHBOARD_TABLE ( 

dashboard_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 

USER_NICKNAME VARCHAR(10) NOT NULL,  DATE DATE NOT NULL, 

TIME TIME NOT NULL, 

TYPE VARCHAR(20) NOT NULL, 

RESULT DECIMAL(7,3) NOT NULL, 

COUNT DECIMAL(6,2) NOT NULL, 

UNIT VARCHAR(10) NOT NULL, 

FOREIGN KEY (USER_NICKNAME) REFERENCES user_table(USER_NICKNAME) 
);



- 목표 테이블
  
CREATE TABLE GOAL_TABLE ( 

GOAL_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 

USER_NICKNAME VARCHAR(10) NOT NULL, 

DATE DATE NOT NULL, 

TODAY_RESULT VARCHAR(20) NOT NULL, 

GOAL_RESULT VARCHAR(20) NOT NULL, 

FOREIGN KEY (USER_NICKNAME) REFERENCES user_table(USER_NICKNAME) 
);



- 미션 테이블
  
CREATE TABLE MISSION_TABLE ( 

MISSION_id INT NOT NULL AUTO_INCREMENT, 

USER_NICKNAME VARCHAR(50) NOT NULL, 

DATE DATE NOT NULL, 

MISSION1_NAME VARCHAR(255), 

MISSION1_SUCCESS INT DEFAULT 0,

MISSION2_NAME VARCHAR(255), 

MISSION2_SUCCESS INT DEFAULT 0, 

MISSION3_NAME VARCHAR(255), 

MISSION3_SUCCESS INT DEFAULT 0, 

PRIMARY KEY (MISSION_id), 

FOREIGN KEY (USER_NICKNAME) REFERENCES USER_TABLE(USER_NICKNAME), 

UNIQUE KEY UQ_MISSION_USER_DATE (USER_NICKNAME, DATE) 
);



- 이모티콘 테이블
  
CREATE TABLE EMOTICON_TABLE ( 

EMOTICON_id INT NOT NULL AUTO_INCREMENT, 

USER_NICKNAME VARCHAR(10) NOT NULL, 

RELEASED_EMOTICON VARCHAR(20) NOT NULL, 

PRIMARY KEY(EMOTICON_id), 

FOREIGN KEY (USER_NICKNAME) REFERENCES USER_TABLE(USER_NICKNAME) 
);

---


📅 개발 기간 및 일정
-
개발 기간

계획/기획: 9월 25일 ~ 10월 17일 (4주차 ~ 7주차)

분석: 10월 20일 ~ 10월 26일 (8주차)

설계: 10월 27일 ~ 11월 12일 (9주차 ~ 11주차)

구현: 11월 13일 ~ 12월 8일 (11주차 ~ 15주차)

테스트: 11월 27일 ~ 12월 8일 (13주차 ~ 15주차)

---

📞 문의처(이메일)
-
chldhksdn37@gmail.com

hwa2367@sunmoon.ac.kr

---
