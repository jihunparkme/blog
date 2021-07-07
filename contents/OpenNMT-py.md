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
