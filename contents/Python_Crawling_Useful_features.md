# Python Crawling Useful features

- Read Excel File & Show Progress bar & Make DataFrame

```python
import pandas as pd
from tqdm import tqdm

file_name = 'test_file'
file_df = pd.read_excel('C:\\Users\\cristoval\\Desktop\\data\\' + file_name + '.xlsx')

data = {'id': [], 'title': [], 'link' : []}
result_df = pd.DataFrame(data=data)

for idx, row in tqdm(file_df.iterrows()):
    # do something
	result_df = result_df.append({'id': _id, 'title': title, 'link' : link}, ignore_index=True)
    
result_file_name = 'result_file'
result_df.to_excel('C:\\Users\\cristoval\\Desktop\\data\\{0}.xlsx'.format(result_file_name), index=False)
```

- Selenium Chrome debug mode

```python
from selenium import webdriver
import subprocess
import shutil
from selenium.webdriver.chrome.options import Options

try:
    shutil.rmtree(r"c:\chrometemp")  # remoce cookie/cache file
except FileNotFoundError:
    pass

# chrome.exe file path : C:\Program Files\Google\Chrome\Application\chrome.exe
# chrome port : 9222
# cookie/cache file path : C:\chrometemp
# chrome.exe --remote-debugging-port=9222 --user-data-dir="C:\chrometemp"
subprocess.Popen(r'C:\Program Files\Google\Chrome\Application\chrome.exe --remote-debugging-port=9222 --user-data-dir="C:\chrometemp"')

option = Options()
option.add_experimental_option("debuggerAddress", "127.0.0.1:9222")
# chromedriver.exe path & Set option
driver = webdriver.Chrome('C:\Program Files\python\chromedriver.exe', options=option)
driver.implicitly_wait(10)

URL = 'https://data-make.tistory.com/'
driver.get(url=URL)
driver.implicitly_wait(time_to_wait=1)

driver.close()
```

## Example Code

```python
from selenium.webdriver.chrome.options import Options
from selenium import webdriver
from tqdm import tqdm
import pandas as pd
import subprocess
import shutil

########################
# Read Excel File
########################
file_name = 'example'
file_df = pd.read_excel('C:\\Users\\cristoval\\Desktop\\data\\' + file_name + '.xlsx')

##########################
# Run Chrome Debug Mode
##########################
try:
    shutil.rmtree(r"c:\chrometemp")  # remove cookie/cache file
except FileNotFoundError:
    pass

subprocess.Popen(r'C:\Program Files\Google\Chrome\Application\chrome.exe --remote-debugging-port=9922 --user-data-dir="C:\chrometemp"')
option = Options()
option.add_experimental_option("debuggerAddress", "127.0.0.1:9922")
driver = webdriver.Chrome('C:\Program Files\python\chromedriver.exe', options=option)
driver.implicitly_wait(10)

###################
# Start Crawling
###################
data = {'id': [], 'title': [], 'link' : []}
result_df = pd.DataFrame(data=data)

for idx, row in tqdm(file_df.iterrows()):

    URL = 'https://data-make.tistory.com/' + str(row['id'])

    driver.get(url=URL)
    driver.implicitly_wait(time_to_wait=1)

    link = ''
    title = ''
    try:
        # Click on a specific element
        element_btn = driver.find_element_by_id('id-name')
        element_btn.click()
        
        # Get data by element 
        element_box = driver.find_element_by_class_name('tit_post')
        link = element_box.find_element_by_tag_name('a').get_attribute('href')
        title = element_box.find_element_by_tag_name('a').text
    except:
        print('fail')
        continue

    # do something

    result_df = result_df.append({'id': str(row['id']), 'title': title, 'link' : link}, ignore_index=True)

driver.close()

################################################
# Extract to Excel File
################################################
result_file_name = 'result'
result_df.to_excel('C:\\Users\\cristoval\\Desktop\\data\\{0}.xlsx'.format(result_file_name), index=False)
```

**Reference**

> [Selenium with Python](https://selenium-python.readthedocs.io/)
>
> [tqdm](https://tqdm.github.io/)
>
> [셀레니움 웹 크롤링 봇 탐지 우회](https://pythondocs.net/selenium/%EC%85%80%EB%A0%88%EB%8B%88%EC%9B%80-%EC%9B%B9-%ED%81%AC%EB%A1%A4%EB%A7%81-%EB%B4%87-%ED%83%90%EC%A7%80-%EC%9A%B0%ED%9A%8C/)


