# CLOVA OCR API

이미지를 텍스트로 추출하기 위한 API 중 NAVER OCR API를 활용해보고자 한다.

# Ready

## 결제수단등록

NAVER CLOVA 사용을 위해 결제수단 등록이 필요하다.

<https://www.ncloud.com/mypage/billing/payment>

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/ocr_img/1.png)

## CLOVA OCR 이용 신청하기

결제수단 등록을 완료했다면 CLOVA OCR 이용 신청을 해보자.

<https://www.ncloud.com/product/aiService/ocr>

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/ocr_img/2.png)

## 도메인 생성

이용 신청을 완료하면 NAVER CLOUD PLATFORM Dashboard 로 이동되었던 것 같다..

- [NAVER CLOUD PLATFORM Dashboard](https://console.ncloud.com/ocr/domain) > Recently Viewd > CLOVA OCR > Domain

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/ocr_img/3.png)

- 도메인 이름, 코드, 지원 언어, 서비스 타입을 선택하고 생성

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/ocr_img/4.png)

**참고 정보**

- 인식 모델 Basic의 Free를 제외한 모든 서비스 플랜은 CLOVA OCR API 호출을 하지 않아도 기본 유지 비용이 발생
- OCR 서비스는 Demo와 API 호출 수를 합산하여 매월 100회 무료 제공
- [CLOVA OCR 요금 탭 참고](https://www.ncloud.com/product/aiService/ocr)

### Secret Key 발급

- 생성된 도메인의 Text OCR 클릭

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/ocr_img/5.png)

- Secret Key 생성 버튼을 누르면 생성

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/ocr_img/6.png)

### Invoke URL 생성

- Invoke URL 생성을 위해 APIGW 자동 연동이 필요한데, 연동을 위해 API Gateway 이용 신청이 필요하다.

  - [API Gateway 이용 신청](https://www.ncloud.com/product/applicationService/apiGateway)

- APIGW 자동 연동 버튼을 클릭하게 되면 API Gateway 의 Product 가 자동으로 생성되고 APIGW Invoke URL 이 생성된다.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/ocr_img/7.png)

# API Test

[Documentation](https://api.ncloud-docs.com/docs/ai-application-service-ocr#)

## Request

- Method : `POST`

**Request Header**

- X-OCR-SECRET : `Client Secret`
- Content-Type : `application/json` or `multipart/form-data`

**Request Body**

- `Content-Type` : `application/json` Case

```json
{
    "version": "V2", # V2 사용을 권장
    "requestId": "4fed2c77-e7db-....", # UUID
    "timestamp": 0, # Timestamp
    "images": [
    			{
    				"format": "jpg",
    				"url": "Image url",
                     "name": "image"
				}
			],
}
```

- `Content-Type` : `multipart/form-data` Case
  - message : `{"version": "V2","requestId": "4fed2c77-e7db-....","timestamp": 0,"images": [{ "format": "jpg", "name": "image"}]}`
  - file : `image file steam data`

## Response

```json
{
    "version": "V2",
    "requestId": "4fed2c77-e7db-....",
    "timestamp": 1634878730279,
    "images": [
        {
            "uid": "84260f1d1aa94a7...",
            "name": "image",
            "inferResult": "SUCCESS", # 이미지 추론 결과 (SUCCESS, FAILURE, ERROR)
            "message": "SUCCESS",
            "validationResult": {
                "result": "NO_REQUESTED"
            },
            "fields": [
                {
                    "valueType": "ALL",
                    "boundingPoly": {
                        "vertices": [
                            {
                                "x": 5.0,
                                "y": 0.0
                            },
                            {
                                "x": 105.0,
                                "y": 0.0
                            },
                            {
                                "x": 105.0,
                                "y": 31.0
                            },
                            {
                                "x": 5.0,
                                "y": 31.0
                            }
                        ]
                    },
                    "inferText": "116204", # OCR 인식 결과 TEXT
                    "inferConfidence": 0.9983, # 결과에 대한 점수 (0~1)
                    "type": "NORMAL",
                    "lineBreak": true
                }
            ]
        }
    ]
}
```

## Python API

- Request with multipart/form-data

```python
import requests
import uuid
import time
import json

api_url = 'YOUR_API_URL'
secret_key = 'YOUR_SECRET_KEY'
image_file = 'YOUR_IMAGE_FILE'

request_json = {
    'images': [
        {
            'format': 'jpg',
            'name': 'demo'
        }
    ],
    'requestId': str(uuid.uuid4()),
    'version': 'V2',
    'timestamp': int(round(time.time() * 1000))
}

payload = {'message': json.dumps(request_json).encode('UTF-8')}
files = [
  ('file', open(image_file,'rb'))
]
headers = {
  'X-OCR-SECRET': secret_key
}

response = requests.request("POST", api_url, headers=headers, data = payload, files = files)

result = response.text.encode('utf8')
result_json = json.loads(result.decode('utf8').replace("'", '"'))
result_text = result_json['images'][0]['fields'][0]['inferText']
print(result_txt)
```

- Request with application/json

```python
import requests
import uuid
import time
import base64
import json

api_url = 'YOUR_API_URL'
secret_key = 'YOUR_SECRET_KEY'
image_url = 'YOUR_IMAGE_URL'

request_json = {
    'images': [
        {
            'format': 'jpg',
            'name': 'demo',
            'url': image_url
        }
    ],
    'requestId': str(uuid.uuid4()),
    'version': 'V2',
    'timestamp': int(round(time.time() * 1000))
}

payload = json.dumps(request_json).encode('UTF-8')
headers = {
  'X-OCR-SECRET': secret_key,
  'Content-Type': 'application/json'
}

response = requests.request("POST", api_url, headers=headers, data = payload)

print(response.text)
```
