@echo off
ffmpeg -i "caches\3bd2cef7\0.mp3" -i "caches\3bd2cef7\1.mp3" -i "caches\3bd2cef7\2.mp3" -i "caches\3bd2cef7\3.mp3" -i "caches\3bd2cef7\4.mp3" -i "caches\3bd2cef7\5.mp3" -i "caches\3bd2cef7\6.mp3" -filter_complex "concat=n=7:v=0:a=1" "records\535108705121992704\2019-05-03 21-51-36.mp3"
pause
