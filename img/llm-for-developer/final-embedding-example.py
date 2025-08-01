import numpy as np

# --- 1. Positional Encoding이란 무엇인가? ---
# LLM (Large Language Model)의 핵심 구조인 Transformer 모델은
# 문장을 한 번에 처리합니다 (병렬 처리).
# 이는 RNN(Recurrent Neural Network)처럼 순차적으로 단어를 하나씩 처리하는 방식과 다릅니다.
#
# 문제점: 순차 처리가 아니므로, "나는 학교에 간다"와 "학교에 나는 간다"처럼
#        단어의 순서가 바뀌면 문장의 의미가 달라지는데, Transformer는 이 순서 정보를 모릅니다.
#
# 해결책: Positional Encoding (위치 인코딩)을 사용하여
#        단어의 '의미 임베딩'에 '위치 정보'를 더해줍니다.
#        이렇게 하면 모델은 각 단어가 문장에서 어디에 위치하는지 알 수 있게 됩니다.

# --- 2. Positional Encoding의 작동 원리 (사인/코사인 함수 기반) ---
# Transformer 논문("Attention Is All You Need")에서 제안된 방식은
# 사인(sin) 함수와 코사인(cos) 함수를 사용합니다.
#
# 주요 아이디어:
# 1. 각 위치(pos)와 임베딩 차원(i)마다 고유한 값을 생성합니다.
# 2. 서로 다른 주파수(frequency)를 사용하여, 다양한 길이의 문장에서
#    단어의 절대적인 위치뿐만 아니라 상대적인 위치까지 파악할 수 있게 합니다.
#    - 저차원(낮은 i): 파장이 길어 넓은 범위의 위치 정보 인코딩
#    - 고차원(높은 i): 파장이 짧아 근접한 위치 정보 인코딩
#
# 수식:
# PE(pos, 2i)   = sin(pos / 10000^(2i / d_model))  # 짝수 인덱스 차원
# PE(pos, 2i+1) = cos(pos / 10000^(2i / d_model))  # 홀수 인덱스 차원
#
# - pos: 현재 단어의 시퀀스 내 위치 (0부터 시작)
# - i: 임베딩 벡터의 차원 인덱스 (0부터 d_model-1까지)
# - d_model: 임베딩 벡터의 총 차원 수 (모델의 '크기'를 나타냄)
# - 10000: 스케일링을 위한 상수 (다양한 주파수를 만들기 위함)

# --- 3. Positional Encoding 생성 함수 구현 ---

def generate_positional_encoding(max_seq_len, d_model):
    """
    주어진 최대 시퀀스 길이와 임베딩 차원에 대한 Positional Encoding 행렬을 생성합니다.

    Args:
        max_seq_len (int): 시퀀스(문장)의 최대 길이.
                           예: 20이면 0부터 19까지의 위치를 인코딩.
        d_model (int): 단어 임베딩 벡터의 차원.
                       예: 512이면 각 단어가 512차원 벡터로 표현됨.

    Returns:
        np.ndarray: (max_seq_len, d_model) 형태의 Positional Encoding 행렬.
                    각 행은 특정 위치의 인코딩 벡터를, 각 열은 특정 차원의 값을 나타냅니다.
    """
    # 0으로 채워진 (max_seq_len, d_model) 크기의 행렬을 만듭니다.
    # 여기에 위치 인코딩 값을 채워 넣을 것입니다.
    pe = np.zeros((max_seq_len, d_model))

    # 'position'은 각 단어의 위치를 나타냅니다.
    # np.arange(max_seq_len)은 [0, 1, 2, ..., max_seq_len-1] 배열을 만듭니다.
    # [:, np.newaxis]를 통해 (max_seq_len, 1) 형태로 변환하여 브로드캐스팅에 용이하게 합니다.
    position = np.arange(max_seq_len)[:, np.newaxis] # 예: [[0], [1], ..., [19]]

    # 'div_term'은 주파수(frequency)를 조절하는 부분입니다.
    # d_model의 짝수 인덱스(0, 2, 4, ...)에 대해 계산됩니다.
    # np.exp(val)은 e^val과 같고, np.log(10000.0)은 ln(10000)입니다.
    # 2i / d_model 부분이 차원별 주파수 조절에 사용됩니다.
    # 결과적으로 각 차원 인덱스 i에 따라 1 / (10000^(2i/d_model)) 값이 됩니다.
    div_term = np.exp(np.arange(0, d_model, 2) * -(np.log(10000.0) / d_model))
    # 예: d_model=4일 때, div_term은 [e^(0 * ...), e^(2 * ...)] = [1, 1/100] (대략)

    # 짝수 차원 인덱스 (0, 2, 4, ...)에는 사인 함수 적용
    # pe[:, 0::2]는 pe 행렬의 모든 행과 짝수 열을 선택합니다.
    # position (max_seq_len, 1) * div_term (d_model/2,) => 브로드캐스팅되어 (max_seq_len, d_model/2) 계산
    pe[:, 0::2] = np.sin(position * div_term)

    # 홀수 차원 인덱스 (1, 3, 5, ...)에는 코사인 함수 적용
    # pe[:, 1::2]는 pe 행렬의 모든 행과 홀수 열을 선택합니다.
    pe[:, 1::2] = np.cos(position * div_term)

    return pe

