-- 계좌 1·2와 두 계좌의 원장을 채우는 공용 픽스처. 영속성 어댑터 테스트와
-- 시스템 테스트가 함께 쓴다.
--
-- 활동 ID는 Hibernate @GeneratedValue 시퀀스(1부터 시작)와 겹치지 않게 1001부터
-- 띄워 잡는다. 마지막 두 행이 1500인 것은 계좌 1의 잔액을 1000으로 만들어
-- 500 이체 뒤에도 500이 남게 하려는 것이다 — 잔액이 이체액과 정확히 같으면
-- 출금 조건이 >=에서 >로 바뀌는 회귀를 테스트가 놓친다.
--
-- 시스템 테스트에는 트랜잭션 롤백이 없다. 앞선 테스트가 남긴 행을 먼저 지워
-- 매 테스트가 같은 상태에서 시작하게 한다. @DataJpaTest에서는 이 delete가
-- 테스트 트랜잭션 안에서 롤백되므로 무해하다.
delete from activity;
delete from account;

insert into account (id) values (1);
insert into account (id) values (2);

insert into activity (id, timestamp, owner_account_id, source_account_id, target_account_id, amount)
values (1001, '2018-08-08 08:00:00.0', 1, 1, 2, 500);

insert into activity (id, timestamp, owner_account_id, source_account_id, target_account_id, amount)
values (1002, '2018-08-08 08:00:00.0', 2, 1, 2, 500);

insert into activity (id, timestamp, owner_account_id, source_account_id, target_account_id, amount)
values (1003, '2018-08-09 10:00:00.0', 1, 2, 1, 1000);

insert into activity (id, timestamp, owner_account_id, source_account_id, target_account_id, amount)
values (1004, '2018-08-09 10:00:00.0', 2, 2, 1, 1000);

insert into activity (id, timestamp, owner_account_id, source_account_id, target_account_id, amount)
values (1005, '2019-08-09 09:00:00.0', 1, 1, 2, 1000);

insert into activity (id, timestamp, owner_account_id, source_account_id, target_account_id, amount)
values (1006, '2019-08-09 09:00:00.0', 2, 1, 2, 1000);

insert into activity (id, timestamp, owner_account_id, source_account_id, target_account_id, amount)
values (1007, '2019-08-09 10:00:00.0', 1, 2, 1, 1500);

insert into activity (id, timestamp, owner_account_id, source_account_id, target_account_id, amount)
values (1008, '2019-08-09 10:00:00.0', 2, 2, 1, 1500);
