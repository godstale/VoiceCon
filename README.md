# VoiceCon
Voice controller using MQTT

MQTT VoiceCon 은 원격에서 음성인식으로 장치를 제어하는데 사용하기 위한 안드로이드 앱입니다.

VoiceCon 앱은 메시지 전송을 위해 MQTT 프로토콜을 사용합니다. 따라서 라즈베리파이 같은 서버에 MQTT broker(server) 프로그램이 실행중이어야 합니다. 일단 MQTT broker 만 준비된다면 다양한 장치를 MQTT broker에 연결하고 VoiceCon 앱으로 음성으로 제어할 수 있습니다.

MQTT 프로토콜과 서버 구성에 대해서는 아래 링크를 참고하세요.

http://www.hardcopyworld.com/gnuboard5/bbs/board.php?bo_table=lecture_rpi&wr_id=60

MQTT VoiceCon 앱은 Paho MQTT android service library 를 일부 수정해서 사용하고 있습니다. 

Paho 라이브러리에 대한 상세 내용은 아래 링크를 참고하세요.

https://eclipse.org/paho/clients/android/

MQTT VoiceCon 앱은 소스가 모두 공개되어 있으니 문서 하단의 GitHub 링크에서 확인하세요.

Google Play Store 앱 다운로드 (v4.0.3 이상 지원)

https://play.google.com/store/apps/details?id=com.hardcopy.vcontroller