# --- 4. Positional Encoding 사용 예시 ---

# 모델의 임베딩 차원 (일반적으로 512, 768, 1024 등)
D_MODEL = 512
# 처리할 문장의 최대 길이 (Transformer 모델이 받아들일 수 있는 최대 토큰 수)
MAX_SEQUENCE_LENGTH = 50

print(f"--- Positional Encoding 생성 시작 ---")
print(f"임베딩 차원 (d_model): {D_MODEL}")
print(f"최대 시퀀스 길이 (max_seq_len): {MAX_SEQUENCE_LENGTH}\n")

# Positional Encoding 행렬 생성
positional_encodings = generate_positional_encoding(MAX_SEQUENCE_LENGTH, D_MODEL)

print(f"생성된 Positional Encoding 행렬의 형태: {positional_encodings.shape}")
print(f"첫 5개 위치에 대한 Positional Encoding의 첫 5개 차원 값:")
# 보기 쉽게 소수점 4자리까지 출력
print(np.round(positional_encodings[:5, :5], 4))

print("\n--- Positional Encoding의 역할 ---")
print("1. 각 단어는 고유한 '단어 임베딩' 벡터를 가집니다. (예: '고양이'라는 단어는 고양이의 의미를 담은 벡터)")
# 가상의 단어 임베딩 예시 (실제로는 모델이 학습합니다)
word_embedding_cat = np.random.rand(D_MODEL) # '고양이'의 512차원 임베딩
word_embedding_dog = np.random.rand(D_MODEL) # '강아지'의 512차원 임베딩

print(f"\n예시: '고양이' 단어의 임베딩 (일부): {np.round(word_embedding_cat[:5], 4)}...")

print("\n2. 이 '단어 임베딩'에 해당 단어의 '위치 임베딩'(Positional Encoding)을 더해줍니다.")
print("   이 덧셈은 단순히 벡터의 각 원소끼리 더하는 방식입니다.")

# 문장 예시: "고양이가 소파 위에서 잠을 잡니다."
# '고양이'는 0번째 위치, '잠을'은 4번째 위치라고 가정합니다.

# '고양이'의 최종 임베딩 = '고양이' 단어 임베딩 + 0번째 위치의 Positional Encoding
cat_final_embedding = word_embedding_cat + positional_encodings[0]

# '잠을'의 최종 임베딩 = '잠을' 단어 임베딩 + 4번째 위치의 Positional Encoding
sleep_final_embedding = np.random.rand(D_MODEL) + positional_encodings[4] # '잠을' 단어 임베딩도 가상으로 생성

print(f"\n'고양이' (0번째 위치)의 최종 임베딩 (일부): {np.round(cat_final_embedding[:5], 4)}...")
print(f"'잠을' (4번째 위치)의 최종 임베딩 (일부): {np.round(sleep_final_embedding[:5], 4)}...")

print("\n--- Positional Encoding의 결과 ---")
print("이렇게 위치 정보가 더해진 임베딩 벡터가 Transformer 모델의 입력으로 들어갑니다.")
print("이를 통해 Transformer는 단어의 의미뿐만 아니라, 단어들이 문장 내에서 어떤 순서로")
print("배치되어 있는지도 파악할 수 있게 되어, 문맥을 정확히 이해하고 복잡한 언어 작업을 수행합니다.")
print("\n예시로, '나는 사과를 먹는다'와 '내가 먹는 사과는 맛있다'에서 '사과'는 의미는 같지만")
print("문장 내 역할(위치)이 다르므로, Positional Encoding을 통해 다른 최종 임베딩을 갖게 됩니다.")