# 궁금해서 살짝(?) 파본 LLM 내부 동작 원리 

해당 글은 백엔드 개발자의 관점에서 작성된 글로, LLM 전문가의 시각과는 다를 수 있습니다.  
내용 중 오류를 발견하시면 언제든 피드백 부탁드립니다.

## LLM 내부 동작 원리 6단계

- 1단계: Tokenization
- 2단계: 임베딩 (Embedding) 의미 부여하기
- 3단계: 위치 인코딩 (Positional Encoding) 순서 기억하기
- 4단계: 트랜스포머 - 어텐션 (Attention) 핵심 파악하기
- 5단계: 다음 토큰 예측 (Prediction) 단어 생성하기
- 6단계: 디코딩 및 반복 (Decoding & Loop) 문장 완성하기 🔄

## 1단계: Tokenization

사용자의 프롬프트를 LLM이 이해할 수 있는 최소 단위인 **토큰(Token)** 으로 분리하는 단계입니다. 이 단계는 JSON 문자열을 파싱하여 객체로 만드는 것과 비슷합니다.
- 예를 들어, "LLM의 원리"라는 문장은 `['LLM', '의', ' ', '원리']` 와 같이 의미 있는 단위로 쪼개집니다.
- 각 토큰은 고유한 정수 ID에 매핑됩니다. (예: `[73, 12, 5, 96]`)

✅ **토큰화(Tokenization)**

토큰화는 LLM이 문장을 이해하기 위해 **의미 있는 최소 단위로 쪼개는 과정**입니다. 마치 문장을 읽을 때 단어 단위로 끊어 읽는 것과 같습니다. 하지만 LLM은 사람과 달라서, 사전에 없는 단어가 나오면 그 단어를 **더 작은 단위로 쪼개서 이해**합니다.
- 예를 들어 "인공지능"이라는 단어는 학습 데이터에 따라 "인공"과 "지능"이라는 두 개의 토큰으로 나뉠 수 있습니다.

