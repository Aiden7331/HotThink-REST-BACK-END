SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Tables */

DROP TABLE IF EXISTS TB_HISTORY;
DROP TABLE IF EXISTS TB_REAL;
DROP TABLE IF EXISTS TB_REPORT;
DROP TABLE IF EXISTS TB_REPLY;
DROP TABLE IF EXISTS TB_REPUTATION;
DROP TABLE IF EXISTS TB_SCRAP;
DROP TABLE IF EXISTS TB_BOARD;
DROP TABLE IF EXISTS TB_CATEG;
DROP TABLE IF EXISTS TB_CONVERSATION;
DROP TABLE IF EXISTS TB_CHATROOM;
DROP TABLE IF EXISTS TB_EMAIL_CONFIRM;
DROP TABLE IF EXISTS TB_FOLLOW;
DROP TABLE IF EXISTS TB_LIKE;
DROP TABLE IF EXISTS TB_PAYLIST;
DROP TABLE IF EXISTS TB_PREFERENCE;
DROP TABLE IF EXISTS TB_SUBSCRIBE;
DROP TABLE IF EXISTS TB_USER;




/* Create Tables */

CREATE TABLE TB_BOARD
(
	BD_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	CT_CODE varchar(30) NOT NULL,
	-- 게시물 번호
	SEQ bigint unsigned NOT NULL COMMENT '게시물 번호',
	HITS int unsigned DEFAULT 0 NOT NULL,
	TITLE varchar(150) NOT NULL,
	BOARD_TYPE varchar(50) NOT NULL,
	CREATE_AT datetime NOT NULL,
	CONTENTS text NOT NULL,
	GOOD int unsigned DEFAULT 0 NOT NULL,
	THUMBNAIL_IMG varchar(255),
	UR_SEQ bigint unsigned NOT NULL,
	PRIMARY KEY (BD_SEQ)
);


CREATE TABLE TB_CATEG
(
	CT_CODE varchar(30) NOT NULL,
	CATEGORY varchar(30) NOT NULL,
	PRIMARY KEY (CT_CODE),
	UNIQUE (CATEGORY)
);


CREATE TABLE TB_CHATROOM
(
	CR_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	STATE varchar(50),
	UR_SELLER bigint unsigned NOT NULL,
	UR_BUYER bigint unsigned NOT NULL,
	PRIMARY KEY (CR_SEQ),
	UNIQUE (CR_SEQ)
);


CREATE TABLE TB_CONVERSATION
(
	CV_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	AT datetime NOT NULL,
	MESSAGE varchar(1500) NOT NULL,
	CR_SEQ bigint unsigned NOT NULL,
	UR_TO bigint unsigned NOT NULL,
	UR_FROM bigint unsigned NOT NULL,
	PRIMARY KEY (CV_SEQ),
	UNIQUE (CV_SEQ),
	UNIQUE (CR_SEQ),
	UNIQUE (UR_TO),
	UNIQUE (UR_FROM)
);


CREATE TABLE TB_EMAIL_CONFIRM
(
	EC_SEQ int unsigned NOT NULL AUTO_INCREMENT,
	EXPIRED_AT datetime NOT NULL,
	REGISTER_AT datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
	-- 이메일로 전송되는 키값
	EMAILKEY int(10) NOT NULL COMMENT '이메일로 전송되는 키값',
	UR_SEQ bigint unsigned NOT NULL,
	PRIMARY KEY (EC_SEQ),
	UNIQUE (UR_SEQ)
);


CREATE TABLE TB_FOLLOW
(
	FL_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	UR_CELEBRITY bigint unsigned NOT NULL,
	UR_FOLLOWER bigint unsigned NOT NULL,
	PRIMARY KEY (FL_SEQ),
	UNIQUE (FL_SEQ)
);


CREATE TABLE TB_HISTORY
(
	HT_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	-- 수정된 날짜
	UPDATE_AT datetime NOT NULL COMMENT '수정된 날짜',
	CONTENTS text NOT NULL,
	TITLE varchar(150) NOT NULL,
	THUMBNAILIMG varchar(280),
	BD_SEQ bigint unsigned NOT NULL,
	PRIMARY KEY (HT_SEQ)
);


CREATE TABLE TB_LIKE
(
	LK_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	UR_SEQ bigint unsigned NOT NULL,
	BOARD_SEQ bigint unsigned NOT NULL,
	BOARD_TYPE varchar(50) NOT NULL,
	PRIMARY KEY (LK_SEQ)
);


CREATE TABLE TB_PAYLIST
(
	IP_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	-- 거래가격
	PRICE double unsigned NOT NULL COMMENT '거래가격',
	-- 거래날짜
	PAY_AT datetime NOT NULL COMMENT '거래날짜',
	-- 결제수단
	PAYMETHOD varchar(100) NOT NULL COMMENT '결제수단',
	PAY_CATEGORY varchar(30) NOT NULL,
	UR_BUYER bigint unsigned NOT NULL,
	PRIMARY KEY (IP_SEQ)
);


CREATE TABLE TB_PREFERENCE
(
	FR_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	PREFERENCE varchar(20) NOT NULL,
	UR_SEQ bigint unsigned NOT NULL,
	PRIMARY KEY (FR_SEQ)
);


CREATE TABLE TB_REAL
(
	RL_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	STATE varchar(15) NOT NULL,
	UPDATE_AT datetime,
	REVIEW text,
	-- 유료정보
	PMATERIAL text COMMENT '유료정보',
	BD_SEQ bigint unsigned NOT NULL,
	PRIMARY KEY (RL_SEQ)
);


