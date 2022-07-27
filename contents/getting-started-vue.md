# Vue

Inflearn [Vue.js 시작하기 - Age of Vue.js](https://www.inflearn.com/course/age-of-vuejs/dashboard) 강의를 듣고 정리한 노트입니다.

## What is the Vue

![Result](https://github.com/jihunparkme/blog/blob/main/img/vue/view-model.png?raw=true 'Result')

- `View`: 눈에 보이는 화면 (화면의 요소는 HTML)
  - HTML은 DOM을 이용해서 javascript로 조작
- `DOM Listeners`: View에서 사용자 이벤트는 Vue의 DOM Listeners로 청취
- `Modal`: Vue에서 청취한 이벤트를 통해 javascript에 있는 데이터를 변경하거나 특정 로직을 실행
  - javascript를 통해 데이터가 변경되었을 경우 `Data Bindings` 동작하여 View에 반영

## Instance

- 뷰 개발 시 필수로 생성이 필요한 코드
- 인스턴스 생성 시 Vue 개발자 도구에서 Root 컴포넌트로 인식

```javascript
var vm = new Vue({
  el: '#app',
  data: {
    message: 'hi'
  },
  methods: {
  },
  created: function() {
  }
});
```

- `el`: app이라는 ID를 가진 태그를 찾아서 인스턴스를 붙여준다.
  - 태그에 인스턴스를 붙여주면 view의 기능과 속성을 조작 가능
- `data`: 뷰의 반응성(Reactivity)이 반영된 데이터 속성
- `template` : 화면에 표시할 요소 (HTML, CSS 등)
- `methods` : 화면의 동작과 이벤트 로직을 제어하는 메서드
- `created` : 뷰의 라이프 사이클과 관련된 속성
- `watch` : data에서 정의한 속성이 변화했을 때 추가 동작을 수행할 수 있게 정의하는 속성

## Components

**화면의 영역을 영역별로 구분해서 코드로 관리하는 것**

- 화면의 영역을 구분하여 개발할 수 있는 뷰의 기능
- 코드의 재사용성이 올라가고 빠른 화면 제작 가능
- `전역 컴포넌트`는 기본적으로 모든 인스턴스에 등록이 되어 있음
- `지역 컴포넌트`는 인스턴스마다 새로 생싱이 필요
- 다만 서비스를 구현할 때는 하나의 인스턴스에 컴포넌트를 붙여 나가는 형식으로 진행

```html
<body>
  <div id="app">
    <app-header></app-header>
    <app-footer></app-footer>
  </div>

  <div id="app2">
    <app-header></app-header>
    <app-footer></app-footer>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
  <script>
    // 전역 컴포넌트(실무에서는 plugin 이나 library 형태로 사용)
    // Vue.component('컴포넌트 이름', 컴포넌트 내용);
    Vue.component('app-header', {
      template: '<h1>Header</h1>'
    });  

    new Vue({
      el: '#app',
      // 지역 컴포넌트 등록 방식(실무에서 가장 많이 사용하는 방식)
      components: {
        // '컴포넌트 이름': 컴포넌트 내용,
        'app-footer': {
          template: '<footer>footer</footer>'
        }
      },
    });

    new Vue({
      el: '#app2',
      components: {
        'app-footer': {
          template: '<footer>footer</footer>'
        }
      }
    })
  </script>
</body>
</html>
```

### 통신 방식

- 뷰 컴포넌트는 각각 고유한 데이터 유효 범위를 갖고 있음
- 따라서, 컴포넌트 간에 데이터를 주고 받기 위해선 따라야 할 규칙이 존재
  - 상위에서 하위로는 데이터를 내려줌, `프롭스 속성`
  - 하위에서 상위로는 이벤트를 올려줌, `이벤트 발생`

## props

- 컴포넌트 간에 데이터를 전달할 수 있는 컴포넌트 통신 방법
- 상위 컴포넌트에서 하위 컴포넌트로 내려보내는 데이터 속성

```html
<body>
  <div id="app">
    <!-- <app-header v-bind:프롭스 속성 이름="상위 컴포넌트의 데이터 이름"></app-header> -->
    <app-header v-bind:propsdata="message"></app-header>
    <app-content v-bind:propsdata="num"></app-content>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
  <script>
    var appHeader = {
      template: '<h1>{{ propsdata }}</h1>',
      props: ['propsdata'] // 하위 컴포넌트의 프롭스 속성명
    }
    var appContent = {
      template: '<div>{{ propsdata }}</div>',
      props: ['propsdata'] // 하위 컴포넌트의 프롭스 속성명
    }

    new Vue({
      el: '#app',
      components: {
        'app-header': appHeader,
        'app-content': appContent
      },
      data: {
        message: 'hi',
        num: 10
      }
    })
  </script>
</body>
```
- props 속성 사용을 위해 하위 컴포넌트의 컴포넌트 내용과
  ```javascript
  var childComponent = {
    props: ['프롭스 속성 명']
  }
  ```
- 상위 컴포넌트의 템플릿에 각각 코드를 추가
  ```html
  <div id="app">
    <child-component v-bind:프롭스 속성 명="상위 컴포넌트의 data 속성"></child-component>
  </div>
 
  ```

## Emit

- 하위 컴포넌트에서 상위 컴포넌트로 통신

```html
<body>
  <div id="app">
    <p>{{ num }}</p>
    <!-- <app-header v-on:하위 컴포넌트에서 발생한 이벤트 이름="상위 컴포넌트의 메서드 이름"></app-header> -->
    <!-- (2). pass 라는 이벤트가 하위 컴포넌트에서 올라왔을 때, 컴포넌트 태그(app-header)가 받아서 logText라는 메서드를 실행 -->
    <app-header v-on:pass="logText"></app-header>

    <app-content v-on:increase="increaseNumber"></app-content>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
  <script>
    var appHeader = {
      template: '<button v-on:click="passEvent">click me</button>',
      methods: {
        passEvent: function() {
          this.$emit('pass'); // (1). 클릭 시 상위 컴포넌트로 pass 이벤트 발생
        }
      }
    }
    var appContent = {
      template: '<button v-on:click="addNumber">add</button>',
      methods: {
        addNumber: function() {
          this.$emit('increase'); 
        }
      }
    }

    var vm = new Vue({
      el: '#app',
      components: {
        'app-header': appHeader,
        'app-content': appContent
      },
      methods: {
        logText: function() { // (3). 하위 컴포넌트에서 이벤트를 전달 받고 실행
          console.log('hi');
        },
        increaseNumber: function() {
          this.num = this.num + 1;
        }
      },
      data: {
        num: 10
      }
    });
  </script>
</body>
```

## Components at the same level

**같은 컴포넌트 레벨 간의 통신 방법**

- 

```html
<body>
  <div id="app">
    <!-- <app-header v-bind:프롭스 속성 이름="상위 컴포넌트의 데이터 이름"></app-header> -->
    <app-header v-bind:propsdata="num"></app-header>
    <!-- (5) 상위 컴포넌트가 가지고 있는 데이터를 하위 컴포넌트로 전달 -->

    <app-content v-on:pass="deliverNum"></app-content> 
    <!-- (1) appContent의 버튼이 눌리면 passNum 메서드가 실행 -->
    <!-- (3) pass 이벤트가 발생되면 deliverNum 메서드 실행 -->
  </div>

  <script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
  <script>
    var appHeader = {
      template: '<div>header</div>',
      props: ['propsdata']
    }
    var appContent = {
      template: '<div>content<button v-on:click="passNum">pass</button></div>',
      methods: {
        passNum: function() { // (2) 상위 컴포넌트로 pass 이벤트와 인자로 데이터 전달
          this.$emit('pass', 10); 
        }
      }
    }

    new Vue({
      el: '#app',
      components: {
        'app-header': appHeader,
        'app-content': appContent
      },
      data: {
        num: 0
      },
      methods: {
        deliverNum: function(value) { // (4) 하위 컴포넌트로부터 받은 이벤트를 통해 메서드 실행
          this.num = value;
        }
      }
    })
  </script>
</body>
```

## Router

**싱글 페이지 애플리케이션을 구현할 경우 사용하는 라이브러리**

### Install Vue Router

- CDN

```javascript
<script src="https://cdn.jsdelivr.net/npm/vue@2/dist/vue.js"></script>
<script src="https://unpkg.com/vue-router@3.5.3/dist/vue-router.js">
```

- NPM

```console
npm install vue-router
```

### Router Example

```html
<body>
  <div id="app">
    <div>
      <!-- <router-link>는 router compoent로 이동할 수 있는 링크를 제공 -->
      <router-link to="/login">Login</router-link>
      <router-link to="/main">Main</router-link>
    </div>
    <!-- router의 compoents를 표현 -->
    <router-view></router-view>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
  <script src="https://unpkg.com/vue-router@3.5.3/dist/vue-router.js"></script>
  <script>
    var LoginComponent = {
      template: '<div>login</div>'
    }
    var MainComponent = {
      template: '<div>main</div>'
    }

    // 라우터 인스턴스 생성
    var router = new VueRouter({
      mode: 'history',  // URL의 해쉬 값 제거 속성
      routes: [// 페이지의 라우팅 정보 (Array)
        { // 로그인 페이지 정보
          path: '/login', // 페이지의 url
          component: LoginComponent // 해당 url에서 표시될 컴포넌트
        },
        { // 메인 페이지 정보
          path: '/main',
          component: MainComponent
        }
      ]
    });

    new Vue({
      el: '#app',
      router: router, // 인스턴스에 라우터 인스턴스를 연결 (#app 안에서 router 유효)
    });
  </script>
</body>
```

> [Vue Router](https://v3.router.vuejs.org/installation.html#direct-download-cdn)
> [라우터 네비게이션 가드](https://joshua1988.github.io/web-development/vuejs/vue-router-navigation-guards/)

## Axios

[axios](https://github.com/axios/axios)

- 뷰에서 권고하는 HTTP 통신 라이브러리
- Promise 기반(JS 비동기 처리 패턴)이며 상대적으로 다른 HTTP 통신 라이브러리들에 비해 문서화가 잘되어 있고 API가 다양

### Install Axios

- CDN

```javascript
<script src="https://unpkg.com/axios/dist/axios.min.js"></script>
```

- NPM

```console
npm install axios
```

### Axios Example

```html
<body>
  <div id="app">
    <button v-on:click="getData">get user</button>
    <div>
      {{ users }}
    </div>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
  <script src="https://unpkg.com/axios/dist/axios.min.js"></script>
  <script>
    new Vue({
      el: '#app',
      data: {
        users: []
      },
      methods: {
        getData: function() { 
          var vm = this;
          axios.get('https://jsonplaceholder.typicode.com/users/')
            .then(function(response) {
              console.log(response.data);
              vm.users = response.data;
            })
            .catch(function(error) {
              console.log(error);
            });
        }
      }
    })
  </script>
</body>
```

> [자바스크립트 비동기 처리와 콜백 함수](https://joshua1988.github.io/web-development/javascript/javascript-asynchronous-operation/)
> [자바스크립트 Promise 쉽게 이해하기](https://joshua1988.github.io/web-development/javascript/promise-for-beginners/)
> [자바스크립트 async와 await](https://joshua1988.github.io/web-development/javascript/js-async-await/)
> [{JSON} Placeholder](https://jsonplaceholder.typicode.com/)
> [자바스크립트의 동작원리: 엔진, 런타임, 호출 스택](https://joshua1988.github.io/web-development/translation/javascript/how-js-works-inside-engine/)
> [HTTP 프로토콜 Part 1](https://joshua1988.github.io/web-development/http-part1/)
> [Chrome DevTools](https://developer.chrome.com/docs/devtools/)

## Template Syntax

**뷰로 화면을 조작하는 방법**

- 템플릿 문법은 크게 데이터 바인딩과 디렉티브로 나뉨

**데이터 바인딩**

- 뷰 인스턴스에서 정의한 속성들을 화면에 표시하는 방법

```html
<div>{{ message }}</div>
```

```javascript
new Vue({
  data: {
    message: 'Hello Vue.js'
  }
})
```

**디렉티브**

- 뷰로 화면의 요소를 더 쉽게 조작하기 위한 문법
- v-if, v-for, v-bind, v-on, v-model ...

```html
<div id="app">
  <p>{{ doubleNum }}</p>

  Hello <span v-if="show">Vue.js</span>

  <ul>
    <li v-for="item in items">{{ item }}</li>
  </ul>

  <!-- id에 인스턴스의 data-uuid를 연결 -->
  <p v-bind:id="uuid" v-bind:class="name">{{ num }}</p>

  <!-- v-if는 DOM을 완전히 제거하고,v-show는 display: none -->
  <div v-if="loading">
    Loading...
  </div>
  <div v-else>
    test user has been logged in
  </div>

  <div v-show="loading">
    Loading...
  </div>

  <!-- v-model은 양방향 데이터 바인딩을 지원하는 속성 -->
  <input type="text" v-model="message">
      <p>{{ message }}</p>
  </div>

  <!-- v-on은 이벤트 발생 속성 -->
  <button v-on:click="logText">click me</button>
  <input type="text" v-on:keyup.enter="logText">
```

```javascript
new Vue({
  el: '#app'
  data: {
    num: 10,
    uuid: 'aaron1234'
    namd: 'text-navy'
    loading: false,
    show: true,
    items: ['shirts', 'jeans', 'hats']
    message: ''
  },
  computed: {
    doubleNum: function() {
      return this.num * 2;
    }
  },
  methods: {
    logText: function() {
      console.log('clicked');
    }
  }
})
```

## Watch

**특정 데이터의 변화를 감지하여 자동으로 특정 로직을 수행해주는 속성**

```html
<body>
  <div id="app">
    {{ num }}
    <button v-on:click="addNum">increase</button>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
  <script>
    new Vue({
      el: '#app',
      data: {
        num: 10
      },
      watch: {
        num: function() { // num이 변경되면 logText 메서드 실행
          this.logText();
        }
      },
      methods: {
        addNum: function() {
          this.num = this.num + 1;
        },
        logText: function() {
          console.log('changed');
        }
      }
    })
  </script>
</body>
```
### VS Computed

- watch
  - (매번 실행되기 부담스러운) 무거운 로직에 주로 사용
  - ex. 데이터 요청에 적합
- computed
  - 단순한 값 계산에 주로 사용
  - ex. validation, 간단한 텍스트, 연산에 적합

```js
computed: {
  doubleNum: function() {
    return this.num * 2;
  }
},
watch: {
  num: function(newValue, oldValue) {
    this.fetchUserByNumber(newValue);
  }
},
methods: {
  fetchUserByNumber: function(num) {
    // console.log(num);
    axios.get(num);
  }
}
```

## Reference

> [Vue.js 입문서](https://joshua1988.github.io/web-development/vuejs/vuejs-tutorial-for-beginner/#vuejs%EB%9E%80-%EB%AC%B4%EC%97%87%EC%9D%B8%EA%B0%80)
> 
> [Cracking Vue.js](https://joshua1988.github.io/vue-camp/textbook.html)
> 
> [vuejs.org](https://vuejs.org/)