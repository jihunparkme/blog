# Vue

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

## Reference

> [Vue.js 입문서](https://joshua1988.github.io/web-development/vuejs/vuejs-tutorial-for-beginner/#vuejs%EB%9E%80-%EB%AC%B4%EC%97%87%EC%9D%B8%EA%B0%80)
> 
> [Cracking Vue.js](https://joshua1988.github.io/vue-camp/textbook.html)