# Download csv file : https://dhlottery.co.kr/gameResult.do?method=byWin

import pandas as pd
from tqdm import tqdm

path = 'D:\\Workspace\\python\\files\\'
file_name = '_Lotto_1_979.xlsx'
data = pd.read_excel(path + file_name, header = 1)

data.iloc[:,[0,1,2,14,15,16,17,18,19,20]]  # '년도','회차','추첨일','1','2','3','4','5','6','보너스'

column = []
for n in range(1, 46):
    column.append(n)

result_df = pd.DataFrame(None, columns=column)

for year in tqdm(range(2002, 2022)):

    data_Year = data.loc[data.iloc[:,0] == year]

    idx = 1
    months = []
    for i in range(1, 13):

        nums = {}
        for n in range(1, 46):
            nums[n] = 0

        m = data_Year.loc[data_Year.iloc[:,3] == i]

        if len(m) == 0:
            months.append(None)
            continue

        for row in m.iterrows():

            nums[row[1][14]] = nums.get(row[1][14]) + 1
            nums[row[1][15]] = nums.get(row[1][15]) + 1
            nums[row[1][16]] = nums.get(row[1][16]) + 1
            nums[row[1][17]] = nums.get(row[1][17]) + 1
            nums[row[1][18]] = nums.get(row[1][18]) + 1
            nums[row[1][19]] = nums.get(row[1][19]) + 1
            nums[row[1][20]] = nums.get(row[1][20]) + 1

        months.append(nums)

    zero = {}
    next = {}
    for n in range(1, 46):
        zero[n] = 0
        next[n] = year


    for idx, month in enumerate(months):
        if month == None :
            result_df = result_df.append(zero, ignore_index=True)
        else :
            result_df = result_df.append(months[idx], ignore_index=True)

    result_df = result_df.append(next, ignore_index=True)


result_df.to_excel(path + 'result.xlsx', index=False)
