# Text preprocessing

COMMIT

유원준님의 "[딥 러닝을 이용한 자연어 처리 입문](https://wikidocs.net/book/2155)" 책을 (제가 보기 편하게) 간략히 정리한 글입니다.

# Table Of Contents

- [Tokenization](#Tokenization)
  - Word Tokenization
  - Sentence Tokenization
  - 한국어 토큰화
  - 품사 태깅(part-of-speech tagging)
- [Cleaning and Normalization](#Cleaning-and-Normalization)
  - 표제어 추출 & 어간 추출
  - Stopword
  - Regular Expression
- [Splitting Data](#Splitting-Data)
- [Text Preprocessing Tools for Korean Text](#Text-Preprocessing-Tools-for-Korean-Text)

# Tokenization

## Word Tokenization

```shell
pip install nltk
```

- `Do`, `n't`

```python
from nltk.tokenize import word_tokenize

print(word_tokenize("Don't be fooled by the dark sounding name, Mr. Jone's Orphanage is as cheery as cheery goes for a pastry shop."))
# ['Do', "n't", 'be', 'fooled', 'by', 'the', 'dark', 'sounding', 'name', ',', 'Mr.', 'Jone', "'s", 'Orphanage', 'is', 'as', 'cheery', 'as', 'cheery', 'goes', 'for', 'a', 'pastry', 'shop', '.']
```

- `Don`, `'`, `t`

```python
from nltk.tokenize import WordPunctTokenizer

print(WordPunctTokenizer().tokenize("Don't be fooled by the dark sounding name, Mr. Jone's Orphanage is as cheery as cheery goes for a pastry shop."))
# ['Don', "'", 't', 'be', 'fooled', 'by', 'the', 'dark', 'sounding', 'name', ',', 'Mr', '.', 'Jone', "'", 's', 'Orphanage', 'is', 'as', 'cheery', 'as', 'cheery', 'goes', 'for', 'a', 'pastry', 'shop', '.']
```

- `don't`

```python
from tensorflow.keras.preprocessing.text import text_to_word_sequence

print(text_to_word_sequence("Don't be fooled by the dark sounding name, Mr. Jone's Orphanage is as cheery as cheery goes for a pastry shop."))
# ["don't", 'be', 'fooled', 'by', 'the', 'dark', 'sounding', 'name', 'mr', "jone's", 'orphanage', 'is', 'as', 'cheery', 'as', 'cheery', 'goes', 'for', 'a', 'pastry', 'shop']
```

- `does`, `n't`

```python
from nltk.tokenize import TreebankWordTokenizer
tokenizer=TreebankWordTokenizer()
text="Starting a home-based restaurant may be an ideal. it doesn't have a food chain or restaurant of their own."

print(tokenizer.tokenize(text))
# ['Starting', 'a', 'home-based', 'restaurant', 'may', 'be', 'an', 'ideal.', 'it', 'does', "n't", 'have', 'a', 'food', 'chain', 'or', 'restaurant', 'of', 'their', 'own', '.']
```

## **Sentence Tokenization**

**en.**

- [영어 약어 사전](https://public.oed.com/how-to-use-the-oed/abbreviations/)
- [문장 토큰화 규칙 예외사항](https://www.grammarly.com/blog/engineering/how-to-split-sentences/)
- 문장 토큰화 수행 오픈소스 : `NLTK`, `OpenNLP`, `스탠포드 CoreNLP`, `splitta`, `LingPipe` 등

```python
from nltk.tokenize import sent_tokenize
text="His barber kept his word. But keeping such a huge secret to himself was driving him crazy. Finally, the barber went up a mountain and almost to the edge of a cliff. He dug a hole in the midst of some reeds. He looked about, to make sure no one was near."

print(sent_tokenize(text))
# ['His barber kept his word.', 'But keeping such a huge secret to himself was driving him crazy.', 'Finally, the barber went up a mountain and almost to the edge of a cliff.', 'He dug a hole in the midst of some reeds.', 'He looked about, to make sure no one was near.']
```

**kor.**

```shell
pip install kss
```

```python
import kss

text='딥 러닝 자연어 처리가 재미있기는 합니다. 그런데 문제는 영어보다 한국어로 할 때 너무 어려워요. 농담아니에요. 이제 해보면 알걸요?'

print(kss.split_sentences(text))
# ['딥 러닝 자연어 처리가 재미있기는 합니다.', '그런데 문제는 영어보다 한국어로 할 때 너무 어려워요.', '농담아니에요.', '이제 해보면 알걸요?']
```

## 한국어 토큰화

**한국어는 교착어**

- 조사( '그가', '그에게', '그를'...), 형태소(뜻을 가진 가장 작은 말의 단위) 분리 필요
- 형태소
  - **자립 형태소** : 접사, 어미, 조사와 상관없이 자립하여 사용할 수 있는 형태소. 그 자체로 단어. 체언(명사, 대명사, 수사), 수식언(관형사, 부사), 감탄사 등 / ex. 에디, 딥러닝책
    **의존 형태소** : 다른 형태소와 결합하여 사용되는 형태소. 접사, 어미, 조사, 어간 / ex. -가, -을, 읽-, -었, -다

**잘 지켜지지 않는 띄어쓰기**

## 품사 태깅(part-of-speech tagging)

각 단어가 어떤 품사로 쓰였는지를 구분

**en.**

- [품사 태그 정보](https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html)

```python
from nltk.tokenize import word_tokenize
text="I am actively looking for Ph.D. students. and you are a Ph.D. student."

print(word_tokenize(text))
# ['I', 'am', 'actively', 'looking', 'for', 'Ph.D.', 'students', '.', 'and', 'you', 'are', 'a', 'Ph.D.', 'student', '.']

from nltk.tag import pos_tag
x=word_tokenize(text)

pos_tag(x)
# [('I', 'PRP'), ('am', 'VBP'), ('actively', 'RB'), ('looking', 'VBG'), ('for', 'IN'), ('Ph.D.', 'NNP'), ('students', 'NNS'), ('.', '.'), ('and', 'CC'), ('you', 'PRP'), ('are', 'VBP'), ('a', 'DT'), ('Ph.D.', 'NNP'), ('student', 'NN'), ('.', '.')]
```

**kor.**

- KoNLPy : Okt(Open Korea Text), 메캅(Mecab), 코모란(Komoran), 한나눔(Hannanum), 꼬꼬마(Kkma)
- [한국어 형태소 분석기 성능 비교](https://iostream.tistory.com/144)
- [KoNLPy 형태소](https://cceeddcc.tistory.com/8)
- [Komoran 품사 정보](https://docs.komoran.kr/firststep/postypes.html)
- [한국어의 문장유형과 종결어미](https://ratsgo.github.io/korean%20linguistics/2017/10/24/ends/)

```shell
pip insatll KoNLPy
```

- Okt

```python
from konlpy.tag import Okt
okt=Okt()

print(okt.morphs("열심히 코딩한 당신, 연휴에는 여행을 가봐요")) # 형태소 추출
#['열심히', '코딩', '한', '당신', ',', '연휴', '에는', '여행', '을', '가봐요']

print(okt.pos("열심히 코딩한 당신, 연휴에는 여행을 가봐요")) # 품사 태깅 (조사 기준 분리)
# [('열심히','Adverb'), ('코딩', 'Noun'), ('한', 'Josa'), ('당신', 'Noun'), (',', 'Punctuation'), ('연휴', 'Noun'), ('에는', 'Josa'), ('여행', 'Noun'), ('을', 'Josa'), ('가봐요', 'Verb')]

print(okt.nouns("열심히 코딩한 당신, 연휴에는 여행을 가봐요")) # 명사 추출
# ['코딩', '당신', '연휴', '여행']
```

- Kkma

```python
from konlpy.tag import Kkma
kkma=Kkma()

print(kkma.morphs("열심히 코딩한 당신, 연휴에는 여행을 가봐요"))
# ['열심히', '코딩', '하', 'ㄴ', '당신', ',', '연휴', '에', '는', '여행', '을', '가보', '아요']

print(kkma.pos("열심히 코딩한 당신, 연휴에는 여행을 가봐요"))
# [('열심히','MAG'), ('코딩', 'NNG'), ('하', 'XSV'), ('ㄴ', 'ETD'), ('당신', 'NP'), (',', 'SP'), ('연휴', 'NNG'), ('에', 'JKM'), ('는', 'JX'), ('여행', 'NNG'), ('을', 'JKO'), ('가보', 'VV'), ('아요', 'EFN')]

print(kkma.nouns("열심히 코딩한 당신, 연휴에는 여행을 가봐요"))
# ['코딩', '당신', '연휴', '여행']
```

# Cleaning and Normalization

- 정제(cleaning) : 갖고 있는 코퍼스로부터 노이즈 데이터를 제거
  - ex) 특수문자, 불용어, 빈도가 적은 단어, 짧은 길이의 단어(en)
  - 정규표현식
- 정규화(normalization) : 표현 방법이 다른 단어들을 통합시켜서 같은 단어로 수정
  - ex) 대, 소문자 통합 (고유 명사 제외)
  - 어간 추출 & 표제어 추출

```python
import re
text = "I was wondering if anyone out there could enlighten me on this car."
shortword = re.compile(r'\W*\b\w{1,2}\b') # 길이가 1~2인 단어들을 정규 표현식을 이용하여 삭제

print(shortword.sub('', text))
# was wondering anyone out there could enlighten this car.
```

## 표제어 추출 & 어간 추출

**en.**

**표제어 추출(Lemmatization)** : 단어들이 다른 형태를 가지더라도, 그 뿌리 단어를 찾아가서 단어의 개수를 줄일 수 있는지 판단

- 표제어 추출은 문맥을 고려하며, 수행했을 때의 결과는 해당 단어의 품사 정보를 보존
- 반면, 어간 추출은 품사 정보가 보존되지 않는 경우가 많음

- 형태학적 파싱은 어간과 접사를 분리하는 작업
  - 어간(stem) : 단어의 의미를 담고 있는 단어의 핵심 부분
  - 접사(affix) : 단어에 추가적인 의미를 주는 부분

```python
from nltk.stem import WordNetLemmatizer
n=WordNetLemmatizer()
words=['policy', 'doing', 'organization', 'have', 'going', 'love', 'lives', 'fly', 'dies', 'watched', 'has', 'starting']

print([n.lemmatize(w) for w in words])
# ['policy', 'doing', 'organization', 'have', 'going', 'love', 'life', 'fly', 'dy', 'watched', 'ha', 'starting']
```

- 본래 단어의 품사 정보를 알아야만 정확한 결과를 얻을 수 있음
  - 품사의 정보를 보존하면 정확한 표제어를 출력

```python
n.lemmatize('dies', 'v')
# 'die'

n.lemmatize('watched', 'v')
# 'watch'

n.lemmatize('has', 'v')
# 'have'
```

**어간 추출(stemming)** : 어간을 추출하는 작업, 정해진 규칙만 보고 단어의 어미를 자르는 어림짐작의 작업

```python
### PorterStemmer
from nltk.stem import PorterStemmer
s=PorterStemmer()
words=['policy', 'doing', 'organization', 'have', 'going', 'love', 'lives', 'fly', 'dies', 'watched', 'has', 'starting']

print([s.stem(w) for w in words])
# ['polici', 'do', 'organ', 'have', 'go', 'love', 'live', 'fli', 'die', 'watch', 'ha', 'start']


### LancasterStemmer
from nltk.stem import LancasterStemmer
l=LancasterStemmer()
words=['policy', 'doing', 'organization', 'have', 'going', 'love', 'lives', 'fly', 'dies', 'watched', 'has', 'starting']

print([l.stem(w) for w in words])
# ['policy', 'doing', 'org', 'hav', 'going', 'lov', 'liv', 'fly', 'die', 'watch', 'has', 'start']
```

**kor.**

용언에 해당되는 '동사'와 '형용사'는 어간(stem)과 어미(ending)의 결합으로 구성

- **활용(conjugation)** : 용언의 어간(stem)이 어미(ending)를 가지는 일
  - **어간(stem)** : 용언(동사, 형용사)을 활용할 때, 원칙적으로 모양이 변하지 않는 부분 / ex. 긋다, 긋고, 그어서, 그어라
  - **어미(ending)** : 용언의 어간 뒤에 붙어서 활용하면서 변하는 부분이며, 여러 문법적 기능을 수행
- 규칙 활용
- [불규칙 활용](https://namu.wiki/w/%ED%95%9C%EA%B5%AD%EC%96%B4/%EB%B6%88%EA%B7%9C%EC%B9%99%20%ED%99%9C%EC%9A%A9)

## Stopword

**en.**

```python
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize

example = "Family is not an important thing. It's everything."
stop_words = set(stopwords.words('english'))

word_tokens = word_tokenize(example)

result = []
for w in word_tokens:
    if w not in stop_words:
        result.append(w)

print(word_tokens)
# ['Family', 'is', 'not', 'an', 'important', 'thing', '.', 'It', "'s", 'everything', '.']

print(result)
# ['Family', 'important', 'thing', '.', 'It', "'s", 'everything', '.']
```

**kor.**

- [Korean Stopwords](https://www.ranks.nl/stopwords/korean)
- 보통 txt 파일이나 csv 파일로 불용어 정리 후 사용

```python
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize

example = "고기를 아무렇게나 구우려고 하면 안 돼. 고기라고 다 같은 게 아니거든. 예컨대 삼겹살을 구울 때는 중요한 게 있지."
stop_words = "아무거나 아무렇게나 어찌하든지 같다 비슷하다 예컨대 이럴정도로 하면 아니거든"
# 불용어 정의 example

stop_words=stop_words.split(' ')
word_tokens = word_tokenize(example)

result = []
for w in word_tokens:
    if w not in stop_words:
        result.append(w)
# result=[word for word in word_tokens if not word in stop_words]

print(word_tokens)
# ['고기를', '아무렇게나', '구우려고', '하면', '안', '돼', '.', '고기라고', '다', '같은', '게', '아니거든', '.', '예컨대', '삼겹살을', '구울', '때는', '중요한', '게', '있지', '.']

print(result)
# ['고기를', '구우려고', '안', '돼', '.', '고기라고', '다', '같은', '게', '.', '삼겹살을', '구울', '때는', '중요한', '게', '있지', '.']
```

## Regular Expression

<https://wikidocs.net/21703>

# Integer Encoding

- 빈도수가 가장 높은 n개의 단어만 사용하고 싶을 경우
- 보통 전처리 또는 빈도수가 높은 단어들만 사용하기 위해서 단어에 대한 빈도수를 기준으로 정렬한 뒤에 부여
- Counter, FreqDist, enumerate, keras tokenizer

**Counter**

```python
from collections import Counter

text = "A barber is a person. a barber is good person. a barber is huge person. he Knew A Secret! The Secret He Kept is huge secret. Huge secret. His barber kept his word. a barber kept his word. His barber kept his secret. But keeping and keeping such a huge secret to himself was driving the barber crazy. the barber went up a huge mountain."

#################
### 문장 토큰화
text = sent_tokenize(text)
print(text)

#################
### 단어 집합 생성
words = sum(sentences, []) #  words = np.hstack(sentences)
print(words)

#################
### Counter 모듈 : 단어의 모든 빈도를 쉽게 계산
vocab = Counter(words)
print(vocab)
# Counter({'barber': 8, 'secret': 6, 'huge': 5, 'kept': 4, 'person': 3, 'word': 2, 'keeping': 2, 'good': 1, 'knew': 1, 'driving': 1, 'crazy': 1, 'went': 1, 'mountain': 1})

print(vocab["barber"])
# 8

#################
### 등장 빈도수가 높은 상위 N개의 단어만 저장
vocab_size = 5
vocab = vocab.most_common(vocab_size)
vocab
# [('barber', 8), ('secret', 6), ('huge', 5), ('kept', 4), ('person', 3)]

######################
### 높은 빈도수를 가진 단어에 낮은 정수 인덱스
word_to_index = {}
i = 0
for (word, frequency) in vocab :
    i = i+1
    word_to_index[word] = i
print(word_to_index)
# {'barber': 1, 'secret': 2, 'huge': 3, 'kept': 4, 'person': 5}
```

**NLTK의 FreqDist**

```python
from nltk import FreqDist
import numpy as np

#################
### 문장 구분 제거
vocab = FreqDist(np.hstack(sentences))

#################
### 단어 빈도수
print(vocab["barber"])  # 8

#################
### 등장 빈도수가 높은 상위 N개의 단어만 저장
vocab_size = 5
vocab = vocab.most_common(vocab_size)
vocab

#################
### 인덱스 부여
word_to_index = {word[0] : index + 1 for index, word in enumerate(vocab)}
print(word_to_index)
```

# Padding

병렬 연산을 위해서 여러 문장(문서)의 길이를 임의로 동일하게 맞춰주는 작업이 필요할 경우

데이터에 특정 값을 채워서 데이터의 크기(shape)를 조정하는 것

```python
from tensorflow.keras.preprocessing.sequence import pad_sequences

encoded = tokenizer.texts_to_sequences(sentences)
print(encoded)
# [[1, 5], [1, 8, 5], [1, 3, 5], [9, 2], [2, 4, 3, 2], [3, 2], [1, 4, 6], [1, 4, 6], [1, 4, 2], [7, 7, 3, 2, 10, 1, 11], [1, 12, 3, 13]]

### pad_sequences를 사용한 패딩 (뒤에 0을 채우고 싶다면 padding='post')
padded = pad_sequences(encoded, padding = 'post')
padded

# 문서 길이 제한 시 max_len 인자 사용 (패딩으로 다른 숫자 사용 시 value 인자)
padded = pad_sequences(encoded, padding = 'post', maxlen = 5)
padded
```

# One-Hot Encoding

- 단어 집합의 크기를 벡터의 차원으로 하고
  - 표현하고 싶은 단어의 인덱스에 1의 값을 부여하고
  - 다른 인덱스에는 0을 부여하는 단어의 벡터 표현 방식
- 원-핫 인코딩의 단점
  - 저장 공간 측면에서는 매우 비효율적
  - 단어의 유사도를 표현하지 못함
  - 해결 방법으로 단어의 잠재 의미를 반영하여 다차원 공간에 벡터화 하는 기법이 존재
    - 카운트 기반의 벡터화 방법인 LSA, HAL 등
    - 예측 기반으로 벡터화하는 NNLM, RNNLM, Word2Vec, FastText 등

```python
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.utils import to_categorical

text="나랑 점심 먹으러 갈래 점심 메뉴는 햄버거 갈래 갈래 햄버거 최고야"

t = Tokenizer()
t.fit_on_texts([text]) # 각 단어에 대한 인코딩
print(t.word_index)
# {'갈래': 1, '점심': 2, '햄버거': 3, '나랑': 4, '먹으러': 5, '메뉴는': 6, '최고야': 7}

###################
### 정수 시퀀스로 변환
sub_text="점심 먹으러 갈래 메뉴는 햄버거 최고야"
encoded=t.texts_to_sequences([sub_text])[0]
print(encoded)
# [2, 5, 1, 6, 3, 7]

###################
### 원-핫 인코딩
one_hot = to_categorical(encoded)
print(one_hot)
```

# Splitting Data

- 기계의 정확도(Accuracy) 측정
- x, y 분리
  - `zip function`, `dataframe`, `Numpy`
- 테스트 데이터 분리
  - ​ `from sklearn.model_selection import train_test_split`

https://wikidocs.net/33274

# Text Preprocessing Tools for Korean Text

## PyKoSpacing

한국어 띄어쓰기 패키지

```python
from pykospacing import Spacing
spacing = Spacing()
kospacing_sent = spacing(new_sent)
```

## Py-Hanspell

네이버 한글 맞춤법 검사기를 바탕으로 만들어진 패키지 + 띄어쓰기 보정

```python
from hanspell import spell_checker

spelled_sent = spell_checker.check(sent)
hanspell_sent = spelled_sent.checked
```

## SOYNLP

품사 태깅, 단어 토큰화, 반복 문자 정제 등을 지원하는 `학습 기반의 단어 토크나이저`

- 학습 과정에서 전체 코퍼스로부터 응집 확률과 브랜칭 엔트로피 단어 점수표를 만듦
  - 응집 확률(cohesion probability)은 내부 문자열(substring)이 얼마나 응집하여 자주 등장하는지를 판단하는 척도
  - 브랜칭 엔트로피(branching entropy)는 확률 분포의 엔트로피값을 사용, 주어진 문자열에서 얼마나 다음 문자가 등장할 수 있는지를 판단하는 척도

```python
import urllib.request
from soynlp import DoublespaceLineCorpus
from soynlp.word import WordExtractor

# 학습에 필요한 한국어 문서
urllib.request.urlretrieve("https://raw.githubusercontent.com/lovit/soynlp/master/tutorials/2016-10-20.txt", filename="2016-10-20.txt")

# 훈련 데이터를 다수의 문서로 분리
corpus = DoublespaceLineCorpus("2016-10-20.txt")
len(corpus) # 30091

# 학습 (전체 코퍼스로부터 응집 확률과 브랜칭 엔트로피 단어 점수표를 만드는 과정)
word_extractor = WordExtractor()
word_extractor.train(corpus)
word_score_table = word_extractor.extract() # 전체 코퍼스에 대해 단어 점수표를 계산
```

# source

[Won Joon Yoo, Introduction to Deep Learning for Natural Language Processing, Wikidocs](https://wikidocs.net/book/2155)

# recommend

[자연어 관련 도서 및 블로그 소개](https://hotorch.tistory.com/12)
