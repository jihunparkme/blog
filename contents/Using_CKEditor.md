# Using CKEditor

CKEditor 적용 및 이미지 업로드

- Spring Boot
- Thymeleaf

[CKEditor Official Homepage](https://ckeditor.com/)

## Download JS File

[CKEditor4 Download](https://ckeditor.com/ckeditor-4/download/)

[CKEditor4 Custome Download](https://ckeditor.com/cke4/builder)

- 개인 선호에 맞게 plugins, skin, 언어 추가 가능

## Add JS File

zip 파일 압축 해제 후 `resource/static/js` 폴더 내로 이동

## Add CKEditor Skin

skin을 추가할 경우 [CKEditor 4 Skins](https://ckeditor.com/cke4/addons/skins/all?sort_by=field_custom_downloads_value&sort_order=DESC&page=0)에서 마음에 드는 skin을 다운로드 후 `resource/static/js/ckeditor/skins` 폴더 내에 추가해주자.

## Set CKEditor

**script import**

```html
<script src="/js/ckeditor/ckeditor.js"></script>
```

**set CKEDITOR**

- `id`에는 CKEditor를 적용할 textarea id를 작성해주자.
- 그 후 필요에 따라 [Config options](https://ckeditor.com/docs/ckeditor4/latest/api/CKEDITOR_config.html)를 추가해주면 된다.

```javascript
CKEDITOR.replace(id, {
  filebrowserUploadUrl: '/common/ckeditor/fileUpload',
  font_names:
    '맑은 고딕/Malgun Gothic;굴림/Gulim;돋움/Dotum;바탕/Batang;궁서/Gungsuh;Arial/Arial;Comic Sans MS/Comic Sans MS;Courier New/Courier New;Georgia/Georgia;Lucida Sans Unicode/Lucida Sans Unicode;Tahoma/Tahoma;Times New Roman/Times New Roman;MS Mincho/MS Mincho;Trebuchet MS/Trebuchet MS;Verdana/Verdana',
  font_defaultLabel: '맑은 고딕/Malgun Gothic',
  fontSize_defaultLabel: '12',
  skin: 'office2013',
  language: 'ko',
});
```

## View

**editGallery.java**

```html
 <!-- ... -->
<div class="container">
    <form id="form" enctype="multipart/form-data" th:object="${gallery}" onsubmit="return false">
    	<input type="hidden" id="id" th:if="*{id != null and id > 0}" th:field="*{id}" />
        <!-- ... -->
        <div class="form-group row">
            <label for="contents" class="col-sm-2 col-form-label"><strong>내용</strong></label>
            <div class="col-sm-10">
                <textarea name="contents" class="form-control" id="contents">[[*{contents}]]</textarea>
            </div>
        </div>
        <!-- ... -->
        <div class="row mt-3">
            <div class="col-auto mr-auto"></div>
            <div class="col-auto">
            	<button id="btnSave" class="btn btn-primary">저장</button>
            	<button id="btnCancel" class="btn btn-light">취소</button>
            </div>
        </div>
    </form>
</div>
<script src="/js/jquery-3.6.0.min.js"></script>
<script src="/js/bootstrap.min.js"></script>
<script src="/js/ckeditor/ckeditor.js"></script>
<script type="text/javascript" th:inline="javascript">
/*<![CDATA[*/
$(function() {

	CKEDITOR.replace('contents',{
		filebrowserUploadUrl: '/common/ckeditor/fileUpload',
		font_names : "맑은 고딕/Malgun Gothic;굴림/Gulim;돋움/Dotum;바탕/Batang;궁서/Gungsuh;Arial/Arial;Comic Sans MS/Comic Sans MS;Courier New/Courier New;Georgia/Georgia;Lucida Sans Unicode/Lucida Sans Unicode;Tahoma/Tahoma;Times New Roman/Times New Roman;MS Mincho/MS Mincho;Trebuchet MS/Trebuchet MS;Verdana/Verdana",
		font_defaultLabel : "맑은 고딕/Malgun Gothic",
		fontSize_defaultLabel : "12",
		skin : "office2013",
		language : "ko"
	});

	// ...
});

// ...

function saveGallery() {

	if (!confirm("저장하시겠습니까?")) {
		return;
	}

 	var formData = new FormData($("form")[0]);

	formData.append("deleteFiles", deleteFileList);
	formData.set("contents", CKEDITOR.instances.contents.getData());

	for (var i = 0; i < inputFileList.length; i++) {
		if(!inputFileList[i].is_delete){
			 formData.append("files", inputFileList[i]);
		}
	}

	for (var pair of formData.entries()) {
		console.log(pair[0]+ ', ' + pair[1]);
	}

	$.ajax({
		type : "POST",
		enctype : "multipart/form-data",
		url : "/gallery",
		data : formData,
		dataType:"json",
		processData : false,
		contentType : false,
		success : function(result) {
			if (result.response == "OK") {
				if ($("#id").val() == undefined) {
					alert("저장되었습니다.");
				} else {
					alert("수정되었습니다.");
				}

				location.href = "/gallery/" + result.galleryId;
			} else {
				alert(result.errorMsg);
			}
		},
	});
}
/*]]>*/
</script>
</body>
</html>
```

**viewGallery.html**

```html
<!-- ... -->
<div class="ckeditor_contents">
  <p class="card-text" th:utext="${gallery.contents}"></p>
</div>
<!-- ... -->
```

## Controller

**CKEditorController.java**

```java
@Controller
@RequestMapping("/common")
public class CKEditorController {

	/**
	 * CKEditor에서 파일 선택 후 서버로 전송 시 처리
	 *
	 * @param response
	 * @param multipartRequest
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/ckeditor/fileUpload")
	public String fileUploadFromCKEditor(HttpServletResponse response, MultipartHttpServletRequest multipartRequest) throws Exception {

		PrintWriter printWriter = null;

		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		try {
			String fileName = FileUtilities.uploadCKEditorFile(multipartRequest.getFile("upload"), "files/ckeditor");
			String fileUrl = "/common/ckeditor/fileDownload?fileName=" + fileName;

			printWriter = response.getWriter();
			// 서버로 파일 전송 후 이미지 정보 확인을 위해 filename, uploaded, fileUrl 정보를 response 해주어야 함
			printWriter.println("{\"filename\" : \"" + fileName + "\", \"uploaded\" : 1, \"url\":\"" + fileUrl + "\"}");
			printWriter.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (printWriter != null) {
				printWriter.close();
			}
		}

		return null;
	}

	/**
	 * 파일 업로드 후 이미지 정보, Editor 화면에 이미지 출력 처리
	 *
	 * @param fileName
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping("/ckeditor/fileDownload")
    public void ckSubmit(@RequestParam(value="fileName") String fileName,
                            HttpServletRequest request, HttpServletResponse response) {

		File file = FileUtilities.getDownloadFile(fileName, "files/ckeditor");

		try {
			byte[] data = FileUtils.readFileToByteArray(file);

			response.setContentType(FileUtilities.getMediaType(fileName).toString());
			response.setContentLength(data.length);
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader("Content-Disposition", "attachment; fileName=\"" + URLEncoder.encode(fileName, "UTF-8") + "\";");

			response.getOutputStream().write(data);
			response.getOutputStream().flush();
			response.getOutputStream().close();

		} catch (IOException e) {
			throw new RuntimeException("파일 다운로드에 실패하였습니다.");
		} catch (Exception e) {
			throw new RuntimeException("시스템에 문제가 발생하였습니다.");
		}
    }
}
```

## File Util

**FileUtilities.java**

```JAVA
public class FileUtilities {

	// Paths.get()으로 운영체제에 따라서 다른 파일구분자 처리
	public final static String rootPath = Paths.get("C:", "Users", "jihun.park", "Desktop", "testFile").toString();

	// ...

	public static String uploadCKEditorFile(MultipartFile multipartFile, String subPath) throws UnsupportedEncodingException, NoSuchAlgorithmException {

		// 파일 업로드 경로 생성
		String savePath = Paths.get(rootPath, subPath).toString();
		verifyUploadPath(savePath);

		String origFilename = multipartFile.getOriginalFilename();
		if (origFilename == null || "".equals(origFilename)) return null;

		String filename = getUuidFileName(origFilename);
		String filePath = Paths.get(savePath, filename).toString();

		try {
			File file = new File(filePath);

			// 파일 권한 설정(쓰기, 읽기)
			file.setWritable(true);
			file.setReadable(true);

			multipartFile.transferTo(file);

		} catch (IOException e) {
			throw new FileException("[" + multipartFile.getOriginalFilename() + "] failed to save file...");

		} catch (Exception e) {
			throw new FileException("[" + multipartFile.getOriginalFilename() + "] failed to save file...");
		}

		return filename;
	}

	private static void verifyUploadPath(String path) {

		if (!new File(path).exists()) {
			try {
				new File(path).mkdir();
			} catch (Exception e) {
				e.getStackTrace();
			}
		}
	}

	/**
	 * 다운로드 받을 파일 생성
	 *
	 * @param attach
	 */
	public static File getDownloadFile(String filaName, String subPath) {

		return new File(Paths.get(rootPath, subPath).toString(), filaName);
	}

	/**
	 * 파일명 중복 방지를 위해 UUID 파일명 생성
	 *
	 * @param filename
	 * @return
	 */
	public static String getUuidFileName(String filename) {

		UUID uuid = UUID.randomUUID();
		StringBuilder sb = new StringBuilder();
		sb.append(FilenameUtils.getBaseName(filename))
			.append("_").append(uuid).append(".").append(FilenameUtils.getExtension(filename));

		return  sb.toString();
	}

    // ...

	/**
	 * MediaType 생성
	 *
	 * @param filename
	 */
	public static MediaType getMediaType(String filename) {

		String contentType = FilenameUtils.getExtension(filename);
		MediaType mediaType = null;

		if (contentType.equals("png")) {
			mediaType = MediaType.IMAGE_PNG;
		} else if (contentType.equals("jpeg") || contentType.equals("jpg")) {
			mediaType = MediaType.IMAGE_JPEG;
		} else if (contentType.equals("gif")) {
			mediaType = MediaType.IMAGE_GIF;
		}

		return mediaType;
	}
}
```

---

## Result

## ![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/CKEditor.jpg 'Result')

## Reference

> [https://mine-it-record.tistory.com/277](https://mine-it-record.tistory.com/277)

---

## Project Code

> [Github](https://github.com/jihunparkme/blog/tree/main/projects/Using_CKEditor/demo)
