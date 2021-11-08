# Python Multiprocessing

## GIL(Global Interpreter Lock)

- 파이썬에서는 하나의 프로세스 안에 모든 자원의 Lock을 Global하게 관리함으로써 한번에 하나의 쓰레드만 자원을 컨트롤하여 동작

  - 여러 쓰레드를 동시에 실행시키지만, 결국 GIL 때문에 한번에 하나의 쓰레드만 계산을 실행
  - GIL로 자원 관리(GC..)를 쉽게 구현 가능해졌지만, 멀티 코어 부분에서는 아쉬움
  - 파이썬의 경우에는 GIL이 존재하여 `멀티 쓰레드보다는 멀티 프로세스`를 사용하는 것이 좋음

## multiprocessing

- 쓰레딩 모듈로 쓰레드를 생성 할 수 있는 것과 동일한 방식으로 프로세스를 생성
- 프로세스는 각자가 고유한 메모리 영역을 가지기 때문에 쓰레드에 비하면 메모리 사용이 늘어난다는 단점
- 싱글 머신 아키텍처로부터 여러 머신을 사용하는 분산 애플리케이션으로 쉽게 전환 가능

**Reference**

> [파이썬 멀티 쓰레드와 멀티 프로세스](https://monkey3199.github.io/develop/python/2018/12/04/python-pararrel.html)
>
> [Python Multiprocessing - Pool](https://niceman.tistory.com/145)
>
> [multiprocessing Basics](https://pymotw.com/2/multiprocessing/basics.html)
>
> [프로세스 기반 병렬 처리](https://docs.python.org/ko/3/library/multiprocessing.html)
>
> [thumbnail](https://biology-statistics-programming.tistory.com/111)

## Code

```python
import multiprocessing
import time
import random

# 시작시간
start_time = time.time()

def count(name):
    print('Start ' + name, " : ")
    time.sleep(random.randrange(1,10))
    print('End ' + name, " : ")

work_list = []
for i in range(1, 11):
    work_list.append('item' + str(i))

if __name__ == '__main__':
    # 멀티 쓰레딩 Pool 사용
    pool = multiprocessing.Pool(processes=6)  # 현재 시스템에서 사용 할 프로세스 개수
    pool.map(count, work_list)
    pool.close()
    pool.join()

    print("--- %s seconds ---" % (round(time.time() - start_time, 4)))
```

- 작업관리자의 프로세스를 확인해보면 코드에서 설정한 processes 개수만큼의 python 이 동시에 동작하고 있는 것을 확인할 수 있다.

## Sample Code

### Base

```python
import pandas as pd
from tqdm import tqdm
from bs4 import BeautifulSoup
import multiprocessing

def doWork(file_name):
    print('Start ' + file_name + ' : ')

    file = open(dir_path + file_name, 'r', encoding="UTF-8")
    file = file.read().strip()
    file_soup = BeautifulSoup(file, 'html5lib')

    columns = {'id': [], 'title': [], 'text': []}
    tmp_df = pd.DataFrame(data=columns)

    for doc in file_soup.find_all('doc'):
        tmp_df = tmp_df.append({'id': doc['id'], 'title': doc['title'], 'text': doc.text}, ignore_index=True)

    # 각 작업에 해당하는 결과물
    # 대량의 데이터를 다룰 시 중간 데이터를 저장해두는 것이 좋음
    tmp_df.to_excel(dir_path + '{0}'.format(file_name + '.xlsx'), index=False)

    print('End ' + file_name + ' : ')


# 작업 리스트를 반환
def getWorkList():
    work_list = []

    for i in tqdm(range(0, 17)):
        work_list.append('file_' + str(i))

    return work_list

dir_path = 'C:\\Users\\jihun.park\\Desktop\\data\\'

if __name__ == '__main__':

    pool = multiprocessing.Pool(processes=3) # 3개의 processes 사용
    pool.map(doWork, getWorkList())
    pool.close()
    pool.join()
```

### Return Type

- multiprocessing 에서는 global 사용이 불가하여 multiprocessing.Manager()를 통해 변수 공유를 할 수 있음
- [multiprocessing.Manager()](https://docs.python.org/3/library/multiprocessing.html#multiprocessing.sharedctypes.multiprocessing.Manager)

```python
import pandas as pd
import multiprocessing

def doWork(w_list):
    list_a = []
    list_b = []
    list_c = []

    for work in w_list:
        list_a.append('a_' + work)
        list_b.append('b_' + work)
        list_c.append('c_' + work)

    return list_a, list_b, list_c


work_list = []
for i in range(0, 10):
    w_list = []
    for w in range(0, 5):
        w_list.append('work_' + str(i) + '_' + str(w))

    work_list.append(w_list)

if __name__ == '__main__':

    list_fst = []
    list_scd = []
    list_thd = []

    # 멀티 쓰레딩 Pool 사용
    pool = multiprocessing.Pool(processes=6)  # 현재 시스템에서 사용 할 프로세스 개수
    results_async = [pool.apply_async(doWork, [i]) for i in work_list]
    results = [r.get() for r in results_async]

    for idx in range(0, len(results)):
        list_fst.append(results[idx][0])
        list_scd.append(results[idx][1])
        list_thd.append(results[idx][2])

    data = {
        'a': list_fst,
        'b': list_scd,
        'c': list_thd
    }

    result_df = pd.DataFrame(data)
    result_df.to_excel('C:\\Cristoval\\data\\{0}'.format('test.xlsx'), index=False)
```
