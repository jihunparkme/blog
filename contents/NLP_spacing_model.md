# Korean spacing Model

Taekyoon 님이 개발하신 한국어 띄어쓰기 모델 `Trainable Korean spacing (TaKos)` 을 간략하게 테스트해보고자 한다.

- 자연어처리에서는 텍스트를 토큰 단위로 구분하여 다루는데, 가장 쉬운 토크나이징 방법은 띄어 쓴 단어를 구분하는 것
- 한국어의 경우 띄어쓰기는 텍스트의 의미를 구분하는 데 큰 영향을 줌

## Reference

[takos-alpha](https://github.com/Taekyoon/takos-alpha)

[한국어 띄어쓰기 프로그램 도전기](https://www.slideshare.net/TaekyoonChoi/taekyoon-choi-pycon)

## Install

```shell
git clone https://github.com/Taekyoon/takos-alpha.git
pip install -r requirements
python setup.py install
```

## Requirements packages

```text
torch
numpy
pandas
tqdm
sklearn
tb-nightly
future
Pillow
prettytable
jiwer
flask
pytorch-pretrained-bert
eli5
```

## Set Configs

**./scripts/my_configs.json**

```json
{
  "type": "word_segment",
  "gpu_device": -1, # gpu_device 를 사용할 경우 device number
  "load_model": null, # 생성된 모델이 있을 경우 모델 경로. "./tmp/bilstm_spacing/model/best_val.pkl"
  "tokenizer": "syllable_tokenizer",
  "dataset": {
    "name": "your_dataset",
    "train": { # 띄어쓰기 모델 학습에 사용할 데이터셋
      "vocab_min_freq": 10, # 단어 최소 빈도수
      "input": "./samples/train.txt"
    },
    "test": { # 띄어쓰기 모델 평가에 사용할 데이터셋
      "limit_len": 150, # 최대 문장 길이
      "input": "./samples/test.txt"
    }
  },
  "deploy": {
    "path": "./tmp/bilstm_spacing" # train result 경로
  },
  "model": {
    "type": "bilstm_crf",
    "parameters": {
      "word_embedding_dims": 32,
      "hidden_dims": 64
    }
  },
  "train": {
    "epochs": 10, # train/valid steps size
    "eval_steps": -1,
    "learning_rate": 3e-4,
    "eval_batch_size": 10,
    "batch_size": 64, # how many samples per batch to load
    "sequence_length": 50
  }
}
```

## Create WordSegmentAgent

```python
from takos.agents.word_segment import WordSegmentAgent

spacing_agent = WordSegmentAgent('./scripts/my_configs.json') # 설정 파일 등록

'''
load_model 이 없을 경우.
2021-11-24 16:56:37,221 (word_segment.py:98): [INFO] - no best model
'''
```

## Train

```python
spacing_agent.train() # 학습
```

- train 이 시작되면 configs 에서 설정한 정보가 출력되고 `number of epochs` 만큼 train/valid steps 를 반복

```shell
2021-11-24 16:56:37,221 (builder.py:217): [INFO] - now get training dataloader object...
2021-11-24 16:56:37,237 (seq_tag_trainer.py:62): [INFO] - deploy path: tmp\bilstm_spacing\model
2021-11-24 16:56:37,252 (seq_tag_trainer.py:63): [INFO] - random seed number: 49
2021-11-24 16:56:37,252 (seq_tag_trainer.py:64): [INFO] - learning rate: 0.0003
2021-11-24 16:56:37,252 (seq_tag_trainer.py:65): [INFO] - evaluation check steps: -1
2021-11-24 16:56:37,252 (seq_tag_trainer.py:66): [INFO] - number of epochs: 10
2021-11-24 16:56:37,252 (seq_tag_trainer.py:67): [INFO] - training device: cpu
2021-11-24 16:56:37,252 (seq_tag_trainer.py:68): [INFO] - eval labels: [3, 4, 5, 6]
2021-11-24 16:56:37,252 (word_segment.py:155): [INFO] - BilstmCRF(
  (_embedding): Embedding(494, 32, padding_idx=0)
  (_bilstm): BiLSTM(
    (_ops): LSTM(32, 64, batch_first=True, bidirectional=True)
  )
  (_fc): Linear(in_features=128, out_features=7, bias=True)
  (_crf): CRF()
)
2021-11-24 16:56:37,252 (seq_tag_trainer.py:107): [INFO] - start training epoch 1
train steps: 100%|██████████| 14/14 [00:04<00:00,  3.41it/s]
2021-11-24 16:56:58,450 (seq_tag_trainer.py:148): [INFO] - epoch 1 is done!
2021-11-24 16:56:58,497 (seq_tag_trainer.py:75): [INFO] - now evaluating...
valid steps: 100%|██████████| 10/10 [00:00<00:00, 17.78it/s]
2021-11-24 16:57:16,116 (seq_tag_trainer.py:157): [INFO] - epoch : 1, steps : 14, tr_loss : 48.384, val_f1 : 0.376, val_score : 6.469
2021-11-24 16:57:16,116 (seq_tag_trainer.py:163): [INFO] - current best tag f1: 0.376
2021-11-24 16:57:16,163 (seq_tag_trainer.py:107): [INFO] - start training epoch 2
train steps: 100%|██████████| 14/14 [00:04<00:00,  3.45it/s]
2021-11-24 16:57:37,361 (seq_tag_trainer.py:148): [INFO] - epoch 2 is done!
2021-11-24 16:57:37,408 (seq_tag_trainer.py:75): [INFO] - now evaluating...
valid steps: 100%|██████████| 10/10 [00:00<00:00, 17.78it/s]
2021-11-24 16:57:54,857 (seq_tag_trainer.py:157): [INFO] - epoch : 2, steps : 28, tr_loss : 46.681, val_f1 : 0.409, val_score : 8.696
2021-11-24 16:57:54,857 (seq_tag_trainer.py:163): [INFO] - current best tag f1: 0.409
2021-11-24 16:57:54,888 (seq_tag_trainer.py:107): [INFO] - start training epoch 3
...
2021-11-24 17:02:40,259 (seq_tag_trainer.py:107): [INFO] - start training epoch 10
train steps: 100%|██████████| 14/14 [00:04<00:00,  2.92it/s]
2021-11-24 17:03:03,254 (seq_tag_trainer.py:148): [INFO] - epoch 10 is done!
2021-11-24 17:03:03,297 (seq_tag_trainer.py:75): [INFO] - now evaluating...
valid steps: 100%|██████████| 10/10 [00:00<00:00, 16.85it/s]
2021-11-24 17:03:23,986 (seq_tag_trainer.py:157): [INFO] - epoch : 10, steps : 140, tr_loss : 35.927, val_f1 : 0.540, val_score : 51.026
2021-11-24 17:03:23,986 (seq_tag_trainer.py:163): [INFO] - current best tag f1: 0.540
```

_seq_tag_trainer.py \_train_epoch() 참고_

- `steps` : epoch \* steps_in_epoch + (step + 1)
  - `steps_in_epoch`
    - batch_size : 32, -> 28
    - batch_size : 64, -> 14
    - batch_size : 128, -> 7
    - batch_size : 256, -> 3
  - `step` : steps_in_epoch 의 각 step, batch_size 가 64 일 경우 0~13
- `tr_loss` :
- [var_f1](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.f1_score.html) : 정밀도와 회수율의 조화 평균으로 해석, 1에 가까울 수록 높은 점수
- `val_score` : 점수 평가

## Evaluation

```python
spacing_agent.eval() # 평가
```

eval_word_segment_model.py summary() 참고

- `WER score` : wer_score / (step + 1 - score_failure_cnt)
  - `WER` : 실제 문장과 기계가 예측한 문장 사이의 최소 편집 거리. [jiwer.wer](https://pypi.org/project/jiwer/)
- `SER score` : corrected_sent_cnt / (step + 1 - score_failure_cnt)
  - `corrected_sent_cnt` : 실제 문장과 기계 예측 문장이 일치하는 개수
- `F1 score` : f1_score / (step + 1 - score_failure_cnt)
  - `f1_score` : 정밀도와 회수율의 조화 평균으로 해석, 1에 가까울 수록 높은 점수. [sklearn.metrics.f1_score](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.f1_score.html)
- `ACC score` : acc_score / (step + 1 - score_failure_cnt)
  - `acc_score` : 정확도 분류 점수. [sklearn.metrics.accuracy_score](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.accuracy_score.html)

**`epochs : 10`**

```shell
2021-11-24 17:03:24,033 (eval_word_segment_model.py:44): [INFO] - now evaluate!
evaluation steps: 100%|██████████| 100/100 [00:01<00:00, 88.15it/s]
2021-11-24 17:03:25,167 (eval_word_segment_model.py:94): [INFO] - evaluation done!
2021-11-24 17:03:25,167 (word_segment.py:170): [INFO] -
+-----------+--------+
|    Name   | Score  |
+-----------+--------+
| WER score | 0.8850 | # Word Error Rate Score
| SER score | 0.9300 | # Sentence Error Rate Score
|  F1 score | 0.7139 | # 띄어쓰기 경계 인식 수준
| ACC score | 0.7625 | # Accuracy Rate
+-----------+--------+
```

**`epochs : 100`**

- Error Rate 비율은 줄어들고 F1, ACC 점수는 향상된 것을 볼 수 있다.

```shell
...
2021-11-24 18:58:13,219 (seq_tag_trainer.py:107): [INFO] - start training epoch 100
train steps: 100%|██████████| 14/14 [00:04<00:00,  3.43it/s]
2021-11-24 18:58:33,714 (seq_tag_trainer.py:148): [INFO] - epoch 100 is done!
2021-11-24 18:58:33,745 (seq_tag_trainer.py:75): [INFO] - now evaluating...
valid steps: 100%|██████████| 10/10 [00:00<00:00, 17.78it/s]
2021-11-24 18:58:50,694 (seq_tag_trainer.py:157): [INFO] - epoch : 100, steps : 1400, tr_loss : 8.865, val_f1 : 0.837, val_score : 127.779
2021-11-24 18:58:50,694 (seq_tag_trainer.py:163): [INFO] - current best tag f1: 0.840

+-----------+--------+
|    Name   | Score  |
+-----------+--------+
| WER score | 0.4588 |
| SER score | 0.8400 |
|  F1 score | 0.8982 |
| ACC score | 0.9008 |
+-----------+--------+
```

## Result

띄어쓰는 지점에 대해 `B` 태그로 예측하도록 학습된 결과

- `B` : Begin
- `I` : Inside
- `E` : End
- `S` : Single

- 테스트 목적으로 `epochs : 10`으로 설정하였더니 결과가 마음에 들진 않는다..

```python
print(spacing_agent('학교종이땡땡땡')) # 실행
print(spacing_agent('오늘은무슨요알'))

'''
{'input': '학교종이땡땡땡', 'label': ['B', 'I', 'E', 'B', 'I', 'I', 'I'], 'sequence_score': array([6.5998054], dtype=float32), 'output': '학교종 이땡땡땡', 'segment_pos': [0, 3]}

{'input': '오늘은무슨요일', 'label': ['I', 'I', 'E', 'B', 'I', 'I', 'E'], 'sequence_score': array([3.519576], dtype=float32), 'output': '오늘은 무슨요일', 'segment_pos': [3]}
'''
```

- `epochs : 100` 설정 결과 정확도가 보다 향상한 것을 볼 수 있다.

```python
print(spacing_agent('학교종이땡땡땡'))
print(spacing_agent('오늘은무슨요일'))

'''
{'input': '학교종이땡땡땡', 'label': ['I', 'E', 'B', 'E', 'B', 'I', 'E'], 'sequence_score': array([9.697083], dtype=float32), 'output': '학교 종이 땡땡땡', 'segment_pos': [2, 4]}

{'input': '오늘은무슨요일', 'label': ['B', 'I', 'E', 'B', 'I', 'I', 'E'], 'sequence_score': array([18.412191], dtype=float32), 'output': '오늘은 무슨요일', 'segment_pos': [0, 3]}
'''
```

## Code

- train() 학습 단계에서 multiprocessing 을 사용하게 되는데, windows 환경일 경우 freeze_support() 호출이 필요하다.

```python
from takos.agents.word_segment import WordSegmentAgent
from multiprocessing import freeze_support

if __name__=='__main__':
    freeze_support() # for multiprocessing other process on windows

    spacing_agent = WordSegmentAgent('./scripts/my_configs.json')

	spacing_agent.train()

    spacing_agent.eval()

    print(spacing_agent('학교종이땡땡땡'))
    print(spacing_agent('오늘은무슨요일'))
```
