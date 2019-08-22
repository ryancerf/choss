
# Choss - Use compiled code in your docs.

Choss allows you to include compiled code snippets in your markdown files. Nothing more.

## Getting Started

### Setup (Maven)

#### Not available yet.

### Write documentation.

Write your markdown files in `./docs/main/`.

#### Add Snippet Directives

Snippet Directives will pull code snippets from other files into your
markdown files.

`./docs/main/http.md`
```md
### Making http call

```java
 @@snip [http_snippet](./docs/src/main/resources/http.java)
``
```

Snippet directives are in the form `@@snip [snippet_tag](./relative/pathto/file.java)`

#### Tag snippet blocks in source files.

`./docs/src/main/resources/http.java`

```java
public class main {
  public static void Main(String[] args) {
    // @@ http_snippet
    URL github = new URL("http://www.github.com/");
    // @@ http_snippet
  }
}
```

snippet blocks are comments in form // @@ my_snippet_identifeir

<br>

#### Run The Plugin

`mvn com.github.ryancerf:choss-maven-plugin:build-docs`

It will copy ALL files in `./docs/main/` to `./target/docs/main` replacing the snippet directives in
the markdown files with the code blocks in the source files.

Admire the results in `./target/docs/main/http.md `

```md
### Making http call

```java
URL yahoo = new URL("http://www.github.com/");
``
```

## Details

Copies ALL files in the source docs directory whether they are content files or not.
Will only attempt to repalce snippets in content files (defaults to `.md` only)

### Snippet Directives(in .md)
* Any line with a snippet directive in a markdown file will be replaced in its entirety.
* Snippets Directives can pull code from any file in the project.
* Paths should be relative to the root of the project (where pom is located).
* If a the path in a snippet directive cannot be resolved, choss will fail loudly.

### Snippet Blocks In source files (.java)
* Each source file can only contain one snippet blocks with same tag.
* Snippet tags E.G. the `http_snippet` in @@ http_snippet must start with a letter and must
match the regex: `[A-Za-z][A-Za-z0-9_]*`
* If a snippet block does not have an ending block it will span to the next snippet block or
or to the end of the file.

### Indentation

Choss will try to remove unnecessary indentation. This allows you to include code from
inside methods, or nested classes without the unnecessary indentation showing up in the snippet.

### Paramaters
* Can set `docsSourceDirectory` which is the directory that contains your docs which will be copied.
 Defaults to `./docs/main/`
* Can set `docsTargetDirectory` which is the directory where the docs will be copied to.
Defaults to `./target/docs/main` (Warning the plugin will empty the contents of the target directory)
* Can set `contentGlob` which is the glob of files that are considered content. Defaults to `glob:**/*.md`.


You can set these parameters in the pom.xml using the `<configuration>` tag.

```xml
<plugin>
  <groupId>com.github.ryancerf</groupId>
  <artifactId>choss-maven-plugin</artifactId>
  <version>master-SNAPSHOT</version>
<!--Add configuraton to set docsSourceDirectory-->
  <configuration>
    <docsSourceDirectory>./documentation/custom/</docsSourceDirectory>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>build-docs</goal>
      </goals>
    </execution>
  </executions>
</plugin>

```


### Previous Art
[Paradox](https://github.com/lightbend/paradox/) is way better, but this might
be more convenient for maven users.
