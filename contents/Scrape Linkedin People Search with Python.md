# Scrape Linkedin People Search Results with Python

먼저 scraping 하고자 하는 링크는 `https://www.linkedin.com/search/results/people/?keywords..` 이고

`https://www.linkedin.com/robots.txt` 에 접속해서 automated scraping 허용 여부를 확인해보자.

Disallow 목록에 포함되지 않는다면 시작 !!

## Run Chrome Debug mode

- chrome 을 자동화 모드가 아닌 Debug 모드로 실행

```python
subprocess.Popen(
    r'C:\Program Files\Google\Chrome\Application\chrome.exe --remote-debugging-port=9222 --user-data-dir="C:\chrometemp"')
option = Options()
option.add_experimental_option("debuggerAddress", "127.0.0.1:9222")
driver = webdriver.Chrome('C:\Program Files\Google\Chrome\Application\chromedriver.exe', options=option)  # https://chromedriver.chromium.org/downloads
driver.implicitly_wait(10)
```

## Login

- 사용자 검색을 위해 Linkedin 로그인이 필요하다.

```python
# Linkedin 로그인 페이지 이동
URL = 'https://www.linkedin.com/login/ko?fromSignIn=true&trk=guest_homepage-basic_nav-header-signin'
driver.get(url=URL)
driver.implicitly_wait(5)

driver.find_element_by_id('username').send_keys('username') # ID 입력
driver.find_element_by_id('password').send_keys('password') # PW 입력
search_btn = driver.find_element_by_css_selector('#organic-div > form > div.login__form_action_container > button') # button element
search_btn.click()
```

## Enter the search keyword

- 검색 키워드 입력

```python
keyword = 'backend developer'
URL = 'https://www.linkedin.com/search/results/people/?keywords=' + keyword
driver.get(url=URL)
driver.implicitly_wait(5)
```

## Find the last page

- 검색 결과 마지막 페이지 번호 찾기

```python
driver.execute_script("window.scrollTo(0,document.body.scrollHeight)") # 페이지 하단으로 스크롤
time.sleep(2)
soup_date_detail = BeautifulSoup(driver.page_source, 'html.parser')
last_page = soup_date_detail.select('li.artdeco-pagination__indicator span')[-1].text
```

## Search Results Structure

**Structure**

- People Search Results List Structure

  - 사용자 검색 결과 리스트는 아래와 같이 `<ul><li>` 태그로 구성되어 있다.

  ![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/linked/2.png)

- People Profile Structure

<img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/linked/7.png" alt="drawing" width="70%"/>

<img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/linked/1.png" alt="drawing" width="70%"/>

- Name Structure

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/linked/3.png)

- subtitle Structure

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/linked/4.png)

- secondary subtitle Structure

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/linked/5.png)

- summary Structure

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/linked/6.png)

## Start Scraping

- name, company, nationality, summary 정보가 없는 경우 exception 이 터져서 try-except 에 담아두긴 했지만 이 부분은 더 개선할 수 있을 듯 보인다.

```python
name_list = []
company_list = []
nationality_list = []
summary_list = []

for page in tqdm(range(1, int(last_page) + 1)):

    URL = 'https://www.linkedin.com/search/results/people/?keywords=' + keyword + '&origin=CLUSTER_EXPANSION&page=' + str(page) +'&\sid=luH'
    driver.get(url=URL)
    driver.implicitly_wait(5)

    soup_date_detail = BeautifulSoup(driver.page_source, 'html.parser')
    user_list = soup_date_detail.select('div.entity-result__content')

    for user in user_list:
        try:
            name = user.find('span', {'aria-hidden': 'true'}).text.strip()
        except:
            name = ''
        try:
            company = user.find('div', {'class': 'entity-result__primary-subtitle'}).text.strip()
        except:
            company = ''
        try:
            nationality = user.find('div', {'class': 'entity-result__secondary-subtitle'}).text.strip()
        except:
            nationality = ''
        try:
            summary = user.find('p', {'class': 'entity-result__summary'}).text.strip()
        except:
            summary = ''

        name_list.append(name)
        company_list.append(company)
        nationality_list.append(nationality)
        summary_list.append(summary)

driver.close()
```