CREATE TABLE TB_REPLY
(
	RP_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	CONTENTS varchar(2000) NOT NULL,
	AT datetime NOT NULL,
	ADOPT varchar(10) NOT NULL,
	GOOD int unsigned DEFAULT 0 NOT NULL,
	BD_SEQ bigint unsigned NOT NULL,
	RE_SEQ bigint unsigned NOT NULL,
	UR_SEQ bigint unsigned NOT NULL,
	PRIMARY KEY (RP_SEQ)
);


CREATE TABLE TB_REPORT
(
	RE_SEQ int unsigned NOT NULL AUTO_INCREMENT,
	REASON varchar(100) NOT NULL,
	DETAIL varchar(3000),
	UR_SEQ bigint unsigned NOT NULL,
	BD_SEQ bigint unsigned,
	RP_SEQ bigint unsigned,
	PRIMARY KEY (RE_SEQ),
	UNIQUE (RE_SEQ)
);


CREATE TABLE TB_REPUTATION
(
	BUYER_SCORE smallint unsigned,
	BUYER_COMMENTS varchar(1500),
	SELLER_SCORE smallint unsigned,
	SELLER_COMMENTS varchar(1500),
	IP_SEQ bigint unsigned NOT NULL,
	BD_SEQ bigint unsigned NOT NULL,
	UNIQUE (IP_SEQ),
	UNIQUE (BD_SEQ)
);


CREATE TABLE TB_SCRAP
(
	SC_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	UR_SEQ bigint unsigned NOT NULL,
	BD_SEQ bigint unsigned NOT NULL,
	PRIMARY KEY (SC_SEQ)
);


CREATE TABLE TB_SUBSCRIBE
(
	UR_SEQ bigint unsigned NOT NULL,
	S_START datetime,
	S_END datetime,
	UNIQUE (UR_SEQ)
);


CREATE TABLE TB_USER
(
	UR_SEQ bigint unsigned NOT NULL AUTO_INCREMENT,
	EMAIL varchar(30) NOT NULL,
	NICKNAME varchar(20) NOT NULL,
	UNAME varchar(20),
	PW varchar(100) NOT NULL,
	TEL varchar(15),
	AUTH varchar(30) DEFAULT 'NONE' NOT NULL,
	UPOINT int DEFAULT 0 NOT NULL,
	-- 리얼띵크 작성권
	REALTICKET smallint unsigned DEFAULT 0 NOT NULL COMMENT '리얼띵크 작성권',
	USE_AT enum('Y','N') DEFAULT 'N' NOT NULL,
	PROFILE_IMG varchar(255),
	PRIMARY KEY (UR_SEQ),
	UNIQUE (UR_SEQ),
	UNIQUE (EMAIL),
	UNIQUE (NICKNAME)
);



/* Create Foreign Keys */

ALTER TABLE TB_HISTORY
	ADD FOREIGN KEY (BD_SEQ)
	REFERENCES TB_BOARD (BD_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_REAL
	ADD FOREIGN KEY (BD_SEQ)
	REFERENCES TB_BOARD (BD_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_REPLY
	ADD FOREIGN KEY (BD_SEQ)
	REFERENCES TB_BOARD (BD_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_REPORT
	ADD FOREIGN KEY (BD_SEQ)
	REFERENCES TB_BOARD (BD_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_REPUTATION
	ADD FOREIGN KEY (BD_SEQ)
	REFERENCES TB_BOARD (BD_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_SCRAP
	ADD FOREIGN KEY (BD_SEQ)
	REFERENCES TB_BOARD (BD_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_BOARD
	ADD FOREIGN KEY (CT_CODE)
	REFERENCES TB_CATEG (CT_CODE)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_CONVERSATION
	ADD FOREIGN KEY (CR_SEQ)
	REFERENCES TB_CHATROOM (CR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_REPUTATION
	ADD FOREIGN KEY (IP_SEQ)
	REFERENCES TB_PAYLIST (IP_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_REPLY
	ADD FOREIGN KEY (RE_SEQ)
	REFERENCES TB_REPLY (RP_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_REPORT
	ADD FOREIGN KEY (RP_SEQ)
	REFERENCES TB_REPLY (RP_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_BOARD
	ADD FOREIGN KEY (UR_SEQ)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_CHATROOM
	ADD FOREIGN KEY (UR_BUYER)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_CHATROOM
	ADD FOREIGN KEY (UR_SELLER)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_CONVERSATION
	ADD FOREIGN KEY (UR_TO)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_CONVERSATION
	ADD FOREIGN KEY (UR_FROM)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_EMAIL_CONFIRM
	ADD FOREIGN KEY (UR_SEQ)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_FOLLOW
	ADD FOREIGN KEY (UR_CELEBRITY)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_FOLLOW
	ADD FOREIGN KEY (UR_FOLLOWER)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_LIKE
	ADD FOREIGN KEY (UR_SEQ)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_PAYLIST
	ADD FOREIGN KEY (UR_BUYER)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_PREFERENCE
	ADD FOREIGN KEY (UR_SEQ)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_REPLY
	ADD FOREIGN KEY (UR_SEQ)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_REPORT
	ADD FOREIGN KEY (UR_SEQ)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_SCRAP
	ADD FOREIGN KEY (UR_SEQ)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE TB_SUBSCRIBE
	ADD FOREIGN KEY (UR_SEQ)
	REFERENCES TB_USER (UR_SEQ)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;