Gemini의 경우 토큰화를 위해 [SentencePiece](https://github.com/google/sentencepiece)라는 tokenizer를 사용하고 있습니다. `SentencePiece`가 기존 tokenizer들과 다른 몇 가지의 중요한 차이점과 토큰이 고유한 정수 ID에 매핑되는 과정을 재현해 보겠습니다.
- **언어 중립적**: 공백을 기준으로 단어를 나누지 않고, 문장을 유니코드 문자들의 연속으로 보고, 가장 자주 함께 등장하는 글자들을 통계적으로 묶어 하나의 토큰으로 생성
  - 한국어처럼 조사가 단어에 붙어 쓰이거나 공백의 의미가 크지 않은 언어를 처리하는 데 매우 효과적
- **Subword 기반**: 어휘 사전에 없는 새로운 단어나 긴 단어가 나오면, 이를 더 작은 의미 단위(Subword)로 분해

```python
# pip install sentencepiece
import sentencepiece as spm

# --- 훈련 데이터 준비 ---
training_data = """제미나이는 구글이 개발한 인공지능 언어 모델입니다.
LLM 내부 동작 원리가 궁금하지 않나요?
LLM 내부 동작 원리를 함께 알아보아요.
"""

with open("sample-data.txt", "w", encoding="utf-8") as f:
    f.write(training_data)

# --- 1. 모델 훈련 ---
# SentencePiece 모델 훈련
# 'sample_tokenizer.model', 'sample_tokenizer.vocab' 파일이 생성
spm.SentencePieceTrainer.train(
    '--input=sample-data.txt ' # 훈련시킬 데이터 파일
    '--model_prefix=sample_tokenizer ' # 생성될 모델 파일의 이름
    '--vocab_size=133 ' # 단어 집합의 크기 (어휘의 총 개수)
    '--model_type=bpe' # 모델 타입 (bpe, unigram ..)
)

# --- 2. 훈련된 모델로 토큰화 실행 ---
sp = spm.SentencePieceProcessor()

# 훈련된 모델 파일 로드
sp.load('sample_tokenizer.model')

# 테스트할 문장
sentence1 = "나는 LLM 내부 동작 원리를 공부한다."

# 문장을 토큰으로 분리
# _(언더바)기호는 띄어쓰기(공백)를 의미
tokens1 = sp.encode_as_pieces(sentence1)
print(tokens1) # ['▁', '나', '는', '▁LLM', '▁내부', '▁동작', '▁원리를', '▁', '공', '부', '한', '다', '.']

# 문장을 숫자 ID로 변환
ids1 = sp.encode_as_ids(sentence1)
print(ids1) # [89, 93, 113, 11, 8, 9, 44, 89, 107, 97, 131, 116, 91]

# 숫자 ID를 다시 문장으로 복원
decoded_sentence = sp.decode_ids(ids1)
print(decoded_sentence) # 나는 LLM 내부 동작 원리를 공부한다.
```

`SentencePieceTrainer.train()` 함수에서는 훈련 데이터 파일의 내용을 통계적으로 분석하여, 어떤 글자들을 한 묶음(토큰)으로 만들지 결정하고, 그 규칙을 분해 규칙 파일(`.model`)에 저장합니다. 어휘 사전 파일(`.vocab`)에는 생성된 토큰 목록과 그 점수가 저장됩니다.

```text
...
개발	-49
구글	-50
궁금	-51
내부	-52
동작	-53
리가	-54
리를	-55
...
```

이처럼 LLM이 실시간으로 요청이 들어올 때마다 사용자 입력을 훈련하지는 않고, 이 과정은 `훈련`(Training)과 `추론`(Inference)이라는 두 단계로 명확하게 나뉘어 동작합니다.

**Training Phase**
* 인공지능 언어 모델 개발사(Google, OpenAI 등)가 **단 한 번, 대규모로 수행하는 과정**
* 이 단계에서 수십 테라바이트에 달하는 방대한 텍스트 데이터로 토크나이저를 학습시켜 **완성된 'vocabulary(.vocab)'와 '모델(.model)'** 을 생성

**Inference Phase**
* **사용자가 질문을 할 때마다 일어나는** 동작
* LLM은 이미 만들어진 고정된 tokenizer를 **단순히 메모리에 로드**해서 사용
* 사용자의 문장을 이 규칙에 따라 즉시 토큰으로 변환하고 응답을 생성

✋🏼 **모델 타입? BPE?, ULM?**

`BPE`(Byte Pair Encoding)가 빈도수가 높은 문자 쌍을 병합하는 'Bottom-up' 방식이라면, `ULM`(Unigram Language Model)은 반대로 전체 단어에서 시작하여 중요도가 낮은 서브워드를 제거하는 'Top-down' 방식에 가깝습니다.

1️⃣ BPE 기본 원리
- 텍스트 내의 모든 개별 문자를 **초기 어휘**로 설정
- 현재 어휘로 구성된 텍스트에서 가장 빈번하게 함께 나타나는 **문자 쌍 찾기**
- 가장 빈번한 문자 쌍을 하나의 새로운 **서브워드 토큰으로 병합**(예: "l o w"에서 'o'와 'w'가 자주 나타나면 'ow'로 병합)
- 새로운 토큰을 어휘에 추가하고, 미리 정의된 어휘 크기에 도달하거나 더 이상 병합할 유의미한 쌍이 없을 때까지 **2단계와 3단계를 반복**

이 과정을 통해 자주 나타나는 문자 조합은 긴 서브워드가 되고, 희귀한 단어나 새로운 단어는 여러 개의 짧은 서브워드로 분리됩니다.

2️⃣ ULM 기본 원리
- 매우 큰 초기 **어휘 집합** 생성
- 각 서브워드 토큰이 텍스트에 나타날 확률을 계산(해당 서브워드의 빈도수 기반)
- 주어진 단어를 여러 서브워드로 분절할 수 있는 모든 가능한 경우의 수를 고려
  - "unigram"이라는 단어가 있다고 가정했을 때, un + igram, uni + gram, u + ni + gram 등 다양한 분절이 가능
  - 이 중 어떤 분절 조합이 가장 높은 확률을 가지는지 계산하여 선택
- 어휘 집합의 크기를 줄여나가는 과정
  - 전체 확률 분포에 미치는 영향이 가장 작은 서브워드를 제거
  - 이 과정을 미리 정해둔 vocab_size에 도달할 때까지 반복