## Extract Excel

```python
data = {
    'name': name_list,
    'company': company_list,
    'nationality': nationality_list,
    'summary': summary_list,
}
result_df = pd.DataFrame(data)
result_df.to_excel('C:\\Users\\cristoval\\Desktop\\' + '{0}.xlsx'.format(keyword), index=False)
```

# Entire Code

```python
from selenium import webdriver
import pandas as pd
from tqdm import tqdm

import subprocess
from selenium.webdriver.chrome.options import Options
import shutil
import time

from bs4 import BeautifulSoup

try:
    shutil.rmtree(r"c:\chrometemp")
except FileNotFoundError:
    pass

subprocess.Popen(
    r'C:\Program Files\Google\Chrome\Application\chrome.exe --remote-debugging-port=9222 --user-data-dir="C:\chrometemp"')
option = Options()
option.add_experimental_option("debuggerAddress", "127.0.0.1:9222")
driver = webdriver.Chrome('C:\Program Files\Google\Chrome\Application\chromedriver.exe', options=option)  # https://chromedriver.chromium.org/downloads
driver.implicitly_wait(10)

# Linkedin 로그인 페이지 이동
URL = 'https://www.linkedin.com/login/ko?fromSignIn=true&trk=guest_homepage-basic_nav-header-signin'

driver.get(url=URL)
driver.implicitly_wait(5)

# 로그인
driver.find_element_by_id('username').send_keys('username') # ID 입력
driver.find_element_by_id('password').send_keys('password') # PW 입력
search_btn = driver.find_element_by_css_selector('#organic-div > form > div.login__form_action_container > button') # button element
search_btn.click()

# Search Keyword
keyword = 'backend developer'
URL = 'https://www.linkedin.com/search/results/people/?keywords=' + keyword
driver.get(url=URL)
driver.implicitly_wait(5)

# Find the last page
driver.execute_script("window.scrollTo(0,document.body.scrollHeight)")
time.sleep(2)
soup_date_detail = BeautifulSoup(driver.page_source, 'html.parser')
last_page = soup_date_detail.select('li.artdeco-pagination__indicator span')[-1].text

# Start Scraping
name_list = []
company_list = []
nationality_list = []
summary_list = []

for page in tqdm(range(1, int(last_page) + 1)):

    URL = 'https://www.linkedin.com/search/results/people/?keywords=' + keyword + '&origin=CLUSTER_EXPANSION&page=' + str(page) +'&\sid=luH'
    driver.get(url=URL)
    driver.implicitly_wait(5)

    soup_date_detail = BeautifulSoup(driver.page_source, 'html.parser')
    user_list = soup_date_detail.select('div.entity-result__content')

    for user in user_list:
        try:
            name = user.find('span', {'aria-hidden': 'true'}).text.strip()
        except:
            name = ''
        try:
            company = user.find('div', {'class': 'entity-result__primary-subtitle'}).text.strip()
        except:
            company = ''
        try:
            nationality = user.find('div', {'class': 'entity-result__secondary-subtitle'}).text.strip()
        except:
            nationality = ''
        try:
            summary = user.find('p', {'class': 'entity-result__summary'}).text.strip()
        except:
            summary = ''

        name_list.append(name)
        company_list.append(company)
        nationality_list.append(nationality)
        summary_list.append(summary)

driver.close()

data = {
    'name': name_list,
    'company': company_list,
    'nationality': nationality_list,
    'summary': summary_list,
}

result_df = pd.DataFrame(data)
result_df.to_excel('C:\\Users\\cristoval\\Desktop\\' + '{0}.xlsx'.format(keyword), index=False)
```
