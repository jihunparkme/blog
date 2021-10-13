# Subword Tokenizer

## 개요

자연어처리 모델을 훈련할 때 tokenizing 된 단어의 개수(단어사전, vocab)는 모델 성능에 다양한 영향을 미치게 된다.

여기서 특히 OOV 문제는 굉장한 이슈거리이다.

**OOV(Out-Of-Vocabulary) 또는 UNK(Unknown Token)**

- 기계가 모르는 단어로 인해 문제를 푸는 것이 까다로워지는 상황 -> **OOV 문제**
- 특히 한국어의 경우 형태소 분석기를 많이 사용하는데, OOV 문제 해결을 위해 사용자 단어 사전을 만들어주기도 한다. -> 하지만, 엄청난 노가다 작업이 필요하다.

이러한 상황들을 해결하기 위해 나온 것이 `Subword Segmentation`

- 서브워드 분리 작업은 하나의 단어는 더 작은 단위의 의미있는 여러 subword(책+가방)의 조합으로 구성되는 경우가 많으므로, 하나의 단어를 여러 subword로 분리해서 단어를 인코딩/임베딩 하겠다는 의도의 전처리 작업.
- 여기서도 단어 집합의 크기를 잘 설정해주는 것이 중요.

- 최근 자연어처리 모델 Transformer, BERT 등에서 Subword Segmentation 방식을 사용.

## BPE(Byte Pair Encoding)

- VOO 문제를 완화하는 대표적인 subword segmenation 알고리즘
- BPE은 기본적으로 연속적으로 가장 많이 등장한 글자의 쌍을 찾아서 하나의 글자로 병합하는 방식을 수행

```
aaabdaaabac
ZabdZabac
ZYdZYac
XdXac
===============
X=ZY(aaab)
Y=ab
Z=aa
```

기존에는 단어의 빈도수로 이루어진 단어 집합을 활용하였다면...

BPE 알고리즘을 적용할 경우

1. 모든 단어들을 글자 단위로 분리

   ```python
   # dictionary
   l o w : 5,  l o w e r : 2,  n e w e s t : 6,  w i d e s t : 3
   # vocabulary
   l, o, w, e, r, n, w, s, t, i, d
   ```

   - 알고리즘 동작 횟수 설정

2. 가장 빈도수가 높은 유니그램의 쌍을 하나의 유니그램으로 통합하는 과정 반복

   - 1회 - dictionary 에서 빈도수가 9로 가장 높은 (e, s) 쌍을 es 로 통합

     ```python
     # dictionary
     l o w : 5
     l o w e r : 2
     n e w (e s) t : 6
     w i d (e s) t : 3
     # vocabulary
     l, o, w, e, r, n, w, s, t, i, d, es
     ```

   - 2회 - 빈도수가 9로 가장 높은 (es, t)의 쌍을 est로 통합

     ```python
     # dictionary
     l o w : 5
     l o w e r : 2
     n e w (es t) : 6
     w i d (es t) : 3
     # vocabulary
     l, o, w, e, r, n, w, s, t, i, d, es, est
     ```

   - 3회 - 빈도수가 7로 가장 높은 (l, o)의 쌍을 lo로 통합

     ```python
     # dictionary
     (l o) w : 5
     (l o) w e r : 2
     n e w est : 6
     w i d est : 3
     # vocabulary
     l, o, w, e, r, n, w, s, t, i, d, es, est, lo
     ```

   - ...

   - 10회

     ```python
     # dictionary update!
     low : 5,
     low e r : 2,
     newest : 6,
     widest : 3
     # vocabulary update!
     l, o, w, e, r, n, w, s, t, i, d, es, est, lo, low, ne, new, newest, wi, wid, widest
     ```

3. `lowest`란 단어가 등장할 경우

   - 기존 방식이라면 OOV 에 포함되겠지만

   - 단어를 글자 단위로 분할 `l,o,w,e,s,t`
   - 단어 집합을 참고하여 `low`와 `est` 조회
   - `lowest`를 `low`와 `est`두 단어로 인코딩

## Wordpiece Model (WPM)

- BPE의 변형 알고리즘
- 최신 딥 러닝 모델 BERT를 훈련하기 위해서 사용되기도 함.
- 병합되었을 때 코퍼스의 우도(Likelihood)를 가장 높이는 쌍을 병합 (Likelihood, 가능도 : 지금 얻은 데이터가 이 분포로부터 나왔을 가능도)
  - 띄어쓰기 : subwords를 구분하는 구분자
  - \_(언더바) : 실제 문장의 띄어쓰기

```text
WPM을 수행하기 이전의 문장: Jet makers feud over seat width with big orders at stake
WPM을 수행한 결과(wordpieces): _J et _makers _fe ud _over _seat _width _with _big _orders _at _stake
```

## SentencePiece

<https://github.com/google/sentencepiece>

- 빈도수를 기반으로 BPE를 수행
  - BPE를 포함하여 기타 서브워드 토크나이징 알고리즘들을 내장
- 사전 토큰화 작업없이 단어 분리 토큰화를 수행하므로 언어에 종속되지 않음.

```shell
spm_train --input=<input> --model_prefix=<model_name> --vocab_size=5000 --character_coverage=1.0 --model_type=<type>
```

- `--input` : 학습시킬 파일
- `--model_prefix` : 만들어질 모델 이름
- `--vocab_size` : 단어 집합의 크기
- `--model_type` : 사용할 모델 (unigram(default), bpe, char, word)
- `--max_sentence_length`: 문장의 최대 길이
- `--pad_id, --pad_piece`: pad token id, 값
- `--unk_id, --unk_piece`: unknown token id, 값
- `--bos_id, --bos_piece`: begin of sentence token id, 값
- `--eos_id, --eos_piece`: end of sequence token id, 값
- `--user_defined_symbols`: 사용자 정의 토큰

```shell
spm_train --input=/opt/train/news.en,/opt/train/news.ko --model_type=bpe --vocab_size=25000 --model_prefix=spm --character_coverage=1
```

결과로 생성되는 .vocab 파일에서 학습된 32000개의 subword 확인

- \_(언더바) : 실제 문장의 띄어쓰기

```text
<unk>	0
<s>	0
</s>	0
▁t	-0
▁a	-1
he	-2
in	-3
on	-4
▁the	-5
re	-6
▁o	-7
er	-8
▁s	-9
at	-10
▁c	-11
en	-12
▁p	-13
or	-14
ed	-15
▁of	-16
ion	-17
▁m	-18
ing	-19
is	-20
▁an	-21
▁in	-22
▁f	-23
it	-24
...
```

.model 파일에서 단어 시퀀스를 확인

```text
<unk>
<s>
</s>
▁t▒
▁a▒▒
he▒
in@▒
on▒▒
▁the▒▒
re▒▒
▁o▒▒
er▒
▁s▒
at ▒
▁c0▒
en@▒
▁pP▒
or`▒
edp▒
▁of▒▒
ion▒▒
▁m▒▒
...
```

---

## Reference

> <https://wikidocs.net/22592>
>
> <https://github.com/google/sentencepiece>
>
> <https://lsjsj92.tistory.com/600>
