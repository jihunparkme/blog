# OpenNMT-py

## Step 1: Prepare the data

사용할 데이터 지정은 .yaml 구성 파일에 작성

**toy_en_de.yaml**

```yaml
## 샘플 생성 위치
save_data: toy-ende/run/example
## 어휘 생성 위치
src_vocab: toy-ende/run/example.vocab.src
tgt_vocab: toy-ende/run/example.vocab.tgt
# 기존 파일 덮어쓰기 방지
overwrite: False

# Corpus opts:
data:
  corpus_1:
    path_src: toy-ende/src-train.txt
    path_tgt: toy-ende/tgt-train.txt
  valid:
    path_src: toy-ende/src-val.txt
    path_tgt: toy-ende/tgt-val.txt
```

**run**

- -n_sample : 어휘 생성을 위해 샘플링된 라인 수 지정

```shell
onmt_build_vocab -config toy_en_de.yaml -n_sample 10000
```

## Step 2: Train the model

모델 학습을 위해 yaml 파일에 사용될 어휘 경로와 매개변수를 추가

**test.yaml**

```yaml
# 생성된 어휘 위치
src_vocab: toy-ende/run/example.vocab.src
tgt_vocab: toy-ende/run/example.vocab.tgt

# GPU 설정
world_size: 1
gpu_ranks: [0]

# 체크포인트 저장 위치
save_model: toy-ende/run/model
save_checkpoint_steps: 500
train_steps: 1000
valid_steps: 500
```

**run**

- 기본 모델 실행 (인코더와 디코드 모두에 500개의 숨겨진 장치가 있는 2계층 LSTM으로 구성)
- -n_sample 옵션 사용 가능

```shell
onmt_train -config toy_en_de.yaml
```

## Step 3: Translate

```shell
onmt_translate -model toy-ende/run/model_step_1000.pt -src toy-ende/src-test.txt -output toy-ende/pred_1000.txt -gpu 0 -verbose
```

# Reference

> <https://github.com/OpenNMT/OpenNMT-py>
>
> <https://github.com/Parkchanjun/OpenNMT-Colab-Tutorial>



# opennmt 번역모델

- Opennmt 사용을 위해 NVIDUA-GPU Driver 필요
  - NVIDUA-GPU Driver 확인 
    - `nvidia-smi`
  - Docker container 에서 생성 시 gpu 사용 옵션 필요
    - `--gpus` : host gpu 사용 옵션
    - `-v` : 공유폴더 사용 옵션
    - ex) `docker run -itd -p 9922:22 --name opennmt_test --gpus all -v [host directory]:[container directory] nmt:opennmt-v2.0 /bin/bash`

```python
# 1. 도커 올리기
docker run -itd -p 9922:22 --name opennmt_test_gpu --gpus all -v /opt/data/nmt-model:/opt/nmt-model nmt:opennmt-v2.0 /bin/bash
            
2. ssh 설치 및 외부 접속
service ssh start

3. 파일 확인
cat xxx | wc -l # 라인 수
cat xxx | more # 파일 확인 (https://webdir.tistory.com/142)

4. OpenNMT-py 설치
https://github.com/OpenNMT/OpenNMT-py

5. yml 파일 작성
step, valid step, 모델 save 시점에 대한 개념
전체 데이터 학습 1회(epoch)와 step 의 차이

train_step : 100000 # 1회 학습 시 10000 step 필요, 통상 10회 내지 대규모에서는 40회 정도 (epoch 기준)
valid_step : 10000
save_checkpoint : 6500 # 상황에 따라 다르지만 보통 train_step 에 맞춤
 
7. 적용
# vocab 생성
onmt_build_vocab -config train.yaml
# train
onmt_train -config train.yaml
#translate check
onmt_translate -model toy-ende/run/model_step_1000.pt -src toy-ende/src-test.txt -output toy-ende/pred_1000.txt -gpu 0 -verbose

8. 실행 전 config 설정
8-1. /available_models/example.conf.json
{
    "models_root": "/opt/nmt-model/train/ip/oa/ko/v1.0/model", # model root path
    "models": [
        {
            "id": 0,
            "model": "incr_step_30000.pt", # 사용 model
            "timeout": 600,	# 특정 시간 이상 사용하지 않을 시
            "on_timeout": "to_cpu",  # cpu 로 전환
            "opt": {
                "gpu": 0,
                "beam_size": 5
            },
            "tokenizer": {
                "type": "sentencepiece",
                "model": "spm.model"  # model
            }
        }
    ]
}

8-2 실행 ('onmt/bin/server.py 에서 url 확인 및 필요 시 수정')
python server.py --ip 0.0.0.0 --port 22 --url_root / --config ./available_models/example.conf.json

'API Request'
POST http://{IP}:{PORT}/translate
'BODY'
[
    {
        "id" : 0,
        "src" : "스왑제공자들에 대한 통지 사본"
    }
]

8-3 결과 확인
[
    [
        {
            "n_best": 1,
            "pred_score": -4.463624000549316,
            "src": "스왑제공자들에 대한 통지 사본",
            "tgt": " ⁇  ⁇  of ⁇  ⁇ "
        }
    ]
]
```

