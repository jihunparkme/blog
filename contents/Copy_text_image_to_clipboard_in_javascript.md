# Copy text, image to clipboard in javascript

웹 개발에서 종종 사용되는 텍스트, 이미지를 클립보드에 복사하는 방법을 기록해보았다.

## Copy Text

```javascript
const tempArea = document.createElement("textarea"); // 임시 element 생성
document.body.appendChild(tempArea);
tempArea.value = $("#copyTextId").val(); // 복사할 영역의 값 저장
tempArea.select();
document.execCommand("copy");
document.body.removeChild(tempArea);
```

---

## Copy Image

이렇게 간단하게 이미지를 클립보드에 복사하는 코드가 많이 없었는데.. (심지에 안 되는 코드가 대부분이었다...)

But..!! 우여곡절 끝에 찾아냈다. Haha~!

Thank you for newbedev

```javascript
window.getSelection().removeAllRanges();
let range = document.createRange();
range.selectNode(document.getElementById("copyImg")); // 복사할 이미지 영역 선택
window.getSelection().addRange(range);
document.execCommand('copy');
window.getSelection().removeAllRanges();
```

> <https://newbedev.com/copy-image-to-clipboard-from-browser-in-javascript>