# Java Write a text file

## Files

- [Paths.get()](<https://docs.oracle.com/javase/8/docs/api/java/nio/file/Paths.html#get(java.net.URI)>)

  - 결과 파일 저장 경로

  - 주어진 URI를 Path 객체로 변환

    ```java
    public static Path get(URI uri)
    ```

- [Files.deleteIfExists()](<https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html#deleteIfExists(java.nio.file.Path)>)

  - 해당 경로에 파일이 존재할 경우 삭제

    ```java
    public static boolean deleteIfExists(Path path) throws IOException
    ```

- [Files.write()](<https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html#write(java.nio.file.Path,%20java.lang.Iterable,%20java.nio.charset.Charset,%20java.nio.file.OpenOption...)>)

  - 파일에 텍스트 작성 (각 줄은 char sequence)

  - 줄 구분 기호로 끝나는 각 줄을 사용하여 파일에 순서대로 기록

  - 문자는 지정된 문자 집합을 사용하여 바이트로 인코딩

    ```java
    public static Path write(Path path,
             Iterable<? extends CharSequence> lines,
             Charset cs,
             OpenOption... options) throws IOException
    ```

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

	public static void main(String[] args) throws IOException {

		// result file path
		Path file = Paths.get("C:\\Users\\cristoval\\Desktop\\test\\java_test.txt");

		// 해당 경로에 파일이 존재할 경우 삭제
		Files.deleteIfExists(file);

		// 줄 구분 기호로 끝나는 각 줄을 사용하여 파일에 순서대로 기록(문자는 지정된 문자 집합을 사용하여 바이트로 인코딩)
		Files.write(file, "".getBytes(), StandardOpenOption.CREATE);

		List<String> datas = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			datas.add(Integer.toString(i));
		}

		Files.write(file,
				String.format("%s\n", datas.stream()
									.collect(Collectors.joining("\n"))).getBytes(),
							StandardOpenOption.APPEND);
	}
}
```

## FileWriter

- [FileWriter class](https://docs.oracle.com/javase/8/docs/api/java/io/FileWriter.html) 는 `java.io.OutputStreamWriter`, `java.io.Writer` class를 상속받아서, OutputStreamWriter, Writer class 에서 제공하는 write() methods 를 사용할 수 있다.

```java
import java.io.FileWriter;

public class Main {

	static String path = "C:\\Users\\cristoval\\Desktop\\test\\java_test.txt";

	public static void main(String[] args) {

		try (
				FileWriter fw = new FileWriter(path);
			){
			for (int i = 0; i < 100; i++) {
				fw.write(Integer.toString(i) + '\n');
			}

			System.out.println("Successfully wrote to the file.");
		} catch (Exception e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
}
```

> <https://www.w3schools.com/java/java_files_create.asp>

## BufferedWriter

- [BufferedWriter class](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedWriter.html) 는 `java.io.Writer` class를 상속받아서, Writer class 에서 제공하는 write() methods 를 사용할 수 있다.

```java
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

	static String path = "C:\\Users\\cristoval\\Desktop\\test\\java_test.txt";

	public static void main(String[] args) {

		File file = new File(path);

		try (
				BufferedWriter bw = new BufferedWriter(new FileWriter(file))
			) {
		    for (int i = 0; i < 100; i++) {
				bw.write(Integer.toString(i) + '\n');
				//bw.append(Integer.toString(i) + '\n');
			}

		    System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
		    e.printStackTrace();
		}
	}
}

```
