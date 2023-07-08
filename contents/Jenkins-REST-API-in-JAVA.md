# Jenkins REST API in JAVA

특정 시기마다 대략 20개 정도의 배치를 Jenkins 에서 수동 재기동해야하는 일이 있다.

가끔씩? 종종? sometimes.. 있는 일이긴 하지만 너무나 귀찮은 일이다.

한 번의 어떠한 동작으로 이 귀찮은 일을 처리할 수 있는 방법을 떠올리다 Jenkins API 를 활용해 보자는 생각이 들었다.

.

이제 하나의 API 호출만으로 20개 정도의 배치가 Jenkins 에서 실행되도록 해보자. 

참고로, [jenkins-rest](https://github.com/cdancy/jenkins-rest) 라이브러리를 활용할 수도 있지만

REST API 호출을 적용해볼 예정이다.

## API Token

REST API 사용을 위해 먼저 사용자 API Token 이 필요하다.

아래 경로에서 토큰을 추가할 수 있다.

**설정 경로**

`Jenkins 메인 ➜ 사람 ➜ 토큰을 발행할 유저 ➜ 설정 ➜ API Token`

또는

`http://{JENKINS_URL}/user/{USER_ID}/configure`

**Add New Token**

`Add new Token` 버튼을 누르면 Default name 입력이 필요한데 적당한 이름 입력 후 `Generate` 버튼을 눌러주면 Token 을 생성해 준다.

이 값는 나중에 다시 보여주지 않는 것 같으니.. 잘 저장해 두자.

## Basic Auth

API 호출 시 사용자 인증을 위해 `Authorization - Basic Auth` 방식으로 인증이 필요하다.

`Username`(USER_ID), `Password`(API_TOKEN) 이 필요한데 우리는 이 정보들을 다 알고 있으니 걱정할 필요는 없다.

.

참고로, `USER_ID`, `API_TOKEN` 을 모두 입력하는 방법보다는 Basic Auth 정보를 BASE64 인코딩하여 `Basic Authentication Header` 로 사용하는 것이 더 편할 것이다.

[Basic Authentication Header Generator](https://mixedanalytics.com/tools/basic-authentication-generator/) 에서 변환이 가능하고,

`username:password` 형태로 인코딩하면 Basic Authentication Header 가 생성된다.

.

Basic Authentication Header 는 Postman - Code snippet - cURL 탭에서도 확인할 수 있다.

```bash
curl --location 'http://{JENKINS_URL}/job/{JOBNAME}/api/json' \
--header 'Authorization: Basic AbCde...' \
...
```

## Jenkins REST API

유용하게 사용할만한 Jenkins REST API 는 아래와 같다.

**Job 조회**
- `GET http://{JENKINS_URL}/job/{JOBNAME}/api/json`
 
**Job 빌드 수행**
- `POST http://{JENKINS_URL}/job/{JOBNAME}/build`
 
**Job 빌드 결과 조회**
- `GET http://{JENKINS_URL}/job/{JOBNAME}/{BUILD_NUMBER}/api/json`
 
**Job 마지막 성공 빌드 조회**
- `GET http://{JENKINS_URL}/job/{JOBNAME}/lastStableBuild/api/json`

.

그밖에 API 정보가 궁금하다면 아래 REST API 에서 확인해 보자.

- http://{JENKINS_URL}/api/
- http://{JENKINS_URL}/job/{JOBNAME}/api/

### Job Info

먼저 Job 정보 조회 API 테스트를 해보자.

```java
@Getter
@Setter
public class JenkinsJobInfoResponse {
    private String description;
    private String displayName;
    private String fullDisplayName;
    private String fullName;
    private String name;
    private String url;
    private String color;
    private boolean buildable;
}

...

@Test
@DisplayName("Job 조회: GET http://{JENKINS_URL}/job/{JOB_NAME}/api/json")
void info() throws Exception {
    //
    final String url = "/job/" + JOB_NAME + "/api/json";

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", BASIC_AUTH + API_TOKEN);

    HttpEntity<JenkinsJobInfoResponse> httpEntity = new HttpEntity<>(null, headers);

    //
    JenkinsJobInfoResponse response = new RestTemplate()
                .postForObject(BASE_URL + url, httpEntity, JenkinsJobInfoResponse.class);

    //
    log.info("description: {}", response.getDescription());
    Assertions.assertEquals("Job DisplayName", response.getDisplayName());
    Assertions.assertEquals("Job FullDisplayName", response.getFullDisplayName());
    Assertions.assertEquals("Job FullName", response.getFullName());
    Assertions.assertEquals("Job DisplayName", response.getDisplayName());
    Assertions.assertEquals("Job Url", response.getUrl());
    Assertions.assertEquals("blue", response.getColor());
}
```

**REST API Response**

```json
{
    "_class": "hudson.model.FreeStyleProject",
    "actions": [
        {
            "_class": "org.jenkinsci.plugins.displayurlapi.actions.JobDisplayAction"
        },
        {
            "_class": "com.cloudbees.plugins.credentials.ViewCredentialsAction"
        }
        ...
    ],
    "description": "Job 설명",
    "displayName": "Job 이름",
    "displayNameOrNull": null,
    "fullDisplayName": "Job 이름",
    "fullName": "Job 이름",
    "name": "Job 이름",
    "url": "http://{JENKINS_URL}/job/{JOB_NAME}/",
    "buildable": true,
    "builds": [
        {
            "_class": "hudson.model.FreeStyleBuild",
            "number": 1427,
            "url": "http://{JENKINS_URL}/job/{JOB_NAME}/1427/"
        },
        {
            "_class": "hudson.model.FreeStyleBuild",
            "number": 1426,
            "url": "http://{JENKINS_URL}/job/{JOB_NAME}/1426/"
        },
        ...
    ],
    "color": "blue",
    "firstBuild": {
        "_class": "hudson.model.FreeStyleBuild",
        "number": 1423,
        "url": "http://{JENKINS_URL}/job/{JOB_NAME}/1423/"
    },
    "healthReport": [
        {
            "description": "Build stability: No recent builds failed.",
            "iconClassName": "icon-health-80plus",
            "iconUrl": "health-80plus.png",
            "score": 100
        }
    ],
    "inQueue": false,
    "keepDependencies": false,
    "lastBuild": {
        "_class": "hudson.model.FreeStyleBuild",
        "number": 1427,
        "url": "http://{JENKINS_URL}/job/{JOB_NAME}/1427/"
    },
    "lastCompletedBuild": {
        "_class": "hudson.model.FreeStyleBuild",
        "number": 1427,
        "url": "http://{JENKINS_URL}/job/{JOB_NAME}/1427/"
    },
    "lastFailedBuild": null,
    "lastStableBuild": {
        "_class": "hudson.model.FreeStyleBuild",
        "number": 1427,
        "url": "http://{JENKINS_URL}/job/{JOB_NAME}/1427/"
    },
    "lastSuccessfulBuild": {
        "_class": "hudson.model.FreeStyleBuild",
        "number": 1427,
        "url": "http://{JENKINS_URL}/job/{JOB_NAME}/1427/"
    },
    "lastUnstableBuild": null,
    "lastUnsuccessfulBuild": null,
    "nextBuildNumber": 1428,
    "property": [
        {
            "_class": "jenkins.model.BuildDiscarderProperty"
        }
    ],
    "queueItem": null,
    "concurrentBuild": false,
    "disabled": false,
    "downstreamProjects": [],
    "labelExpression": null,
    "scm": {
        "_class": "hudson.scm.NullSCM"
    },
    "upstreamProjects": []
}
```

### Job Build

다음으로 Job 빌드 수행 API 테스트를 해보자.

```java
@Test
@DisplayName("Job 빌드 수행: POST http://{JENKINS_URL}/job/{JOBNAME}/build")
void build() throws Exception {
    //
    final String url = "/job/" + JOB_NAME + "/build";
    HttpEntity<JenkinsJobInfoResponse> httpEntity = new HttpEntity<>(null, headers);

    //
    ResponseEntity<String> response = new RestTemplate()
            .postForEntity(BASE_URL + url, httpEntity, String.class);

    //
    Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
}
```

**REST API Response**

Job 빌드 수행 API 는 요청 성공 시 응답으로 statusCode 로 `201 Created` 를 준다.

### Job Build Result

다음으로 Job 빌드 정보 조회 API 테스트를 해보자.

```java
@Getter
@Setter
public class JenkinsJobBuildInfoResponse {
    private List<Action> actions;
    private String displayName;
    private String number;
    private String result;


    @Getter
    @ToString
    private static class Action {
        private List<Cause> causes;
    }

    @Getter
    @ToString
    private static class Cause {
        private String shortDescription;
        private String userId;
        private String userName;
    }
}

...

@Test
@DisplayName("Job 빌드 결과 조회: GET http://{JENKINS_URL}/job/{JOB_NAME}/{BUILD_NUMBER}/api/json`")
void build_result() throws Exception {
    //
    String build_number = "1427";
    final String url = "/job/" + JOB_NAME + "/" + build_number + "/api/json";
    HttpEntity<JenkinsJobBuildInfoResponse> httpEntity = new HttpEntity<>(null, headers);

    //
    JenkinsJobBuildInfoResponse response = new RestTemplate()
            .postForObject(BASE_URL + url, httpEntity, JenkinsJobBuildInfoResponse.class);

    //
    log.info("description: {}", response.getActions());
    Assertions.assertEquals("#1427", response.getDisplayName());
    Assertions.assertEquals("1427", response.getNumber());
    Assertions.assertEquals("SUCCESS", response.getResult());
}
```

**REST API Response**

```json
{
  "_class": "hudson.model.FreeStyleBuild",
  "actions": [
      {
          "_class": "hudson.model.CauseAction",
          "causes": [
              {
                  "_class": "hudson.model.Cause$UserIdCause",
                  "shortDescription": "Started by user Aaron",
                  "userId": "12345",
                  "userName": "Aaron"
              }
          ]
      },
      {},
      {},
      {
          "_class": "org.jenkinsci.plugins.displayurlapi.actions.RunDisplayAction"
      }
  ],
  "artifacts": [],
  "building": false,
  "description": null,
  "displayName": "#1427",
  "duration": 23671,
  "estimatedDuration": 30298,
  "executor": null,
  "fullDisplayName": "{JOB_NAME} #1427",
  "id": "1427",
  "keepLog": false,
  "number": 1427,
  "queueId": 58607993,
  "result": "SUCCESS",
  "timestamp": 1688602315716,
  "url": "http://{JENKINS_URL}/job/{JOB_NAME}/1427/",
  "builtOn": "common09",
  "changeSet": {
      "_class": "hudson.scm.EmptyChangeLogSet",
      "items": [],
      "kind": null
  },
  "culprits": []
}
```

### Job Last Success Build

다음으로 Job 마지막 설공 빌드 정보 조회 API 테스트를 해보자.

```java
@Test
@DisplayName("Job 마지막 성공 빌드 조회: GET http://{JENKINS_URL}/job/{JOBNAME}/lastStableBuild/api/json")
void last_success_build_result() throws Exception {
    //
    final String url = "/job/" + JOB_NAME + "/lastStableBuild/api/json";
    HttpEntity<JenkinsJobBuildInfoResponse> httpEntity = new HttpEntity<>(null, headers);

    //
    JenkinsJobBuildInfoResponse response = new RestTemplate()
            .postForObject(BASE_URL + url, httpEntity, JenkinsJobBuildInfoResponse.class);

    //
    log.info("description: {}", response.getActions());
    Assertions.assertEquals("#1431", response.getDisplayName());
    Assertions.assertEquals("1431", response.getNumber());
    Assertions.assertEquals("SUCCESS", response.getResult());
}
```

**REST API Response**

```json
{
    "_class": "hudson.model.FreeStyleBuild",
    "actions": [
        {
            "_class": "hudson.model.CauseAction",
            "causes": [
                {
                    "_class": "hudson.triggers.TimerTrigger$TimerTriggerCause",
                    "shortDescription": "Started by timer"
                }
            ]
        },
        {},
        {},
        {
            "_class": "org.jenkinsci.plugins.displayurlapi.actions.RunDisplayAction"
        }
    ],
    "artifacts": [],
    "building": false,
    "description": null,
    "displayName": "#1431",
    "duration": 33036,
    "estimatedDuration": 27496,
    "executor": null,
    "fullDisplayName": "{JOB_NAME} #1431",
    "id": "1431",
    "keepLog": false,
    "number": 1431,
    "queueId": 58690216,
    "result": "SUCCESS",
    "timestamp": 1688661003002,
    "url": "http://{JENKINS_URL}/job/{JOB_NAME}/1431/",
    "builtOn": "common05",
    "changeSet": {
        "_class": "hudson.scm.EmptyChangeLogSet",
        "items": [],
        "kind": null
    },
    "culprits": []
}
```