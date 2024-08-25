# 체크인 분리수거장
소프트웨어교육혁신센터에서 주관하는 K-HACKATHON앱 개발 챌린지에서 소개한 애플리케이션 입니다.   
체크인 분리수거장이라는 주제로 모바일 애플리케이션을 구현하였습니다.

## 프로젝트 소개
체크인 분리수거장은 모바일 앱을 통해서 실시간 분리수거량 잔량 현황을 체크하고 qr코드 인증을 통해 제한된 이용자만 방문 가능하도록 만든 어플리케이션 입니다.
분리수거장이 있는 곳을 직접 방문해야 쓰레기 잔량을 확인할 수 있어 이는 청소미화원이나 아파트 관리자 분들에게는 매우 불편한 점이라고 생각합니다.
방문 전에 미리 앱으로 확인하고 일정양이 차면 청소할 수 있게 하여 비효율적인 작업을 줄이고 동선의 최적화를 목적으로 앱을 개발하게 되었습니다.

## 기간 
2021년 6월 ~ 2021년 11월

## 개발환경
- tool : android studio
- language : java
- middleware : php
- dbms : mysql
- api : Gooogle Maps API

## 주요기능
- 분리수거장 위치 확인
    - Google Map Api 사용
      
- 실시간 쓰레기 잔량 체크
    - 아두이노 초음파 센서로 사물과의 거리 측정
      
- QR 체크인을 통한 인증된 사용자만 출입 허용
    - QR 코드를 스캔하여 앱에 인증이 된 사용자일 경우에만 서보모터 동작
  
![슬라이드5](https://github.com/hwajinkim/CheckInRecycle/assets/68608437/ca3bb488-3006-4dc5-a419-f9962273bb11)
![슬라이드6](https://github.com/hwajinkim/CheckInRecycle/assets/68608437/c83417da-3369-4d86-9b4c-cfc9d9e1fe80)
![슬라이드7](https://github.com/hwajinkim/CheckInRecycle/assets/68608437/77ddf96b-7936-41ce-b5d0-a585bfdb8032)

## 기대효과
- 어플을 통한 쓰레기 잔량을 실시간으로 체크하고 쓰레기의 월별 양, 버린 시간 기록
    - 이용자 패턴을 분석하여 쓰레기 감소를 유도할 수 있다.
    
- 허가된 사용자인 경우에만 분리수거장 출입이 가능하고 출입 인증시 인증 내역을 저장
    - 외부인의 쓰레기 무단 투기를 방지하고 방문 기록을 관리 할 수 있다.  

## 서비스 상용화 시 문제점
- 블루투스를 사용하여 아두이노와 연결하였는데 원격지에서 잔량을 확인하려면 이더넷으로 연결하여야 한다.
- 아파트 단지와 같은 경우에는 인터넷 망이 들어서기에는 어려움이 있다.
