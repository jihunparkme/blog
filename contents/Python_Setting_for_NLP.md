# Install Docker & Ubuntu SSH

https://data-make.tistory.com/674

# Install Python

**Install Python & PyDev in Eclipse**

- [Reference](https://m.blog.naver.com/PostView.naver?isHttpsRedirect=true&blogId=opusk&logNo=220984663685)

**Install Python packages offline**

- [Reference](https://dydtjr1128.github.io/python/2020/04/27/Python-offline-install.html)

```shell
##############################################################
## 1. python install package (transformers, pytorch, OpenNMT-py)
## python -m pip --trusted-host pypi.org --trusted-host files.pythonhosted.org install {package name}
##############################################################
pip install transformers

## https://pytorch.org/
pip3 install torch torchvision torchaudio

pip install OpenNMT-py

########################################
## 2. 설치된 라이브러리 목록을 파일로 저장
########################################
python -m pip freeze > requirements.txt

############################################
## 3. 설치된 기록된 파일명 라이브러리들 다운로드
## python -m pip download -r .\requirements.txt
############################################
python -m pip --trusted-host pypi.org --trusted-host files.pythonhosted.org download -r .\requirements.txt

############################################
## 4. 오프라인에서 설치할 컴퓨터로 복사
############################################

############################################
## 5. 다운로드 받은 파일들을 옮겨서 그 위치에서 파이썬을 실행 후 다음 명령어를 실행
############################################
python -m pip install --no-index --find-links="./" -r .\requirements.txt
```

# Building a python venv

**Creating Virtual Environments**

- [Reference](https://docs.python.org/ko/3.7/tutorial/venv.html)

```shell
#############################################
## 1. 가상환경 생성
#############################################
python -m venv ./venv

#############################################
## 2. 가상환경 Shell 접속
#############################################
.\venv\Scripts\activate.bat

############################################
## 3. 기존에 다운로드 받은 package 설치
############################################
python -m pip install --no-index --find-links="./" -r .\requirements.txt

############################################
## 3-1. 설치된 package 확인
############################################
pip freeze

############################################
## 4. venv 종료
############################################
deactivate

############################################
## 5. venv 삭제
############################################
rm -rf [venv Name]
```
