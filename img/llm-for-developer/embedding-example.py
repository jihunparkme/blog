import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.decomposition import PCA
from sklearn.manifold import TSNE
from sentence_transformers import SentenceTransformer

# 1. 모델 로드 (Hugging Face의 sentence-transformers 라이브러리 사용)
# 'all-MiniLM-L6-v2'는 작고 빠르면서도 좋은 성능을 보이는 범용 문장 임베딩 모델입니다.
try:
    model = SentenceTransformer('all-MiniLM-L6-v2')
    print("SentenceTransformer model loaded successfully.")
except Exception as e:
    print(f"Error loading model: {e}")
    print("Please check your internet connection or run 'pip install sentence-transformers'.")
    exit()

# 2. 예시 문장들 정의
# 한국어 문장과 그에 대응하는 영어 문장을 함께 정의하여 시각화에 활용합니다.
sentences_korean = [
    "cat",
    "kitten",
    "dog",
    "houses",
    "man",
    "women",
    "king",
    "queen"
]

sentences_english = [
    "cat",
    "kitten",
    "dog",
    "houses",
    "man",
    "women",
    "king",
    "queen"
]

# 모든 문장을 임베딩에 사용할 리스트로 합칩니다.
all_sentences = sentences_english

print("\n--- Generating Text Embeddings ---")
# 문장 임베딩 생성
# 각 문장이 고차원 벡터로 변환됩니다. (all-MiniLM-L6-v2는 384차원)
sentence_embeddings = model.encode(all_sentences)

print(f"Shape of generated embedding vectors: {sentence_embeddings.shape}")
print(f"First sentence's embedding vector (partial): {sentence_embeddings[0][:10]}...")

# 3. Semantic Similarity (Cosine Similarity) Calculation
# 코사인 유사도는 두 벡터가 가리키는 방향이 얼마나 유사한지를 측정합니다.
# 1에 가까울수록 유사하고, -1에 가까울수록 반대입니다.
cosine_sim_matrix = cosine_similarity(sentence_embeddings)

print("\n--- Cosine Similarity Matrix (partial) ---")
# 보기 좋게 소수점 2자리까지만 출력
print(np.round(cosine_sim_matrix[:5, :5], 2))

# 4. Visualization

# 폰트 설정 (영어 폰트는 기본적으로 지원되므로 별도 설정이 필요 없지만, 명시적으로 깨끗한 폰트 지정 가능)
plt.rcParams['font.family'] = 'DejaVu Sans' # 기본 영어 폰트
plt.rcParams['axes.unicode_minus'] = False # 마이너스 기호 깨짐 방지

# 4.1. Cosine Similarity Heatmap
plt.figure(figsize=(12, 10)) # 그래프 크기 약간 확장
sns.heatmap(cosine_sim_matrix, annot=True, cmap='viridis', fmt=".2f",
            xticklabels=all_sentences, yticklabels=all_sentences)
plt.title('Cosine Similarity Heatmap of Sentence Embeddings', fontsize=16)
plt.xticks(rotation=90, ha='right', fontsize=8) # 레이블이 겹치지 않게 회전
plt.yticks(rotation=0, fontsize=8)
plt.tight_layout()
plt.show()

print("\n--- Dimensionality Reduction and Visualization ---")

# 4.2. Dimensionality Reduction (PCA)
# 고차원 벡터를 2차원으로 축소하여 시각화합니다.
pca = PCA(n_components=2)
reduced_embeddings_pca = pca.fit_transform(sentence_embeddings)

plt.figure(figsize=(12, 10))
for i, (x, y) in enumerate(reduced_embeddings_pca):
    # 한국어 문장과 영어 문장을 함께 출력하여 그룹을 더 명확히 합니다.
    # 한국어/영어 문장을 따로 관리했기 때문에 인덱스에 따라 구분하여 출력합니다.
    label_text = all_sentences[i]
    plt.scatter(x, y)
    plt.annotate(label_text, (x, y), textcoords="offset points", xytext=(5, 5), ha='left', fontsize=9)

plt.title('PCA 2D Dimensionality Reduction Visualization of Sentence Embeddings', fontsize=16)
plt.xlabel('PCA Component 1')
plt.ylabel('PCA Component 2')
plt.grid(True, linestyle='--', alpha=0.6)
plt.tight_layout()
plt.show()


# 4.3. Dimensionality Reduction (t-SNE) - Optional
# t-SNE는 비선형적이며, 군집 시각화에 더 적합하지만, 계산 시간이 오래 걸릴 수 있습니다.
# 데이터 포인트가 적은 경우 perplexity 값을 조정해야 할 수 있습니다.
"""
print("\n--- t-SNE Dimensionality Reduction (May take some time) ---")
try:
    tsne = TSNE(n_components=2, perplexity=min(len(all_sentences)-1, 10), random_state=42, init='pca', learning_rate='auto')
    reduced_embeddings_tsne = tsne.fit_transform(sentence_embeddings)

    plt.figure(figsize=(12, 10))
    for i, (x, y) in enumerate(reduced_embeddings_tsne):
        label_text = all_sentences[i]
        plt.scatter(x, y)
        plt.annotate(label_text, (x, y), textcoords="offset points", xytext=(5, 5), ha='left', fontsize=9)

    plt.title('t-SNE 2D Dimensionality Reduction Visualization of Sentence Embeddings', fontsize=16)
    plt.xlabel('t-SNE Component 1')
    plt.ylabel('t-SNE Component 2')
    plt.grid(True, linestyle='--', alpha=0.6)
    plt.tight_layout()
    plt.show()
except ValueError as ve:
    print(f"t-SNE execution error: {ve}. Adjust perplexity or check data points.")
except Exception as e:
    print(f"Unknown error during t-SNE execution: {e}")
"""

print("\n--- Code Execution Complete ---")
print("In the heatmap, similar sentences should show high cosine similarity (brighter colors).")
print("In the PCA/t-SNE graphs, semantically similar sentences should cluster closer together.")